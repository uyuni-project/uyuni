package com.suse.studio.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import sun.misc.BASE64Encoder;

import com.suse.studio.client.data.Appliance;
import com.suse.studio.client.data.Appliances;

/**
 * Library class for the SUSE Studio REST API.
 * TODO: Implement the REST of the API.
 * 
 * @author Johannes Renner
 */
public class SUSEStudioClient {

    /* The credentials */
    private final String user;
    private final String apiKey;

    // The base URL for accessing the API
    private String baseURL = "http://susestudio.com/api/v2";
    
    /**
     * Constructor
     */
    public SUSEStudioClient(String user, String apiKey) {
        if (user == null || apiKey == null) {
            throw new RuntimeException("We need the user and API key!");
        }
    	this.user = user;
        this.apiKey = apiKey;
    }

    /**
     * List all appliances of the current user.
     * GET /api/v2/user/appliances
     * 
     * @return list of the current user's appliances
     */
    public List<Appliance> getAppliances() throws IOException {
        // Init the URL
        URL url;
        try {
            url = new URL(baseURL + "/user/appliances");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        // Init the connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Write auth header
        connection.setRequestProperty("Authorization", "BASIC "
                + getEncodedCredentials());

        // Do the request
        long time = System.currentTimeMillis();
        connection.connect();
        InputStream responseBodyStream = connection.getInputStream();

        // Parse the resulting XML
        Appliances result;
        try {
            JAXBContext context = JAXBContext.newInstance(Appliances.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            result = (Appliances) unmarshaller.unmarshal(responseBodyStream);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        connection.disconnect();
        time = System.currentTimeMillis() - time;

        return result.getAppliances();
    }

    /**
     * Return the encoded credentials.
     * 
     * @return encoded credentials as {@link String}
     */
    private String getEncodedCredentials() {
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode((user + ":" + apiKey).getBytes());
    }
}
