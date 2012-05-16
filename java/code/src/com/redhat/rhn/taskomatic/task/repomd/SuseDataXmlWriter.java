/**
 * Copyright (c) 2012 Novell
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
import com.redhat.rhn.manager.task.TaskManager;
import com.redhat.rhn.taskomatic.task.TaskConstants;

import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.Writer;

/**
 * susedata.xml writer class
 * @version $Rev $
 *
 */
public class SuseDataXmlWriter extends RepomdWriter {

    private KeywordIterator keywordIterator;
    /**
     *
     * @param writer The writer object for susedata xml
     */
    public SuseDataXmlWriter(Writer writer) {
        super(writer, false);
    }

    /**
     *
     * @param channel channel info
     * @return susedataXml for the given channel
     * @throws Exception exception
     */
    public String getSuseDataXml(Channel channel) throws Exception {
        begin(channel);

        for (PackageDto pkgDto : TaskManager.getChannelPackageDtos(channel)) {
            addPackage(pkgDto);
        }

        end();

        return "";
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
        keywordIterator = new KeywordIterator(channel,
                TaskConstants.TASK_QUERY_REPOMD_GENERATOR_KEYWORDS);
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
    public void addPackage(PackageDto pkgDto) {
        long pkgId = pkgDto.getId().longValue();
        try {
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            SimpleContentHandler tmpHandler = getTemporaryHandler(st);

            SimpleAttributesImpl attr = new SimpleAttributesImpl();
            attr.addAttribute("pkgid", sanitize(pkgId, pkgDto.getChecksum()));
            attr.addAttribute("name", sanitize(pkgId, pkgDto.getName()));
            attr.addAttribute("arch", sanitize(pkgId, pkgDto.getArchLabel()));
            tmpHandler.startDocument();

            tmpHandler.startElement("package", attr);

            addBasicPackageDetails(pkgDto, tmpHandler);
            addKeywords(pkgDto, tmpHandler);
            tmpHandler.endElement("package");
            tmpHandler.endDocument();

            String pkg =  st.toString();
            handler.addCharactersPlain(pkg);
        }
        catch (SAXException e) {
            throw new RepomdRuntimeException(e);
        }
    }

    /**
     *
     * @param pkgDto pkg info to add to xml
     * @throws SAXException sax exception
     */
    private void addBasicPackageDetails(PackageDto pkgDto,
            SimpleContentHandler localHandler) throws SAXException {
        long pkgId = pkgDto.getId().longValue();

        SimpleAttributesImpl attr = new SimpleAttributesImpl();
        attr.addAttribute("ver", sanitize(pkgId, pkgDto.getVersion()));
        attr.addAttribute("rel", sanitize(pkgId, pkgDto.getRelease()));
        attr.addAttribute("epoch", sanitize(pkgId, getPackageEpoch(pkgDto
                .getEpoch())));
        localHandler.startElement("version", attr);
        localHandler.endElement("version");
    }

    /**
     *
     * @param pkgDto pkg info to add to xml
     * @throws SAXException
     */
    private void addKeywords(PackageDto pkgDto,
            SimpleContentHandler localHandler) throws SAXException {
        long pkgId = pkgDto.getId().longValue();
        while (keywordIterator.hasNextForPackage(pkgId)) {
            localHandler.startElement("keyword");
            localHandler.addCharacters(sanitize(pkgId,
                    keywordIterator.getString("keyword")));
            localHandler.endElement("keyword");
        }
    }
}
