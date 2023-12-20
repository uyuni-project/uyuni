# pylint: disable=missing-module-docstring,invalid-name
# Abstraction for an XML importer with a disk base
#
# Copyright (c) 2008--2018 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#

import os
import gzip
from uyuni.common.fileutils import createPath
from uyuni.common.rhnLib import hash_object_id


class MissingXmlDiskSourceFileError(Exception):
    pass


class MissingXmlDiskSourceDirError(Exception):
    pass


class DiskSource:  #  pylint: disable=missing-class-docstring
    subdir = None
    # Allow for compressed files by default
    allow_compressed_files = 1

    def __init__(self, mountPoint):  #  pylint: disable=invalid-name
        self.mountPoint = mountPoint  #  pylint: disable=invalid-name

    # Returns a data stream
    def load(self):
        # Returns a stream
        filename = self._getFile()  #  pylint: disable=assignment-from-none
        return self._loadFile(filename)

    def _getFile(self, create=0):  #  pylint: disable=invalid-name
        # Virtual
        # pylint: disable=W0613,R0201
        return None

    def _loadFile(self, filename):  #  pylint: disable=invalid-name
        # Look for a gzip file first
        if self.allow_compressed_files:
            if filename[-3:] == ".gz" and os.path.exists(filename):
                return gzip.open(filename, "rb")

            if os.path.exists(filename + ".gz"):
                return gzip.open(filename + ".gz", "rb")

        if os.path.exists(filename):
            return open(filename, "r")  #  pylint: disable=unspecified-encoding

        raise MissingXmlDiskSourceFileError("unable to process file %s" % filename)  #  pylint: disable=consider-using-f-string

    def _getDir(self, create=0):  #  pylint: disable=invalid-name
        dirname = "%s/%s" % (self.mountPoint, self.subdir)  #  pylint: disable=consider-using-f-string
        if not create:
            return dirname
        if not os.path.exists(dirname):
            createPath(dirname)
        if not os.path.isdir(dirname):
            raise MissingXmlDiskSourceDirError("%s is not a directory" % dirname)  #  pylint: disable=consider-using-f-string
        return dirname


class ArchesDiskSource(DiskSource):
    subdir = "arches"
    filename = "arches.xml"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return os.path.join(dirname, self.filename)


class ArchesExtraDiskSource(ArchesDiskSource):
    filename = "arches-extra.xml"


class ProductnamesDiskSource(DiskSource):
    subdir = "product_names"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return "%s/product_names.xml" % dirname  #  pylint: disable=consider-using-f-string


class ChannelFamilyDiskSource(DiskSource):
    subdir = "channel_families"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return "%s/channel_families.xml" % dirname  #  pylint: disable=consider-using-f-string


class OrgsDiskSource(DiskSource):
    subdir = "orgs"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return "%s/orgs.xml" % dirname  #  pylint: disable=consider-using-f-string


class ChannelDiskSource(DiskSource):  #  pylint: disable=missing-class-docstring
    subdir = "channels"

    def __init__(self, mountPoint):
        DiskSource.__init__(self, mountPoint)
        self.channel = None

    def setChannel(self, channel):  #  pylint: disable=invalid-name
        self.channel = channel

    def list(self):
        # Lists the available channels
        dirname = self._getDir(create=0)
        if not os.path.isdir(dirname):
            # No channels available
            return []
        return os.listdir(dirname)

    def _getFile(self, create=0):
        dirname = "%s/%s" % (self._getDir(create), self.channel)  #  pylint: disable=consider-using-f-string
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return os.path.join(dirname, self._file_name())

    @staticmethod
    def _file_name():
        return "channel.xml"


class ChannelCompsDiskSource(ChannelDiskSource):
    @staticmethod
    def _file_name():
        return "comps.xml"


class ChannelModulesDiskSource(ChannelDiskSource):
    @staticmethod
    def _file_name():
        return "modules.yaml"


