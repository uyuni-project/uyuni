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

package com.suse.manager.xmlrpc.admin;

import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import com.suse.manager.admin.PaygAdminManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;


/**
 * Payg-as-you-go XMLRPC Handler
 *
 * @xmlrpc.namespace admin
 * @xmlrpc.doc Provides methods to access and modify pay-as-you-go ssh connection data
 */
public class AdminPaygHandler extends BaseHandler {

    private static final Logger LOG = LogManager.getLogger(AdminPaygHandler.class);

    private PaygAdminManager paygAdminManager;

    /**
     * Constructor with an instance of taskomatic API to be used
     * @param taskomaticApi taskomatic api instance
     */
    public AdminPaygHandler(TaskomaticApi taskomaticApi) {
        this.paygAdminManager = new PaygAdminManager(taskomaticApi);
    }

    /**
     *
     * @param loggedInUser The current user
     * @param description Description for the ssh connection data
     * @param host hostname or IP address to the instance
     * @param port shh port to the instance
     * @param username ssh username to connection on the instance
     * @param password ssh password for the instance
     * @param key private key to use in authentication
     * @param keyPassword private key password
     * @return Returns 1 if successful (exception otherwise)
     *
     * @xmlrpc.doc Create a new ssh connection data to extract data from
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "description")
     * @xmlrpc.param #param_desc("string", "host", "hostname or IP address to the instance, will fail if
     * already in use.")
     * @xmlrpc.param #param("int", "port")
     * @xmlrpc.param #param("string", "username")
     * @xmlrpc.param #param("string", "password")
     * @xmlrpc.param #param_desc("string", "key", "private key to use in authentication")
     * @xmlrpc.param #param("string", "keyPassword")
     * @xmlrpc.returntype #return_int_success()
     */
    public int create(User loggedInUser, String description, String host, Integer port, String username,
                      String password, String key, String keyPassword) {

        return create(loggedInUser, description, host, port, username, password, key, keyPassword,
                null, null, null, null, null, null);
    }

    /**
     *
     * @param loggedInUser The current user
     * @param description Description for the ssh connection data
     * @param host hostname or IP address to the instance
     * @param port shh port to the instance
     * @param username ssh username to connection on the instance
     * @param password ssh password for the instance
     * @param key private key to use in authentication
     * @param keyPassword private key password
     * @param bastionHost hostname or IP address to a bastion host
     * @param bastionPort shh port to a bastion instance
     * @param bastionUsername ssh username to connect on bastion
     * @param bastionPassword ssh password for the bastion
     * @param bastionKey private key to use in bastion authentication
     * @param bastionKeyPassword private key password for bastion key
     * @return Returns 1 if successful (exception otherwise)
     *
     * @xmlrpc.doc Create a new ssh connection data to extract data from
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "description")
     * @xmlrpc.param #param_desc("string", "host", "hostname or IP address to the instance, will fail if
     * already in use.")
     * @xmlrpc.param #param("int", "port")
     * @xmlrpc.param #param("string", "username")
     * @xmlrpc.param #param("string", "password")
     * @xmlrpc.param #param_desc("string", "key", "private key to use in authentication")
     * @xmlrpc.param #param("string", "keyPassword")
     * @xmlrpc.param #param_desc("string", "bastionHost", "hostname or IP address to a bastion host")
     * @xmlrpc.param #param("int", "bastionPort")
     * @xmlrpc.param #param("string", "bastionUsername")
     * @xmlrpc.param #param("string", "bastionPassword")
     * @xmlrpc.param #param_desc("string", "bastionKey", "private key to use in bastion authentication")
     * @xmlrpc.param #param("string", "bastionKeyPassword")
     * @xmlrpc.returntype #return_int_success()
     */
    public int create(User loggedInUser, String description, String host, Integer port, String username,
                      String password, String key, String keyPassword,
                      String bastionHost, Integer bastionPort, String bastionUsername,
                      String bastionPassword,
                      String bastionKey, String bastionKeyPassword) {

        ensureSatAdmin(loggedInUser);
        return paygAdminManager.create(description,
                host, port, username, password, key, keyPassword,
                bastionHost, bastionPort, bastionUsername, bastionPassword,
                bastionKey, bastionKeyPassword) != null ? 1 : 0;
    }

    /**
     *
     * @param loggedInUser The current user
     * @param host hostname or IP address to the instance
     * @param details A map containing the new details values
     * @return Returns 1 if successful (exception otherwise)
     *
     * @xmlrpc.doc Updates the details of a ssh connection data
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "host", "hostname or IP address to the instance, will fail if
     * host doesn't exist.")
     * @xmlrpc.param
     *   #struct_desc("details", "user details")
     *     #prop("string", "description")
     *     #prop("int", "port")
     *     #prop("string", "username")
     *     #prop("string", "password")
     *     #prop("string", "key")
     *     #prop("string", "key_password")
     *     #prop("string", "bastion_host")
     *     #prop("int", "bastion_port")
     *     #prop("string", "bastion_username")
     *     #prop("string", "bastion_password")
     *     #prop("string", "bastion_key")
     *     #prop("string", "bastion_key_password")
     *   #struct_end()
     * @xmlrpc.returntype #return_int_success()
     */
    public int setDetails(User loggedInUser, String host, Map details) {

        ensureSatAdmin(loggedInUser);
        return paygAdminManager.setDetails(host,  details) != null ? 1 : 0;
    }

    /**
     *
     * @param loggedInUser The current user
     * @return Returns a list of PaygSshData
     *
     * @xmlrpc.doc Returns a list of ssh connection data registered.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     * #return_array_begin()
     *     $PaygSshDataSerializer
     * #array_end()
     */
    public List<PaygSshData> list(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        return paygAdminManager.list();
    }

    /**
     *
     * @param loggedInUser The current user
     * @param host hostname or IP address to the instance
     * @return Returns a list of PaygSshData
     *
     * @xmlrpc.doc Returns a list of ssh connection data registered.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "host", "hostname or IP address of the instance, will fail if
     * host doesn't exist.")
     * @xmlrpc.returntype $PaygSshDataSerializer
     */
    public PaygSshData getDetails(User loggedInUser, String host) {
        ensureSatAdmin(loggedInUser);
        return paygAdminManager.getDetails(host);
    }


    /**
     *
     * @param loggedInUser The current user
     * @param host hostname or IP address of the instance
     * @return Returns 1 if successful (exception otherwise)
     *
     * @xmlrpc.doc Returns a list of ssh connection data registered.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "host", "hostname or IP address of the instance")
     * @xmlrpc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, String host) {
        ensureSatAdmin(loggedInUser);
        return paygAdminManager.delete(host) ? 1 : 0;
    }
}
