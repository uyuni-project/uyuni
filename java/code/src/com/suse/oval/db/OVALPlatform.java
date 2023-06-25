package com.suse.oval.db;

import javax.persistence.*;

@Entity
@Table(name = "suseOVALPlatform")
public class OVALPlatform {
    private Long id;
    private String name;

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
}
