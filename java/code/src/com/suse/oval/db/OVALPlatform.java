package com.suse.oval.db;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "suseOVALPlatform")
public class OVALPlatform {
    private Long id;
    private String cpe;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "platform_id_seq")
    @SequenceGenerator(name = "platform_id_seq", sequenceName = "suse_oval_platform_id_seq",
            allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "cpe")
    public String getCpe() {
        return cpe;
    }

    public void setCpe(String name) {
        this.cpe = name;
    }

    @Override
    public String toString() {
        return "OVALPlatform{" +
                "id=" + id +
                ", cpe='" + cpe + '\'' +
                '}';
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof OVALPlatform)) {
            return false;
        }
        OVALPlatform castOther = (OVALPlatform) other;
        return new EqualsBuilder()
                .append(id, castOther.id)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .toHashCode();
    }
}
