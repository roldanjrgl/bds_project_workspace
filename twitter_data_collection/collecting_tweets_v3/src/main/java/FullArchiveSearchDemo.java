import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * Sample code to demonstrate the use of the Full archive search endpoint
 * */
public class FullArchiveSearchDemo {

    private static String start_time = "2020-01-01T00:00:00.000Z";
    private static String end_time = "2020-01-30T23:30:00.000Z";
    // To set your enviornment variables in your terminal run the following line:
    // export 'BEARER_TOKEN'='<your_bearer_token>'

    public static void main(String args[]) throws IOException, URISyntaxException, ParseException {
        String bearerToken = System.getenv("BEARER_TOKEN");
        if (null != bearerToken) {
            //Replace the search term with a term of your choice
//            String response = search("from:TwitterDev OR from:SnowBotDev OR from:DailyNASA", bearerToken);
//            String response = search("(FSLR) OR (TetraSun) OR (Mark Widmar) lang:en", bearerToken);
            String response = search("\"ENPH\" lang:en", bearerToken);
//            String response = search("NYU", bearerToken);
            System.out.println(response);
            save_to_csv(response);

        } else {
            System.out.println("There was a problem getting your bearer token. Please make sure you set the BEARER_TOKEN environment variable");
        }
    }

    private static void save_to_csv(String response) throws ParseException, IOException {
        JSONParser parse = new JSONParser();
        JSONObject jobj = (JSONObject)parse.parse(response);
        JSONArray jsonarr_1 = (JSONArray) jobj.get("data");
        System.out.println(jsonarr_1);
        //Get data for Results array

        FileWriter csvWriter = new FileWriter("/Users/Jroldan001/nyu/spring_2021/bds/bds_project_workspace/intellij_tests/collecting_tweets_v3/datadump_enph_v2.csv",true);// change to relative path later
        csvWriter.append("CreatedAt");
        csvWriter.append(",");
        csvWriter.append("ScreenName");
        csvWriter.append(",");
        csvWriter.append("TweetText");
        csvWriter.append("\n");
        for(int i=0;i<jsonarr_1.size();i++) {
            //Store the JSON objects in an array
            //Get the index of the JSON object and print the values as per the index
            JSONObject jsonobj_1 = (JSONObject)jsonarr_1.get(i);
//            System.out.println("Elements under results array");
            csvWriter.append(jsonobj_1.get("created_at").toString());
            csvWriter.append(",");
            csvWriter.append(jsonobj_1.get("id").toString());
            csvWriter.append(",");
            csvWriter.append(jsonobj_1.get("text").toString().replace("\n", "").replace("\r", "").replace(",", " "));
            csvWriter.append("\n");
        }
        csvWriter.flush();
        csvWriter.close();
    }

    /*
     * This method calls the full-archive search endpoint with a the search term passed to it as a query parameter
     * */
    private static String search(String searchString, String bearerToken) throws IOException, URISyntaxException {
        String searchResponse = null;

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets/search/all");
        ArrayList<NameValuePair> queryParameters;
        queryParameters = new ArrayList<>();
        queryParameters.add(new BasicNameValuePair("query", searchString));
        queryParameters.add(new BasicNameValuePair("max_results", "25"));
        queryParameters.add(new BasicNameValuePair("tweet.fields", "created_at"));
//        queryParameters.add(new BasicNameValuePair("start_time", "2020-01-08T11:30:00.000Z"));
//        queryParameters.add(new BasicNameValuePair("end_time", "2020-02-08T11:30:00.000Z"));
        queryParameters.add(new BasicNameValuePair("start_time", start_time));
        queryParameters.add(new BasicNameValuePair("end_time", end_time));

//        queryParameters.add(new BasicNameValuePair("lang", "en"));
        uriBuilder.addParameters(queryParameters);

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpGet.setHeader("Content-Type", "application/json");

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
            searchResponse = EntityUtils.toString(entity, "UTF-8");
        }
        return searchResponse;
    }

}