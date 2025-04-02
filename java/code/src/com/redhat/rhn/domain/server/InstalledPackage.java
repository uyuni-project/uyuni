/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.server;

import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * InstalledPackage
 * This class is a representation of the rhnserverpackage table
 *    it does not map directly to the rhnpackage table, because it can
 *    contain entries that do not correspond to an entry in the rhnpackage table.
 *    This is because it a system may have a package installed that the
 *    satellite does not have.
 *    This object is an instance of a package that is installed on a server
 */
@Entity
@Table(name = "rhnServerPackage")
public class InstalledPackage implements Serializable, Comparable<InstalledPackage> {
    @Embeddable
    public static class InstalledPackageKey implements Serializable {

        @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @JoinColumn(name = "server_id")
        private Server server = new Server();

        @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @JoinColumn(name = "evr_id")
        private PackageEvr evr = new PackageEvr();

        @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @JoinColumn(name = "name_id")
        private PackageName name = new PackageName();

        @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @JoinColumn(name = "package_arch_id")
        private PackageArch arch = new PackageArch();

        /**
         *  Default Constructor
         */
        public InstalledPackageKey() {

        }

        public Server getServer() {
            return server;
        }

        public void setServer(Server serverIn) {
            this.server = serverIn;
        }

        public PackageEvr getEvr() {
            return evr;
        }

        public void setEvr(PackageEvr evrIn) {
            this.evr = evrIn;
        }

        public PackageName getName() {
            return name;
        }

        public void setName(PackageName nameIn) {
            this.name = nameIn;
        }

        public PackageArch getArch() {
            return arch;
        }

        public void setArch(PackageArch archIn) {
            this.arch = archIn;
        }

        @Override
        public boolean equals(Object oIn) {
            if (!(oIn instanceof InstalledPackageKey that)) {
                return false;
            }
            return Objects.equals(server, that.server) &&
                    Objects.equals(evr, that.evr) && Objects.equals(name, that.name) && Objects.equals(arch, that.arch);
        }

        @Override
        public int hashCode() {
            return Objects.hash(server, evr, name, arch);
        }
    }
    /**
     *
     */
    private static final long serialVersionUID = -6158622200264142583L;

    @EmbeddedId
    private InstalledPackageKey id = new InstalledPackageKey();
    @Column(name = "installtime")
    private Date installTime;

    public InstalledPackageKey getId() {
        return id;
    }

    public void setId(InstalledPackageKey idIn) {
        id = idIn;
    }

    /**
     * @return Returns the server.
     */
    public Server getServer() {
        return id.getServer();
    }


    /**
     * @param serverIn The server to set.
     */
    public void setServer(Server serverIn) {
        this.id.setServer(serverIn);
    }

    /**
     * @return Returns the arch.
     */
    public PackageArch getArch() {
        return id.getArch();
    }

    /**
     * @param archIn The arch to set.
     */
    public void setArch(PackageArch archIn) {
        this.id.setArch(archIn);
    }

    /**
     * @return Returns the evr.
     */
    public PackageEvr getEvr() {
        return id.getEvr();
    }

    /**
     * @param evrIn The evr to set.
     */
    public void setEvr(PackageEvr evrIn) {
        this.id.setEvr(evrIn);
    }

    /**
     * @return Returns the name.
     */
    public PackageName getName() {
        return id.getName();
    }

    /**
     * @param nameIn The name to set.
     */
    public void setName(PackageName nameIn) {
        this.id.setName(nameIn);
    }

    /**
     * Getter for installTime
     * @return Date when package was installed (as reported by rpm database).
    */
    public Date getInstallTime() {
        return this.installTime;
    }

    /**
     * Setter for installTime
     * @param installTimeIn to set
    */
    public void setInstallTime(Date installTimeIn) {
        this.installTime = installTimeIn;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        HashCodeBuilder builder =  new HashCodeBuilder().append(this.getName().getName())
                                    .append(this.getEvr().getEpoch())
                                    .append(this.getEvr().getRelease())
                                    .append(this.getEvr().getVersion())
                                    .append(this.getServer().getId());
        if (this.getArch() != null) {
            builder.append(this.getArch().getName());
        }
        return builder.toHashCode();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {

        if (other instanceof InstalledPackage otherPack) {
            return new EqualsBuilder().append(this.getName(), otherPack.getName())
                .append(this.getEvr(), otherPack.getEvr())
                .append(this.getServer(), otherPack.getServer())
                .append(this.getArch(), otherPack.getArch()).isEquals();


        }
        else if (other instanceof Package otherPack) {
            EqualsBuilder builder =  new EqualsBuilder()
                .append(this.getName(), otherPack.getPackageName())
                .append(this.getEvr(), otherPack.getPackageEvr());

            if (this.getArch() != null) {
                builder.append(this.getArch(), otherPack.getPackageArch());
            }
            return builder.isEquals();
        }
        else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(InstalledPackage ip) {
        if (equals(ip)) {
            return 0;
        }
        if (!getName().equals(ip.getName())) {
            return getName().compareTo(ip.getName());
        }
        if (!getEvr().equals(ip.getEvr())) {
            return getEvr().compareTo(ip.getEvr());
        }
        if (getArch() != null) {
            return getArch().compareTo(ip.getArch());
        }

        if (ip.getArch() != null) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("name", Optional.of(this.getName()).map(PackageName::getName).orElse(null))
            .append("evr", this.getArch())
            .append("archLabel", Optional.of(this.getArch()).map(PackageArch::getLabel).orElse(null))
            .append("serverId", Optional.of(this.getServer()).map(Server::getId).orElse(null))
            .toString();
    }
}
