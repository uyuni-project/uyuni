/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.suse.manager.errata.advisorymap;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.util.http.HttpClientAdapter;

import com.suse.manager.errata.model.errata.ErrataAdvisoryMap;
import com.suse.manager.errata.model.errata.ErrataAdvisoryMapFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.NoRouteToHostException;
import java.net.URI;
import java.net.URISyntaxException;

public class ErrataAdvisoryMapManager {

    private final ErrataAdvisoryMapFactory advisoryMapFactory;

    private static final Logger LOG = LogManager.getLogger(ErrataAdvisoryMapManager.class);

    private static final String ADVISORY_MAP_CSV_DELIMITER = ",";

    /**
     * Default constructor
     */
    public ErrataAdvisoryMapManager() {
        this(new ErrataAdvisoryMapFactory());
    }

    /**
     * Builds an instance with the given dependencies
     *
     * @param advisoryMapFactoryIn the errata advisory map factory
     */
    public ErrataAdvisoryMapManager(ErrataAdvisoryMapFactory advisoryMapFactoryIn) {
        this.advisoryMapFactory = advisoryMapFactoryIn;
    }

    /**
     * Returns the advisory map size
     *
     * @return the advisory map size
     */
    public long getAdvisoryMapSize() {
        return advisoryMapFactory.count();
    }

    /**
     * parses an advisory map csv file, saving the entries in the database
     * <p>
     * the advisory map csv file has the following structure:
     * a) first row has headers
     * b) each row represents an entry
     * c) each entry row contains fields, coma (",") separated
     * d) first field is the advisory (e.g. "SUSE-SLE-Module-Basesystem-15-SP6-2025-1733")
     * e) second field is the announcementId (e.g. "SUSE-RU-2025:01733-1")
     * f) third field (optional) is the advisoryUri
     * (e.g. "https://www.suse.com/support/update/announcement/2025/suse-ru-202501733-1")
     *
     * @param inputStreamIn the errata advisory map input stream
     */
    public void readAdvisoryMapPopulateDatabase(InputStream inputStreamIn) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStreamIn))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    //skip first line (headers) and clear database
                    advisoryMapFactory.clearErrataAdvisoryMap();
                }
                else {
                    String[] advisoryItem = line.split(ADVISORY_MAP_CSV_DELIMITER);

                    String advisory = (advisoryItem.length > 0 ? advisoryItem[0] : "");
                    String announcementId = (advisoryItem.length > 1 ? advisoryItem[1] : "");
                    String advisoryUri = (advisoryItem.length > 2 ? advisoryItem[2] : "");

                    ErrataAdvisoryMap advisoryMapEntry = new ErrataAdvisoryMap(advisory, announcementId, advisoryUri);
                    advisoryMapFactory.save(advisoryMapEntry);
                }
                firstLine = false;
            }
        }
    }

    /**
     * Synchronizes the advisory map
     * Downloads the advisory map, parses the csv file and saves the entries in the database
     */
    public void syncErrataAdvisoryMap() throws IOException {
        String downloadUrl = ConfigDefaults.get().getErrataAdvisoryMapCsvDownloadUrl();

        URI advisoryMapURI = null;
        try {
            advisoryMapURI = new URI(downloadUrl);
        }
        catch (URISyntaxException ex) {
            LOG.error("Unable to create advisory map url: {} {}.", downloadUrl, ex.getMessage());
        }

        HttpClientAdapter httpClient = new HttpClientAdapter(null, false);

        HttpRequestBase request = null;
        try {
            request = new HttpGet(advisoryMapURI);

            // Connect and parse the response on success
            HttpResponse response = httpClient.executeRequest(request);
            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode == HttpStatus.SC_OK) {
                try (InputStream inputStream = response.getEntity().getContent()) {
                    readAdvisoryMapPopulateDatabase(inputStream);
                }
            }
            else {
                // Request was not successful
                String errorString = "Unable to get content for advisory map request: response code " + responseCode +
                        " connecting to " + request.getURI();
                LOG.error(errorString);
                throw new IOException(errorString);
            }
        }
        catch (NoRouteToHostException ex) {
            String proxy = ConfigDefaults.get().getProxyHost();
            String errorString = "No route to download advisory map" + (proxy != null ? " or the Proxy: " + proxy : "");
            LOG.error(errorString, ex);
            throw new IOException(errorString);
        }
        catch (IOException ioEx) {
            LOG.error("Unable to get content for advisory map request: {} {}", downloadUrl, ioEx);
            throw ioEx;
        }
        finally {
            request.releaseConnection();
        }
    }
}
