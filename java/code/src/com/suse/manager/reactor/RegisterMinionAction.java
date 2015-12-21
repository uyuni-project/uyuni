/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.CPUArch;
import com.redhat.rhn.domain.server.Dmi;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;

import com.suse.manager.webui.utils.salt.Smbios;
import com.suse.saltstack.netapi.calls.modules.Pkg;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Event handler to create system records for salt minions.
 */
public class RegisterMinionAction extends AbstractDatabaseAction {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(RegisterMinionAction.class);

    // Reference to the SaltService instance
    private final SaltService SALT_SERVICE;


    //HACK: set installed product depending on the grains
    // to get access to suse channels
    private final HashMap<String, Long> productIdMap = new HashMap<>();

    /**
     * Default constructor.
     */
    public RegisterMinionAction() {
        this(SaltAPIService.INSTANCE);
    }

    /**
     * Constructor taking a {@link SaltService} instance.
     *
     * @param saltService the salt service to use
     */
    protected RegisterMinionAction(SaltService saltService) {
        SALT_SERVICE = saltService;
        productIdMap.put("SLES12x86_64", 1117L);
        productIdMap.put("SLES12.1x86_64", 1322L);
        productIdMap.put("SLES11x86_64", 824L);
        productIdMap.put("SLES11.1x86_64", 769L);
        productIdMap.put("SLES11.2x86_64", 690L);
        productIdMap.put("SLES11.3x86_64", 814L);
        productIdMap.put("SLES11.4x86_64", 1300L);
    }

