import logging
from typing import Optional, Dict, Any, List, Tuple
from collections import Counter
import pdb

log = logging.getLogger(__name__)

__salt__: Dict[str, Any] = {}
__opts__: Dict[str, Any] = {}
__virtualname__ = 'uyuni'


class StateResult:

    @staticmethod
    def state_error(name: str, comment: str = None):
        return StateResult.prepare_result(name, False, comment)

    @staticmethod
    def prepare_result(name: str, result: Optional[bool], comment: str = None, changes: Dict = {}):
        return {
            'name': name,
            'changes': changes,
            'result': result,
            'comment': comment,
        }


class UyuniUsers:

    @staticmethod
    def _update_user_roles(name: str,
                           current_roles: List[str] = [],
                           new_roles: List[str] = [],
                           org_admin_user: str = None,
                           org_admin_password: str = None):

        for role_to_remove in (current_roles or []):
            if role_to_remove not in (new_roles or []):
                __salt__['uyuni.user_remove_role'](name, role=role_to_remove,
                                                   org_admin_user=org_admin_user,
                                                   org_admin_password=org_admin_password)

        for role_to_add in (new_roles or []):
            if role_to_add not in (current_roles or []):
                __salt__['uyuni.user_add_role'](name, role=role_to_add,
                                                org_admin_user=org_admin_user,
                                                org_admin_password=org_admin_password)

    @staticmethod
    def _update_user_system_groups(name: str,
                                   current_system_groups: List[str] = [],
                                   system_groups: List[str] = [],
                                   org_admin_user: str = None,
                                   org_admin_password: str = None):

        systems_groups_add = [sys for sys in (system_groups or []) if sys not in (current_system_groups or [])]
        if systems_groups_add:
            __salt__['uyuni.user_add_assigned_system_groups'](uid=name, server_group_names=systems_groups_add,
                                                              org_admin_user=org_admin_user,
                                                              org_admin_password=org_admin_password)

        system_groups_remove = [sys for sys in (current_system_groups or []) if sys not in (system_groups or [])]
        if system_groups_remove:
            __salt__['uyuni.user_remove_assigned_system_groups'](uid=name, server_group_names=system_groups_remove,
                                                                 org_admin_user=org_admin_user,
                                                                 org_admin_password=org_admin_password)

    @staticmethod
    def _compute_changes(user_changes: Dict[str, Any],
                         current_user: Dict[str, Any],
                         roles: List[str],
                         current_roles: List[str],
                         system_groups: List[str],
                         current_system_groups: List[str]):
        changes = {}
        error = None
        # user fields changes
        for field in ["email", "first_name", "last_name"]:
            if (current_user or {}).get(field) != user_changes.get(field):
                changes[field] = {"new": user_changes[field]}
                if current_user:
                    changes[field]["old"] = (current_user or {}).get(field)

        # roles changes
        if Counter(roles or []) != Counter(current_roles or []):
            changes['roles'] = {'new': roles}
            if current_roles:
                changes['roles']['old'] = current_roles

        # system group changes
        if Counter(system_groups or []) != Counter(current_system_groups or []):
            changes['system_groups'] = {'new': system_groups}
            if current_system_groups:
                changes['system_groups']['old'] = current_system_groups

        # check if password have changed
        if current_user:
            try:
                __salt__['uyuni.user_get_details'](user_changes.get('uid'),
                                                   user_changes.get('password'))
            except Exception as exc:
                # check if it's an authentication error. If yes, password have changed
                if exc.faultCode == 2950:
                    changes["password"] = {"new": "(hidden)", "old": "(hidden)"}
                else:
                    error = exc
        return changes, error

    def manage(self, uid: str, password: str, email: str, first_name: str, last_name: str,
               roles: Optional[List[str]] = [], system_groups: Optional[List[str]] = [],
               org_admin_user: str = None, org_admin_password: str = None) -> Dict[str, Any]:
        """
        Manage user, insuring it is present with all his characteristics

        :param uid: user ID
        :param password: desired password for the user
        :param email: valid email address
        :param first_name: First name
        :param last_name: Second name
        :param roles: roles to assign to user
        :param system_groups: system groups to assign user to
        :param org_admin_user: organization administrator username
        :param org_admin_password: organization administrator password
        :return: dict for Salt communication
        """
        current_user = None
        current_roles = None
        current_system_groups_names = None
        try:
            current_user = __salt__['uyuni.user_get_details'](uid, org_admin_user=org_admin_user,
                                                              org_admin_password=org_admin_password)
            current_roles = __salt__['uyuni.user_list_roles'](uid, org_admin_user=org_admin_user,
                                                              org_admin_password=org_admin_password)
            current_system_groups = __salt__['uyuni.user_list_assigned_system_groups'](uid,
                                                                                       org_admin_user=org_admin_user,
                                                                                       org_admin_password=org_admin_password)
            current_system_groups_names = [s["name"] for s in (current_system_groups or [])]
        except Exception as exc:
            if exc.faultCode == 2950:
                log.warning("Error managing user (admin credentials error) '{}': {}".format(uid, exc))
                return StateResult.state_error(uid,
                                               comment="Error managing user (admin credentials error) '{}': {}".format(
                                                   uid, exc))
            pass

        user_paramters = {"uid": uid, "password": password, "email": email,
                          "first_name": first_name, "last_name": last_name,
                          "org_admin_user": org_admin_user, "org_admin_password": org_admin_password}

        changes, error = self._compute_changes(user_paramters, current_user,
                                               roles, current_roles,
                                               system_groups, current_system_groups_names)

        if error:
            return StateResult.state_error(uid, "Error managing user '{}': {}".format(uid, error))
        if not changes:
            return StateResult.prepare_result(uid, True, "{0} is already installed".format(uid))
        if not current_user:
            changes['uid'] = {"new": uid}
            changes['password'] = {"new": "(hidden)"}
        if __opts__['test']:
            return StateResult.prepare_result(uid, None, "{0} would be installed".format(uid), changes)

        try:
            if current_user:
                __salt__['uyuni.user_set_details'](**user_paramters)
            else:
                __salt__['uyuni.user_create'](**user_paramters)

            self._update_user_roles(uid, current_roles, roles,
                                    org_admin_user, org_admin_password)
            self._update_user_system_groups(uid, current_system_groups_names, system_groups,
                                            org_admin_user, org_admin_password)
        except Exception as exc:
            return StateResult.state_error(uid, "Error managing user '{}': {}".format(uid, exc))
        else:
            return StateResult.prepare_result(uid, True, "{0} user successful managed".format(uid), changes)

    def delete(self, uid: str, org_admin_user: str = None, org_admin_password: str = None) -> Dict[str, Any]:
        """
        Remove user from the Uyuni Server

        :param org_admin_user: organization administrator username
        :param org_admin_password: organization administrator password
        :param uid: UID of the user

        :return: dict for Salt communication
        """
        try:
            user = __salt__['uyuni.user_get_details'](uid, org_admin_user=org_admin_user,
                                                      org_admin_password=org_admin_password)
        except Exception as exc:
            if exc.faultCode == -213:
                return StateResult.prepare_result(uid, True, "{0} is already absent".format(uid))
            if exc.faultCode == 2950:
                return StateResult.state_error(uid,
                                               "Error deleting user (organization credentials error) '{}': {}".format(
                                                   uid, exc))
            raise exc
        else:
            changes = {
                'uid': {'old': uid},
                'email': {'old': user.get('email')},
                'first_name': {'old': user.get('first_name')},
                'last_name': {'old': user.get('last_name')}
            }
            if __opts__['test']:
                return StateResult.prepare_result(uid, None, "{0} would be removed".format(uid), changes)

            try:
                __salt__['uyuni.user_delete'](uid,
                                              org_admin_user=org_admin_user,
                                              org_admin_password=org_admin_password)
                return StateResult.prepare_result(uid, True, "User {} has been deleted".format(uid), changes)
            except Exception as exc:
                return StateResult.state_error(uid, "Error deleting user '{}': {}".format(uid, exc))


