package Client;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

import Server.UserList;

public class CallBackImpl extends RemoteObject implements CallBack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GuiClient gui;
	private Vector<String> invitedlist;
	
	public CallBackImpl(GuiClient gui, Vector<String> invitedlist){
		this.invitedlist = invitedlist;
		this.gui=gui;
	}

	@Override
	public void message(UserList list, String user) throws RemoteException {
		
//		Vector<String> offline = new Vector<String>();
		Vector<String> online = new Vector<String>();
		Set<String> key = list.getList().keySet();
		for(String k: key){
//			if(list.getList().get(k).getStatus())
			if(!k.equals(user))
				online.add(list.getList().get(k).getName());
//			else
//				offline.add(list.getList().get(k).getName());
		}
		
		this.gui.list.setListData(online);
//		this.gui.list.setListData(offline);
		this.gui.revalidate();
//		this.gui.list.setText("");
//		for(int i =0;i < online.size();i++)
//			this.gui.list.append(online.get(i)+ "\n");
//		for(int i =0;i < offline.size();i++)
//			this.gui.list.append(offline.get(i)+ "\n");
	}

	@Override
	public void invites(String user) throws RemoteException {
		
		System.out.println("sono"+user);
		if(user!=null){
			System.out.println("dentro l'if ");
			this.invitedlist.add(user);
			this.gui.requestList.setListData(this.invitedlist);
		}
		else{
			System.out.println("sono dentro else calbacck");
			JOptionPane.showMessageDialog(this.gui.getGlassPane(), "An oppontent isn't online","ERROR", JOptionPane.ERROR_MESSAGE);		
		}
	}
}
