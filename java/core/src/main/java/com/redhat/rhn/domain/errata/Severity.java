/*
 * Copyright (c) 2009--2018 Red Hat, Inc.
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
package com.redhat.rhn.domain.errata;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.manager.errata.ErrataManager;

import org.hibernate.annotations.Immutable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Errata Severity
 *
 */
@Entity
@Table(name = "rhnErrataSeverity")
@Immutable
public class Severity implements Serializable {

    // WARNING: These must stay in sync with the values in rhnErrataSeverity
    // there's no need to keep 'unspecified' in db, it equals to null...
    public static final String LOW_LABEL = "errata.sev.label.low";
    public static final String MODERATE_LABEL = "errata.sev.label.moderate";
    public static final String IMPORTANT_LABEL = "errata.sev.label.important";
    public static final String CRITICAL_LABEL = "errata.sev.label.critical";
    public static final String UNSPECIFIED_LABEL = "errata.sev.label.unspecified";

    //dummy rank for webui selects
    public static final Integer UNSPECIFIED_RANK = 4;

    private static final Map<Integer, String> SEVERITY_MAP = Map.of(
            0, CRITICAL_LABEL,
            1, IMPORTANT_LABEL,
            2, MODERATE_LABEL,
            3, LOW_LABEL
    );

    @Serial
    private static final long serialVersionUID = 9009862760448217549L;

    @Id
    private Long id;
    @Column
    private int rank;
    @Column
    private String label;

    /**
     * Protected constructor accesible only by hibernate, to avoid create directly a severity instance.
     * <p>
     * Use {@link #getById(int)} or {@link #getByName(String)}.
     */
    protected Severity() {
        // Nothing to do
    }

    /**
     * Severity id
     * @param idIn id to set
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Severity id
     * @return id from DB
     */
    public Long getId() {
        return id;
    }

    /**
     * Sortable rank
     * @param rankIn rank to sort by
     */
    public void setRank(int rankIn) {
        rank = rankIn;
    }

    /**
     * Sortable rank
     * @return rank to sort by
     */
    public int getRank() {
        return rank;
    }

    /**
     * Label for the severity
     * Labels are resource bundle keys
     * @param labelIn label to set
     */
    public void setLabel(String labelIn) {
        label = labelIn;
    }

    /**
     * Label for the severity
     * Labels are resource bundle keys
     * @return severity label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Looks up label in resource bundle
     * @return localized string corresponding to severity label
     */
    public String getLocalizedLabel() {
        String retval = null;
        if (label != null) {
            retval = LocalizationService.getInstance().getMessage(label);
        }
        return retval;
    }

    /**
     * Looks up label for translated string
     * @return untranslated label
     * @param translated translated string
     */
    public static String getLabelForTranslation(String translated) {
        Map<String, String> labels = ErrataManager.advisorySeverityUntranslatedLabels();
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(translated)) {
                return entry.getKey();
            }
        }
        throw new LookupException("Specified severity is not correct!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Id: " + getId() + ", Rank: " + getRank() + ", Label: " + getLabel() +
            ", Localized label: " + getLocalizedLabel();
    }

    /**
     * Looks up corresponding Severity object by given id
     * @return Severity object
     * @param id severity_id
     */
    public static Severity getById(Integer id) {
        if (id == null || !SEVERITY_MAP.containsKey(id)) {
            return null;
        }

        Severity newSeverity = new Severity();
        newSeverity.setId(Integer.valueOf(id).longValue());
        newSeverity.setLabel(SEVERITY_MAP.get(id));
        newSeverity.setRank(id);
        return newSeverity;
    }

    /**
     * Looks up corresponding Severity object by given name
     * @return Severity object
     * @param name severity_name
     */
    public static Severity getByName(String name) {
        String key = getLabelForTranslation(name);
        if (UNSPECIFIED_LABEL.equals(key)) {
            return null;
        }

        for (Map.Entry<Integer, String> entry : SEVERITY_MAP.entrySet()) {
            if (entry.getValue().equals(key)) {
                Severity newSeverity = new Severity();
                newSeverity.setId(entry.getKey().longValue());
                newSeverity.setLabel(entry.getValue());
                newSeverity.setRank(entry.getKey());
                return newSeverity;
            }
        }

        // This return should never be reachable, since getLabelForTranslation() throws when the give name is missing.
        return null;
    }

}
