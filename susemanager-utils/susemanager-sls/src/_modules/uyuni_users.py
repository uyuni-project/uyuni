# coding: utf-8
from typing import Any, Dict, List, Optional, Union, Tuple
import ssl
import xmlrpc.client  # type: ignore
import logging
import datetime
import copy
import os

import salt.config
import salt.client
import salt.utils.crypt
from salt.exceptions import CommandExecutionError
from salt.utils.minions import CkMinions

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
    __instance__: Optional["RPCClient"] = None
    __instance_path__: str = "/var/cache/salt/minion/uyuni.rpc.s"

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

    def save_session(self) -> bool:
        """
        Save session of the RPC

        :return: boolean, True on success
        """
        os.makedirs(os.path.dirname(RPCClient.__instance_path__), exist_ok=True)
        with open(RPCClient.__instance_path__, 'wb') as fh:
            try:
                fh.write(str(self.get_token()).encode())
                log.debug("Wrote RPC session to %s", RPCClient.__instance_path__)
                ret = True
            except OSError as exc:
                ret = False
                log.error("Unable to serialise RPC client: %s", exc)
            except Exception as exc:
               ret = False
               log.error("Unhandled error while serialising RPC client object: %s", exc)

        return ret

    @staticmethod
    def load_session() -> Optional[str]:
        """
        Read previously saved RPC session token from the disk.
        If this is not possible, None is returned.

        :return: string or None
        """
        obj: Optional[str] = None
        try:
            with open(RPCClient.__instance_path__, 'rb') as fh:
                try:
                    data: bytes = fh.read()
                    if data:
                        obj = data.decode()
                except Exception as exc:
                    log.debug("Unable to load saved RPC session: %s", exc)
        except FileNotFoundError:
            log.debug("No previously saved RPC session")

        return obj

    @staticmethod
    def init(pillar: Optional[Dict[str, Any]] = None):
        """
        Create new instance

        :return:
        """
        if RPCClient.__instance__ is None:
            plr: Optional[Dict[str, Any]] = __pillar__ or {}
            if "xmlrpc" not in (plr or {}).keys():
                plr = pillar

            if "xmlrpc" in (plr or {}).get("uyuni", {}):
                rpc_conf = (plr or {})["uyuni"]["xmlrpc"] or {}
                RPCClient.__instance__ = RPCClient(rpc_conf.get("url", "https://localhost/rpc/api"),
                                                   rpc_conf.get("user", ""), rpc_conf.get("password", ""))
                RPCClient.__instance__.token = RPCClient.load_session()
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
                self.save_session()
            except Exception as exc:
                log.error("Unable to login to the Uyuni server: %s", exc)

        return self.token

    def __call__(self, method: str, *args, **kwargs) -> Any:
        self.get_token()
        if self.token is not None:
            try:
                log.debug("Calling RPC method %s", method)
                return getattr(self.conn, method)(*args)
            except Exception as exc:
                log.debug("Fall back to the second try due to %s", str(exc))
                self.get_token(refresh=True)
                try:
                    return getattr(self.conn, method)(*((self.get_token(),) + args[1:]))
                except Exception as exc:
                    log.error("Unable to call RPC function: %s", str(exc))
                    raise UyuniUsersException(exc)

        raise UyuniUsersException("XML-RPC backend authentication error.")


class UyuniRemoteObject:
    """
    RPC client
    """
    def __init__(self, pillar: Optional[Dict[str, Any]] = None):
        self.client: RPCClient = RPCClient.init(pillar=pillar or __pillar__)

    def get_proto_return(self, exc: Exception = None) -> Dict[str, Any]:
        """
        Protocol return structure.

        :return: dictionary
        """
        ret: Dict[str, Any] = {}
        if exc is not None:
            ret["error"] = str(exc)

        return ret


