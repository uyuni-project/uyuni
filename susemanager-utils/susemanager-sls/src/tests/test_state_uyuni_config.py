import pytest
from mock import MagicMock, patch, call
from . import mockery
import pdb

mockery.setup_environment()

import sys

from ..states import uyuni_config

# Mock globals
uyuni_config.log = MagicMock()
uyuni_config.__salt__ = {}
uyuni_config.__opts__ = {'test': False}


class TestManageUser:

    def test_user_present_new_user_test(self):
        exc = Exception("user not found")
        exc.faultCode = 2951

        with patch.dict(uyuni_config.__salt__, {'uyuni.user_get_details': MagicMock(side_effect=exc)}):
            with patch.dict(uyuni_config.__opts__, {'test': True}):
                result = uyuni_config.user_present('username', 'password', 'mail@mail.com',
                                                  'first_name', 'last_name', False,
                                                  ['role'], ['group'],
                                                  'org_admin_user', 'org_admin_password')
                assert result is not None
                assert result['name'] == 'username'
                assert result['result'] is None
                assert result['comment'] == 'username would be modified'

                assert result['changes'] == {
                    'login': {'new': 'username'},
                    'password': {'new': '(hidden)'},
                    'email': {'new': 'mail@mail.com'},
                    'first_name': {'new': 'first_name'},
                    'last_name': {'new': 'last_name'},
                    'roles': {'new': ['role']},
                    'system_groups': {'new': ['group']}}

                uyuni_config.__salt__['uyuni.user_get_details'].assert_called_once_with('username',
                                                                                        org_admin_user='org_admin_user',
                                                                                        org_admin_password='org_admin_password')

    def test_user_present_new_user_minimal(self):
        exc = Exception("user not found")
        exc.faultCode = 2951

        with patch.dict(uyuni_config.__salt__, {
            'uyuni.user_get_details': MagicMock(side_effect=exc),
            'uyuni.user_create': MagicMock(return_value=True)}):
            result = uyuni_config.user_present('username', 'password', 'mail@mail.com',
                                              'first_name', 'last_name')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result'] is True
            assert result['comment'] == 'username user successfully modified'

            assert result['changes'] == {
                'login': {'new': 'username'},
                'password': {'new': '(hidden)'},
                'email': {'new': 'mail@mail.com'},
                'first_name': {'new': 'first_name'},
                'last_name': {'new': 'last_name'}}

            ## verify mock calls
            uyuni_config.__salt__['uyuni.user_get_details'].assert_called_once_with('username',
                                                                                    org_admin_user=None,
                                                                                    org_admin_password=None)

            uyuni_config.__salt__['uyuni.user_create'].assert_called_once_with(email='mail@mail.com',
                                                                               first_name='first_name',
                                                                               last_name='last_name',
                                                                               use_pam_auth=False,
                                                                               org_admin_password=None,
                                                                               org_admin_user=None,
                                                                               password='password',
                                                                               login='username')

    def test_user_present_new_user_complete(self):
        exc = Exception("user not found")
        exc.faultCode = 2951

        with patch.dict(uyuni_config.__salt__, {
            'uyuni.user_get_details': MagicMock(side_effect=exc),
            'uyuni.user_create': MagicMock(return_value=True),
            'uyuni.user_add_role': MagicMock(return_value=True),
            'uyuni.user_add_assigned_system_groups': MagicMock(return_value=1)}):
            result = uyuni_config.user_present('username', 'password', 'mail@mail.com',
                                              'first_name', 'last_name', False,
                                               ['role'], ['group'],
                                              'org_admin_user', 'org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result'] is True
            assert result['comment'] == 'username user successfully modified'

            assert result['changes'] == {
                'login': {'new': 'username'},
                'password': {'new': '(hidden)'},
                'email': {'new': 'mail@mail.com'},
                'first_name': {'new': 'first_name'},
                'last_name': {'new': 'last_name'},
                'roles': {'new': ['role']},
                'system_groups': {'new': ['group']}}

            ## verify mock calls
            uyuni_config.__salt__['uyuni.user_get_details'].assert_called_once_with('username',
                                                                                    org_admin_user='org_admin_user',
                                                                                    org_admin_password='org_admin_password')

            uyuni_config.__salt__['uyuni.user_create'].assert_called_once_with(email='mail@mail.com',
                                                                               first_name='first_name',
                                                                               last_name='last_name',
                                                                               use_pam_auth=False,
                                                                               org_admin_password='org_admin_password',
                                                                               org_admin_user='org_admin_user',
                                                                               password='password',
                                                                               login='username')

            uyuni_config.__salt__['uyuni.user_add_role'].assert_called_once_with('username', role='role',
                                                                                 org_admin_user='org_admin_user',
                                                                                 org_admin_password='org_admin_password')

            uyuni_config.__salt__['uyuni.user_add_assigned_system_groups'].assert_called_once_with(login='username',
                                                                                                   server_group_names=[
                                                                                                      'group'],
                                                                                                   org_admin_user='org_admin_user',
                                                                                                   org_admin_password='org_admin_password')

    def test_user_present_update_user(self):
        exc = Exception("user not found")
        exc.faultCode = 2950

        current_user = {'uui': 'username',
                        'email': 'mail@mail.com',
                        'first_name': 'first',
                        'last_name': 'last'}

        with patch.dict(uyuni_config.__salt__, {
            'uyuni.user_get_details': MagicMock(side_effect=[current_user, exc]),
            'uyuni.user_list_roles': MagicMock(return_value=['role1', 'role2']),
            'uyuni.user_list_assigned_system_groups': MagicMock(return_value=[{'name': 'group1'}, {'name': 'group2'}]),
            'uyuni.user_set_details': MagicMock(return_value=True),
            'uyuni.user_remove_role': MagicMock(return_value=True),
            'uyuni.user_add_role': MagicMock(return_value=True),
            'uyuni.user_remove_assigned_system_groups': MagicMock(return_value=1),
            'uyuni.user_add_assigned_system_groups': MagicMock(return_value=1)}):
            result = uyuni_config.user_present('username', 'new_password', 'new_mail@mail.com',
                                              'new_first', 'new_last', False,
                                               ['role1', 'role3'], ['group2', 'group3'],
                                              'org_admin_user', 'org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result'] is True
            assert result['comment'] == 'username user successfully modified'
            assert result['changes'] == {
                'password': {'new': '(hidden)', 'old': '(hidden)'},
                'email': {'new': 'new_mail@mail.com', 'old': 'mail@mail.com'},
                'first_name': {'new': 'new_first', 'old': 'first'},
                'last_name': {'new': 'new_last', 'old': 'last'},
                'roles': {'new': ['role1', 'role3'], 'old': ['role1', 'role2']},
                'system_groups': {'new': ['group2', 'group3'], 'old': ['group1', 'group2']}}

            ## verify mock calls
            uyuni_config.__salt__['uyuni.user_get_details'].assert_has_calls([call('username',
                                                                                   org_admin_user='org_admin_user',
                                                                                   org_admin_password='org_admin_password'),
                                                                              call('username', 'new_password')])

            uyuni_config.__salt__['uyuni.user_list_roles'].assert_called_once_with('username',
                                                                                   org_admin_user='org_admin_user',
                                                                                   org_admin_password='org_admin_password')

            uyuni_config.__salt__['uyuni.user_list_assigned_system_groups'].assert_called_once_with('username',
                                                                                                    org_admin_user='org_admin_user',
                                                                                                    org_admin_password='org_admin_password')

            uyuni_config.__salt__['uyuni.user_set_details'].assert_called_once_with(email='new_mail@mail.com',
                                                                                    first_name='new_first',
                                                                                    last_name='new_last',
                                                                                    org_admin_password='org_admin_password',
                                                                                    org_admin_user='org_admin_user',
                                                                                    password='new_password',
                                                                                    login='username')

            uyuni_config.__salt__['uyuni.user_remove_role'].assert_called_once_with('username', role='role2',
                                                                                    org_admin_user='org_admin_user',
                                                                                    org_admin_password='org_admin_password')
            uyuni_config.__salt__['uyuni.user_add_role'].assert_called_once_with('username', role='role3',
                                                                                 org_admin_user='org_admin_user',
                                                                                 org_admin_password='org_admin_password')

            uyuni_config.__salt__['uyuni.user_remove_assigned_system_groups'].assert_called_once_with(login='username',
                                                                                                      server_group_names=[
                                                                                                         'group1'],
                                                                                                      org_admin_user='org_admin_user',
                                                                                                      org_admin_password='org_admin_password')
            uyuni_config.__salt__['uyuni.user_add_assigned_system_groups'].assert_called_once_with(login='username',
                                                                                                   server_group_names=[
                                                                                                      'group3'],
                                                                                                   org_admin_user='org_admin_user',
                                                                                                   org_admin_password='org_admin_password')

    def test_user_absent_auth_error(self):
        exc = Exception("Auth error")
        exc.faultCode = 2950

        with patch.dict(uyuni_config.__salt__, {'uyuni.user_get_details': MagicMock(side_effect=exc)}):
            result = uyuni_config.user_absent('username',
                                             'org_admin_user', 'org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result'] is False
            assert result['comment'] == "Error deleting user (organization credentials error) 'username': Auth error"
            assert result['changes'] == {}

    def test_user_absent_user_not_exits(self):
        exc = Exception("User not found")
        exc.faultCode = -213

        with patch.dict(uyuni_config.__salt__, {'uyuni.user_get_details': MagicMock(side_effect=exc)}):
            result = uyuni_config.user_absent('username',
                                             'org_admin_user', 'org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result'] is True
            assert result['comment'] == "username is already absent"
            assert result['changes'] == {}

    def test_user_absent_generic_error(self):
        exc = Exception("generic error")
        exc.faultCode = 2951

        with patch.dict(uyuni_config.__salt__, {'uyuni.user_get_details': MagicMock(side_effect=exc)}):
            with pytest.raises(Exception) as e:
                uyuni_config.user_absent('username',
                                        'org_admin_user', 'org_admin_password')
            assert e.value.faultCode == 2951
            assert e.value.args[0] == 'generic error'

    def test_user_absent_exists_test(self):
        current_user = {'uui': 'username',
                        'email': 'mail@mail.com',
                        'first_name': 'first',
                        'last_name': 'last'}

        with patch.dict(uyuni_config.__salt__, {'uyuni.user_get_details': MagicMock(return_value=current_user)}):
            with patch.dict(uyuni_config.__opts__, {'test': True}):
                result = uyuni_config.user_absent('username',
                                                 'org_admin_user', 'org_admin_password')

                assert result is not None
                assert result['name'] == 'username'
                assert result['result'] is None
                assert result['comment'] == 'username would be deleted'

                assert result['changes'] == {
                    'login': {'old': 'username'},
                    'email': {'old': 'mail@mail.com'},
                    'first_name': {'old': 'first'},
                    'last_name': {'old': 'last'}}

                uyuni_config.__salt__['uyuni.user_get_details'].assert_called_once_with('username',
                                                                                        org_admin_user='org_admin_user',
                                                                                        org_admin_password='org_admin_password')

    def test_user_absent_exist_user(self):
        current_user = {'uui': 'username',
                        'email': 'mail@mail.com',
                        'first_name': 'first',
                        'last_name': 'last'}

        with patch.dict(uyuni_config.__salt__, {
            'uyuni.user_get_details': MagicMock(return_value=current_user),
            'uyuni.user_delete': MagicMock(return_value=True)}):
            result = uyuni_config.user_absent('username',
                                             'org_admin_user', 'org_admin_password')

            assert result is not None
            assert result['name'] == 'username'
            assert result['result'] is True
            assert result['comment'] == 'User username has been deleted'

            assert result['changes'] == {
                'login': {'old': 'username'},
                'email': {'old': 'mail@mail.com'},
                'first_name': {'old': 'first'},
                'last_name': {'old': 'last'}}

            uyuni_config.__salt__['uyuni.user_get_details'].assert_called_once_with('username',
                                                                                    org_admin_user='org_admin_user',
                                                                                    org_admin_password='org_admin_password')
            uyuni_config.__salt__['uyuni.user_delete'].assert_called_once_with('username',
                                                                               org_admin_user='org_admin_user',
                                                                               org_admin_password='org_admin_password')


