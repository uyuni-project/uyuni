#
# Copyright (c) 2008--2015 Red Hat, Inc.
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
# rhn-ssl-tool openssl.cnf style file manipulation class
#

## FIXME: the logic here is *WAY* too complicated. Need to simplify -taw

## language imports
from __future__ import print_function
import os
import sys
import copy
import time
import socket

## local imports
from spacewalk.common.fileutils import cleanupNormPath, rotateFile, rhn_popen, cleanupAbsPath
from certs.sslToolLib import getMachineName, daysTil18Jan2038, incSerial, fixSerial
from rhn.i18n import sstr

# defaults where we can see them (NOTE: directory is figured at write time)
CERT_PATH = '/usr/share/rhn/certs/'
BUILD_DIR = cleanupNormPath('./ssl-build', dotYN=1)
HOSTNAME = socket.gethostname()
MACHINENAME = getMachineName(HOSTNAME)

CA_KEY_NAME = 'RHN-ORG-PRIVATE-SSL-KEY'
CA_CRT_NAME = 'RHN-ORG-TRUSTED-SSL-CERT'
CA_CRT_RPM_NAME = CA_CRT_NAME.lower()

BASE_SERVER_RPM_NAME = 'rhn-org-httpd-ssl-key-pair'
BASE_SERVER_TAR_NAME = 'rhn-org-httpd-ssl-archive'

LEGACY_CA_KEY_NAME = 'ca.key'
LEGACY_CA_CRT_NAME = 'RHNS-CORP-CA-CERT'
LEGACY_SERVER_RPM_NAME1 = 'rhns-ssl-cert'
LEGACY_SERVER_RPM_NAME2 = 'rhn-httpd-ssl-key-pair'
LEGACY_CA_CERT_RPM_NAME = 'rhns-ca-cert'

CA_OPENSSL_CNF_NAME = 'rhn-ca-openssl.cnf'
SERVER_OPENSSL_CNF_NAME = 'rhn-server-openssl.cnf'

MD = 'sha256'
CRYPTO = '-des3'


def getOption(options, opt):
    """ fetch the value of an options object item
        without blowing up upon obvious errors
    """
    assert opt.find('-') == -1
    if not options:
        return None
    if opt in options.__dict__:
        #print 'XXX opt, options.__dict__[opt]', opt, options.__dict__[opt]
        return options.__dict__[opt]
    else:
        return None

def setOption(options, opt, value):
    """ set the value of an options object item
        without blowing up upon obvious errors
    """
    if not options:
        return
    if opt in options.__dict__:
        options.__dict__[opt] = value


def getStartDate_aWeekAgo():
    """ for SSL cert/key generation, returns now, minus 1 week
        just in case weird time zone issues get in the way of a working
        cert/key.

        format: YYMMDDHHMMSSZ where Z is the capital letter Z
    """
    aweek = 24*60*60*7
    return time.strftime("%y%m%d%H%M%S", time.gmtime(time.time()-aweek)) + 'Z'


_defs = \
    {
        '--dir'             : BUILD_DIR,
        '--ca-key'          : 'RHN-ORG-PRIVATE-SSL-KEY',
        '--ca-cert'         : 'RHN-ORG-TRUSTED-SSL-CERT',
        '--cert-expiration' : int(daysTil18Jan2038()),
        '--startdate'       : getStartDate_aWeekAgo(),

        '--server-key'      : 'server.key',
        '--server-cert-req' : 'server.csr',
        '--server-cert'     : 'server.crt',

        '--jabberd-ssl-cert': 'server.pem',

        '--set-country'     : 'US',
        '--set-common-name' : "",       # these two will never appear
        '--set-hostname'    : HOSTNAME, # at the same time on the CLI

        '--ca-cert-rpm'     : CA_CRT_RPM_NAME,
        '--server-rpm'      : BASE_SERVER_RPM_NAME+'-'+MACHINENAME,
        '--server-tar'      : BASE_SERVER_TAR_NAME+'-'+MACHINENAME,
        '--rpm-packager'    : None,
        '--rpm-vendor'      : None,
    }

_defsCa = copy.copy(_defs)
_defsCa.update(
    {
        '--set-state'       : '',
        '--set-city'        : '',
        '--set-org'         : '',
        '--set-org-unit'    : '',
        '--set-email'       : '',
    })