class UyuniUser(UyuniRemoteObject):
    """
    CRUD operation on users.
    """
    def get_user(self, name: str) -> Dict[str, Any]:
        """
        Get existing user data from the Uyuni.

        :return:
        """
        return self.client("user.getDetails", self.client.get_token(), name)

    def get_all_users(self):
        """
        Return all Uyuni users.

        :return:
        """
        return self.client("user.listUsers", self.client.get_token())

    def create(self, uid: str, password: str, email: str, first_name: str = "", last_name: str = "") -> bool:
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

    def delete(self, name: str) -> bool:
        """
        Remove user from the Uyuni org.

        :param name: UID of the user

        :return: boolean, True if user has been deleted successfully.
        """
        try:
            ret = bool(self.client("user.delete", self.client.get_token(), name))
        except UyuniUsersException as exc:
            log.error('Unable to delete user "%s": %s', name, str(exc))
            ret = False
        except Exception as exc:
            log.error('Unhandled error had happened while deleting user "%s": %s', name, str(exc))
            ret = False

        return ret


class UyuniOrg(UyuniRemoteObject):
    """
    CRUD operations on orgs
    """
    def get_orgs(self) -> Dict[str, Union[int, str, bool]]:
        """
        List all orgs.

        :return:
        """
        return self.client("org.listOrgs", self.client.get_token())

    def get_org_by_name(self, name: str) -> Dict[str, Union[int, str, bool]]:
        """
        Get org data by name.

        :param name: organisation name
        :return:
        """
        try:
            org_data = self.client("org.getDetails", self.client.get_token(), name)
        except UyuniUsersException:
            org_data = {}

        return org_data

    def create(self, name: str, admin_login: str, admin_password: str, admin_prefix: str, first_name: str,
               last_name: str, email: str, pam: bool) -> Tuple[Dict[str, Union[str, int, bool]], str]:
        """
        Create Uyuni org.

        :param name:
        :param admin_login:
        :param admin_password:
        :param admin_prefix:
        :param first_name:
        :param last_name:
        :param email:
        :param pam:
        :return: tuple of data and error/log message
        """
        try:
            log.debug("Creating organisation %s", name)
            ret = self.client("org.create", self.client.get_token(), name, admin_login, admin_password, admin_prefix,
                              first_name, last_name, email, pam)
            msg = 'Organisation "{}" has been created successfully'.format(name)
            log.debug(msg)
        except UyuniUsersException as exc:
            ret = {}
            msg = 'Error while creating organisation: {}'.format(str(exc))
        except Exception as exc:
            ret = {}
            msg = 'Unhandled exception occurred while creating new organisation: {}'.format(str(exc))

        return ret, msg

    def delete(self, name: str) -> Tuple[bool, str]:
        """
        Delete Uyuni org.

        :param name:
        :return: boolean, True if organisation has been deleted.
        """
        org_id = int(self.get_org_by_name(name=name).get("id", -1))
        if org_id > -1:
            res = bool(self.client("org.delete", self.client.get_token(), org_id))
            msg = 'Organisation "{}" (ID: "{}") has been removed'.format(name, org_id)
            log.debug(msg)
        else:
            res = False
            msg = 'Organisation "{}" was not found'.format(name)
            log.error(msg)

        return res, msg


