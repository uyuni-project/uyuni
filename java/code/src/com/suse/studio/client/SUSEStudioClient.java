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

public class SUSEStudioClient {

    /* Store credentials for studio */
    private final String user;
    private final String apiKey;

    /**
     * Constructor
     */
    public SUSEStudioClient(String user, String apiKey) {
        if (user != null && apiKey != null) {
            this.user = user;
            this.apiKey = apiKey;
        } else {
            throw new RuntimeException("We need the user and the API key!");
        }
    }

    /**
     * List all appliances of the current user.
     * 
     * @return The list of the current user's appliances.
     */
    public List<Appliance> getAppliances() throws IOException {
        // Init the URL
        URL url;
        try {
            url = new URL("http://susestudio.com/api/v2/user/appliances");
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
     * Return the encoded credentials of the user.
     * 
     * @return encoded credentials as a {@link String}
     */
    private String getEncodedCredentials() {
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode((user + ":" + apiKey).getBytes());
    }
}
