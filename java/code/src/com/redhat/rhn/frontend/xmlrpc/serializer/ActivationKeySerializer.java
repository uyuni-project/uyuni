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

import com.redhat.rhn.domain.token.ActivationKey;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * ActivationKeySerializer
 *
 * @xmlrpc.doc
 *   #struct_begin("activation key")
 *     #prop("string", "key")
 *     #prop("string", "description")
 *     #prop("int", "usage_limit")
 *     #prop("string", "base_channel_label")
 *     #prop_array("child_channel_labels", "string", "childChannelLabel")
 *     #prop_array("entitlements", "string", "entitlementLabel")
 *     #prop_array("server_group_ids", "string", "serverGroupId")
 *     #prop_array("package_names", "string", "packageName - (deprecated by packages)")
 *     #prop_array_begin("packages")
 *       #struct_begin("package")
 *         #prop_desc("string", "name", "packageName")
 *         #prop_desc("string", "arch", "archLabel - optional")
 *       #struct_end()
 *     #prop_array_end()
 *     #prop("boolean", "universal_default")
 *     #prop("boolean", "disabled")
 *     #prop_desc("string", "contact_method", "One of the following:")
 *       #options()
 *         #item("default")
 *         #item("ssh-push")
 *         #item("ssh-push-tunnel")
 *       #options_end()
 *   #struct_end()
 */
public class ActivationKeySerializer extends ApiResponseSerializer<ActivationKey> {

    @Override
    public Class<ActivationKey> getSupportedClass() {
        return ActivationKey.class;
    }

    @Override
    public SerializedApiResponse serialize(ActivationKey src) {
        SerializationBuilder builder = new SerializationBuilder();
        TokenSerializer.populateTokenInfo(src.getToken(), builder);
        builder.add("key", src.getKey());
        return builder.build();
    }
}
