# Client code for Update Agent
# Copyright (c) 1999--2017 Red Hat, Inc.  Distributed under GPLv2.
#
# Author: Preston Brown <pbrown@redhat.com>
#         Adrian Likins <alikins@redhat.com>
#
"""utility functions for up2date"""

import os
import gettext
from up2date_client import up2dateErrors
from up2date_client import up2dateLog
from up2date_client import config
from up2date_client.pkgplatform import getPlatform
from rhn.stringutils import sstr, bstr

t = gettext.translation('rhn-client-tools', fallback=True)
# Python 3 translations don't have a ugettext method
if not hasattr(t, 'ugettext'):
    t.ugettext = t.gettext
_ = t.ugettext

if getPlatform() == 'deb':
    import lsb_release
    def _getOSVersionAndRelease():
        dist_info = lsb_release.get_distro_information()
        os_name = dist_info['ID']
        os_version = 'n/a'
        if 'CODENAME' in dist_info:
            os_version = dist_info['CODENAME']
        os_release = dist_info['RELEASE']
        return os_name, os_version, os_release

else:
    from up2date_client import transaction
    def _getOSVersionAndRelease():
        osVersionRelease = None
        ts = transaction.initReadOnlyTransaction()
        for h in ts.dbMatch('Providename', "oraclelinux-release"):
            SYSRELVER = 'system-release(releasever)'
            version = sstr(h['version'])
            release = sstr(h['release'])
            if SYSRELVER in (sstr(provide) for provide in h['providename']):
                provides = dict((sstr(n), sstr(v))
                                for n,v in zip(h['providename'], h['provideversion']))
                release = '%s-%s' % (version, release)
                version = provides[SYSRELVER]
            osVersionRelease = (sstr(h['name']), version, release)
            return osVersionRelease
        else:
            for h in ts.dbMatch('Providename', "redhat-release"):
                SYSRELVER = 'system-release(releasever)'
                version = sstr(h['version'])
                release = sstr(h['release'])
                if SYSRELVER in (sstr(provide) for provide in h['providename']):
                    provides = dict((sstr(n), sstr(v))
                                    for n,v in zip(h['providename'], h['provideversion']))
                    release = '%s-%s' % (version, release)
                    version = provides[SYSRELVER]
                osVersionRelease = (sstr(h['name']), version, release)
                return osVersionRelease
            else:
                # new SUSE always has a baseproduct link which point to the
                # product file of the first installed product (the OS)
                # all rpms containing a product must provide "product()"
                # search now for the package providing the base product
                baseproduct = '/etc/products.d/baseproduct'
                if os.path.exists(baseproduct):
                    bp = os.path.abspath(os.path.join(os.path.dirname(baseproduct), os.readlink(baseproduct)))
                    for h in ts.dbMatch('Providename', "product()"):
                        if bstr(bp) in h['filenames']:
                            osVersionRelease = (sstr(h['name']), sstr(h['version']), sstr(h['release']))
                            # zypper requires a exclusive lock on the rpmdb. So we need
                            # to close it here.
                            ts.ts.closeDB()
                            return osVersionRelease
                else:
                    # for older SUSE versions we need to search for distribution-release
                    # package which also has /etc/SuSE-release file
                    for h in ts.dbMatch('Providename', "distribution-release"):
                        osVersionRelease = (sstr(h['name']), sstr(h['version']), sstr(h['release']))
                        if bstr('/etc/SuSE-release') in h['filenames']:
                            # zypper requires a exclusive lock on the rpmdb. So we need
                            # to close it here.
                            ts.ts.closeDB()
                            return osVersionRelease

                log = up2dateLog.initLog()
                log.log_me("Error: Could not determine what version of Linux you are running. "\
                           "Check if the product is installed correctly. Aborting.")
                raise up2dateErrors.RpmError(
                    "Could not determine what version of Linux you "\
                    "are running.\nIf you get this error, try running \n\n"\
                    "\t\trpm --rebuilddb\n\n")

def getVersion():
    '''
    Returns the version of redhat-release rpm
    '''
    cfg = config.initUp2dateConfig()
    if cfg["versionOverride"]:
        return str(cfg["versionOverride"])
    os_release, version, release = _getOSVersionAndRelease()
    return version

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
    if os.access("/etc/rpm/platform", os.R_OK):
        fd = open("/etc/rpm/platform", "r")
        platform = fd.read().strip()

        #bz 216225
        #handle some replacements..
        replace = {"ia32e-redhat-linux": "x86_64-redhat-linux"}
        if platform in replace:
            platform = replace[platform]
        return platform
    arch = os.uname()[4]
    if getPlatform() == 'deb':
        # On debian we only support i386
        if arch in ['i486', 'i586', 'i686']:
            arch = 'i386'
        if arch == 'x86_64':
            arch = 'amd64'
        arch += '-debian-linux'
    return arch


def getMachineId():
    '''
    Returns the SystemD or DBus machine-id
    '''
    def _file_to_string(path):
        if os.path.isfile(path) and os.access(path, os.R_OK):
            return open(path, "r").read().strip()

    # try first /etc/machine-id
    machineId = _file_to_string("/etc/machine-id")
    if not machineId:
        # fallback to dbus
        machineId = _file_to_string("/var/lib/dbus/machine-id")
    return machineId


def version():
    # substituted to the real version by the Makefile at installation time.
    return "@VERSION@"


if __name__ == "__main__":
    print("Version: %s" % getVersion())
    print("OSRelease: %s" % getOSRelease())
    print("Release: %s" % getRelease())
    print("Arch: %s" % getArch())
