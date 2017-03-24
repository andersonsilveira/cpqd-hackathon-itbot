package br.com.hackaton.itbot;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

@Controller
@RequestMapping("/conversation")
public class ControllerConversationBot {

	    @RequestMapping(method = RequestMethod.POST)
	    public @ResponseBody WebhookResponse conversation(@RequestBody String intentContent){
		ConversationService service = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
		service.setUsernameAndPassword("6cd18a8a-bfd3-44ad-a14a-e23ce09605ab", "htALYxSVSRVw");
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode;
		try {
		    rootNode = objectMapper.readTree(intentContent.getBytes());
		    JsonNode contextNode = rootNode.path("context");
		    String text = rootNode.path("input").path("text").textValue();
		    MessageRequest newMessage = null;
		    if(contextNode!=null && !contextNode.isNull()){
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> result = mapper.convertValue(contextNode, Map.class);
			newMessage = new MessageRequest.Builder().inputText(text).context(result).build();
			System.out.println(newMessage);
		    }else{
			newMessage = new MessageRequest.Builder().inputText(text).build(); 
		    }
		    MessageResponse response = service.message("9b99c9e6-d597-4af7-a877-6f54e8315dec", newMessage).execute();
		    System.out.println(response);
		    
		    if(response.getContext().containsKey("confluence_ctx")){
			getContentConfluence(response);
			
		    }
		    
		    WebhookResponse webhookResponse = new WebhookResponse(response.getInputText(),response.getText().get(0),response.getContext());
		    System.out.println("wbehookResponse "+webhookResponse);
		    return webhookResponse;
		} catch (JsonProcessingException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		return null;
	}


	    private void getContentConfluence(MessageResponse response) {
		Object topico = response.getContext().get("topico_ctx");
		Object ferramenta = response.getContext().get("ferramenta_ctx");
		Object problema = response.getInput().get("text");
	    }
	    
	    
	/* void createJira() throws URISyntaxException, IOException{
	     final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
	     URI uri = new URI("https://jira.cpqd.com.br");//https://jira.cpqd.com.br
	    final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(uri, "amorim", "cpqd@2015");
	     try {
	         final Issue issue = restClient.getIssueClient().getIssue("ETICS-153071").claim();
	         User user = restClient.getUserClient().getUser("amorim").claim();
	        // System.out.println(issue);
	        	 System.out.println(user);
	     }
	     finally {
	         // cleanup the restClient
	         restClient.close();
	     }
	 }*/

	
	  
}