class UyuniTrust(UyuniRemoteObject):
    """
    CRUD operations to trusts.
    """
    def __init__(self, org_name: str, pillar: Optional[Dict[str, Any]] = None):
        """

        :param org_name:
        :param pillar:
        """
        UyuniRemoteObject.__init__(self, pillar=pillar)
        self.orgs = UyuniOrg(pillar=pillar)
        self.__this_org: Dict[str, Union[int, str, bool]] = self.orgs.get_org_by_name(org_name)

    @property
    def this_org(self):
        """
        Property getter.

        :return: data of the current organisation
        """
        assert self.__this_org, "No details could be found for the current organisation"
        return self.__this_org

    def get_trust_by_name(self, name: str) -> Dict[str, Union[int, datetime.datetime]]:
        """
        Get trust data by name.
        Trust is a trusted organisation, so this method is looking basically
        for an organisation that has trust enabled flag.

        :param name:
        :return:
        """
        found: bool = False
        trust_data: Dict[str, Union[int, datetime.datetime]] = {}
        for trusted_org in self.get_trusted():
            if trusted_org["org_name"] == name:
                trust_data = self.client("org.trusts.getDetails", self.client.get_token(), trusted_org["id"])
                found = True
                break

        log.debug('Trust "%s" %s', name, "not found" if not found else "has been found")

        return trust_data

    def get_trusted(self) -> List[Dict[str, Union[str, int]]]:
        """
        Get list of all trusted orgs.

        :return:
        """
        try:
            out = self.client("org.trusts.listOrgs", self.client.get_token())
        except Exception as exc:
            out = [self.get_proto_return(exc=exc)]
        return out

    def trust(self, *orgs: str) -> List[str]:
        """
        Set organisation trusted.

        :param name:

        :raises UyuniUsersException: if RPC call has been failed.
        :return: Org names which flag has been changed
        """
        changes: List[str] = []
        # Remove orgs that are already trusted
        l_orgs = list(copy.deepcopy(orgs))
        for trusted_org in self.get_trusted():
            l_trusted_org = str(trusted_org["org_name"])
            if l_trusted_org in l_orgs:
                l_orgs.pop(l_orgs.index(l_trusted_org))
                log.info('Skipping organisation "%s": already trusted', trusted_org)

        for org_name in l_orgs:
            org_data = self.orgs.get_org_by_name(org_name)
            assert org_data, "Adding trust: no information has been found for '{}' organisation".format(org_name)
            if bool(self.client("org.trusts.addTrust", self.client.get_token(), self.this_org["id"], org_data["id"])):
                changes.append(org_name)
                log.info('Added trusted organisation "%s" to the "%s"', org_name, self.this_org["name"])

        return changes

    def untrust(self, *orgs: str) -> List[str]:
        """
        Set organisation untrusted.

        :param name: The organisation name

        :raises UyuniUsersException: If RPC call has been failed.
        :return: boolean, True if trust flag has been changed to False
        """
        changes = []
        for org_name in orgs:
            org_data = self.orgs.get_org_by_name(org_name)
            assert org_data, "Trust removal: no information has been found for '{}' organisation".format(org_name)
            try:
                self.client("org.trusts.removeTrust", self.client.get_token(), self.this_org["id"], org_data["id"])
                changes.append(org_name)
            except UyuniUsersException as exc:
                log.error("Unable to remove trust: %s", exc)

        return changes


class UyuniChannels(UyuniRemoteObject):
    """
    Uyuni channels.
    """
    ACCESS_PUBLIC = "public"
    ACCESS_PRIVATE = "private"
    ACCESS_PROTECTED = "protected"

    def get_sharing(self, channel: str) -> str:
        """
        Get sharing access control in the context to the current organisation.

        :param channel: label of the channel

        :return:
        """
        return self.client("channel.access.getOrgSharing", self.client.get_token(), channel)

    def set_sharing(self, channel: str, access: str) -> None:
        """
        Set sharing access control in the context to the current organisation.

        :param channel: label of the channel
        :param access: access string

        :raises UyuniUsersException: if setting access control was not successful.

        :return:
        """
        if access not in [self.ACCESS_PRIVATE, self.ACCESS_PROTECTED, self.ACCESS_PUBLIC]:
            raise UyuniChannelsException('Access type "%s" is not recognised:', access)

        self.client("channel.access.setOrgSharing", self.client.get_token(), channel, access)

    def restrict(self, label: str) -> None:
        """
        Enable user restrictions for the given channel. If enabled, only selected
        users within the organization may subscribe to the channel.

        :param label:

        :raises UyuniChannelsException: if restriction setting was not successful.

        :return: None
        """
        self.client("channel.access.enableUserRestrictions", self.client.get_token(), label)

    def unrestrict(self, label: str) -> None:
        """
        Disable user restrictions for the given channel. If disabled, all users
        within the organization may subscribe to the channel.

        :param label:

        :raises UyuniChannelsException: if restriction setting was not successful.

        :return:
        """
        self.client("channel.access.disableUserRestrictions", self.client.get_token(), label)


