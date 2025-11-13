/*
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

/**
 * Class to represent grains defined against 'start_event_grains'
 */
public class MinionStartupGrains {
    @SerializedName("machine_id")
    private Optional<String> machineId = Optional.empty();
    @SerializedName("saltboot_initrd")
    private boolean saltbootInitrd = false;
    @SerializedName("susemanager")
    private Optional<SuseManagerGrain> suseManagerGrain = Optional.empty();

    /**
     * no-arg constructor
     */
    public MinionStartupGrains() { }

    /**
     * Constructor which accepts MinionStartupGrainsBuilder and use it to build object
     * @param builder MinionStartupGrainsBuilder
     */
    private MinionStartupGrains(MinionStartupGrainsBuilder builder) {
        this.machineId = Optional.ofNullable(builder.machineId);
        this.saltbootInitrd = builder.saltbootInitrd;
        this.suseManagerGrain = Optional.ofNullable(builder.suseManagerGrain);
    }

    public Optional<String> getMachineId() {
        return machineId;
    }

    public boolean getSaltbootInitrd() {
        return saltbootInitrd;
    }

    public Optional<SuseManagerGrain> getSuseManagerGrain() {
        return suseManagerGrain;
    }

    /**
     * Class to represent `susemanager grain
     */
    public static class SuseManagerGrain {
        @SerializedName("management_key")
        private Optional<String> managementKey = Optional.empty();

        /**
         * no-arg constructor
         */
        public SuseManagerGrain() { }

        /**
         * Constructor which accepts needed grains and return the object
         * @param managementKeyIn management_key grain
         */
        public SuseManagerGrain(Optional<String> managementKeyIn) {
            this.managementKey = managementKeyIn;
        }

        public Optional<String> getManagementKey() {
            return managementKey;
        }
    }

    /**
     * Builder class to build MinionstartupGrains.
     */
    public static class MinionStartupGrainsBuilder {
        private String machineId;
        private boolean saltbootInitrd;
        private SuseManagerGrain suseManagerGrain;

        /**
         * setter for machineId grain
         * @param machineIdIn machineId
         * @return MinionStartupGrainsBuilder
         */
        public MinionStartupGrainsBuilder machineId(String machineIdIn) {
            this.machineId = machineIdIn;
            return this;
        }

        /**
         * setter for saltboot_initrdIn grain
         * @param saltbootInitrdIn saltbootInitrdIn
         * @return MinionStartupGrainsBuilder
         */
        public MinionStartupGrainsBuilder saltbootInitrd(boolean saltbootInitrdIn) {
            this.saltbootInitrd = saltbootInitrdIn;
            return this;
        }

        /**
         * setter for susemanager grain
         * @param suseManagerGrainIn suseManagerGrainIn
         * @return MinionStartupGrainsBuilder
         */
        public MinionStartupGrainsBuilder susemanagerGrain(SuseManagerGrain suseManagerGrainIn) {
            this.suseManagerGrain = suseManagerGrainIn;
            return this;
        }

        /**
         * Method to actually create the {@link MinionStartupGrains} object
         * @return MinionStartupGrains object
         */
        public MinionStartupGrains createMinionStartUpGrains() {
            return new MinionStartupGrains(this);
        }
    }
}
