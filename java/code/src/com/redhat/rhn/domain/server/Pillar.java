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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;


/**
 * Pillar - Class representation of the table suseSaltPillars entries
 *
 * <a href="https://github.com/uyuni-project/uyuni-rfc/pull/51">RFC</a>
 */


@TypeDef(name = "json", typeClass = JsonType.class)
@Entity
@Table(name = "suseSaltPillar")
public class Pillar implements Identifiable, Serializable {

    @Id
    @GeneratedValue(generator = "pillar_seq")
    @GenericGenerator(
            name = "pillar_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "suse_salt_pillar_id_seq"),
                    @Parameter(name = "increment_size", value = "1")
            })
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

    @Type(type = "json")
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
        HibernateFactory.getSession().save(pillar);
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
     * Get a single string value from the pillar
     * The path consists of : separated components. An empty component takes the first item.
     *
     * @param path the path in the pillar
     * @return the value
     */
    public String getPillarValue(String path) {
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

        return (String)value;
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

