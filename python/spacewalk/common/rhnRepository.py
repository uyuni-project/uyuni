#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2008--2016 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SPDX-License-Identifier: GPL-2.0-only
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
#

import os
import stat

from rhn import rpclib

from uyuni.common import usix
from uyuni.common.rhnLib import rfc822time

# local imports
from spacewalk.common import rhnFlags
from spacewalk.common.rhnLog import log_debug
from spacewalk.common.rhnException import rhnException, rhnFault, rhnNotFound
from spacewalk.common.RPC_Base import RPC_Base

# bare-except and broad-except
# pylint: disable=W0702,W0703


class Repository(RPC_Base):
    """Shared repository class, inherited by both the proxy and server specific
    Repository classes.
    """

    def __init__(self, channelName=None):
        log_debug(2, channelName)
        RPC_Base.__init__(self)
        self.channelName = channelName
        # Default visible functions.
        self.functions = [
            "getPackage",
            "getPackageSource",
            "i18n",
            "content",
            "installation_xml",
            "media_1",
        ]

    def __del__(self):
        self.channelName = None
        self.functions = None

    def getPackagePath(self, pkgFilename, redirect=0):
        """Returns the path to a package.
        OVERLOAD this in server and proxy rhnRepository.
        I.e.: they construct the path differently.
        """
        # pylint: disable=R0201,W0613
        raise rhnException("This function should be overloaded.")

    @staticmethod
    def getPackagePathNVRA(_nvra):
        """OVERLOAD this in server and proxy rhnRepository.
        I.e.: they construct the path differently.
        """
        raise rhnException("This function should be overloaded.")

    def getSourcePackagePath(self, _pkgFilename):
        """Returns the path to a package.
        OVERLOAD this in server and proxy rhnRepository.
        I.e.: they construct the path differently.
        """
        # pylint: disable=R0201
        raise rhnException("This function should be overloaded.")

    def getPackage(self, pkgFilename, *args):
        """Get rpm package."""
        log_debug(3, pkgFilename)
        if args:
            pkg_spec = [pkgFilename] + list(args)
        else:
            pkg_spec = pkgFilename

        redirectsSupported = 0

        # If we are talking to a proxy, determine whether it's a version that
        # supports redirects.
        proxyVersionString = rhnFlags.get("x-rhn-proxy-version")
        if proxyVersionString:
            redirectsSupported = 1
        else:
            # Must be a client.  We'll determine the redirect capability via
            # the x-rhn-transport-capability header instead.
            transport_cap = rhnFlags.get("x-rhn-transport-capability")
            if transport_cap:
                transport_cap_list = transport_cap.split("=")
                redirectsSupported = (
                    transport_cap_list[0] == "follow-redirects"
                    and int(transport_cap_list[1]) >= 2
                )

        if redirectsSupported:
            log_debug(3, "Client supports redirects.")
            filePath = self.getPackagePath(pkg_spec, 1)
        else:
            # older clients just return the hosted url and download the package
            filePath = self.getPackagePath(pkg_spec)

        return self._getFile(filePath)

    @staticmethod
    def i18n(_translation, *_args):
        """Translations files for Ubuntu. E.g. Translation-en_US.bz2

        We do not support it so just return 404. But do not fail with
        traceback.
        """
        raise rhnNotFound()

    @staticmethod
    def content():
        """SUSE Content File

        We do not support it so just return 404. But do not fail with
        traceback.
        """
        raise rhnNotFound()

    @staticmethod
    def installation_xml():
        """SUSE File

        We do not support it so just return 404. But do not fail with
        traceback.
        """
        raise rhnNotFound()

    @staticmethod
    def media_1(filePath):
        """SUSE File

        We do not support it so just return 404. But do not fail with
        traceback.
        """
        log_debug(3, filePath)
        raise rhnNotFound()

    def getPackageSource(self, pkgFilename):
        """Get srpm packrge."""
        log_debug(3, pkgFilename)
        # Sanity check:
        l = pkgFilename.split(".")
        # 6/23/05 wregglej 154248, Don't mangle the filename if it's a nosrc package.
        if l[-2] != "nosrc":
            l[-2] = "src"
        pkgFilename = ".".join(l)
        filePath = self.getSourcePackagePath(pkgFilename)
        return self._getFile(filePath)

    # The real workhorse for all flavors of listall
    # It tries to pull data out of a file; if it doesn't work,
    # it calls the data producer with the specified params to generate the
    # data, which is also cached

    # --- PRIVATE METHODS ---

    def _getFile(self, filePath):
        """Returns xmlrpclib file object to any file given a path to it.
        IN:  filePath: path to any file.
        OUT: XMLed rpm or source rpm, or an xmlrpc file object.
        """
        log_debug(3, filePath)
        features = self._fileFeatures(filePath)
        filePath = features["path"]
        length = features["length"]
        lastModified = features["lastModified"]
        self._set_last_modified(lastModified)
        return rpclib.transports.File(open(filePath, "rb"), length, name=filePath)

    @staticmethod
    def _set_last_modified(last_modified, extra_headers=None):
        log_debug(4, last_modified)
        if not last_modified:
            return None
        # Set a field with the name of the header
        transport = rhnFlags.get("outputTransportOptions")
        if last_modified:
            # Put the last-modified info too
            if isinstance(last_modified, (usix.IntType, usix.FloatType)):
                last_modified = rfc822time(last_modified)
            transport["Last-Modified"] = last_modified
        if extra_headers:
            for k, v in list(extra_headers.items()):
                transport[str(k)] = str(v)
        return transport

    @staticmethod
    def _fileFeatures(filePath):
        """From a filepath, construct a dictionary of file features."""
        # pylint: disable=W0702
        log_debug(3, filePath)
        if not filePath:
            raise rhnFault(
                17,
                # pylint: disable-next=consider-using-f-string
                "While looking for file: `%s'" % os.path.basename(filePath),
            )
        try:
            s = os.stat(filePath)
        except:
            s = None
        if not s:
            l = 0
            lastModified = 0
        else:
            l = s[stat.ST_SIZE]
            lastModified = s[stat.ST_MTIME]
        del s

        # Build the result hash
        result = {}
        result["name"] = os.path.basename(filePath)
        result["length"] = l
        result["path"] = filePath
        if lastModified:
            result["lastModified"] = rfc822time(lastModified)
        else:
            result["lastModified"] = None
        return result
