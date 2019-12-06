# coding: utf-8
"""
Uyuni users state module
"""
from typing import Any, Dict, List, Optional, Union, Tuple
import logging
from salt.modules.uyuni import UyuniUsersException, UyuniUser, UyuniOrg, RPCClient, UyuniTrust


log = logging.getLogger(__name__)
__pillar__: Dict[str, Any] = {}
__salt__: Dict[str, Any] = {}
__context__: Dict[str, Any] = {}
__virtualname__: str = "uyuni"


class UyuniFunctions:
    """
    RPC client
    """
    def __init__(self):
        self.client = RPCClient.init(pillar=__pillar__)
        self.users = UyuniUser(pillar=__pillar__)
        self.orgs = UyuniOrg(pillar=__pillar__)

    @staticmethod
    def _get_proto_ret(name: str, result: Optional[bool] = None) -> Dict[str, Any]:
        """
        State proto return container.

        :param name:
        :return:
        """
        changes: Dict[str, str] = {}
        return {
            'name': name,
            'changes': changes,
            'result': result,
            'comment': "",
        }


class UyuniOrgs(UyuniFunctions):
    """
    Uyuni operations over orgs and trusts.
    """
    ADMIN_PREFIXES = ["Dr.", "Hr.", "Miss", "Mr.", "Mrs.", "Ms.", "Sr."]

    class Policy:
        """
        Policy control
        """
        def __init__(self):
            """
            Policy object.

            :param orgid: Organisation ID
            """

            self.__orgid: Optional[int] = None

            # Crash files
            self.crash_file_upload: Optional[bool] = None
            self.crash_file_size_limit: Optional[int] = None
            self.crash_reporting: Optional[bool] = None

            # SCAP
            self.scap_file_upload: Optional[bool] = None
            self.scap_file_limit: Optional[int] = None
            self.scap_result_delete: Optional[bool] = None
            self.scap_result_retention_days: Optional[int] = None

        @property
        def orgid(self) -> int:
            """
            Org ID property

            :return: integer or None
            """
            if self.__orgid is None:
                raise UyuniUsersException("Org ID was not set to the policy container")

            return self.__orgid

        @orgid.setter
        def orgid(self, value: int):
            """
            Org ID setter. Works once per an instance.

            :param value:
            :return:
            """
            if value is None or value < 0:
                raise UyuniUsersException("Org ID should be an integer")

            if self.__orgid is None:
                self.__orgid = value

        def get_crash_file_policies(self) -> List[Tuple[str, List[Union[str, Optional[int], Optional[bool]]]]]:
            """
            Get the policies of crash reporting settings for the given organisation.

            :return:
            """
            policy: List[Tuple[str, List[Union[str, Optional[int], Optional[bool]]]]] = []
            if self.crash_file_size_limit is not None:
                policy.append(("org.setCrashFileSizeLimit", [self.orgid, self.crash_file_size_limit],))

            if self.crash_file_upload is not None:
                policy.append(("org.setCrashfileUpload", [self.orgid, self.crash_file_upload],))

            if self.crash_reporting is not None:
                policy.append(("org.setCrashReporting", [self.orgid, self.crash_file_size_limit],))

            return policy

        def get_scap_file_upload(self) -> List[Tuple[str, List[Union[str, Optional[int], Optional[bool]]]]]:
            """
            Get the status of SCAP detailed result file upload settings for the given organisation.

            :return: dict
            """
            policy: List[Tuple[str, List[Union[str, Optional[int], Optional[bool]]]]] = []
            if self.scap_file_limit is not None and self.scap_file_upload is not None:
                policy.append(("org.setPolicyForScapFileUpload", [self.scap_file_upload, self.scap_file_limit],))

            return policy

        def get_scap_result_delete(self) -> List[Tuple[str, List[Union[str, Optional[int], Optional[bool]]]]]:
            """
            Get the status of SCAP result deletion settins for the given organisation.

            :return:
            """
            policy: List[Tuple[str, List[Union[str, Optional[int], Optional[bool]]]]] = []
            if self.scap_result_delete is not None and self.scap_result_retention_days is not None:
                policy.append(("org.setPolicyForScapResultDeletion", [self.scap_result_delete,
                                                                      self.scap_result_retention_days],))

            return policy

    def delete(self, name: str):
        """
        Delete Uyuni org.

        :param name:
        :return:
        """
        ret: Dict[str, Any] = self._get_proto_ret(name=name)
        ret["result"], ret["comment"] = self.orgs.delete(name)

        return ret

    def manage(self, name: str, admin_login: str, admin_password: str, admin_prefix: str, first_name: str,
               last_name: str, email: str, pam: bool, policy: Policy, content_staging: Optional[bool] = None,
               errata_email_notif: Optional[bool] = None, org_admin_enable: Optional[bool] = None) -> Dict[str, Any]:
        """
        Manage Uyuni organisation.

        :param name:
        :param admin_login:
        :param admin_password:
        :param admin_prefix:
        :param first_name:
        :param last_name:
        :param email:
        :param pam:
        :param content_staging:
        :param errata_email_notif:
        :param org_admin_enable:
        :param policy:
        :return:
        """
        ret = self._get_proto_ret(name)
        if admin_prefix not in self.ADMIN_PREFIXES:
            ret["result"] = False
            ret["comment"] = "Admin prefix must be one of the {}.".format(
                ", ".join(('"{}"'.format(prefix) for prefix in self.ADMIN_PREFIXES)))
        else:
            org_data = self.orgs.get_org_by_name(name)
            if not org_data:
                org_data, ret["comment"] = self.orgs.create(name=name, email=email,  admin_login=admin_login,
                                                            admin_password=admin_password, admin_prefix=admin_prefix,
                                                            first_name=first_name, last_name=last_name, pam=pam)
                ret["result"] = bool(org_data)

            # Set policies
            applied = 0
            policy.orgid = int(org_data.get("id", -1))
            for p_set in [policy.get_crash_file_policies(), policy.get_scap_file_upload(), policy.get_scap_result_delete()]:
                for rpc_func, params in p_set:
                    self.client(rpc_func, self.client.get_token(), *params)
                    applied += 1
            if applied:
                ret["comment"] = "{} Applied {} policies.".format(ret["comment"], applied).strip()

        return ret

    def trust(self, name: str, organisations: List[str]) -> Dict[str, Any]:
        """
        Add trusted organisations to the org.

        :param name:
        :param organisations:
        :return:
        """
        orgs_msg = ",".join(['"{}"'.format(org) for org in organisations])
        ret = self._get_proto_ret(name)
        try:
            changes = UyuniTrust(org_name=name, pillar=__pillar__).trust(*organisations)
            if changes:
                ret["changes"] = {"Added:": changes}
                ret["result"] = True
        except Exception as exc:
            ret["result"] = False
            ret["comment"] = "Failed to add {} organisations: {}".format(orgs_msg, str(exc))

        return ret

    def untrust(self, name: str, organisations: List[str]) -> Dict[str, Any]:
        """
        Remote trusted organisations from an organisation.

        :param name:
        :param organisations:
        :return:
        """
        orgs_msg = ",".join(['"{}"'.format(org) for org in organisations])
        ret = self._get_proto_ret(name)
        try:
            changes = UyuniTrust(org_name=name, pillar=__pillar__).untrust(*organisations)
            if changes:
                ret["changes"] = {"Removed:": changes}
                ret["result"] = True
        except Exception as exc:
            ret["result"] = False
            ret["comment"] = "Failed to remove {} organisations: {}".format(orgs_msg, str(exc))

        return ret


