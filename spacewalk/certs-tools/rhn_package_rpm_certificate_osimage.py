#!/usr/bin/python
#
# Author: Michele Bologna <michele.bologna@suse.com>
#

## language imports
from __future__ import print_function
import os
import sys
import glob
import pwd
import time
import shutil

import argparse

from certs.sslToolCli import CertExpTooShortException, \
        CertExpTooLongException, InvalidCountryCodeException
from certs.sslToolLib import RhnSslToolException, chdir
from spacewalk.common.fileutils import cleanupAbsPath, rhn_popen
from certs.rhn_ssl_tool import legacyTreeFixup, _disableRpmMacros, _reenableRpmMacros, \
    GenCaCertRpmException
from certs.sslToolConfig import DEFS, CERT_PATH, CA_CERT_RPM_SUMMARY, CA_CRT_NAME, CA_CRT_RPM_NAME
from rhn.i18n import bstr

CA_CERT_FULL_PATH_DEFAULT = "/root/ssl-build/" + CA_CRT_NAME

def processCommandline():
    parser = argparse.ArgumentParser(description='Generates RPM certificate for OS image building')
    parser.add_argument('--ca-cert-full-path',
        help='CA certificate filename (default: %s)' % CA_CERT_FULL_PATH_DEFAULT, default=CA_CERT_FULL_PATH_DEFAULT
        )

    args = parser.parse_args()
    if not os.path.isfile(args.ca_cert_full_path):
        print("File '" + args.ca_cert_full_path + "' cannot be opened or does not represent CA certificate. Exiting")
        sys.exit(-1)
    return args

def genCaRpm(options):
    OSIMAGE_RPM_CERTIFICATE_PATH = "/usr/share/susemanager/salt/images"
    CA_CERT_RPM_NAME_OSIMAGE = CA_CRT_RPM_NAME + "-osimage"
    OSIMAGE_RPM_REQUIRES = "ca-certificates"

    ca_cert_name = os.path.basename(options.ca_cert_full_path)
    ca_cert = options.ca_cert_full_path

    ca_cert_rpm_osimage = os.path.join(OSIMAGE_RPM_CERTIFICATE_PATH, CA_CERT_RPM_NAME_OSIMAGE)

    ver, rel = '1.0', '1'

    update_trust_script = os.path.join(CERT_PATH, 'update-ca-cert-trust.sh')

    args = (os.path.join(CERT_PATH, 'gen-rpm.sh') + " "
            "--name %s --version %s --release %s --packager %s --vendor %s "
            "--requires %s "
            "--group 'RHN/Security' --summary %s --description %s "
            "--post %s --postun %s "
            "/usr/share/rhn/%s=%s"
            % (repr(CA_CERT_RPM_NAME_OSIMAGE), ver, rel, None,
               None, OSIMAGE_RPM_REQUIRES,
               repr(CA_CERT_RPM_SUMMARY),
               repr(CA_CERT_RPM_SUMMARY),
               repr(update_trust_script), repr(update_trust_script),
               repr(ca_cert_name), repr(cleanupAbsPath(ca_cert))))
    clientRpmName = '%s-%s-%s' % (ca_cert_rpm_osimage, ver, rel)
    print("""
Generating CA public certificate RPM:
    %s.src.rpm
    %s.noarch.rpm""" % (clientRpmName, clientRpmName))

    _disableRpmMacros()
    cwd = chdir(OSIMAGE_RPM_CERTIFICATE_PATH)
    try:
        ret, out_stream, err_stream = rhn_popen(args)
    except Exception:
        chdir(cwd)
        _reenableRpmMacros()
        raise
    chdir(cwd)
    _reenableRpmMacros()

    out = out_stream.read(); out_stream.close()
    err = err_stream.read(); err_stream.close()

    if ret or not os.path.exists("%s.noarch.rpm" % clientRpmName):
        raise GenCaCertRpmException("CA public SSL certificate RPM generation "
                                "failed:\n%s\n%s" % (out, err))
    os.chmod('%s.noarch.rpm' % clientRpmName, int('0644',8))

    return '%s.noarch.rpm' % clientRpmName

def _main():
    """ main routine """

    options = processCommandline()
    genCaRpm(options)

def main():
    def writeError(e):
        sys.stderr.write(bstr('\nERROR: %s\n' % e))
    ret = 0
    try:
        ret = _main() or 0
    # CA key set errors
    except GenCaCertRpmException as e:
        writeError(e)
        ret = 12
    except RhnSslToolException as e:
        writeError(e)
        ret = 100

    return ret

#-------------------------------------------------------------------------------
if __name__ == "__main__":
    sys.stderr.write('\nWARNING: intended to be wrapped by another executable\n'
                     '           calling program.\n')
    sys.exit(abs(main() or errnoSuccess))
#===============================================================================
