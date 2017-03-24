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
		builder.append("ConfluenceResponse [");
		if (title != null) {
			builder.append("title=");
			builder.append(title);
			builder.append(", ");
		}
		if (link != null) {
			builder.append("link=");
			builder.append(link);
			builder.append(", ");
		}
		if (bodyTextHighlights != null) {
			builder.append("bodyTextHighlights=");
			builder.append(bodyTextHighlights);
		}
		builder.append("]");
		return builder.toString();
	}
	
	

}
