package com.suse.oval.db;

import javax.persistence.*;

@Entity
@Table(name = "suseOVALPlatform")
public class OVALPlatform {
    private Long id;
    private String name;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_oval_platform_id_seq")
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
}
