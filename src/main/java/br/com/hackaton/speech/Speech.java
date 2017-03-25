package br.com.hackaton.speech;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.core.util.Base64;

/**
 * 
 */
public class Speech {

	private static Boolean state = false;
	private static String RECOGNIZED = "RECOGNIZED";
	private static String SORRY_TEXT = "Desculpe, não entendi";
	private static String BEST_SCORE = "100";
	
	/**
	 * DJ - Toca o som
	 * @param stream
	 * @throws LineUnavailableException
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void playSoundStream(InputStream stream) throws LineUnavailableException, UnsupportedAudioFileException, IOException, InterruptedException {
		// reset the flag
		state = false;
		
		Clip clip = AudioSystem.getClip();
		AudioInputStream inputStream = AudioSystem.getAudioInputStream(stream);
		clip.open(inputStream);

		LineListener listener = new LineListener() {
			public void update(LineEvent event){
				if (event.getType() != Type.STOP) {
					return;
				}

				// activate the flag if the audio stopped 
				synchronized (state) { state = true; }
			}
		};

		clip.addLineListener(listener );
		clip.start();

		// wait while playing the audio
		while (true){
			Thread.sleep(10);
			synchronized (state){
				if (state) return;
			}
			System.out.println("Waiting...");
		}
	}
	
	/**
	 * Recebe o caminho de um arquivo de audio e retorna a string
	 * @throws Exception
	 */
	public String toText(String audioPath) throws Exception{
		
		URL request = new URL("https://speechweb.cpqd.com.br/spider/recognize");
		HttpsURLConnection conn = (HttpsURLConnection) request.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("Content-Type", "audio/wav");

		String authString = new String(Base64.encode("hackathon:1234"));
		conn.setRequestProperty("Authorization", "Basic " + authString);
		conn.connect();
		
		// Envia o áudio
		OutputStream out = conn.getOutputStream();
		@SuppressWarnings("resource")
		FileInputStream fin = new FileInputStream(audioPath);
		byte[] buffer = new byte[8000];
		int len = -1;
		while ((len = fin.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}
		
		// Recupera o resultado do reconhecimento
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line = null;
		StringBuilder builder = new StringBuilder();

		while ((line = in.readLine()) != null) {
			builder.append(line);
		}
				
		String jsonResult = builder.toString();
		if(recognized(jsonResult)){
			return getText(jsonResult);
		} 
		
		return SORRY_TEXT;
	}
	
	/**
	 * @param responseJson
	 * @return
	 */
	private String getText(String responseJson) {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode;
		
		try {
			rootNode = objectMapper.readTree(new ByteArrayInputStream(responseJson.getBytes("UTF-8")));
			JsonNode contextNode = rootNode.path("alternatives");
			
			if(contextNode != null){
				
				Iterator<JsonNode> it = contextNode.iterator();
				while (it.hasNext()) {
					ObjectNode foundItem = (ObjectNode)it.next();
					JsonNode scoreNode = foundItem.get("score");
					
					if(scoreNode != null){
						String score = scoreNode.toString().replaceAll("\"", "");
						
						if(score.equals(BEST_SCORE)){
							JsonNode textNode = foundItem.get("text");
							String text = textNode.toString().replaceAll("\"", "");
							return text;
						}
					}
				}				
				return SORRY_TEXT;
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Desculpe, não entendi.";
	}
	
	/**
	 * @param responseJson
	 * @return
	 */
	private boolean recognized(String responseJson) {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode;
		
		try {
			rootNode = objectMapper.readTree(new ByteArrayInputStream(responseJson.getBytes("UTF-8")));
			JsonNode contextNode = rootNode.path("result-status");
			
			if(contextNode != null){
				String result = contextNode.toString().replaceAll("\"", "");
				if(RECOGNIZED.equals(result)){
					return true;
				}
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Recebe um texto e retorna e tranforma em audio
	 * @param text
	 * @throws Exception
	 */
	public void toAudio(String text) throws Exception{
		String authString = new String(Base64.encode("hackathon:1234"));

		StringBuilder sb = new StringBuilder();
		sb.append("?text=").append(URLEncoder.encode(text, "UTF-8")).append("&voice=rosana-highquality");
		URL request = new URL("https://vaas34.cpqd.com.br/rest/textToSpeech" + sb.toString());

		// request the speech synthesis
		HttpsURLConnection conn = (HttpsURLConnection) request.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Authorization", "Basic " + authString);
		conn.connect();

		// parse the REST response
		String response = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line = null;
		while ((line = in.readLine()) != null) {
			response += line;
		}		
		
		int begin = response.indexOf("<url>");
		
		if (begin >= 0){	
			int end = response.indexOf("</url>");
			response = response.substring(begin + 5, end);
		}

		// download the audio file
		System.out.println(response);
		request = new URL(response);
		conn = (HttpsURLConnection) request.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Authorization", "Basic " + authString);
		conn.connect();

		// play the audio synchronously
		playSoundStream(conn.getInputStream());
		System.out.println("Tocou");
	}

    /**
     * Metodo main para testes
     * @param args
     * @throws Exception
     */
    public static void main3(String[] args) throws Exception {
    	    	
    	Speech speech = new Speech();
    	
    	String text = "Boa Noite, posso ajudar?";
    	//speech.toAudio(text);
    	
    	String audioPath = "c:\\hackathon\\som_teste.wav";
    	String textResult = speech.toText(audioPath);	
    	System.out.println("Texto retornado: " + textResult);
	}
}
