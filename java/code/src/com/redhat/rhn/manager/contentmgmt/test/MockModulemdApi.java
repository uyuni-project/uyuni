/*
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.manager.contentmgmt.test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.Modules;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.contentmgmt.modulemd.ConflictingStreamsException;
import com.redhat.rhn.domain.contentmgmt.modulemd.Module;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModuleInfo;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModuleNotFoundException;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulePackagesResponse;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModuleStreams;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApi;
import com.redhat.rhn.domain.contentmgmt.modulemd.RepositoryNotModularException;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageEvrFactoryTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageNameTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.TestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Mock class for libmodulemd API
 */
public class MockModulemdApi extends ModulemdApi {

    private static final String MOUNT_POINT_PATH = Config.get().getString(ConfigDefaults.MOUNT_POINT);

    @Override
    public Map<String, ModuleStreams> getAllModulesInChannel(Channel channel)
            throws RepositoryNotModularException {
        // Dummy call to trigger RepositoryNotModular exception:
        getMetadataPath(channel);
        // Mock map
        Map<String, ModuleStreams> moduleStreamsMap = new HashMap<>();
        moduleStreamsMap.put("postgresql", new ModuleStreams("10", Arrays.asList("10", "9.6")));
        moduleStreamsMap.put("perl", new ModuleStreams("5.26", Arrays.asList("5.26", "5.24")));
        return moduleStreamsMap;
    }

