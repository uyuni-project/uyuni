/**
 * Copyright (c) 2017 SUSE LLC
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

package com.redhat.rhn.manager.visualization.json;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * POJO representing system to be displayed in visualization.
 */
public class System {

    private String id = "";
    private String rawId = "";
    private String parentId;
    private String name;
    @SerializedName("contact_method")
    private String contactMethod;
    @SerializedName("base_channel")
    private String baseChannel;
    @SerializedName("base_entitlement")
    private String baseEntitlement;
    private Long checkin;
    private String type = "system";
    private Set<String> installedProducts = new HashSet<>();
    @SerializedName("managed_groups")
    private Set<String> managedGroups = new HashSet<>();
    @SerializedName("patch_counts")
    private List<Integer> patchCounts = new ArrayList<>();

    /**
     * Standard constructor
     */
    public System() {
    }

    /**
     * Standard constructor
     * @param rawIdIn the real system id
     * @param parentIdIn idIn of parent
     * @param nameIn nameIn
     * @param contactMethodIn contact method
     * @param baseChannelIn base channel
     * @param baseEntitlementIn base entitlement
     * @param checkinIn check-in
     */
    public System(Long rawIdIn, Long parentIdIn, String nameIn, String contactMethodIn,
            String baseChannelIn, String baseEntitlementIn, Date checkinIn) {
        if (rawIdIn != null) {
            this.rawId = rawIdIn.toString();
            this.id += this.rawId;
        }
        if (parentIdIn != null) {
            this.parentId = parentIdIn.toString();
            this.id += this.parentId;
        }
        this.name = nameIn;
        this.contactMethod = contactMethodIn;
        this.baseChannel = baseChannelIn;
        this.baseEntitlement = baseEntitlementIn;
        if (checkinIn != null) {
            this.checkin = checkinIn.getTime();
        }
    }
    /**
     * Standard constructor that will not append the parentId to the id value as other constructor do.
     *
     * @param rawIdIn the real system id
     * @param parentIdIn idIn of parent
     * @param nameIn nameIn
     * @param contactMethodIn contact method
     * @param baseChannelIn base channel
     * @param baseEntitlementIn base entitlement
     * @param checkinIn check-in
     * @param typeIn type-in
     */
    public System(Long rawIdIn, String parentIdIn, String nameIn, String contactMethodIn,
                  String baseChannelIn, String baseEntitlementIn, Date checkinIn, String typeIn) {
        if (rawIdIn != null) {
            this.rawId = rawIdIn.toString();
            this.id += this.rawId;
        }
        this.parentId = parentIdIn;
        this.name = nameIn;
        this.contactMethod = contactMethodIn;
        this.baseChannel = baseChannelIn;
        this.baseEntitlement = baseEntitlementIn;
        if (checkinIn != null) {
            this.checkin = checkinIn.getTime();
        }
        this.type = typeIn;
    }

    /**
     * Standard constructor
     * @param rawIdIn the real system id
     * @param nameIn nameIn
     * @param contactMethodIn contact method
     * @param baseChannelIn base channel
     * @param baseEntitlementIn base entitlement
     * @param checkinIn check-in
     */
    public System(Long rawIdIn, String nameIn, String contactMethodIn, String baseChannelIn,
            String baseEntitlementIn, Date checkinIn) {
        if (rawIdIn != null) {
            this.rawId = rawIdIn.toString();
            this.id += this.rawId;
        }
        this.name = nameIn;
        this.contactMethod = contactMethodIn;
        this.baseChannel = baseChannelIn;
        this.baseEntitlement = baseEntitlementIn;
        if (checkinIn != null) {
            this.checkin = checkinIn.getTime();
        }
    }

    /**
     * Gets the type.
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param idIn - the id
     * @return this
     */
    public System setId(String idIn) {
        id = idIn;
        return this;
    }

    /**
     * Gets the rawId.
     *
     * @return rawId
     */
    public String getRawId() {
        return rawId;
    }

    /**
     * Sets the rawId.
     *
     * @param rawIdIn - the rawId
     * @return this
     */
    public System setRawId(String rawIdIn) {
        rawId = rawIdIn;
        return this;
    }

    /**
     * Gets the parentId.
     *
     * @return parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Sets the parentId.
     *
     * @param parentIdIn - the parentId
     * @return this
     */
    public System setParentId(String parentIdIn) {
        parentId = parentIdIn;
        return this;
    }

    /**
     * Gets the name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param nameIn - the name
     * @return this
     */
    public System setName(String nameIn) {
        name = nameIn;
        return this;
    }

    /**
     * Gets the baseChannel.
     *
     * @return baseChannel
     */
    public String getBaseChannel() {
        return baseChannel;
    }

    /**
     * Sets the baseChannel.
     *
     * @param baseChannelIn - the baseChannel
     * @return this
     */
    public System setBaseChannel(String baseChannelIn) {
        baseChannel = baseChannelIn;
        return this;
    }

    /**
     * Gets the baseEntitlement.
     *
     * @return baseEntitlement
     */
    public String getBaseEntitlement() {
        return baseEntitlement;
    }

    /**
     * Sets the baseEntitlement.
     *
     * @param baseEntitlementIn - the baseEntitlement
     * @return this
     */
    public System setBaseEntitlement(String baseEntitlementIn) {
        baseEntitlement = baseEntitlementIn;
        return this;
    }

    /**
     * Gets the checkin.
     *
     * @return checkin
     */
    public Long getCheckin() {
        return checkin;
    }

    /**
     * Sets the checkin.
     *
     * @param checkinIn - the checkin
     * @return this
     */
    public System setCheckin(Long checkinIn) {
        checkin = checkinIn;
        return this;
    }

    /**
     * Gets the contactMethod.
     *
     * @return contactMethod
     */
    public String getContactMethod() {
        return contactMethod;
    }

    /**
     * Sets the contactMethod.
     *
     * @param contactMethodIn - the contactMethod
     * @return this
     */
    public System setContactMethod(String contactMethodIn) {
        contactMethod = contactMethodIn;
        return this;
    }

    /**
     * Gets the installedProducts.
     *
     * @return installedProducts
     */
    public Set<String> getInstalledProducts() {
        return installedProducts;
    }

    /**
     * Sets the installedProducts.
     *
     * @param installedProductsIn - the installedProducts
     * @return this
     */
    public System setInstalledProducts(Set<String> installedProductsIn) {
        installedProducts = installedProductsIn;
        return this;
    }

    /**
     * Gets the managedGroups.
     *
     * @return managedGroups
     */
    public Set<String> getManagedGroups() {
        return managedGroups;
    }

    /**
     * Sets the managedGroups.
     *
     * @param groupsIn - the managedGroups
     * @return this
     */
    public System setManagedGroups(Set<String> groupsIn) {
        managedGroups = groupsIn;
        return this;
    }

    /**
     * Gets the patchCounts.
     *
     * @return patchCounts
     */
    public List<Integer> getPatchCounts() {
        return patchCounts;
    }

    /**
     * Sets the patchCounts.
     *
     * @param patchCountsIn - the patchCounts
     * @return this
     */
    public System setPatchCounts(List<Integer> patchCountsIn) {
        patchCounts = patchCountsIn;
        return this;
    }
}