class UyuniUserChannels:

    @staticmethod
    def process_changes(current_managed_channels: Optional[List[str]],
                        new_managed_channels: Optional[List[str]],
                        current_subscribe_channels: List[str],
                        new_subscribe_channels: List[str],
                        org_admin_user: str, org_admin_password: str) -> Dict[str, Dict[str, bool]]:
        managed_changes: Dict[str, bool] = {}
        managed_changes.update({new_ma: True for new_ma in (new_managed_channels or [])
                                if new_ma not in current_managed_channels})

        managed_changes.update({old_ma: False for old_ma in (current_managed_channels or [])
                                if old_ma not in new_managed_channels})

        subscribe_changes: Dict[str, bool] = {}
        for new_channel in (new_subscribe_channels or []):
            if new_channel not in (current_subscribe_channels or []) or not managed_changes.get(new_channel, True):
                subscribe_changes[new_channel] = True

        for curr_channel in (current_subscribe_channels or []):
            if not (curr_channel in new_subscribe_channels or curr_channel in new_managed_channels):
                if not __salt__['uyuni.channel_software_is_global_subscribable'](curr_channel,
                                                                                 org_admin_user,
                                                                                 org_admin_password):
                    subscribe_changes[curr_channel] = False
        changes = {}
        if managed_changes:
            changes['manageable_channels'] = managed_changes
        if subscribe_changes:
            changes['subscribable_channels'] = subscribe_changes
        return changes

    def manage(self, uid: str, password: str,
               manageable_channels: Optional[List[str]] = [],
               subscribable_channels: Optional[List[str]] = [],
               org_admin_user: str = None, org_admin_password: str = None) -> Dict[str, Any]:
        """
        User channels management present implementation

        :param uid: user ID
        :param password: user password
        :param manageable_channels: channels user can manage
        :param subscribable_channels: channels user can subscribe
        :param org_admin_user: organization administrator username
        :param org_admin_password: organization administrator password
        :return: dict for Salt communication
        """
        try:
            current_roles = __salt__['uyuni.user_list_roles'](uid, password=password)
            current_manageable_channels = __salt__['uyuni.channel_list_manageable_channels'](uid, password)
            current_subscribe_channels = __salt__['uyuni.channel_list_my_channels'](uid, password)
        except Exception as exc:
            return StateResult.state_error(uid,
                                           comment="Error managing user channels '{}': {}".format(uid, exc))
            pass

        if "org_admin" in current_roles or "channel_admin" in current_roles:
            return StateResult.state_error(uid, "Channels access cannot be managed, "
                                                "User can manage all channel in the organization "
                                                "(\"org_admin\" or \"org channel_admin\" role).")

        current_manageable_channels_list = [c.get("label") for c in (current_manageable_channels or [])]
        current_subscribe_channels_list = [c.get("label") for c in (current_subscribe_channels or [])]

        changes = self.process_changes(current_manageable_channels_list,
                                       manageable_channels,
                                       current_subscribe_channels_list, subscribable_channels,
                                       org_admin_user, org_admin_password)

        if not changes:
            return StateResult.prepare_result(uid, True, "{0} channels is already installed".format(uid))
        if __opts__['test']:
            return StateResult.prepare_result(uid, None, "{0} channels would be installed".format(uid), changes)

        try:
            for channel, action in changes.get('manageable_channels', {}).items():
                __salt__['uyuni.channel_software_set_user_manageable'](channel, uid, action,
                                                                       org_admin_user, org_admin_password)

            for channel, action in changes.get('subscribable_channels', {}).items():
                __salt__['uyuni.channel_software_set_user_subscribable'](channel, uid, action,
                                                                         org_admin_user, org_admin_password)
        except Exception as exc:
            return StateResult.state_error(uid, "Error managing Channel management '{}': {}".format(uid, exc))
        return StateResult.prepare_result(uid, True, "Channel management successful managed", changes)


