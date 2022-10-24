#!/usr/bin/python -u
#
# Copyright (c) 2008--2014 Red Hat, Inc.
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
# generate bootstrap scripts for the various up2date clients
# (namely 2.x, 3.x and 4.x)
#
# Author: Todd Warner <taw@redhat.com>
#

## language imports
from __future__ import print_function
import os
import sys
import glob
import socket
import shutil
import operator

try:
    import urllib.parse as urlparse
except ImportError:
    import urlparse

from optparse import Option, OptionParser, SUPPRESS_HELP

## local imports
from uyuni.common import rhn_rpm
from spacewalk.common.rhnConfig import CFG, initCFG
from .client_config_update import readConfigFile
from .rhn_bootstrap_strings import \
    getHeader, getGPGKeyImportSh, \
    getCorpCACertSh, getRegistrationStackSh, \
    getRegistrationSaltSh, removeTLSCertificate
from .sslToolConfig import CA_CRT_NAME
from uyuni.common.fileutils import rotateFile, cleanupAbsPath
from uyuni.common.checksum  import getFileChecksum

try:
    from spacewalk.common.rhnConfig import PRODUCT_NAME
except:
    PRODUCT_NAME = 'SUSE Manager'

## GLOBALS
if os.path.exists('/usr/share/rhn/proxy') \
  or os.path.exists('/var/www/rhns/proxy'):
    MY_PRODUCT_NAME = PRODUCT_NAME + ' Proxy'
elif os.path.exists('/usr/share/rhn/server') \
  or os.path.exists('/var/www/rhns/server'):
    MY_PRODUCT_NAME = PRODUCT_NAME + ' Server'

DEFAULT_CA_CERT_PATH = '/usr/share/rhn/'+CA_CRT_NAME

initCFG('server')
DOC_ROOT = CFG.DOCUMENTROOT

DEFAULT_APACHE_PUB_DIRECTORY = DOC_ROOT + '/pub'
DEFAULT_OVERRIDES = 'client-config-overrides.txt'
DEFAULT_SCRIPT = 'bootstrap.sh'


# exit codes
errnoSuccess = 0
errnoGeneral = 1
errnoScriptNameClash = 10
errnoBadScriptName = 11
errnoExtraCommandLineArgs = 12
errnoBadHttpProxyString = 13
errnoBadPath = 14
errnoNotFQDN = 15
errnoCANotFound = 16
errnoGPGNotFound = 17


# should come out of common code when we move this code out of
# rhns-certs-tools
def parseUrl(url):
    """ urlparse is more complicated than what we need.

        We make the assumption that the URL has real URL information.
        NOTE: http/https ONLY for right now.

        The normal behavior of urlparse:
            - if no {http[s],file}:// then the string is considered everything
              that normally follows the URL, e.g. /XMLRPC
            - if {http[s],file}:// exists, anything between that and the next /
              is the URL.

        The behavior of *this* function:
            - if no {http[s],file}:// then the string is simply assumed to be a
              URL without the {http[s],file}:// attached. The parsed info is
              reparsed as one would think it would be:

            - returns: (addressing scheme, network location, path,
                        parameters, query, fragment identifier).

              NOTE: netloc (or network location) can be HOSTNAME:PORT
    """

    schemes = ('http', 'https')
    if url is None:
        return None
    parsed = list(urlparse.urlparse(url))
    if not parsed[0] or parsed[0] not in schemes:
        url = 'https://' + url
        parsed = list(urlparse.urlparse(url))
        parsed[0] = ''
    return tuple(parsed)


def parseHttpProxyString(httpProxy):
    """ parse HTTP proxy string and check for validity """

    httpProxy = parseUrl(httpProxy)[1]
    tup = httpProxy.split(':')
    if len(tup) != 2:
        sys.stderr.write("ERROR: invalid host:port (%s)\n" % httpProxy)
        sys.exit(errnoBadHttpProxyString)
    try:
        int(tup[1])
    except ValueError:
        sys.stderr.write("ERROR: invalid host:port (%s)\n" % httpProxy)
        sys.exit(errnoBadHttpProxyString)
    return httpProxy


def processCACertPath(options):
    if options.ssl_cert:
        if options.ssl_cert[-4:] == '.rpm':
            sys.stderr.write("ERROR: SSL Certificate as rpm package not supported anymore")
            sys.exit(errnoCANotFound)

    if not options.ssl_cert:
        # look for the raw cert
        options.ssl_cert = os.path.join(options.pub_tree, CA_CRT_NAME)
        if not os.path.isfile(options.ssl_cert):
            options.ssl_cert = ''


