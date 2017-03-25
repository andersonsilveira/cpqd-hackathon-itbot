package br.com.hackaton.itbot;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    	private static Map<String, Map<String, Object>> sessionMap = new HashMap<String, Map<String,Object>>();
    
	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody WebhookResponse conversation(@RequestParam(name="text") String intentContent, @RequestParam(name="conversationId",required=false) String conversationId){
		ConversationService service = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
		service.setUsernameAndPassword("6cd18a8a-bfd3-44ad-a14a-e23ce09605ab", "htALYxSVSRVw");
		/*ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode;*/
		try {
			System.out.println(intentContent);
			/*String split = intentContent.substring(5,intentContent.length()-2);
			String jsonStr = split.replaceAll("\\\\", "");
			System.out.println(jsonStr);
			rootNode = objectMapper.readTree(jsonStr.getBytes());
			JsonNode contextNode = rootNode.path("context");
			String text = rootNode.path("input").path("text").textValue();
			MessageRequest newMessage = null;*/
			//boolean isInitialContext = contextNode!=null && !contextNode.isNull();
			System.out.println("conversationId "+conversationId);
			boolean isInitialContext = conversationId==null || conversationId.isEmpty() || conversationId.equals("\"0\"");
			MessageRequest newMessage = null;
			MessageResponse response = null;
			if(isInitialContext){
			        newMessage = new MessageRequest.Builder().inputText(intentContent).build();
			        response = service.message("9b99c9e6-d597-4af7-a877-6f54e8315dec", newMessage).execute();
			        Map<String, Object> context = response.getContext();
			        System.out.println("contextoMpa "+context);
			        sessionMap.put(context.get("conversation_id").toString(), context);
			        System.out.println("Mapa 1 "+sessionMap.toString());
				System.out.println(newMessage);
			}else{
			       System.out.println("Mapa 2 "+sessionMap.toString());
			        newMessage = new MessageRequest.Builder().inputText(intentContent).context(sessionMap.get(conversationId)).build();
			        response = service.message("9b99c9e6-d597-4af7-a877-6f54e8315dec", newMessage).execute();
			       	
			}



		    System.out.println(response);
		    
		    WebhookResponse webhookResponse = null;
		    if(response.getContext().containsKey("confluence_ctx")){
		    	webhookResponse = new WebhookResponse(response.getInputText(),response.getText().get(0) + "\n" + getContentConfluence(response),response.getContext());
			} else {
				webhookResponse = new WebhookResponse(response.getInputText(),response.getText().get(0),response.getContext());
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

	private MessageRequest createMessageWithNewContext(String text) {
	    MessageRequest newMessage;
	    newMessage = new MessageRequest.Builder().inputText(text).build();
	    return newMessage;
	}

	private MessageRequest createMessageWithContext(JsonNode contextNode, String text) {
	    MessageRequest newMessage;
	    ObjectMapper mapper = new ObjectMapper();
	    @SuppressWarnings("unchecked")
	    Map<String, Object> result = mapper.convertValue(contextNode, Map.class);
	    newMessage = new MessageRequest.Builder().inputText(text).context(result).build();
	    return newMessage;
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
	



}
