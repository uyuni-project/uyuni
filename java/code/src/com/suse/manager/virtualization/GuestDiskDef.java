/**
 * Copyright (c) 2018 SUSE LLC
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

import org.jdom.Attribute;
import org.jdom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class representing the VM disk device XML definition.
 */
public class GuestDiskDef {

    private String type;
    private String device;
    private String format;
    private String target;
    private String bus;
    private Map<String, Object> source;

    /**
     * @return Returns the type.
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
     * @return Returns the device.
     */
    public String getDevice() {
        return device;
    }

    /**
     * @param deviceIn The device to set.
     */
    public void setDevice(String deviceIn) {
        device = deviceIn;
    }

    /**
     * @return Returns the format.
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param formatIn The format to set.
     */
    public void setFormat(String formatIn) {
        format = formatIn;
    }

    /**
     * @return Returns the target.
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param targetIn The target to set.
     */
    public void setTarget(String targetIn) {
        target = targetIn;
    }

    /**
     * @return Returns the bus.
     */
    public String getBus() {
        return bus;
    }

    /**
     * @param busIn The bus to set.
     */
    public void setBus(String busIn) {
        bus = busIn;
    }

    /**
     * @return Returns the source.
     */
    public Map<String, Object> getSource() {
        return source;
    }

    /**
     * @param sourceIn The source to set.
     */
    public void setSource(Map<String, Object> sourceIn) {
        source = sourceIn;
    }

    /**
     * Parse the &lt;disk&gt; element of a domain XML definition
     *
     * @param node the disk element
     * @return the created disk object
     *
     * @throws IllegalArgumentException if the input element is badly formatted
     */
    @SuppressWarnings("unchecked")
    public static GuestDiskDef parse(Element node) throws IllegalArgumentException {
        GuestDiskDef disk = new GuestDiskDef();
        disk.setType(node.getAttributeValue("type"));
        disk.setDevice(node.getAttributeValue("device", "disk"));

        Element targetNode = node.getChild("target");
        if (targetNode == null) {
            throw new IllegalArgumentException("Missing disk device target");
        }
        disk.setBus(targetNode.getAttributeValue("bus"));
        disk.setTarget(targetNode.getAttributeValue("dev"));

        Element driverNode = node.getChild("driver");
        if (driverNode != null) {
            disk.setFormat(driverNode.getAttributeValue("type"));
        }

        Element sourceNode = node.getChild("source");
        if (sourceNode != null) {
            // Declare functions extracting the data from each source type.
            // extractors maps a disk type to a map of properties / functions to get the value
            Map<String, Map<String, Function<Element, Object>>> extractors = new HashMap<>();

            Map<String, Function<Element, Object>> fileExtractors = new HashMap<>();
            fileExtractors.put("file", element -> element.getAttributeValue("file"));
            extractors.put("file", fileExtractors);

            Map<String, Function<Element, Object>> dirExtractors = new HashMap<>();
            dirExtractors.put("dir", element -> element.getAttributeValue("dir"));
            extractors.put("dir", dirExtractors);

            Map<String, Function<Element, Object>> blockExtractors = new HashMap<>();
            blockExtractors.put("dev", element -> element.getAttributeValue("dev"));
            extractors.put("block", blockExtractors);

            Map<String, Function<Element, Object>> netExtractors = new HashMap<>();
            netExtractors.put("protocol", element -> element.getAttributeValue("protocol"));
            netExtractors.put("name", element -> element.getAttributeValue("name"));
            netExtractors.put("host", element -> ((List<Element>)element.getChildren("host")).stream()
                    .map(hostNode -> ((List<Attribute>)hostNode.getAttributes()).stream()
                            .collect(Collectors.toMap(attr -> attr.getName(), attr -> attr.getValue()))));
            extractors.put("network", netExtractors);

            Map<String, Function<Element, Object>> volumeExtractors = new HashMap<>();
            volumeExtractors.put("pool", element -> element.getAttributeValue("pool"));
            volumeExtractors.put("volume", element -> element.getAttributeValue("volume"));
            extractors.put("volume", volumeExtractors);

            // Now extract the data for the source
            Map<String, Object> source = new HashMap<>();
            Map<String, Function<Element, Object>> extractor = extractors.get(disk.getType());
            if (extractor != null) {
                extractor.forEach((key, func) -> {
                    Object value = func.apply(sourceNode);
                    if (value != null) {
                        source.put(key, value);
                    }
                });
            }
            disk.setSource(source);
        }

        return disk;
    }
}
