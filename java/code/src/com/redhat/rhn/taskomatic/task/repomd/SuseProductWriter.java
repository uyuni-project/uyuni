/**
 * Copyright (c) 2011--2012 Novell
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
package com.redhat.rhn.taskomatic.task.repomd;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.manager.channel.ChannelManager;

import org.xml.sax.SAXException;

import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;

/**
 * products.xml writer class
 *
 * @version $Rev $
 */
public class SuseProductWriter extends RepomdWriter {

    /**
     * Constructor takes in writer.
     * @param writer xml writer object
     */
    public SuseProductWriter(Writer writer) {
        super(writer, true);
    }

    /**
     * Get the products for given channel.
     * @param channel channel info
     * @return updateInfo
     */
    public String getProducts(Channel channel) {
        Iterator iter = ChannelManager.listSuseProductsInChannel(channel).iterator();
        if (!iter.hasNext()) {
            return null;
        }

        begin(channel);

        while (iter.hasNext()) {
            try {
                addProduct((HashMap)iter.next());
            }
            catch (SAXException e) {
                throw new RepomdRuntimeException(e);
            }
        }

        end();

        return "";
    }

    /**
     * End XML creation.
     */
    public void end() {
        try {
            handler.endElement("products");
            handler.endDocument();
        }
        catch (SAXException e) {
            throw new RepomdRuntimeException(e);
        }
    }

    /**
     * Start XML creation.
     * @param c channel info
     */
    public void begin(Channel c) {
        try {
            handler.startElement("products");
        }
        catch (SAXException e) {
            throw new RepomdRuntimeException(e);
        }
    }

    /**
     * Add product to repodata.
     * @param product product values
     * @throws SAXException
     */
    private void addProduct(HashMap product) throws SAXException {
        handler.startElement("product");
        handler.addElementWithCharacters("name", (String) product.get("name"));
        SimpleAttributesImpl attr = new SimpleAttributesImpl();
        attr.addAttribute("ver", (String) product.get("version"));
        attr.addAttribute("rel", (String) product.get("release"));
        attr.addAttribute("epoch", (String) product.get("epoch"));
        handler.startElement("version", attr);
        handler.endElement("version");
        handler.addElementWithCharacters("arch", (String) product.get("arch"));
        handler.addElementWithCharacters("vendor", (String) product.get("vendor"));
        handler.addElementWithCharacters("summary", (String) product.get("summary"));
        handler.addElementWithCharacters("description",
                (String) product.get("description"));

        handler.endElement("product");
    }
}
