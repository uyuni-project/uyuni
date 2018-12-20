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

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;

import simple.http.Request;
import simple.http.Response;

/**
 * Service that simulates the SCC API to be used in test-cases.
 */
public class SCCServerStub implements Responder {

    /** The uri. */
    private URI uri;

    /**
     * Instantiates a new SCC server stub.
     *
     * @param uriIn the uri
     */
    public SCCServerStub(URI uriIn) {
        uri = uriIn;
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
        String path = request.getURI();
        if (!path.endsWith("2")) {
            response.set("Link",
                    "<" + uri + path + "2>; rel=\"last\", " +
                    "<" + uri + path + "2>; rel=\"next\"");
        }
        response.set("Per-Page", "1");
        response.set("Total", "2");

        // Send file content
        try {
            String filename = path.replaceFirst("/", "").replaceFirst("\\?page=", "") + ".json";
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
