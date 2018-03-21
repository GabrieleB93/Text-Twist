package Client;

import java.io.Serializable;
import java.util.Vector;

public class Packet implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public enum Type{INVITE,CONFIRM,ERROR,REFUSE, UDP};
	private Vector<String> list;
	private Type type;
	private String Owner;
	private String Word;
	private String User;
	private int size;
	
	public Packet(Type t){
		switch(t){
		case INVITE:
			list = new Vector<String>();
			this.type = t;
			break;
			
		case CONFIRM:
			this.type = t;
			break;
		
		case ERROR:
			this.type = t;
			break;
			
		case REFUSE:
			this.type = t;
			break;
			
		default:
			break;
		
		}
	}
	
	/**Costruttore per un pacchetto che non verrà mai inviato. È usato per inviare dei dati ai Thread
	 * che gestiscono la partita. È un finto pacchetto
	 * */
	public Packet(Type t , String word, String user, int i ){
		if(t == Type.UDP){
			this.Word = word;
			this.User = user;
			this.size = i;
			
		}
	}
	
	public void add(String s){
		list.add(s);
	}
	
	public int size(){
		return list.size();
	}
	
	public Type getType(){
		return this.type;
	}

	public Vector<String> getList(){
		return this.list;
	}
	
	public void delete(){
		this.list.removeAllElements();
	}
	
	public String getMaster(){
		return this.list.get(list.size()-1);
	}

	public void setOwner(String selectedValue) {
		this.Owner = selectedValue;
	}
	
	public String getOwener(){
		return this.Owner;
	}

	public String Fromwho(){
		return this.User;
	}
	
	public String getWord(){
		return this.Word;
	}
	
	public void setWord(String s){
		 this.Word = s;
	}
	
	public int getSize(){
		return this.size;
	}
}
