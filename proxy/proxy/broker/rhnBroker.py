# Spacewalk Proxy Server Broker handler code.
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

# system module imports
import time
import socket
import re
import os
import base64
try:
    # python 3
    from urllib.parse import urlparse, urlunparse
except ImportError:
    # python 2
    from urlparse import urlparse, urlunparse

# common module imports
from rhn.UserDictCase import UserDictCase
from rhn.stringutils import ustr
from spacewalk.common.rhnConfig import CFG
from spacewalk.common.rhnLog import log_debug, log_error
from spacewalk.common.rhnException import rhnFault
from spacewalk.common import rhnFlags, apache
from spacewalk.common.rhnTranslate import _
from spacewalk.common import suseLib
from uyuni.common.rhnLib import parseUrl

# local module imports
from proxy.rhnShared import SharedHandler
from proxy.rhnConstants import URI_PREFIX_KS_CHECKSUM
import proxy.rhnProxyAuth
from . import rhnRepository


# the version should not be never decreased, never mind that spacewalk has different versioning
_PROXY_VERSION = '5.5.0'
# HISTORY: '0.9.7', '3.2.0', '3.5.0', '3.6.0', '4.1.0',
#          '4.2.0', '5.0.0', '5.1.0', '5.2.0', '0.1',
#          '5.3.0', '5.3.1', '5.4.0', '5.5.0'