class UyuniGroups:

    @staticmethod
    def _update_systems(name: str, new_systems: List[int], current_systems: List[int],
                        org_admin_user: str = None, org_admin_password: str = None):

        remove_systems = [sys for sys in current_systems if sys not in new_systems]
        if remove_systems:
            __salt__['uyuni.systemgroup_add_remove_systems'](name, False, remove_systems,
                                                             org_admin_user=org_admin_user,
                                                             org_admin_password=org_admin_password)

        add_systems = [sys for sys in new_systems if sys not in current_systems]
        if add_systems:
            __salt__['uyuni.systemgroup_add_remove_systems'](name, True, add_systems,
                                                             org_admin_user=org_admin_user,
                                                             org_admin_password=org_admin_password)

    @staticmethod
    def _get_systems_for_group(matching: str, target: str = "glob",
                               org_admin_user: str = None, org_admin_password: str = None):

        selected_minions = __salt__['uyuni.master_select_minions'](matching, target)
        available_system_ids = __salt__['uyuni.systems_get_minion_id_map'](org_admin_user, org_admin_password)

        return [
            available_system_ids[minion_id] for minion_id in selected_minions.get('minions', [])
            if minion_id in available_system_ids
        ]

    def manage(self, name: str, description: str, expression: str, target: str = "glob",
               org_admin_user: str = None, org_admin_password: str = None) -> Dict[str, Any]:
        """
        Create or update group
        :param name: group name
        :param description: group description
        :param expression: expression used to filter which minions should be part of the group
        :param target: target type, one of the following: glob, grain, grain_pcre, pillar, pillar_pcre,
                pillar_exact, compound, compound_pillar_exact. Default: glob.
        :param org_admin_user: organization administrator username
        :param org_admin_password: organization administrator password
        :return: dict for Salt communication
        """
        current_group = None
        current_systems = None
        try:
            current_group = __salt__['uyuni.systemgroup_get_details'](name,
                                                                      org_admin_user=org_admin_user,
                                                                      org_admin_password=org_admin_password)
            current_systems = __salt__['uyuni.systemgroup_list_systems'](name,
                                                                         org_admin_user=org_admin_user,
                                                                         org_admin_password=org_admin_password)
        except Exception as exc:
            if exc.faultCode != 2201:
                return StateResult.state_error(name, "Error managing group '{}': {}".format(name, exc))
            pass

        current_systems_ids = [sys['id'] for sys in (current_systems or [])]
        systems_to_group = self._get_systems_for_group(expression, target,
                                                       org_admin_user=org_admin_user,
                                                       org_admin_password=org_admin_password)

        changes = {}
        if description != (current_group or {}).get('description'):
            changes['description'] = {'new': description}
            if current_group:
                changes['description']['old'] = current_group["description"]

        if Counter(current_systems_ids or []) != Counter(systems_to_group or []):
            changes['systems'] = {'new': systems_to_group}
            if current_group:
                changes['systems']['old'] = current_systems_ids

        if not changes:
            return StateResult.prepare_result(name, True, "{0} is already installed".format(name))

        if not current_group:
            changes["name"] = {"new": name}

        if __opts__['test']:
            return StateResult.prepare_result(name, None, "{0} would be updated".format(name), changes)

        try:
            if current_group:
                __salt__['uyuni.systemgroup_update'](name, description,
                                                     org_admin_user=org_admin_user,
                                                     org_admin_password=org_admin_password)

                self._update_systems(name,
                                     systems_to_group,
                                     current_systems_ids,
                                     org_admin_user=org_admin_user,
                                     org_admin_password=org_admin_password)
            else:
                __salt__['uyuni.systemgroup_create'](name, description,
                                                     org_admin_user=org_admin_user,
                                                     org_admin_password=org_admin_password)
                self._update_systems(name,
                                     systems_to_group,
                                     current_systems_ids,
                                     org_admin_user=org_admin_user,
                                     org_admin_password=org_admin_password)
        except Exception as exc:
            return StateResult.state_error(name, "Error managing group. '{}': {}".format(name, exc))
        else:
            return StateResult.prepare_result(name, True, "{0} successfully managed".format(name), changes)

    def delete(self, name: str, org_admin_user: str = None, org_admin_password: str = None) -> Dict[str, Any]:
        """
        Remove group from the Uyuni

        :param name: Group Name
        :param org_admin_user: organization administrator username
        :param org_admin_password: organization administrator password

        :return: dict for Salt communication
        """
        try:
            current_group = __salt__['uyuni.systemgroup_get_details'](name,
                                                                      org_admin_user=org_admin_user,
                                                                      org_admin_password=org_admin_password)
        except Exception as exc:
            if exc.faultCode == 2201:
                return StateResult.prepare_result(name, True, "{0} is already absent".format(name))
            if exc.faultCode == 2950:
                return StateResult.state_error(name,
                                               "Error deleting group (organization admin credentials error) '{}': {}"
                                               .format(name, exc))
            raise exc
        else:
            if __opts__['test']:
                return StateResult.prepare_result(name, None, "{0} would be removed".format(name))
            try:
                __salt__['uyuni.systemgroup_delete'](name,
                                                     org_admin_user=org_admin_user,
                                                     org_admin_password=org_admin_password)
                return StateResult.prepare_result(name, True, "Group {} has been deleted".format(name),
                                                  {'name': {'old': current_group.get('name')},
                                                   'description': {'old': current_group.get('description')}})
            except Exception as exc:
                return StateResult.state_error(name, "Error deleting group '{}': {}".format(name, exc))