class TestManageUserChannels:

    def test_user_channels_org_admin(self):
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.user_list_roles': MagicMock(return_value=["channel_admin"]),
            'uyuni.channel_list_manageable_channels': MagicMock(),
            'uyuni.channel_list_my_channels': MagicMock()}):
            result = uyuni_config.user_channels('username', 'password',
                                                org_admin_user='org_admin_user',
                                                org_admin_password='org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert not result['result']
            assert result['changes'] == {}
            assert 'org_admin' in result['comment']

            uyuni_config.__salt__['uyuni.user_list_roles'].assert_called_once_with('username', password='password')

            uyuni_config.__salt__['uyuni.channel_list_manageable_channels'].assert_called_once_with('username',
                                                                                                   'password')

            uyuni_config.__salt__['uyuni.channel_list_my_channels'].assert_called_once_with('username',
                                                                                           'password')

    def test_user_channels_channel_admin(self):
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.user_list_roles': MagicMock(return_value=["channel_admin"]),
            'uyuni.channel_list_manageable_channels': MagicMock(),
            'uyuni.channel_list_my_channels': MagicMock()}):
            result = uyuni_config.user_channels('username', 'password',
                                                org_admin_user='org_admin_user',
                                                org_admin_password='org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert not result['result']
            assert result['changes'] == {}
            assert 'channel_admin' in result['comment']

            uyuni_config.__salt__['uyuni.user_list_roles'].assert_called_once_with('username', password='password')

            uyuni_config.__salt__['uyuni.channel_list_manageable_channels'].assert_called_once_with('username',
                                                                                                   'password')

            uyuni_config.__salt__['uyuni.channel_list_my_channels'].assert_called_once_with('username',
                                                                                           'password')

    def test_user_channels_add_all(self):
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.user_list_roles': MagicMock(return_value=[]),
            'uyuni.channel_list_manageable_channels': MagicMock(return_value=[]),
            'uyuni.channel_list_my_channels': MagicMock(return_value=[]),
            'uyuni.channel_software_set_user_manageable': MagicMock(),
            'uyuni.channel_software_set_user_subscribable': MagicMock()}):
            result = uyuni_config.user_channels('username', 'password',
                                                manageable_channels=['manage1'],
                                                subscribable_channels=['subscribe1'],
                                                org_admin_user='org_admin_user',
                                                org_admin_password='org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result']
            assert result['changes'] == {'manageable_channels': {'manage1': True},
                                         'subscribable_channels': {'subscribe1': True}}

            uyuni_config.__salt__['uyuni.user_list_roles'].assert_called_once_with('username', password='password')

            uyuni_config.__salt__['uyuni.channel_list_manageable_channels'].assert_called_once_with('username',
                                                                                                   'password')

            uyuni_config.__salt__['uyuni.channel_list_my_channels'].assert_called_once_with('username',
                                                                                           'password')

            uyuni_config.__salt__['uyuni.channel_software_set_user_manageable'].assert_called_once_with('manage1',
                                                                                                       'username',
                                                                                                        True,
                                                                                                       'org_admin_user',
                                                                                                       'org_admin_password')

            uyuni_config.__salt__['uyuni.channel_software_set_user_subscribable'].assert_called_once_with('subscribe1',
                                                                                                         'username',
                                                                                                          True,
                                                                                                         'org_admin_user',
                                                                                                         'org_admin_password')

    def test_user_channels_no_changes(self):
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.user_list_roles': MagicMock(return_value=[]),
            'uyuni.channel_list_manageable_channels': MagicMock(return_value=[{"label": "manage1"}]),
            'uyuni.channel_list_my_channels': MagicMock(return_value=[{"label": "subscribe1"}]),
            'uyuni.channel_software_set_user_manageable': MagicMock(),
            'uyuni.channel_software_set_user_subscribable': MagicMock()}):
            result = uyuni_config.user_channels('username', 'password',
                                                manageable_channels=['manage1'],
                                                subscribable_channels=['subscribe1'],
                                                org_admin_user='org_admin_user',
                                                org_admin_password='org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result']
            assert result['changes'] == {}

            uyuni_config.__salt__['uyuni.user_list_roles'].assert_called_once_with('username', password='password')

            uyuni_config.__salt__['uyuni.channel_list_manageable_channels'].assert_called_once_with('username',
                                                                                                   'password')

            uyuni_config.__salt__['uyuni.channel_list_my_channels'].assert_called_once_with('username',
                                                                                           'password')

    def test_user_channels_managed_subscribe_change(self):
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.user_list_roles': MagicMock(return_value=[]),
            'uyuni.channel_list_manageable_channels': MagicMock(return_value=[{"label": "manage1"}]),
            'uyuni.channel_list_my_channels': MagicMock(return_value=[{"label": "manage1"}]),
            'uyuni.channel_software_set_user_manageable': MagicMock(),
            'uyuni.channel_software_set_user_subscribable': MagicMock()}):
            result = uyuni_config.user_channels('username', 'password',
                                                manageable_channels=[],
                                                subscribable_channels=['manage1'],
                                                org_admin_user='org_admin_user',
                                                org_admin_password='org_admin_password')
            print(result)
            assert result is not None
            assert result['name'] == 'username'
            assert result['result']
            assert result['changes'] == {'manageable_channels': {'manage1': False},
                                         'subscribable_channels': {'manage1': True}}

            uyuni_config.__salt__['uyuni.user_list_roles'].assert_called_once_with('username', password='password')

            uyuni_config.__salt__['uyuni.channel_list_manageable_channels'].assert_called_once_with('username',
                                                                                                   'password')

            uyuni_config.__salt__['uyuni.channel_list_my_channels'].assert_called_once_with('username',
                                                                                           'password')

            uyuni_config.__salt__['uyuni.channel_software_set_user_manageable'].assert_called_once_with('manage1',
                                                                                                       'username',
                                                                                                        False,
                                                                                                       'org_admin_user',
                                                                                                       'org_admin_password')

            uyuni_config.__salt__['uyuni.channel_software_set_user_subscribable'].assert_called_once_with('manage1',
                                                                                                         'username',
                                                                                                          True,
                                                                                                         'org_admin_user',
                                                                                                         'org_admin_password')


