/*
 * Copyright (c) 2022 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.manager.content;

/**
 * JSON representation of GPG key information
 */
public class GpgInfoEntry {
    private String url;
    private String keyId;
    private String fingerprint;

    /**
     * @return gpg key url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param urlIn set gpg key url
     */
    public void setUrl(String urlIn) {
        url = urlIn;
    }

    /**
     * @return the gpg key id
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * @param keyIdIn set the key id
     */
    public void setKeyId(String keyIdIn) {
        keyId = keyIdIn;
    }

    /**
     * @return the gpg key fingerprint
     */
    public String getFingerprint() {
        return fingerprint;
    }

    /**
     * @param fingerprintIn set the fingerprint
     */
    public void setFingerprint(String fingerprintIn) {
        fingerprint = fingerprintIn;
    }
}
