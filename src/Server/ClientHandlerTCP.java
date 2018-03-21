package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import Client.CallBack;
import Client.Packet;

public class ClientHandlerTCP implements Runnable {

	private Socket client;
	private UserList list;
	private ConcurrentHashMap<String, Future<String>> Matches; //Elenco risposte utenti per le partite
	private ConcurrentHashMap<String, UserList> MasterList; //Elenco utenti trovati online e che devono accettare
//	private ConcurrentHashMap<String, UserList> toRank; //Elenco utenti che hanno iniziato la partita
	private ConcurrentHashMap<String, ArrayBlockingQueue<Packet>> MatchUDP; //Per smistare i pacchetti UDP
	private final int CAPACITY = 10;
	
	
	public ClientHandlerTCP(Socket client,UserList list, ConcurrentHashMap<String,Future<String>> matches2, 
			ConcurrentHashMap<String,UserList> masterList/**, ConcurrentHashMap<String, UserList> rank*/,
			ConcurrentHashMap<String,ArrayBlockingQueue<Packet>> matchUDP) {
		this.client=client;
		this.list = list;
		this.Matches = matches2;
		this.MasterList = masterList;
//		this.toRank = rank;
		this.MatchUDP = matchUDP;
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
						ServerMain.eslist.put(Master,tmp);
						Future<String> futuretmp = tmp.submit(new PreMatch(listfromP));
						Matches.put(Master, futuretmp );
						
					}else{
//						System.out.println("prima else hanndler");
						out.writeUnshared(new Packet(Packet.Type.ERROR));
						callback = ServerMain.userlist.getList().get(Master).getCallBack();
						callback.invites(null);
//						System.out.println("dentro else handler");
					}
					break;
				}
				
				case CONFIRM:{
					
					System.out.println("Master dentro case = "+p.getOwener());
					MasterList.get(p.getOwener()).increment();
					String word = Matches.get(p.getOwener()).get();
					if(word!=null){
						Packet  tmp = new Packet(Packet.Type.CONFIRM);
						tmp.setWord(word);
						tmp.setOwner(p.getOwener());
						out.writeUnshared(tmp);
						
//						toRank.put(p.getOwener(),MasterList.get(p.getOwener()));
						if(MatchUDP.containsKey((p.getOwener()))==false){
							MatchUDP.put(p.getOwener(),new ArrayBlockingQueue<Packet>(CAPACITY));
							ExecutorService es = Executors.newSingleThreadExecutor();
							System.out.println("Mando in esecuzione il server match ");
							es.submit(new MatchServer(MasterList.get(p.getOwener()),MatchUDP.get(p.getOwener())));
						}
						
						
					}else{
						Packet tmp = new Packet(Packet.Type.ERROR);
						out.writeUnshared(tmp);
					}
					ServerMain.eslist.get(p.getOwener()).shutdown();
					break;
				}
				
				case REFUSE:{
//					System.out.println("Dentro Refuse ");
					MasterList.get(p.getOwener()).annulMatch();	
					break;
					
				}
				
				default: System.out.println("dentro switch");
			}
			
		}catch(IOException | ClassNotFoundException | InterruptedException | ExecutionException e){e.printStackTrace();}
		
	}

}
