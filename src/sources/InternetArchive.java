package sources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;

import org.jsoup.Jsoup;

public class InternetArchive extends Source{

	private final String searchForm = "/search.php?query=";


	public InternetArchive(String url) {
		super(url);
	}

	/* altro metodo 
	 * Document doc = Jsoup.connect(this.getUrl()+ this.searchForm + query)
      	.userAgent("Mozilla/5.0 (Windows NT 6.1; rv:52.0) Gecko/20100101 Firefox/52.0").get(); */


	public void printQueryResult(String query) throws MalformedURLException  { 
		org.jsoup.nodes.Document doc = null;
		SocketAddress sockAddr = new InetSocketAddress("127.0.0.1",8118);
		Proxy proxy = new Proxy(Proxy.Type.HTTP,sockAddr);
		URL url = new URL(this.getUrl()+ this.searchForm + query);
		try {
			url.openConnection(proxy);
			InputStreamReader in =new InputStreamReader(url.openConnection(proxy).getInputStream());
			String line = null;
			StringBuffer tmp = new StringBuffer();
			BufferedReader inread = new BufferedReader(in);

			while ((line = inread.readLine()) != null) {
				tmp.append(line);
			} 
			doc = Jsoup.parse(String.valueOf(tmp));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Connection failed to url " + this.getUrl()+ this.searchForm + query);
		}

		System.out.println(doc.toString());		
	}

	public org.jsoup.nodes.Document applyQuery(String query) throws MalformedURLException {
		org.jsoup.nodes.Document doc = null;
		SocketAddress sockAddr = new InetSocketAddress("127.0.0.1",8118);
		Proxy proxy = new Proxy(Proxy.Type.HTTP,sockAddr);
		URL url = new URL(this.getUrl()+ this.searchForm + query);
		try {
			url.openConnection(proxy);
			InputStreamReader in =new InputStreamReader(url.openConnection(proxy).getInputStream());
			String line = null;
			StringBuffer tmp = new StringBuffer();
			BufferedReader inread = new BufferedReader(in);

			while ((line = inread.readLine()) != null) {
				tmp.append(line);
			} 
			doc = Jsoup.parse(String.valueOf(tmp));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Connection failed to url " + this.getUrl()+ this.searchForm + query);
		}

		return doc;	
	}
}




