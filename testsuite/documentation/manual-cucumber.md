# Manual Cucumber - List of Manual Tests

### Introduction

  * This is Suse Manager QA "Manual Cucumber" - a list of scenarios and instructions to be tested manually.
  * **These tests are to be done at least once in a product cycle, and some are to be done multiple times at release milestones.**
  * Some of these tests intentionally duplicate automated tests.
  * This is a *living document* and as such subject to change in line with the agile nature of the project.

### Background information
  * For a high level view of testing, please refer to the [Test Plan](https://gitlab.suse.de/galaxy/testplan).
  * For the automated test suite, please refer to the ["real" Cucumber](https://github.com/SUSE/spacewalk/tree/Manager/testsuite/features).

### Structure of this document
  * Manual Cucumber tests are split by the main component or functionality: server, clients, proxy, retail and other tests.
  * For each test scenario we specify its **priority**:
    * **all tests marked as 'high' must pass for the product to be released.**
  * The tests have information whether they should/could be automated.
  * The "smoke tests" must pass for the milestone to be validated
    * for more information and a list, please refer to test plan

### The priorities, with current count of tests per category, are:
  * high = mandatory (30 scenarios)
  * medium = important (39 scenarios)
  * low = not urgent (20 scenarios)



# Table of contents
1. [Server tests](#a)
1. [Client tests](#b)
1. [Proxy tests](#c)
1. [SUMA for Retail](#d)
1. [Virtual Host Managers](#e)
1. [Various - Basic](#f)
1. [Various - Advanced](#g)


# 1. Server Tests <a name="a"/>

### 1.1 Server (fresh installation, x86_64)
* Priority: high
* Could be automated: yes (has been done in openQA but we don't get the qcow2 from it automatically yet)
1. Install on a x86_64 host **without terraform**, like our customers:
    1. Prepare a fresh disk of at least 180 GB on a VM or a real host
    1. Install SLE15 SP4 Virtual Machine from an [ISO image](http://dist.suse.de/install/SLE-15-SP3-Full-TEST/) or from some other installation source ([SSC-Proxy only for testing purpose](https://github.com/SUSE/spacewalk/wiki/SCC-QA-proxy#installation-from-scratch-using-scc-proxy-40))
    1. At registration screen, register with your employee key (for betas use https://scc.suse.com/dashboard)
    1. When asked, answer you would like to install more repositories
    1. In extra software screen, un-filter the beta versions, and check SUSE Manager server latest version Beta
    1. Accept licence and enter beta key for SUSE Manager server latest version
    1. Accept default software selection (with X Window and GNOME)
    1. At partitioning screen, choose the default proposal
    1. Continue with installation screens
    1. After the system rebooted, make sure ``hostname -f`` gives the correct result
    1. Also make sure that SUSE Manager server software is installed with ``zypper search -t pattern``
    1. Set up the server with ``yast2 susemanager_setup``
    1. On credentials screen, enter "UC7" and the corresponding credential, and press "Test"
    1. Continue with [general server smoke tests here](#generalserversmoke).

### 1.2 Server (fresh installation, s390x)
* Priority: high
* Could be automated: yes (has been done in openQA but we don't get the qcow2 from it automatically yet)
1. Install on a s390x host with ``yast2 susemanager_setup``
1. Continue with [general server smoke tests here](#generalserversmoke).

### 1.3 Server (fresh installation, ppc64le)
* Priority: high
* Could be automated: yes (has been done in openQA but we don't get the qcow2 from it automatically yet)
1. Install on a ppc64le host with ``yast2 susemanager_setup``
1. Continue with [general server smoke tests here](#generalserversmoke).

### 1.4 Server Migration (from the older version to the latest version)
* Priority: high
* Could be automated: yes (basically what we do in MUs and later on 4.0->4.1)
1. Make sure both SLES and SUMA are registered with SCC.
1. Make sure spacewalk services are not running ``spacewalk-service stop``
1. Apply all patches for the old SUMA version by running ``zypper ref`` followed by ``zypper up``
1. Upgrade the database schema by running ``spacewalk-schema-upgrade``
1. Run to command that will perform migration ``zypper migration``
1. Select one of the options and let the migration process complete.
1. If necessary, upgrade the database schema again by running ``spacewalk-schema-upgrade``
1. Run the database migration script ``/usr/lib/susemanager/bin/pg-migrate-X-to-Y``, where
   X is the old version of the database and Y the new one. For example: 10 for
   SUSE Manager 4.0, 12 for SUSE Manager 4.1, 13 for SUSE Manager 4.2.
1. Start the spacewalk services ``spacewalk-service start``
1. Continue using the migrated SUMA.

#### Sequence of test steps
General server smoke tests are: <a name="generalserversmoke"/>
1. Create initial organization, from Web UI
1. Make sure the credentials are accepted, from Web UI
1. Synchronize x86_64 repositories, from Web UI or ``spacewalk-repo-sync``
1. Add custom client-side channels as needed (tools, salt, python...)
1. Depending on which client you want to test, see the corresponding entry in the server documentation [for example the one for sle clients](https://documentation.suse.com/multi-linux-manager/5.1/en/docs/client-configuration/clients-sle.html)
1. Synchronize the custom channels for BETA with ``spacewalk-repo-sync -c <name> -u <url>``
1. Perform package search
1. Create corresponding activation key
1. Add all children channels to activation key
1. Create bootstrap repository for clients (traditional or minions), with ``mgr-create-bootstrap-repo --with-custom-channels``
1. Do one of the client tests below


Add custom client-side channels:
- Go to Software > Channel list > Create a channel
- Inside the Channel create a Repository pointing to one of the custom repos above
- After that you go to Manage Channel, then follow Repositories tab, and inside tap on Sync.


# 2. Client Tests <a name="b"/>

### 2.1 Client Smoke Testing <a name="b1"/>

Client tests are listed by the platform.
Most tests are already automated but are redone manually for safety, or cannot be automated.
The tests that should be considered for future automation have been flagged in the relevant column.

___Platform___ | ___Client Type___ | ___Priority___ | ___Automation<br>candidate___ | ___Tests___
--- | --- | --- | --- | ---
**SLE 15 SP3** | Traditional | high | ... | [General client smoke tests](#generalclientsmoke)
... | Normal Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
... | SSH Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
**SLE 15 SP2** | Traditional | high | ... | [General client smoke tests](#generalclientsmoke)
... | Normal Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
... | SSH Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
**SLE 15 SP1 LTSS** | Traditional | high | ... | [General client smoke tests](#generalclientsmoke)
... | Normal Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
... | SSH Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
**SLE 15 LTSS** | Traditional | high | ... | [General client smoke tests](#generalclientsmoke)
... | Normal Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
... | SSH Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
**SLE 12 SP5** | Traditional | high | ... | [General client smoke tests](#generalclientsmoke)
... | Normal Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
... | SSH Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
... | SSH Salt minion tunnel | high | ... | [General client smoke tests](#generalclientsmoke)
**SLE 12 SP4** | Traditional | high | ... | [General client smoke tests](#generalclientsmoke)
... | Normal Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
... | SSH Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
... | SSH Salt minion tunnel | high | ... | [General client smoke tests](#generalclientsmoke)
 .. | Retail terminal | high | ... | [General client smoke tests](#generalclientsmoke)
**SLE 11 SP4** | Traditional | medium | ... | [General client smoke tests](#generalclientsmoke)
... | Normal Salt minion | medium | ... | [General client smoke tests](#generalclientsmoke)
... | Retail terminal | high | ... | [General client smoke tests](#generalclientsmoke)
**SLE 11 SP3** | Traditional | low | ... | [General client smoke tests](#generalclientsmoke)
... | Normal Salt minion | low | ... | [General client smoke tests](#generalclientsmoke)
**RES 8** | Traditional | high | ... | [General client smoke tests](#generalclientsmoke)
... | Normal Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
... | SSH Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
**RES 7** | Traditional | high | ... | [General client smoke tests](#generalclientsmoke)
... | Normal Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
... | SSH Salt minion | high | ... | [General client smoke tests](#generalclientsmoke)
 **CentOS 8** | Normal Salt minion | medium | ... | [General client smoke tests](#generalclientsmoke)
... | SSH Salt minion | medium | ... | [General client smoke tests](#generalclientsmoke)
 .. | SSH Salt minion tunnel | medium | ... | [General client smoke tests](#generalclientsmoke)
**osad** | Traditional | high | yes | [General client smoke tests](#generalclientsmoke)

#### Sequence of test steps
**General client smoke tests**: <a name="generalclientsmoke"/>
1. (traditional) Boostrap the client using the bootstrap script with the relevant activation key
    OR
    (salt minion) Bootstrap the minion from the UI
    OR
    (salt minion) Bootstrap the minion with salt script
    OR
    (salt-ssh) Still UI but with activation key with Push contact method

1. Install a package via Web UI
1. Install a patch via Web UI
1. Remove a package via Web UI
1. Execute a remote command via Web UI ([Tip for traditional](#remotecommandtraditional))
1. [Apply a configuration file via Web UI](#applyconfigfile) ([Tip for traditional](#remotecommandtraditional))
1. [Schedule Software package refresh](#updatepackagelist)
1. [Schedule Hardware refresh](#hardwardrefresh)
1. Reboot the client via Web UI

**SLES11SPx:** Before bootstrapping be sure to have changed that apache config in the server -> https://documentation.suse.com/external-tree/en-us/suma/4.0/suse-manager/client-configuration/tshoot-clients.html#_ssl_errors

-----------

The following smoke tests and the details on how to do them are done automatically [already here](https://github.com/SUSE/spacewalk/blob/Manager-4.1/testsuite/features/build_validation/smoke_tests/smoke_tests.template) and you can see further details in that template

### 2.2 Additional Client Tests <a name="b2"/>

#### 2.2.1 Client Migrations of Traditional Clients
___Migration FROM___ | ___Migration TO___ | ___Priority___ | ___Automation<br>candidate___ | ___Tests___
--- | --- | --- | --- | ---
Traditional SLES | Normal Salt minion | ... | ... | (automated)
... | Salt SSH minion | ... | ... | (automated)
 .. | SSH Salt minion tunnel | low | yes | [Migration steps](#migrationsteps)
Traditional SLES ES | Normal Salt minion | high | yes | [Migration steps](#migrationsteps)
... | Salt SSH minion | medium | yes | [Migration steps](#migrationsteps)
 .. | SSH Salt minion tunnel | low | yes | [Migration steps](#migrationsteps)

#### 2.2.2 Recycle a Deleted Traditional Client
* Priority: low
* Could be automated: yes
1. Register a traditional client
1. In the system details, unregister this client
1. Go to Systems => Bootstrap
1. In the form, enter its hostname, fill in the other fields and click on the button
1. Check that this machine is no more of "Management" type
1. Do usual [client smoke tests](#generalclientsmoke)

#### Sequence of test steps
Migration test steps tests: <a name="migrationsteps"/>
1. Register a traditional client
1. In  case of SLES ES clients make sure <code>rhnplugin</code> is disabled
1. Go to Systems => Bootstrap
1. In the form, enter its hostname, fill in the other fields (including SSH mode)
1. Use the right activation key
1. Click on the Bootstrap button and wait for the bootstrapping to finish
1. Check that this machine is no more of "Management" type
1. Do usual [client smoke tests](#generalclientsmoke)


# 3. Proxy Tests <a name="c"/>

### 3.1 Proxy - Basic Installation and Configuration
___Type___| ___Priority___ | ___Automation<br>candidate___ | ___Tests___
--- | --- | --- | ---
**Proxy - Traditional** | medium | yes | [Proxy steps](#proxysteps)
**Proxy - Salt** | high | ... | [Proxy steps](#proxysteps)

### 3.2 Proxy Migration (from the older version to the latest version)
* Priority: high
* Could be automated: no (manual in essence)
1. Synchronize Proxy child channel for the latest version
1. Migrate the older proxy with its latest patches the new version of proxy (more steps to be added)

### 3.3 Proxy Migration (old version with the new version's channels)
* Priority: high
* Could be automated: yes
1. Install old version server with or without terraform
1. Create initial organization, from Web UI
1. Make sure the credentials are accepted, from Web UI
1. Synchronize x86_64 repositories, from Web UI or ``spacewalk-repo-sync``
1. Synchronize also Proxy old version child channel
1. Go to "Channels => Manage Software Channels => Create Channel" and create a custom channel named "custom_tools"
1. Synchronize it with the beta URL: ``spacewalk-repo-sync -c custom_tools -u http://<user:password>@beta.suse.com/private/SUSE-Manager-beta/RC1/SLE12/x86_64/update``
1. Create an activation key
1. Add all children channels to activation key
1. Create bootstrap repository for clients (traditional or minions), with ``mgr-create-bootstrap-repo --with-custom-channels``
1. Create a bootstrap script for the proxy
1. Prepare another SLESmachine which will be the proxy
1. Bootstrap the proxy with the server as a traditional client
1. Copy SSL keys as explained in the documentation
1. Run ``configure-proxy.sh`` on the proxy
1. Bootstrap a traditional client attached to the proxy
1. Bootstrap a salt minion attached to the proxy

Then redo the entire procedure, but this time do not add the custom channel to the activation key.
Then, completely at the end:

1. Add the custom channel to the proxy with ``spacewalk-channel -a -c custom_tools``
1. Do a ``zypper refresh`` and a ``zypper update``
1. Restart the proxy services ``salt-broker``, ``apache2`` and ``squid``

and check whether everything continues working.

#### Sequence of test steps
[Proxy steps are](<a name="proxysteps"/>)
1. Synchronize Proxy child channel for the relevant version
1. Create activation key that includes Proxy channels
1. Bootstrap the required type of proxy (traditional or Salt)
1. Run configure-proxy.sh on the proxy (you may need to do this twice - keep an eye on the script output)
1. Make sure SSL keys as copied explained in the documentation
1. Bootstrap a traditional client attached to the proxy
1. Bootstrap a Salt minion attached to the proxy

### 3.4 Proxy Migration (from traditional to salt)
* Priority: medium
* Could be automated: no
1. Install and setup server
1. Install and setup traditional proxy
1. Install and bootstrap a salt minion client attached to the proxy
1. Install and setup a traditional client registered to the proxy
1. Reinstall the proxy and set it up as salt client
   Note it is the same hardware/virtual machine and you should use the same activation key.
1. Check in the web UI that you can check if there are new updates on both clients
1. Check in the web UI that you can run remote commands in the salt minion one.
1. Check in the web UI that the proxy is setup with salt management mode
1. Check in the web UI that the clients are still listed in the proxy

### 3.5 Proxy Migration by Reactivation (from traditional to salt)
* Priority: medium
* Could be automated: ?
1. Install and setup server
1. Install and setup traditional proxy of the previous major version
1. Install and bootstrap a traditional client registered to the proxy
1. Install and bootstrap a salt client registered to the proxy
1. Shutdown the proxy
1. Install and setup a salt proxy for the current version
1. Delete the new proxy from the system list
1. Create a reactivation key for the old proxy
1. Register the new proxy with the reactivation key
1. Run `proxy-configuration.sh` in the new proxy
1. Check in the web UI that you can check if there are new updates on both clients
1. Check in the web UI that you can run remote commands in the salt minion one.
1. Check in the web UI that the proxy is setup with salt management mode
1. Check in the web UI that the clients are still listed in the proxy

# 4. SUMA for Retail <a name="d"/>

### 4.1 SUMA for Retail - Basic workflow
* Priority: high
* Could be automated: to be confirmed
1. Synchronize necessary child child channels
1. Register Image Build Host and create images
1. Configure Branch server
1. Transfer the images from SUMA Server to the Branch server
1. Register new SLE12SP5 terminal
1. Register new SLE15SP1 terminal
1. Log in to the new terminals
1. [Execute general client smoke tests on the terminals](#generalclientsmoke)

#### 4.1.1 Registering retail terminal with UEFI boot enabled

1. Modify saltboot formula in web UI, so `gpt` is used as a partition table type.
1. Install package `qemu-ovmf-x86_64` before creation of libvirt based VM.
1. When package installation is finished, multiple UEFI firmwares are available as `/usr/share/qemu/ovmf-*`.
1. Select UEFI instead of BIOS in _Hypervisor details_ menu via `virt-manager`.
1. Firmware `/usr/share/qemu/ovmf-x86_64-ms-code.bin` is pre-selected by `virt-manager`. This cannot be changed via GUI, but only via `virsh edit` command.
1. VM's domain XML with configuration is very similar like in usual case using BIOS, important elements to check are:
```
  <sysinfo type='smbios'>
    <system>
      <entry name='manufacturer'>Intel</entry>
      <entry name='product'>Genuine</entry>
    </system>
  </sysinfo>

  <os>
    <type arch='x86_64' machine='pc-i440fx-2.9'>hvm</type>
    <!-- Key lines are below, edit carefully -->
    <loader readonly='yes' secure='no' type='pflash'>/usr/share/qemu/ovmf-x86_64-opensuse-code.bin</loader>
    <nvram>/var/lib/libvirt/qemu/nvram/suma-40-efitest_VARS.fd</nvram>
    <smbios mode='sysinfo'/>
  </os>

  <interface type='network'>
    <mac address='****'/>
    <source network='suma-40-private'/>
    <model type='virtio'/>
    <boot order='1'/>
    <address type='pci' domain='0x0000' bus='0x00' slot='0x03' function='0x0'/>
  </interface>
```
**Important note:** Option `secure='no'` doesn't turn off secure boot feature. It is necessary to do it via UEFI menu during VM's boot. To enter UEFI configuration menu, just type `exit` in previous UEFI shell command prompt. Then you can disable secure boot.

#### 4.1.2 Registering retail terminal with EFI HTTP booting
1. This feature is for SLE15SP1 (JeOS7) terminals only, please build kiwi image for it first.
1. Follow the same steps from 4.1.1 section, so everything in the VM for retail terminal is ready for EFI booting
1. Configure DHCP formula as usual and fill `Filename Http:` input field with `http://[branch-server-FQDN-for-private network]/saltboot/boot/shim.efi`
1. Ensure default initrd is set to SLE15SP1 image (JeOS7). Check this path on branch server: `/srv/saltboot/boot` - both symlinks (initrd.gz and link) should point to files containg JeOS7
1. Change default boot option via UEFI menu during VM's boot and set it to *UEFI HTTPv4*. To enter UEFI configuration menu, press ESC and then type `exit` in UEFI shell command prompt
1. Restart and boot the terminal. The rest should follow as usual.
1. If you jump into error message `No mapping found` during booting, try to disable secure boot (see instructions above in section 4.1.1)

**Note:** Apache access log at the branch server should contain lines related to HTTP booting starting with `"HEAD /saltboot/boot/grub.efi HTTP/1.1" 200 - "-" "UefiHttpBoot/1.0"`

#### 4.1.3 Configuration with Multiple Branch Servers
1. Follow the same steps from 4.1 section
1. Create multiple groups named as branch prefixes according to the number of Branch servers
1. Prepare also multiple private virtual networks, one private network per Branch server
1. If you plan to use JeOS image as a base for 3.2 Branch server, assure the content of `/etc/zypp/zypp.conf` contains line `rpm.install.excludedocs = no`
1. This has to be done **before you install proxy pattern** on the branch server
1. Aftern proxy pattern is installed run `configure-proxy.sh` script as usual
1. Then check if content of the file `/etc/rhn/rhn.conf` is not empty and contains line with `proxy.rhn_parent` set to the FQDN of the SUMA Server
1. When tests for the first branch server are finished (and terminals are successfully deployed), the easiest way to switch to the next Branch server is just to change private network
1. It is also suitable to recrate virtual HDD of terminal or restore it from the snapshot before terminals are rebooted

#### 4.1.4. USB stick for booting of retail terminal (instead of HTTP or PXE booting)
1. This feature is for SLE15SP1 (JeOS7) terminals only, please build kiwi image for it first
1. Follow the same steps from 4.1.1 section, this feature is available for both: EFI and legacy BIOS based terminals
1. Modify VM of branch server and add additional storage of size 512 MiB. Make sure it uses **USB bus**
1. Ensure default image is set to SLE15SP1 (JeOS7). You can do it by executing `salt-call pillar.item image-synchronize` at branch server, output should contain `POS_Image_Graphical7-7.0.0` or similar name
1. If default image is not set to JeOS7, do it in branch server profile at `Image Synchronization` formula. Then apply highstate
1. Prepare usb stick by executing `salt-call image_sync_usb.create /dev/sdX` at branch server assuming `/dev/sdX` points to the USB stisk you created in step 3
1. When this is successfully completed, please remove this storage from branch server's VM and add it to the VM for retail terminal
1. Ensure that **secure boot is disabled** in case of EFI usage, you can do it in EFI menu (just type exit and press ENTER at EFI prompt)
1. Set booting sequence so retail terminal boots from this USB stick device
1. Everything should follow like during usual retail terminal bootstrap (accept salt key etc.)

**NOTE:** If you encounter failure at "History > Events" page at profile of retail terminal stating `'Sum of partition sizes ({0}MiB) exceeds disk size ({1}MiB)'.format(size_known, disk_size - 1)) salt.exceptions.SaltException: Sum of partition sizes (1525MiB) exceeds disk size (511.0MiB)'` or similar, make sure saltboot formula points to the proper disk device. You use two devices now, so device you want for image deployment could be detected as `/dev/sdb`

#### 4.1.5 Force redeploy or repartition retail terminal
1. Deploy any retail terminal as usual
1. Run `salt "name-of-retail-terminal" state.apply saltboot.force_redeploy` from SUMA
1. Run `salt "name-of-retail-terminal" state.apply saltboot.force_repartition` as well
1. You should see these salt commands passed. (Terminal must be up and running.)
1. Change any partition size at saltboot formula for related system group to some different value
1. Reboot retail terminal via web UI
1. You should see kiwi image is being redeployed during next boot
1. You can check events page of this retail terminal. You should see new `Apply states [util.syncstates, saltboot]` event there
1. This event should contain states `saltboot_force_redeploy` and `saltboot_force_repartition`
1. You can also run `salt "name-of-retail-terminal" cmd-run lsbk` to see if new partitioning is used

#### 4.1.6 Boot and deployment of retail terminal via wifi connection
1. Real hardware is necessary for this test (retail terminal, wifi router or access point)
2. Setup SUSE Manager 4.1 and branch server localy at your workstation as a VMs using libvirt
3. Create second virtual network interface during branch server VM setup.
4. This interface has to be bridged to network interface at your workstation, which is expected to be connected to your router or access point (for example `Host device eth1: macvtap`)
5. Configure your wifi router / access point to be at regime with DHCP disabled as this is going to be provided via branch server
6. Configure wifi at this device to use WPA2 PSK, set credentials and save them for later use
7. Connect this device with your laptop as described at step 4.
8. Modify kiwi image profile `confix.xml` file for JeOS7 to include `dracut-wireless` package as follows:
```
	<package name="dracut-wireless"/>
```
9. We also have to provide credentials for wifi by creating `root/etc/sysconfig/network/ifcfg-wlan0` file in kiwi image profile:
```
# ALLOW_UPDATE_FROM_INITRD
WIRELESS_ESSID=<wireless network name>
WIRELESS_WPA_PSK=<wireless network password>
```
10. Build kiwi image with these changes and create USB stick as described in [4.1.4](https://gitlab.suse.de/galaxy/testplan/-/blob/master/manual-cucumber.md) section above
11. Boot terminal from USB stick you previously created and terminal boot should proceed as usual

### 4.2 SUMA for Retail - Image Upgrade
* Priority: high
* Could be automated: to be confirmed
1. Set up SUMA for Retail
1. Register terminal using the image for older version of SLE 12 (like SLE 12 SP3)
1. Apply highstate on build host to clear kiwi created cache. (If not, older cached packages may be used when kiwi image is build.)
1. Build a new image for deployment, using newer version of SLE 12 SP4
1. Assign the new image to the terminal by changing field `OS Image to Deploy` to the name of SLE 12 SP4 image at saltboot formula page on HWTYPE system group of this terminal
1. Restart the terminal
1. Confrm that the new SLE is running on the terminal

### 4.3 SUMA for Retail - Migration from SLEPOS
* Priority: high
* Could be automated: yes (all steps already done excepted the first 2 ones)
1. Get an existing XML files from Retail team. Those are currently [provided](https://gitlab.suse.de/SLEPOS/SUMA_Retail/tree/master/doc/migration/test_data). (Prepare two SLE15SP1 based VMs as a branch servers to be used for import.)
1. Check for duplicates in source XML file and also check for too long names (longer than 56 characters) of HWTYPE groups and correct if necessary. (Both is documented in official docs.)
1. Convert XML to yaml file with ``retail_migration`` command
1. Edit yaml file to match kiwi images to be used if it is not possible to use the exported ones
1. Prepare one (or more) branch servers accordingly to generated yaml file
1. Apply that configuration file with ``retail_yaml`` command
1. Check that the branch server is configured accordingly to yaml file
1. Check that the the empty terminal profiles are created accordingly to yaml file
1. Prepare VM for terminal and modify it (via `virsh edit` command) to match MAC address and HWTYPE group of one of imported terminals (please see YAML file or DHCP formula on branch server)
1. Register for real one of these terminals
1. Proceed with POS terminal health-check after registration

# 5. Virtual Host Managers <a name="e"/>

### 5.1 VMware-based Virtual Host Manager
* Priority: medium
* Could be automated: no (as it would mean nested virtualization)
1. Install a VMware ESXI server
1. Enable SSH remote access by pressing F2 in its text console and choosing "Troubleshooting Options"
1. Copy its certification authority to the server with: `scp f50.suse.de:/etc/vmware/ssl/castore.pem /etc/pki/trust/anchors/`
1. Run `update-ca-certificates`
1. Register it as a virtual hosts manager with "Systems => Virtual Hosts Managers => Add VMware-based Virtual Host Manager"
   (port "443", user "root")
1. Press "Refresh Data" button
1. Start a VM on that machine
1. Check that both the VMware host and the VM appear in "Systems => System List => Virtual Systems"
1. Go to "Admin => Task Schedules => gatherer-matcher-default" and schedule a report
1. Check the report in /var/lib/spacewalk/subscription-matcher (CSV format)

### 5.2 File-based Virtual Host Manager
* Priority: low
* Could be automated: yes, in `srv_virtual_host_manager.feature` we only miss the checks explained below
1. On the SUSE Manager server, create a file inside `/srv/www/htdocs/pub/`
1. In this file, put the example JSON data from https://documentation.suse.com/suma/4.2/en/suse-manager/client-configuration/vhm-file.html (the one with "examplevhost")
1. Go to "Systems => Virtual Hosts Manager => Create => File-based" and provide a label and the URL of that file, for example `https://<server_fqdn>/pub/test.json`; validate
1. Press "Refresh Data" button.
1. Return to "Systems => Virtual Hosts Manager", follow the newly created link, and check the new hosts appeared
1. Check that the virtual host manager, the two virtualization hosts it is supposed to control, and the six VMs on those two hosts appear in "Systems => System List => Virtual Systems"
1. Also check in "Systems => Visualization" (dropped in 5.0)

(such a virtual host manager currently cannot be used for subscription matching)

### 5.3 Amazon EC2-based Virtual Host Manager
* Priority: medium
* Could be automated: no
1. Create instance of SLES in AWS cloud with public domain name
1. Install `virtual-host-gatherer-libcloud` package on SUMA server and restart spacewalk services
1. Prepare new virtual host manager via Web UI at "Systems => Virtual Host Managers => Create => Amazon EC2"
1. Open this new virtual host manager and press "Refresh Data"
1. Check at "Nodes => &lt;name of manager&gt; => Virtualization => Guests" that you can see a list of available instances
1. Bootstrap the new client as a SSH minion with its public domain name
1. Check that both the Amazon host and the VM appear in "Systems => System List => Virtual Systems"
1. Also check in "Systems => Visualization" (dropped in 5.0)

### 5.4 Google Cloud Engine-based Virtual Host Manager
* Priority: medium
* Could be automated: no
1. Create instance of SLES in Google cloud with public domain name
1. Install `virtual-host-gatherer-libcloud` package on SUMA server and restart spacewalk services
1. Upload on the SUSE Manager server the key for qa-galaxy@ service account
1. Prepare new virtual host manager via Web UI at "Systems => Virtual Host Managers => Create => Google Cloud Engine"
1. Open this new virtual host manager and press "Refresh Data"
1. Check at "Nodes => &lt;name of manager&gt; => Virtualization => Guests" that you can see a list of available instances
1. Bootstrap the new client as a SSH minion with its public domain name
1. Check that both the Google cloud host and the VM appear in "Systems => System List => Virtual Systems"
1. Also check in "Systems => Visualization" (dropped in 5.0)

### 5.5 Microsoft Azure Engine-based Virtual Host Manager
* Priority: medium
* Could be automated: no
1. Create instance of SLES in Azure with public domain name
1. Install `virtual-host-gatherer-libcloud` package on SUMA server and restart spacewalk services
1. Make sure you know the secret key for registered application qa-galaxy
1. Prepare new virtual host manager via Web UI at "Systems => Virtual Host Managers => Create => Azure"
1. Open this new virtual host manager and press "Refresh Data"
1. Check at "Nodes => &lt;name of manager&gt; => Virtualization => Guests" that you can see a list of available instances
1. Bootstrap the new client as a SSH minion with its public domain name
1. Check that both the Google cloud host and the VM appear in "Systems => System List => Virtual Systems"
1. Also check in "Systems => Visualization" (dropped in 5.0)

### 5.6 Nutanix Virtual Host Manager
* Priority: medium
* Could be automated: no
1. Contact Steven Canova at SUSE Alliance team to discuss the certification at Nutanix.
1. Once we have Steven's green light, contact Raj Kumar Marimuthu at Nutanix to get the credentials
1. Create an instance of SLES and an instance of SUSE Manager in Nutanix AHV lab provided by Nutanix
   (see QA wiki for details)
1. Install `virtual-host-gatherer-nutanix` package on SUMA server and restart spacewalk services
1. Make sure you have installed Nutanix API certificate as explained in the wiki
1. Prepare new virtual host manager via Web UI at "Systems => Virtual Host Managers => Create => Nutanix AHV"
1. Open this new virtual host manager and press "Refresh Data"
1. Check at "Nodes => &lt;name of manager&gt; => Virtualization => Guests" that you can see a list of available instances
1. Bootstrap the new client as a SSH minion
1. Check that both the AHV nodes and the VM appear in "Systems => System List => Virtual Systems"
1. Also check in "Systems => Visualization" (dropped in 5.0)
1. Do the formal registration at Nutanix with Raj

# 6. Various - Basic <a name="f"/>

### 6.1 Documentation
* Priority: high
* Could be automated: no (needs human eyes)
1. Check that there are release notes
1. Check there is online documentation at SUSE web servers
1. Check that documentation can be accessed from web UI

### 6.2 System Overview
* Priority: high
* Could be automated: yes (already partially implemented: allcli_overview_systems_details.feature)
1. Go to "Details => Overview"
1. Check "Activation Key" field
1. Check "Lock Status" field
1. Check "System Types" field
1. Check "Notifications" field
1. Check "System Name" field

### 6.3 Your Preferences
* Priority: medium
* Could be automated: yes

TBD check "Home => Your preferences" page
Note: in automation, we already test some parts of this page, but not all

### 6.4 Visualization (dropped in 5.0)
* Priority: medium
* Could be automated: yes (but with two extra VMs)
1. Prepare a proxy
1. Register two minions throuh the proxy
1. Prepare a virtualization host
1. Register a traditional client VM on the virtualization host
1. Create a systems group
1. Put one minion and the traditional client in that group
1. Check that the proxy hierarchy view is correct
1. Check that the virtualization hierarchy view is correct
1. Check that the systems grouping view is correct
1. Try the filters
1. Try the grouping levels
1. Try the partitioning
1. Add systems to System Set Manager

### 6.5 System Set Manager
* Priority: medium
* Could be automated: yes (might be grouped with other existing SSM tests)
1. Select on minion and one traditional client in System Set Manager
1. Install a package
1. Verify that the package was installed on the target systems

### 6.6 Service Pack Migration (traditional client)
* Priority: medium
* Could be automated: yes (a check of the menus already exists: trad_sp_migration.feature)
1. Make sure all products and channels are fully synchronized
1. Bootstrap a SLE 12 SP1 traditional client
1. In the system overview page, go to Software => SP Migration
1. Press "Select Channels" button
1. On next page, if no warning about missing channels, press "Schedule Migration"
1. First try a dry run
1. Check that the dry run completes
1. Redo for real
1. Check in the system details that the migrated system is now SLE 12 SP3
1. Check on the system itself the repositories with ``zypper lr``

### 6.7 Package and System Locking (salt minions)
* Priority: medium
* Automated: yes
1. Lock a salt system (Formulas- Configuration-Pick System Lock- System Lock Tab- Lock system - Save Formula)
1. Reboot the system, it should be refused
1. Try to install a patch, it should be refused
1. Unlock the salt system
1. Lock a package (Systems -> Software -> Packages -> Lock)
1. Try to install a package, it should be refused


### 6.8 Email Address Change
* Priority: low
* Could be automated: yes
1. Change email address
1. Send automated mail
1. Check that it reaches the correct address

### 6.9 Use a System Group
* Priority: medium
* Could be automated: yes - issue #3998
1. Create a group in "Systems => System Groups"
1. Go to a system's details
1. In "Groups => Join", make it join the group
1. Verify the join worked in "Systems => Visualization => Systems Grouping" by clicking on "Show filters => Partitioning => Split into groups => Add a grouping level" and checking that the name of the group is visible.  (dropped in 5.0)
1. Go to the group's details
1. In "States" tab, apply a state to the group members
1. Verify the state got applied

# 7. Various - Advanced <a name="g"/>

### 7.1 Synchronization Between SUMA Servers
* Priority: medium
* Could be automated: no
1. Export + import with ``mgr-exporter`` and ``mgr-inter-sync``
1. Setup synchronization with a slave and a master server
1. List all repositories available from the ISS master: ``mgr-inter-sync -l``
1. Synchronize a channel: ``mgr-inter-sync -c CHANNEL``

### 7.2 Openscap (RHEL8 with Expanded Support in AWS)
* Priority: low
* Could be automated: yes
1. Login into AWS using your credentials: https://eu-central-1.console.aws.amazon.com/
1. Go to: https://github.com/SUSE-Enceladus/ec2imgutils
1. Install: zypper in python3-ec2imgutils
1. Setup EC2 credentials using ec2utils.conf.example from the repo above
1. Remember that your key-pair is inside EC2 > Key Pair menu in AWS dashboard.
1. Download latest SUMA 4.3 public cloud image.
1. Create a new AMI that will be replaced with the content of the image that we will upload.
1. In order to create an "empty" one, the easy way is to go to Images > AMIs and copy and paste one of the existing AMIs.
1. Upload the image:
ec2uploadimg --access-id 'xxxx' -s 'xxxx' --backing-store ssd --machine 'x86_64' --virt-type hvm --sriov-support --ena-support --verbose --regions 'eu-central-1' --ssh-key-pair 'obarrios' --private-key-file ~/.ssh/obarrios_aws.pem -d 'SUMA 4.3 xxx' --wait-count 3 -n 'susemanager-4-3-server-BYOS-build2.5' --type 't2.micro' --user 'ec2-user' -e 'ami-03dee5a4b23035973' 'SUSE-Manager-Server-BYOS.x86_64-4.3.0-EC2-HVM-xxx.raw.xz'
1. Follow these steps to create an instance using the newly uploaded image: https://github.com/SUSE/spacewalk/wiki/Testing-SUSE-Manager-in-Amazon-public-cloud#deploying-manually
1. After setup SUSE Manager
1. Synchronize this product with all sub-channels:
RHEL or SLES ES or CentOS 8 Base
1. Create custom repositories that contains RHEL8 repos: https://<your_server>/docs/en/suse-manager/client-configuration/clients-sleses.html#_optional_add_base_media_from_a_content_url you can find those repos in your rhel client. Alternatively, once the minion is bootstrap, enable again the disabled repos you have in your client, so you don't need to perform this step using custom repos.
1. To find proper Repo URL of the CDN go inside your client in AWS, you will have this info in:
1. Create Activation Keys for Salt and Salt-SSH Minion for both products
1. Go to EC2 Console and create another instance for:
AMI: 309956199498/RHEL_HA-8.4.0_HVM-20210504-x86_64-2-Hourly2-GP2
AMI: https://aws.amazon.com/marketplace/pp/prodview-puvcki5kgypyy?qid=1622539284199
1. Enable root access via SSH with a password https://github.com/SUSE/spacewalk/wiki/Testing-SUSE-Manager-in-Amazon-public-cloud#essential-operations-on-your-instance-after-connecting
1. Go to the documentation to bootstrap an Expanded Support client https://<your_server>/docs/en/suse-manager/client-configuration/clients-sleses.html
1. Bootstrap a RHEL 8 system as a salt minion
1. Install on it the openscap packages: ``yum install openscap-utils scap-security-guide openscap-content -y``
1. In the Web UI, go to system details, then "Audit => Schedule"
1. Enter "--profile standard" and "/usr/share/xml/scap/ssg/content/ssg-centos8-ds-1.2.xml" in the form and press "Schedule" button
1. Go see the results in "Audit => List Scans"

### 7.3 Live patching
* Priority: medium
* Could be automated: yes
1. Synchronize product Live Patching sub-products for your system
1. Create an activation key with Live Patching child channels activated
1. Boot a minion with this activation key
1. Make sure kGraft is installed on the minion
1. Apply salt highstate
1. In the Overview page for this system, check that "Live patch" is mentioned in "Kernel" row
1. Wait for a "Livepatching patch" for the Linux Kernel to be available or force it by downgrading your kernel version
   - Find the GM Kernel version, and find the package that corresponds to it
   - Install the package using SUMA
   - Reboot and during the boot select the old kernel
   - Remove the newer kernel for the system using SUMA
   - Apply a Package list Refresh
   - Check in the system details if the kernel and patch version are correct
1. Now you should see some critical patches available in the systems overview page
1. CAUTION: If you want to install a concrete patch version, keep in mind that the system by default will install the latest critical kernel patch available in your subscribez channels
   - In this case, you need to create a CLM filter using NEVRA, passing the Version an Release from you want to start denying
   - And then swap the channels and subscribe to the channel created by this CLM project
1. The kernel patches must now have the possibility to be installed without a reboot
1. On "Software => Patches" page, check that the "Reboot required" icon is absent
1. Apply one of this critical patches
1. Check with "kgr patches" command if your patch was applied

### 7.4 Unattended Bare Metal Provisioning
* Priority: medium
* Could be automated: no (too complicated, dangerous)
1. This functionality seems dangerous, test in a private, isolated, network
1. In this network, set up a DHCP server with a "next-server" configuration parameter pointing to the SUSE Manager server
1. Activate the functionality in "Admin => Manager configuration => Bare-metal systems"
1. Boot a machine on the same network
1. The machine should appear in the systems list and be powered off
1. Use the "Provisioning" tab to provision it after powering it on again
1. Then check system sanity with usual smoke tests

### 7.5 Content Lifecycle Management Filters
* Priority: low
* Could be automated: yes
1. Scenario: Deny filter
   - Create a "Deny" filter for a package contains name "ruby"
   - Check those packages containing ruby are not included in the channel
1. Scenario: Combine Deny and Allow filters
   - Create a "Deny" filter for a package containing name "ruby"
   - Create a "Allow" filter for a package containing name "ruby"
   - Check those packages containing ruby are included in the channel
1. Feedback page functionality
   - Create filter "enable module non-existing-module"
   - Create filters "postgres96: enable module postgresql:9.6 and "enable module postgresql:10"
   - Use first, second (or both) examples with filters in project and try to build it
   - Warnings or errors should be shown directly in the filters section

### 7.6 Subscription matching
* Priority: medium
* Could be automated: yes (but preferably not with EC2 instances)
1. Create instance of SLES in AWS cloud with public domain name
1. Install `virtual-host-gatherer-libcloud` package on SUMA server and restart spacewalk services
1. Prepare new virtual host manager via Web UI at "Systems => Virtual Host Managers => Create => Amazon EC2"
1. Open this new virtual host manager and press "Refresh Data"
1. Check at "Nodes => &lt;name of manager&gt; => Virtualization => Guests" that you can see a list of available instances
1. Bootstrap the new client as a SSH minion with its public domain name
1. Go to system details page of bootstrapped system and check that the UUID field is same string as AWS instance ID (starting with `i-0xxxxxxxxx`), apart from the missing dash
1. Go back to  "Systems => Virtual Host Managers" and follow the link to this manager
1. Go to "Nodes => &lt;name of manager&gt; => Virtualization => Guests" and check that the new system is seen as registered
1. Go to "Audit => Subscription Matching => Subscription" page and trigger refresh of matching data
1. Go to "Unmatched products" tab and check that AWS client is listed
1. Go to "Messages" tab and check there is no `Virtual guest has unknown host, assuming it is a physical system` message
1. Go to "Admin => Task Schedules => gatherer-matcher-default" and schedule a report
1. Check the report in /var/lib/spacewalk/subscription-matcher (CSV format)

### 7.7 Hub
* Priority: medium
* Automated: Yes
1. Do 3 SUSE Manager deployments (all 4.1 at least):
   * first deployment: 1 server (the hub server)
   * second deployment: 1 server (the first peripheral server), 1 proxy, 1 client, 1 minion
   * third deployment: 1 server (the second peripheral server), 1 proxy, 1 client, 1 minion
1. Create same organization on all 3 servers
1. Do reposync on all 3 servers
1. Create bootstrap repositories on all 3 servers
1. Register the 2 peripheral servers onto the hub server and register all clients on the peripheral servers
1. Install package `hub-xmlrpc-api` onto the hub server
1. Edit file `/etc/hub/hub-xmlrpc-api-config.json` to change the default URL
1. Restart the SUSE Manager services
1. Enable and start the `hub-xmlrpc-api` systemd service
1. Run the following script:
```
#! /usr/bin/ruby

require 'xmlrpc/client'

DEFAULT_TIMEOUT = 250
HUB_SERVER = 'localhost'.freeze
HUB_USER = 'admin'.freeze
HUB_PASSWORD = 'admin'.freeze

cnx = XMLRPC::Client.new2("http://#{HUB_SERVER}:2830/hub/rpc/api", nil, DEFAULT_TIMEOUT)
response = cnx.call('hub.loginWithAutoconnectMode', HUB_USER, HUB_PASSWORD)

sessionKey = response['SessionKey']

serverIds = cnx.call('hub.listServerIds', sessionKey)

systems = cnx.call('multicast.system.list_systems', sessionKey, serverIds)
puts
puts 'Succesful:'
systems['Successful']['Responses'].each do |resp|
  puts "Response: #{resp}"
end
puts
puts 'Failed:'
systems['Failed']['Responses'].each do |resp|
  puts "Error message: #{resp}"
end
cnx.call('auth.logout', sessionKey)
```
It should list all clients on all peripheral servers.

### 7.8 Salt-minion version backward-compatibility
* Priority: medium
* Could be automated: Yes
1. Having a minion bootstrap with an old salt and python version
1. Perform smoke tests
1. Identify possible compatibility issues

### 7.9 A roster is created for pending minions not yet in DB
* Priority: medium
* Automated: Yes
1. Prepara a VM for a SSH Minion
1. Using IP Tables cut access to the SUMA Server
1. Bootstrap a SSH Minion using the SUMA Proxy
1. Check if a roster file was created (ask MC)
1. Automated test included in https://github.com/uyuni-project/uyuni/pull/5384

### 7.10 Test SLE-Micro
* Priority: medium
* Only available as [tech preview](https://documentation.suse.com/suma/4.3/en/suse-manager/client-configuration/supported-features-sle-micro.html)
* Manual test for SLE-Micro 5.2: https://github.com/SUSE/spacewalk/issues/17785
* Could be automated: Yes
1. Deploy a VM using the latest 5.2 [ISO](https://download.suse.de/download/install/SLE-Micro-5.2-GM/SUSE-MicroOS-5.2-DVD-x86_64-GM-Media1.iso)
1. Install requirement for bootstrapping:
    - `transactional-update pkg install salt-transactional-update`
    - reboot (A reboot is mandatory to add the changes into the snapshot)
1. Synchronize SLE-Micro from the Product Wizard
1. Create an activation key
1. Onboard SLE-Micro as a Salt minion using this activation key
    - blocked atm due to https://github.com/SUSE/spacewalk/issues/17793 and https://github.com/SUSE/spacewalk/issues/16314
1. Check the onboarding event
1. Perform some basic smoke tests
1. To test OpenScap install:
    - `transactional-update pkg install openscap-utils`
    - reboot
1. Try also monitoring feature (Prometheus exporter formula)

### 7.11 Test `spacewalk-hostname-rename`
* Priority: low
* Could be automated: Yes but risky to include in the testsuite.
* Full procedure i described in https://documentation.suse.com/external-tree/en-us/suma/4.1/suse-manager/administration/tshoot-hostname-rename.html
1. Have a SUSE Manager server deployment.
1. Change the server's hostname in /etc/HOSTNAME .
1. Reboot the server - as simply opening a new session does not seem to be sufficient to apply the new hostname for the script.
1. Run `spacewalk-hostname-rename <ip-address>` where `<ip-address>` is the server's ip-address.
1. Follow the certificate creation steps by providing te information if and when prompted.
1. Check on the web UI that the certificate includes the new hostname.
