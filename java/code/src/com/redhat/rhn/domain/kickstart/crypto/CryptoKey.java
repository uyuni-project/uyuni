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

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.Identifiable;
import com.redhat.rhn.domain.org.Org;


import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


/**
 * CryptoKey - Class representation of the table rhnCryptoKey.
 */
@Entity
@Table(name = "rhnCryptoKey")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class CryptoKey extends  BaseDomainHelper implements Identifiable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cryptoKeySeq")
    @SequenceGenerator(name = "cryptoKeySeq", sequenceName = "RHN_CRYPTOKEY_ID_SEQ", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "description", nullable = false, length = 1024)
    private String description;

    @Column(name = "key")
    private byte[] key;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Org org;

    @ManyToOne
    @JoinColumn(name = "crypto_key_type_id",  referencedColumnName = "id")
    private CryptoKeyType cryptoKeyType;

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
    public void setId(Long idIn) {
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
