/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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

import com.redhat.rhn.domain.common.FileList;
import com.redhat.rhn.domain.config.ConfigFileName;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.LinkedList;
import java.util.List;

/**
 * FileListSerializer: Converts a FileList object for representation
 * as an XMLRPC struct.
 *
 * @xmlrpc.doc
 *   #struct_begin("file list")
 *     #prop("string", "name")
 *     #prop_array("file_names", "string", "the list of file names")
 *   #struct_end()
 */
public class FileListSerializer extends ApiResponseSerializer<FileList> {

    @Override
    public Class<FileList> getSupportedClass() {
        return FileList.class;
    }

    @Override
    public SerializedApiResponse serialize(FileList src) {
        List<String> fileNames = new LinkedList<>();
        for (ConfigFileName cfn : src.getFileNames()) {
            fileNames.add(cfn.getPath());
        }
        return new SerializationBuilder()
                .add("name", src.getLabel())
                .add("file_names", fileNames)
                .build();
    }
}