class ShortPackageDiskSource(DiskSource):  #  pylint: disable=missing-class-docstring
    subdir = "packages_short"

    def __init__(self, mountPoint):
        DiskSource.__init__(self, mountPoint)
        # Package ID
        self.id = None
        self._file_suffix = ".xml"

    def setID(self, pid):  #  pylint: disable=invalid-name
        self.id = pid

    # limited dict behaviour
    def has_key(self, pid):
        # Save the old id
        old_id = self.id
        self.id = pid
        f = self._getFile()
        # Restore the old id
        self.id = old_id
        if os.path.exists(f + ".gz") or os.path.exists(f):
            return 1
        return 0

    def _getFile(self, create=0):
        dirname = "%s/%s" % (self._getDir(create), self._hashID())  #  pylint: disable=consider-using-f-string
        # Create the directoru if we have to
        if create and not os.path.exists(dirname):
            createPath(dirname)
        return "%s/%s%s" % (dirname, self.id, self._file_suffix)  #  pylint: disable=consider-using-f-string

    def _hashID(self):  #  pylint: disable=invalid-name
        # Hashes the package name
        return hash_object_id(self.id, 2)


class PackageDiskSource(ShortPackageDiskSource):
    subdir = "packages"


class SourcePackageDiskSource(ShortPackageDiskSource):
    subdir = "source_packages"


class ErrataDiskSource(ShortPackageDiskSource):
    subdir = "errata"

    def _hashID(self):
        # Hashes the erratum name
        return hash_object_id(self.id, 1)


class BlacklistsDiskSource(DiskSource):
    subdir = "blacklists"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return "%s/blacklists.xml" % dirname  #  pylint: disable=consider-using-f-string


class BinaryRPMDiskSource(ShortPackageDiskSource):
    subdir = "rpms"

    def __init__(self, mountPoint):
        ShortPackageDiskSource.__init__(self, mountPoint)
        self._file_suffix = ".rpm"


class SourceRPMDiskSource(BinaryRPMDiskSource):
    subdir = "srpms"


class KickstartDataDiskSource(DiskSource):  #  pylint: disable=missing-class-docstring
    subdir = "kickstart_trees"

    def __init__(self, mountPoint):
        DiskSource.__init__(self, mountPoint)
        self.id = None

    def setID(self, ks_label):  #  pylint: disable=invalid-name
        self.id = ks_label

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return os.path.join(dirname, self.id) + ".xml"


class KickstartFileDiskSource(KickstartDataDiskSource):  #  pylint: disable=missing-class-docstring
    subdir = "kickstart_files"
    allow_compressed_files = 0

    def __init__(self, mountPoint):
        KickstartDataDiskSource.__init__(self, mountPoint)
        # the file's relative path
        self.relative_path = None

    def set_relative_path(self, relative_path):
        self.relative_path = relative_path

    def _getFile(self, create=0):
        path = os.path.join(self._getDir(create), self.id, self.relative_path)
        dirname = os.path.dirname(path)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return path


