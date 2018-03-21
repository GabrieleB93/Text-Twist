package Server;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UserList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConcurrentHashMap<String,User> userlist;
	private AtomicInteger counter ;
	
	public UserList(){
		userlist = new ConcurrentHashMap<String, User>();
		counter =  new AtomicInteger(0);
	}
	
	public int Add(String name, User user){
		if(userlist.containsKey(name))
			return -1;
		else
			userlist.put(name, user);
		return 0;
	}
	
	public ConcurrentHashMap<String,User> getList(){
		return userlist;
	}
	
	public void increment(){
		this.counter.incrementAndGet();
	}
	
	public int getCounter(){
		return this.counter.get();
	}

	public void annulMatch() {
		this.counter.set(-1);
	}
	
}
