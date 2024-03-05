/*
 * Copyright (c) 2008--2010 Red Hat, Inc.
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
package com.redhat.satellite.search.index.builder;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Map;


/**
 * HardwareDeviceDocumentBuilder
 */
public class HardwareDeviceDocumentBuilder implements DocumentBuilder {

    /**
     * {@inheritDoc}
     */
    public Document buildDocument(Long objId, Map<String, String> metadata) {
        Document doc = new Document();
        doc.add(new Field("id", objId.toString(), Field.Store.YES, Field.Index.UN_TOKENIZED));

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            Field.Index tokenize = Field.Index.TOKENIZED;

            String key = entry.getKey();
            String value = entry.getValue();

            if (key.equals("serverId")) {
               tokenize = Field.Index.UN_TOKENIZED;
            }
            // else key.equals("name") || (key.equals("description")

            doc.add(new Field(key, String.valueOf(value), Field.Store.YES, tokenize));
        }
        return doc;
    }

}
