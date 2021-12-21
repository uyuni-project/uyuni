/*
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.virtualization;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.StringReader;

/**
 * Class representing the Virtual Storage Pool XML Definition.
 */
public class PoolDefinition {
    private static final Logger LOG = Logger.getLogger(PoolDefinition.class);

    private String type;
    private String name;
    private String uuid;
    private boolean autostart;

    private PoolTarget target;
    private PoolSource source;

    /**
     * @return the pool type
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn The type to set.
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return pool name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the pool name
     *
     * @param nameIn pool name
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return pool UUID
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuidIn The uuid to set.
     */
    public void setUuid(String uuidIn) {
        uuid = uuidIn;
    }

    /**
     * @return Returns whether the pool is to be autostarted.
     */
    public boolean isAutostart() {
        return autostart;
    }

    /**
     * @param autostartIn whether the pool is to be autostarted.
     */
    public void setAutostart(boolean autostartIn) {
        autostart = autostartIn;
    }

    /**
     * @return Returns the target.
     */
    public PoolTarget getTarget() {
        return target;
    }

    /**
     * @param targetIn The target to set.
     */
    public void setTarget(PoolTarget targetIn) {
        target = targetIn;
    }

    /**
     * @return Returns the source.
     */
    public PoolSource getSource() {
        return source;
    }

    /**
     * @param sourceIn The source to set.
     */
    public void setSource(PoolSource sourceIn) {
        source = sourceIn;
    }

    /**
     * Create a new storage pool definition from a libvirt XML description.
     *
     * @param xmlDef libvirt XML storage pool definition
     * @return parsed definition or {@code null}
     */
    public static PoolDefinition parse(String xmlDef) {
        PoolDefinition def = null;
        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(false);

        try {
            Document doc = builder.build(new StringReader(xmlDef));
            def = new PoolDefinition();

            Element poolElement = doc.getRootElement();
            def.type = poolElement.getAttributeValue("type");
            def.setName(poolElement.getChildText("name"));
            def.uuid = poolElement.getChildText("uuid");

            def.setTarget(PoolTarget.parse(poolElement.getChild("target")));
            def.setSource(PoolSource.parse(poolElement.getChild("source")));
        }
        catch (Exception e) {
            LOG.error("failed to parse libvirt pool XML definition: " + e.getMessage());
            def = null;
        }

        return def;
    }
}
