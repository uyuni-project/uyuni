# pylint: disable=missing-module-docstring,invalid-name
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
#
# This module provides a response context for use by the proxy broker
# and redirect components.  This context provides a stackable set of
# response, header, and connection sets which can be used to easily maintain
# the proxy's response state in the event of redirects.

CXT_RESP_HEADERS = "headers"
CXT_RESP_BODYFD = "bodyFd"
CXT_CONNECTION = "connection"


class ResponseContext:
    """This class provides a response context for use by the proxy broker
    and redirect components.  This context provides a stackable set of
    response, header, and connection sets which can be used to easily maintain
    the proxy's response state in the event of redirects."""

    # Constructors and Destructors ############################################

    def __init__(self):
        # pylint: disable-next=invalid-name
        self._contextStack = []
        self.add()

    # Public Interface ########################################################

    # pylint: disable-next=invalid-name
    def getHeaders(self):
        """Get the current response headers."""
        return self._getCurrentContext()[CXT_RESP_HEADERS]

    # pylint: disable-next=invalid-name,invalid-name
    def setHeaders(self, responseHeaders):
        """Set the current response headers."""
        self._getCurrentContext()[CXT_RESP_HEADERS] = responseHeaders

    # pylint: disable-next=invalid-name
    def getBodyFd(self):
        """Get the current response body file descriptor."""
        return self._getCurrentContext()[CXT_RESP_BODYFD]

    # pylint: disable-next=invalid-name,invalid-name
    def setBodyFd(self, responseBodyFd):
        """Set the current response body file descriptor."""
        self._getCurrentContext()[CXT_RESP_BODYFD] = responseBodyFd

    # pylint: disable-next=invalid-name
    def getConnection(self):
        """Get the current connection object."""
        return self._getCurrentContext()[CXT_CONNECTION]

    # pylint: disable-next=invalid-name
    def setConnection(self, connection):
        """Set the current connection object."""
        self._getCurrentContext()[CXT_CONNECTION] = connection

    def add(self):
        """Add a new context to the stack. The new context becomes the current
        one.
        """
        self._contextStack.append(self._createContext())

    def remove(self):
        """Remove the current context."""
        if not self._isEmpty():
            self.close()
            self._contextStack.pop()

    def close(self):
        """Close the current context."""
        context = self._getCurrentContext()
        self._closeContext(context)

    def clear(self):
        """Close and remove all contexts."""
        while self._contextStack:
            self.remove()

    def __str__(self):
        """String representation."""
        return str(self._contextStack)

    # Helper Methods ##########################################################

    # pylint: disable-next=invalid-name
    def _isEmpty(self):
        return len(self._contextStack) <= 0

    @staticmethod
    # pylint: disable-next=invalid-name
    def _closeContext(context):
        if context:
            if context[CXT_RESP_BODYFD]:
                context[CXT_RESP_BODYFD].close()
            if context[CXT_CONNECTION]:
                context[CXT_CONNECTION].close()

    # pylint: disable-next=invalid-name
    def _getCurrentContext(self):
        return self._contextStack[-1]

    @staticmethod
    # pylint: disable-next=invalid-name,invalid-name,invalid-name
    def _createContext(responseHeaders=None, responseBodyFd=None, connection=None):
        return {
            CXT_RESP_HEADERS: responseHeaders,
            CXT_RESP_BODYFD: responseBodyFd,
            CXT_CONNECTION: connection,
        }


###############################################################################
# Test Routine
###############################################################################

if __name__ == "__main__":
    respContext = ResponseContext()
    print("init   | context = " + str(respContext))

    respContext.remove()
    print("remove | context = " + str(respContext))

    respContext.add()
    print("add    | context = " + str(respContext))

    respContext.remove()
    print("remove | context = " + str(respContext))

    respContext.add()
    respContext.add()
    print("addadd | context = " + str(respContext))

    respContext.clear()
    print("clear  | context = " + str(respContext))

    respContext.add()
    print("add    | context = " + str(respContext))
