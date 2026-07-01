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

package com.suse.manager.admin;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.cloudpayg.CloudRmtHostFactory;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.cloudpayg.PaygSshDataFactory;
import com.redhat.rhn.domain.credentials.CloudCredentials;
import com.redhat.rhn.domain.credentials.CloudRMTCredentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.crypto.CryptoKey;
import com.redhat.rhn.domain.kickstart.crypto.SslCryptoKey;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import com.suse.manager.admin.validator.PaygAdminValidator;
import com.suse.manager.webui.controllers.admin.beans.PaygProperties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaygAdminManager {

    private static final Logger LOG = LogManager.getLogger(PaygAdminManager.class);
    private TaskomaticApi taskomaticApi;

    /**
     * Constructor with the taskomatic instance
     * @param taskomaticApiIn taskomatic api connection instance
     */
    public PaygAdminManager(TaskomaticApi taskomaticApiIn) {
        this.taskomaticApi = taskomaticApiIn;
    }

    /**
     * Create a payg ssh connection data instance in the database
     * @param paygProperties payg properties to create the instance
     * @return a new payg ssh connection data instance
     */
    public PaygSshData create(PaygProperties paygProperties) {
        ValidatorResult result = new ValidatorResult();
        Integer port = null;
        Integer bastionPort = null;

        if (!StringUtils.isEmpty(paygProperties.getPort())) {
            try {
                port = Integer.parseInt(paygProperties.getPort());
            }
            catch (NumberFormatException e) {
                result.addFieldError(PaygAdminFields.port.name(), "payg.port_invalid");
            }
        }
        if (!StringUtils.isEmpty(paygProperties.getBastionPort())) {
            try {
                bastionPort = Integer.parseInt(paygProperties.getBastionPort());
            }
            catch (NumberFormatException e) {
                result.addFieldError(PaygAdminFields.bastion_port.name(), "payg.bastion_port_invalid");
            }
        }

        if (result.hasErrors()) {
            throw new ValidatorException(result);
        }

        return create(paygProperties.getDescription(),
                paygProperties.getHost(), port, paygProperties.getUsername(), paygProperties.getPassword(),
                paygProperties.getKey(), paygProperties.getKeyPassword(),
                paygProperties.getBastionHost(), bastionPort,
                paygProperties.getBastionUsername(), paygProperties.getBastionPassword(),
                paygProperties.getBastionKey(), paygProperties.getBastionKeyPassword());
    }
    /**
     * @param description        Description for the ssh connection data
     * @param host               hostname or IP address to the instance
     * @param port               shh port to the instance
     * @param username           ssh username to connection on the instance
     * @param password           ssh password for the instance
     * @param key                private key to use in authentication
     * @param keyPassword        private key password
     * @param bastionHost        hostname or IP address to a bastion host
     * @param bastionPort        shh port to a bastion instance
     * @param bastionUsername    ssh username to connect on bastion
     * @param bastionPassword    ssh password for the bastion
     * @param bastionKey         private key to use in bastion authentication
     * @param bastionKeyPassword private key password for bastion key
     * @return Returns PaygSshData if successful (exception otherwise)
     */
    public PaygSshData create(String description, String host, Integer port, String username,
                      String password, String key, String keyPassword,
                      String bastionHost, Integer bastionPort, String bastionUsername,
                      String bastionPassword, String bastionKey, String bastionKeyPassword) {

        PaygAdminValidator.validatePaygData(description,
                host, port, username, password, key, keyPassword,
                bastionHost, bastionPort, bastionUsername, bastionPassword, bastionKey, bastionKeyPassword);

        Optional<PaygSshData> paygSshDataOpt = PaygSshDataFactory.lookupByHostname(host);
        if (paygSshDataOpt.isPresent()) {
            LOG.debug("duplicated payg host: {}", host);
            throw new EntityExistsException("Duplicated host: " + host);
        }

        PaygSshData paygSshData = new PaygSshData(description,
                host, port, username, password, key, keyPassword,
                bastionHost, bastionPort, bastionUsername, bastionPassword, bastionKey, bastionKeyPassword);

        PaygSshDataFactory.savePaygSshData(paygSshData);
        // we need to commit before call the taskomatic task, otherwise new data will not be available
        HibernateFactory.commitTransaction();
        try {
            taskomaticApi.scheduleSinglePaygUpdate(paygSshData);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            LOG.warn("unable to start task to update authentication data", e);
        }
        LOG.debug("payg ssh data added for hostname: {}", host);

        return paygSshData;
    }

    private void validateSetDetailsFields(Map<String, Object> details) {
        ValidatorResult result = new ValidatorResult();
        details.keySet().forEach(field -> {
            if (Arrays.stream(PaygAdminFields.values())
                    .filter(PaygAdminFields::isEditable)
                    .noneMatch(f -> f.name().equals(field))) {
                result.addError("payg.unknown_edit_field", field);
            }
        });
        if (result.hasErrors()) {
            throw new ValidatorException(result);
        }
    }

    /**
     * Update a payg ssh connection data instance in the database
     * @param id database id for the payg ssh connection data
     * @param paygProperties properties to be upadated
     * @return the updated ssh connection data
     */
    public PaygSshData setDetails(Integer id, PaygProperties paygProperties) {

        PaygSshData paygSshData = PaygSshDataFactory.lookupById(id)
                .orElseThrow(() -> new LookupException("Payg not found for id: " + id));
        ValidatorResult result = new ValidatorResult();
        Integer port = null;
        Integer bastionPort = null;

        String username;
        String password;
        String key;
        String keyPassword;

        if (paygProperties.isInstanceEdit()) {
            if (!StringUtils.isEmpty(paygProperties.getPort())) {
                try {
                    port = Integer.parseInt(paygProperties.getPort());
                }
                catch (NumberFormatException e) {
                    result.addFieldError(PaygAdminFields.port.name(), "payg.port_invalid");
                }
            }
            username = paygProperties.getUsername();
            password = paygProperties.getPassword();
            key = paygProperties.getKey();
            keyPassword = paygProperties.getKeyPassword();
        }
        else {
            port = paygSshData.getPort();
            username = paygSshData.getUsername();
            password = paygSshData.getPassword();
            key = paygSshData.getKey();
            keyPassword = paygSshData.getKeyPassword();
        }

        String bastionHost;
        String bastionUsername;
        String bastionPassword;
        String bastionKey;
        String bastionKeyPassword;

        if (paygProperties.isBastionEdit()) {
            if (!StringUtils.isEmpty(paygProperties.getBastionPort())) {
                try {
                    bastionPort = Integer.parseInt(paygProperties.getBastionPort());
                }
                catch (NumberFormatException e) {
                    result.addFieldError(PaygAdminFields.bastion_port.name(), "payg.bastion_port_invalid");
                }
            }
            bastionHost = paygProperties.getBastionHost();
            bastionUsername = paygProperties.getBastionUsername();
            bastionPassword = paygProperties.getBastionPassword();
            bastionKey = paygProperties.getBastionKey();
            bastionKeyPassword = paygProperties.getBastionKeyPassword();
        }
        else {
            bastionPort = paygSshData.getBastionPort();
            bastionHost = paygSshData.getBastionHost();
            bastionUsername = paygSshData.getBastionUsername();
            bastionPassword = paygSshData.getBastionPassword();
            bastionKey = paygSshData.getBastionKey();
            bastionKeyPassword = paygSshData.getBastionKeyPassword();
        }

        return setDetails(paygSshData,
                paygProperties.getDescription(),
                paygSshData.getHost(),
                port,
                username,
                password,
                key,
                keyPassword,
                bastionHost,
                bastionPort,
                bastionUsername,
                bastionPassword,
                bastionKey,
                bastionKeyPassword);
    }

    /**
     * @param host    payg data object
     * @param details A map containing the new values for the ssh connection
     * @return Returns true if successful (exception otherwise)
     */
    public PaygSshData setDetails(String host,  Map<String, Object> details) {
        if (StringUtils.isEmpty(host)) {
            LOG.debug("payg empty host");
            throw new BadParameterException("PAYG host cannot be empty");
        }
        PaygSshData paygSshData = PaygSshDataFactory.lookupByHostname(host)
                .orElseThrow(() -> new LookupException("Host not found: " + host));
        validateSetDetailsFields(details);

        String description = (String)details.getOrDefault(PaygAdminFields.description.name(),
                paygSshData.getDescription());

        Integer port = (Integer) details.getOrDefault(PaygAdminFields.port.name(),
                paygSshData.getPort());
        String username = (String)details.getOrDefault(PaygAdminFields.username.name(),
                paygSshData.getUsername());
        String password = (String)details.getOrDefault(PaygAdminFields.password.name(),
                paygSshData.getPassword());
        String key = (String)details.getOrDefault(PaygAdminFields.key.name(),
                paygSshData.getKey());
        String keyPassword = (String)details.getOrDefault(PaygAdminFields.key_password.name(),
                paygSshData.getKeyPassword());

        String bastionHost = (String)details.getOrDefault(PaygAdminFields.bastion_host.name(),
                paygSshData.getBastionHost());
        Integer bastionPort = (Integer)details.getOrDefault(PaygAdminFields.bastion_port.name(),
                paygSshData.getBastionPort());
        String bastionUsername = (String)details.getOrDefault(PaygAdminFields.bastion_username.name(),
                paygSshData.getBastionUsername());
        String bastionPassword = (String)details.getOrDefault(PaygAdminFields.bastion_password.name(),
                paygSshData.getBastionPassword());
        String bastionKey = (String)details.getOrDefault(PaygAdminFields.bastion_key.name(),
                paygSshData.getBastionKey());
        String bastionKeyPassword = (String)details.getOrDefault(PaygAdminFields.bastion_key_password.name(),
                paygSshData.getBastionKeyPassword());

        return setDetails(paygSshData,
                description,
                host, port, username, password,
                key, keyPassword,
                bastionHost, bastionPort, bastionUsername, bastionPassword,
                bastionKey, bastionKeyPassword);
    }

    private PaygSshData setDetails(PaygSshData paygSshData,
                                   String description,
                                   String host,
                                   Integer port,
                                   String username,
                                   String password,
                                   String key,
                                   String keyPassword,
                                   String bastionHost,
                                   Integer bastionPort,
                                   String bastionUsername,
                                   String bastionPassword,
                                   String bastionKey,
                                   String bastionKeyPassword) {

        PaygAdminValidator.validatePaygData(description,
                host, port, username, password, key, keyPassword,
                bastionHost, bastionPort, bastionUsername, bastionPassword,
                bastionKey, bastionKeyPassword);

        paygSshData.setDescription(description);
        // in the update we don't set the hostname, it cannot be changed
        paygSshData.setPort(port);
        paygSshData.setUsername(username);
        paygSshData.setPassword(password);
        paygSshData.setKey(key);
        paygSshData.setKeyPassword(keyPassword);

        paygSshData.setBastionHost(bastionHost);
        paygSshData.setBastionPort(bastionPort);
        paygSshData.setBastionUsername(bastionUsername);
        paygSshData.setBastionPassword(bastionPassword);
        paygSshData.setBastionKey(bastionKey);
        paygSshData.setBastionKeyPassword(bastionKeyPassword);

        paygSshData.setStatus(PaygSshData.Status.P);
        paygSshData.setErrorMessage("");
        PaygSshDataFactory.savePaygSshData(paygSshData);
        // we need to commit before call the taskomatic task, otherwise new data will not be available
        HibernateFactory.commitTransaction();

        try {
            taskomaticApi.scheduleSinglePaygUpdate(paygSshData);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            LOG.warn("unable to start task to update authentication data", e);
        }
        return HibernateFactory.reload(paygSshData);
    }

    /**
     * @return Returns a list of PaygSshData
     */
    public List<PaygSshData> list() {
        return PaygSshDataFactory.lookupPaygSshData();
    }

    /**
     * @param host hostname or IP address to the instance
     * @return Returns a list of PaygSshData
     */
    public PaygSshData getDetails(String host) {
        return PaygSshDataFactory.lookupByHostname(host)
                .orElseThrow(() -> new LookupException("Host not found: " + host));
    }

    /**
     * @param id database id for the PAYG ssh data
     * @return Returns a list of PaygSshData
     */
    public PaygSshData getDetails(Integer id) {
        return PaygSshDataFactory.lookupById(id)
                .orElseThrow(() -> new LookupException("Info not found: " + id));
    }

    /**
     * @param id payg ssh data id to be deleted
     * @return Returns true if successful (exception otherwise)
     */
    public boolean delete(Integer id) {
        return delete(getDetails(id));
    }

    /**
     * @param host hostname or IP address of the instance
     * @return Returns true if successful (exception otherwise)
     */
    public boolean delete(String host) {
        return delete(getDetails(host));
    }

    private boolean delete(PaygSshData paygSshData) {
        CloudCredentials creds = paygSshData.getCredentials();
        LOG.debug("deleting payg data {} -> {}", paygSshData.getId(), paygSshData.getHost());
        if (creds instanceof CloudRMTCredentials) {
            List<SCCRepositoryAuth> existingRepos = SCCCachingFactory.lookupRepositoryAuthByCredential(creds);
            if (LOG.isDebugEnabled()) {
                LOG.debug("deleting repo auth ids {}",
                    existingRepos.stream().map(r -> r.getId().toString()).collect(Collectors.joining(", ")));
            }
            existingRepos.forEach(SCCCachingFactory::deleteRepositoryAuth);
        }
        else { // RHUI - some clouds have no credentials
            List<ContentSource> csUrls = PaygSshDataFactory.listRhuiRepositoriesCreatedByInstance(paygSshData);
            Set<SslCryptoKey> sslCryptoKeys = csUrls.stream()
                    .flatMap(cs -> cs.getSslSets().stream())
                    .flatMap(scs -> Stream.of(scs.getCaCert(), scs.getClientCert(), scs.getClientKey()))
                    .collect(Collectors.toSet());
            if (LOG.isDebugEnabled()) {
                LOG.debug("deleting repositories {}",
                    csUrls.stream().map(ContentSource::getLabel).collect(Collectors.joining(", ")));
            }
            csUrls.forEach(ChannelFactory::remove);
            if (LOG.isDebugEnabled()) {
                LOG.debug("deleting crypto keys {}",
                    sslCryptoKeys.stream().map(CryptoKey::getDescription).collect(Collectors.joining(", ")));
            }
            sslCryptoKeys.forEach(KickstartFactory::removeCryptoKey);
        }
        Optional.ofNullable(creds).ifPresent(CredentialsFactory::removeCredentials);
        Optional.ofNullable(paygSshData.getRmtHosts()).ifPresent(CloudRmtHostFactory::deleteCloudRmtHost);
        PaygSshDataFactory.deletePaygSshData(paygSshData);
        return true;
    }
}
