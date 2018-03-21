package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import Client.CallBack;

public interface ClientManager extends Remote{

	public static final String REMOTE_OBJECT_NAME = "CLIENT_MANAGER";

	public int register(User user) throws RemoteException;

	public int login(User tmp, CallBack stub) throws RemoteException;
	
	public int getList (CallBack clientStub, String user) throws RemoteException;

	public void logout(User user) throws RemoteException;
}