class UyuniOrgs:

    @staticmethod
    def _compute_changes(user_changes: Dict[str, Any],
                         current_user: Dict[str, Any]) -> Dict[str, Any]:
        changes = {}
        for field in ["email", "first_name", "last_name"]:
            if (current_user or {}).get(field) != user_changes.get(field):
                changes[field] = {"new": user_changes[field]}
                if current_user:
                    changes[field]["old"] = (current_user or {}).get(field)
        return changes

    def manage(self, name: str, org_admin_user: str, org_admin_password: str, first_name: str,
               last_name: str, email: str, pam: bool = False,
               admin_user=None, admin_password=None) -> Dict[str, Any]:
        """
        Manage organization.
        Admin user must have SUSE Manager Administrator role to perform this action

        :param name: organization name
        :param org_admin_user: organization admin user
        :param org_admin_password: organization admin password
        :param first_name: organization admin first name
        :param last_name: organization admin last name
        :param email: organization admin email
        :param pam: organization admin pam authentication
        :param admin_user: uyuni admin user
        :param admin_password: uyuni admin password
        :return: dict for Salt communication
        """
        current_org = None
        current_org_admin = None
        try:
            current_org = __salt__['uyuni.org_get_details'](name,
                                                            admin_user=admin_user,
                                                            admin_password=admin_password)
            current_org_admin = __salt__['uyuni.user_get_details'](org_admin_user,
                                                                   org_admin_user=org_admin_user,
                                                                   org_admin_password=org_admin_password)
        except Exception as exc:
            if exc.faultCode != 2850:
                return StateResult.state_error(name, "Error managing org '{}': {}".format(name, exc))

        user_paramters = {"uid": org_admin_user, "password": org_admin_password, "email": email,
                          "first_name": first_name, "last_name": last_name,
                          "org_admin_user": org_admin_user, "org_admin_password": org_admin_password}

        changes = self._compute_changes(user_paramters, current_org_admin)
        if not current_org:
            changes["org_name"] = {"new": name}
            changes["org_admin_user"] = {"new": org_admin_user}
            changes["pam"] = {"new": pam}

        if not changes:
            return StateResult.prepare_result(name, True, "{0} is already installed".format(name))
        if __opts__['test']:
            return StateResult.prepare_result(name, None, "{0} would be installed".format(name), changes)

        try:
            if current_org:
                __salt__['uyuni.user_set_details'](**user_paramters)
            else:
                __salt__['uyuni.org_create'](name=name,
                                             org_admin_user=org_admin_user, org_admin_password=org_admin_password,
                                             first_name=first_name, last_name=last_name, email=email,
                                             admin_user=admin_user, admin_password=admin_password, pam=pam)

        except Exception as exc:
            return StateResult.state_error(name, "Error managing org '{}': {}".format(name, exc))
        else:
            return StateResult.prepare_result(name, True, "{0} org successful managed".format(name), changes)

    def delete(self, name: str, admin_user=None, admin_password=None) -> Dict[str, Any]:
        """
        Remove organization from the Uyuni
        Admin user must have SUSE Manager Administrator role to perform this action

        :param name: Organization Name
        :param admin_user: administrator username
        :param admin_password: administrator password

        :return: dict for Salt communication
        """
        try:
            current_org = __salt__['uyuni.org_get_details'](name,
                                                            admin_user=admin_user,
                                                            admin_password=admin_password)
        except Exception as exc:
            if exc.faultCode == 2850:
                return StateResult.prepare_result(name, True, "{0} is already absent".format(name))
            if exc.faultCode == 2950:
                return StateResult.state_error(name,
                                               "Error deleting organization (admin credentials error) '{}': {}"
                                               .format(name, exc))
            raise exc
        else:
            if __opts__['test']:
                return StateResult.prepare_result(name, None, "{0} would be removed".format(name))
            try:
                __salt__['uyuni.org_delete'](name,
                                             admin_user=admin_user,
                                             admin_password=admin_password)
                return StateResult.prepare_result(name, True, "Org {} has been deleted".format(name),
                                                  {'name': {'old': current_org.get('name')}})
            except Exception as exc:
                return StateResult.state_error(name, "Error deleting Org '{}': {}".format(name, exc))