class UyuniUsers(UyuniFunctions):
    """
    Uyuni operations over users.
    """
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
            ret = self.users.create(uid=uid, password=password, email=email,
                                    first_name=first_name, last_name=last_name)
            log.debug("User has been created")

        return bool(ret)

    def delete(self, name: str) -> Dict[str, Any]:
        """
        Remove user from the Uyuni org.

        :param name: UID of the user

        :return: dict for Salt communication
        """
        ret = self._get_proto_ret(name, result=False)

        try:
            ret["result"] = self.users.delete(name)
            if ret["result"]:
                ret["comment"] = "User {} has been deleted".format(name)
            else:
                ret["comment"] = "Deleting user {} failed. See logs for more details".format(name)
        except Exception as exc:
            ret["result"] = False
            ret["comment"] = str(exc)

        return ret

    def manage(self, name: str, password: str, email: str, first_name: str = "", last_name: str = "",
               org: str = "", roles: Optional[List[str]] = None) -> Dict[str, Any]:
        """
        Manage user with the data. If anything of the initial data is updated, is going to be added.

        :param name: UID of the user
        :param password: Password for the user
        :param email: Email of the user
        :param first_name: First name (optional)
        :param last_name: Second name (options)
        :param org: Organisation
        :param roles: list of roles

        :return:
        """
        result: Optional[bool] = None
        ret = {
            'name': name,
            'changes': {},
            'result': result,
            'comment': "",
        }

        existing_user = None
        try:
            for user in self.users.get_all_users():
                if user.get("login") == name:
                    existing_user = self.users.get_user(name)
                    break
        except UyuniUsersException as exc:
            ret["comment"] = "Error manage user '{}': {}".format(name, exc)
            ret["result"] = False
            log.error(ret["comment"])
        else:
            if existing_user is None:
                changes = {"uid": name, "password": password, "email": email,
                           "first_name": first_name, "last_name": last_name}
                self.create(**changes)
                changes["password"] = "(hidden)"

                ret["changes"] = changes
                ret["comment"] = "Added new user {}".format(name)
                ret["result"] = True
            else:
                # Changes
                ret["comment"] = "No changes has been done"

        return ret


