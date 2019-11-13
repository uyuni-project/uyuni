# coding: utf-8
"""
Uyuni users state module
"""
from typing import Any, Dict, Optional
import ssl
import xmlrpc.client  # type: ignore
import logging


log = logging.getLogger(__name__)
__pillar__: Dict[str, Any] = {}
__virtualname__: str = "uyuni"


class RPCClient:
    """
    RPC Client
    """
    def __init__(self, url: str, user: str, password: str):
        """
        XML-RPC client interface.

        :param url: URL of the remote host
        :param user: username for the XML-RPC endpoints
        :param password: password credentials for the XML-RPC endpoints
        """
        ctx: ssl.SSLContext = ssl.create_default_context()
        ctx.check_hostname = False
        ctx.verify_mode = ssl.CERT_NONE

        self.conn = xmlrpc.client.ServerProxy(url, context=ctx)
        self._user: str = user
        self._password: str = password
        self.token: Optional[str] = None

    def get_token(self, refresh=False):
        """
        Authenticate.

        :return:
        """
        if self.token is None or refresh:
            self.token = self.conn.auth.login(self._user, self._password)

        return self.token

    def __call__(self, method, *args, **kwargs):
        self.get_token()
        try:
            return getattr(self.conn, method)(*args, **kwargs)
        except Exception as exc:
            err = [str(exc)]
            log.debug("Fall back to the second try due to %s", exc)
            self.get_token(refresh=True)
            try:
                getattr(self.conn, method, *args, **kwargs)
            except Exception as exc:
                log.error("Unable to call RPC function: %s", exc)
                err.append(exc)
        return err


class UyuniUsers:
    """
    Uyuni users operations.
    """
    def __init__(self, client: RPCClient):
        self.client = client

    def create_user(self, uid, password, email, first_name="", last_name=""):
        """
        Create user in Uyuni.

        :param uid: desired login name, safe to use if login name is already in use
        :param password: desired password for the user
        :param email: valid email address
        :param first_name: First name
        :param last_name: Second name

        :return: boolean
        """
        log.debug("Adding user to Uyuni")
        if not email:
            ret = 0
            log.debug("Not all parameters has been specified")
            log.error("Email should be specified when create user")
        else:
            ret = self.client("user.create", self.client.get_token(), uid, password, first_name, last_name, email)
            log.debug("User has been created")

        return bool(ret)

    def manage(self, name: str, password: str, email: str, first_name: str = "", last_name: str = "",
               org: str = "", roles: Optional[List[str]] = None) -> Dict[str, Any]:
        """
        Manage user with the data. If anything is updated,

        :param name: UID for the user
        :param password: Password for the user
        :param email: Email of the user
        :param first_name: First name (optional)
        :param last_name: Second name (options)

        :return:
        """
        ret = {
            'name': name,
            'changes': {},
            'result': None,
            'comment': ""
        }

        existing_user = None
        for user in self.client("user.listUsers", self.client.get_token()):
            if user.get("login") == name:
                existing_user = user
                break

        if existing_user is None:
            ret["changes"] = {"uid": name, "password": password, "email": email,
                              "first_name": first_name, "last_name": last_name}
            ret["comment"] = "Added new user {}".format(name)
            self.create_user(**ret["changes"])
            ret["changes"]["password"] = "(hidden)"
        else:
            ret["comment"] = "No changes has been done"

        return ret


__rpc: Optional[RPCClient] = None


def __virtual__():
    """
    Provide Uyuni Users state module.

    :return:
    """
    global __rpc
    if __rpc is None and "xmlrpc" in __pillar__.get("uyuni", {}):
        rpc_conf = __pillar__["uyuni"]["xmlrpc"] or {}
        __rpc = RPCClient(rpc_conf.get("url", "https://localhost/rpc/api"),
                          rpc_conf.get("user", ""), rpc_conf.get("password"))

    return __virtualname__ if __rpc is not None else False


# Salt exported API


def user_present(name, password, email, first_name, last_name, org="", roles=None):
    """
    Ensure Uyuni user present.

    :param name: Uyuni user name
    :param password: Password (should be empty in case of PAM auth)
    :param email: Email for the user
    :param first_name: First name
    :param last_name: Second (last) name
    :param org: Organisation name
    :param roles: List of Uyuni roles that user has to have.

    :raises: UyuniException if roles aren't correctly spelt.
    :return: dictionary for Salt communication protocol
    """
    return UyuniUsers(__rpc).manage(name=name, password=password, email=email, first_name=first_name,
                                    last_name=last_name, org=org, roles=roles)
