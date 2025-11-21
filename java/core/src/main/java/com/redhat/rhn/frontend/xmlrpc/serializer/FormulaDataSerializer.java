/*
 * Copyright (c) 2020 SUSE LLC
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


import com.redhat.rhn.domain.dto.FormulaData;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
*
* FormulaDataSerializer
*
* @apidoc.doc
*
* #struct_begin("formula data")
*     #prop("int", "system_id")
*     #prop("string", "minion_id")
*     #prop_desc("struct", "formula_values", "saved formula values")
* #struct_end()
*/
public class FormulaDataSerializer extends ApiResponseSerializer<FormulaData> {

    @Override
    public Class<FormulaData> getSupportedClass() {
        return FormulaData.class;
    }

    @Override
    public SerializedApiResponse serialize(FormulaData src) {
        return new SerializationBuilder()
                .add("system_id", src.getSystemID())
                .add("minion_id", src.getMinionID())
                .add("formula_values", src.getFormulaValues())
                .build();
    }
}
