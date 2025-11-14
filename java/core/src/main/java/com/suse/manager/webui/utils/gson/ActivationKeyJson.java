/*
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

package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.token.ActivationKey;

/**
 * The Activation key JSON class
 */
public class ActivationKeyJson {

    private Long id;
    private String key;

    /**
     * Instantiates a new Activation key JSON object.
     *
     * @param idIn  the id
     * @param keyIn the key
     */
    public ActivationKeyJson(Long idIn, String keyIn) {
        this.id = idIn;
        this.key = keyIn;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param keyIn the key
     */
    public void setKey(String keyIn) {
        this.key = keyIn;
    }

    /**
     * Creates a JSON object from an activation key
     *
     * @param key the activation key
     * @return the activation key json
     */
    public static ActivationKeyJson fromActivationKey(ActivationKey key) {
        return new ActivationKeyJson(key.getId(), key.getKey());
    }
}
