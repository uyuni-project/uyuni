package com.suse.oval.db;


import com.suse.oval.ovaltypes.EVRDataTypeEnum;
import com.suse.oval.ovaltypes.OperationEnumeration;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "suseOVALPackageEvrState")
/**
 *
 * */
public class OVALPackageEvrStateEntity {
    private Long id;
    private String evr;
    private EVRDataTypeEnum datatype;
    private OperationEnumeration operation;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pkg_evr_state_id_seq")
    @SequenceGenerator(name = "pkg_evr_state_id_seq", sequenceName = "suse_oval_pkg_evr_state_id_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "evr")
    public String getEvr() {
        return evr;
    }

    public void setEvr(String evr) {
        this.evr = evr;
    }

    @Column(name = "datatype")
    @Enumerated(EnumType.STRING)
    public EVRDataTypeEnum getDatatype() {
        return datatype;
    }

    public void setDatatype(EVRDataTypeEnum datatype) {
        this.datatype = datatype;
    }

    @Column(name = "operation")
    @Enumerated(EnumType.STRING)
    public OperationEnumeration getOperation() {
        return operation;
    }

    public void setOperation(OperationEnumeration operation) {
        this.operation = operation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof OVALPackageEvrStateEntity)) {
            return false;
        }
        OVALPackageEvrStateEntity castOther = (OVALPackageEvrStateEntity) other;
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
