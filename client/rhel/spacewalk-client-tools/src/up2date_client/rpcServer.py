#

import os
import sys
import socket
import time

from up2date_client import config
from up2date_client import up2dateLog
from up2date_client import up2dateErrors
from up2date_client import up2dateUtils

from rhn import SSL
from rhn import rpclib
from rhn.tb import raise_with_tb

try: # python2
     import httplib
     import urllib2
     import urlparse
     import xmlrpclib
except ImportError: # python3
     import http.client as httplib
     import urllib.request as urllib2
     import urllib.parse as urlparse
     import xmlrpc.client as xmlrpclib

import gettext
t = gettext.translation('rhn-client-tools', fallback=True)
# Python 3 translations don't have a ugettext method
if not hasattr(t, 'ugettext'):
    t.ugettext = t.gettext
_ = t.ugettext

def stdoutMsgCallback(msg):
    print(msg)


class RetryServer(rpclib.Server):
    def addServerList(self, serverList):
        self.serverList = serverList

    def _request1(self, methodname, params):
        self.log = up2dateLog.initLog()
        while 1:
            try:
                ret = self._request(methodname, params)
            except rpclib.InvalidRedirectionError:
                raise
            except xmlrpclib.Fault:
                raise
            except httplib.BadStatusLine:
                self.log.log_me("Error: Server Unavailable. Please try later.")
                stdoutMsgCallback(
                      _("Error: Server Unavailable. Please try later."))
                sys.exit(-1)
            except:
                server = self.serverList.next()
                if server == None:
                    # since just because we failed, the server list could
                    # change (aka, firstboot, they get an option to reset the
                    # the server configuration) so reset the serverList
                    self.serverList.resetServerIndex()
                    raise

                msg = "An error occurred talking to %s:\n" % self._host
                msg = msg + "%s\n%s\n" % (sys.exc_info()[0], sys.exc_info()[1])
                msg = msg + "Trying the next serverURL: %s\n" % self.serverList.server()
                self.log.log_me(msg)
                # try a different url

                # use the next serverURL
                parse_res = urlparse.urlsplit(self.serverList.server())
                typ = parse_res[0] # scheme
                self._host = parse_res[1] # netloc
                self._handler = parse_res[2] # path
                typ = typ.lower()
                if typ not in ("http", "https"):
                    raise_with_tb(rpclib.InvalidRedirectionError(
                        "Redirected to unsupported protocol %s" % typ))
                self._orig_handler = self._handler
                self._type = typ
                self._uri = self.serverList.server()
                if not self._handler:
                    self._handler = "/RPC2"
                self._allow_redirect = 1
                continue
            # if we get this far, we succedded
            break
        return ret


    def __getattr__(self, name):
        # magic method dispatcher
        return rpclib.xmlrpclib._Method(self._request1, name)


# uh, yeah, this could be an iterator, but we need it to work on 1.5 as well
class ServerList:
    def __init__(self, serverlist=[]):
        self.serverList = serverlist
        self.index = 0

    def server(self):
        self.serverurl = self.serverList[self.index]
        return self.serverurl


    def next(self):
        self.index = self.index + 1
        if self.index >= len(self.serverList):
            return None
        return self.server()

    def resetServerIndex(self):
        self.index = 0


def getServer(refreshCallback=None, serverOverride=None, timeout=None, caChain=None):
    log = up2dateLog.initLog()
    cfg = config.initUp2dateConfig()

    if caChain:
        ca = caChain
    else:
        # Where do we keep the CA certificate for RHNS?
        # The servers we're talking to need to have their certs
        # signed by one of these CA.
        ca = cfg["sslCACert"]
    if not isinstance(ca, list):
        ca = [ca]

    rhns_ca_certs = ca or ["/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT"]
    if cfg["enableProxy"]:
        proxyHost = config.getProxySetting()
    else:
        proxyHost = None

    if not serverOverride:
        serverUrls = config.getServerlURL()
    else:
        serverUrls = serverOverride
    serverList = ServerList(serverUrls)

    proxyUser = None
    proxyPassword = None
    if cfg["enableProxyAuth"]:
        proxyUser = cfg["proxyUser"] or None
        proxyPassword = cfg["proxyPassword"] or None

    lang = None
    for env in 'LANGUAGE', 'LC_ALL', 'LC_MESSAGES', 'LANG':
        if env in os.environ:
            if not os.environ[env]:
                # sometimes unset
                continue
            lang = os.environ[env].split(':')[0]
            lang = lang.split('.')[0]
            break


    s = RetryServer(serverList.server(),
                    refreshCallback=refreshCallback,
                    proxy=proxyHost,
                    username=proxyUser,
                    password=proxyPassword,
                    timeout=timeout)
    s.addServerList(serverList)

    s.add_header("X-Up2date-Version", up2dateUtils.version())

    if lang:
        s.setlang(lang)

    # require SSL CA file to be able to authenticate the SSL connections
    need_ca = [ True for i in s.serverList.serverList
                     if urlparse.urlparse(i)[0] == 'https']
    if need_ca:
        for rhns_ca_cert in rhns_ca_certs:
            if not os.access(rhns_ca_cert, os.R_OK):
                msg = "%s: %s" % (_("ERROR: can not find server CA file"),
                                     rhns_ca_cert)
                log.log_me("%s" % msg)
                raise up2dateErrors.SSLCertificateFileNotFound(msg)

            # force the validation of the SSL cert
            s.add_trusted_cert(rhns_ca_cert)

    return s