_defsServer = copy.copy(_defs)
_defsServer.update(
    {
        '--set-state'       : 'North Carolina',
        '--set-city'        : 'Raleigh',
        '--set-org'         : 'Example Corp. Inc.',
        '--set-org-unit'    : 'unit',
        '--set-email'       : 'admin@example.com',
    })

DEFS = _defsServer


def reInitDEFS(caYN=0):
    global DEFS
    if caYN:
        DEFS.update(_defsCa)
    else:
        DEFS.update(_defsServer)


def figureDEFS_dirs(options):
    """ figure out the directory defaults (after options being at least parsed
        once).
    """

    global DEFS
    ## fix up the --dir setting
    DEFS['--dir'] = getOption(options, 'dir') or DEFS['--dir'] or '.'
    DEFS['--dir'] = cleanupNormPath(DEFS['--dir'], dotYN=1)

    ## fix up the --set-hostname and MACHINENAME settings
    DEFS['--set-hostname'] = getOption(options, 'set_hostname') \
                               or DEFS['--set-hostname'] \
                               or socket.gethostname()

    global MACHINENAME
    MACHINENAME = getMachineName(DEFS['--set-hostname'])

    ## remap to options object
    setOption(options, 'dir', DEFS['--dir'])
    setOption(options, 'set_hostname', DEFS['--set-hostname'])


def figureDEFS_CA(options):
    """ figure out the defaults (after options being at least parsed once) for
        the CA key-pair(set) variables.
    """

    global DEFS
    if not getOption(options, 'ca_key'):
        # the various default names for CA keys (a hierarchy)
        for possibility in (CA_KEY_NAME, 'ca.key', 'cakey.pem'):
            if os.path.exists(os.path.join(DEFS['--dir'], possibility)):
                DEFS['--ca-key'] = possibility
                break

    DEFS['--ca-key'] = os.path.basename(getOption(options, 'ca_key') or DEFS['--ca-key'])
    DEFS['--ca-cert'] = os.path.basename(getOption(options, 'ca_cert') or DEFS['--ca-cert'])

    # the various default names for CA keys and certs
    if not getOption(options, 'ca_cert'):
        if DEFS['--ca-key'] == CA_KEY_NAME:
            DEFS['--ca-cert'] = CA_CRT_NAME
        elif DEFS['--ca-key'] == 'ca.key':
            DEFS['--ca-cert'] = 'ca.crt'
        elif DEFS['--ca-key'] == 'cakey.pem':
            DEFS['--ca-cert'] = 'cacert.pem'
        else:
            DEFS['--ca-cert'] = 'ca.crt'

    DEFS['--cert-expiration'] = getOption(options, 'cert_expiration') \
                                  or int(daysTil18Jan2038())
    DEFS['--ca-cert-rpm'] = getOption(options, 'ca_cert_rpm') \
                              or CA_CRT_RPM_NAME

    DEFS['--rpm-packager'] = getOption(options, 'rpm_packager')
    DEFS['--rpm-vendor'] = getOption(options, 'rpm_vendor')

    if '--cert-expiration' in DEFS:
        # nothing under 1 day or over # days til 18Jan2038
        if DEFS['--cert-expiration'] < 1:
            DEFS['--cert-expiration'] = 1
        _maxdays = int(daysTil18Jan2038()) # already rounded
        if DEFS['--cert-expiration'] > _maxdays:
            DEFS['--cert-expiration'] = _maxdays

    # remap to options object
    setOption(options, 'ca_key', DEFS['--ca-key'])
    setOption(options, 'ca_cert', DEFS['--ca-cert'])
    setOption(options, 'cert_expiration', DEFS['--cert-expiration'])
    setOption(options, 'ca_cert_rpm', DEFS['--ca-cert-rpm'])


