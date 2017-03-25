package br.com.hackaton.jira;

public class ConfluenceResponse {
	
	public ConfluenceResponse(String title, String link, String bodyTextHighlights) {
		super();
		this.title = title;
		this.link = link;
		this.bodyTextHighlights = bodyTextHighlights;
	}

	private String title;
	
	private String link;
	
	private String bodyTextHighlights;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getBodyTextHighlights() {
		return bodyTextHighlights;
	}

	public void setBodyTextHighlights(String bodyTextHighlights) {
		this.bodyTextHighlights = bodyTextHighlights;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (title != null) {
			builder.append(title);
			builder.append("\n");
		}
		if (link != null) {
			builder.append(link);
			builder.append("\n");
		}
		if (bodyTextHighlights != null) {
			builder.append(bodyTextHighlights);
			builder.append("\n");
		}
		return builder.toString();
	}

	

}
