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
package com.redhat.rhn.frontend.action.multiorg;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.frontend.taglibs.list.BaseListFilter;

import java.util.Locale;
import java.util.Map;

/**
 * UserListFilter
 *
 */
public class TrustListFilter extends BaseListFilter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void processMap(Map<String, String> map, Locale userLocale) {
        LocalizationService ls = LocalizationService.getInstance();
        String key = ls.getMessage("org.trust.org", userLocale);
        map.put(key, "org.name");
    }

}