class UyuniOrgsTrust:

    def trust(self, name: str, org_name: str, orgs_trust: List[str],
              admin_user: str = None, admin_password: str = None) -> Dict[str, Any]:
        """
        Add trusted organisations to the a org

        :param name: state name
        :param org_name: organization name
        :param orgs_trust: list of organization names to trust
        :param admin_user: administrator username
        :param admin_password: administrator password
        :return: dict for Salt communication
        """
        try:
            org_trusts = __salt__['uyuni.org_trust_list_trusts'](org_name,
                                                                 admin_user=admin_user, admin_password=admin_password)
            current_org = __salt__['uyuni.org_get_details'](org_name,
                                                            admin_user=admin_user, admin_password=admin_password)
        except Exception as exc:
            return StateResult.state_error(name, "Error managing org Trust'{}': {}".format(org_name, exc))

        trusts_to_add = []
        trusts_to_remove = []
        for org_trust in org_trusts:
            if org_trust.get("orgName") in (orgs_trust or []) and not org_trust.get("trustEnabled"):
                trusts_to_add.append(org_trust)
            elif org_trust.get("orgName") not in (orgs_trust or []) and org_trust.get("trustEnabled"):
                trusts_to_remove.append(org_trust)

        if not trusts_to_add and not trusts_to_remove:
            return StateResult.prepare_result(name, True, "{0} is already installed".format(org_name))
        if __opts__['test']:
            changes = {}
            for org_add in trusts_to_add:
                changes[org_add.get("orgName")] = {'old': None, 'new': True}
            for org_remove in trusts_to_remove:
                changes[org_remove.get("orgName")] = {'old': True, 'new': None}
            return StateResult.prepare_result(name, None, "{0} would be installed".format(org_name), changes)

        processed_changes = {}
        try:
            for org_add in trusts_to_add:
                __salt__['uyuni.org_trust_add_trust'](current_org.get("id"), org_add.get("orgId"),
                                                      admin_user=admin_user, admin_password=admin_password)
                processed_changes[org_add.get("orgName")] = {'old': None, 'new': True}
            for org_remove in trusts_to_remove:
                __salt__['uyuni.org_trust_remove_trust'](current_org.get("id"), org_remove.get("orgId"),
                                                         admin_user=admin_user, admin_password=admin_password)
                processed_changes[org_remove.get("orgName")] = {'old': True, 'new': None}
        except Exception as exc:
            return StateResult.prepare_result(name, False, "Error managing Org Trust '{}': {}".format(org_name, exc),
                                              processed_changes)
        return StateResult.prepare_result(name, True, "Org '{}' Trust successful managed".format(org_name), processed_changes)


