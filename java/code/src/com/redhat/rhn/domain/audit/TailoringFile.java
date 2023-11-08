package com.redhat.rhn.domain.audit;


import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.Org;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "suseScapTailoringFile")
public class TailoringFile extends BaseDomainHelper {

    private Long id;

    private String name;

    private String fileName;

    private Org org;

    /**
     * TailoringFile Default constructor
     */
    public TailoringFile() {

    }
    /**
     * TailoringFile constructor
     * @param name
     * @param fileName
     */
    public TailoringFile(String name, String fileName) {
        this.name = name;
        this.fileName = fileName;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suseScapTailoringFil_seq")
    @SequenceGenerator(name = "suseScapTailoringFil_seq", sequenceName = "suseScapTailoringFil_id_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param nameIn
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    @Column(name = "file_name")
    public String getFileName() {
        return fileName;
    }

    /**
     *  Sets the file Name.
     * @param fileNameIn
     */
    public void setFileName(String fileNameIn) {
        this.fileName = fileNameIn;
    }

    /**
     * @return the org
     */
    @ManyToOne
    public Org getOrg() {
        return org;
    }
    /**
     * @param orgIn the org to set
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        TailoringFile castOther = (TailoringFile) other;
        return new EqualsBuilder()
                .append(name, castOther.name)
                .append(org, castOther.org)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(org)
                .toHashCode();
    }
    @Override
    public String toString() {
        return super.toString();
    }
}