class TestManageGroups:

    def test_group_present_new_group_test_no_systems(self):
        exc = Exception("Group not found")
        exc.faultCode = 2201

        with patch.dict(uyuni_config.__salt__, {
            'uyuni.systemgroup_get_details': MagicMock(side_effect=exc),
            'uyuni.master_select_minions': MagicMock(),
            'uyuni.systems_get_minion_id_map': MagicMock()}):
            with patch.dict(uyuni_config.__opts__, {'test': True}):
                result = uyuni_config.group_present('my_group', 'my group description',
                                                    target='*http*',
                                                    org_admin_user='org_admin_user',
                                                    org_admin_password='org_admin_password')
                assert result is not None
                assert result['name'] == 'my_group'
                assert result['result'] is None
                assert result['comment'] == 'my_group would be updated'

                assert result['changes'] == {'description': {'new': 'my group description'},
                                             'name': {'new': 'my_group'}}

                uyuni_config.__salt__['uyuni.systemgroup_get_details'].assert_called_once_with('my_group',
                                                                                               org_admin_user='org_admin_user',
                                                                                               org_admin_password='org_admin_password')

                uyuni_config.__salt__['uyuni.master_select_minions'].assert_called_once_with('*http*', 'glob')
                uyuni_config.__salt__['uyuni.systems_get_minion_id_map'].assert_called_once_with('org_admin_user',
                                                                                                'org_admin_password')

    def test_group_present_new_group_test(self):
        exc = Exception("Group not found")
        exc.faultCode = 2201

        with patch.dict(uyuni_config.__salt__, {
            'uyuni.systemgroup_get_details': MagicMock(side_effect=exc),
            'uyuni.master_select_minions': MagicMock(return_value={'minions': ['my_minion_1', 'my_minion_2']}),
            'uyuni.systems_get_minion_id_map': MagicMock(return_value={'my_minion_1': '10001'})}):
            with patch.dict(uyuni_config.__opts__, {'test': True}):
                result = uyuni_config.group_present('my_group', 'my group description',
                                                    target='*http*',
                                                    org_admin_user='org_admin_user',
                                                    org_admin_password='org_admin_password')
                assert result is not None
                assert result['name'] == 'my_group'
                assert result['result'] is None
                assert result['comment'] == 'my_group would be updated'

                assert result['changes'] == {'description': {'new': 'my group description'},
                                             'systems': {'new': ['10001']},
                                             'name': {'new': 'my_group'}}

                uyuni_config.__salt__['uyuni.systemgroup_get_details'].assert_called_once_with('my_group',
                                                                                               org_admin_user='org_admin_user',
                                                                                               org_admin_password='org_admin_password')

                uyuni_config.__salt__['uyuni.master_select_minions'].assert_called_once_with('*http*', 'glob')
                uyuni_config.__salt__['uyuni.systems_get_minion_id_map'].assert_called_once_with('org_admin_user',
                                                                                                'org_admin_password')

    def test_group_present_new_group(self):
        exc = Exception("Group not found")
        exc.faultCode = 2201

        with patch.dict(uyuni_config.__salt__, {
            'uyuni.systemgroup_get_details': MagicMock(side_effect=exc),
            'uyuni.master_select_minions': MagicMock(return_value={'minions': ['my_minion_1', 'my_minion_2']}),
            'uyuni.systems_get_minion_id_map': MagicMock(return_value={'my_minion_1': '10001'}),
            'uyuni.systemgroup_create': MagicMock(),
            'uyuni.systemgroup_add_remove_systems': MagicMock()}):
            result = uyuni_config.group_present('my_group', 'my group description',
                                                target='*http*',
                                                org_admin_user='org_admin_user',
                                                org_admin_password='org_admin_password')
            assert result is not None
            assert result['name'] == 'my_group'
            assert result['result'] is True
            assert result['comment'] == 'my_group successfully updated'

            assert result['changes'] == {'description': {'new': 'my group description'},
                                         'systems': {'new': ['10001']},
                                         'name': {'new': 'my_group'}}

            uyuni_config.__salt__['uyuni.systemgroup_get_details'].assert_called_once_with('my_group',
                                                                                           org_admin_user='org_admin_user',
                                                                                           org_admin_password='org_admin_password')

            uyuni_config.__salt__['uyuni.master_select_minions'].assert_called_once_with('*http*', 'glob')
            uyuni_config.__salt__['uyuni.systems_get_minion_id_map'].assert_called_once_with('org_admin_user',
                                                                                            'org_admin_password')

            uyuni_config.__salt__['uyuni.systemgroup_create'].assert_called_once_with('my_group', 'my group description',
                                                                                      org_admin_user='org_admin_user',
                                                                                      org_admin_password='org_admin_password')

            uyuni_config.__salt__['uyuni.systemgroup_add_remove_systems'].assert_called_once_with('my_group', True,
                                                                                                  ['10001'],
                                                                                                  org_admin_user='org_admin_user',
                                                                                                  org_admin_password='org_admin_password')

    def test_group_present_update_group(self):
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.systemgroup_get_details': MagicMock(
                return_value={'description': 'old description', 'name': 'my_group'}),
            'uyuni.systemgroup_list_systems': MagicMock(return_value=[{'id': '10001'}, {'id': '10003'}]),
            'uyuni.master_select_minions': MagicMock(
                return_value={'minions': ['my_minion_1', 'my_minion_2', 'my_minion_4']}),
            'uyuni.systems_get_minion_id_map': MagicMock(return_value={'my_minion_1': '10001', 'my_minion_2': '10002'}),
            'uyuni.systemgroup_update': MagicMock(),
            'uyuni.systemgroup_add_remove_systems': MagicMock()}):
            result = uyuni_config.group_present('my_group', 'my group description',
                                                target='*http*',
                                                org_admin_user='org_admin_user',
                                                org_admin_password='org_admin_password')
            assert result is not None
            assert result['name'] == 'my_group'
            assert result['result']
            assert result['comment'] == 'my_group successfully updated'

            assert result['changes'] == {'description': {'new': 'my group description',
                                                         'old': 'old description'},
                                         'systems': {'new': ['10001', '10002'],
                                                     'old': ['10001', '10003']}}

            uyuni_config.__salt__['uyuni.systemgroup_get_details'].assert_called_once_with('my_group',
                                                                                           org_admin_user='org_admin_user',
                                                                                           org_admin_password='org_admin_password')

            uyuni_config.__salt__['uyuni.systemgroup_list_systems'].assert_called_once_with('my_group',
                                                                                            org_admin_user='org_admin_user',
                                                                                            org_admin_password='org_admin_password')

            uyuni_config.__salt__['uyuni.master_select_minions'].assert_called_once_with('*http*', 'glob')
            uyuni_config.__salt__['uyuni.systems_get_minion_id_map'].assert_called_once_with('org_admin_user',
                                                                                            'org_admin_password')

            uyuni_config.__salt__['uyuni.systemgroup_update'].assert_called_once_with('my_group', 'my group description',
                                                                                      org_admin_user='org_admin_user',
                                                                                      org_admin_password='org_admin_password')

            uyuni_config.__salt__['uyuni.systemgroup_add_remove_systems'].assert_has_calls([call('my_group', False,
                                                                                                 ['10003'],
                                                                                                 org_admin_user='org_admin_user',
                                                                                                 org_admin_password='org_admin_password'),
                                                                                            call('my_group', True,
                                                                                                ['10002'],
                                                                                                org_admin_user='org_admin_user',
                                                                                                org_admin_password='org_admin_password')])

    def test_group_absent_success_test(self):
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.systemgroup_get_details': MagicMock(
                return_value={'description': 'description', 'name': 'my_group'})}):
            with patch.dict(uyuni_config.__opts__, {'test': True}):
                result = uyuni_config.group_absent('my_group',
                                                   org_admin_user='org_admin_user',
                                                   org_admin_password='org_admin_password')
                assert result is not None
                assert result['name'] == 'my_group'
                assert result['result'] is None
                assert result['comment'] == 'my_group would be removed'

                assert result['changes'] == {}
                uyuni_config.__salt__['uyuni.systemgroup_get_details'].assert_called_once_with('my_group',
                                                                                               org_admin_user='org_admin_user',
                                                                                               org_admin_password='org_admin_password')

    def test_group_absent_success(self):
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.systemgroup_get_details': MagicMock(return_value={'description': 'description', 'name': 'my_group'}),
            'uyuni.systemgroup_delete': MagicMock(return_value=True)}):
            result = uyuni_config.group_absent('my_group',
                                               org_admin_user='org_admin_user',
                                               org_admin_password='org_admin_password')
            assert result is not None
            assert result['name'] == 'my_group'
            assert result['result']
            assert result['comment'] == 'Group my_group has been deleted'

            assert result['changes'] == {'description': {'old': 'description'},
                                         'name': {'old': 'my_group'}}
            uyuni_config.__salt__['uyuni.systemgroup_get_details'].assert_called_once_with('my_group',
                                                                                           org_admin_user='org_admin_user',
                                                                                           org_admin_password='org_admin_password')

            uyuni_config.__salt__['uyuni.systemgroup_delete'].assert_called_once_with('my_group',
                                                                                      org_admin_user='org_admin_user',
                                                                                      org_admin_password='org_admin_password')

    def test_group_absent_already_removed(self):
        exc = Exception("Group not found")
        exc.faultCode = 2201

        with patch.dict(uyuni_config.__salt__, {'uyuni.systemgroup_get_details': MagicMock(side_effect=exc)}):
            result = uyuni_config.group_absent('my_group',
                                               org_admin_user='org_admin_user',
                                               org_admin_password='org_admin_password')
            assert result is not None
            assert result['name'] == 'my_group'
            assert result['result']
            assert result['comment'] == 'my_group is already absent'

            assert result['changes'] == {}
            uyuni_config.__salt__['uyuni.systemgroup_get_details'].assert_called_once_with('my_group',
                                                                                           org_admin_user='org_admin_user',
                                                                                           org_admin_password='org_admin_password')


