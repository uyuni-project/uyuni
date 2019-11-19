# coding: utf-8
from typing import Any, Dict, List, Optional, Union, Tuple
import ssl
import xmlrpc.client  # type: ignore
import logging


log = logging.getLogger(__name__)

__pillar__: Dict[str, Any] = {}
__context__: Dict[str, Any] = {}
__virtualname__: str = "uyuni"


class UyuniUsersException(Exception):
    """
    Uyuni users Exception
    """


class RPCClient:
    """
    RPC Client
    """
    __instance__: Optional["RPCClient"] = None

    def __init__(self, url: str, user: str, password: str):
        """
        XML-RPC client interface.

        :param url: URL of the remote host
        :param user: username for the XML-RPC endpoints
        :param password: password credentials for the XML-RPC endpoints
        """
        if self.__instance__ is not None:
            raise UyuniUsersException("Object already instantiated. Use init() method instead.")

        ctx: ssl.SSLContext = ssl.create_default_context()
        ctx.check_hostname = False
        ctx.verify_mode = ssl.CERT_NONE

        self.conn = xmlrpc.client.ServerProxy(url, context=ctx)
        self._user: str = user
        self._password: str = password
        self.token: Optional[str] = None

    @staticmethod
    def init(ext_pillar: Optional[Dict[str, Any]] = None):
        """
        Create new instance

        :return:
        """
        if RPCClient.__instance__ is None:
            plr: Optional[Dict[str, Any]] = __pillar__ or {}
            if "xmlrpc" not in (plr or {}).keys():
                plr = ext_pillar

            if "xmlrpc" in (plr or {}).get("uyuni", {}):
                rpc_conf = (plr or {})["uyuni"]["xmlrpc"] or {}
                RPCClient.__instance__ = RPCClient(rpc_conf.get("url", "https://localhost/rpc/api"),
                                                   rpc_conf.get("user", ""), rpc_conf.get("password", ""))
            else:
                raise UyuniUsersException("Unable to find Pillar configuration for Uyuni XML-RPC API")

        return RPCClient.__instance__

    def get_token(self, refresh: bool = False) -> Optional[str]:
        """
        Authenticate.

        :return:
        """
        if self.token is None or refresh:
            try:
                self.token = self.conn.auth.login(self._user, self._password)
            except Exception as exc:
                log.error("Unable to login to the Uyuni server: %s", exc)

        return self.token

    def __call__(self, method: str, *args, **kwargs):
        self.get_token()
        if self.token is not None:
            try:
                return getattr(self.conn, method)(*args)
            except Exception as exc:
                log.debug("Fall back to the second try due to %s", exc)
                self.get_token(refresh=True)
                try:
                    return getattr(self.conn, method)(*args)
                except Exception as exc:
                    log.error("Unable to call RPC function: %s", exc)
                    raise UyuniUsersException(exc)

        raise UyuniUsersException("XML-RPC backend authentication error.")

