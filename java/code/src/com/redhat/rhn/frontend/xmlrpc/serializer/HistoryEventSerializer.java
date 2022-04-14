/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.frontend.dto.HistoryEvent;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * HistoryEventSerializer
 *
 * @xmlrpc.doc
 *  #struct_begin("History Event")
 *      #prop_desc("dateTime.iso8601", "completed", "Date that
 *          the event occurred (optional)")
 *      #prop_desc("string", "summary", "Summary of the event")
 *      #prop_desc("string", "details", "Details of the event")
 *  #struct_end()
 *
 *
 */
public class HistoryEventSerializer extends ApiResponseSerializer<HistoryEvent> {

    @Override
    public Class<HistoryEvent> getSupportedClass() {

        return HistoryEvent.class;
    }

    @Override
    public SerializedApiResponse serialize(HistoryEvent src) {
        return new SerializationBuilder()
                .add("summary", src.getSummary())
                .add("completed", src.getCompleted())
                .add("details", StringUtils.defaultString(src.getDetails()))
                .build();
    }
}
