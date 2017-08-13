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

import com.jaunt.ResponseException;
import com.jaunt.UserAgent;

public abstract class Source {
	private String url; 

	public Source(String url) {
		this.setUrl(url);		
	}

	public com.jaunt.Document getJauntDocument() {
		UserAgent userAgent = new UserAgent(); //create new userAgent (headless browser)
		userAgent.setProxyHost("127.0.0.1");
		userAgent.setProxyPort(8118);
		try {
			userAgent.visit(this.url);
		} catch (ResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Response Error");
		}
		return userAgent.doc;

	}

	public org.jsoup.nodes.Document getJsoupDocument() throws MalformedURLException {
		SocketAddress sockAddr = new InetSocketAddress("127.0.0.1",8118);
		Proxy proxy = new Proxy(Proxy.Type.HTTP,sockAddr);
		URL url = new URL("http://archivecrfip2lpi.onion/");
		org.jsoup.nodes.Document doc = null;
		try {
			InputStreamReader in =new InputStreamReader(url.openConnection(proxy).getInputStream());
			String line = null;
			StringBuffer tmp = new StringBuffer();
			BufferedReader inread = new BufferedReader(in);
			while ((line = inread.readLine()) != null) {
				tmp.append(line);
			}
			doc = Jsoup.parse(String.valueOf(tmp));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return doc;


	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	abstract public void printQueryResult(String query) throws MalformedURLException;

}
