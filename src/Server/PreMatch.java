package Server;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PreMatch implements Callable<String>{

	private UserList userlist;
	private final int NUMROW =  58110;
	
	private Vector<String> Permutations = new Vector<String>();
//	private ConcurrentHashMap<String,UserList> Rank;
	
	public PreMatch( UserList userlist/**, ConcurrentHashMap<String,UserList> rank*/) {
		
		this.userlist = userlist;
//		this.Rank = rank;
	}


	@Override
	public String call() throws Exception {
		
//		long waitTime = 420000;
		long waitTime = 60000;
		long endTime = System.currentTimeMillis() + waitTime;
		System.out.println("SONO DENTRO PRE MATCH");
		
		while(System.currentTimeMillis() < endTime){
			
			if(userlist.getCounter() < 0)
				return null;
			else{
				if(userlist.getCounter() == userlist.getList().size())
					return Permutation();
			}
		}
		return null;
	}


	private String Permutation() {

		try(RandomAccessFile dictionary = new RandomAccessFile(ServerMain.DICTIONARY, "r");) {
			
			Random rand = new Random();
			int n = rand.nextInt(NUMROW);
			dictionary.seek(n);
			String tmp = dictionary.readLine();
			String word =  dictionary.readLine();
			
			if(word==null){
				word = tmp;
			}
			
			while(word.length()<6 || word.length() > 8){
				n = rand.nextInt(NUMROW);
				dictionary.seek(n);
				tmp = dictionary.readLine();
				word = dictionary.readLine();
				if(word==null){
					word = tmp;
				}
			}
			System.out.println("Parola trovata "+word);
			permutation("",word);
			n =  rand.nextInt(Permutations.size());
			
			return Permutations.get(n);
			
		} catch (IOException e) {e.printStackTrace();}
		
		return null;
	}
	
	private void permutation(String prefix, String str) {
	    int n = str.length();
	    if (n == 0) Permutations.add(prefix);
	    else {
	        for (int i = 0; i < n; i++)
	            permutation(prefix + str.charAt(i), str.substring(0, i) + str.substring(i+1, n));
	    }
	}
}
