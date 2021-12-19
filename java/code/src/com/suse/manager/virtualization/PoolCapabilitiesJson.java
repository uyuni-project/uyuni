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
package com.suse.manager.virtualization;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Class representing the salt output for pool capabilities
 */
public class PoolCapabilitiesJson {
    private boolean computed;

    @SerializedName("pool_types")
    private List<PoolType> poolTypes;


    /**
     * @return Returns the computed flag.
     */
    public boolean isComputed() {
        return computed;
    }


    /**
     * @param computedIn The computed flag to set.
     */
    public void setComputed(boolean computedIn) {
        computed = computedIn;
    }


    /**
     * @return Returns the pool types.
     */
    public List<PoolType> getPoolTypes() {
        return poolTypes;
    }


    /**
     * @param poolTypesIn The pool types to set.
     */
    public void setPoolTypes(List<PoolType> poolTypesIn) {
        poolTypes = poolTypesIn;
    }

    /**
     * Represents the items in the poolTypes property
     */
    public class PoolType {
        private String name;
        private boolean supported;
        private Options options;

        /**
         * @return Returns the name.
         */
        public String getName() {
            return name;
        }

        /**
         * @param nameIn The name to set.
         */
        public void setName(String nameIn) {
            name = nameIn;
        }

        /**
         * @return Returns the supported.
         */
        public boolean isSupported() {
            return supported;
        }

        /**
         * @param supportedIn The supported to set.
         */
        public void setSupported(boolean supportedIn) {
            supported = supportedIn;
        }

        /**
         * @return Returns the options.
         */
        public Options getOptions() {
            return options;
        }

        /**
         * @param optionsIn The options to set.
         */
        public void setOptions(Options optionsIn) {
            options = optionsIn;
        }
    }

    /**
     * Holds the pool and volume options structures
     */
    public class Options {
        private PoolOptions pool;
        private VolumeOptions volume;

        /**
         * @return Returns the pool options.
         */
        public PoolOptions getPool() {
            return pool;
        }

        /**
         * @param poolIn The pool options to set.
         */
        public void setPool(PoolOptions poolIn) {
            pool = poolIn;
        }

        /**
         * @return Returns the volume options.
         */
        public VolumeOptions getVolume() {
            return volume;
        }

        /**
         * @param volumeIn The volume options to set.
         */
        public void setVolume(VolumeOptions volumeIn) {
            volume = volumeIn;
        }
    }

    /**
     * Represents the pool options
     */
    public class PoolOptions {
        @SerializedName("default_format")
        private String defaultFormat;
        private List<String> sourceFormatType;

        /**
         * @return Returns the default format.
         */
        public String getDefaultFormat() {
            return defaultFormat;
        }

        /**
         * @param defaultFormatIn The default format to set.
         */
        public void setDefaultFormat(String defaultFormatIn) {
            defaultFormat = defaultFormatIn;
        }

        /**
         * @return Returns the sourceFormatType.
         */
        public List<String> getSourceFormatType() {
            return sourceFormatType;
        }

        /**
         * @param sourceFormatTypeIn The sourceFormatType to set.
         */
        public void setSourceFormatType(List<String> sourceFormatTypeIn) {
            sourceFormatType = sourceFormatTypeIn;
        }
    }

    /**
     * Represents the volume options
     */
    public class VolumeOptions {
        @SerializedName("default_format")
        private String defaultFormat;
        private List<String> targetFormatType;

        /**
         * @return Returns the default format.
         */
        public String getDefaultFormat() {
            return defaultFormat;
        }

        /**
         * @param defaultFormatIn The default format to set.
         */
        public void setDefaultFormat(String defaultFormatIn) {
            defaultFormat = defaultFormatIn;
        }

        /**
         * @return Returns the targetFormatType.
         */
        public List<String> getTargetFormatType() {
            return targetFormatType;
        }

        /**
         * @param targetFormatTypeIn The targetFormatType to set.
         */
        public void setTargetFormatType(List<String> targetFormatTypeIn) {
            targetFormatType = targetFormatTypeIn;
        }
    }
}
