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
package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.suse.manager.webui.utils.salt.custom.ImageChecksum.Checksum;
import com.suse.manager.webui.utils.salt.custom.ImageChecksum.SHA256Checksum;

import java.io.IOException;

/**
 * Adapter to convert a Checksum formatted string to Checksum
 */
public class ChecksumAdapter extends TypeAdapter<Checksum> {

    @Override
    public void write(JsonWriter jsonWriter, Checksum checksum) throws IOException {
        if (checksum == null) {
            throw new JsonParseException("null is not a valid value for Checksum");
        }
        else {
            jsonWriter.value(checksum.toString());
        }
    }

    @Override
    public Checksum read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            throw new JsonParseException("null is not a valid value for Checksum");
        }
        String checksumStr = jsonReader.nextString();
        String[] chk = checksumStr.split(":", 2);
        if (chk.length != 2) {
            throw new JsonParseException("Unable to parse checksum");
        }
        switch (chk[0]) {
        case "sha256":
            return new SHA256Checksum(chk[1]);
        default:
            throw new JsonParseException("Unknown checksum type: " + chk[0]);
        }
    }
}
