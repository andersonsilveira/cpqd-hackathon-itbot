package br.com.hackaton.itbot;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.hackaton.jira.ConfluenceResponse;
import br.com.hackaton.jira.ConfluenceService;

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
			System.out.println(intentContent);
			String split = intentContent.substring(5,intentContent.length()-2);
			String jsonStr = split.replaceAll("\\\\", "");
			System.out.println(jsonStr);
			rootNode = objectMapper.readTree(jsonStr.getBytes());
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
		    
		    
		    WebhookResponse webhookResponse = null;
		    if(response.getContext().containsKey("confluence_ctx")){
		    	webhookResponse = new WebhookResponse(response.getInputText(),response.getText().get(0) + "\n" + getContentConfluence(response).toString(),response.getContext());
			} else {
				webhookResponse = new WebhookResponse(response.getInputText(),response.getText().get(0),response.getContext());
			}

			System.out.println("webhookResponse "+webhookResponse);
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


	private List<ConfluenceResponse> getContentConfluence(MessageResponse response) {

		//String topico = response.getContext().get("topico_ctx").toString();
		String ferramenta = (String) response.getContext().get("ferramenta_ctx");
		String problema = (String) response.getInput().get("text");

		ConfluenceService cs = new ConfluenceService();
		return cs.queryKnowledgebase(ferramenta, problema);

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
