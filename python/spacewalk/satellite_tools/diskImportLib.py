#  pylint: disable=missing-module-docstring,invalid-name
#
# Common dumper stuff
#
# Copyright (c) 2008--2017 Red Hat, Inc.
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
#

import os

from . import xmlSource
from uyuni.common.rhnLib import hash_object_id
from spacewalk.server.importlib.backendOracle import SQLBackend
from spacewalk.server.importlib.channelImport import ChannelImport, ChannelFamilyImport
from spacewalk.server.importlib.packageImport import PackageImport, SourcePackageImport
from spacewalk.server.importlib import archImport
from spacewalk.server.importlib import productNamesImport
from spacewalk.server.importlib import orgImport
from spacewalk.server.importlib import supportInformationImport, suseProductsImport


class Backend:
    __backend = None

    def __init__(self):
        pass

    def get_backend(self):
        if self.__backend:
            return self.__backend

        Backend.__backend = SQLBackend()
        return Backend.__backend


# get_backend() returns a shared instance of an Oracle backend


def get_backend():
    return Backend().get_backend()


# Functions for dumping packages


def rpmsPath(obj_id, mountPoint, sources=0):
    # returns the package path (for exporter/importer only)
    # not to be confused with where the package lands on the satellite itself.
    if not sources:
        template = "%s/rpms/%s/%s.rpm"
    else:
        template = "%s/srpms/%s/%s.rpm"
    return os.path.normpath(template % (mountPoint, hash_object_id(obj_id, 2), obj_id))


# pylint: disable=W0232
class diskImportLibContainer:

    """virtual class - redefines endContainerCallback"""

    # pylint: disable=E1101,E0203,W0201
    # this class has no __init__ for the purpose
    # it's used in multiple inheritance mode and inherited classes should
    # use __init__ from the other base class

    importer_class = object

    def endContainerCallback(self):
        importer = self.importer_class(self.batch, get_backend())
        importer.run()
        self.batch = []


# pylint: disable-next=missing-class-docstring
class OrgContainer(xmlSource.OrgContainer):
    importer_class = orgImport.OrgImport
    master_label = None
    create_orgs = False

    def __init__(self):
        xmlSource.OrgContainer.__init__(self)

    def set_master_and_create_org_args(self, master, create_orgs):
        self.master_label = master
        self.create_orgs = create_orgs

    def endContainerCallback(self):
        importer = self.importer_class(
            self.batch, get_backend(), self.master_label, self.create_orgs
        )
        importer.run()
        self.batch = []


class ProductNamesContainer(diskImportLibContainer, xmlSource.ProductNamesContainer):
    importer_class = productNamesImport.ProductNamesImport

    def endContainerCallback(self):
        if not self.batch:
            return
        diskImportLibContainer.endContainerCallback(self)


class ChannelArchContainer(diskImportLibContainer, xmlSource.ChannelArchContainer):
    importer_class = archImport.ChannelArchImport


class PackageArchContainer(diskImportLibContainer, xmlSource.PackageArchContainer):
    importer_class = archImport.PackageArchImport


class ServerArchContainer(diskImportLibContainer, xmlSource.ServerArchContainer):
    importer_class = archImport.ServerArchImport


class CPUArchContainer(diskImportLibContainer, xmlSource.CPUArchContainer):
    importer_class = archImport.CPUArchImport


class ServerPackageArchCompatContainer(
    diskImportLibContainer, xmlSource.ServerPackageArchCompatContainer
):
    importer_class = archImport.ServerPackageArchCompatImport


class ServerChannelArchCompatContainer(
    diskImportLibContainer, xmlSource.ServerChannelArchCompatContainer
):
    importer_class = archImport.ServerChannelArchCompatImport


class ChannelPackageArchCompatContainer(
    diskImportLibContainer, xmlSource.ChannelPackageArchCompatContainer
):
    importer_class = archImport.ChannelPackageArchCompatImport


class ServerGroupServerArchCompatContainer(
    diskImportLibContainer, xmlSource.ServerGroupServerArchCompatContainer
):
    importer_class = archImport.ServerGroupServerArchCompatImport


class ChannelFamilyContainer(diskImportLibContainer, xmlSource.ChannelFamilyContainer):
    importer_class = ChannelFamilyImport


class ChannelContainer(diskImportLibContainer, xmlSource.ChannelContainer):
    importer_class = ChannelImport


class PackageContainer(diskImportLibContainer, xmlSource.PackageContainer):
    importer_class = PackageImport


class SourcePackageContainer(diskImportLibContainer, xmlSource.SourcePackageContainer):
    importer_class = SourcePackageImport


class SupportInformationContainer(
    diskImportLibContainer, xmlSource.SupportInformationContainer
):
    importer_class = supportInformationImport.SupportInformationImport

    def endContainerCallback(self):
        if not self.batch:
            return
        diskImportLibContainer.endContainerCallback(self)


class SuseProductsContainer(diskImportLibContainer, xmlSource.SuseProductsContainer):
    importer_class = suseProductsImport.SuseProductsImport

    def endContainerCallback(self):
        if not self.batch:
            return
        diskImportLibContainer.endContainerCallback(self)


class SuseProductChannelsContainer(
    diskImportLibContainer, xmlSource.SuseProductChannelsContainer
):
    importer_class = suseProductsImport.SuseProductChannelsImport

    def endContainerCallback(self):
        if not self.batch:
            return
        diskImportLibContainer.endContainerCallback(self)


class SuseUpgradePathsContainer(
    diskImportLibContainer, xmlSource.SuseUpgradePathsContainer
):
    importer_class = suseProductsImport.SuseUpgradePathsImport

    def endContainerCallback(self):
        if not self.batch:
            return
        diskImportLibContainer.endContainerCallback(self)


class SuseProductExtensionsContainer(
    diskImportLibContainer, xmlSource.SuseProductExtensionsContainer
):
    importer_class = suseProductsImport.SuseProductExtensionsImport

    def endContainerCallback(self):
        if not self.batch:
            return
        diskImportLibContainer.endContainerCallback(self)


class SuseProductRepositoriesContainer(
    diskImportLibContainer, xmlSource.SuseProductRepositoriesContainer
):
    importer_class = suseProductsImport.SuseProductRepositoriesImport

    def endContainerCallback(self):
        if not self.batch:
            return
        diskImportLibContainer.endContainerCallback(self)


class SCCRepositoriesContainer(
    diskImportLibContainer, xmlSource.SCCRepositoriesContainer
):
    importer_class = suseProductsImport.SCCRepositoriesImport

    def endContainerCallback(self):
        if not self.batch:
            return
        diskImportLibContainer.endContainerCallback(self)


class SuseSubscriptionsContainer(
    diskImportLibContainer, xmlSource.SuseSubscriptionsContainer
):
    importer_class = suseProductsImport.SuseSubscriptionsImport

    def endContainerCallback(self):
        if not self.batch:
            return
        diskImportLibContainer.endContainerCallback(self)


class ClonedChannelsContainer(
    diskImportLibContainer, xmlSource.ClonedChannelsContainer
):
    importer_class = suseProductsImport.ClonedChannelsImport

    def endContainerCallback(self):
        if not self.batch:
            return
        diskImportLibContainer.endContainerCallback(self)
