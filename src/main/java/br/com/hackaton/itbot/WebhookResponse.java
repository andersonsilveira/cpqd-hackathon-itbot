package br.com.hackaton.itbot;

import java.util.List;
import java.util.Map;

public class WebhookResponse {
    private final String speech;
    private final String displayText;

    private final String source = "java-webhook";
    
    private final String conversationId = "";
    private final Map<String, Object> context;

   /**
     * @param inputText
     * @param displayText2
     * @param context
     */
    public WebhookResponse(String inputText, List<String> displayText2, Map<String, Object> context) {
	 this.context = context;
	 this.speech = inputText;
	 StringBuilder stringBuilder = new StringBuilder();
	 for (String string : displayText2) {
	     stringBuilder.append(string).append("\n");
	}
	 displayText = stringBuilder.toString();
    }
    public WebhookResponse(String inputText, String displayText2, Map<String, Object> context) {
  	 this.context = context;
  	 this.speech = inputText;
  	 this.displayText = displayText2;
      }


	public String getSpeech() {
        return speech;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getSource() {
        return source;
    }
    
 

    public String getConversationId() {
        return conversationId;
    }


    @Override
    public String toString() {
	return "WebhookResponse [speech=" + speech + ", displayText=" + displayText + ", source=" + source
		+ ", conversationId=" + conversationId + "]";
    }


    public Map<String, Object> getContext() {
        return context;
    }
}