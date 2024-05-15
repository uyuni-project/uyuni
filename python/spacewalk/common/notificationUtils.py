#  pylint: disable=missing-module-docstring,invalid-name
from uyuni.common.rhnConfig import initCFG, CFG
from spacewalk.common.rhnLog import log_debug

# pylint: disable-next=unused-import
from spacewalk.server import rhnSQL

# pylint: disable-next=unused-import
import json

try:
    import xmlrpc.client as xmlrpc_client
except ImportError:
    import xmlrpclib as xmlrpc_client

# see TaskoXmlRpcHandler.java for available methods
TASKOMATIC_XMLRPC_URL = "http://localhost:2829/RPC2"


def getNotificationsTypeDisabled():
    """Return list of types which are disabled"""
    disabledTypes = []
    comp = CFG.getComponent()
    initCFG("java")
    if CFG.notifications_type_disabled:
        disabledTypes = CFG.notifications_type_disabled.split(",")
    initCFG(comp)
    return disabledTypes


# pylint: disable-next=missing-class-docstring
class CreateBootstrapRepoFailed:
    def __init__(self, ident, detail=""):
        self.identifier = ident
        self.details = detail
        self.type = "CreateBootstrapRepoFailed"

    def store(self):
        if self.type in getNotificationsTypeDisabled():
            return

        return self._create_bootstrap_repo_failed_notification()

    def _create_bootstrap_repo_failed_notification(self):
        client = xmlrpc_client.Server(TASKOMATIC_XMLRPC_URL)
        log_debug(
            2,
            # pylint: disable-next=consider-using-f-string
            "Calling createBootstrapRepoFailedNotification({0}, {1})".format(
                self.identifier, self.details
            ),
        )
        return client.tasko.createBootstrapRepoFailedNotification(
            self.identifier, self.details
        )
