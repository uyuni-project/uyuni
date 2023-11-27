/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.common.finder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Finder implementation that uses multiple finders to search
 */
class CompositeFinder implements Finder {

    private final List<Finder> finderList;

    CompositeFinder(Collection<? extends Finder> finders) {
        this.finderList = new ArrayList<>(finders);
    }

    @Override
    public List<String> findExcluding(String[] excluding, String endStr) {
        return this.finderList.stream()
                              .flatMap(finder -> finder.findExcluding(excluding, endStr).stream())
                              .collect(Collectors.toList());
    }
}
