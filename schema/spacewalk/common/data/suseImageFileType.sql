--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

INSERT INTO suseImageFileType (extension, filetype, comment, enabled) VALUES
        ('.gce', 'image', 'Google Compute Engine image', TRUE),
        ('.qcow2', 'image', 'QEMU Copy-On-Write disk image', TRUE),
        ('.vmdk', 'image', 'Virtual Machine Disk format', TRUE),
        ('.ova', 'image', 'Open Virtual Appliance format', TRUE),
        ('.vmx', 'image', 'VMware configuration file', TRUE),
        ('.vhd', 'image', 'Virtual Hard Disk format', TRUE),
        ('.vhdx', 'image', 'Virtual Hard Disk v2 format', TRUE),
        ('.vhdfixed', 'image', 'Fixed Virtual Hard Disk format', TRUE),
        ('.vdi', 'image', 'VirtualBox Disk Image format', TRUE),
        ('.vagrant.libvirt.box', 'image', 'Vagrant box for libvirt', TRUE),
        ('.vagrant.virtualbox.box', 'image', 'Vagrant box for VirtualBox', TRUE),
        ('.gz', 'image', 'Gzip compressed file', TRUE),
        ('.bz', 'image', 'Bzip compressed file', TRUE),
        ('.xz', 'image', 'XZ compressed file', TRUE),
        ('.tar.xz', 'image', 'XZ compressed tarball', TRUE),
        ('.install.iso', 'image', 'Installable ISO 9660 CD-ROM disk image', TRUE),
        ('.iso', 'image', 'ISO 9660 CD-ROM disk image', TRUE),
        ('.raw', 'image', 'Raw disk image', FALSE),
        ('.squashfs', 'image', 'SquashFS file system image', TRUE),
        ('.kernel', 'kernel', 'PXE boot kernel', TRUE),
        ('.initrd', 'initrd', 'PXE boot initial ramdisk', TRUE),
        ('.spdx.json', 'sbom', 'SPDX Software Bill of Materials (SBOM)', TRUE)
ON CONFLICT (extension) WHERE org_id IS NULL DO NOTHING;

commit;
