/*
 * Copyright (c) 2010--2011 Red Hat, Inc.
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
package com.redhat.rhn.frontend.dto;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.ServerNetAddress6;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.token.ActivationKeyManager;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * SystemCompareDto
 */
public class SystemCompareDto {

    private List<Server> servers;
    private User user;
    /**
     * Constructor to setup System Compare objects
     * @param systems the list of systems to compare
     * @param u the user object
     */
    public SystemCompareDto(List<Server> systems, User u) {
        servers = systems;
        user = u;
    }

    /**
     * list of nicely formatted last checkin dates in the same order
     * as the systems were added
     * @return the last checkin dates
     */
    public List<Item> getLastCheckinDates() {
        LocalizationService ls = LocalizationService.getInstance();
        List<String> ret = new LinkedList<>();
        for (Server server : servers) {
            ret.add(ls.formatDate(server.getLastCheckin()));
        }
        return compare(ret);
    }

    /**
     * @return the list of servers being compared
     */
    public List<Server> getServers() {
        return servers;
    }

    /**
     * @return the number of servers
     */
    public int getSize() {
        return servers.size();
    }


    private List<List<Item>> compareList(List<List> lists) {
        return compareList(lists, Collections.emptyMap());
    }

    /**
     * Call this compare things lists of tuples like
     *  [ [1,2,3] . [5,2], [9]  ] ->
     *  Note we expect unique set of items
     * @param lists lists of tuples
     * @param idMap a map of value : id if its needed
     * @return a List of List of items
     */
    private List<List<Item>> compareList(List<List> lists, Map<String, String> idMap) {
        List<List<Item>> compared = new LinkedList<>();
        Map<String, Integer> similarity = new HashMap<>();
        for (List list : lists) {
            List<Item> itemized = new LinkedList<>();
            for (Object o : list) {
                String s = (o != null) ? String.valueOf(o).trim() : "";
                Item i = new Item();
                i.value = s;
                itemized.add(i);
                if (!similarity.containsKey(s)) {
                    similarity.put(s, 0);
                }
                similarity.put(s, similarity.get(s) + 1);
            }
            compared.add(itemized);
        }

        for (List<Item> items : compared) {
            for (Item item : items) {
                item.similar = !StringUtils.isBlank(item.value) &&
                                            similarity.get(item.value) > 1;
                item.id = idMap.get(item.value);
            }
        }
        return compared;
    }

    private List<Item> compare(List strings) {
        return compare(strings, Collections.emptyMap());
    }

    private List<Item> compare(List strings, Map<String, String> idMap) {
        List<Item> compared = new LinkedList<>();
        Map<String, Integer> similarity = new HashMap<>();
        for (Object o : strings) {
            String s = (o != null) ? String.valueOf(o).trim() : "";
            Item i = new Item();
            i.value = s;
            compared.add(i);
            if (!similarity.containsKey(s)) {
                similarity.put(s, 0);
            }
            similarity.put(s, similarity.get(s) + 1);
        }

        for (Item item : compared) {
            item.similar = !StringUtils.isBlank(item.value) &&
                                    similarity.get(item.value) > 1;
            item.id = idMap.get(item.value);
        }
        return compared;
    }

    /**
     * Returns a list of ( mac addresses available per system)
     * The similar items will be marked accordingly
     * @return a list of Mac addresses per system
     */

    public List<List<Item>> getMacAddresses() {
        List<List> addresses = new LinkedList<>();
        for (Server system : servers) {
            addresses.add(getMacAddresses(system));
        }
        return compareList(addresses);
    }

    /**
     * Returns a list of ( ip addresses available per system)
     * @return a list of ip addresses per system
     */
    public List<List<Item>> getIpAddresses() {
        List<List> addresses = new LinkedList<>();
        for (Server system : servers) {
            addresses.add(getIpAddresses(system));
        }
        return compareList(addresses);
    }

    /**
     * Returns a list of ( ipv6 addresses available per system)
     * @return a list of ip addresses per system
     */
    public List<List<Item>> getIpv6Addresses() {
        List<List> addresses = new LinkedList<>();
        for (Server system : servers) {
            addresses.add(getIpv6Addresses(system));
        }
        return compareList(addresses);
    }

    /**
     * Returns a list of (system groups available per system)
     * @return a list of system groups per system
     */
    public List<List<Item>> getSystemGroups() {
        List<List> groups = new LinkedList<>();
        Map<String, String> idMap = new HashMap<>();
        for (Server system : servers) {
            List sysGroups = new LinkedList<>();
            for (ServerGroup sg : system.getManagedGroups()) {
                sysGroups.add(sg.getName());
                idMap.put(sg.getName(), sg.getId().toString());
            }
            groups.add(sysGroups);
        }
        return compareList(groups, idMap);
    }

    /**
     * List of registration dates in the same order as the system list passed in
     * @return the list of registration dates
     */
    public List<Item> getRegistrationDates() {
        LocalizationService ls = LocalizationService.getInstance();
        List<String> dates = new LinkedList<>();
        for (Server s : servers) {
            dates.add(ls.formatDate(s.getCreated()));
        }
        return compare(dates);
    }

    /**
     * List of system ids in the same order as the system list passed in
     * @return the list of system ids
     */
    public List<Item> getSystemIds() {
        List<String> ids = new LinkedList<>();
        for (Server s : servers) {
            ids.add(s.getId().toString());
        }
        return compare(ids);
    }

