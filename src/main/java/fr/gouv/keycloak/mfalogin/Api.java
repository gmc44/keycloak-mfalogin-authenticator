package fr.gouv.keycloak.mfalogin;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.services.ServicesLogger;

public class Api
{
    /**
     * Sends call to api
     * 
     * @return string
     */
   
    public String post(String rootUrl, String apiTokenid, String apiToken, String path, StringEntity data)
    {
        ServicesLogger.LOGGER.info("CALL api = "+rootUrl+" POST path = "+path+" data = "+data);
        String responseString = "";
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost(rootUrl + path);
            request.addHeader("content-type", "application/json");
            request.addHeader(apiTokenid, apiToken);
            request.setEntity(data);
            HttpResponse response = httpClient.execute(request);
            responseString = new BasicResponseHandler().handleResponse(response);
        } catch (Exception e) {
            responseString = "ERROR : failed to contact api " + e;
            ServicesLogger.LOGGER.info(responseString);
        }
        ObjectMapper eval = new ObjectMapper();
        String responseString2;
        try {
            responseString2 = eval.readValue(responseString, String.class);
        } catch (IOException e) {
            responseString2 = responseString;
        }
        return responseString2;
    }
}