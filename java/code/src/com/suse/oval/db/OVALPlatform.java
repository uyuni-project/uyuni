package com.suse.oval.db;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "suseOVALPlatform")
@NamedQueries({
        @NamedQuery(name = "OVALPlatform.getPlatformsAffectedByCve",
                query = "SELECT platform " +
                        "FROM OVALPlatform platform " +
                        "JOIN platform.definitions def " +
                        "JOIN def.cve cve " +
                        "WHERE cve.name = :cve"
        ),
        @NamedQuery(name = "OVALPlatform.getPlatformsAffectedByDefinition",
                query = "SELECT platform " +
                        "FROM OVALPlatform platform " +
                        "JOIN platform.definitions def " +
                        "WHERE def.id = :defId"
        )
})
public class OVALPlatform {
    private Long id;
    private String cpe;
    private Set<OVALDefinition> definitions;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "suseOVALDefinitionAffectedPlatform",
            joinColumns = {@JoinColumn(name = "platform_id")},
            inverseJoinColumns = {@JoinColumn(name = "definition_id")})
    public Set<OVALDefinition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(Set<OVALDefinition> definitions) {
        this.definitions = definitions;
    }

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