def figureDEFS_server(options):
    """ figure out the defaults (after options being at least parsed once) for
        the server key-pair(set) variables.
    """

    global DEFS
    DEFS['--server-key'] = os.path.basename(getOption(options, 'server_key') \
                             or DEFS['--server-key'] or 'server.key')
    DEFS['--server-cert-req'] = \
      os.path.basename(getOption(options, 'server_cert_req') \
        or DEFS['--server-cert-req'] or 'server.csr')
    DEFS['--server-cert'] = os.path.basename(getOption(options, 'server_cert')\
                              or DEFS['--server-cert'] or 'server.crt')
    DEFS['--cert-expiration'] = getOption(options, 'cert_expiration') \
                                  or int(daysTil18Jan2038()) # already rounded
    DEFS['--server-rpm'] = getOption(options, 'server_rpm') \
                             or BASE_SERVER_RPM_NAME+'-'+MACHINENAME
    DEFS['--server-tar'] = getOption(options, 'server_tar') \
                             or BASE_SERVER_TAR_NAME+'-'+MACHINENAME

    DEFS['--rpm-packager'] = getOption(options, 'rpm_packager')
    DEFS['--rpm-vendor'] = getOption(options, 'rpm_vendor')

    if '--cert-expiration' in DEFS:
        # nothing under 1 day or over # days til 18Jan2038
        if DEFS['--cert-expiration'] < 1:
            DEFS['--cert-expiration'] = 1
        _maxdays = int(daysTil18Jan2038()) # already rounded
        if DEFS['--cert-expiration'] > _maxdays:
            DEFS['--cert-expiration'] = _maxdays

    # remap to options object
    setOption(options, 'server_key', DEFS['--server-key'])
    setOption(options, 'server_cert_req', DEFS['--server-cert-req'])
    setOption(options, 'server_cert', DEFS['--server-cert'])
    setOption(options, 'cert_expiration', DEFS['--cert-expiration'])
    setOption(options, 'server_rpm', DEFS['--server-rpm'])
    setOption(options, 'server_tar', DEFS['--server-tar'])


def figureDEFS_distinguishing(options):
    """ figure out the defaults (after options being at least parsed once) for
        the distinguishing variables (C, ST, L, O, OU, CN, emailAddress)
        First from config file, then from commanline.
    """

    global DEFS
    #if options:
    #    print 'XXX options.__dict__.keys()', options.__dict__.keys()
    #print 'XXX figureDEFS_distinguishing()'

    ## map the config file settings to the DEFS object
    conf = {}
    caYN = '--gen-ca-cert' in sys.argv or '--gen-ca' in sys.argv
    if caYN:
        conf = ConfigFile(os.path.join(DEFS['--dir'], CA_OPENSSL_CNF_NAME)).parse()
    else:
        conf = ConfigFile(os.path.join(DEFS['--dir'], MACHINENAME, SERVER_OPENSSL_CNF_NAME)).parse()

    mapping = {
            'C'            : ('--set-country',),
            'ST'           : ('--set-state',),
            'L'            : ('--set-city',),
            'O'            : ('--set-org',),
            'OU'           : ('--set-org-unit',),
            'CN'           : ('--set-common-name', '--set-hostname'),
            #'CN'           : ('--set-common-name',),
            'emailAddress' : ('--set-email',),
              }

    # map config file settings to DEFS (see mapping dict above)
    for key in conf.keys():
        #print 'XXX KEY', key, repr(mapping[key])
        for v in mapping[key]:
            DEFS[v] = conf[key]
            #print 'XXX DEFS["%s"]' % v, '=', conf[key]

    ## map commanline options to the DEFS object
    if getOption(options, 'set_country') is not None:
        DEFS['--set-country'] = getOption(options, 'set_country')
    if getOption(options, 'set_state') is not None:
        DEFS['--set-state'] = getOption(options, 'set_state')
    if getOption(options, 'set_city') is not None:
        DEFS['--set-city'] = getOption(options, 'set_city')
    if getOption(options, 'set_org') is not None:
        DEFS['--set-org'] = getOption(options, 'set_org')
    if getOption(options, 'set_org_unit') is not None:
        DEFS['--set-org-unit'] = getOption(options, 'set_org_unit')
    if getOption(options, 'set_common_name') is not None:
        DEFS['--set-common-name'] = getOption(options, 'set_common_name')
    if getOption(options, 'set_hostname') is not None:
        DEFS['--set-hostname'] = getOption(options, 'set_hostname')
    if getOption(options, 'set_email') is not None:
        DEFS['--set-email'] = getOption(options, 'set_email')
    DEFS['--set-cname'] = getOption(options, 'set_cname') # this is list

    # remap to options object
    setOption(options, 'set_country', DEFS['--set-country'])
    setOption(options, 'set_state', DEFS['--set-state'])
    setOption(options, 'set_city', DEFS['--set-city'])
    setOption(options, 'set_org', DEFS['--set-org'])
    setOption(options, 'set_org_unit', DEFS['--set-org-unit'])
    setOption(options, 'set_common_name', DEFS['--set-common-name'])
    #setOption(options, 'set_hostname', DEFS['--set-hostname'])
    setOption(options, 'set_email', DEFS['--set-email'])
    setOption(options, 'set_cname', DEFS['--set-cname'])


