/**
 * Copyright (c) 2012 SUSE LLC
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
import com.redhat.rhn.frontend.dto.PackageDto;
import com.redhat.rhn.manager.EulaManager;
import com.redhat.rhn.manager.task.TaskManager;

import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

/**
 * susedata.xml writer class
 *
 */
public class SuseDataXmlWriter extends RepomdWriter {

    private Long channelId;

    /**
     *
     * @param writer The writer object for susedata xml
     */
    public SuseDataXmlWriter(Writer writer) {
        super(writer, false);
    }

    /**
     * end xml metadata generation
     */
    public void end() {
        try {
            handler.endElement("susedata");
            handler.endDocument();
        }
        catch (SAXException e) {
            throw new RepomdRuntimeException(e);
        }
    }

    /**
     * Start xml metadata generation
     * @param channel channel data
     */
    public void begin(Channel channel) {
        channelId = channel.getId();
        SimpleAttributesImpl attr = new SimpleAttributesImpl();
        attr.addAttribute("xmlns", "http://linux.duke.edu/metadata/common");
        attr.addAttribute("xmlns:rpm", "http://linux.duke.edu/metadata/rpm");
        attr.addAttribute("packages", Integer.toString(channel.getPackageCount()));

        try {
            handler.startElement("susedata", attr);
        }
        catch (SAXException e) {
            throw new RepomdRuntimeException(e);
        }
    }

    /**
     *
     * @param pkgDto pkg info to add to xml
     */
    public void addPackage(com.redhat.rhn.domain.rhnpackage.Package pkgDto) {
        long pkgId = pkgDto.getId().longValue();
        List<String> eulas = new EulaManager().getEulasForPackage(pkgId);

        Collection<String> keywords = TaskManager
                .getChannelPackageKeywords(channelId, pkgId);

        if (keywords.isEmpty() && eulas.isEmpty()) {
            // this package has no keywords and no EULA
            return;
        }
        try {
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            SimpleContentHandler tmpHandler = getTemporaryHandler(st);

            tmpHandler.startDocument();
            addPackageBoilerplate(tmpHandler, pkgDto);
            addEulas(pkgId, eulas, tmpHandler);
            addKeywords(pkgId, keywords, tmpHandler);
            tmpHandler.endElement("package");
            tmpHandler.endDocument();

            String pkg =  st.toString();
            handler.addCharacters(pkg);
        }
        catch (SAXException e) {
            throw new RepomdRuntimeException(e);
        }
    }

    /**
     *
     * @param pkgId pkg id
     * @param keywords list of keywords for this package
     * @param localHandler SAX helper
     * @throws SAXException
     */
    private void addKeywords(long pkgId, Collection<String> keywords,
            SimpleContentHandler localHandler) throws SAXException {
        for (String keyword : keywords) {
            localHandler.startElement("keyword");
            localHandler.addCharacters(sanitize(pkgId, keyword));
            localHandler.endElement("keyword");
        }
    }

    /**
     * Adds the specified EULA strings to the package
     * @param pkgId ID of the package
     * @param eulas EULA strings
     * @param localHandler SAX helper
     * @throws SAXException if anything goes wrong
     */
    private void addEulas(long pkgId, List<String> eulas, SimpleContentHandler localHandler)
        throws SAXException {
        for (String eula : eulas) {
            localHandler.startElement("eula");
            localHandler.addCharacters(sanitize(pkgId, eula));
            localHandler.endElement("eula");
        }
    }
}
