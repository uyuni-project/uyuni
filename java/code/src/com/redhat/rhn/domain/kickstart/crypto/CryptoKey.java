/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.domain.kickstart.crypto;

import com.redhat.rhn.domain.Identifiable;
import com.redhat.rhn.domain.org.Org;

import org.hibernate.annotations.DiscriminatorFormula;

import java.nio.charset.StandardCharsets;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * CryptoKey - Class representation of the table rhnCryptoKey.
 */
@Entity
@Table(name = "rhnCryptoKey")
@DiscriminatorFormula("(SELECT type.label FROM rhnCryptoKeyType type WHERE type.id = crypto_key_type_id)")
public class CryptoKey implements Identifiable {

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RHN_CRYPTOKEY_ID_SEQ")
	@SequenceGenerator(name = "RHN_CRYPTOKEY_ID_SEQ", sequenceName = "RHN_CRYPTOKEY_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column
    private byte[] key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crypto_key_type_id")
    private CryptoKeyType cryptoKeyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Org org;


    /**
     * Getter for id
     * @return Long to get
    */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     * @param idIn to set
    */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Getter for description
     * @return String to get
    */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for description
     * @param descriptionIn to set
    */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * @return Returns the cryptoKeyType.
     */
    public CryptoKeyType getCryptoKeyType() {
        return cryptoKeyType;
    }


    /**
     * @param cryptoKeyTypeIn The cryptoKeyType to set.
     */
    public void setCryptoKeyType(CryptoKeyType cryptoKeyTypeIn) {
        this.cryptoKeyType = cryptoKeyTypeIn;
    }

    /**
     *
     * @return true if this is a SSL key
     */
    public boolean isSSL() {
        return false;
    }

    /**
     *
     * @return if this is a GPG key
     */
    public boolean isGPG() {
        return false;
    }

    /**
     * @return Returns the org.
     */
    public Org getOrg() {
        return org;
    }


    /**
     * @param orgIn The org to set.
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }


    /**
     * @return Returns the key.
     */
    public byte[] getKey() {
        return key;
    }


    /**
     * @param keyIn The key to set.
     */
    public void setKey(byte[] keyIn) {
        this.key = keyIn;
    }

    /**
     * Get a string version of this key.  Convenience method.
     *
     * @return String version of the key.
     */
    public String getKeyString() {
        if (this.key != null) {
            return new String(this.key, StandardCharsets.UTF_8);
        }
        return "";
    }

}
