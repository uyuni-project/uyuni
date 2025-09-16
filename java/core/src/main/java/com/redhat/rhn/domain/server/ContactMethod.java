/*
 * Copyright (c) 2013 SUSE LLC
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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.AbstractLabelNameHelper;

/**
 * Class representation of the table suseServerContactMethod.
 */
public class ContactMethod extends AbstractLabelNameHelper {
    /**
     * Set the localized name.
     * @param key the key to lookup the localized name
     */
    @Override
    public void setName(String key) {
        super.setName(LocalizationService.getInstance().getMessage(key));
    }
}
