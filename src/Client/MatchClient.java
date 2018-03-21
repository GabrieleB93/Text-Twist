package Client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.swing.JTextField;

public class MatchClient implements Runnable {

	
	private JTextField FieldText;
	private String UDPADDRESS;
	private int UDPORT;
	private ArrayList<String> words;
	private String user;
	private String master;

	public MatchClient(JTextField fieldText, ArrayList<String> words, String string, String user) {
		this.FieldText =  fieldText;
		this.words = words;
		this.user= user;
		this.master = string;

	}

	@Override
	public void run() {

		long waitTime = 120000;
//		long endTime = System.currentTimeMillis() + waitTime;
		
		Configuration();
		System.out.println(master + " Dentro Match client master \n");
		
		FieldText.setEnabled(true);
//		while(System.currentTimeMillis() < endTime){}
		try(DatagramSocket s = new DatagramSocket();) {
			InetAddress address = InetAddress.getByName(UDPADDRESS);

			Thread.sleep(waitTime);
			DatagramPacket toSend;
			for(int i=0; i < words.size(); i++){
				String tmp = words.get(i) +","+ master +","+ user + "," + words.size();
				System.out.print("Dentro MatchClient "+tmp + "\n");
				toSend =  new DatagramPacket(tmp.getBytes("UTF-8"),0,tmp.getBytes("UTF-8").length,address,UDPORT);
				s.send(toSend);
			}
			
		} catch (InterruptedException | IOException e) {e.printStackTrace();} 
		FieldText.setEnabled(false);
		FieldText.setText("");
		
	}

	private void Configuration() {

		try(BufferedReader bf =new BufferedReader(new InputStreamReader(new FileInputStream("ConfigurationC.txt")));){
			
			UDPORT = Integer.parseInt(bf.readLine());
			UDPADDRESS = bf.readLine();
			
		}catch (NumberFormatException | IOException e) {e.printStackTrace();} 
	}
}
