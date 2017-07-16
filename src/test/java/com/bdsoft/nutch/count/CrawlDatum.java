package com.bdsoft.nutch.count;

public class CrawlDatum {
	private String url;
	private String status;
	private String fetchTime;
	private String score;
	private String meta;// 元数据

	@Override
	public String toString() {
		return "CrawlDatum [url=" + url + ", status=" + status + ", fetchTime="
				+ fetchTime + ", score=" + score + ", meta=" + meta + "]";
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFetchTime() {
		return fetchTime;
	}

	public void setFetchTime(String fetchTime) {
		this.fetchTime = fetchTime;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}

}