    /**
     * List of base channels in the same order as the system list passed in
     * @return the list of base channels
     */
    public List<Item> getBaseChannels() {
        List<String> ids = new LinkedList<>();
        Map<String, String> idMap = new HashMap<>();
        for (Server s : servers) {
            if (s.getBaseChannel() != null) {
                ids.add(s.getBaseChannel().getName());
                idMap.put(s.getBaseChannel().getName(),
                        s.getBaseChannel().getId().toString());
            }
            else {
                ids.add("");
            }

        }
        return compare(ids, idMap);
    }
    /**
     * Returns a list of (child channels per system)
     * @return a list of child channels per system
     */
    public List<List<Item>> getChildChannels() {
        List<List> ret = new LinkedList<>();
        Map<String, String> idMap = new HashMap<>();
        for (Server system : servers) {
            List keys = new LinkedList<>();
            Set<Channel> childChannels = system.getChildChannels();
            if (childChannels != null) {
                for (Channel channel : childChannels) {
                    keys.add(channel.getName());
                    idMap.put(channel.getName(), channel.getId().toString());
                }
            }
            ret.add(keys);
        }
        return compareList(ret, idMap);
    }


    /**
     * Returns a list of (configuration channels per system)
     * @return a list of configuration channels per system
     */
    public List<List<Item>> getConfigChannels() {
        List<List> ret = new LinkedList<>();
        Map<String, String> idMap = new HashMap<>();
        for (Server system : servers) {
            List keys = new LinkedList<>();
            if (system.getConfigChannelCount() > 0) {
                system.getConfigChannelStream().forEach(ch -> {
                    keys.add(ch.getName());
                    idMap.put(ch.getName(), ch.getId().toString());
                });
            }

            ret.add(keys);
        }
        return compareList(ret, idMap);
    }

    /**
     * Returns a list of (system addon entitlements per system)
     * @return a list of system add-on entitlements per system
     */
    public List<List<Item>> getSystemEntitlements() {
        List<List> ret = new LinkedList<>();
        for (Server system : servers) {
            List keys = new LinkedList<>();
            if (!system.getEntitlements().isEmpty()) {
                if (system.getAddOnEntitlements().isEmpty()) {
                    keys.add(EntitlementManager.MANAGEMENT.getHumanReadableTypeLabel());
                }
                else {
                    for (Entitlement ent :  system.getAddOnEntitlements()) {
                        keys.add(ent.getHumanReadableLabel());
                    }

                }

            }
            else {
                LocalizationService ls = LocalizationService.getInstance();
                keys.add(ls.getMessage("Unentitled"));
            }
            ret.add(keys);
        }
        return compareList(ret);
    }

    /**
     * Returns a list of (channel family entitlements per system)
     * @return a list of channel family entitlements per system
     */
    public List<List<Item>> getSoftwareEntitlements() {
        List<List> ret = new LinkedList<>();
        for (Server system : servers) {
            List keys = new LinkedList<>();

            if (system.getBaseChannel() != null && !system.getBaseChannel().isCustom()) {
                keys.add(system.getBaseChannel().getChannelFamily().getName());
            }

            if (system.getChildChannels() != null) {
                for (Channel channel : system.getChildChannels()) {
                    if (!channel.isCustom()) {
                        keys.add(channel.getChannelFamily().getName());
                    }
                }
            }
            ret.add(keys);
        }
        return compareList(ret);
    }

    /**
     * Returns a list of (activation keys used per system)
     * @return a list of activation keys used per system
     */
    public List<List<Item>> getActivationKeys() {
        List<List> ret = new LinkedList<>();
        Map<String, String> idMap = new HashMap<>();
        ActivationKeyManager akm = ActivationKeyManager.getInstance();
        for (Server system : servers) {
            List keys = new LinkedList<>();
            for (ActivationKey ak : akm.findByServer(system, user)) {
                keys.add(ak.getKey());
                idMap.put(ak.getKey(), ak.getToken().getId().toString());
            }
            ret.add(keys);
        }
        return compareList(ret, idMap);
    }



    private List<String> getMacAddresses(Server system) {
        List<String> macs = new LinkedList<>();
        for (NetworkInterface n : system.getNetworkInterfaces()) {
            if (!n.getIPv4Addresses().isEmpty()) {
                for (ServerNetAddress4 addr : n.getIPv4Addresses()) {
                    if (!addr.getAddress().equals("127.0.0.1") &&
                        !addr.getAddress().equals("127.0.0.2")) {
                        macs.add(n.getHwaddr());
                        break;
                    }
                }
            }
        }
        return macs;
    }

    private List<String> getIpAddresses(Server system) {
        List<String> macs = new LinkedList<>();
        for (NetworkInterface n : system.getNetworkInterfaces()) {
            if (!n.getIPv4Addresses().isEmpty()) {
                for (ServerNetAddress4 addr : n.getIPv4Addresses()) {
                    if (!addr.getAddress().equals("127.0.0.1") &&
                        !addr.getAddress().equals("127.0.0.2")) {
                        macs.add(addr.getAddress());
                    }
                }
            }
        }
        return macs;
    }

    private List<String> getIpv6Addresses(Server system) {
        List<String> addrs = new LinkedList<>();
        for (NetworkInterface n : system.getNetworkInterfaces()) {
            for (ServerNetAddress6 ad6 : n.getIPv6Addresses()) {
                String addr = ad6.getAddress();
                if (addr != null && !addr.equals("::1")) {
                    addrs.add(addr);
                }
            }
        }
        return addrs;
    }

    /**
     * An item object to represent
     * value/ id/ and similarity
     * Item
     */
    public static class Item {
        private String value;
        private boolean similar;
        private String id;
        /**
         * @return Returns the value.
         */
        public String getValue() {
            return value;
        }

        /**
         * @return Returns the similar.
         */
        public boolean isSimilar() {
            return similar;
        }


        /**
         * @return Returns the id.
         */
        public String getId() {
            return id;
        }

    }
}
