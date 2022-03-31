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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.errata.Errata;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * ErrataSerializer
 *
 * @xmlrpc.doc
 *      #struct_begin("errata")
 *          #prop_desc("int", "id", "Errata Id")
 *          #prop_desc("string", "date", "Date erratum was created.")
 *          #prop_desc("string", "advisory_type", "Type of the advisory.")
 *          #prop_desc("string", "advisory_status", "Status of the advisory.")
 *          #prop_desc("string", "advisory_name", "Name of the advisory.")
 *          #prop_desc("string", "advisory_synopsis", "Summary of the erratum.")
 *     #struct_end()
 */
public class ErrataSerializer extends ApiResponseSerializer<Errata> {

    @Override
    public Class<Errata> getSupportedClass() {
        return Errata.class;
    }

    @Override
    public SerializedApiResponse serialize(Errata src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                // Short format of the date to match ErrataOverviewSerializer:
                .add("date", LocalizationService.getInstance().formatShortDate(src.getUpdateDate()))
                .add("advisory_synopsis", src.getSynopsis())
                .add("advisory_name", src.getAdvisoryName())
                .add("advisory_type", src.getAdvisoryType())
                .add("advisory_status", src.getAdvisoryStatus().getMetadataValue())
                .build();
    }
}
