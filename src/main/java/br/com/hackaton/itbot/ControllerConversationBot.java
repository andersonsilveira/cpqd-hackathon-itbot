package br.com.hackaton.itbot;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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

		MessageRequest newMessage = new MessageRequest.Builder().inputText(intentContent).build();
		MessageResponse response = service.message("f09a47a0-9a08-4725-ac65-fe881fee19ca", newMessage).execute();
		System.out.println(response);
		WebhookResponse webhookResponse = new WebhookResponse(response.getInputText(),response.getText().get(0));
		System.out.println("wbehookResponse "+webhookResponse);
		return webhookResponse;
	}
	    
	    
	public static void main(String[] args) {
		new ControllerConversationBot().conversation("Ola");
	}
	
	


	  
}
