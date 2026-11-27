<!--
SPDX-FileCopyrightText: 2026 SUSE LLC

SPDX-License-Identifier: Apache-2.0
-->

# Testing Cobbler

## Build Cobbler from source

Use a disposable VM. `make install` can overwrite an existing Cobbler
configuration. If you already installed Cobbler using `zypper` or an RPM,
skip the source build and go to [Set up standalone Cobbler](#set-up-standalone-cobbler).

This walkthrough records a Cobbler 3.4.0 development snapshot on openSUSE
Leap 15.3 and Tumbleweed in 2022. The package list and service setup are
historical, not instructions for provisioning a current server. For another
version, use the matching
[upstream source installation guide](https://cobbler.readthedocs.io/en/latest/installation-guide.html#id2).

Install the prerequisites and check out the snapshot:

```bash
sudo zypper install \
  python3 python3-cheetah python3-netaddr python3-simplejson python3-librepo \
  python3-devel python3-pip python3-setuptools python3-wheel python3-distro \
  python3-coverage python3-ldap python3-Sphinx python3-dnspython \
  python3-PyYAML python3-schema python3-magic \
  apache2 apache2-devel apache2-mod_wsgi-python3 rsync fence-agents genders \
  xorriso tftp supervisor ipmitool acl grub2-x86_64-efi

git clone https://github.com/cobbler/cobbler.git
cd cobbler
git checkout --detach 7324b1918848d2bcc6c126a40defa7e3a56c75ce
pip3 install -r docs/requirements.rtd.txt
pip3 install django file-magic ldap3 pymongo tornado dnspython mod_wsgi
sudo make install
```

Find the daemon and install its service file:

```bash
command -v cobblerd
sudo cp /etc/cobbler/cobblerd.service /etc/systemd/system/
```

The daemon was at `/usr/bin/cobblerd` in the original run. If it is installed
elsewhere, open the unit with `sudoedit /etc/systemd/system/cobblerd.service`
and set `ExecStart` to the actual path. Then start the service and check it:

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now cobblerd.service
systemctl is-active cobblerd.service
sudo cobbler version
```

Expected service state and example version output (the build time will differ):

```text
active
Cobbler 3.4.0
  source: 7324b191, Fri Mar 11 17:29:17 2022 +0100
  build time: Mon Mar 14 14:15:51 2022
```

## Set up standalone Cobbler

The [Cobbler quickstart](https://cobbler.readthedocs.io/en/latest/quickstart-guide.html)
explains `cobbler check` and the initial configuration. If you enable
Cobbler-managed DHCP, use an isolated network without another DHCP server.
The following example imports the Leap 15.3 DVD into the historical test
instance. For other Cobbler releases, choose a distribution supported by that
release and use the distro and profile names reported by the import.

```bash
iso=openSUSE-Leap-15.3-DVD-x86_64-Current.iso
wget "https://download.opensuse.org/distribution/leap/15.3/iso/$iso"
sudo mkdir -p /mnt/cobbler-iso
sudo mount -o loop,ro "$iso" /mnt/cobbler-iso
sudo cobbler import --name=leap153 --path=/mnt/cobbler-iso
sudo umount /mnt/cobbler-iso
sudo cobbler distro list
sudo cobbler profile list
```

Example import output, with intermediate log lines omitted:

```console
Importing distro leap153
task started: 2022-03-15_110644_import
task started (id=Media import, time=Tue Mar 15 11:06:44 2022)
...
Writing template files for leap153-x86_64
Writing template files for leap153-x86_64
*** TASK COMPLETE ***
$ sudo cobbler distro list
   leap153-x86_64
$ sudo cobbler profile list
   leap153-x86_64
```

Create a system from the imported profile. For PXE testing, generate the boot
loaders and sync the configuration:

```bash
sudo cobbler system add --name=testbed --profile=leap153-x86_64
sudo cobbler system list
sudo cobbler mkloaders
sudo cobbler sync
sudo cobbler report
```

Example system and boot loader output:

```console
$ sudo cobbler system list
   testbed
$ sudo cobbler mkloaders
task started: 2022-03-15_111605_mkloaders
task started (id=Create bootable bootloader images, time=Tue Mar 15 11:16:05 2022)
...
Successfully built bootloader for arch "x86_64-efi"!
*** TASK COMPLETE ***
```

`cobbler report` shows the distro, profile, and system settings.

## Build and inspect an installation ISO

Use the distro name printed by `cobbler distro list`. The historical Cobbler
snapshot uses `--tempdir`; consult the
[current ISO reference](https://cobbler.readthedocs.io/en/latest/user-guide/building-isos.html)
for newer versions, which document `--buildisodir` instead.

```bash
sudo mkdir -p /var/cache/cobbler/buildiso
sudo cobbler buildiso --tempdir=/var/cache/cobbler/buildiso \
  --iso="$HOME/autoinst.iso" --distro=leap153-x86_64
```

The original openSUSE walkthrough used `parti` to inspect the generated
image:

```bash
sudo zypper install parti
parti "$HOME/autoinst.iso"
```

<details>
<summary>Example partition and boot catalog output from <code>parti</code></summary>

```text
autoinst.iso: 137363456 bytes
- - - - - - - - - - - - - - - -
file system:
  fs "iso9660", label "COBBLER_INSTALL", uuid "2022-03-15-12-26-29-00"
- - - - - - - - - - - - - - - -
mbr id: 0x28aece75
  sector size: 512
  isolinux: 2656, "/isolinux/isolinux.bin"
  mbr partition table (chs 16/255/63, inconsistent geo):
  1  * 0 - 268287 (size 268288), chs 0/0/1 - 130/63/32
       type 0x00 (empty)
       fs "iso9660", label "COBBLER_INSTALL", uuid "2022-03-15-12-26-29-00"
  2    136 - 2655 (size 2520), chs 1023/254/63 - 1023/254/63
       type 0xef (efi)
- - - - - - - - - - - - - - - -
gpt (primary) guid: a4c6cccb-6c0c-475d-8a00-4be716872961
  sector size: 512
  revision: 1.0
  header: size 92, crc 0x900ef692 - ok
  position: current 1, backup 268287
  usable area: 64 - 268224 (size 268161)
  partition table: 2 - 63 (size 62, crc 0x05243025 - ok), entries 248, entry_size 128
  1    0 - 268223 (size 268224)
       type ebd0a0a2-b9e5-4433-87c0-68b6b72699c7 (windows data), attributes 0x1000000000000001 (system)
       guid a4c6cccb-6c0c-475d-8a01-4be716872961
       name[9] "ISOHybrid"
       fs "iso9660", label "COBBLER_INSTALL", uuid "2022-03-15-12-26-29-00"
  2    136 - 2655 (size 2520)
       type ebd0a0a2-b9e5-4433-87c0-68b6b72699c7 (windows data), attributes 0x1000000000000001 (system)
       guid a4c6cccb-6c0c-475d-8a02-4be716872961
       name[10] "ISOHybrid1"
- - - - - - - - - - - - - - - -
gpt (backup) guid: a4c6cccb-6c0c-475d-8a00-4be716872961
  sector size: 512
  revision: 1.0
  header: size 92, crc 0x5544767f - ok
  position: current 268287, backup 1
  usable area: 64 - 268224 (size 268161)
  partition table: 268225 - 268286 (size 62, crc 0x05243025 - ok), entries 248, entry_size 128
  1    0 - 268223 (size 268224)
       type ebd0a0a2-b9e5-4433-87c0-68b6b72699c7 (windows data), attributes 0x1000000000000001 (system)
       guid a4c6cccb-6c0c-475d-8a01-4be716872961
       name[9] "ISOHybrid"
       fs "iso9660", label "COBBLER_INSTALL", uuid "2022-03-15-12-26-29-00"
  2    136 - 2655 (size 2520)
       type ebd0a0a2-b9e5-4433-87c0-68b6b72699c7 (windows data), attributes 0x1000000000000001 (system)
       guid a4c6cccb-6c0c-475d-8a02-4be716872961
       name[10] "ISOHybrid1"
- - - - - - - - - - - - - - - -
el torito:
  sector size: 512
  boot catalog: 132
  0    type 0x01 (validation entry)
       platform id 0x00, crc 0x55aa (ok), magic ok
       manufacturer[0] ""
  1  * type 0x88 (initial/default entry)
       boot type 0 (no emulation)
       load address 0x00000, system type 0x00
       start 2656, size 4, "/isolinux/isolinux.bin"
       selection criteria 0x00 ""
  2    type 0x91 (last section header)
       platform id 0xef, name[0] ""
       entries 1
  3  * type 0x88 (initial/default entry)
       boot type 0 (no emulation)
       load address 0x00000, system type 0x00
       start 136, size 2520, "/grub/grub.efi"
       selection criteria 0x00 ""
```

</details>

If `parti` is unavailable or you only need to see the ISO's boot entries, use
`xorriso`:

```bash
xorriso -indev "$HOME/autoinst.iso" -report_el_torito plain
```

Look for the BIOS and UEFI entries in the report. An entry confirms that the
boot image is listed in the catalog, not that it boots. To check that, boot
the ISO in a VM. For the product-specific workflow, see
[Build an ISO with Cobbler](https://documentation.suse.com/multi-linux-manager/5.2/en/docs/client-configuration/autoinst-cdrom.html#build-iso-with-cobbler).

## Run the Uyuni integration tests

For integration testing, use the Cobbler instance in the Uyuni test deployment,
not the standalone instance. Set up a
[sumaform test environment](https://github.com/uyuni-project/sumaform/blob/master/README_TESTING.md)
and complete the prerequisite core tests before running the secondary tests.

On the test controller, run this from the `testsuite` directory:

```bash
TAGS=@scope_cobbler rake cucumber:secondary
```

The tagged scenarios cover [Cobbler sync in the web UI](../features/secondary/srv_cobbler_sync.feature),
autoinstallation distributions and profiles,
[ISO generation](../features/secondary/srv_cobbler_buildiso.feature), and
[PXE boot through a containerized proxy](../features/secondary/proxy_container_cobbler_pxeboot.feature).
For the proxy PXE scenario, the environment also needs a containerized server,
a proxy, a build host, a private network, and a
[PXE boot host](https://github.com/uyuni-project/sumaform/blob/master/README_ADVANCED.md#pxe-boot-hosts).
A tag filter does not provision these components.
