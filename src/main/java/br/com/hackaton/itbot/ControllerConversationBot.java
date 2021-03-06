package br.com.hackaton.itbot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.hackaton.jira.ConfluenceService;
import br.com.hackaton.jira.JiraService;
import br.com.hackaton.mail.Mail;

import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.input.IssueInputBuilder;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.sun.jersey.api.client.ClientHandlerException;

@Controller
public class ControllerConversationBot {

    	/**
     * 
     */
    private static final String PASSWORD = "htALYxSVSRVw";
	/**
     * 
     */
    private static final String USER = "6cd18a8a-bfd3-44ad-a14a-e23ce09605ab";
	/**
     * 
     */
    private static final String WORKSPACE = "afac64e0-4ee0-4e39-a3ea-75d0ae90e4cc";
	private static Map<String, Map<String, Object>> sessionMap = new HashMap<String, Map<String,Object>>();
	private static Map<String, List<String>> sessionConversationMap = new HashMap<String, List<String>>();
	private static Map<String, String> userConversation = new HashMap<String,String>();

	@RequestMapping(value="/conversation",method = RequestMethod.GET)
	public @ResponseBody WebhookResponse conversation(@RequestParam(name="user") String userParam, @RequestParam(name="text") String intentContent, @RequestParam(name="conversationId",required=false) String conversationId){
		ConversationService service = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
		service.setUsernameAndPassword(USER, PASSWORD);
		try {
			System.out.println(intentContent);
			System.out.println("conversationId "+conversationId);
			boolean isInitialContext = conversationId==null || conversationId.isEmpty() || conversationId.equals("\"0\"");
			MessageRequest newMessage = null;
			MessageResponse response = null;
			List<String> text = null;
			if(isInitialContext){
			        newMessage = new MessageRequest.Builder().inputText(intentContent).build();
			        response = service.message(WORKSPACE, newMessage).execute();
			        Map<String, Object> context = response.getContext();
			        System.out.println("contextoMpa "+context);
			        sessionMap.put(context.get("conversation_id").toString(), context);
			        System.out.println("Mapa 1 "+sessionMap.toString());
				System.out.println(newMessage);
				sessionMap.get(context.get("conversation_id").toString()).put("user", userParam);
				sessionMap.get(context.get("conversation_id").toString()).put("userMail", userParam+"@cpqd.com.br");
				System.out.println("senha: "  + userConversation.get(userParam)); 
				System.out.println("userConversation: " + userConversation);
				sessionMap.get(context.get("conversation_id").toString()).put("password", userConversation.get(userParam));
				text = response.getText();
			}else{
			       System.out.println("Mapa 2 "+sessionMap.toString());
			        newMessage = new MessageRequest.Builder().inputText(intentContent).context(sessionMap.get(conversationId)).build();
			        response = service.message(WORKSPACE, newMessage).execute();
			        sessionMap.put(conversationId, response.getContext());
			        text = response.getText();
			       	
			}

			String conversation_id = response.getContext().get("conversation_id").toString();
			
			if(!sessionConversationMap.containsKey(conversation_id)){
				sessionConversationMap.put(response.getContext().get("conversation_id").toString(), new ArrayList<String>());							
			} 
			
			sessionConversationMap.get(response.getContext().get("conversation_id")).add(response.getInputText());
			sessionConversationMap.get(response.getContext().get("conversation_id")).add(text.get(0));
			
		    System.out.println(response);
		    WebhookResponse webhookResponse = null;
		    if(response.getContext().containsKey("confluence_ctx")){
			String text1 = response.getText().get(0) + "\n" + getContentConfluence(response);
			String text2 = response.getText().get(1);
			ArrayList<String> texts = new ArrayList<String>();
			texts.add(text1);
			texts.add(text2);
				webhookResponse = new WebhookResponse(response.getInputText(),texts, response.getContext());
		    	response.getContext().remove("confluence_ctx");
		    } else if(response.getContext().containsKey("jira_ctx")){
		    	System.out.println("entrou no jira ctx");
		    	response.getContext().remove("jira_ctx");
		    	String user = sessionMap.get(conversationId).get("user").toString();
			System.out.println("usuario abri jira"+user);
		    	String password = sessionMap.get(conversationId).get("password").toString();
			System.out.println("senha jira" +password);
		    	
		    	//11200 - Solicitação de Serviço
		    	//46 = Incidente
		    	String jiraType = "11200";		    			    	
		    	String jiraDescription = "Solicitação de instalação da Ferramenta GIT - cpqd037167";
		    	String jiraSummary = "Instalação da Ferramenta GIT";
		    	
		    	//abre um chamado
		    	JiraService jira = new JiraService(user, password);	
		    	
		    	IssueInputBuilder issueBuilder = new IssueInputBuilder("HACK", new Long(jiraType));
				issueBuilder.setDescription(jiraDescription);
				issueBuilder.setSummary(jiraSummary);
								
		    	BasicIssue basicIssue = jira.createIssue(issueBuilder.build(), sessionConversationMap.get(response.getContext().get("conversation_id")));

		    	StringBuilder responseText = new StringBuilder();
		    	responseText.append(text.get(0));
		    	responseText.append(" - ");
		    	responseText.append(basicIssue.getKey());
		    	responseText.append(" - ");
		    	responseText.append(basicIssue.getSelf().toString());
		   
		    	//coloca na resposta o numero e o link do jira	 
		    	webhookResponse = new WebhookResponse(response.getInputText(), responseText.toString() , response.getContext());
		    
		    } else if(response.getContext().containsKey("end_ctx")){
		    
			response.getContext().remove("end_ctx");
		    	System.out.println("Finalizando conversa.");		    	
		    	String user = sessionMap.get(conversationId).get("user").toString();
		    	System.out.println("User: " + user);
		    	String userMail = sessionMap.get(conversationId).get("userMail").toString();
		    	System.out.println("UserMail: " + userMail);
		    	
		    	//envia o formulario de pesquisa
		    	Mail mail = new Mail();
		    	mail.sendConversation(user, userMail, sessionConversationMap.get(response.getContext().get("conversation_id")));
		    	mail.sendForm(user, userMail);	 
		    	
		    	webhookResponse = new WebhookResponse(response.getInputText(), text.get(0) , response.getContext());
		    	System.out.println("Emails enviados");
		    	System.out.println("Texto da resposta: " + response.getText().get(0));
		    	
		    } else {
				webhookResponse = new WebhookResponse(response.getInputText(),text.get(0),response.getContext());
			}

			System.out.println("webhookResponse "+webhookResponse);
			/*Node node = nodeBuilder().settings(Settings.builder()
				.put("path.home", "/path/to/elasticsearch/home/dir")).node();
			Client client = node.client();
			client.prepareIndex("suportbotit", "chat", "1")
			.setSource(putJsonDocument(webhookResponse)).execute().actionGet();
			node.close();*/
			return webhookResponse;
		} catch (Exception e) {
			e.printStackTrace();
		}


		return null;
	}

		
	public static Map<String, Object> putJsonDocument(WebhookResponse webhookResponse){
            Map<String, Object> jsonDocument = new HashMap<String, Object>();
            jsonDocument.put("speech", webhookResponse.getSpeech());
            jsonDocument.put("tes", webhookResponse.getDisplayText());
            jsonDocument.put("postDate", new Date());
            jsonDocument.put("user", "amorim");
            jsonDocument.put("entidades", "ferramentas");
            return jsonDocument;
        }


	private String getContentConfluence(MessageResponse response) {

		//String topico = response.getContext().get("topico_ctx").toString();
		String ferramenta = (String) response.getContext().get("ferramenta_ctx");
		String problema = (String) response.getInput().get("text");

		ConfluenceService cs = new ConfluenceService();
		return cs.formateResponseToInterface(cs.queryKnowledgebase(ferramenta, problema));

	}
	
	@RequestMapping(value="/login",method = RequestMethod.GET)
	public @ResponseBody String login(@RequestParam(name="usuario") String user, @RequestParam(name="senha") String pwd) throws AuthenticationException{
	    JiraService jiraService = new JiraService(user, pwd);
	    try {
		
		String auth = jiraService.validateUser(user, pwd);
		userConversation.put(user, pwd);
		return "200";
	    } catch (AuthenticationException e) {
		throw e;
	    } catch (ClientHandlerException e) {
		throw e;
	    }
	 
	    
	}
	



}