CONF_TEMPLATE_CA = """\
# rhn-ca-openssl.cnf
#---------------------------------------------------------------------------
# RHN Management {Satellite,Proxy} autogenerated openSSL configuration file.
#---------------------------------------------------------------------------

[ ca ]
default_ca              = CA_default

[ CA_default ]
default_bits            = 2048
x509_extensions         = ca_x509_extensions
dir                     = %s
database                = $dir/index.txt
serial                  = $dir/serial

# how closely we follow policy
policy                  = policy_optional
copy_extensions         = copy

[ policy_optional ]
countryName             = optional
stateOrProvinceName     = optional
organizationName        = optional
organizationalUnitName  = optional
commonName              = optional
emailAddress            = optional

#---------------------------------------------------------------------------

[ req ]
default_bits            = 2048
distinguished_name      = req_distinguished_name
prompt                  = no
x509_extensions         = req_ca_x509_extensions

[ req_distinguished_name ]
%s

[ req_ca_x509_extensions ]
basicConstraints = CA:true
keyUsage = digitalSignature, keyEncipherment, keyCertSign
extendedKeyUsage = serverAuth, clientAuth
# PKIX recommendations harmless if included in all certificates.
nsComment               = "RHN SSL Tool Generated Certificate"
subjectKeyIdentifier    = hash
authorityKeyIdentifier  = keyid, issuer:always

[ req_server_x509_extensions ]
basicConstraints = CA:false
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
nsCertType = server
# PKIX recommendations harmless if included in all certificates.
nsComment               = "RHN SSL Tool Generated Certificate"
subjectKeyIdentifier    = hash
authorityKeyIdentifier  = keyid, issuer:always
#===========================================================================
"""


CONF_TEMPLATE_SERVER = """\
# rhn-server-openssl.cnf
#---------------------------------------------------------------------------
# RHN Management {Satellite,Proxy} autogenerated openSSL configuration file.
#---------------------------------------------------------------------------
[ req ]
default_bits            = 2048
distinguished_name      = req_distinguished_name
prompt                  = no
x509_extensions         = req_server_x509_extensions
req_extensions          = v3_req

[ req_distinguished_name ]
%s

[ req_server_x509_extensions ]
basicConstraints = CA:false
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
nsCertType = server
# PKIX recommendations harmless if included in all certificates.
nsComment               = "RHN SSL Tool Generated Certificate"
subjectKeyIdentifier    = hash
authorityKeyIdentifier  = keyid, issuer:always

[ v3_req ]
# Extensions to add to a certificate request
basicConstraints = CA:FALSE
keyUsage = nonRepudiation, digitalSignature, keyEncipherment

# Some CAs do not yet support subjectAltName in CSRs.
# Instead the additional names are form entries on web
# pages where one requests the certificate...
subjectAltName          = @alt_names

[alt_names]
%s
#===========================================================================
"""


def gen_req_alt_names(d, hostname):
    """ generates the alt_names section of the *-openssl.cnf file """
    i = 0
    result = ''
    dnsname = [ hostname ]
    if '--set-cname' in d and d['--set-cname']:
        dnsname.extend(d['--set-cname'])
    for name in dnsname:
        i += 1
        result += "DNS.%d = %s\n" % (i, name)
    return result

def gen_req_distinguished_name(d):
    """ generates the rhn_distinguished section of the *-openssl.cnf file """

    s = ""
    keys = ('C', 'ST', 'L', 'O', 'OU', 'CN', 'emailAddress')
    for key in keys:
        if key in d and d[key].strip():
            s = s + key + (24-len(key))*' ' + '= %s\n' % d[key].strip()
        else:
            s = s + '#' + key + (24-len(key))*' ' + '= ""\n'

    return s


