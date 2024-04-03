/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
/*
 * Copyright (c) 2010 SUSE LINUX Products GmbH, Nuernberg, Germany.
 */
package org.cobbler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cobbler Distribution
 *
 * @author paji
 * @see <a href="https://cobbler.readthedocs.io/en/v3.3.3/code-autodoc/cobbler.items.html#module-cobbler.items.distro">RTFD - Cobbler - 3.3.3 - Distro</a>
 */
public class Distro extends CobblerObject {
    /**
     * Constant to define the field name for the Cobbler kernel property
     */
    private static final String KERNEL = "kernel";
    /**
     * Constant to define the field name for the Cobbler architecture property
     */
    private static final String ARCH = "arch";
    /**
     * Constant to define the field name for the Cobbler OS breed property
     */
    private static final String BREED = "breed";
    /**
     * Constant to define the field name for the Cobbler OS version property
     */
    private static final String OS_VERSION = "os_version";
    /**
     * Constant to define the field name for the Cobbler initrd property
     */
    private static final String INITRD = "initrd";
    /**
     * Constant to define the field name for the Cobbler source repository property
     */
    private static final String SOURCE_REPOS = "source_repos";
    /**
     * Constant to define the field name for the Cobbler tree build time property
     */
    private static final String TREE_BUILD_TIME = "tree_build_time";

    private Distro(CobblerConnection clientIn) {
        client = clientIn;
    }

    /**
     * Returns a distro matching the given name or null
     *
     * @param client the xmlrpc client
     * @param name   the distro name
     * @return the distro that maps to the name or null
     */
    public static Distro lookupByName(CobblerConnection client, String name) {
        return handleLookup(client, lookupDataMapByName(client, name, "get_distro"));
    }

    /**
     * Returns a distro matching the given uid or null
     *
     * @param client the xmlrpc client
     * @param id     the uid to search for
     * @return the distro matching the UID
     */
    public static Distro lookupById(CobblerConnection client, String id) {
        return handleLookup(client, lookupDataMapById(client,
                id, "find_distro"));
    }

    /**
     * Creates a Distribution that is constructed by the Map that is handed to the function
     *
     * @param client The Client that holds the connection to the Cobbler server
     * @param distroMap The Key-Value Map with the content of the distribution
     * @return Either null or the distribution that has been build by the Map
     */
    @SuppressWarnings("unchecked")
    private static Distro handleLookup(CobblerConnection client, Map<String, Object> distroMap) {
        if (distroMap != null) {
            Distro distro = new Distro(client);
            distro.dataMap = distroMap;
            distro.dataMapResolved = (Map<String, Object>) client.invokeMethod(
                    "get_distro",
                    distro.getName(), // object name
                    false, // flatten
                    true // resolved
            );
            return distro;
        }
        return null;
    }

    /**
     * Returns a list of available Distros
     *
     * @param connection the cobbler connection
     * @return a list of Distros.
     */
    @SuppressWarnings("unchecked")
    public static List<Distro> list(CobblerConnection connection) {
        List<Distro> distros = new LinkedList<>();
        List<Map<String, Object>> cDistros = (List<Map<String, Object>>)
                connection.invokeMethod("get_distros");

        for (Map<String, Object> distroMap : cDistros) {
            Distro distro = new Distro(connection);
            distro.dataMap = distroMap;
            distro.dataMapResolved = (Map<String, Object>) connection.invokeMethod(
                    "get_distro",
                    distro.getName(), // object name
                    false, // flatten
                    true // resolved
            );
            distros.add(distro);
        }
        return distros;
    }

