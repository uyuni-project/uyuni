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

package com.suse.scc.registration;

import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.server.ServerFactory;

import com.suse.scc.SCCSystemId;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.model.SCCSystemCredentialsJson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.Map;

/**
 * This object is responsible for updating cached tems .
 */
public class SCCSystemRegistrationUpdateCachedItems implements SCCSystemRegistrationContextHandler {
    private static final Logger LOG = LogManager.getLogger(SCCSystemRegistrationUpdateCachedItems.class);

    @Override
    public void handle(SCCSystemRegistrationContext context) {
        updateSuccessfullyRegisteredItems(context);
        updateFailedRegisteredItems(context);
        updatePaygSystems(context);

        // save all items
        context.getItems().forEach(cacheItem -> cacheItem.getOptServer().ifPresent(ServerFactory::save));
    }

    private void updateSuccessfullyRegisteredItems(SCCSystemRegistrationContext context) {
        for (SCCSystemCredentialsJson systemCredentials : context.getRegisteredSystems()) {
            SCCSystemId sccSystemId = new SCCSystemId(systemCredentials.getLogin(), systemCredentials.getPassword());
            SCCRegCacheItem cacheItem = context.getItemsBySccSystemId().get(sccSystemId);

            cacheItem.setSccId(systemCredentials.getId());
            cacheItem.setSccLogin(systemCredentials.getLogin());
            cacheItem.setSccPasswd(systemCredentials.getPassword());

            context.setItemAsNonRegistrationRequiredItem(cacheItem);
            context.getPendingRegistrationSystems().remove(sccSystemId);
        }
    }

    private void updateFailedRegisteredItems(SCCSystemRegistrationContext context) {
        for (Map.Entry<SCCSystemId, SCCRegisterSystemJson> entry : context.getPendingRegistrationSystems().entrySet()) {
            SCCRegCacheItem cacheItem = context.getItemsBySccSystemId().get(entry.getKey());
            LOG.error("Error registering system {}", cacheItem.getId());
            cacheItem.setRegistrationErrorTime(new Date());
        }
    }

    private void updatePaygSystems(SCCSystemRegistrationContext context) {
        context.getPaygSystems().forEach(cacheItem -> context.setItemAsNonRegistrationRequiredItem(cacheItem));
    }

}