def figureSerial(caCertFilename, serialFilename, indexFilename):
    """ for our purposes we allow the same serial number for server certs
        BUT WE DO NOT ALLOW server certs and CA certs to share the same
        serial number.

        We blow away the index.txt file each time because we are less
        concerned with matching serials/signatures between server.crt's.
    """

    # what serial # is the ca cert using (we need to increment from that)
    ret, outstream, errstream = rhn_popen(['/usr/bin/openssl', 'x509', '-noout',
                                           '-serial', '-in', caCertFilename])
    out = sstr(outstream.read())
    outstream.close()
    errstream.read()
    errstream.close()
    assert not ret
    caSerial = out.strip().split('=')
    assert len(caSerial) > 1
    caSerial = caSerial[1]
    caSerial = eval('0x'+caSerial)

    # initialize the serial value (starting at whatever is in
    # serialFilename or 1)
    serial = 1
    if os.path.exists(serialFilename):
        serial = open(serialFilename, 'r').read().strip()
        if serial:
            serial = eval('0x'+serial)
        else:
            serial = 1

    # make sure it is at least 1 more than the CA's serial code always
    # REMEMBER: openssl will incremented the serial number each time
    # as well.
    if serial <= caSerial:
        serial = incSerial(hex(caSerial))
        serial = eval('0x' + serial)
    serial = fixSerial(hex(serial))

    # create the serial file if it doesn't exist
    # write the digits to this file
    open(serialFilename, 'w').write(serial+'\n')
    os.chmod(serialFilename, int('0600',8))

    # truncate the index.txt file. Less likely to have unneccessary clashes.
    open(indexFilename, 'w')
    os.chmod(indexFilename, int('0600',8))
    return serial


class ConfigFile:
    def __init__(self, filename=None):
        self.filename = filename
        if self.filename is None:
            self.filename = SERVER_OPENSSL_CNF_NAME
            if os.path.exists(os.path.join(DEFS['--dir'], 'rhn_openssl.cnf')):
                self.filename = os.path.join(DEFS['--dir'], "rhn_openssl.cnf")
            elif os.path.exists(os.path.join(DEFS['--dir'], 'openssl.cnf')):
                self.filename = os.path.join(DEFS['--dir'], "openssl.cnf")
        self.filename = cleanupAbsPath(self.filename)

    def parse(self):
        """ yank all the pertinent ssl data from a previously
            generated openssl.cnf.

            NOTE: we get a limited sampling of info here. We have no concept
            of the [ some heading ] divisions in the rhn_openssl.cnf file.
        """

        d = {}

        try:
            fo = open(self.filename, 'r')
        except:
            return d

        line = fo.readline()
        while line:
            if line.strip() == '[ req_distinguished_name ]':
                break
            line = fo.readline()

        #genKeys = ['dir']
        #caKeys = ['private_key', 'certificate',]
        keys = ['C', 'ST', 'L', 'O', 'OU', 'CN',
                'emailAddress',
               ]
        #       ] + caKeys + genKeys

        for s in fo.readlines():
            s = s.strip()
            if len(s) > 2 and s[0]=='[' and s[-1]==']':
                break
            split = s.split()
            if not split or len(split) < 3:
                continue
            if split[0] not in keys:
                continue
            split = s.split('=')
            if len(split) != 2:
                continue
            for i in range(len(split)):
                split[i] = split[i].strip()
            d[split[0]] = split[1]

        return d

    def updateLegacy(self, newdir=None, verbosity=1):
        """ in slightly older formatted ca_openssl.cnf files, there
            was no dir setting seperate from the database and serial
            settings. This function fixes that setup.

            Most of the time this function short-circuits early.
        """

        try:
            fo = open(self.filename, 'r')
        except:
            return

        if newdir is None:
            newdir = os.path.dirname(self.filename)

        newfile = ""
        in_CA_defaultYN = 0
        dirSetYN = 0

        line = fo.readline()
        while line:
            cleanLine = line.strip()

            # is this a label?
            isLabelYN = 0
            if cleanLine \
              and (cleanLine[0], cleanLine[-1]) == ('[',']'):
                isLabelYN = 1

            if cleanLine == '[ CA_default ]':
                # we don't care much until we hit this label
                in_CA_defaultYN = 1
            elif isLabelYN:
                in_CA_defaultYN = 0 # hit another label

            if in_CA_defaultYN:
                vector = line.split('=')
                if len(vector) == 2:
                    key = vector[0].strip()
                    if key == 'dir':
                        # we should be OK - short-circuit
                        return
                    if key in ('database', 'serial'):
                        # we never hit a "dir" key
                        if not dirSetYN:
                            newfile = newfile + """\
dir                     = %s
database                = $dir/index.txt
serial                  = $dir/serial
""" % newdir
                            dirSetYN = 1
                        line = fo.readline()
                        continue

            newfile = newfile + line
            line = fo.readline()

        try:
            rotated = rotateFile(filepath=self.filename, verbosity=verbosity)
            if verbosity>=0 and rotated:
                print("Rotated: %s --> %s" % (os.path.basename(self.filename),
                                              os.path.basename(rotated)))
        except ValueError:
            pass
        fo = open(self.filename, 'w')
        fo.write(newfile)
        fo.close()
        os.chmod(self.filename, int('0600',8))

        return dirSetYN

    def updateDir(self, newdir=None, verbosity=0):
        """ changes the CA configuration file's directory setting (if need be)
            in place. Touches nothing else.
        """

        if self.updateLegacy(newdir):
            return

        try:
            fo = open(self.filename, 'r')
        except:
            return

        olddir = ''
        if newdir is None:
            newdir = os.path.dirname(self.filename)

        newfile = ""
        hit_CA_defaultYN = 0

        line = fo.readline()
        while line:
            if line.strip() == '[ CA_default ]':
                # we don't care much until we hit this label
                hit_CA_defaultYN = 1
            if hit_CA_defaultYN:
                vector = line.split('=')
                if len(vector) == 2:
                    key, value = vector
                    if key.strip() == 'dir':
                        value = value.strip()
                        olddir = value
                        line = '%s= %s\n' % (key, newdir)
                        hit_CA_defaultYN = 0
                        if newdir == olddir:
                            # nothing to do
                            return
            newfile = newfile + line
            line = fo.readline()

        try:
            rotated = rotateFile(filepath=self.filename, verbosity=verbosity)
            if verbosity>=0 and rotated:
                print("Rotated: %s --> %s" % (os.path.basename(self.filename),
                                              os.path.basename(rotated)))
        except ValueError:
            pass
        fo = open(self.filename, 'w')
        fo.write(newfile)
        fo.close()
        os.chmod(self.filename, int('0600',8))

    def save(self, d, caYN=0, verbosity=0):
        """ d == commandline dictionary """

        mapping = {
                '--set-country'     : 'C',
                '--set-state'       : 'ST',
                '--set-city'        : 'L',
                '--set-org'         : 'O',
                '--set-org-unit'    : 'OU',
                '--set-common-name' : 'CN', # these two will never occur at the
                '--set-hostname'    : 'CN', # same time
                '--set-email'       : 'emailAddress',
                  }

        rdn = {}
        for k in d.keys():
            if k in mapping:
                rdn[mapping[k]] = d[k].strip()

        openssl_cnf = ''
        if caYN:
            openssl_cnf = CONF_TEMPLATE_CA % (
              os.path.dirname(self.filename)+'/',
              gen_req_distinguished_name(rdn),
              )
        else:
            openssl_cnf = CONF_TEMPLATE_SERVER \
              % (gen_req_distinguished_name(rdn), gen_req_alt_names(d, rdn['CN']))

        try:
            rotated = rotateFile(filepath=self.filename,verbosity=verbosity)
            if verbosity>=0 and rotated:
                print("Rotated: %s --> %s" % (os.path.basename(self.filename),
                                              os.path.basename(rotated)))
        except ValueError:
            pass
        fo = open(self.filename, 'w')
        fo.write(openssl_cnf)
        fo.close()
        os.chmod(self.filename, int('0600',8))
        return openssl_cnf