class BrokerHandler(SharedHandler):

    """ Spacewalk Proxy broker specific handler code called by rhnApache.

        Workflow is:
        Client -> Apache:Broker -> Squid -> Apache:Redirect -> Satellite

        Broker handler get request from clients from outside. Some request
        (POST and HEAD) bypass cache so, it is passed directly to parent.
        For everything else we transform destination to localhost:80 (which
        is handled by Redirect handler) and set proxy as local squid.
        This way we got all request cached localy by squid.
    """

    # pylint: disable=R0902
    def __init__(self, req):
        SharedHandler.__init__(self, req)

        # Initialize variables
        self.componentType = 'proxy.broker'
        self.cachedClientInfo = None  # headers - session token
        self.authChannels = None
        self.clientServerId = None
        self.rhnParentXMLRPC = None
        self.authToken = None
        self.fullRequestURL = None
        hostname = ''
        # should *always* exist and be my ip address
        my_ip_addr = req.headers_in['SERVER_ADDR']
        if 'Host' in req.headers_in:
            # the client has provided a host header
            try:
                # When a client with python 2.4 (RHEL 5) uses SSL
                # the host header is in the 'hostname:port' form
                # (In python 2.6 RFE #1472176 changed this and 'hostname'
                # is used). We need to use the 'hostname' part in any case
                # or we create bogus 'hostname:port' DNS queries
                host_header = req.headers_in['Host'].split(':')[0]
                if host_header != my_ip_addr and \
                    socket.gethostbyname(host_header) == my_ip_addr:
                    # if host header is valid (i.e. not just an /etc/hosts
                    # entry on the client or the hostname of some other
                    # machine (say a load balancer)) then use it
                    hostname = host_header
            except (socket.gaierror, socket.error,
                    socket.herror, socket.timeout):
                # hostname probably didn't exist, fine
                pass
        if not hostname:
            # okay, that didn't work, let's do a reverse dns lookup on my
            # ip address
            try:
                hostname = socket.gethostbyaddr(my_ip_addr)[0]
            except (socket.gaierror, socket.error,
                    socket.herror, socket.timeout):
                # unknown host, we don't have a hostname?
                pass
        if not hostname:
            # this shouldn't happen
            # socket.gethostname is a punt. Shouldn't need to do it.
            hostname = socket.gethostname()
            log_debug(-1, 'WARNING: no hostname in the incoming headers; '
                          'punting: %s' % hostname)
        hostname = parseUrl(hostname)[1].split(':')[0]
        self.proxyAuth = proxy.rhnProxyAuth.get_proxy_auth(hostname)

        self._initConnectionVariables(req)

    def _initConnectionVariables(self, req):
        """ set connection variables
            NOTE: self.{caChain,rhnParent,httpProxy*} are initialized
                  in SharedHandler

            rules:
                - GET requests:
                      . are non-SSLed (potentially SSLed by the redirect)
                      . use the local cache
                      . use the SSL Redirect
                        (i.e., parent is now 127.0.0.1)
                        . NOTE: the reason we use the SSL Redirect if we
                                are going through an outside HTTP_PROXY:
                                o CFG.HTTP_PROXY is ONLY used by an SSL
                                redirect - maybe should rethink that.
                - POST and HEAD requests (not GET) bypass both the local cache
                       and SSL redirect (we SSL it directly)
        """

        scheme = 'http'
        # self.{caChain,httpProxy*,rhnParent} initialized in rhnShared.py
        effectiveURI = self._getEffectiveURI()
        effectiveURI_parts = urlparse(effectiveURI)
        # Fixup effectiveURI_parts, if effectiveURI is dirty.
        # We are doing this because the ubuntu clients request uris like
        # 'http://hostname//XMLRPC...'. See bug 1220399 for details.
        if not effectiveURI_parts.scheme and effectiveURI_parts.netloc and effectiveURI_parts.netloc == 'XMLRPC':
            effectiveURI_parts = urlparse(urlunparse([
                '',
                '',
                '/' + effectiveURI_parts.netloc + effectiveURI_parts.path,
                effectiveURI_parts.params,
                effectiveURI_parts.query,
                effectiveURI_parts.fragment]))

        # The auth token is sent in either a header or in the query part of the URI:
        # SLE minions -> query part of the URI.
        # RHEL minions -> 'X-Mgr-Auth' header.
        # Debian -> Authorization (Basic Auth)
        #
        # Traditional SLE and RHEL clients uses 'X-RHN-Auth' header, but
        # no auth token is used in order to authenticate.
        if 'X-Mgr-Auth' in self.req.headers_in:
            self.authToken = self.req.headers_in['X-Mgr-Auth']
            del self.req.headers_in['X-Mgr-Auth']
        elif 'Authorization' in self.req.headers_in and effectiveURI_parts.path.startswith('/rhn/manager/download/'):
            # we need to remove Basic Auth, otherwise squid does not cache the package
            # so we convert it into token auth
            # The token is the login. So it is not secret
            try:
                lpw = ustr(base64.b64decode(self.req.headers_in['Authorization'][6:])) # "Basic " == 6 characters
                self.authToken = lpw[:lpw.find(':')]
                del self.req.headers_in['Authorization']
            except Exception as e:
                log_error("Unable to decode Authorization header.", e)
        elif 'X-RHN-Auth' not in self.req.headers_in:
            self.authToken = effectiveURI_parts.query

        if req.method == 'GET':
            self.fullRequestURL = "%s://%s%s" % (self.req.headers_in['REQUEST_SCHEME'], self.rhnParent, effectiveURI)
            effectiveURI_parts = urlparse(urlunparse([
                effectiveURI_parts.scheme,
                effectiveURI_parts.netloc,
                effectiveURI_parts.path,
                effectiveURI_parts.params,
                '',
                effectiveURI_parts.fragment]))
            scheme = 'http'
            self.httpProxy = CFG.SQUID
            self.caChain = self.httpProxyUsername = self.httpProxyPassword = ''
            self.rhnParent = self.proxyAuth.hostname
        else:
            scheme = 'https'

        self.rhnParentXMLRPC = urlunparse((scheme, self.rhnParent, '/XMLRPC', '', '', ''))
        self.rhnParent = urlunparse((scheme, self.rhnParent) + effectiveURI_parts[2:])

        log_debug(2, 'set self.rhnParent:       %s' % self.rhnParent)
        log_debug(2, 'set self.rhnParentXMLRPC: %s' % self.rhnParentXMLRPC)
        if self.httpProxy:
            if self.httpProxyUsername and self.httpProxyPassword:
                log_debug(2, 'using self.httpProxy:     %s (authenticating)' % self.httpProxy)
            else:
                log_debug(2, 'using self.httpProxy:     %s (non-authenticating)' % self.httpProxy)
        else:
            log_debug(2, '*not* using an http proxy')

    def handler(self):
        """ Main handler to handle all requests pumped through this server. """

        # pylint: disable=R0915
        log_debug(2)
        self._prepHandler()

        _oto = rhnFlags.get('outputTransportOptions')

        # tell parent that we can follow redirects, even if client is not able to
        _oto['X-RHN-Transport-Capability'] = "follow-redirects=3"

        # No reason to put Host: in the header, the connection object will
        # do that for us

        # Add/modify the X-RHN-IP-Path header.
        ip_path = None
        if 'X-RHN-IP-Path' in _oto:
            ip_path = _oto['X-RHN-IP-Path']
        log_debug(4, "X-RHN-IP-Path is: %s" % repr(ip_path))
        client_ip = self.req.connection.remote_ip
        if ip_path is None:
            ip_path = client_ip
        else:
            ip_path += ',' + client_ip
        _oto['X-RHN-IP-Path'] = ip_path

        # NOTE: X-RHN-Proxy-Auth described in broker/rhnProxyAuth.py
        if 'X-RHN-Proxy-Auth' in _oto:
            log_debug(5, 'X-RHN-Proxy-Auth currently set to: %s' % repr(_oto['X-RHN-Proxy-Auth']))
        else:
            log_debug(5, 'X-RHN-Proxy-Auth is not set')

        if 'X-RHN-Proxy-Auth' in self.req.headers_in:
            tokens = []
            if 'X-RHN-Proxy-Auth' in _oto:
                tokens = _oto['X-RHN-Proxy-Auth'].split(',')
            log_debug(5, 'Tokens: %s' % tokens)

        # GETs: authenticate user, and service local GETs.
        getResult = self.__local_GET_handler(self.req)
        if getResult is not None:
            # it's a GET request
            return getResult

        # 1. check cached version of the proxy login,
        #    snag token if there...
        #    if not... login...
        #    if good token, cache it.
        # 2. push into headers.
        authToken = self.proxyAuth.check_cached_token()
        log_debug(5, 'Auth token for this machine only! %s' % authToken)
        tokens = []

        _oto = rhnFlags.get('outputTransportOptions')
        if 'X-RHN-Proxy-Auth' in _oto:
            log_debug(5, '    (auth token prior): %s' % repr(_oto['X-RHN-Proxy-Auth']))
            tokens = _oto['X-RHN-Proxy-Auth'].split(',')

        # list of tokens to be pushed into the headers.
        tokens.append(authToken)
        tokens = [t for t in tokens if t]

        _oto['X-RHN-Proxy-Auth'] = ','.join(tokens)
        log_debug(5, '    (auth token after): %s'
                  % repr(_oto['X-RHN-Proxy-Auth']))

        if self.fullRequestURL and self.authToken:
            # For RHEL Minions the auth token is not included in the fullRequestURL
            # because it was provided as 'X-Mgr-Auth' header.
            # In this case We need to append it to the URL to check if accessible
            # with the given auth token.
            checkURL = self.fullRequestURL
            if not self.authToken in checkURL:
                checkURL += "?" + self.authToken
            if not suseLib.accessible(checkURL):
                return apache.HTTP_FORBIDDEN
        if self.authToken:
            _oto['X-Suse-Auth-Token'] = self.authToken

        log_debug(3, 'Trying to connect to parent')

        # Loops twice? Here's why:
        #   o If no errors, the loop is broken and we move on.
        #   o If an error, either we get a new token and try again,
        #     or we get a critical error and we fault.
        for _i in range(2):
            self._connectToParent()  # part 1

            log_debug(4, 'after _connectToParent')
            # Add the proxy version
            rhnFlags.get('outputTransportOptions')['X-RHN-Proxy-Version'] = str(_PROXY_VERSION)

            status = self._serverCommo()       # part 2

            # check for proxy authentication blowup.
            respHeaders = self.responseContext.getHeaders()
            if not respHeaders or \
               'X-RHN-Proxy-Auth-Error' not in respHeaders:
                # No proxy auth errors
                # XXX: need to verify that with respHeaders ==
                #      None that is is correct logic. It should be -taw
                break

            error = str(respHeaders['X-RHN-Proxy-Auth-Error']).split(':')[0]

            # If a proxy other than this one needs to update its auth token
            # pass the error on up to it
            if ('X-RHN-Proxy-Auth-Origin' in respHeaders and
                    respHeaders['X-RHN-Proxy-Auth-Origin'] != self.proxyAuth.hostname):
                break

            # Expired/invalid auth token; go through the loop once again
            if error == '1003': # invalid token
                msg = "SUSE Manager Proxy Session Token INVALID -- bad!"
                log_error(msg)
                log_debug(0, msg)
            elif error == '1004':
                log_debug(2,
                          "SUSE Manager Proxy Session Token expired, acquiring new one.")
            else: # this should never happen.
                msg = "SUSE Manager Proxy login failed, error code is %s" % error
                log_error(msg)
                log_debug(0, msg)
                raise rhnFault(1000,
                               _("SUSE Manager Proxy error (issues with proxy login). "
                                 "Please contact your system administrator."))

            # Forced refresh of the proxy token
            rhnFlags.get('outputTransportOptions')['X-RHN-Proxy-Auth'] = self.proxyAuth.check_cached_token(1)
        else:  # for
            # The token could not be aquired
            log_debug(0, "Unable to acquire proxy authentication token")
            raise rhnFault(1000,
                           _("SUSE Manager Proxy error (unable to acquire proxy auth token). "
                             "Please contact your system administrator."))

        # Support for yum byte-range
        if status not in (apache.OK, apache.HTTP_PARTIAL_CONTENT):
            log_debug(1, "Leaving handler with status code %s" % status)
            return status

        self.__handleAction(self.responseContext.getHeaders())

        return self._clientCommo()

    def _prepHandler(self):
        """ prep handler and check PROXY_AUTH's expiration. """
        SharedHandler._prepHandler(self)

    @staticmethod
    def _split_ks_url(req):
        """ read kickstart options from incoming url
            URIs we care about look something like:
            /ks/dist/session/2xfe7113bc89f359001280dee1f4a020bc/
                ks-rhel-x86_64-server-6-6.5/Packages/rhnsd-4.9.3-2.el6.x86_64.rpm
            /ks/dist/ks-rhel-x86_64-server-6-6.5/Packages/
                rhnsd-4.9.3-2.el6.x86_64.rpm
            /ks/dist/org/1/ks-rhel-x86_64-server-6-6.5/Packages/
                rhnsd-4.9.3-2.el6.x86_64.rpm
            /ks/dist/ks-rhel-x86_64-server-6-6.5/child/sherr-child-1/Packages/
                rhnsd-4.9.3-2.el6.x86_64.rpm
        """
        args = req.path_info.split('/')
        params = {'child': None, 'session': None, 'orgId': None,
                  'file': args[-1]}
        action = None
        if args[2] == 'org':
            params['orgId'] = args[3]
            kickstart = args[4]
            if args[5] == 'Packages':
                action = 'getPackage'
        elif args[2] == 'session':
            params['session'] = args[3]
            kickstart = args[4]
            if args[5] == 'Packages':
                action = 'getPackage'
        elif args[3] == 'child':
            params['child'] = args[4]
            kickstart = args[2]
            if args[5] == 'Packages':
                action = 'getPackage'
        else:
            kickstart = args[2]
            if args[3] == 'Packages':
                action = 'getPackage'
        return kickstart, action, params

    @staticmethod
    def _split_url(req):
        """ read url from incoming url and return (req_type, channel, action, params)
            URI should look something like:
            /GET-REQ/rhel-i386-server-5/getPackage/autofs-5.0.1-0.rc2.143.el5_5.6.i386.rpm
        """
        args = req.path_info.split('/')
        if len(args) < 5:
            return (None, None, None, None)

        return (args[1], args[2], args[3], args[4:])

    # --- PRIVATE METHODS ---

    def __handleAction(self, headers):
        log_debug(2)
        # Check if proxy is interested in this action, and execute any
        # action required:
        if 'X-RHN-Action' not in headers:
            # Don't know what to do
            return

        log_debug(2, "Action is %s" % headers['X-RHN-Action'])
        # Now, is it a login? If so, cache the session token.
        if headers['X-RHN-Action'] != 'login':
            # Don't care
            return

        # A login. Cache the session token
        self.__cacheClientSessionToken(headers)

    def __local_GET_handler(self, req):
        """ GETs: authenticate user, and service local GETs.
            if not a local fetch, return None
        """

        log_debug(2, 'request method: %s' % req.method)
        # Early test to check if this is a request the proxy can handle
        # Can we serve this request?
        if req.method != "GET" or not CFG.PKG_DIR:
            # Don't know how to handle this
            return None

        # Tiny-url kickstart requests (for server kickstarts, aka not profiles)
        # have been name munged and we've already sent a HEAD request to the
        # Satellite to get a checksum for the rpm so we can find it in the
        # squid cache.
        # Original url looks like /ty/bSWE7qIq/Packages/policycoreutils-2.0.83
        #  -19.39.el6.x86_64.rpm which gets munged to be /ty-cksm/ddb43838ad58
        #  d74dc95badef543cd96459b8bb37ff559339de58ec8dbbd1f18b/Packages/polic
        #  ycoreutils-2.0.83-19.39.el6.x86_64.rpm
        args = req.path_info.split('/')
        # urlparse returns a ParseResult, index 2 is the path
        if re.search('^' + URI_PREFIX_KS_CHECKSUM, urlparse(self.rhnParent)[2]):
            # We *ONLY* locally cache RPMs for kickstarts
            if len(args) < 3 or args[2] != 'Packages':
                return None
            req_type = 'tinyurl'
            reqident = args[1]
            reqaction = 'getPackage'
            reqparams = [args[-1]]
            self.cachedClientInfo = UserDictCase()
        elif (len(args) > 3 and args[1] == 'dist'):
            # This is a kickstart request
            req_type = 'ks-dist'
            reqident, reqaction, reqparams = self._split_ks_url(req)
            self.cachedClientInfo = UserDictCase()
        else:
            # Some other type of request
            (req_type, reqident, reqaction, reqparams) = self._split_url(req)
            if reqaction == 'getPackage':
                reqparams = tuple([os.path.join(*reqparams)])

        if req_type is None or (req_type not in
                                ['$RHN', 'GET-REQ', 'tinyurl', 'ks-dist']):
            # not a traditional RHN GET (i.e., it is an arbitrary get)
            # XXX: there has to be a more elegant way to do this
            return None

        # kickstarts don't auth...
        if req_type in ['$RHN', 'GET-REQ']:
            # --- AUTH. CHECK:
            # Check client authentication. If not authenticated, throw
            # an exception.
            token = self.__getSessionToken()
            self.__checkAuthSessionTokenCache(token, reqident)

            # Is this channel local?
            for ch in self.authChannels:
                channel, _version, _isBaseChannel, isLocalChannel = ch[:4]
                if channel == reqident and str(isLocalChannel) == '1':
                    # Local channel
                    break
            else:
                # Not a local channel
                return None

        # --- LOCAL GET:
        localFlist = CFG.PROXY_LOCAL_FLIST or []

        if reqaction not in localFlist:
            # Not an action we know how to handle
            return None

        # We have a match; we'll try to serve packages from the local
        # repository
        log_debug(3, "Retrieve from local repository.")
        log_debug(3, req_type, reqident, reqaction, reqparams)
        result = self.__callLocalRepository(req_type, reqident, reqaction,
                                            reqparams)
        if result is None:
            log_debug(3, "Not available locally; will try higher up the chain.")
        else:
            # Signal that we have to XMLRPC encode the response in apacheHandler
            rhnFlags.set("NeedEncoding", 1)

        return result

    @staticmethod
    def __getSessionToken():
        """ Get/test-for session token in headers (rhnFlags) """
        log_debug(2)
        if not rhnFlags.test("AUTH_SESSION_TOKEN"):
            raise rhnFault(33, "Missing session token")
        return rhnFlags.get("AUTH_SESSION_TOKEN")

    def __cacheClientSessionToken(self, headers):
        """pull session token from headers and push to caching daemon. """

        log_debug(2)
        # Get the server ID
        if 'X-RHN-Server-ID' not in headers:
            log_debug(3, "Client server ID not found in headers")
            # XXX: no client server ID in headers, should we care?
            #raise rhnFault(1000, _("Client Server ID not found in headers!"))
            return None
        serverId = 'X-RHN-Server-ID'

        self.clientServerId = headers[serverId]
        token = UserDictCase()

        # The session token contains everything that begins with
        # "x-rhn-auth"
        prefix = "x-rhn-auth"
        l = len(prefix)
        tokenKeys = [x for x in list(headers.keys()) if x[:l].lower() == prefix]
        for k in tokenKeys:
            if k.lower() == 'x-rhn-auth-channels':
                # Multivalued header
                #values = headers.getHeaderValues(k)
                values = self._get_header(k)
                token[k] = [x.split(':') for x in values]
            else:
                # Single-valued header
                token[k] = headers[k]

        # Dump the proxy's clock skew in the dict
        serverTime = float(token['X-RHN-Auth-Server-Time'])
        token["X-RHN-Auth-Proxy-Clock-Skew"] = time.time() - serverTime

        # Save the token
        self.proxyAuth.set_client_token(self.clientServerId, token)
        return token

    def __callLocalRepository(self, req_type, identifier, funct, params):
        """ Contacts the local repository and retrieves files"""

        log_debug(2, req_type, identifier, funct, params)

        # NOTE: X-RHN-Proxy-Auth described in broker/rhnProxyAuth.py
        if 'X-RHN-Proxy-Auth' in rhnFlags.get('outputTransportOptions'):
            self.cachedClientInfo['X-RHN-Proxy-Auth'] = rhnFlags.get('outputTransportOptions')['X-RHN-Proxy-Auth']
        if 'Host' in rhnFlags.get('outputTransportOptions'):
            self.cachedClientInfo['Host'] = rhnFlags.get('outputTransportOptions')['Host']

        if req_type == 'tinyurl':
            try:
                rep = rhnRepository.TinyUrlRepository(identifier,
                                                      self.cachedClientInfo, rhnParent=self.rhnParent,
                                                      rhnParentXMLRPC=self.rhnParentXMLRPC,
                                                      httpProxy=self.httpProxy,
                                                      httpProxyUsername=self.httpProxyUsername,
                                                      httpProxyPassword=self.httpProxyPassword,
                                                      caChain=self.caChain,
                                                      systemId=self.proxyAuth.get_system_id())
            except rhnRepository.NotLocalError:
                return None
        elif req_type == 'ks-dist':
            try:
                rep = rhnRepository.KickstartRepository(identifier,
                                                        self.cachedClientInfo, rhnParent=self.rhnParent,
                                                        rhnParentXMLRPC=self.rhnParentXMLRPC,
                                                        httpProxy=self.httpProxy,
                                                        httpProxyUsername=self.httpProxyUsername,
                                                        httpProxyPassword=self.httpProxyPassword,
                                                        caChain=self.caChain, orgId=params['orgId'],
                                                        child=params['child'], session=params['session'],
                                                        systemId=self.proxyAuth.get_system_id())
            except rhnRepository.NotLocalError:
                return None
            params = [params['file']]
        else:
            # Find the channel version
            version = None
            for c in self.authChannels:
                ch, ver = c[:2]
                if ch == identifier:
                    version = ver
                    break

            # We already know he's subscribed to this channel
            # channel, so the version is non-null
            rep = rhnRepository.Repository(identifier, version,
                                           self.cachedClientInfo, rhnParent=self.rhnParent,
                                           rhnParentXMLRPC=self.rhnParentXMLRPC,
                                           httpProxy=self.httpProxy,
                                           httpProxyUsername=self.httpProxyUsername,
                                           httpProxyPassword=self.httpProxyPassword,
                                           caChain=self.caChain)

        f = rep.get_function(funct)
        if not f:
            raise rhnFault(1000,
                           _("SUSE Manager Proxy configuration error: invalid function %s") % funct)

        log_debug(3, "Calling %s(%s)" % (funct, params))
        if params is None:
            params = ()
        try:
            ret = f(*params)
        except rhnRepository.NotLocalError:
            # The package is not local
            return None

        return ret

    def __checkAuthSessionTokenCache(self, token, channel):
        """ Authentication / authorize the channel """

        log_debug(2, token, channel)
        self.clientServerId = token['X-RHN-Server-ID']

        cachedToken = self.proxyAuth.get_client_token(self.clientServerId)
        if not cachedToken:
            # maybe client logged in through different load-balanced proxy
            # try to update the cache an try again
            cachedToken = self.proxyAuth.update_client_token_if_valid(
                self.clientServerId, token)

            if not cachedToken:
                msg = _("Invalid session key - server ID not found in cache: %s") \
                        % self.clientServerId
                log_error(msg)
                raise rhnFault(33, msg)

        self.cachedClientInfo = UserDictCase(cachedToken)

        clockSkew = self.cachedClientInfo["X-RHN-Auth-Proxy-Clock-Skew"]
        del self.cachedClientInfo["X-RHN-Auth-Proxy-Clock-Skew"]

        # Add the server id
        self.authChannels = self.cachedClientInfo['X-RHN-Auth-Channels']
        del self.cachedClientInfo['X-RHN-Auth-Channels']
        self.cachedClientInfo['X-RHN-Server-ID'] = self.clientServerId
        log_debug(4, 'Retrieved token from cache: %s' % self.cachedClientInfo)

        authChannels = [x[0] for x in self.authChannels]
        log_debug(4, "Auth channels: '%s'" % authChannels)

        # Compare the two things
        if not _dictEquals(token, self.cachedClientInfo, ['X-RHN-Auth-Channels']) or \
                channel not in authChannels:
            # Maybe the client logged in through a different load-balanced
            # proxy? Check validity of the token the client passed us.
            updatedToken = self.proxyAuth.update_client_token_if_valid(
                self.clientServerId, token)
            # fix up the updated token the same way we did above
            if updatedToken:
                self.cachedClientInfo = UserDictCase(updatedToken)
                clockSkew = self.cachedClientInfo[
                    "X-RHN-Auth-Proxy-Clock-Skew"]
                del self.cachedClientInfo["X-RHN-Auth-Proxy-Clock-Skew"]
                self.authChannels = self.cachedClientInfo[
                    'X-RHN-Auth-Channels']
                del self.cachedClientInfo['X-RHN-Auth-Channels']
                self.cachedClientInfo['X-RHN-Server-ID'] = \
                        self.clientServerId
                log_debug(4, 'Retrieved token from cache: %s' %
                          self.cachedClientInfo)

            if not updatedToken or not _dictEquals(
                    token, self.cachedClientInfo, ['X-RHN-Auth-Channels']):
                log_debug(3, "Session tokens different")
                raise rhnFault(33)  # Invalid session key

        # Check the expiration
        serverTime = float(token['X-RHN-Auth-Server-Time'])
        offset = float(token['X-RHN-Auth-Expire-Offset'])
        if time.time() > serverTime + offset + clockSkew:
            log_debug(3, "Session token has expired")
            raise rhnFault(34)  # Session key has expired

        # Only autherized channels are the ones stored in the cache.
        authChannels = [x[0] for x in self.authChannels]
        log_debug(4, "Auth channels: '%s'" % authChannels)
        # Check the authorization
        if channel not in authChannels:
            log_debug(4, "Not subscribed to channel %s; unauthorized" %
                      channel)
            raise rhnFault(35, _('Unauthorized channel access requested.'))


def _dictEquals(d1, d2, exceptions=None):
    """ Function that compare two dictionaries, ignoring certain keys """
    exceptions = [x.lower() for x in (exceptions or [])]
    for k, v in list(d1.items()):
        if k.lower() in exceptions:
            continue
        if k not in d2 or d2[k] != v:
            return 0
    for k, v in list(d2.items()):
        if k.lower() in exceptions:
            continue
        if k not in d1 or d1[k] != v:
            return 0
    return 1


#===============================================================================
