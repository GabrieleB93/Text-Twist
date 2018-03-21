package Client;
import java.rmi.Remote;
import java.rmi.RemoteException;

import Server.UserList;

public interface CallBack extends Remote {

	public void message(UserList userlist, String user) throws RemoteException;
	public void invites(String user) throws RemoteException;
}
 