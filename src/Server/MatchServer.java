package Server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import Client.Packet;

public class MatchServer implements Runnable {

	private final int LEN = 512;
	private ConcurrentHashMap<String,ArrayList<String>> words = new ConcurrentHashMap<String,ArrayList<String>>();
	private UserList UserMatch;
	private ArrayBlockingQueue<Packet> wordsMatch;
	

	public MatchServer(UserList userList, ArrayBlockingQueue<Packet> arrayBlockingQueue) {

		this.UserMatch =  userList;
		this.wordsMatch = arrayBlockingQueue;
		
		
	}

	@Override
	public void run() {
			
			long waitTime = 300000;
			long endTime = System.currentTimeMillis() + waitTime;
			HashMap<String,Boolean> valuesSize =  new HashMap<String,Boolean>();
			
			Set<String> key = UserMatch.getList().keySet();

			for(String s : key){ //Inizializzazione
				System.out.println("Inizializzazione della dimensione di : "+s);
				valuesSize.put(s, false);
				words.put(s, new ArrayList<String>());
			}
			
			
			
			try {Thread.sleep(120000);} catch (InterruptedException e1) {e1.printStackTrace();}
			while(System.currentTimeMillis() < endTime && valuesSize.containsValue(false) == true){
				
				Packet tmp = null;
				try {
					tmp = wordsMatch.poll(5, TimeUnit.SECONDS);
				} catch (InterruptedException e) {e.printStackTrace();}
				if(tmp != null){
					String from = tmp.Fromwho();
					String what = tmp.getWord();
					int size =  tmp.getSize();
					System.out.println("Ricevuto dentro MAtchServer da "+from+ " la parola "+ what + " n° Parole : "+ size);
					
					if(size!=0)
						words.get(from).add(what);	
						
					if(words.get(from).size() == size){
						valuesSize.put(from, true);
						System.out.println("Assaporo la libertà grazie a " + from);
					}
					
				}
			}
			
			System.out.println("Fuori dal while MatchServer \n" );
//			for(String s : key){
//				System.out.println(valuesSize.get(s));
//			}
			
			SetPoint();
			
//			DatagramPacket multicastPacket= new DatagramPacket(packet.getData(),packet.getOffset(),packet.getLength(),multicastGroup,3000);
//			socket.send(multicastPacket);
//		}catch(IOException e){e.printStackTrace();}
	}


	private void SetPoint() {

		try(RandomAccessFile dictionary = new RandomAccessFile(ServerMain.DICTIONARY, "r");) {
			
			Set<String> key = UserMatch.getList().keySet();

			for(String s : key){ 
				int oldscore = UserMatch.getList().get(s).getScore();
				int dim = words.get(s).size();
				int newscore = oldscore;
				boolean result = false;
				for(int i=0; i<dim; i++){ 
					String toFind = words.get(s).get(i);
					result = Binarysearch(0,dictionary.length(),dictionary,toFind);
					System.out.println("risultato binario di "+ toFind + " : "+ result + " per l'utente : "+ s);
					if(result){
						User tmp = UserMatch.getList().get(s);
						tmp.updateScore(toFind.length());
						newscore=tmp.getScore();
					}
						
				}
//				System.out.println("Prima di update");
				if(oldscore != newscore)
					Update(s,newscore);
				
			}
		} catch (FileNotFoundException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();}
	}

	
	private void Update(String username, int score) throws IOException {

//		System.out.println("prima di try");
		try(FileChannel inChannel = FileChannel.open(Paths.get(ServerMain.SCORELIST), 
			StandardOpenOption.READ,StandardOpenOption.WRITE)){
//			System.out.println("Ci arrivo?");
			long size = inChannel.size();
			MappedByteBuffer mappedFile = inChannel.map(MapMode.READ_WRITE,0,size);
			while(mappedFile.hasRemaining()){
				int nameSize = mappedFile.getInt();
				StringBuilder name = new StringBuilder();
				for(int i=0; i<nameSize;i++)
					name.append(mappedFile.getChar());
				System.out.println("l'utente al quale cambio il punteggio " + name.toString());
				if(name.toString().equals(username)){
					mappedFile.putInt(score);
					System.out.println("score "+score+" punteggio di "+name.toString() +" : " + UserMatch.getList().get(name.toString()).getScore());
					break;
				}else
					mappedFile.getInt();
			}
		}
	}

	/**Ricerca binaria sul Dizionario.*/
	private static boolean Binarysearch(long start, long end, RandomAccessFile dictionary, String string) throws IOException {
		
		String tmp = null;
		String tmp1 = null;
		if(start > end)
			return false;
		else{
			long m = (start + end)/2;
			dictionary.seek(m);
			tmp = dictionary.readLine();
			tmp1 = dictionary.readLine();
			if(tmp1!=null)
				tmp=tmp1;
			if(tmp.equals(string))
				return true;
			else{
				if(tmp.compareToIgnoreCase(string) < 0)
					return Binarysearch(m+1,end,dictionary,string);
				else
					return Binarysearch(start,m-1, dictionary,string);
			}
		}
	}
	
}
