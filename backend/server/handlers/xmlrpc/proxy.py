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

# system module import
import time

from rhn.UserDictCase import UserDictCase

# common module imports
from rhn.UserDictCase import UserDictCase
from spacewalk.common import rhnFlags
from spacewalk.common.rhnLog import log_debug, log_error
from spacewalk.common.rhnConfig import CFG
from spacewalk.common.rhnException import rhnFault
from spacewalk.common.rhnTranslate import _

# local module imports
from spacewalk.server.rhnLib import computeSignature
from spacewalk.server.rhnHandler import rhnHandler
from spacewalk.server import rhnServer, rhnSQL, apacheAuth, rhnPackage, rhnChannel

# a class that provides additional authentication support for the
# proxy functions


class rhnProxyHandler(rhnHandler):

    def __init__(self):
        rhnHandler.__init__(self)

    def auth_system(self, system_id):
        """ System authentication. We override the standard function because
            we need to check additionally if this system_id is entitled for
            proxy functionality.
        """
        log_debug(3)
        server = rhnHandler.auth_system(self, system_id)
        # if it did not blow up, we have a valid server. Check proxy
        # entitlement.
        # XXX: this needs to be moved out of the rhnServer module,
        # possibly in here
        h = rhnSQL.prepare("""
        select 1
        from rhnProxyInfo pi
        where pi.server_id = :server_id
        """)
        h.execute(server_id=self.server_id)
        row = h.fetchone_dict()
        if not row:
            # we require entitlement for this functionality
            log_error("Server not entitled for Proxy", self.server_id)
            raise rhnFault(1002, _(
                'SUSE Manager Proxy service not enabled for server profile: "%s"')
                % server.server["name"])
        # we're fine...
        return server

    def auth_client(self, token):
        """ Authenticate a system based on the same authentication tokens
            the client is sending for GET requests
        """
        log_debug(3)
        # Build a UserDictCase out of the token
        dict = UserDictCase(token)
        # Set rhnFlags so that we can piggyback on apacheAuth's auth_client
        rhnFlags.set('AUTH_SESSION_TOKEN', dict)

        # XXX To clean up apacheAuth.auth_client's logging, this is not about
        # GET requests
        result = apacheAuth.auth_client()

        if not result:
            raise rhnFault(33, _("Invalid session key"))

        log_debug(4, "Client auth OK")
        # We checked it already, so we're sure it's there
        client_id = dict['X-RHN-Server-Id']

        server = rhnServer.search(client_id)
        if not server:
            raise rhnFault(8, _("This server ID no longer exists"))
        # XXX: should we check if the username still has access to it?
        # probably not, because there is no known good way we can
        # update the server system_id on the client side when
        # permissions change... Damn it. --gafton
        self.server = server
        self.server_id = client_id
        self.user = dict['X-RHN-Auth-User-Id']
        return server


