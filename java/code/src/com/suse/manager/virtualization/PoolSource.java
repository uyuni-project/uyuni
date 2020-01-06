/**
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

import java.util.List;

/**
 * Represents a virtual pool storage source definition
 */
public class PoolSource {

    private String dir;
    private List<PoolSourceDevice> devices;
    private List<String> hosts;
    private PoolSourceAdapter adapter;
    private PoolSourceAuthentication auth;
    private String name;
    private String format;
    private String initiator;

    /**
     * @return Returns the source directory.
     */
    public String getDir() {
        return dir;
    }

    /**
     * @param dirIn The source directory to set.
     */
    public void setDir(String dirIn) {
        dir = dirIn;
    }

    /**
     * @return Returns the source devices.
     */
    public List<PoolSourceDevice> getDevices() {
        return devices;
    }

    /**
     * @param devicesIn The source devices to set.
     */
    public void setDevices(List<PoolSourceDevice> devicesIn) {
        devices = devicesIn;
    }

    /**
     * @return Returns the source hosts.
     */
    public List<String> getHosts() {
        return hosts;
    }

    /**
     * @param hostsIn The source hosts to set.
     */
    public void setHosts(List<String> hostsIn) {
        hosts = hostsIn;
    }

    /**
     * @return Returns the source SCSI adapter.
     */
    public PoolSourceAdapter getAdapter() {
        return adapter;
    }

    /**
     * @param adapterIn The source SCSI adapter to set.
     */
    public void setAdapter(PoolSourceAdapter adapterIn) {
        adapter = adapterIn;
    }

    /**
     * @return Returns the source authentication.
     */
    public PoolSourceAuthentication getAuth() {
        return auth;
    }

    /**
     * @param authIn The source authentication to set.
     */
    public void setAuth(PoolSourceAuthentication authIn) {
        auth = authIn;
    }

    /**
     * @return Returns the source name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn The source name to set.
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * @return Returns the source format.
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param formatIn The source format to set.
     */
    public void setFormat(String formatIn) {
        format = formatIn;
    }

    /**
     * @return Returns the initiator IQN.
     */
    public String getInitiator() {
        return initiator;
    }

    /**
     * @param initiatorIn The initiator IQN to set.
     */
    public void setInitiator(String initiatorIn) {
        initiator = initiatorIn;
    }
}
