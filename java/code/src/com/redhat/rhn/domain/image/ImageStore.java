/*
 * Copyright (c) 2017--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.image;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.credentials.RegistryCredentials;
import com.redhat.rhn.domain.org.Org;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.beans.Transient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * ImageStore
 */
@Entity
@Table(name = "suseImageStore")
public class ImageStore extends BaseDomainHelper {

    /** The id. */
    private Long id;

    private String label;

    private String uri;

    private ImageStoreType storeType;

    private Org org;

    private RegistryCredentials creds;

    /**
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imgstore_seq")
    @SequenceGenerator(name = "imgstore_seq", sequenceName = "suse_imgstore_id_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * @return the label
     */
    @Column(name = "label")
    public String getLabel() {
        return label;
    }

    /**
     * @return the uri
     */
    @Column(name = "uri")
    public String getUri() {
        return uri;
    }

    /**
     * @return the storeType
     */
    @ManyToOne
    @JoinColumn(name = "store_type_id")
    public ImageStoreType getStoreType() {
        return storeType;
    }

    /**
     * @return the org
     */
    @ManyToOne
    public Org getOrg() {
        return org;
    }

    /**
     * @return the creds
     */
    @ManyToOne(cascade = CascadeType.ALL)
    public RegistryCredentials getCreds() {
        return creds;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @param labelIn the label to set
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * @param uriIn the uri to set
     */
    public void setUri(String uriIn) {
        this.uri = uriIn;
    }

    /**
     * @param storeTypeIn the storeType to set
     */
    public void setStoreType(ImageStoreType storeTypeIn) {
        this.storeType = storeTypeIn;
    }

    /**
     * @param orgIn the org to set
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * @param credsIn the creds to set
     */
    public void setCreds(RegistryCredentials credsIn) {
        this.creds = credsIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ImageStore castOther)) {
            return false;
        }
        return new EqualsBuilder().append(label, castOther.label)
                                  .append(uri, castOther.uri)
                                  .append(org, castOther.org)
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(label)
                                    .append(uri)
                                    .append(org)
                                    .toHashCode();
    }

    /**
     * @param stores list of stores
     * @return map
     */
    @Transient
    public static Map<String, Object> dockerRegPillar(List<ImageStore> stores) {
        Map<String, Object> dockerRegistries = new HashMap<>();
        stores.forEach(store -> Optional.ofNullable(store.getCreds())
                .ifPresent(credentials -> {
                    Map<String, Object> reg = new HashMap<>();
                    reg.put("email", "tux@example.com");
                    reg.put("password", credentials.getPassword());
                    reg.put("username", credentials.getUsername());
                    dockerRegistries.put(store.getUri(), reg);
                }));
        return dockerRegistries;
    }

}
