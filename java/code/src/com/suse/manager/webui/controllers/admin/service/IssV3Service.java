/*
 * Copyright (c) 2025 SUSE LLC
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
package com.suse.manager.webui.controllers.admin.service;

import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssPeripheralChannels;
import com.suse.manager.webui.controllers.admin.beans.IssV3PeripheralsResponse;

import java.util.List;


public class IssV3Service {

    public List<IssV3PeripheralsResponse> getPeripheralsList() {
        return new HubFactory().listPeripherals().stream().map(
                peripheralEntity -> new IssV3PeripheralsResponse(
                        peripheralEntity.getId(),
                        peripheralEntity.getFqdn(),
                        peripheralEntity.getPeripheralChannels().size(),
                        peripheralEntity.getPeripheralChannels().stream()
                                .map(IssPeripheralChannels::getPeripheralOrgId).distinct().count(),
                        peripheralEntity.getRootCa()
                )
        ).toList();
    }
}
