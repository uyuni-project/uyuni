/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

import com.redhat.rhn.domain.server.Note;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 *
 * NoteSerializer: Converts a Note object for representation as an XMLRPC struct.
 *
 * @apidoc.doc
 * #struct_begin("note details")
 *   #prop("int", "id")
 *   #prop_desc("string", "subject", "subject of the note")
 *   #prop_desc("string", "note", "contents of the note")
 *   #prop_desc("int", "system_id", "the ID of the system associated with the note")
 *   #prop_desc("string", "creator",  "creator of the note if exists (optional)")
 *   #prop_desc("date", "updated",  "date of the last note update")
 * #struct_end()
 */
public class NoteSerializer extends ApiResponseSerializer<Note> {

    @Override
    public Class<Note> getSupportedClass() {
        return Note.class;
    }

    @Override
    public SerializedApiResponse serialize(Note src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("id", src.getId())
                .add("subject", src.getSubject());
        add(builder, "note", src.getNote());
        add(builder, "system_id", src.getServer().getId());
        // Creator account may be deleted.
        if (src.getCreator() != null) {
            add(builder, "creator", src.getCreator().getLogin());
        }
        add(builder, "updated", src.getModified());
        return builder.build();
    }

    private void add(SerializationBuilder builder, String name, Object value) {
        if (value != null) {
            builder.add(name, value);
        }
    }
}