def getDefaultOptions():
    _defopts = {
            'activation-keys': '',
            'overrides': DEFAULT_OVERRIDES,
            'script': DEFAULT_SCRIPT,
            'hostname': socket.getfqdn(),
            'ssl-cert': '', # will trigger a search
            'gpg-key': "",
            'http-proxy': "",
            'http-proxy-username': "",
            'http-proxy-password': "",
            'allow-config-actions': 0,
            'allow-remote-commands': 0,
            'no-bundle': 0,
            'force-bundle': 0,
            'no-gpg': 0,
            'force': 0,
            'pub-tree': DEFAULT_APACHE_PUB_DIRECTORY,
            'verbose': 0,
               }
    return _defopts

defopts = getDefaultOptions()


def getOptionsTable():
    """ returns the command line options table """

    def getSetString(value):
        if value:
            return 'SET'
        return 'UNSET'

    # the options
    bsOptions = [
        Option('--activation-keys',
               action='store',
               type='string', default=defopts['activation-keys'],
               help='activation key as defined in the web UI - only 1 key is allowed now (currently: %s)' % repr(defopts['activation-keys'])),
        Option('--overrides',
               action='store',
               type='string', default=defopts['overrides'],
               help='configuration overrides filename (currently: %s)' % defopts['overrides']),
        Option('--script',
               action='store',
               type='string', default=defopts['script'],
               help='bootstrap script filename. (currently: %s)' % defopts['script']),
        Option('--hostname',
               action='store',
               type='string', default=defopts['hostname'],
               help='hostname (FQDN) to which clients connect (currently: %s)' % defopts['hostname']),
        Option('--ssl-cert',
               action='store',
               type='string', default=defopts['ssl-cert'],
               help='path to corporate public SSL certificate - an RPM or a raw certificate. It will be copied to --pub-tree. A value of "" will force a search of --pub-tree.'),
        Option('--gpg-key',
               action='store',
               type='string', default=defopts['gpg-key'],
               help='path to corporate public GPG key, if used. It will be copied to the location specified by the --pub-tree option. Format is GPG_KEY1,GPG_KEY2 (currently: %s)' % repr(defopts['gpg-key'])),
        Option('--http-proxy',
               action='store',
               type='string', default=defopts['http-proxy'],
               help='HTTP proxy setting for the clients - hostname:port. --http-proxy="" disables. (currently: %s)' % repr(defopts['http-proxy'])),
        Option('--http-proxy-username',
               action='store',
               type='string', default=defopts['http-proxy-username'],
               help='if using an authenticating HTTP proxy, specify a username. --http-proxy-username="" disables. (currently: %s)' % repr(defopts['http-proxy-username'])),
        Option('--http-proxy-password',
               action='store',
               type='string', default=defopts['http-proxy-password'],
               help='if using an authenticating HTTP proxy, specify a password. (currently: %s)' % repr(defopts['http-proxy-password'])),
        Option('--no-bundle',
               action='store_true',
               help='boolean; avoid installing salt minion bundle (venv-salt-minion) instead of salt minion (currently %s)' % getSetString(defopts['no-bundle'])),
        Option('--force-bundle',
               action='store_true',
               help='boolean; Force installing salt minion bundle (venv-salt-minion) instead of salt minion (currently %s)' % getSetString(defopts['force-bundle'])),
        Option('--no-gpg',
               action='store_true',
               help='(not recommended) boolean; turn off GPG checking by the clients (currently %s)' % getSetString(defopts['no-gpg'])),
        Option('--pub-tree',
               action='store',
               type='string', default=defopts['pub-tree'],
               help='(change not recommended) public directory tree where the CA SSL cert/cert-RPM will land as well as the bootstrap directory and scripts. (currently %s)' % defopts['pub-tree']),
        Option('--force',
               action='store_true',
               help='(not recommended) boolean; including this option forces bootstrap script generation despite warnings (currently %s)' % getSetString(defopts['force'])),
        Option('-v','--verbose',
               action='count',
               help='be verbose - accumulable: -vvv means "be *really* verbose" (currently %s)' % defopts['verbose']),
    ]

    return bsOptions


