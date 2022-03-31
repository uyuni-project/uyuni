/*
 * Copyright (c) 2017 SUSE LLC
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

import com.redhat.rhn.manager.audit.AuditChannelInfo;
import com.redhat.rhn.manager.audit.CVEAuditImage;
import com.redhat.rhn.manager.audit.ErrataIdAdvisoryPair;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * CVEAuditImageSerializer
 *
 * @xmlrpc.doc
 *
 * #struct_begin("cve_audit_image")
 *     #prop("int", "image_id")
 *     #prop("string", "patch_status")
 *         #options()
 *             #item_desc ("AFFECTED_PATCH_INAPPLICABLE",
 *                "Affected, patch available in unassigned channel")
 *             #item_desc ("AFFECTED_PATCH_APPLICABLE",
 *                "Affected, patch available in assigned channel")
 *             #item_desc ("NOT_AFFECTED", "Not affected")
 *             #item_desc ("PATCHED", "Patched")
 *         #options_end()
 *     #prop_array("string", "channel_labels",
 *         "Labels of channels that contain an unapplied patch")
 *     #prop_array("string", "errata_advisories",
 *         "Advisories of erratas that patch the specified vulnerability")
 * #struct_end()
 */
public class CVEAuditImageSerializer extends ApiResponseSerializer<CVEAuditImage> {

    // TODO: Find out why?
    // FIXME: change to new class
    @Override
    public Class<CVEAuditImage> getSupportedClass() {
        return CVEAuditImage.class;
    }

    @Override
    public SerializedApiResponse serialize(CVEAuditImage src) {
        // FIXME: change to new class
        Collection<AuditChannelInfo> channels = src.getChannels();
        List<String> channelLabels = new ArrayList<>(channels.size());
        for (AuditChannelInfo channel : channels) {
            channelLabels.add(channel.getLabel());
        }
        Collection<ErrataIdAdvisoryPair> erratas = src.getErratas();
        List<String> errataAdvisories = new ArrayList<>(erratas.size());
        for (ErrataIdAdvisoryPair errata : erratas) {
            errataAdvisories.add(errata.getAdvisory());
        }

        return new SerializationBuilder()
                .add("image_id", src.getId())
                .add("patch_status", src.getPatchStatus().toString())
                .add("channel_labels", channelLabels)
                .add("errata_advisories", errataAdvisories)
                .build();
    }
}
