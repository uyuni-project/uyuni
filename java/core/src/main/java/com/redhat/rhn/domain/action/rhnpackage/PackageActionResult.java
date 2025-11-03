/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.rhnpackage;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * PackageActionResult
 */
@Entity
@Table(name = "rhnServerActionPackageResult")
public class PackageActionResult extends BaseDomainHelper {

    @Embeddable
    public static class PackageActionResultId implements Serializable {

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "server_id", nullable = false)
        private Server server = new Server();

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "results", nullable = false)
        private PackageActionDetails details;

        // Constructors, getters, setters, equals, and hashCode

        /**
         * Default Constructor.
         */
        public PackageActionResultId() {
        }

        /**
         * Constructor.
         * @param serverIn the server
         * @param  detailsIn the details
         */
        public PackageActionResultId(Server serverIn, PackageActionDetails detailsIn) {
            this.server = serverIn;
            this.details = detailsIn;
        }

        /**
         * @return Return the server.
         */
        public Server getServer() {
            return server;
        }

        /**
         * @param serverIn The server to set.
         */
        public void setServer(Server serverIn) {
            this.server = serverIn;
        }

        /**
         * @return Return the detatils.
         */
        public PackageActionDetails getDetails() {
            return details;
        }

        /**
         * @param detailsIn The details to set.
         */
        public void setDetails(PackageActionDetails detailsIn) {
            this.details = detailsIn;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PackageActionResultId that = (PackageActionResultId) o;
            return server.equals(that.server) && details.equals(that.details);
        }

        @Override
        public int hashCode() {
            return Objects.hash(server, details);
        }
    }

    @EmbeddedId
    private PackageActionResultId id = new PackageActionResultId();

    @Column(name = "result_code")
    private Long resultCode;


    public PackageActionResultId getId() {
        return id;
    }

    public void setId(PackageActionResultId idIn) {
        id = idIn;
    }

    /**
     * @return Returns the packageActionDetails.
     */
    public PackageActionDetails getDetails() {
        return this.getId().getDetails();
    }

    /**
     * @param p The packageActionDetails to set.
     */
    public void setDetails(PackageActionDetails p) {
        this.getId().setDetails(p);
    }

    /**
     * @return Returns the resultCode.
     */
    public Long getResultCode() {
        return resultCode;
    }

    /**
     * @param r The resultCode to set.
     */
    public void setResultCode(Long r) {
        this.resultCode = r;
    }

    /**
     * @return Returns the server.
     */
    public Server getServer() {
        return id.getServer();
    }

    /**
     * @param s The server to set.
     */
    public void setServer(Server s) {
        this.id.setServer(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PackageActionResult p)) {
            return false;
        }
        return new EqualsBuilder().append(this.getDetails(), p.getDetails())
                                  .append(this.getServer(), p.getServer())
                                  .append(this.getResultCode(), p.getResultCode())
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getDetails())
                                    .append(this.getServer())
                                    .append(this.getResultCode())
                                    .toHashCode();
    }
}
