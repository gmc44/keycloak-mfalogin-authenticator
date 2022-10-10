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
    private final HttpClient httpClient = HttpClientBuilder.create().build();
    private final String rootUrl;
    private final String apiTokenid;
    private final String apiToken;

    public Api(String rootUrl, String apiTokenid, String apiToken)
    {
        this.rootUrl=rootUrl;
        this.apiTokenid=apiTokenid;
        this.apiToken=apiToken;
    }

    /**
     * Sends call to api
     * 
     * @return string
     */
   
    public String post(String path, StringEntity data)
    {
        ServicesLogger.LOGGER.info("CALL api = "+rootUrl+" POST path = "+path+" data = "+data);
        String responseString = "";
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

    public Ldap getLdapConnexion(String LdapMasterDn, String LdapMasterPwd, String uid) {
        return new Ldap(this, LdapMasterDn, LdapMasterPwd, uid);
    }

    public class Ldap {
        private final Api api;
        private final String LdapMasterDn;
        private final String LdapMasterPwd;
        private final String uid;

        private Ldap(Api api, String login, String pass, String uid) {
            this.api = api;
            this.uid = uid;
            this.LdapMasterDn = login;
            this.LdapMasterPwd = pass;
        }

        public String post(String path, String attr, String value) {
            return api.post(path, new StringEntity("{\"bind_dn\":\"" + LdapMasterDn + "\",\"bind_pwd\":\"" + LdapMasterPwd + "\",\"uid\":\"" + uid + "\",\"attr\":\""+attr+"\",\"value\":\"" + value + "\"}","utf-8"));
        }
        public String smartupdate(String path, String attr, String value, String attrlastupdate) {
            return api.post(path, new StringEntity("{\"bind_dn\":\"" + LdapMasterDn + "\",\"bind_pwd\":\"" + LdapMasterPwd + "\",\"uid\":\"" + uid + "\",\"attr\":\""+attr+"\",\"value\":\"" + value + "\",\"attrlastupdate\":\"" + attrlastupdate + "\"}","utf-8"));
        }
    }
}