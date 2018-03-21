package Client;

import java.rmi.RemoteException;

import Server.ClientManager;

public class FindOnlineClient implements Runnable {

	private ClientManager server;
	private CallBack clientStub;
	private String user;
	
	
	public FindOnlineClient(ClientManager server, CallBack clientStub, String user) {
	
		this.server = server;
		this.clientStub = clientStub;
		this.user = user;
		
	}

	@Override
	public void run() {
		
		while(true){
			try {
				server.getList(clientStub, user);
				Thread.sleep(4200);
			} catch (RemoteException | InterruptedException e) {e.printStackTrace();}
			
		}
	}
}
