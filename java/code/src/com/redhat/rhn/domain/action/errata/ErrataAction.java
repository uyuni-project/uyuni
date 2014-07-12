/**
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
package com.redhat.rhn.domain.action.errata;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.server.Server;

import java.util.HashSet;
import java.util.Set;

/**
 * ErrataAction - Class representation of the table rhnAction.
 * @version $Rev$
 */
public class ErrataAction extends Action {

    private Set<Errata> errata;

    /**
     * @return Returns the errata.
     */
    public Set<Errata> getErrata() {
        return errata;
    }

    /**
     * @param errataIn The errata to set.
     */
    public void setErrata(Set<Errata> errataIn) {
        this.errata = errataIn;
    }

    /**
     * Add an Errata to this action.
     * @param e Errata to add
     */
    public void addErrata(Errata e) {
        if (errata == null) {
            errata = new HashSet<Errata>();
        }
        errata.add(e);
    }

    /**
     * Get the Formatter for this class but in this case we use
     * ErrataActionFormatter.
     *
     * {@inheritDoc}
     */
    public ActionFormatter getFormatter() {
        if (formatter == null) {
            formatter = new ErrataActionFormatter(this);
        }
        return formatter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHistoryDetails(Server server) {
        LocalizationService ls = LocalizationService.getInstance();
        StringBuilder retval = new StringBuilder();
        retval.append("</br>");
        retval.append(ls.getMessage("system.event.affectedErrata"));
        retval.append("</br>");
        retval.append("<ul>");
        for (Errata e : this.getErrata()) {
            retval.append("<li>");
            retval.append(e.getAdvisoryName());
            retval.append(" - ");
            retval.append(e.getSynopsis());
            retval.append("</li>");
        }
        retval.append("</ul>");
        return retval.toString();
    }

}
