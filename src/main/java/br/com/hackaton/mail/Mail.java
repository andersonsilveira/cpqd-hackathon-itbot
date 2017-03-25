package src.main.java.br.com.hackaton.mail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
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

public class Mail {
	
	/**
	 * Envia o email com a conversa para o usuario
	 * @param fileContent
	 * @throws Exception
	 */
	public void send(String to, String attachment) throws Exception {
		
		String from = "ac885565a7-a9d626@inbox.mailtrap.io";
		
		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");	
		props.put("mail.smtp.host", "smtp.mailtrap.io");
		props.put("mail.smtp.port", "25");
		
		final String username = "a52f59b3a5589b";
		final String password = "c5948e3fe67c47";
		
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			Message message = new MimeMessage(session);
     		message.setFrom(new InternetAddress(from));

	    	message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject("Testing Subject");

			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText("This is message body");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// Parte dois eh o anexo do email
			messageBodyPart = new MimeBodyPart();
			
			File file = File.createTempFile("chat", ".txt");					      
		    FileWriter fileWriter = new FileWriter(file);  
		    fileWriter.write("conversa");
		    fileWriter.close();  
			
			DataSource source = new FileDataSource(file.getPath());
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(file.getName());
			multipart.addBodyPart(messageBodyPart);

			message.setContent(multipart);

			Transport.send(message);

			System.out.println("Email enviado com sucesso");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}