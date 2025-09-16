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

import com.redhat.rhn.frontend.dto.XccdfTestResultDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * XccdfTestResultDtoSerializer
 * @apidoc.doc
 * #struct_begin("OpenSCAP XCCDF Scan")
 *   #prop_desc("int", "xid", "XCCDF TestResult ID")
 *   #prop_desc("string", "profile", "XCCDF Profile")
 *   #prop_desc("string", "path", "path to XCCDF document")
 *   #prop_desc("string", "ovalfiles", "optional OVAL files")
 *   #prop_desc($date, "completed", "scan completion time")
 * #struct_end()
 */
public class XccdfTestResultDtoSerializer extends ApiResponseSerializer<XccdfTestResultDto> {

    @Override
    public Class<XccdfTestResultDto> getSupportedClass() {
        return XccdfTestResultDto.class;
    }

    @Override
    public SerializedApiResponse serialize(XccdfTestResultDto src) {
        SerializationBuilder builder = new SerializationBuilder();
        add(builder, "xid", src.getXid());
        add(builder, "profile", src.getProfile());
        add(builder, "path", src.getPath());
        add(builder, "ovalfiles", src.getOvalfiles());
        add(builder, "completed", src.getCompleted());
        return builder.build();
    }

    private static void add(SerializationBuilder builder, String label, Object value) {
        if (value != null) {
            builder.add(label, value);
        }
    }
}