class TestManageOrgs:

    def test_org_present_new_org_test(self):
        exc = Exception("org not found")
        exc.faultCode = 2850

        with patch.dict(uyuni_config.__salt__, {'uyuni.org_get_details': MagicMock(side_effect=exc)}):
            with patch.dict(uyuni_config.__opts__, {'test': True}):
                result = uyuni_config.org_present('my_org', 'org_admin_user', 'org_admin_password',
                                                 'First Name', 'Last Name', 'email@email.com',
                                                  admin_user='admin_user',
                                                  admin_password='admin_password')

                assert result is not None
                assert result['name'] == 'my_org'
                assert result['result'] is None
                assert result['comment'] == 'my_org would be updated'
                assert result['changes'] == {'email': {'new': 'email@email.com'},
                                             'first_name': {'new': 'First Name'},
                                             'last_name': {'new': 'Last Name'},
                                             'org_admin_user': {'new': 'org_admin_user'},
                                             'org_name': {'new': 'my_org'},
                                             'pam': {'new': False}}
                uyuni_config.__salt__['uyuni.org_get_details'].assert_called_once_with('my_org',
                                                                                       admin_user='admin_user',
                                                                                       admin_password='admin_password')

    def test_org_present_new_org(self):
        exc = Exception("org not found")
        exc.faultCode = 2850

        with patch.dict(uyuni_config.__salt__, {
            'uyuni.org_get_details': MagicMock(side_effect=exc),
            'uyuni.org_create': MagicMock()}):
            result = uyuni_config.org_present('my_org', 'org_admin_user', 'org_admin_password',
                                             'First Name', 'Last Name', 'email@email.com',
                                              admin_user='admin_user',
                                              admin_password='admin_password')

            assert result is not None
            assert result['name'] == 'my_org'
            assert result['result']
            assert result['comment'] == 'my_org org successfully modified'
            assert result['changes'] == {'email': {'new': 'email@email.com'},
                                         'first_name': {'new': 'First Name'},
                                         'last_name': {'new': 'Last Name'},
                                         'org_admin_user': {'new': 'org_admin_user'},
                                         'org_name': {'new': 'my_org'},
                                         'pam': {'new': False}}
            uyuni_config.__salt__['uyuni.org_get_details'].assert_called_once_with('my_org',
                                                                                   admin_user='admin_user',
                                                                                   admin_password='admin_password')
            uyuni_config.__salt__['uyuni.org_create'].assert_called_once_with(name='my_org',
                                                                              org_admin_user="org_admin_user",
                                                                              org_admin_password="org_admin_password",
                                                                              first_name="First Name",
                                                                              last_name="Last Name",
                                                                              email="email@email.com",
                                                                              admin_user='admin_user',
                                                                              admin_password='admin_password',
                                                                              pam=False)

    def test_org_present_update_org(self):
        current_user = {'uui': 'org_admin_user',
                        'email': 'old_mail@mail.com',
                        'first_name': 'first',
                        'last_name': 'last'}
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.org_get_details': MagicMock(return_value={'id': 100, 'name': 'my_org'}),
            'uyuni.user_get_details': MagicMock(return_value=current_user),
            'uyuni.user_set_details': MagicMock()}):
            result = uyuni_config.org_present('my_org', 'org_admin_user', 'org_admin_password',
                                             'First Name', 'Last Name', 'email@email.com',
                                              admin_user='admin_user',
                                              admin_password='admin_password')

            assert result is not None
            assert result['name'] == 'my_org'
            assert result['result']
            assert result['comment'] == 'my_org org successfully modified'
            assert result['changes'] == {'email': {'new': 'email@email.com',
                                                   'old': 'old_mail@mail.com'},
                                         'first_name': {'new': 'First Name',
                                                        'old': 'first'},
                                         'last_name': {'new': 'Last Name',
                                                       'old': 'last'}}

            uyuni_config.__salt__['uyuni.org_get_details'].assert_called_once_with('my_org',
                                                                                   admin_user='admin_user',
                                                                                   admin_password='admin_password')
            uyuni_config.__salt__['uyuni.user_get_details'].assert_called_once_with('org_admin_user',
                                                                                    org_admin_user='org_admin_user',
                                                                                    org_admin_password='org_admin_password')

            uyuni_config.__salt__['uyuni.user_set_details'].assert_called_once_with(login='org_admin_user',
                                                                                    password='org_admin_password',
                                                                                    email='email@email.com',
                                                                                    first_name='First Name',
                                                                                    last_name='Last Name',
                                                                                    org_admin_user='org_admin_user',
                                                                                    org_admin_password='org_admin_password')

    def test_org_present_no_changes(self):
        current_user = {'uui': 'org_admin_user',
                        'email': 'email@email.com',
                        'first_name': 'First Name',
                        'last_name': 'Last Name'}
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.org_get_details': MagicMock(return_value={'id': 100, 'name': 'my_org'}),
            'uyuni.user_get_details': MagicMock(return_value=current_user),
            'uyuni.user_set_details': MagicMock()}):
            result = uyuni_config.org_present('my_org', 'org_admin_user', 'org_admin_password',
                                             'First Name', 'Last Name', 'email@email.com',
                                              admin_user='admin_user',
                                              admin_password='admin_password')

            assert result is not None
            assert result['name'] == 'my_org'
            assert result['result']
            assert result['comment'] == 'my_org is already in the desired state'
            assert result['changes'] == {}

            uyuni_config.__salt__['uyuni.org_get_details'].assert_called_once_with('my_org',
                                                                                   admin_user='admin_user',
                                                                                   admin_password='admin_password')
            uyuni_config.__salt__['uyuni.user_get_details'].assert_called_once_with('org_admin_user',
                                                                                    org_admin_user='org_admin_user',
                                                                                    org_admin_password='org_admin_password')

    def test_org_absent_success_test(self):
        with patch.dict(uyuni_config.__salt__,
                        {'uyuni.org_get_details': MagicMock(return_value={'id': 100, 'name': 'my_org'})}):
            with patch.dict(uyuni_config.__opts__, {'test': True}):
                result = uyuni_config.org_absent('my_org',
                                                 admin_user='admin_user',
                                                 admin_password='admin_password')

                assert result is not None
                assert result['name'] == 'my_org'
                assert result['result'] is None
                assert result['comment'] == 'my_org would be removed'
                assert result['changes'] == {}
                uyuni_config.__salt__['uyuni.org_get_details'].assert_called_once_with('my_org',
                                                                                       admin_user='admin_user',
                                                                                       admin_password='admin_password')

    def test_org_absent_success(self):
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.org_get_details': MagicMock(return_value={'id': 100, 'name': 'my_org'}),
            'uyuni.org_delete': MagicMock()}):
            result = uyuni_config.org_absent('my_org',
                                             admin_user='admin_user',
                                             admin_password='admin_password')

            assert result is not None
            assert result['name'] == 'my_org'
            assert result['result']
            assert result['comment'] == 'Org my_org has been deleted'
            assert result['changes'] == {'name': {'old': 'my_org'}}
            uyuni_config.__salt__['uyuni.org_get_details'].assert_called_once_with('my_org',
                                                                                   admin_user='admin_user',
                                                                                   admin_password='admin_password')
            uyuni_config.__salt__['uyuni.org_delete'].assert_called_once_with('my_org',
                                                                              admin_user='admin_user',
                                                                              admin_password='admin_password')

    def test_org_absent_not_present(self):
        exc = Exception("org not found")
        exc.faultCode = 2850

        with patch.dict(uyuni_config.__salt__, {'uyuni.org_get_details': MagicMock(side_effect=exc)}):
            result = uyuni_config.org_absent('my_org',
                                             admin_user='admin_user',
                                             admin_password='admin_password')

            assert result is not None
            assert result['name'] == 'my_org'
            assert result['result']
            assert result['comment'] == 'my_org is already absent'
            assert result['changes'] == {}
            uyuni_config.__salt__['uyuni.org_get_details'].assert_called_once_with('my_org',
                                                                                   admin_user='admin_user',
                                                                                   admin_password='admin_password')