class UyuniSystemgroup(UyuniRemoteObject):
    """
    Provides methods to access and modify system groups.
    """
    def get(self, name: str) -> Dict[str, Union[int, str]]:
        """
        Retrieve details of a ServerGroup.

        :param name: Name of the system group.

        :return: data of the system group.
        """
        return self.client("systemgroup.getDetails", self.client.get_token(), name)

    def create(self, name: str, description: str) -> Dict[str, Union[int, str]]:
        """
        Create a new system group.

        :param name: Name of the system group.
        :param description: Description of the system group.

        :return:
        """
        return self.client("systemgroup.create", self.client.get_token(), name, description)

    def delete(self, name: str) -> None:
        """
        Delete a system group.

        :param name: Name of the system group.

        :return:
        """
        self.client("systemgroup.delete", self.client.get_token(), name)

    def update(self, name: str, description: str) -> Dict[str, Union[int, str]]:
        """
        Update an existing system group.

        :param name: Name of the system group.
        :param description: Description of the system group.

        :return:
        """
        return self.client("systemgroup.update", self.client.get_token(), name, description)


class UyuniChildMasterIntegration(UyuniRemoteObject):
    """
    Integration with the Salt Master which is running
    on the same host as this current Minion.
    """
    DEFAULT_MASTER_CONFIG_PATH = "/etc/salt/master"

    class FCkMinions(CkMinions):
        """
        Minion data matcher.
        """
        def _get_key_fingerprint(self, minion_id: str) -> str:
            """
            Get minion key fingerprint.

            :param minion_id:
            :return: fingerprint or an empty string if not found
            """
            keypath = os.path.join(self.opts['pki_dir'], self.acc, minion_id)
            return salt.utils.crypt.pem_finger(path=keypath, sum_type=self.opts["hash_type"])

        def _get_fingerprints(self, minion_ids: List[str]) -> Dict[str, str]:
            """
            Resolve all fingerprints.

            :param minion_ids:
            :return:
            """
            minions = {}
            for mid in minion_ids:
                minions[mid] = self._get_key_fingerprint(minion_id=mid)

            return minions

    def __init__(self, pillar: Optional[Dict[str, Any]] = None):
        UyuniRemoteObject.__init__(self, pillar=pillar)
        self._minions = UyuniChildMasterIntegration.FCkMinions(salt.config.master_config(self._get_master_config()))

    @staticmethod
    def _get_master_config() -> str:
        """
        Return master config.
        :return:
        """
        cfg_path = UyuniChildMasterIntegration.DEFAULT_MASTER_CONFIG_PATH
        for path in __pillar__.get("uyuni", {}).get("masters", {}).get("configs", [cfg_path]):
            if os.path.exists(path):
                cfg_path = path
                break

        return cfg_path

    def list_minions(self, active: bool = False) -> List[str]:
        """
        Return list of currently registered minions.

        :param active: Return only active minions.
        :return: list of minion ids
        """
        return self._minions.connected_ids() if active else self._minions._pki_minions()

    def list_minions_fp(self, active: bool = False) -> Dict[str, str]:
        """
        Return list of currently registered minions, including their key fingerprints.

        :param active: Return only active minions.
        :return: mapping of minion ids to the fingerprints
        """
        return self._minions._get_fingerprints(self.list_minions(active=active))

    def select_minions(self, expr: str, tgt: str = "glob") -> Dict[str, Union[List[str], bool]]:
        """
        Select minion IDs that matches the expression.

        :param expr: expression
        :param tgt: target type, one of the following: glob, grain, grain_pcre, pillar, pillar_pcre,
                    pillar_exact, compound, compound_pillar_exact. Default: glob.

        :return: list of minions
        """
        return self._minions.check_minions(expr=expr, tgt_type=tgt)

    def select_minions_fp(self, expr: str, tgt: str = "glob") -> Dict[str, Union[str, bool]]:
        """
        Select minion IDs that matches the expression.

        :param expr: expression
        :param tgt: target type, one of the following: glob, grain, grain_pcre, pillar, pillar_pcre,
                    pillar_exact, compound, compound_pillar_exact. Default: glob.

        :return: mapping of minion ids to the fingerprints
        """
        selected = self.select_minions(expr=expr, tgt=tgt)
        ret = {
            "minions": self._minions._get_fingerprints(selected["minions"]),
            "missing": self._minions._get_fingerprints(selected["missing"]),
            "ssh_minions": selected.get("ssh_minions", False)
        }

        return ret

    def master(self, *args: List[str], **kwargs: Dict[str, Any]):
        """
        Run arbitrary commands on the target master.

        :param args: First two positional arguments are "minion_id" and "module.function".
                    Other are positional arguments to that module function.
        :param kwargs: Named keywords to the remote "module.function".

        :return:
        """
        if len(args) < 2:
            raise CommandExecutionError("Should be target minion name and function passed")

        expr, func = args[:2]
        fargs = args[2:]
        fkwargs = {}
        for kw_key in kwargs:
            if not kw_key.startswith("_"):
                fkwargs[kw_key] = kwargs[kw_key]

        client = salt.client.LocalClient()
        return client.cmd(expr, func, fargs, kwarg=fkwargs)


