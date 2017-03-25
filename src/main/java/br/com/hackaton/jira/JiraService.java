package br.com.hackaton.jira;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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
import com.atlassian.jira.rest.client.domain.input.IssueInput;
import com.atlassian.jira.rest.client.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;

/**
 *
 */
public class JiraService {
	
   private static final String JIRA_URL = "https://jira.cpqd.com.br";
   private static final String CREATE_ISSUE = "/rest/api/2/issue/";
   
   private String username;
   private String password;
   
	/**
	 * @param username
	 * @param password
	 */
	public JiraService(String username, String password) {
		this.setUsername(username);
		this.setPassword(password);
	} 
	public JiraService(){
	    
	}
	
	/**
	 * @param attachment
	 * @return
	 * @throws AuthenticationException
	 * @throws ClientHandlerException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public BasicIssue createIssue(IssueInput issueInput, List<String> attachment) throws AuthenticationException, ClientHandlerException, URISyntaxException, IOException {
	    
		JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
	    URI uri = new URI(JIRA_URL);

		JiraRestClient client = factory.createWithBasicHttpAuthentication(uri, this.getUsername(), this.getPassword());				
		IssueRestClient issueClient = client.getIssueClient();
		
		Promise<BasicIssue> promise = issueClient.createIssue(issueInput);	
		BasicIssue bi = promise.claim();
		
		System.out.println(bi.getKey());
		
		if(attachment != null){
			addAttachmentToIssue(bi.getKey(), attachment);
		}
		
		return bi;		
	}	
	
	/**
	 * Anexa um arquivo no jira
	 * @param issueKey
	 * @param attachment
	 * @return
	 * @throws IOException
	 */
	public boolean addAttachmentToIssue(String issueKey, List<String> attachment) throws IOException{
		 
	    CloseableHttpClient httpclient = HttpClients.createDefault();
	     
	    HttpPost httppost = new HttpPost(JIRA_URL + "/rest/api/2/issue/" + issueKey + "/attachments");
	    httppost.setHeader("X-Atlassian-Token", "nocheck");
	    httppost.setHeader("Authorization", "Basic "+ getEncodedCredentials());
	    
	    StringBuilder builder = new StringBuilder();
	    for(String s : attachment){
	    	builder.append(s);
	    	builder.append("\n");	    	
	    }
	    
		File file = File.createTempFile("chat", ".txt");					      
	    FileWriter fileWriter = new FileWriter(file);  
	    fileWriter.write(builder.toString());
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
	     
	    if(response.getStatusLine().getStatusCode() == 200){
	        return true;
	    } else {
	        return false;
	    }	 
	}
	
	/**
	 * @return
	 */
	private String getEncodedCredentials() {
		return new String(Base64.encode(this.getUsername() + ":" + this.getPassword()));
	}

	/**
	 * Valida se o usuario existe no cadastro do jira
	 * @param username
	 * @param password
	 * @throws AuthenticationException
	 * @throws ClientHandlerException
	 */
	public String validateUser(String username, String password) throws AuthenticationException, ClientHandlerException {
	    System.out.println("validade user:"+username+": "+password);
		String auth = new String(Base64.encode(username + ":" + password));
		
		Client client = Client.create();
	    WebResource webResource = client.resource(JIRA_URL);
	    ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json").accept("application/json").get(ClientResponse.class);
	    int statusCode = response.getStatus();
	    
	    if (statusCode == 401) {
	        throw new AuthenticationException("Invalid Username or Password");
	    }	
	    return auth;
	}  
	
	/**
	 * Retorna o email do usuario cadastro no jira
	 * @return
	 * @throws Exception
	 */
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
	 
    /**
     * Retorna todos os jiras do usuario no projeto HACK
     * @return
     * @throws Exception
     */
    public List<Issue> getUserIssues() throws Exception {
	    
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI uri = new URI(JIRA_URL);

		JiraRestClient client = factory.createWithBasicHttpAuthentication(uri, this.getUsername(), this.getPassword());				
		
		SearchRestClient issueSearch = client.getSearchClient();
		Promise<SearchResult> result = issueSearch.searchJql("reporter = " + this.getUsername() + " AND project = HACK");
		
		SearchResult sr = result.claim();		
		Iterable<BasicIssue> basicIssues = sr.getIssues();
		
		IssueRestClient issueClient = client.getIssueClient();
	
		List<Issue> issues = new ArrayList<Issue>();
		
		for(BasicIssue i : basicIssues){
			Promise<Issue> promiseIssue = issueClient.getIssue(i.getKey());
			Issue issue = promiseIssue.claim();
					
			System.out.println(issue.getKey());
			System.out.println(issue.getSelf());
			System.out.println(issue.getStatus().getName());
			
			issues.add(issue);
		}		
		return issues;
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
	
	 /**
	  * Metodo main para testes
	 * @param args
	 * @throws Exception
	 */
	public static void main2(String[] args) throws Exception {
			
			//validateUser("bla", "bla");
			
			JiraService jira = new JiraService("bla", "bla");
			//app.addAttachmentToIssue("HACK-13", "Conversa legal");
			
			String jiraType = "11200";		    			    	
	    	String jiraDescription = "Solicitação de instalação da Ferramenta GIT - cpqd037167";
	    	String jiraSummary = "Instalação da Ferramenta GIT";
	    	
	    	//abre um chamado	    		    	
	    	IssueInputBuilder issueBuilder = new IssueInputBuilder("HACK", new Long(jiraType));
			issueBuilder.setDescription(jiraDescription);
			issueBuilder.setSummary(jiraSummary);
			
			List<String> conversation = new ArrayList<String>();
			conversation.add("Oi");
			conversation.add("\n");
			conversation.add("Tchau");			
 							
	    	BasicIssue basicIssue = jira.createIssue(issueBuilder.build(), conversation);	    	
			System.out.println(basicIssue);
			
			//app.getUserIssues();
						
			//String response = app.getUserMail();			
			//String response = "{\"id\":\"1840987\",\"key\":\"HACK-13\",\"self\":\"https://jira.cpqd.com.br/rest/api/2/issue/1840987\"}";
			//System.out.println(response);//			
			     	      
		}
}