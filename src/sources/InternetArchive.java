package sources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



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
			System.out.println(getLinksTrack(doc));
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Connection failed to url " + this.getUrl()+ this.searchForm + query);
		}

		return doc;	
	}
	
	public String getLinksTrack(org.jsoup.nodes.Document doc) {
		org.jsoup.nodes.Document columns = Jsoup.parse(doc.getElementsByClass("columns-items").html());
		Elements links = columns.select("a[href]");
		String result = "";
		for (Element link : links) {
			String songLink=link.attr("href");
			if (songLink.contains("details"))
				result +=super.getUrl()+songLink + "\n";
		}
		return result;
		
	}
}




