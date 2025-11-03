/*
 * Copyright (c) 2011 SUSE LLC
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
package com.redhat.rhn.domain.action.image;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * DeployImageActionDetails - Class representation of the table rhnActionImageDeploy.
 */
@Entity
@Table(name = "rhnActionImageDeploy")
public class DeployImageActionDetails extends BaseDomainHelper {

    @Id
    @GeneratedValue(generator = "RHN_ACTION_IMAGE_DEPLOY_ID_SEQ")
    @GenericGenerator(
        name = "RHN_ACTION_IMAGE_DEPLOY_ID_SEQ",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @Parameter(name = "sequence_name", value = "RHN_ACTION_IMAGE_DEPLOY_ID_SEQ"),
                @Parameter(name = "increment_size", value = "1")
        })
    private Long id;

    @Column
    private Long vcpus;

    @Column(name = "mem_kb")
    private Long memKb;

    @Column(name = "bridge_device")
    private String bridgeDevice;

    @Column(name = "download_url")
    private String downloadUrl;

    @Column(name = "proxy_server")
    private String proxyServer;

    @Column(name = "proxy_user")
    private String proxyUser;

    @Column(name = "proxy_pass")
    private String proxyPass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", updatable = false, nullable = false, insertable = true)
    private Action parentAction;

    /**
     * Return the ID.
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the ID.
     * @param idIn id
     */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Return the number of virtual CPUs.
     * @return vcpus
     */
    public Long getVcpus() {
        return vcpus;
    }

    /**
     * Set the number of virtual CPUs.
     * @param vcpusIn vcpus
     */
    public void setVcpus(Long vcpusIn) {
        this.vcpus = vcpusIn;
    }

    /**
     * Return the amount of memory in KB.
     * @return memKb
     */
    public Long getMemKb() {
        return memKb;
    }

    /**
     * Set the amount of memory in KB.
     * @param memkb memory in KB
     */
    public void setMemKb(Long memkb) {
        this.memKb = memkb;
    }

    /**
     * Return the bridge device.
     * @return bridgeDevice
     */
    public String getBridgeDevice() {
        return bridgeDevice;
    }

    /**
     * Set the bridge device.
     * @param bridgeDeviceIn bridge device
     */
    public void setBridgeDevice(String bridgeDeviceIn) {
        this.bridgeDevice = bridgeDeviceIn;
    }

    /**
     * Set the download URL.
     * @return downloadUrl
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * Return the download URL.
     * @param downloadUrlIn download URL
     */
    public void setDownloadUrl(String downloadUrlIn) {
        this.downloadUrl = downloadUrlIn;
    }

    /**
     * Return the proxy server.
     * @return proxyServer
     */
    public String getProxyServer() {
        return proxyServer;
    }

    /**
     * Set the proxy server.
     * @param proxyServerIn proxy server
     */
    public void setProxyServer(String proxyServerIn) {
        this.proxyServer = proxyServerIn;
    }

    /**
     * Return the proxy user.s
     * @return proxyUser
     */
    public String getProxyUser() {
        return proxyUser;
    }

    /**
     * Set the proxy user.
     * @param proxyUserIn proxy user
     */
    public void setProxyUser(String proxyUserIn) {
        this.proxyUser = proxyUserIn;
    }

    /**
     * Return the proxy password.
     * @return proxyPass
     */
    public String getProxyPass() {
        return proxyPass;
    }

    /**
     * Set the proxy password.
     * @param proxyPassIn proxy password
     */
    public void setProxyPass(String proxyPassIn) {
        this.proxyPass = proxyPassIn;
    }

    /**
     * Gets the parent Action associated with this ServerAction record
     * @return Returns the parentAction.
     */
    public Action getParentAction() {
        return parentAction;
    }

    /**
     * Sets the parent Action associated with this ServerAction record
     * @param parentActionIn The parentAction to set.
     */
    public void setParentAction(Action parentActionIn) {
        this.parentAction = parentActionIn;
    }
}
