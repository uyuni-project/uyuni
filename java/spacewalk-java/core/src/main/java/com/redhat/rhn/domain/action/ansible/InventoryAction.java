/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.ansible;

import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.AnsibleFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ansible.InventoryPath;
import com.redhat.rhn.manager.system.AnsibleManager;

import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.utils.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;


/**
 * InventoryAction - Action class representing the execution of an Ansible inventory refresh
 */
@Entity
@DiscriminatorValue("525")
public class InventoryAction extends Action {
    private static final Logger LOG = LogManager.getLogger(InventoryAction.class);

    @OneToOne(mappedBy = "parentAction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private InventoryActionDetails details;

    /**
     * Return the details.
     * @return details
     */
    public InventoryActionDetails getDetails() {
        return details;
    }

    /**
     * Set the details.
     * @param detailsIn details
     */
    public void setDetails(InventoryActionDetails detailsIn) {
        if (detailsIn != null) {
            detailsIn.setParentAction(this);
        }
        this.details = detailsIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (!(oIn instanceof InventoryAction that)) {
            return false;
        }
        return new EqualsBuilder().appendSuper(super.equals(oIn)).append(details, that.details).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(details)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        return singletonMap(executeInventoryActionCall(), minionSummaries);
    }

    private LocalCall<?> executeInventoryActionCall() {
        String inventoryPath = details.getInventoryPath();

        return new LocalCall<>(SaltParameters.ANSIBLE_INVENTORIES, empty(), of(Map.of("inventory", inventoryPath)),
                new TypeToken<>() { });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        if (jsonResult == null) {
            serverAction.setStatusFailed();
            serverAction.setResultMsg(
                    "Error while requesting inventory data from target system: Got no result from system");
            return;
        }
        String inventoryPath = details.getInventoryPath();
        if (serverAction.isStatusCompleted()) {
            try {
                Set<String> inventorySystems = AnsibleManager.parseInventoryAndGetHostnames(
                        Json.GSON.fromJson(jsonResult, Map.class));

                InventoryPath inventory = AnsibleFactory.lookupAnsibleInventoryPath(
                                serverAction.getServerId(), inventoryPath)
                        .orElseThrow(() -> new LookupException("Unable to find Ansible inventory: " +
                                inventoryPath + " for system " + serverAction.getServerId()));

                Set<Server> systemsToAdd = inventorySystems.stream().map(s -> ServerFactory.findByFqdn(s)
                        .orElse(null)).filter(Objects::nonNull).collect(Collectors.toSet());

                AnsibleManager.handleInventoryRefresh(inventory, systemsToAdd);
                AnsibleFactory.saveAnsiblePath(inventory);

                serverAction.setResultMsg("Refreshed Ansible managed systems of inventory: '" + inventoryPath + "'");
            }
            catch (JsonSyntaxException e) {
                LOG.error("Unable to parse Ansible hostnames from json: {}", e.getMessage());
                serverAction.setStatusFailed();
                serverAction.setResultMsg("Unable to parse hostnames from inventory: " + inventoryPath);
            }
            catch (LookupException e) {
                LOG.error(e.getMessage());
                serverAction.setStatusFailed();
                serverAction.setResultMsg(e.getMessage());
            }
        }
        else {
            serverAction.setResultMsg(jsonResult.getAsString());
        }
    }
}
