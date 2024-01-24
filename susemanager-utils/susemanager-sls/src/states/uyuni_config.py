import logging
from typing import Optional, Dict, Any, List, Tuple
from collections import Counter

SERVER_GROUP_NOT_FOUND_ERROR = 2201
NO_SUCH_USER_ERROR = -213
ORG_NOT_FOUND_ERROR = 2850
ACTIVATION_KEY_NOT_FOUND_ERROR = -212
AUTHENTICATION_ERROR = 2950

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
            __salt__['uyuni.user_add_assigned_system_groups'](login=name, server_group_names=systems_groups_add,
                                                              org_admin_user=org_admin_user,
                                                              org_admin_password=org_admin_password)

        system_groups_remove = [sys for sys in (current_system_groups or []) if sys not in (system_groups or [])]
        if system_groups_remove:
            __salt__['uyuni.user_remove_assigned_system_groups'](login=name, server_group_names=system_groups_remove,
                                                                 org_admin_user=org_admin_user,
                                                                 org_admin_password=org_admin_password)

    @staticmethod
    def _compute_changes(user_changes: Dict[str, Any],
                         current_user: Dict[str, Any],
                         roles: List[str],
                         current_roles: List[str],
                         system_groups: List[str],
                         current_system_groups: List[str],
                         use_pam_auth: bool = False):
        changes = {}
        error = None
        # user field changes
        for field in ["email", "first_name", "last_name"]:
            if (current_user or {}).get(field) != user_changes.get(field):
                changes[field] = {"new": user_changes[field]}
                if current_user:
                    changes[field]["old"] = (current_user or {}).get(field)

        # role changes
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
        if current_user and not use_pam_auth:
            try:
                __salt__['uyuni.user_get_details'](user_changes.get('login'),
                                                   user_changes.get('password'))
            except Exception as exc:
                # check if it's an authentication error. If yes, password have changed
                if exc.faultCode == AUTHENTICATION_ERROR:
                    changes["password"] = {"new": "(hidden)", "old": "(hidden)"}
                else:
                    error = exc
        return changes, error

    def manage(self, login: str, password: str, email: str, first_name: str, last_name: str, use_pam_auth: bool = False,
               roles: Optional[List[str]] = [], system_groups: Optional[List[str]] = [],
               org_admin_user: str = None, org_admin_password: str = None) -> Dict[str, Any]:
        """
        Ensure a user is present with all specified properties

        :param login: user login ID
        :param password: desired password for the user
        :param email: valid email address
        :param first_name: First name
        :param last_name: Last name
        :param use_pam_auth: if you wish to use PAM authentication for this user
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
            current_user = __salt__['uyuni.user_get_details'](login, org_admin_user=org_admin_user,
                                                              org_admin_password=org_admin_password)
            current_roles = __salt__['uyuni.user_list_roles'](login, org_admin_user=org_admin_user,
                                                              org_admin_password=org_admin_password)
            current_system_groups = __salt__['uyuni.user_list_assigned_system_groups'](login,
                                                                                       org_admin_user=org_admin_user,
                                                                                       org_admin_password=org_admin_password)
            current_system_groups_names = [s["name"] for s in (current_system_groups or [])]
        except Exception as exc:
            if exc.faultCode == AUTHENTICATION_ERROR:
                error_message = "Error while retrieving user information (admin credentials error) '{}': {}".format(
                    login, exc)
                log.warning(error_message)
                return StateResult.state_error(login, comment=error_message)

        user_paramters = {"login": login, "password": password, "email": email,
                          "first_name": first_name, "last_name": last_name,
                          "org_admin_user": org_admin_user, "org_admin_password": org_admin_password}

        changes, error = self._compute_changes(user_paramters, current_user,
                                               roles, current_roles,
                                               system_groups, current_system_groups_names,
                                               use_pam_auth=use_pam_auth)

        if error:
            return StateResult.state_error(login, "Error computing changes for user '{}': {}".format(login, error))
        if not changes:
            return StateResult.prepare_result(login, True, "{0} is already in the desired state".format(login))
        if not current_user:
            changes['login'] = {"new": login}
            changes['password'] = {"new": "(hidden)"}
        if __opts__['test']:
            return StateResult.prepare_result(login, None, "{0} would be modified".format(login), changes)

        try:
            if current_user:
                __salt__['uyuni.user_set_details'](**user_paramters)
            else:
                user_paramters["use_pam_auth"] = use_pam_auth
                __salt__['uyuni.user_create'](**user_paramters)

            self._update_user_roles(login, current_roles, roles,
                                    org_admin_user, org_admin_password)
            self._update_user_system_groups(login, current_system_groups_names, system_groups,
                                            org_admin_user, org_admin_password)
        except Exception as exc:
            return StateResult.state_error(login, "Error modifying user '{}': {}".format(login, exc))
        else:
            return StateResult.prepare_result(login, True, "{0} user successfully modified".format(login), changes)

    def delete(self, login: str, org_admin_user: str = None, org_admin_password: str = None) -> Dict[str, Any]:
        """
        Remove an Uyuni user

        :param org_admin_user: organization administrator username
        :param org_admin_password: organization administrator password
        :param login: login of the user

        :return: dict for Salt communication
        """
        try:
            user = __salt__['uyuni.user_get_details'](login, org_admin_user=org_admin_user,
                                                      org_admin_password=org_admin_password)
        except Exception as exc:
            if exc.faultCode == NO_SUCH_USER_ERROR:
                return StateResult.prepare_result(login, True, "{0} is already absent".format(login))
            if exc.faultCode == AUTHENTICATION_ERROR:
                return StateResult.state_error(login,
                                               "Error deleting user (organization credentials error) '{}': {}".format(
                                                   login, exc))
            raise exc
        else:
            changes = {
                'login': {'old': login},
                'email': {'old': user.get('email')},
                'first_name': {'old': user.get('first_name')},
                'last_name': {'old': user.get('last_name')}
            }
            if __opts__['test']:
                return StateResult.prepare_result(login, None, "{0} would be deleted".format(login), changes)

            try:
                __salt__['uyuni.user_delete'](login,
                                              org_admin_user=org_admin_user,
                                              org_admin_password=org_admin_password)
                return StateResult.prepare_result(login, True, "User {} has been deleted".format(login), changes)
            except Exception as exc:
                return StateResult.state_error(login, "Error deleting user '{}': {}".format(login, exc))


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
                if not __salt__['uyuni.channel_software_is_globally_subscribable'](curr_channel,
                                                                                   org_admin_user,
                                                                                   org_admin_password):
                    subscribe_changes[curr_channel] = False
        changes = {}
        if managed_changes:
            changes['manageable_channels'] = managed_changes
        if subscribe_changes:
            changes['subscribable_channels'] = subscribe_changes
        return changes

    def manage(self, login: str, password: str,
               manageable_channels: Optional[List[str]] = [],
               subscribable_channels: Optional[List[str]] = [],
               org_admin_user: str = None, org_admin_password: str = None) -> Dict[str, Any]:
        """
        Modifies user-channel associations

        :param login: user login ID
        :param password: user password
        :param manageable_channels: channels user can manage
        :param subscribable_channels: channels user can subscribe
        :param org_admin_user: organization administrator username
        :param org_admin_password: organization administrator password
        :return: dict for Salt communication
        """
        try:
            current_roles = __salt__['uyuni.user_list_roles'](login, password=password)
            current_manageable_channels = __salt__['uyuni.channel_list_manageable_channels'](login, password)
            current_subscribe_channels = __salt__['uyuni.channel_list_my_channels'](login, password)
        except Exception as exc:
            return StateResult.state_error(login,
                                           comment="Error retrieving information about user channels '{}': {}".format(
                                               login, exc))

        if "org_admin" in current_roles or "channel_admin" in current_roles:
            return StateResult.state_error(login, "Channels access cannot be changed, because "
                                                  "the target user can manage all channels in the organization "
                                                  "(having an \"org_admin\" or \"channel_admin\" role).")

        current_manageable_channels_list = [c.get("label") for c in (current_manageable_channels or [])]
        current_subscribe_channels_list = [c.get("label") for c in (current_subscribe_channels or [])]

        changes = self.process_changes(current_manageable_channels_list,
                                       manageable_channels,
                                       current_subscribe_channels_list, subscribable_channels,
                                       org_admin_user, org_admin_password)

        if not changes:
            return StateResult.prepare_result(login, True,
                                              "{0} channels are already in the desired state".format(login))
        if __opts__['test']:
            return StateResult.prepare_result(login, None, "{0} channels would be configured".format(login), changes)

        try:
            for channel, action in changes.get('manageable_channels', {}).items():
                __salt__['uyuni.channel_software_set_user_manageable'](channel, login, action,
                                                                       org_admin_user, org_admin_password)

            for channel, action in changes.get('subscribable_channels', {}).items():
                __salt__['uyuni.channel_software_set_user_subscribable'](channel, login, action,
                                                                         org_admin_user, org_admin_password)
        except Exception as exc:
            return StateResult.state_error(login, "Error changing channel assignments '{}': {}".format(login, exc))
        return StateResult.prepare_result(login, True, "Channel set to the desired state", changes)


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
    def _get_systems_for_group(target: str, target_type: str = "glob",
                               org_admin_user: str = None, org_admin_password: str = None):

        selected_minions = __salt__['uyuni.master_select_minions'](target, target_type)
        available_system_ids = __salt__['uyuni.systems_get_minion_id_map'](org_admin_user, org_admin_password)

        return [
            available_system_ids[minion_id] for minion_id in selected_minions.get('minions', [])
            if minion_id in available_system_ids
        ]

    def manage(self, name: str, description: str, target: str, target_type: str = "glob",
               org_admin_user: str = None, org_admin_password: str = None) -> Dict[str, Any]:
        """
        Create or update a system group

        :param name: group name
        :param description: group description
        :param target: target expression used to filter which minions should be part of the group
        :param target_type: target type, one of the following: glob, grain, grain_pcre, pillar, pillar_pcre,
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
            if exc.faultCode != SERVER_GROUP_NOT_FOUND_ERROR:
                return StateResult.state_error(name,
                                               "Error retrieving information about system group '{}': {}".format(name,
                                                                                                                 exc))

        current_systems_ids = [sys['id'] for sys in (current_systems or [])]
        systems_to_group = self._get_systems_for_group(target, target_type,
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
            return StateResult.prepare_result(name, True, "{0} is already in the desired state".format(name))

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
            return StateResult.state_error(name, "Error updating group. '{}': {}".format(name, exc))
        else:
            return StateResult.prepare_result(name, True, "{0} successfully updated".format(name), changes)

    def delete(self, name: str, org_admin_user: str = None, org_admin_password: str = None) -> Dict[str, Any]:
        """
        Remove an Uyuni system group

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
            if exc.faultCode == SERVER_GROUP_NOT_FOUND_ERROR:
                return StateResult.prepare_result(name, True, "{0} is already absent".format(name))
            if exc.faultCode == AUTHENTICATION_ERROR:
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
        Create or update an Uyuni organization.
        Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

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
            if exc.faultCode != ORG_NOT_FOUND_ERROR:
                return StateResult.state_error(name,
                                               "Error retrieving information about organization '{}': {}".format(name,
                                                                                                                 exc))

        user_paramters = {"login": org_admin_user, "password": org_admin_password, "email": email,
                          "first_name": first_name, "last_name": last_name,
                          "org_admin_user": org_admin_user, "org_admin_password": org_admin_password}

        changes = self._compute_changes(user_paramters, current_org_admin)
        if not current_org:
            changes["org_name"] = {"new": name}
            changes["org_admin_user"] = {"new": org_admin_user}
            changes["pam"] = {"new": pam}

        if not changes:
            return StateResult.prepare_result(name, True, "{0} is already in the desired state".format(name))
        if __opts__['test']:
            return StateResult.prepare_result(name, None, "{0} would be updated".format(name), changes)

        try:
            if current_org:
                __salt__['uyuni.user_set_details'](**user_paramters)
            else:
                __salt__['uyuni.org_create'](name=name,
                                             org_admin_user=org_admin_user, org_admin_password=org_admin_password,
                                             first_name=first_name, last_name=last_name, email=email,
                                             admin_user=admin_user, admin_password=admin_password, pam=pam)

        except Exception as exc:
            return StateResult.state_error(name, "Error updating organization '{}': {}".format(name, exc))
        else:
            return StateResult.prepare_result(name, True, "{0} org successfully modified".format(name), changes)

    def delete(self, name: str, admin_user=None, admin_password=None) -> Dict[str, Any]:
        """
        Remove an Uyuni organization
        Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

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
            if exc.faultCode == ORG_NOT_FOUND_ERROR:
                return StateResult.prepare_result(name, True, "{0} is already absent".format(name))
            if exc.faultCode == AUTHENTICATION_ERROR:
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

    def trust(self, name: str, org_name: str, trusted_orgs: List[str],
              admin_user: str = None, admin_password: str = None) -> Dict[str, Any]:
        """
        Establish trust relationships between organizations

        :param name: state name
        :param org_name: organization name
        :param trusted_orgs: list of organization names to trust
        :param admin_user: administrator username
        :param admin_password: administrator password

        :return: dict for Salt communication
        """
        try:
            current_org_trusts = __salt__['uyuni.org_trust_list_trusts'](org_name,
                                                                         admin_user=admin_user,
                                                                         admin_password=admin_password)
            current_org = __salt__['uyuni.org_get_details'](org_name,
                                                            admin_user=admin_user, admin_password=admin_password)
        except Exception as exc:
            return StateResult.state_error(name,
                                           "Error retrieving information about an organization trust'{}': {}".format(
                                               org_name, exc))

        trusts_to_add = []
        trusts_to_remove = []
        for org_trust in current_org_trusts:
            if org_trust.get("orgName") in (trusted_orgs or []) and not org_trust.get("trustEnabled"):
                trusts_to_add.append(org_trust)
            elif org_trust.get("orgName") not in (trusted_orgs or []) and org_trust.get("trustEnabled"):
                trusts_to_remove.append(org_trust)

        if not trusts_to_add and not trusts_to_remove:
            return StateResult.prepare_result(name, True, "{0} is already in the desired state".format(org_name))
        if __opts__['test']:
            changes = {}
            for org_add in trusts_to_add:
                changes[org_add.get("orgName")] = {'old': None, 'new': True}
            for org_remove in trusts_to_remove:
                changes[org_remove.get("orgName")] = {'old': True, 'new': None}
            return StateResult.prepare_result(name, None, "{0} would be created".format(org_name), changes)

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
            return StateResult.prepare_result(name, False,
                                              "Error updating organization trusts '{}': {}".format(org_name, exc),
                                              processed_changes)
        return StateResult.prepare_result(name, True, "Org '{}' trusts successfully modified".format(org_name),
                                          processed_changes)


class UyuniActivationKeys:

    @staticmethod
    def _normalize_list_packages(list_packages: [Any]):
        return [(f['name'], f.get('arch', None)) for f in (list_packages or [])]

    @staticmethod
    def _compute_changes(ak_parameters: Dict[str, Any],
                         current_ak: Dict[str, Any],
                         configure_after_registration: bool,
                         current_configure_after_registration: bool,
                         current_config_channels: List[str],
                         configuration_channels: List[str]) -> Dict[str, Any]:
        changes = {}
        for field in ["description", 'base_channel', 'usage_limit', 'universal_default', 'contact_method']:
            if current_ak.get(field) != ak_parameters.get(field):
                changes[field] = {"new": ak_parameters[field]}
                if current_ak:
                    changes[field]["old"] = current_ak.get(field)

        # list fields
        for field in ['system_types', 'child_channels', 'server_groups']:
            if sorted((ak_parameters or {}).get(field) or []) != sorted(current_ak.get(field) or []):
                changes[field] = {"new": ak_parameters[field]}
                if current_ak:
                    changes[field]["old"] = current_ak.get(field)

        new_packages = UyuniActivationKeys._normalize_list_packages((ak_parameters or {}).get('packages', []))
        old_packages = UyuniActivationKeys._normalize_list_packages((current_ak or {}).get('packages', []))
        if sorted(new_packages) != sorted(old_packages):
            changes['packages'] = {"new": ak_parameters['packages']}
            if current_ak:
                changes['packages']["old"] = current_ak.get('packages')

        if configure_after_registration != current_configure_after_registration:
            changes['configure_after_registration'] = {"new": configure_after_registration}
            if current_configure_after_registration is not None:
                changes['configure_after_registration']["old"] = current_configure_after_registration

        # we don't want to sort configuration channels since the order matters in this case
        if (current_config_channels or []) != (configuration_channels or []):
            changes['configuration_channels'] = {"new": configuration_channels}
            if current_config_channels:
                changes['configuration_channels']['old'] = current_config_channels

        return changes

    @staticmethod
    def _update_system_type(current_system_types, new_system_types,
                            key, org_admin_user, org_admin_password):
        add_system_types = [t for t in new_system_types if t not in current_system_types]
        if add_system_types:
            __salt__['uyuni.activation_key_add_entitlements'](key, add_system_types,
                                                              org_admin_user=org_admin_user,
                                                              org_admin_password=org_admin_password)

        remove_system_types = [t for t in current_system_types if t not in new_system_types]
        if remove_system_types:
            __salt__['uyuni.activation_key_remove_entitlements'](key, remove_system_types,
                                                                 org_admin_user=org_admin_user,
                                                                 org_admin_password=org_admin_password)

    @staticmethod
    def _update_child_channels(current_child_channels, new_child_channels,
                               key, org_admin_user, org_admin_password):
        add_child_channels = [t for t in new_child_channels if t not in current_child_channels]
        if add_child_channels:
            __salt__['uyuni.activation_key_add_child_channels'](key, add_child_channels,
                                                                org_admin_user=org_admin_user,
                                                                org_admin_password=org_admin_password)

        remove_child_channels = [t for t in current_child_channels if t not in new_child_channels]
        if remove_child_channels:
            __salt__['uyuni.activation_key_remove_child_channels'](key, remove_child_channels,
                                                                   org_admin_user=org_admin_user,
                                                                   org_admin_password=org_admin_password)

    @staticmethod
    def _update_server_groups(current_server_groups, new_server_groups,
                               key, org_admin_user, org_admin_password):
        add_server_groups = [t for t in new_server_groups if t not in current_server_groups]
        if add_server_groups:
            __salt__['uyuni.activation_key_add_server_groups'](key, add_server_groups,
                                                                org_admin_user=org_admin_user,
                                                                org_admin_password=org_admin_password)

        remove_server_groups = [t for t in current_server_groups if t not in new_server_groups]
        if remove_server_groups:
            __salt__['uyuni.activation_key_remove_server_groups'](key, remove_server_groups,
                                                                   org_admin_user=org_admin_user,
                                                                   org_admin_password=org_admin_password)



    @staticmethod
    def _format_packages_data(packages):
        return [{'name': f[0], **(({'arch': f[1]}) if f[1] else {})} for f in packages]

    @staticmethod
    def _update_packages(current_packages, new_packages, key, org_admin_user, org_admin_password):

        new_packages_normalized = UyuniActivationKeys._normalize_list_packages(new_packages)
        current_packages_normalized = UyuniActivationKeys._normalize_list_packages(current_packages)
        add_packages = [t for t in new_packages_normalized if t not in current_packages_normalized]
        if add_packages:
            pass
            __salt__['uyuni.activation_key_add_packages'](key,
                                                          UyuniActivationKeys._format_packages_data(add_packages),
                                                          org_admin_user=org_admin_user,
                                                          org_admin_password=org_admin_password)

        remove_packages = [t for t in current_packages_normalized if t not in new_packages_normalized]
        if remove_packages:
            pass
            __salt__['uyuni.activation_key_remove_packages'](key,
                                                             UyuniActivationKeys._format_packages_data(remove_packages),
                                                             org_admin_user=org_admin_user,
                                                             org_admin_password=org_admin_password)

    def manage(self, name: str, description: str,
               base_channel: str = '',
               usage_limit: int = 0,
               contact_method: str = 'default',
               system_types: List[str] = [],
               universal_default: bool = False,
               child_channels: List[str] = [],
               configuration_channels: List[str] = [],
               packages: List[str] = [],
               server_groups: List[str] = [],
               configure_after_registration: bool = False,
               org_admin_user: str = None, org_admin_password: str = None) -> Dict[str, Any]:
        """
        Ensure an Uyuni Activation Key is present.

        :param name: the Activation Key name
        :param description: the Activation description
        :param base_channel: base channel to be used
        :param usage_limit: activation key usage limit
        :param contact_method: contact method to be used. Can be one of: 'default', 'ssh-push' or 'ssh-push-tunnel'
        :param system_types: system types to be assigned.
                             Can be one of: 'virtualization_host', 'container_build_host',
                             'monitoring_entitled', 'osimage_build_host', 'virtualization_host'
        :param universal_default: sets this activation key as organization universal default
        :param child_channels: list of child channels to be assigned
        :param configuration_channels: list of configuration channels to be assigned
        :param packages: list of packages which will be installed
        :param server_groups: list of server groups to assign the activation key with
        :param configure_after_registration: deploy configuration files to systems on registration
        :param org_admin_user: organization administrator username
        :param org_admin_password: organization administrator password

        :return:  dict for Salt communication
        """
        current_ak = {}
        key = None
        current_configure_after_registration = None
        system_groups_keys = {}
        current_config_channels = []
        output_field_names = {
            'description': 'description',
            'base_channel_label': 'base_channel',
            'usage_limit': 'usage_limit',
            'universal_default': 'universal_default',
            'contact_method': 'contact_method',
            'entitlements': 'system_types',
            'child_channel_labels': 'child_channels',
            'server_group_ids': 'server_groups',
            'packages': 'packages'
        }
        try:
            all_groups = __salt__['uyuni.systemgroup_list_all_groups'](org_admin_user, org_admin_password)
            group_id_to_name = {}
            for g in (all_groups or []):
                system_groups_keys[g.get('name')] = g.get('id')
                group_id_to_name[g.get('id')] = g.get('name')

            current_org_user = __salt__['uyuni.user_get_details'](org_admin_user, org_admin_password)

            key = "{}-{}".format(current_org_user['org_id'], name)
            returned_ak = __salt__['uyuni.activation_key_get_details'](key, org_admin_user=org_admin_user,
                                                                       org_admin_password=org_admin_password)

            for returned_name, output_name in output_field_names.items():
                current_ak[output_name] = returned_ak[returned_name]

            current_ak['server_groups'] = [group_id_to_name[s] for s in (current_ak['server_groups'] or [])]

            if current_ak.get('base_channel', None) == 'none':
                current_ak['base_channel'] = ''

            current_configure_after_registration = __salt__['uyuni.activation_key_check_config_deployment'](key,
                                                                                                            org_admin_user,
                                                                                                            org_admin_password)

            config_channels_output = __salt__['uyuni.activation_key_list_config_channels'](key,
                                                                                            org_admin_user,
                                                                                            org_admin_password)
            current_config_channels = [cc['label'] for cc in (config_channels_output or [])]

        except Exception as exc:
            if exc.faultCode != ACTIVATION_KEY_NOT_FOUND_ERROR:
                return StateResult.state_error(key, "Error retrieving information about Activation Key '{}': {}".format(key, exc))

        ak_paramters = {'description': description,
                        'base_channel': base_channel,
                        'usage_limit': usage_limit,
                        'contact_method': contact_method,
                        'system_types': system_types,
                        'universal_default': universal_default,
                        'child_channels': child_channels,
                        'server_groups': server_groups,
                        'packages': packages}

        changes = self._compute_changes(ak_paramters, current_ak,
                                        configure_after_registration,
                                        current_configure_after_registration,
                                        current_config_channels,
                                        configuration_channels)

        if not current_ak:
            changes["key"] = {"new": key}

        if not changes:
            return StateResult.prepare_result(key, True, "{0} is already in the desired state".format(key))
        if __opts__['test']:
            return StateResult.prepare_result(key, None, "{0} would be updated".format(key), changes)

        try:
            if current_ak:
                __salt__['uyuni.activation_key_set_details'](key,
                                                             description=description,
                                                             contact_method=contact_method,
                                                             base_channel_label=base_channel,
                                                             usage_limit=usage_limit,
                                                             universal_default=universal_default,
                                                             org_admin_user=org_admin_user,
                                                             org_admin_password=org_admin_password)

                if changes.get('system_types', False):
                    self._update_system_type(current_ak.get('system_types', []), system_types or [],
                                             key, org_admin_user, org_admin_password)

            else:
                __salt__['uyuni.activation_key_create'](key=name,
                                                        description=description,
                                                        base_channel_label=base_channel,
                                                        usage_limit=usage_limit,
                                                        system_types=system_types,
                                                        universal_default=universal_default,
                                                        org_admin_user=org_admin_user,
                                                        org_admin_password=org_admin_password)

                __salt__['uyuni.activation_key_set_details'](key, contact_method=contact_method,
                                                             usage_limit=usage_limit,
                                                             org_admin_user=org_admin_user,
                                                             org_admin_password=org_admin_password)

            if changes.get('child_channels', False):
                self._update_child_channels(current_ak.get('child_channels', []),
                                            child_channels or [],
                                            key, org_admin_user, org_admin_password)

            if changes.get('server_groups', False):
                old_server_groups_id = [system_groups_keys[s] for s in current_ak.get('server_groups', [])]
                new_server_groups_id = [system_groups_keys[s] for s in (server_groups or [])]
                self._update_server_groups(old_server_groups_id,
                                           new_server_groups_id,
                                           key, org_admin_user, org_admin_password)

            if changes.get('configure_after_registration', False):
                if configure_after_registration:
                    __salt__['uyuni.activation_key_enable_config_deployment'](key,
                                                                              org_admin_user=org_admin_user,
                                                                              org_admin_password=org_admin_password)
                else:
                    if current_ak:
                        __salt__['uyuni.activation_key_disable_config_deployment'](key,
                                                                                   org_admin_user=org_admin_user,
                                                                                   org_admin_password=org_admin_password)

            if changes.get('packages', False):
                self._update_packages(current_ak.get('packages', []),
                                           packages or [],
                                            key, org_admin_user, org_admin_password)

            if changes.get('configuration_channels', False):
                __salt__['uyuni.activation_key_set_config_channels']([key],
                                                                     config_channel_label=configuration_channels,
                                                                     org_admin_user=org_admin_user,
                                                                     org_admin_password=org_admin_password)

        except Exception as exc:
            return StateResult.state_error(key, "Error updating activation key '{}': {}".format(key, exc))
        else:
            return StateResult.prepare_result(key, True, "{0} activation key successfully modified".format(key), changes)

    def delete(self, name: str, org_admin_user: str = None, org_admin_password: str = None) -> Dict[str, Any]:
        """
        Remove an Uyuni Activation Key.

        :param name: the Activation Key Name
        :param org_admin_user: organization administrator username
        :param org_admin_password: organization administrator password

        :return: dict for Salt communication
        """
        try:
            current_org_user = __salt__['uyuni.user_get_details'](org_admin_user, org_admin_password)
            key = "{}-{}".format(current_org_user['org_id'], name)
            ak = __salt__['uyuni.activation_key_get_details'](key, org_admin_user=org_admin_user,
                                                                       org_admin_password=org_admin_password)
        except Exception as exc:
            if exc.faultCode == ACTIVATION_KEY_NOT_FOUND_ERROR:
                return StateResult.prepare_result(name, True, "{0} is already absent".format(key))
            if exc.faultCode == AUTHENTICATION_ERROR:
                return StateResult.state_error(name,
                                               "Error deleting Activation Key (organization credentials error) '{}': {}"
                                               .format(key, exc))
            raise exc
        else:
            changes = {
                'id': {'old': key},
            }
            if __opts__['test']:
                return StateResult.prepare_result(name, None, "{0} would be deleted".format(key), changes)

            try:
                __salt__['uyuni.activation_key_delete'](key,
                                                        org_admin_user=org_admin_user,
                                                        org_admin_password=org_admin_password)
                return StateResult.prepare_result(name, True, "Activation Key {} has been deleted".format(key), changes)
            except Exception as exc:
                return StateResult.state_error(name, "Error deleting Activation Key '{}': {}".format(key, exc))


def __virtual__():
    return __virtualname__


def user_present(name, password, email, first_name, last_name, use_pam_auth=False,
                 roles=None, system_groups=None,
                 org_admin_user=None, org_admin_password=None):
    """
    Create or update an Uyuni user

    :param name: user login name
    :param password: desired password for the user
    :param email: valid email address
    :param first_name: First name
    :param last_name: Last name
    :param use_pam_auth: if you wish to use PAM authentication for this user
    :param roles: roles to assign to user
    :param system_groups: system_groups to assign to user
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password

    :return: dict for Salt communication
    """
    return UyuniUsers().manage(name, password, email, first_name, last_name, use_pam_auth,
                               roles, system_groups,
                               org_admin_user, org_admin_password)


def user_channels(name, password,
                  manageable_channels=[], subscribable_channels=[],
                  org_admin_user=None, org_admin_password=None):
    """
    Ensure a user has access to the specified channels

    :param name: user login name
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
    Ensure an Uyuni user is not present.

    :param name: user login name
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password

    :return:  dict for Salt communication
    """
    return UyuniUsers().delete(name, org_admin_user, org_admin_password)


def org_present(name, org_admin_user, org_admin_password,
                first_name, last_name, email, pam=False,
                admin_user=None, admin_password=None):
    """
    Create or update an Uyuni organization
    Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

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
    Ensure an Uyuni organization is not present
    Note: the configured admin user must have the SUSE Manager/Uyuni Administrator role to perform this action

    :param name: organization name
    :param admin_user: uyuni admin user
    :param admin_password: uyuni admin password

    :return: dict for Salt communication
    """
    return UyuniOrgs().delete(name, admin_user, admin_password)


def org_trust(name, org_name, trusts, admin_user=None, admin_password=None):
    """
    Establish trust relationships between Uyuni organizations.

    :param name: state name
    :param org_name: Organization name
    :param trusts: list of organization names to trust
    :param admin_user: administrator username
    :param admin_password: administrator password

    :return: dict for Salt communication
    """
    return UyuniOrgsTrust().trust(name, org_name, trusts, admin_user, admin_password)


def group_present(name, description, target=None, target_type="glob",
                  org_admin_user=None, org_admin_password=None):
    """
    Create or update an Uyuni system group

    :param name: group name
    :param description: group description
    :param target: target expression used to filter which minions should be part of the group
    :param target_type: target type, one of the following: glob, grain, grain_pcre, pillar, pillar_pcre,
            pillar_exact, compound, compound_pillar_exact. Default: glob.
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password

    :return: dict for Salt communication
    """
    return UyuniGroups().manage(name, description, target, target_type,
                                org_admin_user, org_admin_password)


def group_absent(name, org_admin_user=None, org_admin_password=None):
    """
    Ensure an Uyuni system group is not present

    :param name: Group Name
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password

    :return: dict for Salt communication
    """
    return UyuniGroups().delete(name, org_admin_user, org_admin_password)


def activation_key_absent(name, org_admin_user=None, org_admin_password=None):
    """
    Ensure an Uyuni Activation Key is not present.

    :param name: the Activation Key name
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password

    :return:  dict for Salt communication
    """
    return UyuniActivationKeys().delete(name, org_admin_user, org_admin_password)


def activation_key_present(name,
                           description,
                           base_channel='',
                           usage_limit=0,
                           contact_method='default',
                           system_types=[],
                           universal_default=False,
                           child_channels=[],
                           configuration_channels=[],
                           packages=[],
                           server_groups=[],
                           configure_after_registration=False,
                           org_admin_user=None, org_admin_password=None):
    """
    Ensure an Uyuni Activation Key is present.

    :param name: the Activation Key name
    :param description: the Activation description
    :param base_channel: base channel to be used
    :param usage_limit: activation key usage limit. Default value is 0, which means unlimited usage
    :param contact_method: contact method to be used. Can be one of: 'default', 'ssh-push' or 'ssh-push-tunnel'
    :param system_types: system types to be assigned.
                         Can be one of: 'virtualization_host', 'container_build_host',
                         'monitoring_entitled', 'osimage_build_host', 'virtualization_host'
    :param universal_default: sets this activation key as organization universal default
    :param child_channels: list of child channels to be assigned
    :param configuration_channels: list of configuration channels to be assigned
    :param packages: list of packages which will be installed
    :param server_groups: list of server groups to assign the activation key with
    :param configure_after_registration: deploy configuration files to systems on registration
    :param org_admin_user: organization administrator username
    :param org_admin_password: organization administrator password

    :return:  dict for Salt communication
    """
    return UyuniActivationKeys().manage(name, description,
                                        base_channel=base_channel,
                                        usage_limit=usage_limit,
                                        contact_method=contact_method,
                                        system_types=system_types,
                                        universal_default=universal_default,
                                        child_channels=child_channels,
                                        configuration_channels=configuration_channels,
                                        packages=packages,
                                        server_groups=server_groups,
                                        configure_after_registration=configure_after_registration,
                                        org_admin_user=org_admin_user,
                                        org_admin_password=org_admin_password)