def parseCommandline():
    "parse the commandline/options, sanity checking, et c."

    _progName = "mgr-bootstrap"
    _usage = """\
%s [options]

Note: for mgr-bootstrap to work, certain files are expected to be
      in %s/ (the default Apache public directory):
        - the CA SSL public certificate (probably RHN-ORG-TRUSTED-SSL-CERT)
    """ % (_progName, DEFAULT_APACHE_PUB_DIRECTORY)

    # preliminary parse (-h/--help is acted upon during final parse)
    optionList = getOptionsTable()

    optionListNoHelp = optionList[:]
    fake_help = Option("-h", "--help", action="count", help='')
    optionListNoHelp.append(fake_help)
    options, _args = OptionParser(option_list=optionListNoHelp, add_help_option=0).parse_args()

    # we take no extra commandline arguments that are not linked to an option
    if _args:
        sys.stderr.write("\nERROR: these arguments make no sense in this "
                         "context (try --help): %s\n" % repr(_args))
        sys.exit(errnoExtraCommandLineArgs)


    # reset the defaults - I need them on the next pass
    global defopts
    defopts = {
            'activation-keys': options.activation_keys,
            'overrides': options.overrides or DEFAULT_OVERRIDES,
            'script': options.script or DEFAULT_SCRIPT,
            'hostname': options.hostname,
            'ssl-cert': options.ssl_cert,
            'gpg-key': options.gpg_key,
            'http-proxy': options.http_proxy,
            'http-proxy-username': options.http_proxy_username,
            'http-proxy-password': options.http_proxy,
            'no-bundle': not not options.no_bundle,
            'force-bundle': not not options.force_bundle,
            'no-gpg': not not options.no_gpg,
            'pub-tree': options.pub_tree,
            'force': options.force,
            'verbose': options.verbose or 0,
              }

    processCACertPath(options)
    defopts['ssl-cert'] = options.ssl_cert

    # final parse after defaults have been remapped
    options, _args = OptionParser(option_list=getOptionsTable(), usage=_usage).parse_args()

    return options


def processCommandline():
    options = parseCommandline()

    if options.script[-3:] != '.sh':
        sys.stderr.write("""\
ERROR: value of --script must end in '.sh':
       '%s'\n""" % options.script)
        if not options.force:
            sys.stderr.write("exiting\n")
            sys.exit(errnoBadScriptName)

    options.pub_tree = cleanupAbsPath(options.pub_tree or DEFAULT_APACHE_PUB_DIRECTORY)
    options.overrides = os.path.basename(options.overrides)
    options.script = os.path.basename(options.script)

    if options.pub_tree.find(DEFAULT_APACHE_PUB_DIRECTORY) != 0:
        sys.stderr.write("WARNING: it's *highly* suggested that --pub-tree is set to:\n")
        sys.stderr.write("           %s\n" % DEFAULT_APACHE_PUB_DIRECTORY)
        sys.stderr.write("         It is currently set to:\n")
        sys.stderr.write("           %s\n" % options.pub_tree)
        if not options.force:
            sys.stderr.write("exiting\n")
            sys.exit(errnoBadPath)

    if options.overrides == options.script:
        sys.stderr.write("""\
ERROR: the value of --overrides and --script cannot be the same!
       '%s'\n""" % options.script)
        sys.exit(errnoScriptNameClash)

    if len(options.hostname.split('.')) < 3:
        msg = ("WARNING: --hostname (%s) doesn't appear to be a FQDN.\n"
               % options.hostname)
        sys.stderr.write(msg)
        if not options.force:
            sys.stderr.write("exiting\n")
            sys.exit(errnoNotFQDN)

    processCACertPath(options)
    if options.ssl_cert and not os.path.exists(options.ssl_cert):
        sys.stderr.write("ERROR: CA SSL certificate file not found\n")
        sys.exit(errnoCANotFound)

    if not options.no_gpg and options.gpg_key:
        for gpg_key in options.gpg_key.split(","):
            if not os.path.exists(gpg_key):
                sys.stderr.write("ERROR: corporate public GPG key file '{0}' not found\n".format(gpg_key))
                sys.exit(errnoGPGNotFound)

    if options.http_proxy != "":
        options.http_proxy = parseHttpProxyString(options.http_proxy)

    if not options.http_proxy:
        options.http_proxy_username = ''

    if not options.http_proxy_username:
        options.http_proxy_password = ''

    # forcing numeric values
    for opt in ['force_bundle', 'no_bundle', 'no_gpg', 'verbose']:
        # operator.truth should return (0, 1) or (False, True) depending on
        # the version of python; passing any of those values through int()
        # will return an int
        val = int(operator.truth(getattr(options, opt)))
        setattr(options, opt, val)

    return options


