/**
 * Copyright (c) 2014 SUSE
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. You should have received a copy of GPLv2 along with this
 * software; if not, see http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is granted to
 * use or replicate Red Hat trademarks that are incorporated in this software or
 * its documentation.
 */
package com.redhat.rhn.manager.setup.test;

import com.redhat.rhn.testing.httpservermock.Responder;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import simple.http.Response;

/**
 * Service that simulates the NCC subscription API to be used in test-cases.
 */
public class NCCServerStub implements Responder {

    private final static String FAKE_SUBSCRIPTION
            = "<subscriptionlist lang='en'>"
            + "<authuser>authuser1</authuser>"
            + "<smtguid>smtguid1</smtguid>"
            + "<subscription>"
            + "  <subid>1</subid>"
            + "  <regcode>1234</regcode>"
            + "  <subname>subname0</subname>"
            + "  <type>Gold</type>"
            + "  <substatus>Turbo</substatus>"
            + "  <start-date>1333231200</start-date>"
            + "  <end-date>1427839200</end-date>"
            + "  <duration>3</duration>"
            + "  <product-class>Blade</product-class>"
            + "  <server-class>Blade</server-class>"
            + "  <productlist>Product1</productlist>"
            + "  <nodecount>10</nodecount>"
            + "  <consumed>2</consumed>"
            + "  <consumed-virtual>3</consumed-virtual>"
            + "</subscription>"
            + "</subscriptionlist>";

    @Override
    public void respond(Response response) {
        PrintStream body;
        try {
            body = response.getPrintStream();
            long time = System.currentTimeMillis();
            response.set("Content-Type", "text/xml");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);
            body.println(FAKE_SUBSCRIPTION);
            body.close();
        } catch (IOException ex) {
            Logger.getLogger(NCCServerStub.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
