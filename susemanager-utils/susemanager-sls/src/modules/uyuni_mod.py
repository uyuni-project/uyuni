# coding: utf-8
from typing import Any, Dict, List, Optional, Union, Tuple
import ssl
import xmlrpc.client  # type: ignore
import logging


log = logging.getLogger(__name__)


class UyuniUsersException(Exception):
    """
    Uyuni users Exception
    """


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

