/*
 * Copyright (c) 2014--2015 SUSE LLC
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
package com.redhat.rhn.manager.setup;

import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.frontend.dto.BaseDto;

/**
 * Representation of a pair of credentials for SUSE Customer Center.
 */
public class MirrorCredentialsDto extends BaseDto {

    private Long id;
    private String user;
    private String password;
    private boolean primary = false;

    /**
     * Default constructor.
     */
    public MirrorCredentialsDto() {
    }

    /**
     * Instantiates a new mirror credentials DTO.
     *
     * @param userIn the user
     * @param passwordIn the password
     */
    public MirrorCredentialsDto(String userIn, String passwordIn) {
        this.setUser(userIn);
        this.setPassword(passwordIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Use the ranking from rhn.conf.
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param userIn the user to set
     */
    public void setUser(String userIn) {
        this.user = userIn;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param passwordIn the password to set
     */
    public void setPassword(String passwordIn) {
        this.password = passwordIn;
    }

    /**
     * @return the primary
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * @param primaryIn the primary to set
     */
    public void setPrimary(boolean primaryIn) {
        this.primary = primaryIn;
    }

    /**
     * Creates an instance based on the specified SCC credentials
     * @param c the scc credentials
     * @return an instance of the dto using the data from the credentials
     */
    public static MirrorCredentialsDto fromSCCCredentials(SCCCredentials c) {
        MirrorCredentialsDto creds = new MirrorCredentialsDto(c.getUsername(), c.getPassword());
        creds.setId(c.getId());
        creds.setPrimary(c.isPrimary());
        return creds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MirrorCredentialsDto other = (MirrorCredentialsDto) obj;
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        }
        else if (!password.equals(other.password)) {
            return false;
        }
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        }
        else if (!user.equals(other.user)) {
            return false;
        }
        return true;
    }
}
