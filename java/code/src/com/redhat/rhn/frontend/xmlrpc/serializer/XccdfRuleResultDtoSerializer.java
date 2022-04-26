/*
 * Copyright (c) 2012 Red Hat, Inc.
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

import com.redhat.rhn.frontend.dto.XccdfRuleResultDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * XccdfRuleResultDtoSerializer
 * @xmlrpc.doc
 * #struct_begin("OpenSCAP XCCDF RuleResult")
 *   #prop_desc("string", "idref", "idref from XCCDF document.")
 *   #prop_desc("string", "result", "Result of evaluation.")
 *   #prop_desc("string", "idents", "Comma separated list of XCCDF idents.")
 * #struct_end()
 */
public class XccdfRuleResultDtoSerializer extends ApiResponseSerializer<XccdfRuleResultDto> {

    @Override
    public Class<XccdfRuleResultDto> getSupportedClass() {
        return XccdfRuleResultDto.class;
    }

    @Override
    public SerializedApiResponse serialize(XccdfRuleResultDto src) {
        return new SerializationBuilder()
                .add("idref", src.getDocumentIdref())
                .add("result", src.getLabel())
                .add("idents", src.getIdentsString())
                .build();
    }
}
