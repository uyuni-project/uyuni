/*
 * Copyright (c) 2010--2013 Red Hat, Inc.
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

import com.redhat.rhn.domain.kickstart.cobbler.CobblerSnippet;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * SnippetSerializer
 *
 * @xmlrpc.doc
 *   #struct_begin("snippet")
 *     #prop("string", "name")
 *     #prop("string", "contents")
 *     #prop_desc("string", "fragment", "The string to include in a kickstart
 *                          file that will generate this snippet.")
 *     #prop_desc("string", "file", "The local path to the file containing this snippet.")
 *   #struct_end()
 */
public class SnippetSerializer extends ApiResponseSerializer<CobblerSnippet> {

    @Override
    public Class<CobblerSnippet> getSupportedClass() {
        return CobblerSnippet.class;
    }

    @Override
    public SerializedApiResponse serialize(CobblerSnippet src) {
        return new SerializationBuilder()
                .add("name", src.getName())
                .add("contents", src.getContents())
                .add("fragment", src.getFragment())
                .add("file", src.getPath().getAbsolutePath())
                .build();
    }
}
