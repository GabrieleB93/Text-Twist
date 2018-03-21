package Server;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.Set;
import java.util.Vector;
import org.json.JSONException;
import org.json.simple.JSONArray;
import Client.CallBack;

public class ClientManagerImpl extends RemoteObject implements ClientManager {

	private static final long serialVersionUID = 1L;

	public static Vector<JsonWriter> jw = new Vector<JsonWriter>();
	public static JSONArray jsonworkers = new JSONArray();
	
	@SuppressWarnings("unchecked")
	public ClientManagerImpl(Boolean export){
		
		if(!export){
			Set<String> elenco = ServerMain.userlist.getList().keySet();
			for(String s : elenco){
				try {
					jsonworkers.add((new User(s,ServerMain.userlist.getList().get(s).getPass())).toJson());
//					System.out.println(s +" "+ ServerMain.userlist.getList().get(s).toString());
				} catch (JSONException e) {e.printStackTrace();}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int register(User user) throws RemoteException {
		
		System.out.println("dentro registrer");
		
		if(ServerMain.userlist.Add(user.getName(), user)==0){
			try {
				jsonworkers.add(user.toJson());
			} catch (JSONException e1) {e1.printStackTrace();}
			
			 try(FileWriter registro = new FileWriter(ServerMain.REGISTER);
				FileChannel outchannel = FileChannel.open(Paths.get(ServerMain.SCORELIST),
				StandardOpenOption.CREATE,StandardOpenOption.WRITE, StandardOpenOption.APPEND/**,StandardOpenOption.READ*/);){
				 
				 jsonworkers.writeJSONString(registro);
				 
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				 
//				Set<String> key = ServerMain.userlist.getList().keySet();
//				MappedByteBuffer buffer = outchannel.map(MapMode.READ_WRITE,0,outchannel.size());
//				for(String s : key){ 
//					System.out.println("Dentro managerimp" +s);
					buffer.putInt(user.getName().length());
					for(int i=0;i< user.getName().length(); i++)//{
						buffer.putChar(user.getName().charAt(i));
//						System.out.println(user.getName().charAt(i));
//					}
					buffer.putInt(0);
//				}
					buffer.flip();
				outchannel.write(buffer);
//				outchannel.write(buffer);
				 
				 
			 }catch(IOException e) { e.printStackTrace();}		
			
			 return 0;
		}
		else
			return -1;
	}

	@Override
	public int login(User user, CallBack stub) throws RemoteException {
		
		
		if(ServerMain.userlist.getList().containsKey(user.getName())){
			if(ServerMain.userlist.getList().get(user.getName()).getPass().equals(user.getPass())){
				ServerMain.userlist.getList().get(user.getName()).setOnline();
				ServerMain.userlist.getList().get(user.getName()).setCallBack(stub);
				return 0; //Loggato con successo
			}
			else
				return -1; //Password erratta
		}else{
			if(user.getStatus())
				return -2; //Già Online
		}
			return -3; //Nome errato
	}
	
	@Override
	public int getList(CallBack stub, String user){
		
		synchronized(ServerMain.userlist){
			try {
				stub.message(ServerMain.userlist, user);
			} catch (RemoteException e) {e.printStackTrace();}
		}
		return 0;
	}

	@Override
	public void logout(User user) throws RemoteException {
		ServerMain.userlist.getList().get(user.getName()).setOffline();
	}
}
