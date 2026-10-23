--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

CREATE TABLE IF NOT EXISTS suseImageFileType
(
    id        INT GENERATED ALWAYS AS IDENTITY,
    org_id    NUMERIC
                CONSTRAINT suse_img_type_files_oid_fk
                  REFERENCES web_customer (id)
                  ON DELETE CASCADE,
    extension VARCHAR(64) NOT NULL,
    comment   TEXT,
    enabled   BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT suse_img_type_files_pk PRIMARY KEY (id),
    CONSTRAINT suse_img_type_files_oid_ext_uq UNIQUE (org_id, extension)
);

-- NULL org_id are the defaults of the instance, UNIQUE does not treat NULLs as equal
CREATE UNIQUE INDEX IF NOT EXISTS suse_img_type_files_default_ext_uq
    ON suseImageFileType (extension)
    WHERE org_id IS NULL;

INSERT INTO suseImageFileType (extension, comment) VALUES
        ('.gce', 'Google Compute Engine image'),
        ('.qcow2', 'QEMU Copy-On-Write disk image'),
        ('.vmdk', 'Virtual Machine Disk format'),
        ('.ova', 'Open Virtual Appliance format'),
        ('.vmx', 'VMware configuration file'),
        ('.vhd', 'Virtual Hard Disk format'),
        ('.vhdx', 'Virtual Hard Disk v2 format'),
        ('.vhdfixed', 'Fixed Virtual Hard Disk format'),
        ('.vdi', 'VirtualBox Disk Image format'),
        ('.vagrant.libvirt.box', 'Vagrant box for libvirt'),
        ('.vagrant.virtualbox.box', 'Vagrant box for VirtualBox'),
        ('.gz', 'Gzip compressed file'),
        ('.bz', 'Bzip compressed file'),
        ('.xz', 'XZ compressed file'),
        ('.tar.xz', 'XZ compressed tarball'),
        ('.install.iso', 'Installable ISO 9660 CD-ROM disk image'),
        ('.iso', 'ISO 9660 CD-ROM disk image'),
        ('.raw', 'Raw disk image'),
        ('.squashfs', 'SquashFS file system image'),
        ('.kernel', 'PXE boot kernel'),
        ('.initrd', 'PXE boot initial ramdisk')
ON CONFLICT (extension) WHERE org_id IS NULL DO NOTHING;