class MetadataDiskSource:  #  pylint: disable=missing-class-docstring
    def __init__(self, mountpoint):
        self.mountpoint = mountpoint

    @staticmethod
    def is_disk_loader():
        return True

    def getArchesXmlStream(self):  #  pylint: disable=invalid-name
        return ArchesDiskSource(self.mountpoint).load()

    def getArchesExtraXmlStream(self):  #  pylint: disable=invalid-name
        return ArchesExtraDiskSource(self.mountpoint).load()

    def getChannelFamilyXmlStream(self):  #  pylint: disable=invalid-name
        return ChannelFamilyDiskSource(self.mountpoint).load()

    def getOrgsXmlStream(self):  #  pylint: disable=invalid-name
        return OrgsDiskSource(self.mountpoint).load()

    def getProductNamesXmlStream(self):  #  pylint: disable=invalid-name
        return ProductnamesDiskSource(self.mountpoint).load()

    def getComps(self, label):  #  pylint: disable=invalid-name
        sourcer = ChannelCompsDiskSource(self.mountpoint)
        sourcer.setChannel(label)
        return sourcer.load()

    def getModules(self, label):  #  pylint: disable=invalid-name
        sourcer = ChannelModulesDiskSource(self.mountpoint)
        sourcer.setChannel(label)
        return sourcer.load()

    def getChannelXmlStream(self):  #  pylint: disable=invalid-name
        sourcer = ChannelDiskSource(self.mountpoint)
        channels = sourcer.list()
        stream_list = []
        for c in channels:
            sourcer.setChannel(c)
            stream_list.append(sourcer.load())
        return stream_list

    def getChannelShortPackagesXmlStream(self):  #  pylint: disable=invalid-name
        return ShortPackageDiskSource(self.mountpoint)

    def getPackageXmlStream(self):  #  pylint: disable=invalid-name
        return PackageDiskSource(self.mountpoint)

    def getSourcePackageXmlStream(self):  #  pylint: disable=invalid-name
        return SourcePackageDiskSource(self.mountpoint)

    def getKickstartsXmlStream(self):  #  pylint: disable=invalid-name
        return KickstartDataDiskSource(self.mountpoint)

    def getErrataXmlStream(self):  #  pylint: disable=invalid-name
        return ErrataDiskSource(self.mountpoint)

    def getSupportInformationXmlStream(self):  #  pylint: disable=invalid-name
        return SupportInformationDiskSource(self.mountpoint).load()

    def getSuseProductsXmlStream(self):  #  pylint: disable=invalid-name
        return SuseProductsDiskSource(self.mountpoint).load()

    def getSuseProductChannelsXmlStream(self):  #  pylint: disable=invalid-name
        return SuseProductChannelsDiskSource(self.mountpoint).load()

    def getSuseUpgradePathsXmlStream(self):  #  pylint: disable=invalid-name
        return SuseUpgradePathsDiskSource(self.mountpoint).load()

    def getSuseProductExtensionsXmlStream(self):  #  pylint: disable=invalid-name
        return SuseProductExtensionsDiskSource(self.mountpoint).load()

    def getSuseProductRepositoriesXmlStream(self):  #  pylint: disable=invalid-name
        return SuseProductRepositoriesDiskSource(self.mountpoint).load()

    def getSCCRepositoriesXmlStream(self):  #  pylint: disable=invalid-name
        return SCCRepositoriesDiskSource(self.mountpoint).load()

    def getSuseSubscriptionsXmlStream(self):  #  pylint: disable=invalid-name
        return SuseSubscriptionsDiskSource(self.mountpoint).load()

    def getClonedChannelsXmlStream(self):  #  pylint: disable=invalid-name
        return ClonedChannelsDiskSource(self.mountpoint).load()


class SupportInformationDiskSource(DiskSource):
    subdir = "support_info"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return "%s/support_info.xml" % dirname  #  pylint: disable=consider-using-f-string


class SuseProductsDiskSource(DiskSource):
    subdir = "suse_products"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return "%s/suse_products.xml" % dirname  #  pylint: disable=consider-using-f-string


class SuseProductChannelsDiskSource(DiskSource):
    subdir = "suse_products"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return "%s/suse_product_channels.xml" % dirname  #  pylint: disable=consider-using-f-string


class SuseUpgradePathsDiskSource(DiskSource):
    subdir = "suse_products"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return "%s/suse_upgrade_paths.xml" % dirname  #  pylint: disable=consider-using-f-string


class SuseProductExtensionsDiskSource(DiskSource):
    subdir = "suse_product_extensions"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return "%s/suse_product_extensions.xml" % dirname  #  pylint: disable=consider-using-f-string


class SuseProductRepositoriesDiskSource(DiskSource):
    subdir = "suse_product_repositories"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return "%s/suse_product_repositories.xml" % dirname  #  pylint: disable=consider-using-f-string


class SCCRepositoriesDiskSource(DiskSource):
    subdir = "scc_repositories"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return "%s/scc_repositories.xml" % dirname  #  pylint: disable=consider-using-f-string


class SuseSubscriptionsDiskSource(DiskSource):
    subdir = "suse_products"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return "%s/suse_subscriptions.xml" % dirname  #  pylint: disable=consider-using-f-string


class ClonedChannelsDiskSource(DiskSource):
    subdir = "suse_products"

    def _getFile(self, create=0):
        dirname = self._getDir(create)
        if create and not os.path.isdir(dirname):
            createPath(dirname)
        return "%s/cloned_channels.xml" % dirname  #  pylint: disable=consider-using-f-string


if __name__ == "__main__":
    # TEST CODE
    s = ChannelDiskSource("/tmp")
    print((s.list()))
    s.setChannel("redhat-linux-i386-7.2")
    print((s.load()))
