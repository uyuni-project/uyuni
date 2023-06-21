package com.suse.oval.db;

import com.suse.oval.ovaltypes.OperationEnumeration;

import javax.persistence.*;

@Entity
@Table(name = "suseOVALPackageVersionState")
public class OVALPackageVersionStateEntity {
    private Integer id;
    private String value;
    private OperationEnumeration operation;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_oval_pkg_version_state_id_seq")
    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "value")
    public String getValue() {
        return value;
    }

    public void setValue(String version) {
        this.value = version;
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
