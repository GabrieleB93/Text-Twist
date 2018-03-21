package Server;

import java.io.Serializable;

import org.json.JSONException;
import org.json.simple.JSONObject;


public class JsonWriter implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String name;
	private String password ;
	
	
	public JsonWriter(String name, String password) {
		this.name = name;
		this.password = password;
	}
	
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append(this.name);
		sb.append(" - ");
		sb.append(this.password);
		return sb.toString();
	}
	
	public String getName(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.name);
		return sb.toString();
	}
	
	public String getPass(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.password);
		return sb.toString();
		
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() throws JSONException{
		JSONObject result=new JSONObject();
		
		result.put("name",this.name);
		result.put("pssw",this.password);
		return result;
	}
	
	public static  JsonWriter fromJSON(JSONObject object) throws JSONException {
		return new JsonWriter((String)object.get("name"),(String)object.get("pssw"));
	}

}

