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

import com.redhat.rhn.domain.kickstart.KickstartScript;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * ErrataOverviewSerializer
 *
 * @xmlrpc.doc
 *      #struct_begin("kickstart script")
 *          #prop("int", "id")
 *          #prop("string", "name")
 *          #prop("string", "contents")
 *          #prop_desc("string", "script_type", "Which type of script ('pre' or 'post').")
 *          #prop_desc("string", "interpreter", "The scripting language interpreter to use
 *                      for this script.  An empty string indicates the default kickstart
 *                      shell.")
 *          #prop_desc("boolean", "chroot", "True if the script will be executed within the
 *                  chroot environment.")
 *          #prop_desc("boolean", "erroronfail", "True if the script will throw an error if
 *                  it fails.")
 *          #prop_desc("boolean", "template", "True if templating using cobbler is enabled")
 *          #prop_desc("boolean", "beforeRegistration", "True if script will run before the
 *                  server registers and performs server actions.")
 *     #struct_end()
 */
public class KickstartScriptSerializer extends ApiResponseSerializer<KickstartScript> {

    @Override
    public Class<KickstartScript> getSupportedClass() {
        return KickstartScript.class;
    }

    @Override
    public SerializedApiResponse serialize(KickstartScript src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getScriptName())
                .add("contents", src.getDataContents())
                .add("script_type", src.getScriptType())
                .add("interpreter", StringUtils.defaultString(src.getInterpreter()))
                .add("chroot", src.getChroot().equals("Y"))
                .add("erroronfail", src.getErrorOnFail())
                .add("template", !src.getRaw())
                .add("beforeRegistration",
                        src.getScriptType().equals(KickstartScript.TYPE_PRE) || src.getPosition() < 0L)
                .build();
    }
}
