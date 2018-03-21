package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import Client.Packet;

public class ClientHandlerUDP implements Runnable {
	
//	private ConcurrentHashMap<String, UserList> toRank;
	private ConcurrentHashMap<String, ArrayBlockingQueue<Packet>> Match;
	private final int LEN = 512;

	public ClientHandlerUDP(/**ConcurrentHashMap<String, UserList> rank, */
			ConcurrentHashMap<String, ArrayBlockingQueue<Packet>> match){

//		this.toRank = rank;
		this.Match = match;
	}

	@Override
	public void run() {


		try(DatagramSocket socket = new DatagramSocket(ServerMain.UDPORT);){
			DatagramPacket packet = new DatagramPacket(new byte[LEN],LEN);
//			InetAddress multicastGroup = InetAddress.getByName(ServerMain.MULTIADDRESS);
			
			
			while(true){
				try {
//					System.out.println("Prima di pacchetto");
					socket.receive(packet);
					String tmp =  new String(packet.getData(),packet.getOffset(),packet.getLength(), "UTF-8");
					String [] tmp1 = tmp.split(",");
					
					String word = tmp1[0];
					String Master = tmp1[1];
					String user = tmp1[2];
					int  size = Integer.parseInt(tmp1[3]);
					
//					System.out.println("Ricevuto dentro ClientUDP dall'utente "+ user + " la parola "+word+" della partita di  "+Master + "Totale parole: "+size);
					
					Packet WU = new Packet(Packet.Type.UDP, word, user,size);
					
					Match.get(Master).put(WU);
					
					
//					toRrank.get(Master).getList().get(user).addWord(word);
					
					
					
				} catch (IOException | InterruptedException e) {e.printStackTrace();}
			}
			
			
//			DatagramPacket multicastPacket= new DatagramPacket(packet.getData(),packet.getOffset(),packet.getLength(),multicastGroup,3000);
//			socket.send(multicastPacket);
		}catch(IOException e){e.printStackTrace();}
	}


}
