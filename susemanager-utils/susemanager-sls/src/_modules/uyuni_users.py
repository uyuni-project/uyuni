# coding: utf-8
from typing import Any, Dict, List, Optional, Union, Tuple
import ssl
import xmlrpc.client  # type: ignore
import logging
import datetime
import copy
import os


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
        log.debug("*** FREEZING ***")
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

    def __call__(self, method: str, *args, **kwargs):
        self.get_token()
        if self.token is not None:
            try:
                return getattr(self.conn, method)(*args)
            except Exception as exc:
                log.debug("Fall back to the second try due to %s", exc)
                self.get_token(refresh=True)
                try:
                    return getattr(self.conn, method)(*((self.get_token(),) + args[1:]))
                except Exception as exc:
                    log.error("Unable to call RPC function: %s", exc)
                    raise UyuniUsersException(exc)

        raise UyuniUsersException("XML-RPC backend authentication error.")


class UyuniRemoteObject:
    """
    RPC client
    """
    def __init__(self, pillar: Optional[Dict[str, Any]] = None):
        self.client: RPCClient = RPCClient.init(pillar=pillar)

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
            log.error('Unable to delete user "%s": %s', name, exc)
            ret = False
        except Exception as exc:
            log.error('Unhandled error had happend while deleting user "%s": %s', name, exc)
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
            msg = 'Organisation "{}" (ID: "{}") has been removed.'.format(name, org_id)
            log.debug(msg)
        else:
            res = False
            msg = 'Organisation "{}" was not found.'.format(name)
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
        assert self.this_org, "No details could be found for the current organisation"
        return self.__this_org

    def get_trust_by_name(self, name: str) -> Dict[str, Union[int, datetime.datetime]]:
        """
        Get trust data by name.
        Trust is a trusted organisation, so this method is looking basically
        for an organisation that has trust enabled flag.

        :param name:
        :return:
        """
        return {}

    def trust(self, name: str) -> bool:
        """
        Set organisation trusted.

        :param name:

        :raises UyuniUsersException: if RPC call has been failed.
        :return: boolean, True if trust flag has been changed to True
        """

        return False

    def untrust(self, name: str) -> bool:
        """
        Set organisation untrusted.

        :param name: The organisation name

        :raises UyuniUsersException: If RPC call has been failed.
        :return: boolean, True if trust flag has been changed to False
        """

        return False


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
    return UyuniOrg(pillar=__pillar__).create(name=name, admin_login=admin_login, admin_password=admin_password,
                                              email=email, first_name=first_name, last_name=last_name,
                                              admin_prefix=admin_prefix, pam=pam)


def delete_org(name):
    """
    Delete organisation by name.

    :param name:
    :return:
    """
    return UyuniOrg(pillar=__pillar__).delete(name=name)
