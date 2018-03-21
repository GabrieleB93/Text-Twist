package Server;

import java.util.Vector;

import Client.CallBack;

public class User extends JsonWriter {

	private static final long serialVersionUID = 1L;
	
	private Boolean online;
	private CallBack callback;
	private Vector<String> wordsGame;
	private int score = 0;
	
	public User(String name, String password) {
		super(name, password);
		this.online=false;
		wordsGame =  new Vector<String>();
	}

	public Boolean getStatus(){
		return this.online;
	}

	public void setOnline() {
		this.online = true;
	}
	
	public void setOffline(){
		this.online = false;
	}
	
	public void setCallBack(CallBack stub){
		this.callback=stub;
	}
	
	public CallBack getCallBack(){
		return this.callback;
	}
	
	public void addWord(String s){
		this.wordsGame.add(s);
	}
	
	public void updateScore(int i){
		this.score +=i;
	}
	
	public int getScore(){
		return this.score;
	}
	
}
