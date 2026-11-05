/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.manager.errata.syncwithpackages.dto.SyncErrataWithPackagesResult;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * SyncErrataWithPackagesResultSerializer
 *
 * @apidoc.doc
 *  #struct_begin("result")
 *      #prop_array_begin_desc("erratas", "the synchronized errata, empty when synchronizing asynchronously")
 *          $SyncedErratumSerializer
 *      #prop_array_end()
 *      #prop_array("unmatched_advisory_names", "string", "the first 20 unmatched advisory names")
 *      #prop_desc("int", "unmatched_remaining", "the number of unmatched advisory names not listed")
 *  #struct_end()
 */
public class SyncErrataWithPackagesResultSerializer extends ApiResponseSerializer<SyncErrataWithPackagesResult> {

    @Override
    public Class<SyncErrataWithPackagesResult> getSupportedClass() {
        return SyncErrataWithPackagesResult.class;
    }

    @Override
    public SerializedApiResponse serialize(SyncErrataWithPackagesResult src) {
        return new SerializationBuilder()
                .add("erratas", src.erratas())
                .add("unmatched_advisory_names", src.unmatchedAdvisoryNames())
                .add("unmatched_remaining", src.unmatchedRemaining())
                .build();
    }
}