def __virtual__():
    """
    Provide Uyuni Users state module.

    :return:
    """
    return __virtualname__ if RPCClient.init(pillar=__pillar__) is not None else False


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
    return UyuniUsers().manage(name=name, password=password, email=email, first_name=first_name,
                               last_name=last_name, org=org, roles=roles)


def user_absent(name):
    """
    Remove Uyuni user.

    :param name: Uyuni user name

    :return: dictionary for Salt communication protocol
    """
    return UyuniUsers().delete(name)


def org_present(name, admin_login, admin_password, admin_prefix, first_name, last_name, email, pam=False,
                content_staging=None, errata_email_notif=None, org_admin_enable=None,
                crash_file_size_limit=None, crash_reporting=None, crash_file_upload=None,
                scap_file_upload=None, scap_file_size_limit=None, scap_result_delete=None,
                scap_result_retention_days=None):
    """
    Ensure org present or managed with the given state.

    :param name:
    :param admin_login:
    :param admin_password:
    :param admin_prefix:
    :param first_name:
    :param last_name:
    :param email:
    :param pam:
    :param content_staging:
    :param errata_email_notif:
    :param org_admin_enable:
    :param crash_file_size_limit:
    :param crash_reporting:
    :param crash_file_upload:
    :param scap_file_upload:
    :param scap_file_size_limit:
    :param scap_result_delete:
    :param scap_result_retention_days:
    :return:
    """

    policy = UyuniOrgs.Policy()

    policy.scap_file_upload = scap_file_upload
    policy.scap_file_limit = scap_file_size_limit
    policy.scap_result_delete = scap_result_delete
    policy.scap_result_retention_days = scap_result_retention_days

    policy.crash_reporting = crash_reporting
    policy.crash_file_upload = crash_file_upload
    policy.crash_file_size_limit = crash_file_size_limit

    return UyuniOrgs().manage(name=name, admin_login=admin_login, admin_password=admin_password,
                              admin_prefix=admin_prefix, first_name=first_name, last_name=last_name, email=email,
                              pam=pam, content_staging=content_staging, errata_email_notif=errata_email_notif,
                              org_admin_enable=org_admin_enable, policy=policy)


def org_absent(name):
    """
    Delete organisation.

    :param name:
    :return:
    """
    return UyuniOrgs().delete(name=name)


def org_trusted(name, organisations):
    """
    Add trusted organisations to the trusted org.

    :param name:
    :param organisations:
    :return:
    """
    return UyuniOrgs().trust(name, organisations=organisations)


def org_untrusted(name, organisations):
    """
    Remove trusted organisations from the trusted org.

    :param name:
    :param organisations:
    :return:
    """
    return UyuniOrgs().untrust(name, organisations=organisations)
