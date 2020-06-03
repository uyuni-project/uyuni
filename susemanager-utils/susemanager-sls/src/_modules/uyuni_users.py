# coding: utf-8
from typing import Any, Dict, List, Optional
import ssl
import xmlrpc.client  # type: ignore
import logging
import datetime

log = logging.getLogger(__name__)

__pillar__: Dict[str, Any] = {}
__context__: Dict[str, Any] = {}
__virtualname__: str = "uyuni"


class UyuniUsersException(Exception):
    """
    Uyuni users Exception
    """


class UyuniChannelsException(Exception):
    """
    Uyuni channels Exception
    """


class RPCClient:
    """
    RPC Client
    """

    def __init__(self, user: str = None, password: str = None, url: str = "https://localhost/rpc/api"):
        """
        XML-RPC client interface.

        :param user: username for the XML-RPC API endpoints
        :param password: password credentials for the XML-RPC API endpoints
        :param url: URL of the remote host
        """

        ctx: ssl.SSLContext = ssl.create_default_context()
        ctx.check_hostname = False
        ctx.verify_mode = ssl.CERT_NONE

        self.conn = xmlrpc.client.ServerProxy(url, context=ctx, use_datetime=True, use_builtin_types=True)
        if user is None or password is None:
            # if user or password not set, fallback to default user defined on pillar data
            if "xmlrpc" in (__pillar__ or {}).get("uyuni", {}):
                rpc_conf = (__pillar__ or {})["uyuni"]["xmlrpc"] or {}
                self._user: str = rpc_conf.get("user", "")
                self._password: str = rpc_conf.get("password", "")
            else:
                raise UyuniUsersException("Unable to find Pillar configuration for Uyuni XML-RPC API")
        else:
            self._user: str = user
            self._password: str = password

        self.token: Optional[str] = None

    def get_user(self):
        return self._user

    def get_token(self, refresh: bool = False) -> Optional[str]:
        """
        Authenticate.
        If a authentication token is present on __context__ it will be returned
        Otherwise get an ew authentication token from xml rpc.
        If refresh parameter where set to True, it will get a new token from the API

        :param refresh: force token to the refreshed, cached values
        :return: authentication token
        """
        if self.token is None or refresh:
            try:
                auth_token_key = "uyuni.auth_token_" + self._user
                if (not auth_token_key in __context__) or refresh:
                    __context__[auth_token_key] = self.conn.auth.login(self._user, self._password)
            except Exception as exc:
                log.error("Unable to login to the Uyuni server: %s", exc)
                raise exc
            self.token = __context__[auth_token_key]
        return self.token

    def __call__(self, method: str, *args, **kwargs) -> Any:
        self.get_token()
        if self.token is not None:
            try:
                log.debug("Calling RPC method %s", method)
                return getattr(self.conn, method)(*((self.token,) + args))
            except Exception as exc:
                if exc.faultCode != 2950:
                    raise exc
                """
                Authentication error when using Token, it can have expired.
                Call a second time with a new session token
                """
                log.warning("Fall back to the second try due to %s", str(exc))
                try:
                    return getattr(self.conn, method)(*((self.get_token(refresh=True),) + args))
                except Exception as exc:
                    log.error("Unable to call RPC function: %s", str(exc))
                    raise exc

        raise UyuniUsersException("XML-RPC backend authentication error.")


class UyuniRemoteObject:
    """
    RPC client
    """

    def __init__(self, user: str = None, password: str = None):
        self.client: RPCClient = RPCClient(user=user, password=password)

    @staticmethod
    def _convert_datetime_str(response: Dict[str, Any]) -> Dict[str, Any]:
        """
        modify any key-value pair where value is a datetime object to a string.

        :param response: response dictionary to be processed
        :return: new dictionary with datetime objects converted to sting
        """
        if response:
            return dict(
                [
                    (k, "{0}".format(v)) if isinstance(v, datetime.datetime) else (k, v)
                    for k, v in response.items()
                ]
            )
        return None

    @staticmethod
    def _convert_datetime_list(response: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        modify any list of key-value pair where value is a datetime object to a string.
        :param response: list of dictionaries to be processed
        :return: List of new dictionaries with datetime objects converted to sting
        """
        if response:
            return [UyuniRemoteObject._convert_datetime_str(value) for value in response]
        return None
