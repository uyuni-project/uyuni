/**
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
package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Object representation of the results of a call to state.apply
 * images.kiwi-image-inspect.
 */
public class OSImageInspectSlsResult {

    public static class Image {

        @SerializedName("hash")
        private String hash;
        @SerializedName("compression")
        private String compression;
        @SerializedName("name")
        private String name;
        @SerializedName("filepath")
        private String filepath;
        @SerializedName("type")
        private String type;
        @SerializedName("basename")
        private String basename;
        @SerializedName("fstype")
        private String fstype;
        @SerializedName("version")
        private String version;
        @SerializedName("filename")
        private String filename;
        @SerializedName("arch")
        private String arch;
        @SerializedName("size")
        private Double size;

        public String getHash() {
            return hash;
        }

        public String getCompression() {
            return compression;
        }

        public String getName() {
            return name;
        }

        public String getFilepath() {
            return filepath;
        }

        public String getType() {
            return type;
        }

        public String getBasename() {
            return basename;
        }

        public String getFstype() {
            return fstype;
        }

        public String getVersion() {
            return version;
        }

        public String getFilename() {
            return filename;
        }

        public String getArch() {
            return arch;
        }

        public Double getSize() {
            return size;
        }
    }

    public static class BootImage {

        @SerializedName("kernel")
        private Kernel kernel;
        @SerializedName("basename")
        private String basename;
        @SerializedName("arch")
        private String arch;
        @SerializedName("name")
        private String name;
        @SerializedName("initrd")
        private Initrd initrd;

        public static class Kernel {
            @SerializedName("version")
            private String version;
            @SerializedName("hash")
            private String hash;
            @SerializedName("filename")
            private String filename;
            @SerializedName("size")
            private Double size;

            public String getVersion() {
                return version;
            }

            public String getHash() {
                return hash;
            }

            public String getFilename() {
                return filename;
            }

            public Double getSize() {
                return size;
            }
        }

        public Kernel getKernel() {
            return kernel;
        }

        public String getBasename() {
            return basename;
        }

        public String getArch() {
            return arch;
        }

        public String getName() {
            return name;
        }

        public Initrd getInitrd() {
            return initrd;
        }

        public static class Initrd {
            @SerializedName("version")
            private String version;
            @SerializedName("hash")
            private String hash;
            @SerializedName("filename")
            private String filename;
            @SerializedName("size")
            private Double size;

            public String getVersion() {
                return version;
            }

            public String getHash() {
                return hash;
            }

            public String getFilename() {
                return filename;
            }

            public Double getSize() {
                return size;
            }
        }
    }

    public static class Bundle {
        @SerializedName("hash")
        private String hash;
        @SerializedName("suffix")
        private String suffix;
        @SerializedName("filepath")
        private String filepath;
        @SerializedName("basename")
        private String basename;
        @SerializedName("filename")
        private String filename;
        @SerializedName("id")
        private String id;

        public String getHash() {
            return hash;
        }

        public String getSuffix() {
            return suffix;
        }

        public String getFilepath() {
            return filepath;
        }

        public String getBasename() {
            return basename;
        }

        public String getFilename() {
            return filename;
        }

        public String getId() {
            return id;
        }
    }

    @SerializedName("image")
    private Image image;
    @SerializedName("boot_image")
    private BootImage bootImage;
    @SerializedName("bundle")
    private Bundle bundle;
    @SerializedName("packages")
    private List<Package> packages;

    public static class Package {
        @SerializedName("name")
        private String name;
        @SerializedName("disturl")
        private String distUrl;
        @SerializedName("epoch")
        private String epoch;
        @SerializedName("version")
        private String version;
        @SerializedName("release")
        private String release;
        @SerializedName("arch")
        private String arch;

        public String getName() {
            return name;
        }

        public String getDistUrl() {
            return distUrl;
        }

        public String getEpoch() {
            return epoch;
        }

        public String getVersion() {
            return version;
        }

        public String getRelease() {
            return release;
        }

        public String getArch() {
            return arch;
        }
    }

    public List<Package> getPackages() {
        return packages;
    }

    public Image getImage() {
        return image;
    }

    public BootImage getBootImage() {
        return bootImage;
    }

    public Bundle getBundle() {
        return bundle;
    }
}
