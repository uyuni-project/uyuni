import pytest
from mock import MagicMock, patch, call
from . import mockery
import pdb

mockery.setup_environment()

import sys

sys.path.append("../_states")
import uyuni_users

# Mock globals
uyuni_users.log = MagicMock()
uyuni_users.__salt__ = {}
uyuni_users.__opts__ = {'test': False}


class TestManageUser:

    def test_user_present_new_user_test(self):
        exc = Exception("user not found")
        exc.faultCode = 2951

        with patch.dict(uyuni_users.__salt__, {'uyuni.user_get_details': MagicMock(side_effect=exc)}):
            with patch.dict(uyuni_users.__opts__, {'test': True}):
                result = uyuni_users.user_present('username', 'password', 'mail@mail.com',
                                                  'first_name', 'last_name',
                                                  ['role'], ['group'],
                                                  'org_admin_user', 'org_admin_password')
                assert result is not None
                assert result['name'] == 'username'
                assert result['result'] is None
                assert result['comment'] == 'username would be installed'

                assert result['changes'] == {
                    'uid': {'new': 'username'},
                    'password': {'new': '(hidden)'},
                    'email': {'new': 'mail@mail.com'},
                    'first_name': {'new': 'first_name'},
                    'last_name': {'new': 'last_name'},
                    'roles': {'new': ['role']},
                    'system_groups': {'new': ['group']}}

                uyuni_users.__salt__['uyuni.user_get_details'].assert_called_once_with('username',
                                                                                       org_admin_user='org_admin_user',
                                                                                       org_admin_password='org_admin_password')

    def test_user_present_new_user_minimal(self):
        exc = Exception("user not found")
        exc.faultCode = 2951

        with patch.dict(uyuni_users.__salt__, {
            'uyuni.user_get_details': MagicMock(side_effect=exc),
            'uyuni.user_create': MagicMock(return_value=True)}):
            result = uyuni_users.user_present('username', 'password', 'mail@mail.com',
                                              'first_name', 'last_name')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result'] is True
            assert result['comment'] == 'username user successful managed'

            assert result['changes'] == {
                'uid': {'new': 'username'},
                'password': {'new': '(hidden)'},
                'email': {'new': 'mail@mail.com'},
                'first_name': {'new': 'first_name'},
                'last_name': {'new': 'last_name'}}

            ## verify mock calls
            uyuni_users.__salt__['uyuni.user_get_details'].assert_called_once_with('username',
                                                                                   org_admin_user=None,
                                                                                   org_admin_password=None)

            uyuni_users.__salt__['uyuni.user_create'].assert_called_once_with(email='mail@mail.com',
                                                                              first_name='first_name',
                                                                              last_name='last_name',
                                                                              org_admin_password=None,
                                                                              org_admin_user=None,
                                                                              password='password',
                                                                              uid='username')

    def test_user_present_new_user_complete(self):
        exc = Exception("user not found")
        exc.faultCode = 2951

        with patch.dict(uyuni_users.__salt__, {
            'uyuni.user_get_details': MagicMock(side_effect=exc),
            'uyuni.user_create': MagicMock(return_value=True),
            'uyuni.user_add_role': MagicMock(return_value=True),
            'uyuni.user_add_assigned_system_groups': MagicMock(return_value=1)}):
            result = uyuni_users.user_present('username', 'password', 'mail@mail.com',
                                              'first_name', 'last_name',
                                              ['role'], ['group'],
                                              'org_admin_user', 'org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result'] is True
            assert result['comment'] == 'username user successful managed'

            assert result['changes'] == {
                'uid': {'new': 'username'},
                'password': {'new': '(hidden)'},
                'email': {'new': 'mail@mail.com'},
                'first_name': {'new': 'first_name'},
                'last_name': {'new': 'last_name'},
                'roles': {'new': ['role']},
                'system_groups': {'new': ['group']}}

            ## verify mock calls
            uyuni_users.__salt__['uyuni.user_get_details'].assert_called_once_with('username',
                                                                                   org_admin_user='org_admin_user',
                                                                                   org_admin_password='org_admin_password')

            uyuni_users.__salt__['uyuni.user_create'].assert_called_once_with(email='mail@mail.com',
                                                                              first_name='first_name',
                                                                              last_name='last_name',
                                                                              org_admin_password='org_admin_password',
                                                                              org_admin_user='org_admin_user',
                                                                              password='password',
                                                                              uid='username')

            uyuni_users.__salt__['uyuni.user_add_role'].assert_called_once_with('username', role='role',
                                                                                org_admin_user='org_admin_user',
                                                                                org_admin_password='org_admin_password')

            uyuni_users.__salt__['uyuni.user_add_assigned_system_groups'].assert_called_once_with(uid='username',
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

        with patch.dict(uyuni_users.__salt__, {
            'uyuni.user_get_details': MagicMock(side_effect=[current_user, exc]),
            'uyuni.user_list_roles': MagicMock(return_value=['role1', 'role2']),
            'uyuni.user_list_assigned_system_groups': MagicMock(return_value=[{'name': 'group1'}, {'name': 'group2'}]),
            'uyuni.user_set_details': MagicMock(return_value=True),
            'uyuni.user_remove_role': MagicMock(return_value=True),
            'uyuni.user_add_role': MagicMock(return_value=True),
            'uyuni.user_remove_assigned_system_groups': MagicMock(return_value=1),
            'uyuni.user_add_assigned_system_groups': MagicMock(return_value=1)}):
            result = uyuni_users.user_present('username', 'new_password', 'new_mail@mail.com',
                                              'new_first', 'new_last',
                                              ['role1', 'role3'], ['group2', 'group3'],
                                              'org_admin_user', 'org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result'] is True
            assert result['comment'] == 'username user successful managed'
            assert result['changes'] == {
                'password': {'new': '(hidden)', 'old': '(hidden)'},
                'email': {'new': 'new_mail@mail.com', 'old': 'mail@mail.com'},
                'first_name': {'new': 'new_first', 'old': 'first'},
                'last_name': {'new': 'new_last', 'old': 'last'},
                'roles': {'new': ['role1', 'role3'], 'old': ['role1', 'role2']},
                'system_groups': {'new': ['group2', 'group3'], 'old': ['group1', 'group2']}}

            ## verify mock calls
            uyuni_users.__salt__['uyuni.user_get_details'].assert_has_calls([call('username',
                                                                                  org_admin_user='org_admin_user',
                                                                                  org_admin_password='org_admin_password'),
                                                                             call('username', 'new_password')])

            uyuni_users.__salt__['uyuni.user_list_roles'].assert_called_once_with('username',
                                                                                  org_admin_user='org_admin_user',
                                                                                  org_admin_password='org_admin_password')

            uyuni_users.__salt__['uyuni.user_list_assigned_system_groups'].assert_called_once_with('username',
                                                                                                   org_admin_user='org_admin_user',
                                                                                                   org_admin_password='org_admin_password')

            uyuni_users.__salt__['uyuni.user_set_details'].assert_called_once_with(email='new_mail@mail.com',
                                                                                   first_name='new_first',
                                                                                   last_name='new_last',
                                                                                   org_admin_password='org_admin_password',
                                                                                   org_admin_user='org_admin_user',
                                                                                   password='new_password',
                                                                                   uid='username')

            uyuni_users.__salt__['uyuni.user_remove_role'].assert_called_once_with('username', role='role2',
                                                                                   org_admin_user='org_admin_user',
                                                                                   org_admin_password='org_admin_password')
            uyuni_users.__salt__['uyuni.user_add_role'].assert_called_once_with('username', role='role3',
                                                                                org_admin_user='org_admin_user',
                                                                                org_admin_password='org_admin_password')

            uyuni_users.__salt__['uyuni.user_remove_assigned_system_groups'].assert_called_once_with(uid='username',
                                                                                                     server_group_names=[
                                                                                                         'group1'],
                                                                                                     org_admin_user='org_admin_user',
                                                                                                     org_admin_password='org_admin_password')
            uyuni_users.__salt__['uyuni.user_add_assigned_system_groups'].assert_called_once_with(uid='username',
                                                                                                  server_group_names=[
                                                                                                      'group3'],
                                                                                                  org_admin_user='org_admin_user',
                                                                                                  org_admin_password='org_admin_password')

    def test_user_absent_auth_error(self):
        exc = Exception("Auth error")
        exc.faultCode = 2950

        with patch.dict(uyuni_users.__salt__, {'uyuni.user_get_details': MagicMock(side_effect=exc)}):
            result = uyuni_users.user_absent('username',
                                             'org_admin_user', 'org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result'] is False
            assert result['comment'] == "Error deleting user (organization credentials error) 'username': Auth error"
            assert result['changes'] == {}

    def test_user_absent_user_not_exits(self):
        exc = Exception("User not found")
        exc.faultCode = -213

        with patch.dict(uyuni_users.__salt__, {'uyuni.user_get_details': MagicMock(side_effect=exc)}):
            result = uyuni_users.user_absent('username',
                                             'org_admin_user', 'org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result'] is True
            assert result['comment'] == "username is already absent"
            assert result['changes'] == {}

    def test_user_absent_generic_error(self):
        exc = Exception("generic error")
        exc.faultCode = 2951

        with patch.dict(uyuni_users.__salt__, {'uyuni.user_get_details': MagicMock(side_effect=exc)}):
            with pytest.raises(Exception) as e:
                uyuni_users.user_absent('username',
                                        'org_admin_user', 'org_admin_password')
            assert e.value.faultCode == 2951
            assert e.value.args[0] == 'generic error'

    def test_user_absent_exists_test(self):
        current_user = {'uui': 'username',
                        'email': 'mail@mail.com',
                        'first_name': 'first',
                        'last_name': 'last'}

        with patch.dict(uyuni_users.__salt__, {'uyuni.user_get_details': MagicMock(return_value=current_user)}):
            with patch.dict(uyuni_users.__opts__, {'test': True}):
                result = uyuni_users.user_absent('username',
                                                 'org_admin_user', 'org_admin_password')

                assert result is not None
                assert result['name'] == 'username'
                assert result['result'] is None
                assert result['comment'] == 'username would be removed'

                assert result['changes'] == {
                    'uid': {'old': 'username'},
                    'email': {'old': 'mail@mail.com'},
                    'first_name': {'old': 'first'},
                    'last_name': {'old': 'last'}}

                uyuni_users.__salt__['uyuni.user_get_details'].assert_called_once_with('username',
                                                                                       org_admin_user='org_admin_user',
                                                                                       org_admin_password='org_admin_password')

    def test_user_absent_exist_user(self):
        current_user = {'uui': 'username',
                        'email': 'mail@mail.com',
                        'first_name': 'first',
                        'last_name': 'last'}

        with patch.dict(uyuni_users.__salt__, {
            'uyuni.user_get_details': MagicMock(return_value=current_user),
            'uyuni.user_delete': MagicMock(return_value=True)}):
            result = uyuni_users.user_absent('username',
                                             'org_admin_user', 'org_admin_password')

            assert result is not None
            assert result['name'] == 'username'
            assert result['result'] is True
            assert result['comment'] == 'User username has been deleted'

            assert result['changes'] == {
                'uid': {'old': 'username'},
                'email': {'old': 'mail@mail.com'},
                'first_name': {'old': 'first'},
                'last_name': {'old': 'last'}}

            uyuni_users.__salt__['uyuni.user_get_details'].assert_called_once_with('username',
                                                                                   org_admin_user='org_admin_user',
                                                                                   org_admin_password='org_admin_password')
            uyuni_users.__salt__['uyuni.user_delete'].assert_called_once_with('username',
                                                                              org_admin_user='org_admin_user',
                                                                              org_admin_password='org_admin_password')


class TestManageUserChannels:

    def test_user_channels_org_admin(self):
        with patch.dict(uyuni_users.__salt__, {
            'uyuni.user_list_roles': MagicMock(return_value=["channel_admin"]),
            'uyuni.channel_list_manageable_channels': MagicMock(),
            'uyuni.channel_list_my_channels': MagicMock()}):
            result = uyuni_users.user_channels('username', 'password',
                                               org_admin_user='org_admin_user',
                                               org_admin_password='org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert not result['result']
            assert result['changes'] == {}
            assert 'org_admin' in result['comment']

            uyuni_users.__salt__['uyuni.user_list_roles'].assert_called_once_with('username', password='password')

            uyuni_users.__salt__['uyuni.channel_list_manageable_channels'].assert_called_once_with('username',
                                                                                                   'password')

            uyuni_users.__salt__['uyuni.channel_list_my_channels'].assert_called_once_with('username',
                                                                                           'password')

    def test_user_channels_channel_admin(self):
        with patch.dict(uyuni_users.__salt__, {
            'uyuni.user_list_roles': MagicMock(return_value=["channel_admin"]),
            'uyuni.channel_list_manageable_channels': MagicMock(),
            'uyuni.channel_list_my_channels': MagicMock()}):
            result = uyuni_users.user_channels('username', 'password',
                                               org_admin_user='org_admin_user',
                                               org_admin_password='org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert not result['result']
            assert result['changes'] == {}
            assert 'channel_admin' in result['comment']

            uyuni_users.__salt__['uyuni.user_list_roles'].assert_called_once_with('username', password='password')

            uyuni_users.__salt__['uyuni.channel_list_manageable_channels'].assert_called_once_with('username',
                                                                                                   'password')

            uyuni_users.__salt__['uyuni.channel_list_my_channels'].assert_called_once_with('username',
                                                                                           'password')

    def test_user_channels_add_all(self):
        with patch.dict(uyuni_users.__salt__, {
            'uyuni.user_list_roles': MagicMock(return_value=[]),
            'uyuni.channel_list_manageable_channels': MagicMock(return_value=[]),
            'uyuni.channel_list_my_channels': MagicMock(return_value=[]),
            'uyuni.channel_software_set_user_manageable': MagicMock(),
            'uyuni.channel_software_set_user_subscribable': MagicMock()}):
            result = uyuni_users.user_channels('username', 'password',
                                               manageable_channels=['manage1'],
                                               subscribable_channels=['subscribe1'],
                                               org_admin_user='org_admin_user',
                                               org_admin_password='org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result']
            assert result['changes'] == {'manageable_channels': {'manage1': True},
                                         'subscribable_channels': {'subscribe1': True}}

            uyuni_users.__salt__['uyuni.user_list_roles'].assert_called_once_with('username', password='password')

            uyuni_users.__salt__['uyuni.channel_list_manageable_channels'].assert_called_once_with('username',
                                                                                                   'password')

            uyuni_users.__salt__['uyuni.channel_list_my_channels'].assert_called_once_with('username',
                                                                                           'password')

            uyuni_users.__salt__['uyuni.channel_software_set_user_manageable'].assert_called_once_with('manage1',
                                                                                                       'username',
                                                                                                       True,
                                                                                                       'org_admin_user',
                                                                                                       'org_admin_password')

            uyuni_users.__salt__['uyuni.channel_software_set_user_subscribable'].assert_called_once_with('subscribe1',
                                                                                                         'username',
                                                                                                         True,
                                                                                                         'org_admin_user',
                                                                                                         'org_admin_password')

    def test_user_channels_no_changes(self):
        with patch.dict(uyuni_users.__salt__, {
            'uyuni.user_list_roles': MagicMock(return_value=[]),
            'uyuni.channel_list_manageable_channels': MagicMock(return_value=[{"label": "manage1"}]),
            'uyuni.channel_list_my_channels': MagicMock(return_value=[{"label":"subscribe1"}]),
            'uyuni.channel_software_set_user_manageable': MagicMock(),
            'uyuni.channel_software_set_user_subscribable': MagicMock()}):
            result = uyuni_users.user_channels('username', 'password',
                                               manageable_channels=['manage1'],
                                               subscribable_channels=['subscribe1'],
                                               org_admin_user='org_admin_user',
                                               org_admin_password='org_admin_password')
            assert result is not None
            assert result['name'] == 'username'
            assert result['result']
            assert result['changes'] == {}

            uyuni_users.__salt__['uyuni.user_list_roles'].assert_called_once_with('username', password='password')

            uyuni_users.__salt__['uyuni.channel_list_manageable_channels'].assert_called_once_with('username',
                                                                                                   'password')

            uyuni_users.__salt__['uyuni.channel_list_my_channels'].assert_called_once_with('username',
                                                                                           'password')

    def test_user_channels_managed_subscribe_change(self):
        with patch.dict(uyuni_users.__salt__, {
            'uyuni.user_list_roles': MagicMock(return_value=[]),
            'uyuni.channel_list_manageable_channels': MagicMock(return_value=[{"label": "manage1"}]),
            'uyuni.channel_list_my_channels': MagicMock(return_value=[{"label": "manage1"}]),
            'uyuni.channel_software_set_user_manageable': MagicMock(),
            'uyuni.channel_software_set_user_subscribable': MagicMock()}):
            result = uyuni_users.user_channels('username', 'password',
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

            uyuni_users.__salt__['uyuni.user_list_roles'].assert_called_once_with('username', password='password')

            uyuni_users.__salt__['uyuni.channel_list_manageable_channels'].assert_called_once_with('username',
                                                                                                   'password')

            uyuni_users.__salt__['uyuni.channel_list_my_channels'].assert_called_once_with('username',
                                                                                           'password')

            uyuni_users.__salt__['uyuni.channel_software_set_user_manageable'].assert_called_once_with('manage1',
                                                                                                       'username',
                                                                                                       False,
                                                                                                       'org_admin_user',
                                                                                                       'org_admin_password')

            uyuni_users.__salt__['uyuni.channel_software_set_user_subscribable'].assert_called_once_with('manage1',
                                                                                                         'username',
                                                                                                         True,
                                                                                                         'org_admin_user',
                                                                                                         'org_admin_password')

