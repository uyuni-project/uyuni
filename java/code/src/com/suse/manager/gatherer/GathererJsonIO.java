/**
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.gatherer;

import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.suse.manager.model.gatherer.GathererModule;

import java.io.IOException;
import java.util.Map;

/**
 * Json IO handler to and from virtual-host-gatherer
 */
public class GathererJsonIO {

    /** (De)serializer instance. */
    private Gson gson;

    /** Default constructor. */
    public GathererJsonIO() {
        gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(GathererModule.class, new GathererModuleAdapter())
            .registerTypeAdapter(VirtualHostManager.class, new VHMAdapter())
            .create();
    }

    /**
     * Read virtual-host-gatherer modules from its JSON output.
     *
     * @param reader the reader object
     * @return a map from module name to {@link GathererModule} object
     * @throws JsonSyntaxException in case JSON does not have correct syntax
     */
    public Map<String, GathererModule> readGathererModules(String reader)
            throws JsonSyntaxException {
        return  gson.fromJson(reader,
                new TypeToken<Map<String, GathererModule>>() { }.getType());
    }

    /**
     * Read Hosts from virtual-host-gatherer's JSON output.
     *
     * @param reader the reader object
     * @return a map from virtual host manager names to
     * (virtual name, {@link HostJson}) pairs
     * @throws JsonSyntaxException in case JSON does not have correct syntax
     */
    public Map<String, Map<String, HostJson>> readHosts(String reader)
            throws JsonSyntaxException {
        return  gson.fromJson(reader,
                new TypeToken<Map<String, Map<String, HostJson>>>() { }.getType());
    }

    /**
     * Write Object to Json
     * @param o an object
     * @return the corresponding JSON string
     */
    public String toJson(Object o) {
        return gson.toJson(o);
    }

    /**
     * (De)serializer for GathererModule class.
     * Breaks the incoming arguments into module and parameter list
     * and fills a new GathererModule instance.
     */
    public class GathererModuleAdapter extends TypeAdapter<GathererModule> {
        /**
         * {@inheritDoc}
         */
        @Override
        public GathererModule read(JsonReader reader) throws IOException {
            GathererModule gm = new GathererModule();
            reader.beginObject();
            while (reader.hasNext()) {
              String key = reader.nextName();
              if (key.equals("module")) {
                gm.setName(reader.nextString());
              }
              else {
                  gm.addParameter(key, reader.nextString());
              }
            }
            reader.endObject();
            return gm;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(JsonWriter writer, GathererModule value) throws IOException {
            writer.beginObject();
            writer.name("module").value(value.getName());
            for (Map.Entry<String, String> e : value.getParameters().entrySet()) {
                writer.name(e.getKey()).value(e.getValue());
            }
            writer.endObject();
        }
    }

    /**
     * Serializer for VirtualHostManager class.
     * Convert the Object into the correct JSON representation
     */
    public class VHMAdapter extends TypeAdapter<VirtualHostManager> {
        /**
         * {@inheritDoc}
         */
        @Override
        public VirtualHostManager read(JsonReader reader) throws IOException {
            throw new UnsupportedOperationException("Reading VirtualHostManager " +
                    "from JSON is not supported.");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(JsonWriter writer, VirtualHostManager value) throws IOException {
            writer.beginObject();
            writer.name("id").value(value.getLabel());
            writer.name("module").value(value.getGathererModule());
            if (value.getCredentials() != null) {
                writer.name("username").value(value.getCredentials().getUsername());
                writer.name("password").value(value.getCredentials().getPassword());
            }
            for (VirtualHostManagerConfig c : value.getConfigs()) {
                writer.name(c.getParameter()).value(c.getValue());
            }
            writer.endObject();
        }
    }
}
