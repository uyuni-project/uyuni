package com.suse.oval.db;

import com.suse.oval.ovaltypes.OperationEnumeration;

import javax.persistence.*;

@Entity
@Table(name = "suseOVALPackageArchState")
/**
 * */
public class OVALPackageArchStateEntity {
    private Integer id;
    // We can't use rhnPackageArch because value could be a pattern, not the exact arch
    private String value;
    private OperationEnumeration operation;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_oval_pkg_arch_state_id_seq")
    public Integer getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String arch) {
        this.value = arch;
    }

    @Enumerated(EnumType.STRING)
    public OperationEnumeration getOperation() {
        return operation;
    }

    public void setOperation(OperationEnumeration operation) {
        this.operation = operation;
    }

    public void setId(int id) {
        this.id = id;
    }
}
