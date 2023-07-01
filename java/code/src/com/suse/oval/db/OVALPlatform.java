package com.suse.oval.db;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
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
    private String name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "OVALPlatform{" +
                "id=" + id +
                ", name='" + name + '\'' +
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
