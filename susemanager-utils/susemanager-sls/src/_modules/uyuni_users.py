# coding: utf-8
from typing import Any, Dict, List, Optional, Union, Tuple
import ssl
import xmlrpc.client  # type: ignore
import logging

import os
import salt.config
from salt.utils.minions import CkMinions
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


class UyuniUser(UyuniRemoteObject):
    """
    CRUD operation on users.
    """

    def get_details(self, uid: str) -> Dict[str, Any]:
        """
        Get existing user data from the Uyuni.

        :param: uid: user name to lookup
        :return: Dictionary with user details
        """
        log.debug("get user details: %s", uid)
        return self.client("user.getDetails", uid)

    def list_users(self) -> List[Dict[str, Any]]:
        """
        Return all Uyuni users.
        Uyuni XML-RPC listUsers return all users that are visible for the authenticated user.
        This could be a sub-set of all existing users.

        :return: all users visible to the authenticated user
        """
        log.debug("list existing users")
        return self.client("user.listUsers")

    def create(self, uid: str, password: str, email: str, first_name: str = "", last_name: str = "") -> bool:
        """
        Create user in Uyuni.
        User will be created in the same organization as the authenticated user.

        :param uid: desired login name
        :param password: desired password for the user
        :param email: valid email address
        :param first_name: First name
        :param last_name: Second name

        :return: True on success, raise exception otherwise
        """
        log.debug("Adding user to Uyuni: %s", uid)
        return bool(self.client("user.create", uid, password, first_name, last_name, email))

    def set_details(self, uid: str, password: str, email: str, first_name: str = "", last_name: str = "") -> bool:
        """
        Update user information on Uyuni.

        :param uid: login name
        :param password: desired password for the user
        :param email: valid email address
        :param first_name: First name
        :param last_name: Second name

        :return: True on success, raise exception otherwise
        """
        log.debug("Updating user to Uyuni: %s", uid)
        return bool(self.client("user.setDetails", uid, {
            "password": password,
            "first_name": first_name,
            "last_name": last_name,
            "email": email
        }))

    def delete(self, uid: str) -> bool:
        """
        Remove user from the Uyuni.

        :param uid: UID of the user
        :return: boolean, True if user has been deleted successfully.
        """
        log.debug("delete user: %s", uid)
        return bool(self.client("user.delete", uid))

    def list_roles(self, uid: str) -> List[str]:
        """
        Get existing user data from the Uyuni.

        :param: uid: user name to use on lookup
        :return: list of user roles
        """
        log.debug("get user roles: %s", uid)
        return self.client("user.listRoles", uid)

    def add_role(self, uid: str, role: str) -> bool:
        """
        Add role to user

        :param uid: UID of the user
        :param role: one of uyuni user roles

        :return: boolean, True if role has been added successfully.
        """
        log.debug("add role '%s' to user %s", role, uid)
        return bool(self.client("user.addRole", uid, role))

    def remove_role(self, uid: str, role: str) -> bool:
        """
        Remove user from the Uyuni org.

        :param uid: UID of the user
        :param role: one of uyuni user roles

        :return: boolean, True if role has been removed successfully.
        """
        log.debug("remove role '%s' to user %s", role, uid)
        return bool(self.client("user.removeRole", uid, role))


def __virtual__():
    """
    Provide Uyuni Users state module.

    :return:
    """

    return __virtualname__


def user_get_details(uid, password=None, org_admin_user=None, org_admin_password=None):
    """
    Get user in Uyuni.
    If user password is provided name and password fields are use to authenticate
    If no user credentials are provided, organization administrator credentials will be used
    If no user credentials neither organization admin credentials are provided, credentials from pillar will be used

    :param uid: user id to look for
    :param password: password for the user
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password
    :return: The user information
    """
    return UyuniUser(org_admin_user if password is None else uid,
                     org_admin_password if password is None else password).get_details(uid=uid)


def user_list_users(org_admin_user=None, org_admin_password=None):
    """
    Get user in Uyuni.
    If no organization admin credentials are provided, credentials from pillar are used

    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password
    :return: list of user roles
    """
    return UyuniUser(org_admin_user, org_admin_password).list_users()


def user_create(uid, password, email, first_name=None, last_name=None,
                org_admin_user=None, org_admin_password=None):
    """
    Create user in Uyuni.
    If no organization admin credentials are provided, credentials from pillar are used

    :param uid: user id to look for
    :param password: password for the user
    :param email: user email address
    :param first_name: user first name
    :param last_name: user last name
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password
    :return: boolean indication success in operation
    """
    return UyuniUser(org_admin_user, org_admin_password).create(uid=uid, password=password, email=email,
                                                                first_name=first_name, last_name=last_name)


def user_set_details(uid, password, email, first_name=None, last_name=None,
                     org_admin_user=None, org_admin_password=None):
    """
    Update user in Uyuni.
    If no organization admin credentials are provided, credentials from pillar are used

    :param uid: user id to look for
    :param password: password for the user
    :param email: user email address
    :param first_name: user first name
    :param last_name: user last name
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password
    :return: boolean indication success in operation
    """
    return UyuniUser(org_admin_user, org_admin_password).set_details(uid=uid, password=password, email=email,
                                                                     first_name=first_name, last_name=last_name)


def user_delete(uid, org_admin_user=None, org_admin_password=None):
    """
    Create user in Uyuni.
    If no organization admin credentials are provided, credentials from pillar are used

    :param uid: user id to look for
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password
    :return: boolean indication success in operation
    """
    return UyuniUser(org_admin_user, org_admin_password).delete(uid=uid)


def user_list_roles(uid, password=None, org_admin_user=None, org_admin_password=None):
    """
    Get user roles in Uyuni.
    If user password is provided name and password fields are use to authenticate
    If no user credentials are provided, organization administrator credentials will be used
    If no user credentials neither organization admin credentials are provided, credentials from pillar are used

    :param uid: user id to look for
    :param password: password for the user
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password
    :return: List of user roles assigned
    """
    return UyuniUser(org_admin_user if password is None else uid,
                     org_admin_password if password is None else password).list_roles(uid=uid)


def user_add_role(uid, role, org_admin_user=None, org_admin_password=None):
    """
    Add role to user in Uyuni.
    If no organization admin credentials are provided, credentials from pillar are used

    :param uid: user id to look for
    :param role: role to be added to the user
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password
    :return: boolean indication success in operation
    """
    return UyuniUser(org_admin_user, org_admin_password).add_role(uid=uid, role=role)


def user_remove_role(uid, role, org_admin_user=None, org_admin_password=None):
    """
    Remove role to user in Uyuni.
    If no organization admin credentials are provided, credentials from pillar are used

    :param uid: user id to look for
    :param role: role to be removed from the user
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password
    :return: boolean indication success in operation
    """
    return UyuniUser(org_admin_user, org_admin_password).remove_role(uid=uid, role=role)

