# Client code for Update Agent
# Copyright (c) 1999-2002 Red Hat, Inc.  Distributed under GPL.
#
# Author: Preston Brown <pbrown@redhat.com>
#         Adrian Likins <alikins@redhat.com>
#
"""utility functions for up2date"""

import os
import string

import up2dateErrors
import rpm
import config


import gettext
_ = gettext.gettext


def _getOSVersionAndRelease():
    cfg = config.initUp2dateConfig()
    ts = rpm.TransactionSet()
    for h in ts.dbMatch('Providename', "redhat-release"):
        if cfg["versionOverride"]:
            version = cfg["versionOverride"]
        else:
            version = h['version']

        osVersionRelease = (h['name'], version, h['release'])
        ts.closeDB()
        return osVersionRelease
    else:
        # new SUSE always has a baseproduct link which point to the
        # product file of the first installed product (the OS)
        # all rpms containing a product must provide "product()"
        # search now for the package providing the base product
        baseproduct = '/etc/products.d/baseproduct'
        bp = os.path.abspath(os.path.join(os.path.dirname(baseproduct), os.readlink(baseproduct)))
        for h in ts.dbMatch('Providename', "product()"):
            if bp in h['filenames']:
                # zypper requires a exclusive lock on the rpmdb. So we need
                # to close it here.
                ts.closeDB()
                return (h['name'], h['version'], h['release'])
        else:
            # for older SUSE versions we need to search for distribution-release
            # package which also has /etc/SuSE-release file
            osVersionRelease = None
            for h in ts.dbMatch('Providename', "distribution-release"):
                osVersionRelease = (h['name'], h['version'], h['release'])
                if '/etc/SuSE-release' in h['filenames']:
                    break
            # zypper requires a exclusive lock on the rpmdb. So we need
            # to close it here.
            ts.closeDB()
            if osVersionRelease is None:
                raise up2dateErrors.RpmError(
                    "Could not determine what version of Red Hat Linux you "\
                    "are running.\nIf you get this error, try running \n\n"\
                    "\t\trpm --rebuilddb\n\n")
            return osVersionRelease

def getVersion():
    '''
    Returns the version of redhat-release rpm
    '''
    os_release, version, release = _getOSVersionAndRelease()
    return str(version)

def getOSRelease():
    '''
    Returns the name of the redhat-release rpm
    '''
    os_release, version, release = _getOSVersionAndRelease()
    return os_release

def getRelease():
    '''
    Returns the release of the redhat-release rpm
    '''
    os_release, version, release = _getOSVersionAndRelease()
    return release

def getArch():
    if not os.access("/etc/rpm/platform", os.R_OK):
        return os.uname()[4]

    fd = open("/etc/rpm/platform", "r")
    platform = string.strip(fd.read())

    #bz 216225
    #handle some replacements..
    replace = {"ia32e-redhat-linux": "x86_64-redhat-linux"}
    if replace.has_key(platform):
        platform = replace[platform]

    return platform


def version():
    # substituted to the real version by the Makefile at installation time.
    return "@VERSION@"


if __name__ == "__main__":
    print "Version: %s" % getVersion()
    print "OSRelease: %s" % getOSRelease()
    print "Release: %s" % getRelease()
    print "Arch: %s" % getArch()
