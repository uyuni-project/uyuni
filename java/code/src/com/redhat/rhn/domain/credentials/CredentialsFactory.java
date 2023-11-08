/*
 * Copyright (c) 2012 SUSE LLC
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

package com.redhat.rhn.domain.credentials;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.AESCryptException;
import com.redhat.rhn.common.util.CryptHelper;
import com.redhat.rhn.common.util.FileUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * CredentialsFactory
 */
public class CredentialsFactory extends HibernateFactory {

    private static CredentialsFactory singleton = new CredentialsFactory();
    private static Logger log = LogManager.getLogger(CredentialsFactory.class);
    public static final String SALTED_MAGIC_B64 = "U2FsdGVkX1"; // "Salted_" base64 encoded

    private CredentialsFactory() {
        super();
    }

    /**
     * Create new empty {@link Credentials}.
     * @return new empty credentials
     */
    public static Credentials createCredentials() {
        return new Credentials();
    }

    /**
     * Store {@link Credentials} to the database.
     * @param creds credentials
     */
    public static void storeCredentials(Credentials creds) {
        creds.setModified(new Date());
        singleton.saveObject(creds);
    }

    /**
     * Delete {@link Credentials} from the database.
     * @param creds credentials
     */
    public static void removeCredentials(Credentials creds) {
        singleton.removeObject(creds);
    }

    /**
     * Find a {@link CredentialsType} by a given label.
     * @param label label
     * @return CredentialsType instance for given label
     */
    public static CredentialsType findCredentialsTypeByLabel(String label) {
        if (label == null) {
            return null;
        }
        return singleton.lookupObjectByNamedQuery("CredentialsType.findByLabel", Map.of("label", label));
    }

    /**
     * Helper method for creating new SCC {@link Credentials}
     * @return new credential with type SCC
     */
    public static Credentials createSCCCredentials() {
        Credentials creds = createCredentials();
        creds.setType(CredentialsFactory
                .findCredentialsTypeByLabel(Credentials.TYPE_SCC));
        return creds;
    }

    /**
     * Helper method for looking up SCC credentials.
     * @return credentials or null
     */
    public static List<Credentials> listSCCCredentials() {
        return listCredentialsByType(Credentials.TYPE_SCC);
    }

    /**
     * Helper method for creating new Virtual Host Manager {@link Credentials}
     * @return new credential with type Virtual Host Manager
     */
    public static Credentials createVHMCredentials() {
        Credentials creds = createCredentials();
        creds.setType(CredentialsFactory
                .findCredentialsTypeByLabel(Credentials.TYPE_VIRT_HOST_MANAGER));
        return creds;
    }

    /**
     * Find credentials by ID.
     * @param id the id
     * @return credentials object or null
     */
    public static Credentials lookupCredentialsById(long id) {
        Session session = getSession();
        Credentials creds = session.get(Credentials.class, id);
        return creds;
    }

    /**
     * Helper method for creating new Registry {@link Credentials}
     * @return new credential with type Registry
     */
    public static Credentials createRegistryCredentials() {
        Credentials creds = createCredentials();
        creds.setType(CredentialsFactory
                .findCredentialsTypeByLabel(Credentials.TYPE_REGISTRY));
        return creds;
    }

    /**
     * Helper method for creating new Cloud Rmt {@link Credentials}
     * @return new credential with type Cloud Rmt
     */
    public static Credentials createCloudRmtCredentials() {
        Credentials creds = createCredentials();
        creds.setType(CredentialsFactory
                .findCredentialsTypeByLabel(Credentials.TYPE_CLOUD_RMT));
        return creds;
    }

    /**
     * Helper method for creating new RHUI {@link Credentials}
     * @return new credential with type RHUI
     */
    public static Credentials createRhuiCredentials() {
        Credentials creds = createCredentials();
        creds.setType(CredentialsFactory
                .findCredentialsTypeByLabel(Credentials.TYPE_RHUI));
        return creds;
    }

    /**
     * Helper method for creating new Virtual Host Manager {@link Credentials}
     * @return new credential with type Virtual Host Manager
     */
    public static Credentials createReportCredentials() {
        Credentials creds = createCredentials();
        creds.setType(CredentialsFactory
                .findCredentialsTypeByLabel(Credentials.TYPE_REPORT_CREDS));
        return creds;
    }

    /**
     * Create Credentials of a specific type
     * @param username - the username
     * @param password - the password
     * @param credentialsType - credentials type
     * @return new Credentials instance
     */
    public static Credentials createCredentials(String username, String password, String credentialsType)
            throws AESCryptException {
        if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException("no username provided");
        }

        Credentials credentials = null;
        if (credentialsType.equals(Credentials.TYPE_REGISTRY)) {
            credentials = CredentialsFactory.createRegistryCredentials();
        }
        else if (credentialsType.equals(Credentials.TYPE_VIRT_HOST_MANAGER)) {
            credentials = CredentialsFactory.createVHMCredentials();
        }
        else if (credentialsType.equals(Credentials.TYPE_SCC)) {
            credentials = CredentialsFactory.createSCCCredentials();
        }
        else if (credentialsType.equals(Credentials.TYPE_CLOUD_RMT)) {
            credentials = CredentialsFactory.createCloudRmtCredentials();
        }
        else if (credentialsType.equals(Credentials.TYPE_RHUI)) {
            credentials = CredentialsFactory.createRhuiCredentials();
        }
        else if (credentialsType.equals(Credentials.TYPE_REPORT_CREDS)) {
            credentials = CredentialsFactory.createReportCredentials();
        }
        else {
            return credentials;
        }
        credentials.setUsername(username);
        credentials.setPassword(password);
        CredentialsFactory.storeCredentials(credentials);

        return credentials;
    }

    /**
     * @param type the credential type label
     * @return return a list of credentials of the given type
     */
    public static List<Credentials> listCredentialsByType(String type) {
        return getSession()
                .createNamedQuery("Credentials.listByType", Credentials.class)
                .setParameter("type", findCredentialsTypeByLabel(type))
                .list();
    }

    /**
     * Re-encode the passsword of all credentials with a new master password.
     * The old master password is required. Rename the master password file and append the suffix ".old".
     * Put the new password in the default master password file.
     * If the password is just base64 encoded, this method will encrpyt it with the new master password using
     * AES algorithm.
     * @throws AESCryptException on fatal crypt errors
     */
    public static void reencodeCredentials() throws AESCryptException, IOException {
        String oldPW = null;
        String oldMasterPasswordFile = String.format("%s.old", Config.getDefaultMasterPasswordFile());
        if (new File(oldMasterPasswordFile).exists()) {
            oldPW = FileUtils.readFirstLineFromFile(oldMasterPasswordFile);
        }

        List<Credentials> creds = getSession().createQuery("FROM Credentials", Credentials.class).list();
        for (Credentials c : creds) {
            String encPw = c.getEncodedPassword();
            if (StringUtils.isBlank(encPw)) {
                // we have credentials where password is not set
                continue;
            }
            if (encPw.startsWith(SALTED_MAGIC_B64)) {
                // AES encrypted - we need to decrypt with the old password
                c.setPassword(CryptHelper.aes256Decrypt(encPw, oldPW));
            }
            else {
                // Base64 encoded - just encrypt it with AES
                c.setPassword(c.getPassword());
            }
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
