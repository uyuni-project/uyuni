/**
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

import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * ImageProfileSerializer
 *
 * @xmlrpc.doc
 * #struct("Image Profile information")
 *   #prop("string", "label")
 *   #prop("string", "imageType")
 *   #prop("string", "imageStore")
 *   #prop("string", "activationKey")
 * #struct_end()
 */
public class ImageProfileSerializer extends RhnXmlRpcCustomSerializer {

    /**
     *
     * {@inheritDoc}
     */
    @Override
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
       throws XmlRpcException, IOException {
        SerializerHelper helper = new SerializerHelper(serializer);
        ImageProfile profile = (ImageProfile) value;
        helper.add("label", profile.getLabel());
        helper.add("imageType", profile.getImageType());
        helper.add("imageStore", profile.getTargetStore().getLabel());

        String activationKey = profile.getToken() != null ?
                ActivationKeyFactory.lookupByToken(profile.getToken()).getKey() : "";
        helper.add("activationKey", activationKey);

        helper.writeTo(output);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getSupportedClass() {
        return ImageProfile.class;
    }
}
