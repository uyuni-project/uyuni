package com.suse.oval.db;


import com.suse.oval.ovaltypes.EVRDataTypeEnum;
import com.suse.oval.ovaltypes.OperationEnumeration;

import javax.persistence.*;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_oval_pkg_evr_state_id_seq")
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
}
