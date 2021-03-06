package cinema;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import helloworld.GatewayResponse;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<Object, Object> {

    public Object handleRequest(final Object input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        Rss rss = null;

        try {
            rss = parseRssFromUrl( new URL("http://rss.allocine.fr/ac/cine/cettesemaine?format=xml") );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        List<Film> films = parseFilms(rss);
        Response response = new Response(films);

        return response;

    }

    public Rss parseRssFromUrl( URL url ) {
        try {
            JAXBContext jaxbContext;
            jaxbContext = JAXBContext.newInstance(Rss.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Rss rss = (Rss) jaxbUnmarshaller.unmarshal( url );

            return rss;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Film> parseFilms(Rss rss){
        Channel channel = rss.getChannel();
        List<Film> films = new ArrayList<>();
        List<Item> items = channel.getItems();

        /*for(Item i : items) {
            String description;
            String duree;
            String categorie;
            String tempDescription = i.getDescription();
            String[] temp = tempDescription.split(" - ");
            description = "&lt;p&gt;" + temp[1] + " - " + temp[2];
            temp = temp[0].split(Pattern.quote("("));
            String[] tempCategorie = temp[0].split(";");
            categorie = tempCategorie[2].trim();
            duree = temp[1].substring(0, 8);
            films.add(new Film(i.getTitle(), description, categorie, duree));
        }*/

        for(Item i : items) {
            String description;
            String duree;
            String categorie;
            String tempDescription = i.getDescription();
            String[] temp = tempDescription.split(" - ");
            String balise = "<p>";
            if(temp.length==3){
                description = balise + temp[1] + temp[2];
            }else{
                description = balise + temp[1];
            }
            temp = temp[0].split(Pattern.quote(" ("));
            categorie = temp[0].split(">")[1];
            duree = temp[1].substring(0, 8);
            films.add(new Film(i.getTitle(), description, categorie, duree));
        }


        return films;
    }
}
