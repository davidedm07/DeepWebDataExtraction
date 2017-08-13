package connection;

import java.net.MalformedURLException;

import sources.*;

// USER AGENT STRING TOR : Mozilla/5.0 (Windows NT 6.1; rv:52.0) Gecko/20100101 Firefox/52.0


public class DeepWebExtraction {

	public static void main(String[] args) {
		InternetArchive source1 = new InternetArchive("https://archive.org/");
		Avax source2 = new Avax("http://avaxhome5lcpcok5.onion/");
		try { 
			//source1.printQueryResult("nirvana");
			com.jaunt.Document albumDetails = source2.applyQuery("linkin park hybrid theory");
			source2.getAlbumDetails(albumDetails);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