class TestManageOrgsTrust:

    def test_org_trust_test(self):
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.org_trust_list_trusts': MagicMock(
                return_value=[{'orgId': 2, 'orgName': 'new_org_1', 'trustEnabled': True},
                              {'orgId': 3, 'orgName': 'new_org_2', 'trustEnabled': False}]),
            'uyuni.org_get_details': MagicMock(return_value={'id': 1, 'name': 'my_org'})}):
            with patch.dict(uyuni_config.__opts__, {'test': True}):
                result = uyuni_config.org_trust('state_name', 'my_org', ['new_org_1', 'new_org_2'],
                                                admin_user='admin_user',
                                                admin_password='admin_password')

                assert result is not None
                assert result['name'] == 'state_name'
                assert result['result'] is None
                assert result['comment'] == 'my_org would be created'
                assert result['changes'] == {'new_org_2': {'new': True, 'old': None}}

                uyuni_config.__salt__['uyuni.org_trust_list_trusts'].assert_called_once_with('my_org',
                                                                                             admin_user='admin_user',
                                                                                             admin_password='admin_password')
                uyuni_config.__salt__['uyuni.org_get_details'].assert_called_once_with('my_org',
                                                                                       admin_user='admin_user',
                                                                                       admin_password='admin_password')

    def test_org_trust_update(self):
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.org_trust_list_trusts': MagicMock(
                return_value=[{'orgId': 2, 'orgName': 'new_org_1', 'trustEnabled': True},
                              {'orgId': 3, 'orgName': 'new_org_2', 'trustEnabled': False},
                              {'orgId': 4, 'orgName': 'new_org_3', 'trustEnabled': True}]),
            'uyuni.org_get_details': MagicMock(return_value={'id': 1, 'name': 'my_org'}),
            'uyuni.org_trust_add_trust': MagicMock(return_value=True),
            'uyuni.org_trust_remove_trust': MagicMock(return_value=True)}):

            result = uyuni_config.org_trust('state_name', 'my_org', ['new_org_1', 'new_org_2'],
                                            admin_user='admin_user',
                                            admin_password='admin_password')

            assert result is not None
            assert result['name'] == 'state_name'
            assert result['result']
            assert result['comment'] == "Org 'my_org' trusts successfully modified"
            assert result['changes'] == {'new_org_2': {'new': True, 'old': None},
                                         'new_org_3': {'new': None, 'old': True}}

            uyuni_config.__salt__['uyuni.org_trust_list_trusts'].assert_called_once_with('my_org',
                                                                                         admin_user='admin_user',
                                                                                         admin_password='admin_password')
            uyuni_config.__salt__['uyuni.org_get_details'].assert_called_once_with('my_org',
                                                                                   admin_user='admin_user',
                                                                                   admin_password='admin_password')
            uyuni_config.__salt__['uyuni.org_trust_add_trust'].assert_called_once_with(1, 3,
                                                                                       admin_user='admin_user',
                                                                                       admin_password='admin_password')
            uyuni_config.__salt__['uyuni.org_trust_remove_trust'].assert_called_once_with(1, 4,
                                                                                          admin_user='admin_user',
                                                                                          admin_password='admin_password')


    def test_org_trust_no_changes(self):
        with patch.dict(uyuni_config.__salt__, {
            'uyuni.org_trust_list_trusts': MagicMock(
                return_value=[{'orgId': 2, 'orgName': 'new_org_1', 'trustEnabled': True},
                              {'orgId': 3, 'orgName': 'new_org_2', 'trustEnabled': True},
                              {'orgId': 4, 'orgName': 'new_org_3', 'trustEnabled': False}]),
            'uyuni.org_get_details': MagicMock(return_value={'id': 1, 'name': 'my_org'})}):

            result = uyuni_config.org_trust('state_name', 'my_org', ['new_org_1', 'new_org_2'],
                                            admin_user='admin_user',
                                            admin_password='admin_password')

            assert result is not None
            assert result['name'] == 'state_name'
            assert result['result']
            assert result['comment'] == 'my_org is already in the desired state'
            assert result['changes'] == {}

            uyuni_config.__salt__['uyuni.org_trust_list_trusts'].assert_called_once_with('my_org',
                                                                                         admin_user='admin_user',
                                                                                         admin_password='admin_password')
            uyuni_config.__salt__['uyuni.org_get_details'].assert_called_once_with('my_org',
                                                                                   admin_user='admin_user',
                                                                                   admin_password='admin_password')