    /**
     * @inheritDoc
     */
    @Override
    protected String invokeGetHandle() {
        return (String) client.invokeTokenMethod("get_distro_handle", this.getName());
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void invokeModify(String key, Object value) {
        client.invokeTokenMethod("modify_distro", getHandle(), key, value);
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void invokeModifyResolved(String key, Object value) {
        client.invokeTokenMethod("set_item_resolved_value", getUid(), key, value);
    }

    /**
     * Save the distro
     */
    @Override
    protected void invokeSave() {
        client.invokeTokenMethod("save_distro", getHandle());
    }

    /**
     * Remove the distro
     */
    @Override
    protected boolean invokeRemove() {
        return (Boolean) client.invokeTokenMethod("remove_distro", getName());
    }

    /**
     * Rename the distro
     */
    @Override
    protected void invokeRename(String newNameIn) {
        client.invokeTokenMethod("rename_distro", getHandle(), newNameIn);
    }

    /**
     * Reloads the distro
     */
    @Override
    public void reload() {
        Distro newDistro = lookupById(client, getId());
        dataMap = newDistro.dataMap;
        dataMapResolved = newDistro.dataMapResolved;
    }

    /**
     * Getter for the distro architecture
     *
     * @return the arch
     */
    public String getArch() {
        return (String) dataMap.get(ARCH);
    }


    /**
     * Setter for the distro architecture
     *
     * @param archIn the arch to set
     */
    public void setArch(String archIn) {
        modify(ARCH, archIn);
    }

    /**
     * Getter for the distro kernel path
     *
     * @return the kernelPath
     */
    public String getKernel() {
        return (String) dataMap.get(KERNEL);
    }


    /**
     * Setter for the distro kernel path
     *
     * @param kernelPathIn the kernelPath to set
     */
    public void setKernel(String kernelPathIn) {
        modify(KERNEL, kernelPathIn);
    }


    /**
     * Getter for the distro operating system version
     *
     * @return the osVersion
     */
    public String getOsVersion() {
        return (String) dataMap.get(OS_VERSION);
    }


    /**
     * Setter for the distro operating system version
     *
     * @param osVersionIn the osVersion to set
     */
    public void setOsVersion(String osVersionIn) {
        modify(OS_VERSION, osVersionIn);
    }


    /**
     * Getter for the distro initrd path
     *
     * @return the initrdPath
     */
    public String getInitrd() {
        return (String) dataMap.get(INITRD);
    }


    /**
     * Setter for the distro initrd path
     *
     * @param initrdPathIn the initrdPath to set
     */
    public void setInitrd(String initrdPathIn) {
        modify(INITRD, initrdPathIn);
    }


    /**
     * Getter for the source repositories
     *
     * @return the sourceRepos
     */
    public List<String> getSourceRepos() {
        return (List<String>) dataMap.get(SOURCE_REPOS);
    }


    /**
     * Setter for the source repositories
     *
     * @param sourceReposIn the sourceRepos to set
     */
    public void setSourceRepos(List<String> sourceReposIn) {
        modify(SOURCE_REPOS, sourceReposIn);
    }

    /**
     * Getter for the Tree Build Time in Cobbler
     *
     * @return the treeBuildTime
     */
    public long getTreeBuildTime() {
        return (Long) dataMap.get(TREE_BUILD_TIME);
    }


    /**
     * Setter for the Tree Build Time in Cobbler
     *
     * @param treeBuildTimeIn the treeBuildTime to set
     */
    public void setTreeBuildTime(long treeBuildTimeIn) {
        modify(TREE_BUILD_TIME, treeBuildTimeIn);
    }

    /**
     * Getter for the operating system breed
     *
     * @return the breed
     */
    public String getBreed() {
        return (String) dataMap.get(BREED);
    }


    /**
     * Setter for the operating system breed
     *
     * @param breedIn the breed to set
     */
    public void setBreed(String breedIn) {
        modify(BREED, breedIn);
    }

    /**
     * Builder to create a Distro
     *
     * @param <T> This parameter decides if you will supply a String or Map to the kernel and post kernel options.
     */
    public static class Builder<T> {

        private String name;
        private String kernel;
        private String initrd;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<Map<String, Object>> ksmeta = Optional.empty();
        private String breed;
        private String osVersion;
        private String arch = "x86_64";
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<T> kernelOptions = Optional.empty();
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<T> kernelOptionsPost = Optional.empty();


        /**
         * Set architecture
         *
         * @param archIn value to set
         * @return this builder to chain operations
         */
        public Builder<T> setArch(String archIn) {
            arch = archIn;
            return this;
        }

        /**
         * Set breed
         *
         * @param breedIn value to set
         * @return this builder to chain operations
         */
        public Builder<T> setBreed(String breedIn) {
            breed = breedIn;
            return this;
        }

        /**
         * Set initrd path
         *
         * @param initrdIn value to set
         * @return this builder to chain operations
         */
        public Builder<T> setInitrd(String initrdIn) {
            initrd = initrdIn;
            return this;
        }

        /**
         * Set kernel path
         *
         * @param kernelIn value to set
         * @return this builder to chain operations
         */
        public Builder<T> setKernel(String kernelIn) {
            kernel = kernelIn;
            return this;
        }

        /**
         * Set kernel options
         *
         * @param kernelOptionsIn value to set
         * @return this builder to chain operations
         */
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public Builder<T> setKernelOptions(Optional<T> kernelOptionsIn) {
            kernelOptions = kernelOptionsIn;
            return this;
        }

        /**
         * Set kernel options (post install)
         *
         * @param kernelOptionsPostIn value to set
         * @return this builder to chain operations
         */
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public Builder<T> setKernelOptionsPost(Optional<T> kernelOptionsPostIn) {
            kernelOptionsPost = kernelOptionsPostIn;
            return this;
        }

        /**
         * Set kickstart metadata
         *
         * @param ksmetaIn value to set
         * @return this builder to chain operations
         */
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public Builder<T> setKsmeta(Optional<Map<String, Object>> ksmetaIn) {
            ksmeta = ksmetaIn;
            return this;
        }

        /**
         * Set distro name
         *
         * @param nameIn value to set
         * @return this builder to chain operations
         */
        public Builder<T> setName(String nameIn) {
            name = nameIn;
            return this;
        }

        /**
         * Set OS version
         *
         * @param osVersionIn value to set
         * @return this builder to chain operations
         */
        public Builder<T> setOsVersion(String osVersionIn) {
            osVersion = osVersionIn;
            return this;
        }

        /**
         * Create the distro with the current builder settings
         *
         * @param connection Connection to use for Distro creation
         * @return this builder to chain operations
         */
        public Distro build(CobblerConnection connection) {
            Distro distro = new Distro(connection);
            distro.handle = (String) connection.invokeTokenMethod("new_distro");
            distro.modify(NAME, name, false);
            distro.modify(KERNEL, kernel, false);
            distro.modify(INITRD, initrd, false);
            distro.modify(ARCH, arch, false);
            distro.save();
            distro = lookupByName(connection, name);

            if (breed != null) {
                distro.setBreed(breed);
            }
            if (ksmeta.isPresent()) {
                if (ksmeta.get().containsKey("autoyast")) {
                    distro.setBreed("suse");
                }
                distro.setKsMeta(ksmeta);
            }
            if (osVersion != null) {
                distro.setOsVersion(osVersion);
            }
            if (kernelOptions.isPresent()) {
                distro.setKernelOptions(kernelOptions);
            }
            if (kernelOptionsPost.isPresent()) {
                distro.setKernelOptionsPost(kernelOptionsPost);
            }
            if (kernelOptionsPost.isEmpty()) {
                distro.setKernelOptionsPost(Optional.of(new HashMap<String, Object>()));
            }
            distro.save();
            distro = lookupByName(connection, name);
            return distro;
        }

    }
}
