package com.example.by_the_way;

public class ChatMessages {
	public static final int Message_from=0;
	public static final int Message_to=1;
	private int direction;
	private String content;
	public ChatMessages(int direction, String content){
		super();
		this.direction=direction;
		this.content=content;
	}
	public int getDirection(){
		return direction;
	}
	public void setDirectoin(int direction){
		this.direction=direction;
	}
	public void setContent(String content){
		this.content=content;
	}
	public CharSequence getContent(){
		return content;
	}
}
