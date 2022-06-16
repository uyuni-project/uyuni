/**
 * Copyright (c) 2013 SUSE LLC
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

import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import com.redhat.rhn.manager.audit.AuditChannelInfo;
import com.redhat.rhn.manager.audit.CVEAuditServer;
import com.redhat.rhn.manager.audit.ErrataIdAdvisoryPair;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * CVEAuditServerSerializer
 *
 * @xmlrpc.doc
 *
 * #struct_begin("CVE audit system")
 *     #prop("int", "system_id")
 *     #prop("string", "patch_status")
 *         #options()
 *             #item_desc ("AFFECTED_PATCH_INAPPLICABLE",
 *                "affected, patch available in unassigned channel")
 *             #item_desc ("AFFECTED_PATCH_APPLICABLE",
 *                "affected, patch available in assigned channel")
 *             #item_desc ("NOT_AFFECTED", "not affected")
 *             #item_desc ("PATCHED", "patched")
 *         #options_end()
 *     #prop_array("string", "channel_labels",
 *         "labels of channels that contain an unapplied patch")
 *     #prop_array("string", "errata_advisories",
 *         "advisories of erratas that patch the specified vulnerability")
 * #struct_end()
 *
 */
public class CVEAuditServerSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class<CVEAuditServer> getSupportedClass() {
        return CVEAuditServer.class;
    }

    /**
     * {@inheritDoc}
     */
    public void doSerialize(Object value, Writer output,
            XmlRpcSerializer serializer) throws XmlRpcException, IOException {

        CVEAuditServer system = (CVEAuditServer) value;
        Collection<AuditChannelInfo> channels = system.getChannels();
        List<String> channelLabels = new ArrayList<String>(channels.size());
        for (AuditChannelInfo channel : channels) {
            channelLabels.add(channel.getLabel());
        }
        Collection<ErrataIdAdvisoryPair> erratas = system.getErratas();
        List<String> errataAdvisories = new ArrayList<String>(erratas.size());
        for (ErrataIdAdvisoryPair errata : erratas) {
            errataAdvisories.add(errata.getAdvisory());
        }

        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("system_id", system.getId());
        helper.add("patch_status", system.getPatchStatus().toString());
        helper.add("channel_labels", channelLabels);
        helper.add("errata_advisories", errataAdvisories);
        helper.writeTo(output);
    }
}
