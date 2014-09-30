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

import com.redhat.rhn.testing.httpservermock.Responder;

import com.suse.scc.client.SCCConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import simple.http.Request;
import simple.http.Response;

/**
 * Service that simulates the SCC API to be used in test-cases.
 */
public class SCCServerStub implements Responder {

    // Path to example json files and filename
    private static final String PATH_EXAMPLES = "/com/suse/scc/test/";

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
        String schema = config.getSchema();
        String hostname = config.getHostname();
        if (!uri.endsWith("2")) {
            response.set("Link",
                    "<" + schema + hostname + uri + "2>; rel=\"last\", " +
                    "<" + schema + hostname + uri + "2>; rel=\"next\"");
        }

        // Send file content
        PrintStream printStream = null;
        try {
            printStream = response.getPrintStream();
            InputStream inputStream = getStreamFromExampleFile(uri + ".json");

            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) >= 0) {
                printStream.write(buffer, 0, length);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (printStream != null) {
                printStream.close();
            }
        }
    }

    /**
     * Return an example resource file as a stream.
     *
     * @param filename filename of test resource
     * @return an input stream
     */
    public static InputStream getStreamFromExampleFile(String filename) {
        String path = PATH_EXAMPLES + filename;
        InputStream stream = SCCServerStub.class.getResourceAsStream(path);
        return stream;
    }
}
