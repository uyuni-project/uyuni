/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.manager.admin.validator;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorResult;

import com.suse.manager.admin.PaygAdminFields;

import org.apache.commons.lang3.StringUtils;

public class PaygAdminValidator {

    private PaygAdminValidator() {
    }

    /**
     * Validate ContentProject properties
     * @param description
     * @param host
     * @param port
     * @param username
     * @param password
     * @param key
     * @param keyPassword
     * @param bastionHost
     * @param bastionPort
     * @param bastionUsername
     * @param bastionPassword
     * @param bastionKey
     * @param bastionKeyPassword
     * @throws ValidatorException when the parameters do not pass the validation
     */
    public static void validatePaygData(String description,
                                         String host, Integer port,
                                         String username, String password,
                                         String key, String keyPassword,
                                         String bastionHost, Integer bastionPort,
                                         String bastionUsername, String bastionPassword,
                                         String bastionKey, String bastionKeyPassword) throws ValidatorException {
        ValidatorResult result = new ValidatorResult();

        if (StringUtils.isEmpty(description)) {
            result.addFieldError(PaygAdminFields.description.name(), "payg.description_required");
        }
        else {
            if (description.length() > 255) {
                result.addFieldError(PaygAdminFields.description.name(), "payg.description_to_long");
            }
        }

        if (StringUtils.isEmpty(host)) {
            result.addFieldError(PaygAdminFields.host.name(), "payg.host_required");
        }
        else {
            if (host.length() > 255) {
                result.addFieldError(PaygAdminFields.host.name(), "payg.host_to_long");
            }
        }

        if (port != null && port <= 0) {
            result.addFieldError(PaygAdminFields.port.name(), "payg.port_invalid");
        }

        if (StringUtils.isEmpty(username)) {
            result.addFieldError(PaygAdminFields.username.name(), "payg.username_required");
        }
        else {
            if (username.length() > 32) {
                result.addFieldError(PaygAdminFields.username.name(), "payg.username_to_long");
            }
        }

        if (!StringUtils.isEmpty(password) && password.length() > 32) {
            result.addFieldError(PaygAdminFields.password.name(), "payg.password_to_long");
        }

        if (!StringUtils.isEmpty(keyPassword) && keyPassword.length() > 32) {
            result.addFieldError(PaygAdminFields.key_password.name(), "payg.key_password_to_long");
        }

        if (!StringUtils.isEmpty(bastionHost) && bastionHost.length() > 255) {
            result.addFieldError(PaygAdminFields.bastion_host.name(), "payg.bastion_host_to_long");
        }
        if (StringUtils.isEmpty(bastionHost) &&
                (bastionPort != null ||
                !StringUtils.isEmpty(bastionUsername) ||
                !StringUtils.isEmpty(bastionPassword) ||
                !StringUtils.isEmpty(bastionKey) ||
                !StringUtils.isEmpty(bastionKeyPassword))) {
            result.addFieldError(PaygAdminFields.bastion_host.name(), "payg.bastion_host_required");
        }

        if (bastionPort != null && bastionPort <= 0) {
            result.addFieldError(PaygAdminFields.bastion_port.name(), "payg.bastion_port_invalid");
        }

        if (StringUtils.isEmpty(bastionUsername) && !StringUtils.isEmpty(bastionHost)) {
            result.addFieldError(PaygAdminFields.bastion_username.name(), "payg.bastion_username_required");
        }

        if (!StringUtils.isEmpty(bastionUsername) && bastionUsername.length() > 32) {
            result.addFieldError(PaygAdminFields.bastion_username.name(), "payg.bastion_username_to_long");
        }

        if (!StringUtils.isEmpty(bastionPassword) && bastionPassword.length() > 32) {
            result.addFieldError(PaygAdminFields.bastion_password.name(), "payg.bastion_password_to_long");
        }

        if (!StringUtils.isEmpty(bastionKeyPassword) && bastionKeyPassword.length() > 32) {
            result.addFieldError(PaygAdminFields.bastion_key_password.name(), "payg.bastion_key_password_to_long");
        }

        if (result.hasErrors()) {
            throw new ValidatorException(result);
        }
    }
}
