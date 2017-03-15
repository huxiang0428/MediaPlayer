package com.example.msg;

public class videoMsg {
	private String name;
	private String path;

	public videoMsg(String name, String path) {
		this.name = name;
		this.setPath(path);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
