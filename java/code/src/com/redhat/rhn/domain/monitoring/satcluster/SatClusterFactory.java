/**
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
package com.redhat.rhn.domain.monitoring.satcluster;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.MD5Crypt;
import com.redhat.rhn.domain.user.User;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SatClusterFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.monitoring.config.* objects from the
 * database.
 * @version $Rev: 51602 $
 */
public class SatClusterFactory extends HibernateFactory {

    private static SatClusterFactory singleton = new SatClusterFactory();
    private static Logger log = Logger.getLogger(SatClusterFactory.class);
    // There is only one of these
    private static final PhysicalLocation PHYSICAL_LOCATION =
        lookupPhysicalLocation(new Long(1));

    private static final Long DEFAULT_LOG_LEVEL = new Long(1);

    private SatClusterFactory() {
        super();
    }

    private static PhysicalLocation lookupPhysicalLocation(Long id) {
        Session session = HibernateFactory.getSession();
        PhysicalLocation u = (PhysicalLocation)
            session.get(PhysicalLocation.class, id);
        return u;
    }

    /**
     * Get the Logger for the derived class so log messages
     * show up on the correct class
     */
    protected Logger getLogger() {
        return log;
    }

    /**
     * Return the <code>SatCluster</code> with ID <code>satClusterID</code>
     * @param satClusterID the ID of the <code>SatCluster</code> to find
     * @return the <code>SatCluster</code> with ID <code>satClusterID</code>
     */
    public static SatCluster findSatClusterById(Long satClusterID) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("satClusterID", satClusterID);
        return (SatCluster)
            singleton.lookupObjectByNamedQuery("SatCluster.findByID", params);
    }

    /**
     * Return the default SatCluster created during satellite installation.
     *
     * @return Default SatCluster.
     */
    public static SatCluster getDefaultSatCluster() {
            SatCluster scout = SatClusterFactory.findSatClusterById(
                    new Long(1));
            return scout;
    }

    /**
     * Create a new SatCluster (scout)
     * @param user who creates the Scout
     * @return new instance
     */
    public static SatCluster createSatCluster(User user) {
        SatCluster retval = new SatCluster();
        CommandTarget ct = new CommandTarget();
        ct.setOrg(user.getOrg());
        ct.setTargetType("cluster");
        retval.setCommandTarget(ct);
        retval.setOrg(user.getOrg());
        retval.setPhysicalLocation(PHYSICAL_LOCATION);
        retval.setTargetType(ct.getTargetType());
        retval.setDeployed("1");
        retval.setLastUpdateUser(user.getLogin());
        retval.setLastUpdateDate(new Date());
        try {
            InetAddress ip = InetAddress.getLocalHost();
            boolean haveIpv4 = ip instanceof Inet4Address;
            if (haveIpv4) {
                retval.setVip(ip.getHostAddress());
            }
            else {
                retval.setVip6(ip.getHostAddress());
            }
            NetworkInterface ni = NetworkInterface.getByInetAddress(ip);
            for (InterfaceAddress ifa : ni.getInterfaceAddresses()) {
                InetAddress ia = ifa.getAddress();
                if ((ia instanceof Inet4Address) != haveIpv4) {
                    if (haveIpv4) {
                        retval.setVip6(ia.getHostAddress());
                    }
                    else {
                        retval.setVip(ia.getHostAddress());
                    }
                    break;
                }
            }
        }
        catch (Exception e) {
            log.warn("Failed to find out IP host addresses. " +
                    "Setting loopback IPs instead.");
            try {
                NetworkInterface ni = NetworkInterface.getByName("lo");
                for (InterfaceAddress ifa : ni.getInterfaceAddresses()) {
                    InetAddress ia = ifa.getAddress();
                    if (ia instanceof Inet4Address) {
                        if (StringUtils.isEmpty(retval.getVip())) {
                            retval.setVip(ia.getHostAddress());
                        }
                    }
                    else { //IPv6
                        if (StringUtils.isEmpty(retval.getVip6())) {
                            retval.setVip6(ia.getHostAddress());
                        }
                    }
                }
            }
            catch (SocketException se) {
                log.fatal("Failed to find out loopback IPs.");
                se.printStackTrace();
            }
        }
        return retval;
    }

    /**
     * Create a new SatNode.
     * @param userIn who is creating node
     * @param clusterIn SatCluster associated with this SatNode
     * @return SatNode instance
     */
    public static SatNode createSatNode(User userIn, SatCluster clusterIn) {
        SatNode sn = new SatNode();
        CommandTarget ct = new CommandTarget();
        ct.setOrg(userIn.getOrg());
        ct.setTargetType("node");
        sn.setCommandTarget(ct);
        sn.setSatCluster(clusterIn);
        sn.setIp(clusterIn.getVip());
        sn.setIp6(clusterIn.getVip6());
        sn.setSchedLogLevel(DEFAULT_LOG_LEVEL);
        sn.setDqLogLevel(DEFAULT_LOG_LEVEL);
        sn.setSputLogLevel(DEFAULT_LOG_LEVEL);
        sn.setMacAddress("not set");
        sn.setScoutSharedKey(generateScoutSharedKey());
        sn.setMaxConcurrentChecks(new Long(10));
        sn.setTargetType(ct.getTargetType());
        return sn;
    }

    // Generate a random set of string data
    private static String generateScoutSharedKey() {
        String random = RandomStringUtils.random(128);
        String digest = MD5Crypt.md5Hex(random);
        return digest.substring(0, 12);
    }

    /**
     * Save the SatCluster to the DB
     * @param sc to store.
     */
    public static void saveSatCluster(SatCluster sc) {
        singleton.saveObject(sc);
    }

    /**
     * Save the SatNode to the DB.
     * @param sn to save
     */
    public static void saveSatNode(SatNode sn) {
        singleton.saveObject(sn);

    }

    /**
     * Lookup a SatNode based on the SatCluster id.
     * @param clusterIn associated with this SatNode
     * @return SatNode if found.
     */
    public static SatNode lookupSatNodeByCluster(SatCluster clusterIn) {
        return (SatNode) HibernateFactory.getSession()
                      .getNamedQuery("SatNode.findBySatCluster")
                      .setEntity("satCluster", clusterIn)
                      .uniqueResult();
    }

    /**
     * Return list of satClusters regardless of Org
     * @return list of sat clusters
     */
    public static List<SatCluster> findSatClusters() {
        Map<String, Object> params = new HashMap<String, Object>();
        return singleton.listObjectsByNamedQuery("SatCluster.findAll", params);
    }

}

