/*
 * Copyright (c) 2008--2011 Red Hat, Inc.
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
 * ErrataDocumentBuilder
 */
public class ErrataDocumentBuilder implements DocumentBuilder {

    // TODO: add some information about fields and their indexing states.

    /**
     * {@inheritDoc}
     */
    public Document buildDocument(Long objId, Map<String, String> metadata) {
        Document doc = new Document();
        doc.add(new Field("id", objId.toString(), Field.Store.YES, Field.Index.UN_TOKENIZED));

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            Field.Store store;
            Field.Index tokenize = Field.Index.TOKENIZED;
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.equals("name") || key.equals("advisoryName")) {
                store = Field.Store.YES;
            }
            else if (key.equals("synopsis") || key.equals("description") ||
                    key.equals("topic") || key.equals("solution")) {
                // index, but do not store
                store = Field.Store.NO;
            }
            else {
                // skip - do not store or index
                continue;
            }

            doc.add(new Field(key, String.valueOf(value), store, tokenize));
        }
        return doc;
    }
}
