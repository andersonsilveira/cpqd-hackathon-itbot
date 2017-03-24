package br.com.hackaton.jira;

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
import javax.naming.AuthenticationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public class JiraService {
	
   private static final String JIRA_URL = "https://jira.cpqd.com.br";
   private static final String CREATE_ISSUE = "/rest/api/2/issue/";
   
   private String username;
   private String password;
   
   JiraService(String username, String password){
	   this.setUsername(username);
	   this.setPassword(password);
   }  
 
	
	public String createIssue(String data) throws AuthenticationException, ClientHandlerException {
	    
		String url = JIRA_URL + CREATE_ISSUE;
		String auth = getEncodedCredentials();
		
		Client client = Client.create();
	    WebResource webResource = client.resource(url);
	    ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json").accept("application/json").post(ClientResponse.class, data);
	    int statusCode = response.getStatus();
	    if (statusCode == 401) {
	        throw new AuthenticationException("Invalid Username or Password");
	    }
	    return response.getEntity(String.class);
	}
	
	public boolean addAttachmentToIssue(String issueKey, String fileContent) throws IOException{
		 
	    CloseableHttpClient httpclient = HttpClients.createDefault();
	     
	    HttpPost httppost = new HttpPost(JIRA_URL + "/rest/api/2/issue/" + issueKey + "/attachments");
	    httppost.setHeader("X-Atlassian-Token", "nocheck");
	    httppost.setHeader("Authorization", "Basic "+ getEncodedCredentials());
	    
	    //https://jira.cpqd.com.br/rest/api/2/issue/HACK-13/attachments
	     
		File file = File.createTempFile("chat", ".txt");					      
	    FileWriter fileWriter = new FileWriter(file);  
	    fileWriter.write(fileContent);
	    fileWriter.close();  
	    		
	    FileBody fileBody = new FileBody(file);
	     
	    HttpEntity entity = MultipartEntityBuilder.create().addPart("file", fileBody).build();
	     
	    httppost.setEntity(entity);
	     
	    CloseableHttpResponse response;
	     
	    try {
	        response = httpclient.execute(httppost);
	    } finally {
	        httpclient.close();
	    }
	     
	    System.out.println(response.getStatusLine());
	    if(response.getStatusLine().getStatusCode() == 200)
	        return true;
	    else
	        return false;
	 
	}
	
	private String getEncodedCredentials() {
		return "ZGllZ29jOipENjY3ODEyYyo=";
		//return new String(Base64.encode(this.getUsername() + ":" + this.getPassword()));
	}

	public void validateUser(String username, String password) throws AuthenticationException, ClientHandlerException {
	    
		String auth = getEncodedCredentials();
		
		Client client = Client.create();
	    WebResource webResource = client.resource(JIRA_URL);
	    ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json").accept("application/json").get(ClientResponse.class);
	    int statusCode = response.getStatus();
	    
	    System.out.println("Status Code: " + statusCode);
	   
	    if (statusCode == 401) {
	        throw new AuthenticationException("Invalid Username or Password");
	    }	    
	}  
	
	public String getUserMail() throws Exception {
	    
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI uri = new URI(JIRA_URL);

		JiraRestClient client = factory.createWithBasicHttpAuthentication(uri, this.getUsername(), this.getPassword());				
		Promise<User> promise = client.getUserClient().getUser(this.getUsername());
		User jiraUser = promise.claim();
		
		if(jiraUser != null){
			return jiraUser.getEmailAddress();
		}
				
		return "";
	}
	 
    public List<Issue> getUserIssues() throws Exception {
	    
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI uri = new URI(JIRA_URL);

		JiraRestClient client = factory.createWithBasicHttpAuthentication(uri, this.getUsername(), this.getPassword());				
		
		SearchRestClient issueSearch = client.getSearchClient();
		Promise<SearchResult> result = issueSearch.searchJql("reporter = " + this.getUsername() + "S AND project = HACK");
		
		SearchResult sr = result.claim();		
		Iterable<BasicIssue> basicIssues = sr.getIssues();
		
		IssueRestClient issueClient = client.getIssueClient();
	
		List<Issue> issues = new ArrayList<Issue>();
		
		for(BasicIssue i : basicIssues){
			Promise<Issue> promiseIssue = issueClient.getIssue(i.getKey());
			Issue issue = promiseIssue.claim();
					
			System.out.println(i.getKey());
			System.out.println(i.getSelf());
			System.out.println(issue.getStatus().getName());
			
			issues.add(issue);
		}
		
		return issues;
	}
	
	public void sendMail(String fileContent) throws Exception {
		//String to = this.getUserMail();
		String to = "diegoconstantini@hotmail.com";
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

			// Part two is attachment
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

			System.out.println("Sent message successfully....");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	 public static void main2(String[] args) throws Exception {
			
			String auth = "ZGllZ29jOipENjY3ODEyYyo="; //(diegoc user + senha encodado)
			String data = "{\"fields\":{\"project\":{\"key\":\"HACK\"},\"summary\":\"REST Test\",\"issuetype\":{\"name\":\"Incidente\"}}}";
			//System.out.println(data);
				
			//String response = createIssue(auth, url, data);
			//validateUser("bla", "bla");
			
			JiraService app = new JiraService("bla", "bla");
			//app.addAttachmentToIssue("HACK-13", "Conversa legal");
			//app.sendMail("bla");
			
			
			app.getUserIssues();
						
			//String response = app.getUserMail();
			
			//String response = "{\"id\":\"1840987\",\"key\":\"HACK-13\",\"self\":\"https://jira.cpqd.com.br/rest/api/2/issue/1840987\"}";
			//System.out.println(response);
			
			//JSONArray projectArray = new JSONArray(response);
			//for (int i = 0; i < projectArray.length(); i++) {
			  //  JSONObject proj = projectArray.getJSONObject(i);
			    //System.out.println("Key:"+proj.getString("key")+", Name:"+proj.getString("name"));
			//}       	      
		}
}