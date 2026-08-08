/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.testing.building;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.Address;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.RandomStringUtils;

public class UserBuilder {

    private String userName = null;
    private String orgName = null;
    private Org organization = null;
    private Address address = null;
    private boolean orgAdmin = false;
    private boolean satAdmin = false;

    /**
     * Set the userName for the User
     * @param userNameIn the userName
     * @return this builder
     */
    public UserBuilder withUserName(String userNameIn) {
        this.userName = userNameIn;
        return this;
    }

    /**
     * Set the organization for the User.
     * @param orgIn the org
     * @return this builder
     */
    public UserBuilder withOrganization(Org orgIn) {
        this.organization = orgIn;
        return this;
    }

    /**
     * Set the name of the organization for the User.
     * @param orgNameIn the org name
     * @return this builder
     */
    public UserBuilder withOrganizationName(String orgNameIn) {
        this.orgName = orgNameIn;
        return this;
    }

    public UserBuilder withAddress(Address addressIn) {
        this.address = addressIn;
        return this;
    }

    /**
     * Set the user as a regular user
     * @return this builder
     */
    public UserBuilder asRegular() {
        this.orgAdmin = false;
        this.satAdmin = false;
        return this;
    }

    /**
     * Set the user as an organization administrator
     * @return this builder
     */
    public UserBuilder asOrgAdmin() {
        this.orgAdmin = true;
        this.satAdmin = false;
        return this;
    }

    /**
     * Set the user as a satellite administrator
     * @return this builder
     */
    public UserBuilder asSatAdmin() {
        this.orgAdmin = false;
        this.satAdmin = true;
        return this;
    }

    /**
     * Builds and persists the User
     *
     * @return the created User
     */
    public User build() {
        if (organization == null && orgName == null) {
            throw new IllegalStateException("Provide either the name or the id of the organization");
        }

        if (organization != null && orgName != null) {
            throw new IllegalStateException("The name and the id of the organization cannot be provided together");
        }

        if (organization == null) {
            organization = new OrgBuilder().withName(orgName).build();
        }

        User user = new UserImpl();

        user.setLogin(userName + RandomStringUtils.insecure().nextAlphanumeric(13));
        user.setPassword("password");
        user.setFirstNames("userName" + RandomStringUtils.insecure().nextAlphanumeric(13));
        user.setLastName("userName" + RandomStringUtils.insecure().nextAlphanumeric(13));
        user.setPrefix(LocalizationService.getInstance().availablePrefixes().iterator().next());
        user.setEmail("javaTest@example.com");
        user.setOrg(organization);
        user.setAddress1(orgName);

        // We need to save here because addPermanentRole() calls UserGroupFactory.save() and needs the user
        user = UserFactory.saveNewUser(user, address, organization.getId());

        if (orgAdmin) {
            user.addPermanentRole(RoleFactory.ORG_ADMIN);
        }
        else if (satAdmin) {
            user.addPermanentRole(RoleFactory.SAT_ADMIN);
        }

        return user;
    }
}