def copyFiles(options):
    """ copies SSL cert and GPG key to --pub-tree if not in there already
        existence check should have already been done.
    """

    pubDir = cleanupAbsPath(options.pub_tree or DEFAULT_APACHE_PUB_DIRECTORY)

    def copyFile(file0, file1):
        if not os.path.exists(os.path.dirname(file1)):
            sys.stderr.write("ERROR: directory does not exist:\n       %s\n"
                             % os.path.dirname(file1))
            sys.exit(errnoBadPath)
        if not os.path.exists(file0):
            sys.stderr.write("ERROR: file does not exist:\n       %s\n"
                             % file0)
            sys.exit(errnoCANotFound)
        sys.stderr.write("""\
  Coping file into public directory tree:
    %s to
    %s
""" % (file0, file1))
        shutil.copy(file0, file1)

    # CA SSL cert
    if options.ssl_cert:
        writeYN = 1
        dest = os.path.join(pubDir, os.path.basename(options.ssl_cert))
        if os.path.dirname(options.ssl_cert) != pubDir:
            if os.path.isfile(dest) \
              and getFileChecksum('md5', options.ssl_cert) != getFileChecksum('md5', dest):
                rotateFile(dest, options.verbose)
            elif os.path.isfile(dest):
                writeYN = 0
            if writeYN:
                copyFile(options.ssl_cert, dest)

    # corp GPG keys
    if not options.no_gpg and options.gpg_key:
        for gpg_key in options.gpg_key.split(","):
            writeYN = 1
            dest = os.path.join(pubDir, os.path.basename(gpg_key))
            if os.path.dirname(gpg_key) != pubDir:
                if os.path.isfile(dest) \
                  and getFileChecksum('md5', gpg_key) != getFileChecksum('md5', dest):
                    rotateFile(dest, options.verbose)
                elif os.path.isfile(dest):
                    writeYN = 0
                if writeYN:
                    copyFile(gpg_key, dest)


def writeClientConfigOverrides(options):
    """ write our "overrides" configuration file
        This generated file is a configuration mapping file that is used
        to map settings in up2date and rhn_register when run through a
        seperate script.
    """

    up2dateConfMap = {
        # some are directly mapped, others are handled more delicately
        'http_proxy':           'httpProxy',
        'http_proxy_username':  'proxyUser',
        'http_proxy_password':  'proxyPassword',
        'hostname':             'serverURL',
        'ssl_cert':             'sslCACert',
        'no_gpg':               'useGPG',
    }

    _bootstrapDir = cleanupAbsPath(os.path.join(options.pub_tree, 'bootstrap'))

    if not os.path.exists(_bootstrapDir):
        print("* creating '%s'" % _bootstrapDir)
        os.makedirs(_bootstrapDir) # permissions should be fine

    d = {}
    if options.hostname:
        d['serverURL'] = 'https://' + options.hostname + '/XMLRPC'

    # if proxy, enable it
    # if "", disable it
    if options.http_proxy:
        d['enableProxy'] = '1'
        d[up2dateConfMap['http_proxy']] = options.http_proxy
    else:
        d['enableProxy'] = '0'
        d[up2dateConfMap['http_proxy']] = ""

    # if proxy username, enable auth proxy
    # if "", disable it
    if options.http_proxy_username:
        d['enableProxyAuth'] = '1'
        d[up2dateConfMap['http_proxy_username']] = options.http_proxy_username
        d[up2dateConfMap['http_proxy_password']] = options.http_proxy_password
    else:
        d['enableProxyAuth'] = '0'
        d[up2dateConfMap['http_proxy_username']] = ""
        d[up2dateConfMap['http_proxy_password']] = ""

    processCACertPath(options)
    if not options.ssl_cert:
        sys.stderr.write("WARNING: no SSL CA certificate found in %s\n" % options.pub_tree)
    _certname = os.path.basename(options.ssl_cert) or CA_CRT_NAME
    _certdir = os.path.dirname(DEFAULT_CA_CERT_PATH)
    d[up2dateConfMap['ssl_cert']] = os.path.join(_certdir, _certname)
    d[up2dateConfMap['no_gpg']] = int(operator.truth(not options.no_gpg))

    writeYN = 1
    _overrides = cleanupAbsPath(os.path.join(_bootstrapDir, options.overrides))
    if os.path.exists(_overrides):
        if readConfigFile(_overrides) != d:
            # only back it up if different
            backup = rotateFile(_overrides, depth=5, verbosity=options.verbose)
            if backup and options.verbose>=0:
                print("""\
* WARNING: if there were hand edits to the rotated (backed up) file,
           some settings may need to be migrated.""")
        else:
            # exactly the same... no need to write
            writeYN = 0
            print("""\
* client configuration overrides (old and new are identical; not written):
  '%s'\n""" % _overrides)

    if writeYN:
        fout = open(_overrides, 'w')
        # header
        fout.write("""\
# RHN Client (rhn_register/up2date) config-overrides file v4.0
#
# To be used only in conjuction with client_config_update.py
#
# This file was autogenerated.
#
# The simple rules:
#     - a setting explicitely overwrites the setting in
#       /etc/syconfig/rhn/{rhn_register,up2date} on the client system.
#     - if a setting is removed, the client's state for that setting remains
#       unchanged.

""")
        keys = list(d.keys())
        keys.sort()
        for key in keys:
            if d[key] is not None:
                fout.write("%s=%s\n" % (key, d[key]))
        fout.close()
        print("""\
* bootstrap overrides (written):
  '%s'\n""" % _overrides)
        if options.verbose>=0:
            print("Values written:")
            for k, v in list(d.items()):
                print(k + ' '*(25-len(k)) + repr(v))


