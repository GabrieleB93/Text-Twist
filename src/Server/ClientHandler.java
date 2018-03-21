package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import Client.CallBack;
import Client.Packet;

public class ClientHandler implements Runnable {

	private Socket client;
	private UserList list;
//	private ExecutorService matches;
	private ConcurrentHashMap<String, Future<String>> matches;
	private ConcurrentHashMap<String, UserList> MasterList;
	private Vector<ExecutorService> eslist;
//	private AtomicBoolean refuse = new AtomicBoolean(false);
	
	public ClientHandler(Socket client,UserList list, ConcurrentHashMap<String,Future<String>> matches2, 
			ConcurrentHashMap<String,UserList> masterList, Vector<ExecutorService> eslist) {
		this.client=client;
		this.list = list;
		this.matches = matches2;
		this.MasterList = masterList;
		this.eslist = eslist;
	}

	@Override
	public void run() {
		
		try(ObjectOutputStream out = new ObjectOutputStream(this.client.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(this.client.getInputStream());){
			
			Boolean ok=true;
			CallBack callback = null;
			Packet p = (Packet) in.readUnshared(); //Ricevo pacchetto
			System.out.println(p.getType());
			
			UserList listfromP = new UserList();
			switch(p.getType()){
			
				case INVITE:{
					System.out.println("Lista = "+p.getList());
					String Master = p.getMaster();
					System.out.println(Master + "Prima for");
					for(String s: p.getList()){
						System.out.println(list.getList().get(s).getStatus() + " - ");
						
						if(list.getList().get(s).getStatus()){
							ok = true;
							listfromP.Add(s, list.getList().get(s));
						}
						else{
							ok = false;
							listfromP.getList().clear();
							break;
						}
					}
					System.out.println(ok+ " valore prima di callback");
					if(ok){
						for(String s: p.getList()){
							if(!s.equals(Master)){
								callback = ServerMain.userlist.getList().get(s).getCallBack();
								callback.invites(Master);
							}
						}
						out.writeUnshared(new Packet(Packet.Type.CONFIRM));
						
						MasterList.put(Master,listfromP);
						ExecutorService tmp = Executors.newSingleThreadExecutor();
						eslist.add(tmp);
						Future<String> futuretmp = tmp.submit(new PreMatch(listfromP));
						matches.put(Master, futuretmp );
						
					}else{
						System.out.println("prima else hanndler");
						out.writeUnshared(new Packet(Packet.Type.ERROR));
						callback = ServerMain.userlist.getList().get(Master).getCallBack();
						callback.invites(null);
						System.out.println("dentro else handler");
					}
					break;
				}
				
				case CONFIRM:{
					
					System.out.println("Master dentro case = "+p.getOwener());
					MasterList.get(p.getOwener()).increment();
					String word = matches.get(p.getOwener()).get();
					if(word!=null){
						Packet  tmp = new Packet(Packet.Type.CONFIRM);
						tmp.setOwner(word);
						out.writeUnshared(tmp);
					}else{
						Packet tmp = new Packet(Packet.Type.ERROR);
						out.writeUnshared(tmp);
					}
					break;
				}
				
				case REFUSE:{
					System.out.println("Dentro Refuse ");
					MasterList.get(p.getOwener()).annulMatch();	
					break;
					
				}
				
				default: System.out.println("dentro switch");
			}
			
		}catch(IOException | ClassNotFoundException | InterruptedException | ExecutionException e){e.printStackTrace();}
		
	}

}
