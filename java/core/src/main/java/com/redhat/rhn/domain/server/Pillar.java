/*
 * Copyright (c) 2021--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.server;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.Identifiable;
import com.redhat.rhn.domain.org.Org;

import io.hypersistence.utils.hibernate.type.json.JsonType;

import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


/**
 * Pillar - Class representation of the table suseSaltPillars entries
 *
 * <a href="https://github.com/uyuni-project/uyuni-rfc/pull/51">RFC</a>
 */


@Entity
@Table(name = "suseSaltPillar")
public class Pillar implements Identifiable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pillar_seq")
    @SequenceGenerator(name = "pillar_seq", sequenceName = "suse_salt_pillar_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "server_id")
    private MinionServer minion;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Org org;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private ServerGroup group;

    @Column(name = "category")
    private String category;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> pillar = new TreeMap<>();

    /**
     * Default constructor. Mostly for hibernate use.
     */
    public Pillar() {
        initPillar(null, null, null, null, null);
    }

    /**
     * Constructor for global pillar
     *
     * @param categoryIn category of the pillar
     * @param pillarIn data in JSON format
     */
    public Pillar(String categoryIn, Map<String, Object> pillarIn) {
        initPillar(categoryIn, pillarIn, null, null, null);
    }

    /**
     * Constructor for minion pillar
     *
     * @param categoryIn category of the pillar
     * @param pillarIn pillar data in JSON format
     * @param minionIn MinionServer owner of the pillar
     */
    public Pillar(String categoryIn, Map<String, Object> pillarIn, MinionServer minionIn) {
        initPillar(categoryIn, pillarIn, minionIn, null, null);
    }

    /**
     * Constructor for system group pillar
     *
     * @param categoryIn category of the pillar
     * @param pillarIn pillar data in JSON format
     * @param groupIn ServerGroup owning the pillar
     */
    public Pillar(String categoryIn, Map<String, Object> pillarIn, ServerGroup groupIn) {
        initPillar(categoryIn, pillarIn, null, groupIn, null);
    }

    /**
     * Constructor for organization pillar
     *
     * @param categoryIn category of the pillar
     * @param pillarIn pillar data in JSON format
     * @param orgIn organization owning the pillar
     */
    public Pillar(String categoryIn, Map<String, Object> pillarIn, Org orgIn) {
        initPillar(categoryIn, pillarIn, null, null, orgIn);
    }

    /**
     * @return The global pillars
     */
    public static List<Pillar> getGlobalPillars() {
        CriteriaBuilder criteriaBuilder = HibernateFactory.getSession().getCriteriaBuilder();
        CriteriaQuery<Pillar> criteriaQuery = criteriaBuilder.createQuery(Pillar.class);
        Root<Pillar> root = criteriaQuery.from(Pillar.class);
        criteriaQuery.where(
                criteriaBuilder.isNull(root.get("minion")),
                criteriaBuilder.isNull(root.get("org")),
                criteriaBuilder.isNull(root.get("group"))
        );
        return HibernateFactory.getSession().createQuery(criteriaQuery).getResultList();
    }

    /**
     * Create a global pillar and store it in the database.
     *
     * Note that this function doesn't check if an existing global pillar with the same category is existing.
     *
     * @param category the pillar category
     * @param data the pillar data
     *
     * @return the created pillar
     */
    public static Pillar createGlobalPillar(String category, Map<String, Object> data) {
        Pillar pillar = new Pillar(category, data);
        HibernateFactory.getSession().persist(pillar);
        return pillar;
    }

    private void initPillar(String categoryIn, Map<String, Object> pillarIn,
                            MinionServer serverIn,
                            ServerGroup groupIn, Org orgIn) {
        this.category = categoryIn;
        this.pillar = pillarIn;
        this.minion = serverIn;
        this.org = orgIn;
        this.group = groupIn;
    }

    @Override
    public Long getId() {
        return id;
    }
    public void setId(Long idIn) {
        this.id = idIn;
    }

    public boolean isMinionPillar() {
        return minion != null;
    }

    public boolean isGroupPillar() {
        return group != null;
    }

    public boolean isOrgPillar() {
        return org != null;
    }

    public boolean isGlobalPillar() {
        return minion == null && group == null && org == null;
    }

    /**
     * Set pillar as group pillar and its owning group
     *
     * This removes any other owners and changes pillar to group pillar
     * @param groupIn SystemGroup owner of the pillar
     * @return itself
     */
    public Pillar setGroup(ServerGroup groupIn) {
        this.group = groupIn;
        this.org = null;
        this.minion = null;
        return this;
    }

    /**
     * Set pillar as organization pillar and its owning organization
     *
     * This removes any other owners and changes pillar to organization pillar
     * @param orgIn Org owner of the pillar
     * @return itself
     */
    public Pillar setOrg(Org orgIn) {
        this.org = orgIn;
        this.group = null;
        this.minion = null;
        return this;
    }

    /**
     * Set pillar as minion pillar and its owning minion
     *
     * This removes any other owners and changes pillar to minion pillar
     * @param minionIn MinionServer owner of the pillar
     * @return itself
     */
    public Pillar setMinion(MinionServer minionIn) {
        this.minion = minionIn;
        this.group = null;
        this.org = null;
        return this;
    }

    /**
     * Set pillar as global pillar and remove any owner
     *
     * This removes previous owner and changes pillar to global pillar
     * @return itself
     */
    public Pillar setGlobal() {
        this.minion = null;
        this.group = null;
        this.org = null;
        return this;
    }

    /**
     * Get pillar value in JSON format
     *
     * @return pillar value
     */
    public Map<String, Object> getPillar() {
        return pillar;
    }

    /**
     * Get a single value from the pillar
     *
     * @param path  A colon-delimited string describing the JSON path (e.g., "A:B:C").
     *              An empty component means first available item is used (e.g., "A::B")
     * @throws LookupException if the path is invalid or entry does not exist
     * @return A pillar value object
     */
    public Object getPillarValue(String path) throws LookupException {
        Object value = getPillar();
        try {
            for (String key: path.split(":")) {
                Map<String, Object> entry = (Map<String, Object>)value;
                if (key.isEmpty()) {
                    value = entry.entrySet().iterator().next().getValue();
                }
                else {
                    value = entry.get(key);
                }
            }
        }
        catch (NullPointerException e) {
            throw new LookupException("The pillar path does not exist");
        }
        if (value == null) {
            throw new LookupException("The pillar entry does not exist");
        }

        return value;
    }

    /**
     * Inserts or updates a value in the pillar structure at the specified path.
     * The method will create the necessary nested objects if they do not exist.
     *
     * @param path  A colon-delimited string describing the JSON path (e.g., "A:B:C").
     *              Unlike in getter, empty components are not allowed.
     * @param value The value to be inserted.
     * @throws LookupException if the path is invalid (e.g., empty component)
     */
    public void setPillarValue(String path, Object value) throws LookupException {
        Map<String, Object> entry = this.pillar;
        String[] keys = path.split(":");
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (key.isEmpty()) {
                throw new LookupException("Empty key is not allowed");
            }
            // This is the last key
            if (i == keys.length - 1) {
                entry.put(key, value);
            }
            else {
                if (entry.containsKey(key)) {
                    // Traverse a level below
                    entry = (Map<String, Object>) entry.get(key);
                }
                else {
                    // Missing struct, create one
                    Map<String, Object> tmp = new TreeMap<>();
                    entry.put(key, tmp);
                    entry = tmp;
                }
            }
        }
    }

    /**
     * Set pillar value
     *
     * @param pillarIn pillar value in JSON format
     * @return itself
     */
    public Pillar setPillar(Map<String, Object> pillarIn) {
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
     * Add an entry at the pillar root level.
     *
     * @param key the pillar name
     * @param value the pillar value
     */
    public void add(String key, Object value) {
        getPillar().put(key, value);
    }

    /**
     * Get a list of ServerGroups for a given category
     *
     * @param category the pillar category
     * @return List of ServerGroups that have pillars with the given category
     */
    public static List<ServerGroup> getGroupsForCategory(String category) {
        CriteriaBuilder criteriaBuilder = HibernateFactory.getSession().getCriteriaBuilder();
        CriteriaQuery<ServerGroup> criteriaQuery = criteriaBuilder.createQuery(ServerGroup.class);
        Root<Pillar> root = criteriaQuery.from(Pillar.class);

        criteriaQuery.select(root.get("group"))
                 .where(criteriaBuilder.and(
                     criteriaBuilder.equal(root.get("category"), category),
                     criteriaBuilder.isNotNull(root.get("group"))
                 ))
                 .distinct(true);

        return HibernateFactory.getSession().createQuery(criteriaQuery).getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return pillar.toString();
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        Pillar pillar1 = (Pillar) oIn;
        return Objects.equals(id, pillar1.id) &&
                Objects.equals(minion, pillar1.minion) &&
                Objects.equals(org, pillar1.org) &&
                Objects.equals(group, pillar1.group) &&
                category.equals(pillar1.category) &&
                Objects.equals(pillar, pillar1.pillar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minion, org, group, category);
    }
}

