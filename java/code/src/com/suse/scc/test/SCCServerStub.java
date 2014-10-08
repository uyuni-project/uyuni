/**
 * Copyright (c) 2014 SUSE
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.scc.test;

import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.httpservermock.Responder;

import com.suse.scc.client.SCCConfig;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

import simple.http.Request;
import simple.http.Response;

/**
 * Service that simulates the SCC API to be used in test-cases.
 */
public class SCCServerStub implements Responder {

    /** The configuration object. */
    private SCCConfig config;

    /**
     * Standard constructor
     * @param configIn the configuration object
     */
    public SCCServerStub(SCCConfig configIn) {
        config = configIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void respond(Request request, Response response) {
        // Set some respond headers
        response.set("Content-Type", "application/json");
        long time = System.currentTimeMillis();
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);
        String uri = request.getURI();
        if (!uri.endsWith("2")) {
            response.set("Link",
                    "<" + config.getUrl() + uri + "2>; rel=\"last\", " +
                    "<" + config.getUrl() + uri + "2>; rel=\"next\"");
        }

        // Send file content
        try {
            String filename = uri.replaceFirst("/", "") + ".json";
            URL url = TestUtils.findTestData(filename);

            InputStream in = url.openStream();
            PrintStream out = response.getPrintStream();
            IOUtils.copy(in, out);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
