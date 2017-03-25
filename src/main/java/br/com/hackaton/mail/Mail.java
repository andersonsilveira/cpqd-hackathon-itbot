package br.com.hackaton.mail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import br.com.hackaton.form.Form;

/**
 *
 */
public class Mail {
	
	final String from = "hackathon.cpqd@gmail.com";
	final String pass = "cpqdcpqd";
	
	/**
	 * @param user
	 * @param userMail
	 * @param attachment
	 * @throws IOException
	 */
	public void sendConversation(String user, String userMail, List<String> attachment) throws IOException{
		
		Properties props = getProperties();
		Session session = getSession(props);
		//session.setDebug(true);
        
        try {

        	Message message = new MimeMessage(session);
     		message.setFrom(new InternetAddress(from));

	    	message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userMail));
			message.setSubject("HelpDesk - CPqD");

			BodyPart messageBodyPartLine = new MimeBodyPart();
			messageBodyPartLine.setText("");
			
			BodyPart messageBodyPartHi = new MimeBodyPart();
			messageBodyPartHi.setText("Olá " + user + "!");
									
			BodyPart messageBodyPart1 = new MimeBodyPart();
			messageBodyPart1.setText("Você está recebendo uma cópia do nosso atendimento.");
			
			BodyPart messageBodyPart2 = new MimeBodyPart();
			messageBodyPart2.setText("Qualquer dúvida estamos à disposição.");
			
			BodyPart messageBodyPartThanks = new MimeBodyPart();
			messageBodyPartThanks.setText("Equipe HelpDesk.");			
			
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPartHi);
			multipart.addBodyPart(messageBodyPartLine);			
			multipart.addBodyPart(messageBodyPart1);
			multipart.addBodyPart(messageBodyPartLine);
			multipart.addBodyPart(messageBodyPart2);
			multipart.addBodyPart(messageBodyPartLine);
			multipart.addBodyPart(messageBodyPartThanks);
			                   
			// Parte dois eh o anexo do email
			BodyPart attachmentPart = new MimeBodyPart();

			StringBuilder builder = new StringBuilder();
			for (String s : attachment) {
				builder.append(s);
				builder.append("\n");
			}
			    
			File file = File.createTempFile("chat", ".txt");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(builder.toString());
			fileWriter.close();

			DataSource source = new FileDataSource(file.getPath());
			attachmentPart.setDataHandler(new DataHandler(source));
			attachmentPart.setFileName(file.getName());
			multipart.addBodyPart(attachmentPart);

			message.setContent(multipart);

			/** Método para enviar a mensagem criada */
			Transport.send(message);
			//System.out.println("Feito!!!");
			
         } catch (MessagingException e) {
              throw new RuntimeException(e);
        }
	}

	/**
	 * @param props
	 * @return
	 */
	private Session getSession(Properties props) {
		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, pass);
			}
		});
		return session;
	}

	/**
	 * @return
	 */
	private Properties getProperties() {
		Properties props = new Properties();
        
		/** Parâmetros de conexão com servidor Gmail */
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
		return props;
	}	
	
	/**
	 * @param user
	 * @param userMail
	 * @param attachment
	 * @throws IOException
	 */
	public void sendForm(String user, String userMail) throws IOException{	
		
		Properties props = getProperties();
		Session session = getSession(props);
		//session.setDebug(true);
        
        try {

        	Message message = new MimeMessage(session);
     		message.setFrom(new InternetAddress(from));

	    	message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userMail));
			message.setSubject("HelpDesk - CPqD - Pesquisa de Satisfação");

			BodyPart messageBodyPartLine = new MimeBodyPart();
			messageBodyPartLine.setText("");
			
			BodyPart messageBodyPartHi = new MimeBodyPart();
			messageBodyPartHi.setText("Olá " + user + "!");
									
			BodyPart messageBodyPart1 = new MimeBodyPart();
			messageBodyPart1.setText("Você está recebendo um link com a nossa pesquisa de satisfação.");
			
			BodyPart messageBodyPart2 = new MimeBodyPart();
			messageBodyPart2.setText("Nos ajude a melhorar nosso atendimento respondendo a pesquisa. É rapidinho. =D ");
			
			BodyPart messageBodyPartLink = new MimeBodyPart();			
			Form form = new Form();
			messageBodyPartLink.setText(form.getLink());
			
			BodyPart messageBodyPartThanks = new MimeBodyPart();
			messageBodyPartThanks.setText("A equipe HelpDesk agradece!");			
			
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPartHi);
			multipart.addBodyPart(messageBodyPartLine);			
			multipart.addBodyPart(messageBodyPart1);
			multipart.addBodyPart(messageBodyPartLine);
			multipart.addBodyPart(messageBodyPart2);
			multipart.addBodyPart(messageBodyPartLine);
			multipart.addBodyPart(messageBodyPartLink);
			multipart.addBodyPart(messageBodyPartLine);
			multipart.addBodyPart(messageBodyPartThanks);

			message.setContent(multipart);

			/** Método para enviar a mensagem criada */
			Transport.send(message);
			//System.out.println("Feito!!!");
			
         } catch (MessagingException e) {
              throw new RuntimeException(e);
        }
	}	
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main1(String[] args) throws IOException {
		
		Mail mail = new Mail();
		//mail.sendConversation("", "lfmrocha88@gmail.com", "Oi gato");		
		mail.sendForm("Gustavao Pegador", "gbrandao@cpqd.com.br");
	}
}