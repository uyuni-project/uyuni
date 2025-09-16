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

import com.redhat.rhn.frontend.dto.PackageOverview;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import org.apache.commons.lang3.StringUtils;

/**
 * PackageOverviewSerializer
 *
 * @apidoc.doc
 *   #struct_begin("package overview")
 *   #prop("int", "id")
 *   #prop("string", "name")
 *   #prop("string", "summary")
 *   #prop("string", "description")
 *   #prop("string", "version")
 *   #prop("string", "release")
 *   #prop("string", "arch")
 *   #prop("string", "epoch")
 *   #prop("string", "provider")
 *   #struct_end()
 */
public class PackageOverviewSerializer extends ApiResponseSerializer<PackageOverview> {

    @Override
    public Class<PackageOverview> getSupportedClass() {
        return PackageOverview.class;
    }

    @Override
    public SerializedApiResponse serialize(PackageOverview src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getPackageName())
                .add("summary", src.getSummary())
                .add("description", StringUtils.defaultString(src.getDescription()))
                .add("version", src.getVersion())
                .add("release", src.getRelease())
                .add("epoch", StringUtils.defaultString(src.getEpoch()))
                .add("arch", src.getPackageArch())
                .add("nvre", src.getPackageNvre())
                .add("nvrea", src.getNvrea())
                .add("packageChannels", src.getPackageChannels())
                .add("provider", src.getProvider())
                .build();
    }
}