class TestUyuniActivationKeys:
    def test_ak_absent_not_present(self):
        exc = Exception("ak not found")
        exc.faultCode = -212

        with patch.dict(uyuni_config.__salt__, {'uyuni.activation_key_get_details': MagicMock(side_effect=exc)}):
            result = uyuni_config.activation_key_absent('1-ak',
                                                        org_admin_user='org_admin_user',
                                                        org_admin_password='org_admin_password')

            assert result is not None
            assert result['name'] == '1-ak'
            assert result['result']
            assert result['comment'] == '1-ak is already absent'
            assert result['changes'] == {}
            uyuni_config.__salt__['uyuni.activation_key_get_details'].assert_called_once_with('1-ak',
                                                                                              org_admin_user='org_admin_user',
                                                                                              org_admin_password='org_admin_password')

    def test_ak_absent_present(self):

        with patch.dict(uyuni_config.__salt__, {'uyuni.activation_key_get_details': MagicMock(return_value={}),
                                                'uyuni.activation_key_delete': MagicMock()}):

            result = uyuni_config.activation_key_absent('1-ak',
                                                        org_admin_user='org_admin_user',
                                                        org_admin_password='org_admin_password')

            assert result is not None
            assert result['name'] == '1-ak'
            assert result['result']
            assert result['comment'] == 'Activation Key 1-ak has been deleted'
            assert result['changes'] == {'id': {'old': '1-ak'}}
            uyuni_config.__salt__['uyuni.activation_key_get_details'].assert_called_once_with('1-ak',
                                                                                              org_admin_user='org_admin_user',
                                                                                              org_admin_password='org_admin_password')
            uyuni_config.__salt__['uyuni.activation_key_delete'].assert_called_once_with('1-ak',
                                                                                          org_admin_user='org_admin_user',
                                                                                          org_admin_password='org_admin_password')
