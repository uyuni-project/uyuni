package com.suse.oval.db;

import com.suse.oval.ovaltypes.OperationEnumeration;

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
@Table(name = "suseOVALPackageArchState")
/**
 * */
public class OVALPackageArchStateEntity {
    private Integer id;
    // We can't use rhnPackageArch because value could be a pattern, not the exact arch
    private String value;
    private OperationEnumeration operation;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pkg_arch_state_id_seq")
    @SequenceGenerator(name = "pkg_arch_state_id_seq", sequenceName = "suse_oval_pkg_arch_state_id_seq", allocationSize = 1)
    public Integer getId() {
        return id;
    }

    @Column(name = "value")
    public String getValue() {
        return value;
    }

    public void setValue(String arch) {
        this.value = arch;
    }

    @Column(name = "operation")
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
