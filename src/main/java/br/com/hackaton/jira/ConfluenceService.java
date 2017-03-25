package br.com.hackaton.jira;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ConfluenceService {

	private static final String CONFLUENCE_URL = "https://jira.cpqd.com.br";
	private static final String QUERY_KNOWLEDGEBASE = "/rest/servicedesk/knowledgebase/latest/articles/search?query=%s&spaceKey=TI&project=HD";

	public String formateResponseToInterface (List<ConfluenceResponse> confluenseResponse) {
		StringBuilder formattedString = new StringBuilder();
		int responseCount = 1;
		if (confluenseResponse != null && !confluenseResponse.isEmpty()) {
			for (ConfluenceResponse confluenceResponse : confluenseResponse) {
				formattedString.append(responseCount + ") " + confluenceResponse);
				responseCount++;
			}
		} else {
			formattedString.append("Nenhum item encontrado na base de conhecimento");
		}
		return formattedString.toString();
	}

	public List<ConfluenceResponse> queryKnowledgebase(String tool, String issue) {

		List<ConfluenceResponse> responseItems;

		String url = setUpQueryString(tool, issue);

		String responseJson = sendRequestAndGetResponse(url);

		responseItems = formatReceivedJson(responseJson);

		System.out.println("responseItems: " + formateResponseToInterface(responseItems));

		return responseItems;
	}

	private List<ConfluenceResponse> formatReceivedJson(String responseJson) {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode;
		List<ConfluenceResponse> responseItems = new ArrayList<ConfluenceResponse>();

		try {
			rootNode = objectMapper.readTree(new ByteArrayInputStream(responseJson.getBytes("UTF-8")));
			JsonNode contextNode = rootNode.path("results");
			Iterator it = contextNode.iterator();
			while (it.hasNext()) {
				System.out.println("----------------------------");

				ObjectNode foundItem = (ObjectNode)it.next();
				System.out.println("foundItem: " + foundItem);

				System.out.println("title: " + foundItem.get("title"));
				String formattedTitle = foundItem.get("title").toString();
				formattedTitle = formattedTitle.replaceAll("(@(.*?)@*)[A-Za-z]*(@(.*?)@*)", "");
				System.out.println("formattedTitle: " + formattedTitle);

				System.out.println("link: " + foundItem.get("appLinkUrl") + foundItem.get("url"));
				String formattedLink = foundItem.get("appLinkUrl").toString() + foundItem.get("url").toString();
				formattedLink = formattedLink.replaceAll("\"", "");
				System.out.println("formattedLink: " + formattedLink);

				System.out.println("bodyTextHighlights: " + foundItem.get("bodyTextHighlights"));
				String formattedBodyTextHighlights = foundItem.get("bodyTextHighlights").toString();
				formattedBodyTextHighlights = formattedBodyTextHighlights.replaceAll("(@(.*?)@*)[A-Za-z]*(@(.*?)@*)", "");
				System.out.println("formattedBodyTextHighlights: " + formattedBodyTextHighlights);

				responseItems.add(new ConfluenceResponse(formattedTitle, formattedLink, formattedBodyTextHighlights));

				System.out.println("----------------------------");
			}

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responseItems;
	}

	private String sendRequestAndGetResponse(String url) {
		Client client = Client.create();
		WebResource webResource = client.resource(url);
		ClientResponse response = webResource.header("Authorization", "Basic " + "ZGllZ29jOipENjY3ODEyYyo=").type("application/json").accept("application/json").get(ClientResponse.class);
		int statusCode = response.getStatus();
		System.out.println("statusCode: " + statusCode);
		System.out.println("response: " + response);
		String responseJson = response.getEntity(String.class);
		System.out.println("response json: " + responseJson);
		return responseJson;
	}

	private String setUpQueryString(String tool, String issue) {
		System.out.println("tool: " + tool);
		System.out.println("issue: " + issue);
		String queryString = String.format(QUERY_KNOWLEDGEBASE, tool + "+" + issue);
		System.out.println("queryString: " + queryString);
		String url = CONFLUENCE_URL + queryString;
		System.out.println("url: " + url);
		return url;
	}

	/*public static void main(String[] args) {
		ConfluenceService cs = new ConfluenceService();
		cs.queryKnowledgebase("git", "acesso");
	}*/

}