##
## generated RPM "configuration" dumping ground:
##

POST_UNINSTALL_SCRIPT = """\
if [ \$1 = 0 ]; then
    # The following steps are copied from mod_ssl's postinstall scriptlet
    # Make sure the permissions are okay
    umask 077

    if [ ! -f /etc/httpd/conf/ssl.key/server.key ] ; then
        /usr/bin/openssl genrsa -rand /proc/apm:/proc/cpuinfo:/proc/dma:/proc/filesystems:/proc/interrupts:/proc/ioports:/proc/pci:/proc/rtc:/proc/uptime 1024 > /etc/httpd/conf/ssl.key/server.key 2> /dev/null
    fi

    if [ ! -f /etc/httpd/conf/ssl.crt/server.crt ] ; then
        cat << EOF | /usr/bin/openssl req -new -key /etc/httpd/conf/ssl.key/server.key -x509 -days 365 -out /etc/httpd/conf/ssl.crt/server.crt 2>/dev/null
--
SomeState
SomeCity
SomeOrganization
SomeOrganizationalUnit
localhost.localdomain
root@localhost.localdomain
EOF
    fi
    /sbin/service httpd graceful
    exit 0
fi
"""

SERVER_RPM_SUMMARY = "Organizational server (httpd) SSL key-pair/key-set."
CA_CERT_RPM_SUMMARY = ("Organizational public SSL CA certificate "
                       "(client-side).")



#===============================================================================