    @Override
    public List<String> getAllPackages(List<Channel> sources) {
        // Dummy call to trigger RepositoryNotModular exception:
        getMetadataPaths(sources);

        // Mock list
        try {
            return doGetAllPackages();
        }
        catch (ModuleNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ModulePackagesResponse getPackagesForModules(List<Channel> sources, List<Module> selectedModules)
            throws ConflictingStreamsException, ModuleNotFoundException {
        // Dummy call to trigger RepositoryNotModular exception:
        getMetadataPaths(sources);

        Map<String, List<Module>> moduleMap = selectedModules.stream().collect(Collectors.groupingBy(Module::getName));
        for (Map.Entry<String, List<Module>> m : moduleMap.entrySet()) {
            if (m.getValue().size() > 1) {
                throw new ConflictingStreamsException(m.getValue().get(0), m.getValue().get(1));
            }
        }

        return new ModulePackagesResponse(getRpmApis(selectedModules), getPackages(selectedModules),
                selectedModules.stream()
                        .map(m -> new ModuleInfo(m.getName(), m.getStream(), "1000000001", "6789abcd", "x86_64"))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Gets the package count for a specificed module stream. Used for testing only.
     * @param module the module name
     * @param stream the stream name
     * @return the package count
     * @throws ModuleNotFoundException if the specified module is not found
     */
    public static int getPackageCount(String module, String stream) throws ModuleNotFoundException {
        return getPackages(singletonList(new Module(module, stream))).size();
    }

    /**
     * Creates a test channel including all the modular packages served by this mock class.
     *
     * In addition, the channel includes also the 'perl-5.26.3' package, which is part of 'perl:5.26' module definition,
     * but served as a regular package.
     *
     * @param user the user to create the channel with
     * @return a populated test channel
     */
    public static Channel createModularTestChannel(User user) throws Exception {
        Channel channel = TestUtils.reload(ChannelFactoryTest.createTestChannel(user, "channel-x86_64"));
        channel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha1"));
        Modules modulemd = new Modules();
        modulemd.setChannel(channel);
        modulemd.setRelativeFilename("/path/to/modulemd.yaml");
        channel.setModules(modulemd);

        List<String> nevras = doGetAllPackages();
        // perl 5.26 is a special package which is included in the module definition even though it's not served as a
        // modular package. We need it in the channel to be able to test this case.
        nevras.add("perl-5.26.3-416.el8:4.x86_64");

        Pattern nevraPattern = Pattern.compile("^(.*)-(\\d+):(.*)-(.*)\\.(.*)$");
        for (String nevra : nevras) {
            Matcher m = nevraPattern.matcher(nevra);
            if (m.matches()) {
                Package pkg = PackageTest.createTestPackage(user.getOrg());
                PackageArch packageArch = PackageFactory.lookupPackageArchByLabel(m.group(5));
                PackageTest.populateTestPackage(pkg, user.getOrg(), PackageNameTest.createTestPackageName(m.group(1)),
                        PackageEvrFactoryTest.createTestPackageEvr(m.group(2), m.group(3), m.group(4),
                                packageArch.getArchType().getPackageType()),
                                packageArch
                        );
                channel.addPackage(pkg);
            }
        }

        ChannelFactory.save(channel);
        return channel;
    }

    private static List<String> getRpmApis(List<Module> modules) throws ModuleNotFoundException {
        List<String> apiList = new LinkedList<>();

        List<Module> missingModules = new ArrayList<>();
        for (Module m : modules) {
            if ("postgresql".equals(m.getName()) && ("10".equals(m.getStream()) || "9.6".equals(m.getStream()))) {
                apiList.addAll(asList("postgresql", "postgresql-server"));
            }
            else if ("perl".equals(m.getName()) && "5.26".equals(m.getStream())) {
                // No apis in the module
            }
            else if ("perl".equals(m.getName()) && "5.24".equals(m.getStream())) {
                apiList.add("perl");
            }
            else {
                missingModules.add(m);
            }
        }

        if (!missingModules.isEmpty()) {
            throw new ModuleNotFoundException(missingModules);
        }
        return apiList;
    }

    private static List<String> getPackages(List<Module> moduleList) throws ModuleNotFoundException {
        // Mock package lists
        List<String> pkgList = new LinkedList<>();

        List<Module> missingModules = new ArrayList<>();
        for (Module module : moduleList) {
            if ("postgresql".equals(module.getName()) && "10".equals(module.getStream())) {
                pkgList.addAll(asList(
                        "postgresql-0:10.6-1.module_el8.0.0+15+f57f353b.x86_64",
                        "postgresql-server-0:10.6-1.module_el8.0.0+15+f57f353b.x86_64",
                        "postgresql-server-devel-0:10.6-1.module_el8.0.0+15+f57f353b.x86_64"
                ));
            }
            else if ("postgresql".equals(module.getName()) && "9.6".equals(module.getStream())) {
                pkgList.addAll(asList(
                        "postgresql-0:9.6.10-1.module_el8.0.0+16+7a9f6089.x86_64",
                        "postgresql-server-0:9.6.10-1.module_el8.0.0+16+7a9f6089.x86_64"
                ));
            }
            else if ("perl".equals(module.getName()) && "5.26".equals(module.getStream())) {
                // No pkgs in the module (perl-5.26 is served as a regular package)
            }
            else if ("perl".equals(module.getName()) && "5.24".equals(module.getStream())) {
                // Return multiple versions of the package. The different versions are represented as separate module
                // entries in the metadata and they are merged in to a package list with multiple versions.
                pkgList.addAll(asList(
                        "perl-0:5.24.0-xxx.x86_64",
                        "perl-0:5.24.1-yyy.x86_64"
                ));
            }
            else {
                missingModules.add(module);
            }
        }

        if (!missingModules.isEmpty()) {
            throw new ModuleNotFoundException(missingModules);
        }
        return pkgList;
    }

    private static List<String> doGetAllPackages() throws ModuleNotFoundException {
        return getPackages(asList(
                new Module("postgresql", "9.6"),
                new Module("postgresql",  "10"),
                new Module("perl", "5.26"),
                new Module("perl", "5.24")
        ));
    }

    private static List<String> getMetadataPaths(List<Channel> sources) throws RepositoryNotModularException {
        return sources.stream().map(MockModulemdApi::getMetadataPath).collect(Collectors.toList());
    }

    private static String getMetadataPath(Channel channel) throws RepositoryNotModularException {
        if (!channel.isModular()) {
            throw new RepositoryNotModularException();
        }
        Modules metadata = channel.getModules();
        return new File(MOUNT_POINT_PATH, metadata.getRelativeFilename()).getAbsolutePath();
    }

}