class Proxy(rhnProxyHandler):

    """ this is the XML-RPC receiver for proxy calls """

    def __init__(self):
        log_debug(3)
        rhnProxyHandler.__init__(self)
        self.functions.append('package_source_in_channel')
        self.functions.append('login')
        self.functions.append('listAllPackagesKickstart')
        self.functions.append('getKickstartChannel')
        self.functions.append('getKickstartOrgChannel')
        self.functions.append('getKickstartSessionChannel')
        self.functions.append('getKickstartChildChannel')
        self.functions.append('getTinyUrlChannel')
        self.functions.append('checkTokenValidity')

    # Method to force a check of the client's auth token.
    # Proxy may call this if it does not recognize the token, which may
    # happen if the proxy is load-balanced.
    def checkTokenValidity(self, token, systemid):
        log_debug(5, token, systemid)
        # authenticate that this request is initiated from a proxy
        try:
            self.auth_system(systemid)
            server = self.auth_client(token) # sets self.server_id
        except rhnFault:
            # A Fault means that something did not auth. Either the caller
            # is not a proxy or the token is not valid, return false.
            return False
        # Proxy has to calculate new proxy-clock-skew, and needs channel info
        ret = {}
        ret['X-RHN-Auth-Server-Time'] = str(time.time())
        channels = rhnChannel.getSubscribedChannels(self.server_id)
        ret['X-RHN-Auth-Channels'] = channels
        return ret

    def package_source_in_channel(self, package, channel, auth_token):
        """ Validates the client request for a source package download """
        log_debug(3, package, channel)
        server = self.auth_client(auth_token)
        return rhnPackage.package_source_in_channel(self.server_id,
                                                    package, channel)

    def login(self, system_id):
        """ Login routine for the proxy

            Return a formatted string of session token information as regards
            an Spacewalk Proxy.  Also sets this information in the headers.

            NOTE: design description for the auth token format and how it is
               is used is well documented in the proxy/broker/rhnProxyAuth.py
               code.
        """
        log_debug(5, system_id)
        # Authenticate. We need the user record to be able to generate
        # auth tokens
        self.load_user = 1
        self.auth_system(system_id)
        # log the entry
        log_debug(1, self.server_id)
        rhnServerTime = str(time.time())
        expireOffset = str(CFG.PROXY_AUTH_TIMEOUT)
        signature = computeSignature(CFG.SECRET_KEY, self.server_id, self.user,
                                     rhnServerTime, expireOffset)

        token = '%s:%s:%s:%s:%s' % (self.server_id, self.user, rhnServerTime,
                                    expireOffset, signature)

        # NOTE: for RHN Proxies of version 3.1+ tokens are passed up in a
        #       multi-valued header with HOSTNAME tagged onto the end of the
        #       token, so, it looks something like this:
        #           x-rhn-proxy-auth: 'TOKEN1:HOSTNAME1,TOKEN2:HOSTNAME2'
        #       This note is only that -- a "heads up" -- in case anyone gets
        #       confused.

        # Push this value into the headers so that the proxy can
        # intercept and cache it without parsing the xmlrpc.
        transport = rhnFlags.get('outputTransportOptions')
        transport['X-RHN-Action'] = 'login'
        transport['X-RHN-Proxy-Auth'] = token
        return token

    def listAllPackagesKickstart(self, channel, system_id):
        """ Creates and/or serves up a cached copy of all the packages for
        this channel, including checksum information.
        """
        log_debug(5, channel)
        # authenticate that this request is initiated from a proxy
        self.auth_system(system_id)

        packages = rhnChannel.list_all_packages_checksum(channel)

        # transport options...
        rhnFlags.set("compress_response", 1)
        return packages

    def getKickstartChannel(self, kickstart, system_id):
        """ Gets channel information for this kickstart tree"""
        log_debug(5, kickstart)
        # authenticate that this request is initiated from a proxy
        self.auth_system(system_id)
        return self.__getKickstartChannel(kickstart)

    def getKickstartOrgChannel(self, kickstart, org_id, system_id):
        """ Gets channel information for this kickstart tree"""
        log_debug(5, kickstart, org_id)
        # authenticate that this request is initiated from a proxy
        self.auth_system(system_id)
        ret = rhnChannel.getChannelInfoForKickstartOrg(kickstart, org_id)
        return self.__getKickstart(kickstart, ret)

    def getKickstartSessionChannel(self, kickstart, session, system_id):
        """ Gets channel information for this kickstart tree"""
        log_debug(5, kickstart, session)
        # authenticate that this request is initiated from a proxy
        self.auth_system(system_id)
        return self.__getKickstartSessionChannel(kickstart, session)

    def getKickstartChildChannel(self, kickstart, child, system_id):
        """ Gets channel information for this kickstart tree"""
        log_debug(5, kickstart, child)
        # authenticate that this request is initiated from a proxy
        self.auth_system(system_id)
        if (hasattr(CFG, 'KS_RESTRICT_CHILD_CHANNELS') and
                CFG.KS_RESTRICT_CHILD_CHANNELS):
            return getKickstartChannel(kickstart)

        ret = rhnChannel.getChildChannelInfoForKickstart(kickstart, child)
        return self.__getKickstart(kickstart, ret)

    def getTinyUrlChannel(self, tinyurl, system_id):
        """ Gets channel information for this tinyurl"""
        log_debug(5, tinyurl)
        # authenticate that this request is initiated from a proxy
        self.auth_system(system_id)
        ret = rhnChannel.getChannelInfoForTinyUrl(tinyurl)
        if not ret or not 'url' in ret or len(ret['url'].split('/')) != 6:
            raise rhnFault(40,
                           "could not find any data on tiny url '%s'" % tinyurl)

        # tiny urls are always for kickstart sessions
        args = ret['url'].split('/')
        return self.__getKickstartSessionChannel(args[-1], args[-2])


#-----------------------------------------------------------------------------

    def __getKickstartChannel(self, kickstart):
        ret = rhnChannel.getChannelInfoForKickstart(kickstart)
        return self.__getKickstart(kickstart, ret)

    def __getKickstartSessionChannel(self, kickstart, session):
        ret = rhnChannel.getChannelInfoForKickstartSession(session)

        if not ret:
            return self.__getKickstartChannel(kickstart)
        return self.__getKickstart(kickstart, ret)

    def __getKickstart(self, kickstart, ret):
        if not ret:
            raise rhnFault(40,
                           "could not find any data on kickstart '%s'" % kickstart)
        return ret
