/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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
/*
 * Copyright (c) 2010-2019 SUSE LLC
 */
package com.redhat.rhn.manager.channel.repo;

import com.redhat.rhn.common.client.InvalidCertificateException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.ContentSourceType;
import com.redhat.rhn.domain.channel.SslContentSource;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.crypto.SslCryptoKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.frontend.xmlrpc.channel.repo.InvalidRepoLabelException;
import com.redhat.rhn.frontend.xmlrpc.channel.repo.InvalidRepoTypeException;
import com.redhat.rhn.frontend.xmlrpc.channel.repo.InvalidRepoUrlException;
import com.redhat.rhn.frontend.xmlrpc.channel.repo.InvalidRepoUrlInputException;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * BaseRepoCommand - Command to create or edit a repo
 */
public abstract class BaseRepoCommand {

    public static final String REPOSITORY_LABEL_REGEX =
        "^[a-zA-Z\\d][\\w\\d\\s\\-\\.\\'\\(\\)\\/\\_]*$";


    protected ContentSource repo;

    private String label;
    private String url;
    private String type;
    private Set<SslContentSource> sslSetsToAdd = new HashSet<>();
    private Set<SslContentSource> sslSetsToDelete = new HashSet<>();
    private Org org;
    private boolean metadata_signed;

    /**
     *
     * @return Org of repo
     */
    public Org getOrg() {
        return org;
    }

    /**
     *
     * @param orgIn to set for repo
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     *
     * @return label for repo
     */
    public String getLabel() {
        return label;
    }

    /**
     *
     * @param labelIn to set for repo
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     *
     * @return url for repo
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param urlIn to set for repo
     */
    public void setUrl(String urlIn) {
        this.url = urlIn;
    }

    /**
     *
     * @return type of repo
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param typeIn to set type of repo
     */
    public void setType(String typeIn) {
        this.type = typeIn;
    }


    private SslContentSource createSslSet(Long sslCaCertId, Long sslClientCertId,
                          Long sslClientKeyId) throws InvalidCertificateException {
        SslCryptoKey caCert = lookupSslCryptoKey(sslCaCertId, org);
        SslCryptoKey clientCert = lookupSslCryptoKey(sslClientCertId, org);
        SslCryptoKey clientKey = lookupSslCryptoKey(sslClientKeyId, org);
        if (caCert == null) {
            return null;
        }
        else if (clientCert == null && clientKey != null) {
            throw new InvalidCertificateException(
                    "client key is provided but client certificate is missing");
        }
        SslContentSource sslSet = ChannelFactory.createRepoSslSet();
        sslSet.setCaCert(caCert);
        sslSet.setClientCert(clientCert);
        sslSet.setClientKey(clientKey);
        sslSet.setCreated(new Date());
        sslSet.setModified(new Date());
        return sslSet;
    }

    /**
     * Marks some SSL set for assigning to repository
     * @param sslCaCertId ca cert id
     * @param sslClientCertId client cert id
     * @param sslClientKeyId client key
     * @throws InvalidCertificateException in case ca cert is missing or client key is set,
     * but client certificate is missing
     */
    public void addSslSet(Long sslCaCertId, Long sslClientCertId, Long sslClientKeyId)
            throws InvalidCertificateException {
        SslContentSource sslSet = createSslSet(sslCaCertId, sslClientCertId,
                sslClientKeyId);
        if (sslSet != null) {
            sslSetsToAdd.add(sslSet);
            sslSetsToDelete.remove(sslSet);
        }
    }

    /**
     * Marks all assigned SSL sets for deletion
     */
    public void deleteAllSslSets() {
        if (repo != null) {
            Set<SslContentSource> repoSslSets = repo.getSslSets();
            sslSetsToDelete.addAll(repoSslSets);
            sslSetsToAdd.removeAll(repoSslSets);
        }
    }

    /**
     *
     * @return true if metadata should be signed
     */
    public boolean getMetadataSigned() {
        return metadata_signed;
    }

    /**
     *
     * @param md set if metadata are signed
     */
    public void setMetadataSigned(boolean md) {
        this.metadata_signed = md;
    }

    /**
     * Check for errors and store Org to db.
     * @throws InvalidRepoUrlException in case repo wih given url already exists
     * in the org
     * @throws InvalidRepoLabelException in case repo witch given label already exists
     * in the org
     * @throws InvalidRepoTypeException in case repo wih given type already exists
     * in the org
     * @throws InvalidRepoUrlInputException in case the user entered an invalid repo url
     */
    public void store() throws InvalidRepoUrlException, InvalidRepoLabelException,
            InvalidRepoTypeException, InvalidRepoUrlInputException {

        // create new repository
        if (repo == null) {
            this.repo = new ContentSource();
        }

        Set<SslContentSource> repoSslSets = repo.getSslSets();
        for (SslContentSource sslSet : sslSetsToAdd) {
            repoSslSets.add(sslSet);
        }
        for (SslContentSource sslSet : sslSetsToDelete) {
            repoSslSets.remove(sslSet);
        }

        repo.setOrg(org);

        if (this.label != null && !this.label.equals(repo.getLabel())) {
            if (ChannelFactory.lookupContentSourceByOrgAndLabel(org, label) != null) {
                throw new InvalidRepoLabelException(label);
            }
            if (!Pattern.compile(REPOSITORY_LABEL_REGEX).matcher(this.label).find()) {
                throw new InvalidRepoLabelException(label,
                    InvalidRepoLabelException.Reason.REGEX_FAILS,
                    "edit.channel.repo.invalidrepolabel", "");
            }
            repo.setLabel(this.label);
        }

        if (this.url != null && this.type != null) {
            try {
                final URL u;
                if (this.type.equals("uln")) {
                    // URL for ULN repositories, i.a. uln:///uln_channel_label, are not
                    // passing Java URL validation due unknown protocol. We fake the
                    // protocol to "file" and run the validation for the rest of the URL.
                    final URI uri = new URI(this.url);
                    if (uri.getScheme().equals("uln")) {
                       u = new URI("file", uri.getSchemeSpecificPart(), uri.getFragment()).toURL();
                    }
                    else {
                        throw new InvalidRepoUrlInputException(url);
                    }
                }
                else {
                    u = new URL(this.url);
                }
            }
            catch (Exception e) {
                throw new InvalidRepoUrlInputException(url);
            }
            ContentSourceType cst = ChannelFactory.lookupContentSourceType(this.type);
            boolean alreadyExists = !ChannelFactory.lookupContentSourceByOrgAndRepo(
                    org, cst, url).isEmpty();
            if (!this.url.equals(repo.getSourceUrl())) {
                if (alreadyExists) {
                    throw new InvalidRepoUrlException(url);
                }
                repo.setSourceUrl(this.url);
            }
            if (!cst.equals(repo.getType())) {
                if (alreadyExists) {
                    throw new InvalidRepoTypeException(this.type);
                }
                repo.setType(cst);
            }
        }
        repo.setMetadataSigned(this.metadata_signed);

        ChannelFactory.save(repo);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
    }

    /**
     * Get the repo
     * @return repo
     */
    public ContentSource getRepo() {
        return this.repo;
    }

    private SslCryptoKey lookupSslCryptoKey(Long keyId, Org orgIn) {
        if (keyId == null) {
            return null;
        }
        return KickstartFactory.lookupSslCryptoKeyById(keyId, orgIn);
    }
}