def generateBootstrapScript(options):
    "write, copy and place files into <DEFAULT_APACHE_PUB_DIRECTORY>/bootstrap/"

    orgCACert = os.path.basename(options.ssl_cert or '')

    # write to <DEFAULT_APACHE_PUB_DIRECTORY>/bootstrap/<options.overrides>
    writeClientConfigOverrides(options)

    processCACertPath(options)
    pubname = os.path.basename(options.pub_tree)

    newScript = []

    # generate script
    # In processCommandline() we have turned all boolean values to 0 or 1
    # this means that we can negate those booleans with 1 - their current
    # value (instead of doing not value which can yield True/False, which
    # would print as such)
    newScript.append(
                    getHeader(
                            MY_PRODUCT_NAME,
                            options,
                            orgCACert,
                            pubname,
                            DEFAULT_APACHE_PUB_DIRECTORY
                            )
                    )

    writeYN = 1

    newScript.append(getGPGKeyImportSh())
    newScript.append(getCorpCACertSh())

    # SLES: install packages required for registration on systems that do not have them installed
    newScript.append(getRegistrationStackSh())

    newScript.append(removeTLSCertificate())
    newScript.append(getRegistrationSaltSh(MY_PRODUCT_NAME))

    _bootstrapDir = cleanupAbsPath(os.path.join(options.pub_tree, 'bootstrap'))
    _script = cleanupAbsPath(os.path.join(_bootstrapDir, options.script))

    newScript = ''.join(newScript)

    if os.path.exists(_script):
        oldScript = open(_script, 'r').read()
        if oldScript == newScript:
            writeYN = 0
        elif os.path.exists(_script):
            backup = rotateFile(_script, depth=5, verbosity=options.verbose)
            if backup and options.verbose>=0:
                print("* rotating %s --> %s" % (_script, backup))
        del oldScript

    if writeYN:
        fout = open(_script, 'w')
        fout.write(newScript)
        fout.close()
        print("""\
* bootstrap script (written):
    '%s'\n""" % _script)
    else:
        print("""\
* boostrap script (old and new scripts identical; not written):
    '%s'\n""" % _script)


def main():
    """ Main code block:

        o options on commandline take precedence, but if option not set...
        o prepopulate the commandline options from already generated
          <DEFAULT_APACHE_PUB_DIRECTORY>/bootstrap/client-config-overrides.txt if in existance.
          FIXME: isn't done as of yet.
        o set defaults otherwise
    """

    if '--salt' in sys.argv:
        sys.stderr.write('-' * 65)
        sys.stderr.write('\n')
        sys.stderr.write('DEPRECATION WARNING:\n' \
        '\tThe option "--salt" is default and has been deprecated.\n' \
        '\tThis option should not be specified anymore.' \
        '\tIt will be not recognized in the next release!\n')
        sys.stderr.write('-' * 65)
        sys.stderr.write('\n')

    options = processCommandline()
    copyFiles(options)
    generateBootstrapScript(options)

    return 0


if __name__ == "__main__":
    """ Exit codes - defined at top of module:
            errnoSuccess = 0
            errnoGeneral = 1
            errnoScriptNameClash = 10
            errnoBadScriptName = 11
            errnoExtraCommandLineArgs = 12
            errnoBadHttpProxyString = 13
            errnoBadPath = 14
            errnoNotFQDN = 15
            errnoCANotFound = 16
            errnoGPGNotFound = 17
    """

    try:
        sys.exit(abs(main() or errnoSuccess))
    except SystemExit:
        # No problem, sys.exit() raises this
        raise
    except KeyboardInterrupt:
        sys.exit(errnoSuccess)
    except ValueError as e:
        raise # should exit with a 1 (errnoGeneral)
    except Exception:
        sys.stderr.write('Unhandled ERROR occurred.\n')
        raise # should exit with a 1 (errnoGeneral)
