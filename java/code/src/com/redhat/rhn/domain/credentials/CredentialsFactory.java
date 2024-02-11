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

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/**
 * CredentialsFactory
 */
public class CredentialsFactory extends HibernateFactory {

    private static CredentialsFactory singleton = new CredentialsFactory();
    private static Logger log = LogManager.getLogger(CredentialsFactory.class);

    private CredentialsFactory() {
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
     * Helper method for looking up SCC credentials.
     * @return credentials or null
     */
    public static List<SCCCredentials> listSCCCredentials() {
        return listCredentialsByType(SCCCredentials.class);
    }

    /**
     * Find credentials by ID.
     * @param id the id
     * @return credentials object or null
     */
    public static Credentials lookupCredentialsById(long id) {
        return getSession().get(BaseCredentials.class, id);
    }

    /**
     * Retrieves the instance of {@link SCCCredentials} having the specified id
     * @param id the id of the credentials
     * @return an optional containing the SCCCredentials with the given id, or empty if not found
     */
    public static Optional<SCCCredentials> lookupSCCCredentialsById(long id) {
        SCCCredentials creds = getSession().get(SCCCredentials.class, id);
        return Optional.ofNullable(creds);
    }

    /**
     * Helper method for creating new SCC {@link Credentials}
     * @param username the username
     * @param password the password that will be BASE64 encoded
     * @return new credential with type SCC
     */
    public static SCCCredentials createSCCCredentials(String username, String password) {
        return new SCCCredentials(username, password);
    }

    /**
     * Helper method for creating new Virtual Host Manager {@link Credentials}
     * @param username the username
     * @param password the password that will be BASE64 encoded
     * @return new credential with type Virtual Host Manager
     */
    public static VHMCredentials createVHMCredentials(String username, String password) {
        return new VHMCredentials(username, password);
    }

    /**
     * Helper method for creating new Registry {@link Credentials}
     * @param username the username
     * @param password the password that will be BASE64 encoded
     * @return new credential with type Registry
     */
    public static RegistryCredentials createRegistryCredentials(String username, String password) {
        return new RegistryCredentials(username, password);
    }

    /**
     * Helper method for creating new Cloud Rmt {@link Credentials}
     * @param username the username
     * @param password the password that will be BASE64 encoded
     * @param url the server url
     * @return new credential with type Cloud Rmt
     */
    public static CloudRMTCredentials createCloudRmtCredentials(String username, String password, String url) {
        return new CloudRMTCredentials(username, password, url);
    }

    /**
     * Helper method for creating new RHUI {@link Credentials}
     * @return new credential with type RHUI
     */
    public static RHUICredentials createRhuiCredentials() {
        return new RHUICredentials();
    }

    /**
     * Helper method for creating new Report DB {@link ReportDBCredentials}
     * @param username the username
     * @param password the password that will be BASE64 encoded
     * @return new credential with type Report DB
     */
    public static ReportDBCredentials createReportCredentials(String username, String password) {
        ReportDBCredentials reportDBCredentials = new ReportDBCredentials(username, password);
        reportDBCredentials.setUsername(username);
        reportDBCredentials.setPassword(password);
        return reportDBCredentials;
    }

    /**
     * List all the existing credentials
     * @return the list of all the existing credentials
     */
    public static List<Credentials> listCredentials() {
        CriteriaBuilder criteriaBuilder = getSession().getCriteriaBuilder();
        CriteriaQuery<BaseCredentials> query = criteriaBuilder.createQuery(BaseCredentials.class);
        query.from(BaseCredentials.class);
        return getSession()
            .createQuery(query)
            .stream()
            .map(Credentials.class::cast)
            .collect(Collectors.toList());
    }

    /**
     * @param type the credential type label
     * @return return a list of credentials of the given type
     * @param <T> the type of credentials
     */
    public static <T extends Credentials> List<T> listCredentialsByType(Class<T> type) {
        CriteriaBuilder criteriaBuilder = getSession().getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(type);
        query.from(type);
        return getSession()
                .createQuery(query)
                .list();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
