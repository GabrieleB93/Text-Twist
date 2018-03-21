import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import Server.ServerMain;

public class prova {

	public static void main(String[] args) {


		try(RandomAccessFile dictionary = new RandomAccessFile("dictionary.txt", "r");) {
			
			
			boolean cos = Binarysearch(0,dictionary.length(),dictionary,"bae");
			System.out.println(cos);
			
		
		} catch (FileNotFoundException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();}
	}

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
