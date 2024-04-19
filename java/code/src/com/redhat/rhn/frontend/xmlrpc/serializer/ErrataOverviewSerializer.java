/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import com.redhat.rhn.frontend.dto.ErrataOverview;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * ErrataOverviewSerializer
 * @apidoc.doc
 *     #struct_begin("errata")
 *          #prop_desc("int", "id", "errata ID")
 *          #prop_desc("string", "issue_date", "the date erratum was updated (deprecated)")
 *          #prop_desc("string", "date", "the date erratum was created (deprecated)")
 *          #prop_desc("string", "update_date", "the date erratum was updated (deprecated)")
 *          #prop_desc("string", "advisory_synopsis", "summary of the erratum")
 *          #prop_desc("string", "advisory_type", "type label such as 'Security', 'Bug Fix'")
 *          #prop_desc("string", "advisory_status", "status label such as 'final', 'testing', 'retracted'")
 *          #prop_desc("string", "advisory_name", "name such as 'RHSA', etc.")
 *      #struct_end()
 */
public class ErrataOverviewSerializer extends ApiResponseSerializer<ErrataOverview> {

    @Override
    public Class<ErrataOverview> getSupportedClass() {
        return ErrataOverview.class;
    }

    @Override
    public SerializedApiResponse serialize(ErrataOverview src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("issue_date", src.getIssueDateIsoFormat())
                .add("date", src.getUpdateDateIsoFormat())
                .add("update_date", src.getUpdateDateIsoFormat())
                .add("advisory_synopsis", src.getAdvisorySynopsis())
                .add("advisory_type", src.getAdvisoryType())
                .add("advisory_status", src.getAdvisoryStatus().getMetadataValue())
                .add("advisory_name", src.getAdvisoryName())
                .add("reboot", src.isRebootSuggested())
                .add("reload", src.isRestartSuggested())
                .build();
    }
}
