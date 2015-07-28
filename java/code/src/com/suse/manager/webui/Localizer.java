/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui;

import com.redhat.rhn.common.localization.LocalizationService;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Localizer class allowing to use internationalized strings in templates.
 */
public class Localizer implements Map<String, String> {

    @Override
    public boolean containsKey(Object key) {
        return LocalizationService.getInstance().hasMessage(normalize(key));
    }

    @Override
    public String get(Object key) {
        return LocalizationService.getInstance().getMessage(normalize(key));
    }

    /**
     * HACK Mustache doesn't like "." in identifiers, but we use them
     * extensively in our files, so let's swap them with "$"
     */
    private String normalize(Object key) {
        return ((String) key).replace('$', '.');
    }

    // unsupported operations needed to implement the Map interface

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(Object valueIn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String put(String keyIn, String valueIn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String remove(Object keyIn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> mIn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
