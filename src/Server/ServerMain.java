package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
//import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Client.Packet;

public class ServerMain {

	
	public static UserList userlist = new UserList();
	private static ConcurrentHashMap<String,Future<String>> Matches = new ConcurrentHashMap<String,Future<String>>();
	private static ConcurrentHashMap<String,UserList> MasterList = new ConcurrentHashMap<String,UserList>();
	public static ConcurrentHashMap<String, ExecutorService> eslist =  new ConcurrentHashMap<String, ExecutorService>();
	private static ConcurrentHashMap<String, ArrayBlockingQueue<Packet>> MatchUDP = new ConcurrentHashMap<String, ArrayBlockingQueue<Packet>>();
//	private static ConcurrentHashMap<String, UserList> rank = new ConcurrentHashMap<String,UserList>();
	
	public static String UDPADDRESS;
	public static int UDPORT;
	public static String  TCPADDRESS;
	public static int TCPORT;
	public static String MULTIADDRESS;
	public static String DICTIONARY;
	public static String REGISTER;
	public static int MULTIPORT;
	public static String SCORELIST;
	
	public static void main(String[] args) {
		
		Configuration();
		
		/**Inizializzazione Lista Utenti*/
		ArrayList<JsonWriter> jworkers= new ArrayList<JsonWriter>();
		JSONParser parser = new JSONParser();
		File registro = new File(REGISTER);
		Boolean export = false;
		try{
			if(registro.exists()){
				FileReader registroj = new FileReader(REGISTER);
				JSONArray array = (JSONArray) parser.parse(registroj);
				for (Object jo : array){ 
					jworkers.add(JsonWriter.fromJSON((JSONObject)jo));
					userlist.Add(jworkers.get(jworkers.size()-1).getName(),new User(jworkers.get(jworkers.size()-1).getName(),jworkers.get(jworkers.size()-1).getPass()));
					
//					System.out.println(jworkers.get(jworkers.size()-1).toString());
				}
			}else{
				registro.createNewFile();
				export=true;
			}
		}catch(IOException | ParseException | JSONException e){ e.printStackTrace();}
		
		/** Inizializzazione File Punteggi*/
		File scorelist = new File(SCORELIST);
		if(scorelist.exists()){
			System.out.println("Dentro l'esiste");
			
			try(FileChannel inChannel = FileChannel.open(Paths.get(ServerMain.SCORELIST), 
					StandardOpenOption.READ,StandardOpenOption.WRITE)){
					
					long size = inChannel.size();
					MappedByteBuffer mappedFile = inChannel.map(MapMode.READ_WRITE,0,size);
					while(mappedFile.hasRemaining()){
						int nameSize = mappedFile.getInt();
						StringBuilder name = new StringBuilder();
						for(int i=0; i<nameSize;i++)
							name.append(mappedFile.getChar());
//						System.out.println(name.toString());
//						if(name.toString().equals(username)){
//							mappedFile.putInt(score);
//							break;
//						}else
						int tmp = mappedFile.getInt();
						if(userlist.getList().containsKey(name.toString())){
							System.out.println("Utente " +name.toString() + " con punteggio: "+ tmp);
							userlist.getList().get(name.toString()).updateScore(tmp);
						}	
					}
//				}
//			try(FileChannel inChannel =  FileChannel.open(Paths.get(SCORELIST),StandardOpenOption.READ,StandardOpenOption.WRITE)){
//				long size = inChannel.size();
////				MappedByteBuffer mappedFile = inChannel.map(MapMode.READ_WRITE,0,size);
//				ByteBuffer buffer = ByteBuffer.allocate(1024);
//				while(inChannel.read(buffer)!=-1){
//					buffer.flip();
//					int nameLength = buffer.getInt();
//					StringBuilder name = new StringBuilder();
//					for(int j=0; j<nameLength; j++)
//						name.append(buffer.getChar());
//
//					int tmp = buffer.getInt();
//					buffer.compact();
//					if(userlist.getList().containsKey(name.toString())){
//						System.out.println("dove sono " +name.toString() + " "+ tmp);
//						userlist.getList().get(name.toString()).updateScore(tmp);
//					}
//				}
//				
//				buffer.flip();
//				while(buffer.hasRemaining()){
//					int nameLength =  buffer.getInt();
//					StringBuilder name = new StringBuilder();
//					for(int j=0; j<nameLength; j++)
//						name.append(buffer.getChar());
//					
//					int tmp = buffer.getInt();
//					
//					if(userlist.getList().containsKey(name.toString())){
//						System.out.println("dove sono " +name.toString() + " "+ tmp);
//						userlist.getList().get(name.toString()).updateScore(tmp);
//					}
//				}
//				while(mappedFile.hasRemaining()){
//					int nameSize = mappedFile.getInt();
//					System.out.println(nameSize + "dimensione");
//					StringBuilder name = new StringBuilder();
//					for(int i=0;i<nameSize;i++)
//						name.append(mappedFile.getChar());
//					int tmp = mappedFile.getInt();
//					System.out.println("prima if " + tmp);
//					System.out.println("nome " +name.toString());
//					if(userlist.getList().contains(name.toString())){
//						System.out.println("dove sono " +name.toString() + " "+ tmp);
//						userlist.getList().get(name.toString()).updateScore(tmp);
//						
//					}
//				}
			} catch (IOException e) {e.printStackTrace();}
		} else{
			try {
				scorelist.createNewFile();
			} catch (IOException e1) {e1.printStackTrace();}
		}
		/**RMI	*/
		try{
			ClientManager managerStub = (ClientManager) UnicastRemoteObject.exportObject( new ClientManagerImpl(export), 0);//0=porta aleatoria
			
			Registry registry= LocateRegistry.createRegistry(5002);
			registry.rebind(ClientManager.REMOTE_OBJECT_NAME, managerStub);
			System.out.println("Mi sto per chiudere?");
		}catch(RemoteException e){
			System.err.println("Server configuration RMI failed");
		}
		
		/** TCP*/
		ExecutorService es = null;
		Socket client = null;
		try(ServerSocket server = new ServerSocket()){
			
			server.bind(new InetSocketAddress(TCPADDRESS,TCPORT));
			es = Executors.newCachedThreadPool();
			es.submit(new ClientHandlerUDP(MatchUDP));
			while(true){
				try{
					client=server.accept();
					ClientHandlerTCP handler = new ClientHandlerTCP(client,userlist,Matches,MasterList,MatchUDP);
					es.submit(handler);
				}catch(IOException e){e.printStackTrace();}
			}
		}catch(IOException e){e.printStackTrace();}
		finally{
			
			if(es!=null )
				es.shutdown();
			
//			if(eslist.size()!=0){
//				for(int i=0; i<eslist.size();i ++){
//					eslist.get(i).shutdown();
//				}
//			}
			try {client.close();}catch (IOException e) {e.printStackTrace();}
		}
	}

	private static void Configuration() {
		

		try(BufferedReader bf =new BufferedReader(new InputStreamReader(new FileInputStream("ConfigurationS.txt")));){
			
			UDPORT = Integer.parseInt(bf.readLine());
			UDPADDRESS = bf.readLine();
			TCPORT = Integer.parseInt(bf.readLine());
			TCPADDRESS = bf.readLine();
			MULTIADDRESS = bf.readLine();
			MULTIPORT =  Integer.parseInt(bf.readLine());
			DICTIONARY = bf.readLine();
			REGISTER =  bf.readLine();
			SCORELIST = bf.readLine();
			
			
		}catch (NumberFormatException | IOException e) {e.printStackTrace();} 
	}
}