def __virtual__():
    '''
    TODO add a check to Only Runs in Uyuni server
    '''
    return __virtualname__


def user_present(name, password, email, first_name, last_name,
                 roles=None, system_groups=None,
                 org_admin_user=None, org_admin_password=None):
    """
    Insure user is present with all his characteristics

    :param name: user ID
    :param password: desired password for the user
    :param email: valid email address
    :param first_name: First name
    :param last_name: Second name
    :param roles: roles to assign to user
    :param system_groups: system_groups to assign to user
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password
    :return: dict for Salt communication
    """
    return UyuniUsers().manage(name, password, email, first_name, last_name,
                               roles, system_groups,
                               org_admin_user, org_admin_password)


def user_channels(name, password,
                  manageable_channels=[], subscribable_channels=[],
                  org_admin_user=None, org_admin_password=None):
    """
    Insure user is present with all his characteristics

    :param name: user ID
    :param password: user password
    :param manageable_channels: channels user can manage
    :param subscribable_channels: channels user can subscribe
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password
    :return: dict for Salt communication
    """
    return UyuniUserChannels().manage(name, password,
                                      manageable_channels, subscribable_channels,
                                      org_admin_user, org_admin_password)


def user_absent(name, org_admin_user=None, org_admin_password=None):
    """

    :param name: user id
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password
    :return:
    """
    return UyuniUsers().delete(name, org_admin_user, org_admin_password)


