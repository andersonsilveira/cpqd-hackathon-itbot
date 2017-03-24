package br.com.hackaton.itbot;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
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
	    
	    
	 void createJira() throws URISyntaxException, IOException{
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
	 }

	
	  
}
