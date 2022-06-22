/*
 * Copyright (c) 2018 SUSE LLC
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

import com.suse.manager.webui.utils.salt.custom.ImageChecksum.Checksum;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Object representation of the results of a call to state.apply
 * images.kiwi-image-inspect.
 */
public class OSImageInspectSlsResult {

    /**
     * The type Image.
     */
    public static class Image {

        @SerializedName("hash")
        private Checksum checksum;
        private String compression;
        private String compressed_hash;
        private String name;
        private String filepath;
        private String type;
        private String basename;
        private String fstype;
        private String version;
        private String filename;
        private String arch;
        private String build_id;
        private Long size;

        /**
         * @return the hash
         */
        public Checksum getChecksum() {
            return checksum;
        }

        /**
         * @return the compression
         */
        public String getCompression() {
            return compression;
        }

        /**
         * @return the compression checksum
         */
        public String getCompressedHash() {
            return compressed_hash;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the filepath
         */
        public String getFilepath() {
            return filepath;
        }

        /**
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * @return the basename
         */
        public String getBasename() {
            return basename;
        }

        /**
         * @return the filesystem type
         */
        public String getFstype() {
            return fstype;
        }

        /**
         * @return the version
         */
        public String getVersion() {
            return version;
        }

        /**
         * @return the filename
         */
        public String getFilename() {
            return filename;
        }

        /**
         * @return the arch
         */
        public String getArch() {
            return arch;
        }

        /**
         * @return the build id
         */
        public String getId() {
            return build_id;
        }

        /**
         * @return the image size
         */
        public Long getSize() {
            return size;
        }
    }

    /**
     * The type BootImage.
     */
    public static class BootImage {

        private Kernel kernel;
        private String basename;
        private String arch;
        private String name;
        private Initrd initrd;

        /**
         * The type Kernel.
         */
        public static class Kernel {
            private String version;
            @SerializedName("hash")
            private Checksum checksum;
            private String filename;
            private String filepath;
            private Long size;

            /**
             * @return the version
             */
            public String getVersion() {
                return version;
            }

            /**
             * @return the hash
             */
            public Checksum getChecksum() {
                return checksum;
            }

            /**
             * @return the filename
             */
            public String getFilename() {
                return filename;
            }

            /**
             * @return the filepath
             */
            public String getFilepath() {
                return filepath;
            }

            /**
             * @return the size
             */
            public Long getSize() {
                return size;
            }
        }

        /**
         * @return the kernel
         */
        public Kernel getKernel() {
            return kernel;
        }

        /**
         * @return the basename
         */
        public String getBasename() {
            return basename;
        }

        /**
         * @return the arch
         */
        public String getArch() {
            return arch;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the initrd
         */
        public Initrd getInitrd() {
            return initrd;
        }

        /**
         * The type Initrd.
         */
        public static class Initrd {
            private String version;
            @SerializedName("hash")
            private Checksum checksum;
            private String filename;
            private String filepath;
            private Long size;

            /**
             * @return the version
             */
            public String getVersion() {
                return version;
            }

            /**
             * @return the hash
             */
            public Checksum getChecksum() {
                return checksum;
            }

            /**
             * @return the filename
             */
            public String getFilename() {
                return filename;
            }

            /**
             * @return the filepath
             */
            public String getFilepath() {
                return filepath;
            }

            /**
             * @return the size
             */
            public Long getSize() {
                return size;
            }
        }
    }

    /**
     * The type Bundle.
     */
    public static class Bundle {
        @SerializedName("hash")
        private Checksum checksum;
        private String suffix;
        private String filepath;
        private String basename;
        private String filename;
        private String id;

        /**
         * @return the checksum
         */
        public Checksum getChecksum() {
            return checksum;
        }

        /**
         * @return the suffix
         */
        public String getSuffix() {
            return suffix;
        }

        /**
         * @return the filepath
         */
        public String getFilepath() {
            return filepath;
        }

        /**
         * @return the basename
         */
        public String getBasename() {
            return basename;
        }

        /**
         * @return the filename
         */
        public String getFilename() {
            return filename;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }
    }

    private Image image;
    @SerializedName("boot_image")
    private BootImage bootImage;
    private Bundle bundle;
    private List<Bundle> bundles;
    private List<Package> packages;

    /**
     * The type Package.
     */
    public static class Package {
        private String name;
        private String distUrl;
        private String epoch;
        private String version;
        private String release;
        private String arch;

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the dist url
         */
        public String getDistUrl() {
            return distUrl;
        }

        /**
         * @return the epoch
         */
        public String getEpoch() {
            return epoch;
        }

        /**
         * @return the version
         */
        public String getVersion() {
            return version;
        }

        /**
         * @return the release
         */
        public String getRelease() {
            return release;
        }

        /**
         * @return the arch
         */
        public String getArch() {
            return arch;
        }
    }

    /**
     * @return the packages
     */
    public List<Package> getPackages() {
        return packages;
    }

    /**
     * @return the image
     */
    public Image getImage() {
        return image;
    }

    /**
     * @return the boot image
     */
    public Optional<BootImage> getBootImage() {
        return Optional.ofNullable(bootImage);
    }

    /**
     * @return the bundle
     */
    public List<Bundle> getBundles() {
        if (bundles != null) {
            return bundles;
        }
        else if (bundle != null) {
            return Collections.singletonList(bundle);
        }
        else {
            return Collections.emptyList();
        }
    }
}