    /**
     * {@inheritDoc}
     */
    public void doExecute(EventMessage msg) {
        RegisterMinionEvent event = (RegisterMinionEvent) msg;
        String minionId = event.getMinionId();

        // Match minions via their machine id
        String machineId = SALT_SERVICE.getMachineId(minionId);
        if (machineId == null) {
            LOG.info("Cannot find machine id for minion: " + minionId);
            return;
        }
        if (ServerFactory.findRegisteredMinion(machineId) != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Minion already registered, skipping registration: " +
                        minionId + " [" + machineId + "]");
            }
            return;
        }
        try {
            // Create the server
            Server server = ServerFactory.createServer();
            server.setName(minionId);
            server.setDigitalServerId(machineId);

            // All registered minions initially belong to the default organization
            server.setOrg(OrgFactory.getSatelliteOrg());

            SALT_SERVICE.syncGrains(minionId);

            // TODO: Set complete OS, hardware and network information here
            Map<String, Object> grains = SALT_SERVICE.getGrains(minionId);

            String osfullname = getValueAsString(grains, "osfullname");
            String osrelease = getValueAsString(grains, "osrelease");
            String kernelrelease = getValueAsString(grains, "kernelrelease");
            String osarch = getValueAsString(grains, "osarch");

            server.setOs(osfullname);
            server.setRelease(osrelease);
            server.setRunningKernel(kernelrelease);
            server.setSecret(RandomStringUtils.randomAlphanumeric(64));
            server.setAutoUpdate("N");
            server.setLastBoot(System.currentTimeMillis() / 1000);
            server.setCreated(new Date());
            server.setModified(server.getCreated());
            server.setContactMethod(ServerFactory.findContactMethodByLabel("default"));
            server.setServerArch(
                    ServerFactory.lookupServerArchByLabel(osarch + "-redhat-linux"));
            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setServer(server);
            server.setServerInfo(serverInfo);

            mapHardwareDetails(minionId, server, grains);

            Map<String, Pkg.Info> saltPackages =
                    SALT_SERVICE.getInstalledPackageDetails(minionId);
            Set<InstalledPackage> packages = saltPackages.entrySet().stream().map(
                    entry -> createPackageFromSalt(entry.getKey(), entry.getValue(), server)
            ).collect(Collectors.toSet());

            server.setPackages(packages);


            //HACK: set installed product depending on the grains
            // to get access to suse channels
            String key = osfullname + osrelease + osarch;
            Optional.ofNullable(productIdMap.get(key)).ifPresent(productId -> {
                SUSEProduct product =  SUSEProductFactory.lookupByProductId(productId);
                if (product != null) {
                    // Insert into suseInstalledProduct
                    InstalledProduct prd = new InstalledProduct();
                    prd.setName(product.getName());
                    prd.setVersion(product.getVersion());
                    prd.setRelease(product.getRelease());
                    prd.setArch(product.getArch());
                    prd.setBaseproduct(true);

                    Set<InstalledProduct> products = new HashSet<>();
                    products.add(prd);

                    // Insert into suseServerInstalledProduct
                    server.setInstalledProducts(products);
                }
            });

            ServerFactory.save(server);

            // Assign the SaltStack base entitlement by default
            server.setBaseEntitlement(
                    EntitlementManager.getByName(EntitlementManager.SALTSTACK_ENTITLED));

            Map<String, String> data = new HashMap<>();
            data.put("minionId", minionId);
            data.put("machineId", machineId);
            SALT_SERVICE.sendEvent("susemanager/minion/registered", data);
            LOG.info("Finished minion registration: " + minionId);
        }
        catch (Throwable t) {
            LOG.error("Error registering minion for event: " + event, t);
        }
    }

    private void mapHardwareDetails(String minionId, Server server,
            Map<String, Object> grains) {
        String cpuarch = getValueAsString(grains, "cpuarch");
        server.setRam(getValueAsLong(grains, "mem_total").orElse(0L));

        mapCpuDetails(minionId, server, grains, cpuarch);
        mapDmiInfo(minionId, server);

        // TODO devices information
    }

    private CPU mapCpuDetails(String minionId, Server server, Map<String, Object> grains,
            String cpuarch) {
        CPU cpu = new CPU();

        cpu.setModel(getValueAsString(grains, "cpu_model"));
        cpu.setNrCPU(getValueAsLong(grains, "num_cpus").orElse(0L));

        CPUArch arch = ServerFactory.lookupCPUArchByName(cpuarch);
        cpu.setArch(arch);

        Map<String, Object> cpuinfo = SALT_SERVICE.getCpuInfo(minionId);

        cpu.setMHz(getValueAsString(cpuinfo, "cpu MHz"));
        cpu.setVendor(getValueAsString(cpuinfo, "vendor_id"));
        cpu.setStepping(getValueAsString(cpuinfo, "stepping"));
        cpu.setFamily(getValueAsString(cpuinfo, "cpu family"));
        cpu.setCache(getValueAsString(cpuinfo, "cache size"));
        cpu.setNrsocket(getValueAsLong(grains, "cpusockets").orElse(0L));

        if (arch != null) {
            // shuld not happen but if we don't have the arch we cannot insert the cpu data
            cpu.setServer(server);
            server.setCpu(cpu);
        }

        return cpu;
    }

    private Dmi mapDmiInfo(String minionId, Server server) {
        String biosVendor = null, biosVersion = null, biosReleseDate = null,
                productName = null, systemVersion = null, systemSerial = null,
                chassisSerial = null, chassisTag = null, boardSerial = null;

        try {
            // TODO get all records at once? less roundtrips but larger response
            Map<String, Object>  bios = SALT_SERVICE.getDmiRecords(minionId,
                    Smbios.RecordType.BIOS);
            Map<String, Object>  system = SALT_SERVICE.getDmiRecords(minionId,
                    Smbios.RecordType.SYSTEM);
            Map<String, Object>  chassis = SALT_SERVICE.getDmiRecords(minionId,
                    Smbios.RecordType.CHASSIS);
            Map<String, Object>  board = SALT_SERVICE.getDmiRecords(minionId,
                    Smbios.RecordType.BASEBOARD);

            biosVendor = getValueAsString(bios, "vendor");
            biosVersion = getValueAsString(bios, "version");
            biosReleseDate = getValueAsString(bios, "release_date");

            productName = getValueAsString(system, "product_name");
            systemVersion = getValueAsString(system, "version");
            systemSerial = getValueAsString(system, "serial_number");

            chassisSerial = getValueAsString(chassis, "serial_number");
            chassisTag = getValueAsString(chassis, "asset_tag");

            boardSerial = getValueAsString(board, "serial_number");
        }
        catch (com.google.gson.JsonSyntaxException e) {
            LOG.warn("Could not retrieve DMI info from minion '" + minionId +
                    "'. JSON syntax error.");
            // In order to behave like the "old style" registration we
            // go on and persist an empty Dmi bean.
        }

        Dmi dmi = new Dmi();
        StringBuilder system = new StringBuilder();
        if (StringUtils.isNotBlank(productName)) {
            system.append(productName);
        }
        if (StringUtils.isNotBlank(systemVersion)) {
            if (system.length() > 0) {
                system.append(" ");
            }
            system.append(systemVersion);
        }
        dmi.setSystem(system.length() > 0 ? system.toString().trim() : null);
        dmi.setProduct(productName);
        dmi.setBios(biosVendor, biosVersion, biosReleseDate);
        dmi.setVendor(biosVendor);

        dmi.setAsset(String.format("(chassis: %s) (chassis: %s) (board: %s) (system: %s)",
                Objects.toString(chassisSerial, ""), Objects.toString(chassisTag, ""),
                Objects.toString(boardSerial, ""), Objects.toString(systemSerial, "")));

        dmi.setServer(server);
        server.setDmi(dmi);

        return dmi;
    }

    private String getValueAsString(Map<String, Object> valueMap, String key) {
        return get(valueMap, key).map(Object::toString).orElse("");
    }

    private Optional<Long> getValueAsLong(Map<String, Object> valueMap, String key) {
        return get(valueMap, key).flatMap(this::toLong);
    }

    private Optional<Long> toLong(Object value) {
        if (value instanceof Double) {
            return Optional.of(((Double)value).longValue());
        }
        else if (value instanceof Long) {
            return Optional.of((Long)value);
        }
        else if (value instanceof Integer) {
            return Optional.of(((Integer)value).longValue());
        }
        else if (value instanceof String) {
            try {
                return Optional.of(Long.parseLong((String) value));
            }
            catch (NumberFormatException e) {
                LOG.warn("Error converting  '" + value + "' to long", e);
                return Optional.empty();
            }
        }
        else {
            LOG.warn("Value '" + ObjectUtils.toString(value) +
                    "' could not be converted to long.");
            return Optional.empty();
        }
    }

    private Optional<Object> get(Map<String, Object> valueMap, String key) {
        return Optional.ofNullable(valueMap.get(key));
    }

    /**
     * Creates a new InstalledPackage object from package name and info
     * @param name name of the package
     * @param info package info from salt
     * @param server server this package will be added to
     * @return The InstalledPackage object
     */
    private InstalledPackage createPackageFromSalt(String name, Pkg.Info info,
                                                   Server server) {

        String epoch = info.getEpoch().orElse(null);
        String release = info.getRelease().orElse(null);
        String version = info.getVersion();
        PackageEvr evr = PackageEvrFactory
                .lookupOrCreatePackageEvr(epoch, version, release);

        PackageName pkgName = PackageFactory.lookupOrCreatePackageByName(name);

        String arch = info.getArchitecture();
        PackageArch packageArch = PackageFactory.lookupPackageArchByLabel(arch);

        InstalledPackage pkg = new InstalledPackage();
        pkg.setEvr(evr);
        pkg.setArch(packageArch);

        pkg.setInstallTime(Date.from(info.getInstallDate().toInstant()));
        pkg.setName(pkgName);
        pkg.setServer(server);
        return pkg;
    }
}