def __virtual__():
    """
    Provide Uyuni Users state module.

    :return:
    """

    return __virtualname__


def create_org(name, admin_login, first_name, last_name, email, admin_password=None, admin_prefix="Mr.", pam=False):
    """
    Create org in Uyuni.

    :param name:
    :param admin_login:
    :param first_name:
    :param last_name:
    :param email:
    :param admin_password:
    :param admin_prefix:
    :param pam:
    :return:
    """
    return UyuniOrg().create(name=name, admin_login=admin_login, admin_password=admin_password,
                             email=email, first_name=first_name, last_name=last_name,
                             admin_prefix=admin_prefix, pam=pam)


def delete_org(name):
    """
    Delete organisation by name.

    :param name:
    :return:
    """
    return UyuniOrg().delete(name=name)


def list_orgs():
    """
    List all available organisations.

    :return: List of hashes per organisation.
    """
    return UyuniOrg().get_orgs()


def list_trusts(name):
    """
    List trusted orgs on given organisation name.

    :param name: Base organisation name

    :return: dictionary of trusted organisations. See "org.trusts.listTrusted" from the Uyuni API.
    """
    return UyuniTrust(org_name=name).get_trusted()


def share_channel(name, access):
    """
    Set organization sharing access control.

    :param name:
    :param access:

    :return:
    """
    return UyuniChannels().set_sharing(channel=name, access=access)


def shared_channel(name):
    """
    Get organization sharing access control.

    :param name:

    :return:
    """
    return UyuniChannels().get_sharing(channel=name)


def restrict_channel(name):
    """
    Restrict channel to organisation users.

    :param name:
    :return: None
    """
    return UyuniChannels().restrict(label=name)


def unrestrict_channel(name):
    """
    Remove channel restrictions from the organisation users.

    :param name:
    :return: None
    """
    return UyuniChannels().unrestrict(label=name)


def create_sysgroup(name, descr):
    """
    Create system group.

    :param name: Name of the system group.
    :param descr: Description of the system group.

    :return: server group structure.
    """
    return UyuniSystemgroup().create(name=name, description=descr)


def update_sysgroup(name, descr):
    """
    Update system group.

    :param name: Name of the system group.
    :param descr: Description of the system group.

    :return: server group structure.
    """
    return UyuniSystemgroup().update(name=name, description=descr)


def delete_sysgroup(name):
    """
    Delete system group.

    :param name: Name of the system group.

    :return: None
    """
    return UyuniSystemgroup().delete(name=name)


def list_minions(expr=None, tgt="glob", active=False, fp=False):
    """
    Return list of all available minions from the configured
    Salt Master on the same host.

    :param active: Return only active minions.
    :param fp: Include fingerprints

    :return: list of minion IDs
    """
    cmi = UyuniChildMasterIntegration()

    if expr is not None:
        ret = (cmi.select_minions_fp if fp else cmi.select_minions)(expr=expr, tgt=tgt)
    else:
        ret = (cmi.list_minions if fp else cmi.list_minions)(active=active)

    return ret


def master(*args, **kwargs):
    """
    Perform arbitrary operation on a remote master.

    :param args: minimum two parameters, where first is minion name or a selector, second is a function. There can be
                also positional parameters to the function itself.
    :param kwargs: named arguments to the called remote function on the sibling master.

    :return: return data from the target minion.
    """
    return UyuniChildMasterIntegration().master(*args, **kwargs)