def org_present(name, org_admin_user, org_admin_password,
                first_name, last_name, email, pam=False,
                admin_user=None, admin_password=None):
    """
    Create or update uyuni organization
    Admin user must have SUSE Manager Administrator role to perform this action

    :param name: organization name
    :param org_admin_user: organization admin user
    :param org_admin_password: organization admin password
    :param first_name: organization admin first name
    :param last_name: organization admin last name
    :param email: organization admin email
    :param pam: organization admin pam authentication
    :param admin_user: uyuni admin user
    :param admin_password: uyuni admin password
    :return: dict for Salt communication
    """
    return UyuniOrgs().manage(name, org_admin_user, org_admin_password, first_name,
                              last_name, email, pam,
                              admin_user, admin_password)


def org_absent(name, admin_user=None, admin_password=None):
    """
    Delete uyuni organization
    Admin user must have SUSE Manager Administrator role to perform this action

    :param name: organization name
    :param admin_user: uyuni admin user
    :param admin_password: uyuni admin password
    :return: dict for Salt communication
    """
    return UyuniOrgs().delete(name, admin_user, admin_password)


def org_trust(name, org_name, trusts, admin_user=None, admin_password=None):
    """
    Add trusted organisations from a organization.
    :param name: state name
    :param org_name: Organization name
    :param trusts: list of organization names to trust
    :param admin_user: administrator username
    :param admin_password: administrator password
    :return: dict for Salt communication
    """
    return UyuniOrgsTrust().trust(name, org_name, trusts, admin_user, admin_password)


def group_present(name, description, expression=None, target="glob",
                  org_admin_user=None, org_admin_password=None):
    """
    Create or update group

    :param name: group name
    :param description: group description
    :param expression: expression used to filter which minions should be part of the group
    :param target: target type, one of the following: glob, grain, grain_pcre, pillar, pillar_pcre,
            pillar_exact, compound, compound_pillar_exact. Default: glob.
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password
    :return: dict for Salt communication
    """
    return UyuniGroups().manage(name, description, expression, target,
                                org_admin_user, org_admin_password)


def group_absent(name, org_admin_user=None, org_admin_password=None):
    """
    Remove group from the Uyuni

    :param name: Group Name
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password

    :return: dict for Salt communication
    """
    return UyuniGroups().delete(name, org_admin_user, org_admin_password)
