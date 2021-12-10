/*
 * Copyright (c) 2021 SUSE LLC
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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper functions to call the virtualization states
 */
public abstract  class VirtStatesHelper {

    /**
     * Convert a Range object to a map with start and end keys.
     *
     * @param data the Range to convert
     * @param <T> the type of the value contained in the Range
     * @return the pillar fragment
     */
    public static <T> Map<String, T> rangeToPillar(Range<T> data) {
        Map<String, T> pillar = new HashMap<>();
        pillar.put("start", data.getStart());
        pillar.put("end", data.getEnd());
        return pillar;
    }


    /**
     * Convert an IpDef object to pillar data.
     *
     * @param ip the IpDef to convert
     * @return the pillar fragment
     */
    public static Map<String, Object> ipToPillar(IpDef ip) {
        Map<String, Object> pillar = new HashMap<>();
        pillar.put("cidr", ip.getAddress() + "/" + ip.getPrefix());
        ip.getBootpFile().ifPresent(path -> {
            Map<String, Object> bootp = new HashMap<>();
            bootp.put("file", path);
            ip.getBootpServer().ifPresent(server -> bootp.put("server", server));
            pillar.put("bootp", bootp);
        });
        ip.getTftp().ifPresent(tftp -> pillar.put("tftp", tftp));
        if (!ip.getDhcpRanges().isEmpty()) {
            pillar.put("dhcp_ranges", ip.getDhcpRanges().stream()
                .map(VirtStatesHelper::rangeToPillar)
                .collect(Collectors.toList()));
        }
        if (!ip.getHosts().isEmpty()) {
            pillar.put("hosts", ip.getHosts().stream()
                .collect(Collectors.toMap(
                    DhcpHostDef::getIp,
                    host -> {
                        Map<String, Object> res = new HashMap<>();
                        host.getMac().ifPresent(mac -> res.put("mac", mac));
                        host.getId().ifPresent(id -> res.put("id", id));
                        host.getName().ifPresent(name -> res.put("name", name));
                        return res;
                    })));
        }
        return pillar;
    }
}
