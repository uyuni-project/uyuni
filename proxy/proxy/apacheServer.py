# apacheServer.py      - Apache XML-RPC server for mod_python (Spacewalk).
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

# common module imports
from spacewalk.common.rhnConfig import CFG, initCFG
from spacewalk.common.rhnLog import initLOG, log_setreq, log_debug
from spacewalk.common.rhnTB import Traceback
from spacewalk.common import apache


class HandlerWrap:

    """ Wrapper handlers to catch unwanted exceptions """
    svrHandlers = None

    def __init__(self, name, init=0):
        self.__name = name
        # Flag: should we initialize the config and logging components?
        self.__init = init

    def __call__(self, req):
        # NOTE: all imports done here due to required initialization of
        #       of the configuration module before all others.
        #       Initialization is dependent on RHNComponentType in the
        #       req object.

        if self.__init:
            from apacheHandler import getComponentType
            # We cannot trust the config files to tell us if we are in the
            # broker or in the redirect because we try to always pass
            # upstream all requests
            componentType = getComponentType(req)
            initCFG(componentType)
            initLOG(CFG.LOG_FILE, CFG.DEBUG)
            log_debug(1, 'New request, component %s' % (componentType, ))

        # Instantiate the handlers
        if HandlerWrap.svrHandlers is None:
            HandlerWrap.svrHandlers = self.get_handler_factory(req)()

        if self.__init:
            # Set the component type
            HandlerWrap.svrHandlers.set_component(componentType)

        try:
            log_setreq(req)
            if hasattr(HandlerWrap.svrHandlers, self.__name):
                f = getattr(HandlerWrap.svrHandlers, self.__name)
                ret = f(req)
            else:
                raise Exception("Class has no attribute %s" % self.__name)
        # pylint: disable=W0702
        except:
            Traceback(self.__name, req, extra="Unhandled exception type",
                      severity="unhandled")
            return apache.HTTP_INTERNAL_SERVER_ERROR
        else:
            return ret

    @staticmethod
    def get_handler_factory(_req):
        """ Handler factory. Redefine in your subclasses if so choose """
        from apacheHandler import apacheHandler
        return apacheHandler


# Instantiate external entry points:
HeaderParserHandler = HandlerWrap("headerParserHandler", init=1)
Handler = HandlerWrap("handler")
CleanupHandler = HandlerWrap("cleanupHandler")
LogHandler = HandlerWrap("logHandler")
