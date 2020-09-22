# coding: utf-8
from typing import Any, Dict, List, Optional, Union, Tuple
import ssl
import xmlrpc.client  # type: ignore
import logging

import os
import salt.config
from salt.utils.minions import CkMinions
import datetime

AUTHENTICATION_ERROR = 2950

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
        Otherwise get a new authentication token from xml rpc.
        If refresh is True, get a new token from the API regardless of prior status.

        :param refresh: force token refresh, discarding any cached value
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
                if exc.faultCode != AUTHENTICATION_ERROR:
                    log.error("Unable to call RPC function: %s", str(exc))
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

    @staticmethod
    def _convert_bool_response(response: int):
        return response == 1

class UyuniUser(UyuniRemoteObject):
    """
    CRUD operation on users.
    """

    def get_details(self, login: str) -> Dict[str, Any]:
        """
        Retrieve details of an Uyuni user.

        :param: login: user name to lookup

        :return: Dictionary with user details
        """
        return self.client("user.getDetails", login)

    def list_users(self) -> List[Dict[str, Any]]:
        """
        Return all Uyuni users visible to the authenticated user.

        :return: all users visible to the authenticated user
        """
        return self.client("user.listUsers")

    def create(self, login: str, password: str, email: str, first_name: str = "", last_name: str = "",
               use_pam_auth: bool = False) -> bool:
        """
        Create an Uyuni user.
        User will be created in the same organization as the authenticated user.

        :param login: desired login name
        :param password: desired password for the user
        :param email: valid email address
        :param first_name: First name
        :param last_name: Last name
        :param use_pam_auth: if you wish to use PAM authentication for this user

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("user.create", login, password,
                                                       first_name, last_name, email, int(use_pam_auth)))

    def set_details(self, login: str, password: str, email: str, first_name: str = "", last_name: str = "") -> bool:
        """
        Update an Uyuni user information.

        :param login: login name
        :param password: desired password for the user
        :param email: valid email address
        :param first_name: First name
        :param last_name: Last name

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("user.setDetails", login, {
            "password": password,
            "first_name": first_name,
            "last_name": last_name,
            "email": email
        }))

    def delete(self, login: str) -> bool:
        """
        Remove an Uyuni user.

        :param login: login of the user

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("user.delete", login))

    def list_roles(self, login: str) -> List[str]:
        """
        Return the list of roles of a user.

        :param: login: user name to use on lookup

        :return: list of user roles
        """
        return self.client("user.listRoles", login)

    def add_role(self, login: str, role: str) -> bool:
        """
        Add a role to a user

        :param login: login of the user
        :param role: a new role

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("user.addRole", login, role))

    def remove_role(self, login: str, role: str) -> bool:
        """
        Remove user from the Uyuni org.

        :param login: login of the user
        :param role: one of uyuni user roles

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("user.removeRole", login, role))

    def list_assigned_system_groups(self, login: str) -> List[Dict[str, Union[int, str]]]:
        """
        Returns the system groups that a user can administer.

        :param login: login of the user

        :return: List of system groups that a user can administer
        """
        return self.client("user.listAssignedSystemGroups", login)

    def add_assigned_system_groups(self, login: str, server_group_names: List[str], set_default: bool = False) -> int:
        """
        Add system groups to a user's list of assigned system groups.

        :param login: user id to look for
        :param server_group_names: system groups to add
        :param set_default: True if the system groups should also be added to user's default list.

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("user.addAssignedSystemGroups",
                                                       login, server_group_names, set_default))

    def remove_assigned_system_groups(self, login: str, server_group_names: List[str], set_default: bool = False) -> int:
        """
        Remove system groups from a user's list of assigned system groups

        :param login: user id to look for
        :param server_group_names: systems groups to remove from list of assigned system groups
        :param set_default: True if the system groups should also be removed to user's default list.

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("user.removeAssignedSystemGroups",
                                                       login, server_group_names, set_default))


class UyuniChannel(UyuniRemoteObject):
    def list_manageable_channels(self) -> List[Dict[str, Union[int, str]]]:
        """
        List all software channels that the user is entitled to manage.

        :return: list of manageable channels
        """
        return self.client("channel.listManageableChannels")

    def list_my_channels(self) -> List[Dict[str, Union[int, str]]]:
        """
        List all software channels that the user is entitled to manage.

        :return: list of manageable channels
        """
        return self.client("channel.listMyChannels")


class UyuniChannelSoftware(UyuniRemoteObject):
    def set_user_manageable(self, channel_label: str, login: str, access: bool) -> int:
        """
        Set the manageable flag for a given channel and user.
        If access is set to 'true', this method will give the user manage permissions to the channel.
        Otherwise, that privilege is revoked.

        :param channel_label: label of the channel
        :param login: user login id
        :param access: True if the user should have management access to channel

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("channel.software.setUserManageable",
                                                       channel_label, login, access))

    def set_user_subscribable(self, channel_label: str, login: str, access: bool) -> int:
        """
        Set the subscribable flag for a given channel and user.
        If value is set to 'true', this method will give the user subscribe permissions to the channel.
        Otherwise, that privilege is revoked.

        :param channel_label: label of the channel
        :param login: user login id
        :param access: True if the user should have subscribe permission to the channel

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("channel.software.setUserSubscribable",
                                                       channel_label, login, access))

    def is_user_manageable(self, channel_label: str, login: str) -> bool:
        """
        Returns whether the channel may be managed by the given user.

        :param channel_label: label of the channel
        :param login: user login id

        :return: boolean which indicates if user can manage channel or not
        """
        return self._convert_bool_response(self.client("channel.software.isUserManageable", channel_label, login))

    def is_user_subscribable(self, channel_label: str, login: str) -> bool:
        """
        Returns whether the channel may be subscribed to by the given user.

        :param channel_label: label of the channel
        :param login: user login id

        :return: boolean which indicates if user subscribe the channel or not
        """
        return self._convert_bool_response(self.client("channel.software.isUserSubscribable", channel_label, login))

    def is_globally_subscribable(self, channel_label: str) -> bool:
        """
        Returns whether the channel is globally subscribable on the organization

        :param channel_label: label of the channel

        :return: boolean which indicates if channel is globally subscribable
        """
        return self._convert_bool_response(self.client("channel.software.isGloballySubscribable", channel_label))


class UyuniOrg(UyuniRemoteObject):
    """
    CRUD operations on organizations
    """

    def list_orgs(self) -> Dict[str, Union[int, str, bool]]:
        """
        List all organizations.

        :return: list of all existing organizations
        """
        return self.client("org.listOrgs")

    def get_details(self, name: str) -> Dict[str, Union[int, str, bool]]:
        """
        Get org data by name.

        :param name: organisation name

        :return: organization details
        """
        return self.client("org.getDetails", name)

    def create(self, name: str, org_admin_user: str, org_admin_password: str,
               first_name: str, last_name: str, email: str,
               admin_prefix: str = "Mr.", pam: bool = False) -> Dict[str, Union[str, int, bool]]:
        """
        Create a new Uyuni org.

        :param name: organization name
        :param org_admin_user: organization admin user
        :param org_admin_password: organization admin password
        :param first_name: organization admin first name
        :param last_name: organization admin last name
        :param email: organization admin email
        :param admin_prefix: organization admin prefix
        :param pam:organization admin pam authentication

        :return: dictionary with org information
        """
        return self.client("org.create", name, org_admin_user, org_admin_password, admin_prefix,
                           first_name, last_name, email, pam)

    def delete(self, name: str) -> int:
        """
        Delete an Uyuni org.

        :param name: organization name

        :return: boolean, True indicates success
        """
        org_id = int(self.get_details(name=name).get("id", -1))
        return self._convert_bool_response(self.client("org.delete", org_id))

    def update_name(self, org_id: int, name: str) -> Dict[str, Union[str, int, bool]]:
        """
        Update an Uyuni org name.

        :param org_id: organization internal id
        :param name: new organization name

        :return: organization details
        """
        return self.client("org.updateName", org_id, name)


class UyuniOrgTrust(UyuniRemoteObject):

    def __init__(self, user: str = None, password: str = None):
        UyuniRemoteObject.__init__(self, user, password)
        self._org_manager = UyuniOrg(user, password)

    def list_orgs(self) -> List[Dict[str, Union[str, int]]]:
        """
        List all organizations trusted by the authenticated user organization

        :return: List of organization details
        """
        return self.client("org.trusts.listOrgs")

    def list_trusts(self, org_name: str) -> List[Dict[str, Union[str, int, bool]]]:
        """
        List all trusts for the organization

        :return: list with all organizations and their trust status
        """
        org = self._org_manager.get_details(org_name)
        return self.client("org.trusts.listTrusts", org["id"])

    def add_trust_by_name(self, org_name: str, org_trust: str) -> int:
        """
        Set an organisation as trusted by another

        :param org_name: organization name
        :param org_trust: name of organization to trust

        :return: boolean, True indicates success
        """
        this_org = self._org_manager.get_details(org_name)
        trust_org = self._org_manager.get_details(org_trust)
        return self.add_trust(this_org["id"], trust_org["id"])

    def add_trust(self, org_id: str, org_trust_id: str) -> int:
        """
        Set an organisation as trusted by another

        :param org_id: organization id
        :param org_trust_id: organization id to trust

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("org.trusts.addTrust", org_id, org_trust_id))

    def remove_trust_by_name(self, org_name: str, org_untrust: str) -> int:
        """
        Set an organisation as not trusted by another

        :param org_name: organization name
        :param org_untrust: organization name to untrust

        :return: boolean, True indicates success
        """
        this_org = self._org_manager.get_details(org_name)
        trust_org = self._org_manager.get_details(org_untrust)
        return self.remove_trust(this_org["id"], trust_org["id"])

    def remove_trust(self, org_id: str, org_untrust_id: str) -> int:
        """
        Set an organisation as not trusted by another

        :param org_id: organization id
        :param org_untrust_id: organization id to untrust

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("org.trusts.removeTrust", org_id, org_untrust_id))


class UyuniSystemgroup(UyuniRemoteObject):
    """
    Provides methods to access and modify system groups.
    """

    def get_details(self, name: str) -> Dict[str, Union[int, str]]:
        """
        Retrieve details of a system group.

        :param name: Name of the system group.
        :return: data of the system group.
        """
        return self.client("systemgroup.getDetails", name)

    def create(self, name: str, description: str) -> Dict[str, Union[int, str]]:
        """
        Create a new system group.

        :param name: Name of the system group.
        :param description: Description of the system group.

        :return: data of the system group.
        """
        return self.client("systemgroup.create", name, description)

    def delete(self, name: str) -> int:
        """
        Delete a system group.

        :param name: Name of the system group.

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("systemgroup.delete", name))

    def update(self, name: str, description: str) -> Dict[str, Union[int, str]]:
        """
        Update an existing system group.

        :param name: Name of the system group.
        :param description: Description of the system group.

        :return: data of the system group.
        """
        return self.client("systemgroup.update", name, description)

    def list_systems(self, name: str, minimal: bool = True) -> List[Dict[str, Any]]:
        """
        Get information about systems in a group.

        :param name: Group name
        :param minimal: default True. Only return minimal information about systems, use False to get more details

        :return: List of system information
        """
        return self._convert_datetime_list(
            self.client("systemgroup.listSystemsMinimal" if minimal else "systemgroup.listSystems", name))

    def add_remove_systems(self, name: str, add_remove: bool, system_ids: List[int] = []) -> int:
        """
        Add or remove systems from a system group

        :param name: Group name
        :param add_remove: True to add to the group, False to remove
        :param system_ids: List of system ids to add or remove

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("systemgroup.addOrRemoveSystems", name, system_ids, add_remove))


class UyuniSystems(UyuniRemoteObject):

    def get_minion_id_map(self, refresh: bool = False) -> Dict[str, int]:
        """
        Returns a map from minion ID to Uyuni system ID for all systems a user has access to
        This method caches results, in order to avoid multiple XMLRPC calls.

        :param refresh: Get new data from server, ignoring values in local context cache
        :return: Map between minion ID and system ID of all system accessible by authenticated user
        """
        minions_token_key = "uyuni.minions_id_map_" + self.client.get_user()
        if (not minions_token_key in __context__) or refresh:
            __context__[minions_token_key] = self.client("system.getMinionIdMap")
        return __context__[minions_token_key]


class UyuniActivationKey(UyuniRemoteObject):
    """
    CRUD operations on Activation Keys.
    """

    def get_details(self, id: str) -> Dict[str, Any]:
        """
        Retrieve details of an Uyuni Activation Key.

        :param id: the Activation Key ID

        :return: Dictionary with Activation Key details
        """
        return self.client("activationkey.getDetails", id)

    def delete(self, id: str) -> bool:
        """
        Remove an Uyuni Activation Key.

        :param id: the Activation Key ID

        :return: boolean, True indicates success
        """
        return self._convert_bool_response(self.client("activationkey.delete", id))


class UyuniChildMasterIntegration:
    """
    Integration with the Salt Master which is running
    on the same host as this current Minion.
    """
    DEFAULT_MASTER_CONFIG_PATH = "/etc/salt/master"

    def __init__(self):
        self._minions = CkMinions(salt.config.client_config(self._get_master_config()))

    @staticmethod
    def _get_master_config() -> str:
        """
        Return master config.
        :return: path to salt master configuration file
        """
        cfg_path = UyuniChildMasterIntegration.DEFAULT_MASTER_CONFIG_PATH
        for path in __pillar__.get("uyuni", {}).get("masters", {}).get("configs", [cfg_path]):
            if os.path.exists(path):
                cfg_path = path
                break

        return cfg_path

    def select_minions(self, target: str, target_type: str = "glob") -> Dict[str, Union[List[str], bool]]:
        """
        Select minion IDs that matches the target expression.

        :param target: target expression to be applied
        :param target_type: target type, one of the following: glob, grain, grain_pcre, pillar, pillar_pcre,
                    pillar_exact, compound, compound_pillar_exact. Default: glob.

        :return: list of minions
        """
        return self._minions.check_minions(expr=target, tgt_type=target_type)


def __virtual__():
    """
    Provide Uyuni configuration state module.

    :return:
    """

    return __virtualname__


# Users

def user_get_details(login, password=None, org_admin_user=None, org_admin_password=None):
    """
    Get details of an Uyuni user
    If password is provided as a parameter, then it will be used to authenticate
    If no user credentials are provided, organization administrator credentials will be used
    If no user credentials neither organization admin credentials are provided, credentials from pillar will be used

    :param login: user id to look for
    :param password: password for the user
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: The user information
    """
    return UyuniUser(org_admin_user if password is None else login,
                     org_admin_password if password is None else password).get_details(login)


def user_list_users(org_admin_user=None, org_admin_password=None):
    """
    Return all Uyuni users visible to the authenticated user.

    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: all users visible to the authenticated user
    """
    return UyuniUser(org_admin_user, org_admin_password).list_users()


def user_create(login, password, email, first_name, last_name, use_pam_auth=False,
                org_admin_user=None, org_admin_password=None):
    """
    Create an Uyuni user.

    :param login: user id to look for
    :param password: password for the user
    :param email: user email address
    :param first_name: user first name
    :param last_name: user last name
    :param use_pam_auth: if you wish to use PAM authentication for this user
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: boolean, True indicates success
    """
    return UyuniUser(org_admin_user, org_admin_password).create(login=login, password=password, email=email,
                                                                first_name=first_name, last_name=last_name,
                                                                use_pam_auth=use_pam_auth)


def user_set_details(login, password, email, first_name=None, last_name=None,
                     org_admin_user=None, org_admin_password=None):
    """
    Update an Uyuni user.

    :param login: user id to look for
    :param password: password for the user
    :param email: user email address
    :param first_name: user first name
    :param last_name: user last name
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: boolean, True indicates success
    """
    return UyuniUser(org_admin_user, org_admin_password).set_details(login=login, password=password, email=email,
                                                                     first_name=first_name, last_name=last_name)


def user_delete(login, org_admin_user=None, org_admin_password=None):
    """
    Deletes an Uyuni user

    :param login: user id to look for
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: boolean, True indicates success
    """
    return UyuniUser(org_admin_user, org_admin_password).delete(login)


def user_list_roles(login, password=None, org_admin_user=None, org_admin_password=None):
    """
    Returns an Uyuni user roles.
    If password is provided as a parameter, then it will be used to authenticate
    If no user credentials are provided, organization administrator credentials will be used
    If no user credentials neither organization admin credentials are provided, credentials from pillar are used

    :param login: user id to look for
    :param password: password for the user
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: List of user roles assigned
    """
    return UyuniUser(org_admin_user if password is None else login,
                     org_admin_password if password is None else password).list_roles(login)


def user_add_role(login, role, org_admin_user=None, org_admin_password=None):
    """
    Adds a role to an Uyuni user.

    :param login: user id to look for
    :param role: role to be added to the user
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: boolean, True indicates success
    """
    return UyuniUser(org_admin_user, org_admin_password).add_role(login=login, role=role)


def user_remove_role(login, role, org_admin_user=None, org_admin_password=None):
    """
    Remove a role from an Uyuni user.

    :param login: user id to look for
    :param role: role to be removed from the user
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: boolean, True indicates success
    """
    return UyuniUser(org_admin_user, org_admin_password).remove_role(login=login, role=role)


def user_list_assigned_system_groups(login, org_admin_user=None, org_admin_password=None):
    """
    Returns the system groups that a user can administer.

    :param login: user id to look for
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: List of system groups that a user can administer
    """
    return UyuniUser(org_admin_user,
                     org_admin_password).list_assigned_system_groups(login=login)


def user_add_assigned_system_groups(login, server_group_names, set_default=False,
                                    org_admin_user=None, org_admin_password=None):
    """
    Add system groups to user's list of assigned system groups.

    :param login: user id to look for
    :param server_group_names: systems groups to add to list of assigned system groups
    :param set_default: Should system groups also be added to user's list of default system groups.
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: boolean, True indicates success
    """
    return UyuniUser(org_admin_user,
                     org_admin_password).add_assigned_system_groups(login=login,
                                                                    server_group_names=server_group_names,
                                                                    set_default=set_default)


def user_remove_assigned_system_groups(login, server_group_names, set_default=False,
                                       org_admin_user=None, org_admin_password=None):
    """
    Remove system groups from a user's list of assigned system groups.

    :param login: user id to look for
    :param server_group_names: systems groups to remove from list of assigned system groups
    :param set_default: Should system groups also be added to user's list of default system groups.
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: boolean, True indicates success
    """
    return UyuniUser(org_admin_user,
                     org_admin_password).remove_assigned_system_groups(login=login,
                                                                       server_group_names=server_group_names,
                                                                       set_default=set_default)


# Software channels

def channel_list_manageable_channels(login, password):
    """
    List all of manageable channels for the authenticated user

    :param login: user login id
    :param password: user password

    :return: list of manageable channels for the user
    """
    return UyuniChannel(login, password).list_manageable_channels()


def channel_list_my_channels(login, password):
    """
    List all of subscribed channels for the authenticated user

    :param login: user login id
    :param password: user password

    :return: list of subscribed channels for the user
    """
    return UyuniChannel(login, password).list_my_channels()


def channel_software_set_user_manageable(channel_label, login, access,
                                         org_admin_user=None, org_admin_password=None):
    """
    Set the manageable flag for a given channel and user.
    If access is set to 'true', this method will give the user manage permissions to the channel.
    Otherwise, that privilege is revoked.

    :param channel_label: label of the channel
    :param login: user login id
    :param access: True if the user should have management access to channel
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: boolean, True indicates success
    """
    return UyuniChannelSoftware(org_admin_user, org_admin_password).set_user_manageable(channel_label, login, access)


def channel_software_set_user_subscribable(channel_label, login, access,
                                           org_admin_user=None, org_admin_password=None):
    """
    Set the subscribable flag for a given channel and user.
    If value is set to 'true', this method will give the user subscribe permissions to the channel.
    Otherwise, that privilege is revoked.

    :param channel_label: label of the channel
    :param login: user login id
    :param access: True if the user should have subscribe access to channel
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: boolean, True indicates success
    """
    return UyuniChannelSoftware(org_admin_user, org_admin_password).set_user_subscribable(channel_label, login, access)


def channel_software_is_user_manageable(channel_label, login, org_admin_user=None, org_admin_password=None):
    """
    Returns whether the channel may be managed by the given user.

    :param channel_label: label of the channel
    :param login: user login id
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: boolean which indicates if user can manage channel or not
    """
    return UyuniChannelSoftware(org_admin_user, org_admin_password).is_user_manageable(channel_label, login)


def channel_software_is_user_subscribable(channel_label, login, org_admin_user=None, org_admin_password=None):
    """
    Returns whether the channel may be subscribed by the given user.

    :param channel_label: label of the channel
    :param login: user login id
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: boolean which indicates if user subscribe the channel or not
    """
    return UyuniChannelSoftware(org_admin_user, org_admin_password).is_user_subscribable(channel_label, login)


def channel_software_is_globally_subscribable(channel_label, org_admin_user=None, org_admin_password=None):
    """
    Returns whether the channel is globally subscribable on the organization

    :param channel_label: label of the channel
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: boolean which indicates if channel is globally subscribable
    """
    return UyuniChannelSoftware(org_admin_user, org_admin_password).is_globally_subscribable(channel_label)


def org_list_orgs(admin_user=None, admin_password=None):
    """
    List all organizations.
    Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

    :param admin_user: uyuni admin user
    :param admin_password: uyuni admin password

    :return: list of all available organizations.
    """
    return UyuniOrg(admin_user, admin_password).list_orgs()


def org_get_details(name, admin_user=None, admin_password=None):
    """
    Get details of an organization.
    Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

    :param name: organisation name
    :param admin_user: uyuni admin user
    :param admin_password: uyuni admin password

    :return: organization details
    """
    return UyuniOrg(admin_user, admin_password).get_details(name)


def org_delete(name, admin_user=None, admin_password=None):
    """
    Delete an organization
    Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

    :param name: organization name
    :param admin_user: uyuni admin user
    :param admin_password: uyuni admin password

    :return: boolean, True indicates success
    """
    return UyuniOrg(admin_user, admin_password).delete(name)


def org_create(name, org_admin_user, org_admin_password, first_name, last_name, email,
               admin_prefix="Mr.", pam=False, admin_user=None, admin_password=None):
    """
    Create an Uyuni organization
    Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

    :param name: organization name
    :param org_admin_user: organization admin user
    :param org_admin_password: organization admin password
    :param first_name: organization admin first name
    :param last_name: organization admin last name
    :param email: organization admin email
    :param admin_prefix: organization admin prefix
    :param pam:organization admin pam authentication
    :param admin_user: uyuni admin user
    :param admin_password: uyuni admin password

    :return: dictionary with org information
    """
    return UyuniOrg(admin_user, admin_password).create(name=name, org_admin_user=org_admin_user,
                                                       org_admin_password=org_admin_password,
                                                       first_name=first_name, last_name=last_name, email=email,
                                                       admin_prefix=admin_prefix, pam=pam)


def org_update_name(org_id, name, admin_user=None, admin_password=None):
    """
    update an Uyuni organization name
    Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

    :param org_id: organization internal id
    :param name: new organization name
    :param admin_user: uyuni admin user
    :param admin_password: uyuni admin password

    :return: organization details
    """
    return UyuniOrg(admin_user, admin_password).update_name(org_id, name)


def org_trust_list_orgs(org_admin_user=None, org_admin_password=None):
    """
    List all organizations trusted by the authenticated user organization

    :param org_admin_user: organization admin user
    :param org_admin_password: organization admin password

    :return: List of organization details
    """
    return UyuniOrgTrust(org_admin_user, org_admin_password).list_orgs()


def org_trust_list_trusts(org_name, admin_user=None, admin_password=None):
    """
    List all trusts for one organization
    Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

    :param org_name: Name of the organization to get the trusts
    :param admin_user: authentication user
    :param admin_password: authentication user password

    :return: list with all organizations and their trust status
    """
    return UyuniOrgTrust(admin_user, admin_password).list_trusts(org_name)


def org_trust_add_trust_by_name(org_name, org_trust, admin_user=None, admin_password=None):
    """
    Add an organization to the list of trusted organizations.
    Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

    :param org_name: organization name
    :param org_trust: Trust organization name
    :param admin_user: uyuni admin user
    :param admin_password: uyuni admin password

    :return: boolean, True indicates success
    """
    return UyuniOrgTrust(admin_user, admin_password).add_trust_by_name(org_name, org_trust)


def org_trust_add_trust(org_id, org_trust_id, admin_user=None, admin_password=None):
    """
    Add an organization to the list of trusted organizations.
    Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

    :param org_id: Organization id
    :param org_trust_id: Trust organization id
    :param admin_user: uyuni admin user
    :param admin_password: uyuni admin password

    :return: boolean, True indicates success
    """
    return UyuniOrgTrust(admin_user, admin_password).add_trust(org_id, org_trust_id)


def org_trust_remove_trust_by_name(org_name, org_untrust, admin_user=None, admin_password=None):
    """
    Remove an organization from the list of trusted organizations.
    Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

    :param org_name: organization name
    :param org_untrust: organization name to untrust
    :param admin_user: uyuni admin user
    :param admin_password: uyuni admin password

    :return: boolean, True indicates success
    """
    return UyuniOrgTrust(admin_user, admin_password).remove_trust_by_name(org_name, org_untrust)


def org_trust_remove_trust(org_id, org_untrust_id, admin_user=None, admin_password=None):
    """
    Remove an organization from the list of trusted organizations.
    Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

    :param org_id: orgnization id
    :param org_untrust_id: organizaton id to untrust
    :param admin_user: uyuni admin user
    :param admin_password: uyuni admin password

    :return: boolean, True indicates success
    """
    return UyuniOrgTrust(admin_user, admin_password).remove_trust(org_id, org_untrust_id)


# System Groups

def systemgroup_create(name, descr, org_admin_user=None, org_admin_password=None):
    """
    Create a system group.

    :param name: Name of the system group.
    :param descr: Description of the system group.
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password

    :return: details of the system group
    """
    return UyuniSystemgroup(org_admin_user, org_admin_password).create(name=name, description=descr)


def systemgroup_get_details(name, org_admin_user=None, org_admin_password=None):
    """
    Return system group details.

    :param name: Name of the system group.
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password

    :return: details of the system group
    """
    return UyuniSystemgroup(org_admin_user, org_admin_password).get_details(name=name)


def systemgroup_update(name, descr, org_admin_user=None, org_admin_password=None):
    """
    Update a system group.

    :param name: Name of the system group.
    :param descr: Description of the system group.
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password

    :return: details of the system group
    """
    return UyuniSystemgroup(org_admin_user, org_admin_password).update(name=name, description=descr)


def systemgroup_delete(name, org_admin_user=None, org_admin_password=None):
    """
    Delete a system group.

    :param name: Name of the system group.
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password

    :return: boolean, True indicates success
    """
    return UyuniSystemgroup(org_admin_user, org_admin_password).delete(name=name)


def systemgroup_list_systems(name, minimal=True, org_admin_user=None, org_admin_password=None):
    """
    List systems in a system group

    :param name: Name of the system group.
    :param minimal: default True. Only return minimal information about systems, use False to get more details
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password

    :return: List of system information
    """
    return UyuniSystemgroup(org_admin_user, org_admin_password).list_systems(name=name, minimal=minimal)


def systemgroup_add_remove_systems(name, add_remove, system_ids=[],
                                   org_admin_user=None, org_admin_password=None):
    """
    Update systems on a system group.

    :param name: Name of the system group.
    :param add_remove: True to add to the group, False to remove.
    :param system_ids: list of system ids to add/remove from group
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password

    :return: boolean, True indicates success
    """
    return UyuniSystemgroup(org_admin_user, org_admin_password).add_remove_systems(name=name, add_remove=add_remove,
                                                                                   system_ids=system_ids)


def master_select_minions(target=None, target_type="glob"):
    """
    Return list minions from the configured Salt Master on the same host which match the expression on the defined target

    :param target: target expression to filter minions
    :param target_type: target type, one of the following: glob, grain, grain_pcre, pillar, pillar_pcre,
                pillar_exact, compound, compound_pillar_exact. Default: glob.

    :return: list of minion IDs
    """
    cmi = UyuniChildMasterIntegration()

    return cmi.select_minions(target=target, target_type=target_type)


def systems_get_minion_id_map(username=None, password=None, refresh=False):
    """
    Returns a map from minion ID to Uyuni system ID for all systems a user has access to

    :param username: username to authenticate
    :param password: password for user
    :param refresh: Get new data from server, ignoring values in local context cache

    :return: Map between minion ID and system ID of all system accessible by authenticated user
    """
    return UyuniSystems(username, password).get_minion_id_map(refresh)


# Activation Keys

def activation_key_get_details(id, org_admin_user=None, org_admin_password=None):
    """
    Get details of an Uyuni Activation Key

    :param id: the Activation Key ID
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: Activation Key information
    """
    return UyuniActivationKey(org_admin_user, org_admin_password).get_details(id)

def activation_key_delete(id, org_admin_user=None, org_admin_password=None):
    """
    Deletes an Uyuni Activation Key

    :param id: the Activation Key ID
    :param org_admin_user: organization admin username
    :param org_admin_password: organization admin password

    :return: boolean, True indicates success
    """
    return UyuniActivationKey(org_admin_user, org_admin_password).delete(id)