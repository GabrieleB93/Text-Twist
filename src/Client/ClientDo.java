package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import Server.User;

public class ClientDo {

	private String TCPADDRESS;
	private int TCPORT;
	private GuiClient gui;
	private ArrayList<String> words;
	private String user;
	
	public ClientDo(String string, int i, GuiClient gui, ArrayList<String> words, String user) {
	
		this.gui=gui;
		this.TCPADDRESS = string;
		this.TCPORT = i;
		this.words = words;
		this.user = user;
	}
	
	public void sendPacket(Packet packet){
		
		try(Socket socket = new Socket(TCPADDRESS,TCPORT);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			){
				switch(packet.getType()){
				
				case INVITE:{
					
					System.out.print("Lista utenti "+packet.getList());
					out.writeUnshared(packet);
				
					Packet tmp = (Packet) in.readUnshared(); 
					if(tmp.getType() == Packet.Type.CONFIRM)
						JOptionPane.showMessageDialog(this.gui.getGlassPane(), "Invite Confirm","OK", JOptionPane.INFORMATION_MESSAGE);		
					
					socket.close();
					break;
				}
				
				case CONFIRM:{
					
					out.writeUnshared(packet);
					
					Packet tmp1 = (Packet) in.readUnshared(); 
					if(tmp1.getType() == Packet.Type.CONFIRM){
						JOptionPane.showMessageDialog(this.gui.getGlassPane(), "Start!","OK", JOptionPane.INFORMATION_MESSAGE);
		  				ExecutorService qwerty = Executors.newSingleThreadExecutor();
		  				qwerty.submit(new MatchClient(this.gui.getFieldText(),words,tmp1.getOwener(),user));
						this.gui.getPanelText().setText(tmp1.getWord());
					}else
						JOptionPane.showMessageDialog(this.gui.getGlassPane(), "An opponent refuse match","ERROR", JOptionPane.ERROR_MESSAGE);		
					
					socket.close();
					break;
				}
				
				case REFUSE:{
					out.writeUnshared(packet);
					socket.close();
					break;
				}
					
				default:
					break;
				
				}
		}catch(IOException | ClassNotFoundException e){e.printStackTrace();}
	}
}

//	public void sendRequest(Packet invited) {
//		
//		try(Socket socket = new Socket(TCPADDRESS,TCPORT);
//			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//			){
//			System.out.print("Lista utenti "+invited.getList());
//			out.writeUnshared(invited);
//			
//			Packet tmp = (Packet) in.readUnshared(); 
//			
//			if(tmp.getType() == Packet.Type.CONFIRM)
//				JOptionPane.showMessageDialog(this.gui.getGlassPane(), "Invite Confirm","OK", JOptionPane.INFORMATION_MESSAGE);		
//			
//			socket.close();
//			
//		}catch(IOException | ClassNotFoundException e){e.printStackTrace();}
//	}
//
//	public void sendConfirm(Packet confirm, JTextPane wordMaster){
//		try(Socket socket = new Socket(TCPADDRESS,TCPORT);
//			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//			){
//			
//			out.writeUnshared(confirm);
//			
//			Packet tmp = (Packet) in.readUnshared(); 
//			
//			if(tmp.getType() == Packet.Type.CONFIRM){
//  				wordMaster.setText(tmp.getOwener());
//			}else
//				JOptionPane.showMessageDialog(this.gui.getGlassPane(), "An opponent refuse match","ERROR", JOptionPane.ERROR_MESSAGE);		
//			
//			socket.close();
//			
//			
//		}catch(IOException | ClassNotFoundException e){e.printStackTrace();}
//	}
//}
	
//	@Override
//	public void run() {
//
//		try(Socket socket = new Socket(TCPADDRESS,TCPORT);
//			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//			){
//				System.out.print(invited.getList());
//				out.writeUnshared(invited);
//				
//				Packet tmp = (Packet) in.readUnshared();
//				if(tmp.getType() == Packet.Type.CONFIRM)
//					JOptionPane.showMessageDialog(this.gui.getGlassPane(), "Invite Confirm","OK", JOptionPane.OK_OPTION);		
//					socket.close();
//		}catch(IOException | ClassNotFoundException e){e.printStackTrace();}
//	}

