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

import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.ServerNetAddress6;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * NetworkInterfaceSerializer
 *
 * @xmlrpc.doc
 *      #struct_begin("network device")
 *          #prop_desc("string", "ip", "IP address assigned to this network device")
 *          #prop_desc("string", "interface", "Network interface assigned to device e.g.
 *                              eth0")
 *          #prop_desc("string", "netmask", "Network mask assigned to device")
 *          #prop_desc("string", "hardware_address", "Hardware Address of device.")
 *          #prop_desc("string", "module", "Network driver used for this device.")
 *          #prop_desc("string", "broadcast", " Broadcast address for device.")
 *          #prop_desc("array", "ipv6", "List of IPv6 addresses")
 *            #array_begin()
 *               #struct_begin("ipv6 address")
 *                 #prop_desc("string", "address", "IPv6 address of this network device")
 *                 #prop_desc("string", "netmask", "IPv6 netmask of this network device")
 *                 #prop_desc("string", "scope", "IPv6 address scope")
 *               #struct_end()
 *            #array_end()
 *          #prop_desc("array", "ipv4", "List of IPv4 addresses")
 *            #array_begin()
 *               #struct_begin("ipv4 address")
 *                 #prop_desc("string", "address", "IPv4 address of this network device")
 *                 #prop_desc("string", "netmask", "IPv4 netmask of this network device")
 *                 #prop_desc("string", "broadcast", "IPv4 broadcast address of this network device")
 *               #struct_end()
 *            #array_end()
 *      #struct_end()
 *
 */
public class NetworkInterfaceSerializer extends ApiResponseSerializer<NetworkInterface> {

    @Override
    public Class<NetworkInterface> getSupportedClass() {
        return NetworkInterface.class;
    }

    @Override
    public SerializedApiResponse serialize(NetworkInterface src) {
        ArrayList<Map<String, String>> ipv6List = new ArrayList<>();
        ArrayList<Map<String, String>> ipv4List = new ArrayList<>();

        for (ServerNetAddress6 addr : src.getIPv6Addresses()) {
            Map<String, String> m = new HashMap<>();
            m.put("address", StringUtils.defaultString(addr.getAddress()));
            m.put("netmask", StringUtils.defaultString(addr.getNetmask()));
            m.put("scope", StringUtils.defaultString(addr.getScope()));
            ipv6List.add(m);
        }
        for (ServerNetAddress4 addr : src.getIPv4Addresses()) {
            Map<String, String> m = new HashMap<>();
            m.put("address", StringUtils.defaultString(addr.getAddress()));
            m.put("netmask", StringUtils.defaultString(addr.getNetmask()));
            m.put("broadcast", StringUtils.defaultString(addr.getBroadcast()));
            ipv4List.add(m);
      }

        SerializationBuilder builder = new SerializationBuilder()
                .add("interface", StringUtils.defaultString(src.getName()))
                .add("ipv6", ipv6List)
                .add("ipv4", ipv4List);

        // for backward compatibility reason we return the first IP direct
        if (ipv4List.isEmpty()) {
            builder.add("ip", "")
                    .add("netmask", "")
                    .add("broadcast", "");
        }
        else {
            builder.add("ip", StringUtils.defaultString(ipv4List.get(0).get("address")))
                    .add("netmask", StringUtils.defaultString(ipv4List.get(0).get("netmask")))
                    .add("broadcast", StringUtils.defaultString(ipv4List.get(0).get("broadcast")));
        }
        builder.add("hardware_address", StringUtils.defaultString(src.getHwaddr()))
                .add("module", StringUtils.defaultString(src.getModule()));
        return builder.build();
    }
}
