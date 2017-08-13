package sources;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jaunt.Element;
import com.jaunt.NotFound;
import com.jaunt.ResponseException;
import com.jaunt.UserAgent;
import com.jaunt.component.Form;

public class Avax extends Source {

	public Avax(String url) {
		super(url);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void printQueryResult(String query) throws MalformedURLException {
		// TODO Auto-generated method stub
		UserAgent userAgent = new UserAgent(); //create new userAgent (headless browser)
		userAgent.setProxyHost("127.0.0.1");
		userAgent.setProxyPort(8118);
		com.jaunt.Document answer = null;
		try {
			userAgent.visit(this.getUrl());
		} catch (ResponseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<Form> forms = userAgent.doc.getForms();
		Form form = forms.get(3);
		try {
			answer = form.setTextField("q", query).submit();
			getAlbumDetails(answer);
		} catch (NotFound | ResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		//System.out.println(answer.innerHTML());

	}

	public com.jaunt.Document applyQuery(String query) throws MalformedURLException {
		UserAgent userAgent = new UserAgent(); //create new userAgent (headless browser)
		userAgent.setProxyHost("127.0.0.1");
		userAgent.setProxyPort(8118);
		com.jaunt.Document answer = null;
		try {
			userAgent.visit(this.getUrl());
		} catch (ResponseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<Form> forms = userAgent.doc.getForms();
		Form form = forms.get(3);
		try {
			answer = form.setTextField("q", query).submit();
		} catch (NotFound | ResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return answer;
	}

	public  com.jaunt.Document getAlbumDetails(com.jaunt.Document queryResult) {
		UserAgent userAgent = new UserAgent(); //create new userAgent (headless browser)
		userAgent.setProxyHost("127.0.0.1");
		userAgent.setProxyPort(8118);
		com.jaunt.Document answer = null;
		try {
			Element details = queryResult.findFirst("<span class=\"glyphicon glyphicon-link\">");
			String albumDetailsLink = details.getParent().getAt("href");
			userAgent.visit(albumDetailsLink);
			answer = userAgent.doc;
			System.out.println(getTracklist(answer));
		} catch (NotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return answer;
	}
	// CLEAN TRACKS REGEX [^0-9][^\. ][a-z]+[^0-9\(]
	public List<String> getTracklist(com.jaunt.Document albumDetails) {
		Element tracklist;
		List<String> tracks = new ArrayList<String>();
		try {
			tracklist = albumDetails.findFirst("<b>Tracklist");
			Element tmp = tracklist.nextSiblingElement();
			Element cdElement = tmp.nextSiblingElement().nextSiblingElement();
			ArrayList<String> listTracks = null;
			if (cdElement.toString().equals("<i>")) 
				listTracks = new ArrayList<String>(Arrays.asList(cdElement.innerHTML().split("<br>")));		
			else { 
				tmp = tracklist;
				listTracks = new ArrayList<String>();
				String rawText = tmp.getParent().getText();
				listTracks = new ArrayList<String>(Arrays.asList(rawText.trim().split("[0-9][0-9] - ")));				
			}
			Pattern p = Pattern.compile("[A-Za-z_]+([a-zA-Z_0-9 ])*");
			Matcher m;
			for(String rawTrack:listTracks)
				if(!rawTrack.isEmpty() && !rawTrack.equals(" ") && !rawTrack.contains("CD")) {
					m = p.matcher(rawTrack.trim());
					m.find();
					String cleanedTrack = m.group(); 
					tracks.add(cleanedTrack);
				}
		} catch (NotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return tracks;
	}


}
