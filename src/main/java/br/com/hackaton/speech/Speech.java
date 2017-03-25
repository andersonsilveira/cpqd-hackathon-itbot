package br.com.hackaton.speech;

import java.io.*;
import java.net.*;

import javax.net.ssl.HttpsURLConnection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * 
 */
public class Speech {

	private static Boolean state = false;

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
		AudioFormat format = new AudioFormat(16000, 16, 1, true, false);	    
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
	public void toText(String audioPath) throws Exception{
		
	/*	URL request = new URL("https://speechweb.cpqd.com.br/spider/recognize");
		HttpsURLConnection conn = (HttpsURLConnection) request.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("Content-Type", "audio/wav");

		String authString = new String(Base64.getEncoder().encode("hackathon:1234".getBytes()));
		conn.setRequestProperty("Authorization", "Basic " + authString);
		conn.connect();
		
		// Envia o audio
		OutputStream out = conn.getOutputStream();

		FileInputStream fin = new FileInputStream("C:\\hackathon\\som_teste.wav");
		byte[] buffer = new byte[8000];
		int len = -1;
		while ((len = fin.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}

		// Recupera o resultado do reconhecimento
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line = null;

		while ((line = in.readLine()) != null) {
			System.out.println(line);
		}	*/	
	}
	
	/**
	 * Recebe um texto e retorna e tranforma em audio
	 * @param text
	 * @throws Exception
	 */
	public void toAudio(String text) throws Exception{
		/*String authString = new String(Base64.getEncoder().encode("hackathon:1234".getBytes()));

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
		System.out.println("Tocou");*/
	}

    /**
     * Metodo main para testes
     * @param args
     * @throws Exception
     */
    public static void main3(String[] args) throws Exception {
    	String text = "Boa Noite, posso ajudar?";
    	
    	Speech speech = new Speech();
    	//speech.toAudio(text);
    	
    	speech.toText("bla");
		
	}
}
