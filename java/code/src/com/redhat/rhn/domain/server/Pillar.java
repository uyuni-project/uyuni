/**
 * Copyright (c) 2021 SUSE LLC
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

import com.redhat.rhn.domain.Identifiable;
import com.redhat.rhn.domain.org.Org;
import com.suse.utils.Json;

import org.apache.log4j.Logger;

import java.util.Optional;


/**
 * Pillar - Class representation of the table suseSaltPillars entries
 *
 * https://github.com/uyuni-project/uyuni-rfc/pull/51
 */
public class Pillar implements Identifiable {

    private static final Logger LOG = Logger.getLogger(Pillar.class);

    private Long id;
    private Optional<MinionServer> minion;
    private Optional<Org> org;
    private Optional<ManagedServerGroup> group;
    private String category;
    private Json pillar;

    /**
     * Constructor for global pillar
     *
     * @param categoryIn category of the pillar
     * @param pillarIn data in JSON format
     */
    public Pillar(String categoryIn, Json pillarIn) {
        initPillar(categoryIn, pillarIn, Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * Constructor for minion pillar
     *
     * @param categoryIn category of the pillar
     * @param pillarIn pillar data in JSON format
     * @param minionIn MinionServer owner of the pillar
     */
    public Pillar(String categoryIn, Json pillarIn, MinionServer minionIn) {
        initPillar(categoryIn, pillarIn, Optional.of(minionIn), Optional.empty(), Optional.empty());
    }

    /**
     * Constructor for system group pillar
     *
     * @param categoryIn category of the pillar
     * @param pillarIn pillar data in JSON format
     * @param groupIn ServerGroup owning the pillar
     */
    public Pillar(String categoryIn, Json pillarIn, ManagedServerGroup groupIn) {
        initPillar(categoryIn, pillarIn, Optional.empty(), Optional.of(groupIn), Optional.empty());
    }

    /**
     * Constructor for organization pillar
     *
     * @param categoryIn category of the pillar
     * @param pillarIn pillar data in JSON format
     * @param orgIn organization owning the pillar
     */
    public Pillar(String categoryIn, Json pillarIn, Org orgIn) {
        initPillar(categoryIn, pillarIn, Optional.empty(), Optional.empty(), Optional.of(orgIn));
    }

    private void initPillar(String categoryIn, Json pillarIn,
                            Optional<MinionServer> serverIn,
                            Optional<ManagedServerGroup> groupIn, Optional<Org> orgIn) {
        this.category = categoryIn;
        this.pillar = pillarIn;
        this.minion = serverIn;
        this.org = orgIn;
        this.group = groupIn;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long idIn) {
        this.id = idIn;
    }

    public boolean isMinionPillar() {
        return minion.isPresent();
    }

    public boolean isGroupPillar() {
        return group.isPresent();
    }

    public boolean isOrgPillar() {
        return org.isPresent();
    }

    public boolean isGlobalPillar() {
        return minion.isEmpty() && group.isEmpty() && org.isEmpty();
    }

    /**
     * Set pillar as group pillar and its owning group
     *
     * This removes any other owners and changes pillar to group pillar
     * @param groupIn SystemGroup owner of the pillar
     * @return itself
     */
    public Pillar setGroup(Optional<ManagedServerGroup> groupIn) {
        this.group = groupIn;
        this.org = Optional.empty();
        this.minion = Optional.empty();
        return this;
    }

    /**
     * Set pillar as organization pillar and its owning organization
     *
     * This removes any other owners and changes pillar to organization pillar
     * @param orgIn Org owner of the pillar
     * @return itself
     */
    public Pillar setOrg(Optional<Org> orgIn) {
        this.org = orgIn;
        this.group = Optional.empty();
        this.minion = Optional.empty();
        return this;
    }

    /**
     * Set pillar as minion pillar and its owning minion
     *
     * This removes any other owners and changes pillar to minion pillar
     * @param minionIn MinionServer owner of the pillar
     * @return itself
     */
    public Pillar setMinion(Optional<MinionServer> minionIn) {
        this.minion = minionIn;
        this.group = Optional.empty();
        this.org = Optional.empty();
        return this;
    }

    /**
     * Set pillar as global pillar and remove any owner
     *
     * This removes previous owner and changes pillar to global pillar
     * @return itself
     */
    public Pillar setGlobal() {
        this.minion = Optional.empty();
        this.group = Optional.empty();
        this.org = Optional.empty();
        return this;
    }

    /**
     * Get pillar value in JSON format
     *
     * @return pillar value
     */
    public Json getPillar() {
        return pillar;
    }

    /**
     * Set pillar value
     *
     * @param pillarIn pillar value in JSON format
     * @return itself
     */
    public Pillar setPillar(Json pillarIn) {
        this.pillar = pillarIn;
        return this;
    }

    /**
     * Get pillar category
     *
     * @see #setCategory
     * @return pillar category name
     */
    public String getCategory() {
        return category;
    }

    /**
     * Set pillar category
     *
     * Category is helper construct that each generator class can maintain their own pillar under its own category
     * and does not need to concert itself with merging pillar data into one pillar.
     *
     * @param categoryIn category name
     * @return itself
     */
    public Pillar setCategory(String categoryIn) {
        this.category = categoryIn;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return pillar.toString();
    }
}

