/*
 * Copyright (c) 2013--2017 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.kickstart.crypto.SslCryptoKey;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 * SslContentSource
 */
@Entity
@Table(name = "rhnContentSourceSsl")
public class SslContentSource extends BaseDomainHelper {

    @Id
    @Column(name = "content_source_id")
    private Long id;

    @ManyToOne
    @MapsId
    @JoinColumn(name = "content_source_id")
    private ContentSource contentSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ssl_ca_cert_id", nullable = false)
    private  SslCryptoKey caCert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ssl_client_cert_id")
    private  SslCryptoKey clientCert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ssl_client_key_id")
    private  SslCryptoKey clientKey;

    /**
     * Constructor
     */
    public SslContentSource() {
    }

    /**
     * Copy Constructor
     * @param ssl ssl content source template
     */
    public SslContentSource(SslContentSource ssl) {
        id = ssl.getId();
        contentSource = ssl.getContentSource();
        caCert = ssl.getCaCert();
        clientCert = ssl.getClientCert();
        clientKey = ssl.getClientKey();
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn The id to set.
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return Returns the ContentSource.
     */
    public ContentSource getContentSource() {
        return contentSource;
    }

    /**
     * @param contentSourceIn The ContentSource to set.
     */
    public void setContentSource(ContentSource contentSourceIn) {
        contentSource = contentSourceIn;
    }

    /**
     * @return Returns the caCert.
     */
    public SslCryptoKey getCaCert() {
        return caCert;
    }

    /**
     * @param caCertIn The caCert to set.
     */
    public void setCaCert(SslCryptoKey caCertIn) {
        caCert = caCertIn;
    }

    /**
     * @return Returns the clientCert.
     */
    public SslCryptoKey getClientCert() {
        return clientCert;
    }

    /**
     * @param clientCertIn The clientCert to set.
     */
    public void setClientCert(SslCryptoKey clientCertIn) {
        clientCert = clientCertIn;
    }

    /**
     * @return Returns the clientKey.
     */
    public SslCryptoKey getClientKey() {
        return clientKey;
    }

    /**
     * @param clientKeyIn The clientKey to set.
     */
    public void setClientKey(SslCryptoKey clientKeyIn) {
        clientKey = clientKeyIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SslContentSource r)) {
            return false;
        }
        return new EqualsBuilder().append(r.getCaCert(), getCaCert())
                .append(r.getClientCert(), getClientCert())
                .append(r.getClientKey(), getClientKey())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getCaCert())
                .append(getClientCert())
                .append(getClientKey())
                .toHashCode();
    }
}
