from spacewalk.common.rhnConfig import initCFG, CFG  #  pylint: disable=missing-module-docstring,invalid-name
from spacewalk.common.rhnLog import log_debug
from spacewalk.server import rhnSQL  #  pylint: disable=unused-import
import json  #  pylint: disable=unused-import

try:
    import xmlrpc.client as xmlrpc_client
except ImportError:
    import xmlrpclib as xmlrpc_client

# see TaskoXmlRpcHandler.java for available methods
TASKOMATIC_XMLRPC_URL = "http://localhost:2829/RPC2"


def getNotificationsTypeDisabled():  #  pylint: disable=invalid-name
    """Return list of types which are disabled"""
    disabledTypes = []  #  pylint: disable=invalid-name
    comp = CFG.getComponent()
    initCFG("java")
    if CFG.notifications_type_disabled:
        disabledTypes = CFG.notifications_type_disabled.split(",")  #  pylint: disable=invalid-name
    initCFG(comp)
    return disabledTypes


class CreateBootstrapRepoFailed:  #  pylint: disable=missing-class-docstring
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
            "Calling createBootstrapRepoFailedNotification({0}, {1})".format(  #  pylint: disable=consider-using-f-string
                self.identifier, self.details
            ),
        )
        return client.tasko.createBootstrapRepoFailedNotification(
            self.identifier, self.details
        )
