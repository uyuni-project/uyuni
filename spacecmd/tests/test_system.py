# coding: utf-8
"""
Test suite for spacecmd.system module.
"""
from datetime import datetime
from unittest.mock import MagicMock, patch, mock_open
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.system


class TestSystem:
    """
    Test suite for "system" module.
    """

    def test_help_system_listevents_new_version_api_deprecation(self, shell):
        """
            test help_system_listevents to ensure the deprecation warning is shown for recent API version
        """
        shell.check_api_version = MagicMock(return_value=True)

        m_logger = MagicMock()

        with patch("spacecmd.system.logging", m_logger):
            spacecmd.system.help_system_listevents(shell)

        assert m_logger.warning.called
        assert_list_args_expect(m_logger.warning.call_args_list,
                                ['This method is deprecated and will be removed in a future API version. '
                                 'Please use system_listeventhistory instead.\n'])

    def test_help_system_listevents_old_version_api_no_deprecation(self, shell):
        """
            test help_system_listevents to ensure the deprecation warning is shown for recent API version
        """
        shell.check_api_version = MagicMock(return_value=False)

        m_logger = MagicMock()

        with patch("spacecmd.system.logging", m_logger):
            spacecmd.system.help_system_listevents(shell)

        assert not m_logger.warning.called

    def test_do_system_listevents_new_version_api_deprecation(self, shell):
        """
            test do_system_listevents to ensure the deprecation warning is shown for recent API version
        """
        shell.check_api_version = MagicMock(return_value=True)
        shell.client.system.getEventHistory = MagicMock()

        shell.expand_systems = MagicMock(return_value=["system-a"])
        shell.get_system_id = MagicMock(side_effect=[1000010000])

        m_logger = MagicMock()

        with patch("spacecmd.system.logging", m_logger):
            spacecmd.system.do_system_listevents(shell, "123456789")

        assert m_logger.warning.called
        assert_list_args_expect(m_logger.warning.call_args_list,
                                ['This method is deprecated and will be removed in a future API version. '
                                 'Please use system_listeventhistory instead.\n'])

        assert shell.client.system.getEventHistory.called

    def test_do_system_listeventhistory_noargs(self, shell):
        """
        Test do_system_listeventhistory without arguments
         """

        shell.help_system_listeventhistory = MagicMock()
        shell.client.system.getEventHistory = MagicMock()

        spacecmd.system.do_system_listeventhistory(shell, "")

        assert shell.help_system_listeventhistory.called
        assert not shell.client.system.getEventHistory.called

    def test_do_system_listeventhistory_old_version(self, shell):
        """
        Test do_system_listeventhistory with an old API version
         """

        shell.help_system_listeventhistory = MagicMock()
        shell.client.system.getEventHistory = MagicMock()

        shell.check_api_version = MagicMock(return_value=None)

        m_logger = MagicMock()

        with patch("spacecmd.system.logging", m_logger):
            spacecmd.system.do_system_listeventhistory(shell, "")

        assert m_logger.warning.called
        assert_list_args_expect(m_logger.warning.call_args_list,
                                ["This version of the API doesn't support this method"])

        assert not shell.help_system_listeventhistory.called
        assert not shell.client.system.getEventHistory.called

    def test_do_system_listeventhistory_only_server(self, shell):
        """
        Test do_system_listeventhistory with only the server parameter
         """

        shell.help_system_listeventhistory = MagicMock()
        shell.client.system.getEventHistory = MagicMock()
        shell.session = MagicMock()

        shell.expand_systems = MagicMock(return_value=["system-a"])
        shell.get_system_id = MagicMock(side_effect=[1000010000])

        spacecmd.system.do_system_listeventhistory(shell, "123456789")

        assert shell.client.system.getEventHistory.called
        assert_args_expect(shell.client.system.getEventHistory.call_args_list,
                           [((shell.session, 1000010000, datetime(1970, 1, 1)), {})])

        assert not shell.help_system_listeventhistory.called

    def test_do_system_listeventhistory_server_and_start_time(self, shell):
        """
        Test do_system_listeventhistory with server and start time
         """

        shell.help_system_listeventhistory = MagicMock()
        shell.client.system.getEventHistory = MagicMock()
        shell.session = MagicMock()

        shell.expand_systems = MagicMock(return_value=["system-a"])
        shell.get_system_id = MagicMock(side_effect=[1000010000])

        spacecmd.system.do_system_listeventhistory(shell, "123456789 -s 20211020")

        assert shell.client.system.getEventHistory.called
        assert_args_expect(shell.client.system.getEventHistory.call_args_list,
                           [((shell.session, 1000010000, datetime(2021, 10, 20)), {})])

        assert not shell.help_system_listeventhistory.called

    def test_do_system_listeventhistory_server_and_start_time_and_limit(self, shell):
        """
        Test do_system_listeventhistory with server, start time and limit
         """

        shell.help_system_listeventhistory = MagicMock()
        shell.client.system.getEventHistory = MagicMock()
        shell.session = MagicMock()

        shell.expand_systems = MagicMock(return_value=["system-a"])
        shell.get_system_id = MagicMock(side_effect=[1000010000])

        spacecmd.system.do_system_listeventhistory(shell, "123456789 -s 20211020 -l 10")

        assert shell.client.system.getEventHistory.called
        assert_args_expect(shell.client.system.getEventHistory.call_args_list,
                           [((shell.session, 1000010000, datetime(2021, 10, 20), 0, 10), {})])

        assert not shell.help_system_listeventhistory.called

    def test_do_system_listeventhistory_server_and_start_time_and_limit_and_offset(self, shell):
        """
        Test do_system_listeventhistory with server, start time, limit and offset
         """

        shell.help_system_listeventhistory = MagicMock()
        shell.client.system.getEventHistory = MagicMock()
        shell.session = MagicMock()

        shell.expand_systems = MagicMock(return_value=["system-a"])
        shell.get_system_id = MagicMock(side_effect=[1000010000])

        spacecmd.system.do_system_listeventhistory(shell, "123456789 -s 20211020 -l 10 -o 5")

        assert shell.client.system.getEventHistory.called
        assert_args_expect(shell.client.system.getEventHistory.call_args_list,
                           [((shell.session, 1000010000, datetime(2021, 10, 20), 5, 10), {})])

        assert not shell.help_system_listeventhistory.called

    def test_do_system_listeventhistory_invalid_start_time(self, shell):
        """
        Test do_system_listeventhistory with an invalid start time parameter
         """

        shell.help_system_listeventhistory = MagicMock()
        shell.client.system.getEventHistory = MagicMock()
        shell.session = MagicMock()

        shell.expand_systems = MagicMock(return_value=["system-a"])
        shell.get_system_id = MagicMock(side_effect=[1000010000])

        m_logger = MagicMock()

        with patch("spacecmd.utils.logging", m_logger):
            spacecmd.system.do_system_listeventhistory(shell, "123456789 -s qwe123")

        assert m_logger.error.called
        assert_list_args_expect(m_logger.error.call_args_list,
                                ['Invalid time provided'])

        assert not shell.client.system.getEventHistory.called
        assert not shell.help_system_listeventhistory.called

    def test_do_system_listeventhistory_invalid_limit(self, shell):
        """
        Test do_system_listeventhistory with an invalid limit parameter
         """

        shell.help_system_listeventhistory = MagicMock()
        shell.client.system.getEventHistory = MagicMock()
        shell.session = MagicMock()

        shell.expand_systems = MagicMock(return_value=["system-a"])
        shell.get_system_id = MagicMock(side_effect=[1000010000])

        m_logger = MagicMock()

        with patch("spacecmd.system.logging", m_logger):
            spacecmd.system.do_system_listeventhistory(shell, "123456789 -l wrong")

        assert m_logger.error.called
        assert_list_args_expect(m_logger.error.call_args_list,
                                ['Invalid limit'])

        assert not shell.client.system.getEventHistory.called
        assert not shell.help_system_listeventhistory.called

    def test_do_system_listeventhistory_invalid_offset(self, shell):
        """
        Test do_system_listeventhistory with an invalid offset parameter
         """

        shell.help_system_listeventhistory = MagicMock()
        shell.client.system.getEventHistory = MagicMock()
        shell.session = MagicMock()

        shell.expand_systems = MagicMock(return_value=["system-a"])
        shell.get_system_id = MagicMock(side_effect=[1000010000])

        m_logger = MagicMock()

        with patch("spacecmd.system.logging", m_logger):
            spacecmd.system.do_system_listeventhistory(shell, "123456789 -l 10 -o wrong")

        assert m_logger.error.called
        assert_list_args_expect(m_logger.error.call_args_list,
                                ['Invalid offset'])

        assert not shell.client.system.getEventHistory.called
        assert not shell.help_system_listeventhistory.called

    def test_do_system_listeventhistory_offset_ignore_when_limit_not_provided(self, shell):
        """
        Test do_system_listeventhistory to make sure the offset is ignored if the limit is not specified as well
         """

        shell.help_system_listeventhistory = MagicMock()
        shell.client.system.getEventHistory = MagicMock()
        shell.session = MagicMock()

        shell.expand_systems = MagicMock(return_value=["system-a"])
        shell.get_system_id = MagicMock(side_effect=[1000010000])

        spacecmd.system.do_system_listeventhistory(shell, "123456789 -o 10")

        assert shell.client.system.getEventHistory.called
        assert_args_expect(shell.client.system.getEventHistory.call_args_list,
                           [((shell.session, 1000010000, datetime(1970, 1, 1)), {})])

        assert not shell.help_system_listeventhistory.called

    def test_do_system_listeventhistory_output(self, shell):
        """
        Test do_system_listeventhistory output format
         """

        shell.help_system_listeventhistory = MagicMock()
        shell.client.system.getEventHistory = MagicMock(return_value=[{
            "id": 3, "history_type": "Apply states", "status": "Completed",
            "summary": "Apply states [certs, channels] scheduled by (none)", "completed": "20211015T16:56:27",
        }, {
            "id": 1, "history_type": "History Event", "status": "(n/a)",
            "summary": "added system entitlement", "completed": "20211015T16:56:14",
        }])
        shell.session = MagicMock()

        shell.expand_systems = MagicMock(return_value=["system-a"])
        shell.get_system_id = MagicMock(side_effect=[1000010000])

        m_print = MagicMock()

        with patch("spacecmd.system.print", m_print):
            spacecmd.system.do_system_listeventhistory(shell, "123456789 -o 10")

        assert shell.client.system.getEventHistory.called
        assert not shell.help_system_listeventhistory.called

        expected = [
            '',
            'Id:           3',
            'History type: Apply states',
            'Status:       Completed',
            'Summary:      Apply states [certs, channels] scheduled by (none)',
            'Completed:    20211015T16:56:27',
            '',
            'Id:           1',
            'History type: History Event',
            'Status:       (n/a)',
            'Summary:      added system entitlement',
            'Completed:    20211015T16:56:14'
        ]

        for call in m_print.call_args_list:
            assert_expect([call], next(iter(expected)))
            expected.pop(0)
        assert not expected

    def test_do_system_event_details_noargs(self, shell):
        """
        Test do_system_event_details with no arguments.
        """

        shell.help_system_eventdetails = MagicMock()
        shell.client.system.getEventDetails = MagicMock()

        spacecmd.system.do_system_eventdetails(shell, "")

        assert shell.help_system_eventdetails.called
        assert not shell.client.system.getEventDetails.called

    def test_do_system_event_details_noevent(self, shell):
        """
        Test do_system_event_details with no event
        """

        shell.help_system_eventdetails = MagicMock()
        shell.client.system.getEventDetails = MagicMock()

        m_logger = MagicMock()

        with patch("spacecmd.system.logging", m_logger):
            spacecmd.system.do_system_eventdetails(shell, "123456789")

        assert m_logger.warning.called
        assert_list_args_expect(m_logger.warning.call_args_list, ['No event specified'])

        assert not shell.help_system_eventdetails.called
        assert not shell.client.system.getEventDetails.called

    def test_do_system_event_details_old_version(self, shell):
        """
        Test do_system_event_details when using an old version of the API
         """

        shell.help_system_eventdetails = MagicMock()
        shell.client.system.getEventDetails = MagicMock()
        shell.check_api_version = MagicMock(return_value=None)

        m_logger = MagicMock()

        with patch("spacecmd.system.logging", m_logger):
            spacecmd.system.do_system_eventdetails(shell, "123456789")

        assert m_logger.warning.called
        assert_list_args_expect(m_logger.warning.call_args_list,
                                ["This version of the API doesn't support this method"])

        assert not shell.help_system_eventdetails.called
        assert not shell.client.system.getEventDetails.called

    def test_do_system_event_details_history_output(self, shell):
        """
        Test do_system_event_details output format with an event of type history
        """

        shell.help_system_eventdetails = MagicMock()
        shell.expand_systems = MagicMock(return_value=["system-a"])
        shell.get_system_id = MagicMock(side_effect=[1000010000])
        shell.client.system.getEventDetails = MagicMock(return_value={
            "id": 1, "history_type": "History Event", "status": "(n/a)",
            "summary": "added system entitlement", "completed": "20211005T09:47:49"
        })

        m_logger = MagicMock()
        m_print = MagicMock()

        with patch("spacecmd.system.logging", m_logger), patch("spacecmd.system.print", m_print):
            spacecmd.system.do_system_eventdetails(shell, "123456 1")

        assert not shell.help_system_eventdetails.called
        assert not m_logger.warning.called

        assert shell.client.system.getEventDetails.called

        expected = [
            '',
            'Id:              1',
            '',
            'History type:    History Event',
            'Status:          (n/a)',
            'Summary:         added system entitlement',
            '',
            'Created:         None',
            'Picked up:       None',
            'Completed:       20211005T09:47:49'
        ]

        for call in m_print.call_args_list:
            assert_expect([call], next(iter(expected)))
            expected.pop(0)
        assert not expected

    def test_do_system_event_details_action_output(self, shell):
        """
        Test do_system_event_details output format with an event of type action
        """

        shell.help_system_eventdetails = MagicMock()
        shell.expand_systems = MagicMock(return_value=["system-a"])
        shell.get_system_id = MagicMock(side_effect=[1000010000])
        shell.client.system.getEventDetails = MagicMock(return_value={
            "id": 1, "history_type": "Apply states", "status": "Completed",
            "summary": "Apply states [certs] scheduled by (none)",
            "created": "20211005T09:47:53", "picked_up": "20211005T09:48:03",
            "completed": "20211005T09:48:18", "earliest_action": "20211005T09:47:53",
            "result_msg": "Successfully applied state(s): [certs]",
            "result_code": 0,
            "additional_info": [{
                "result": 0,
                "detail": """----------
          ID: /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
    Function: file.managed
        Name: /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
      Result: true
     Comment: File /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT updated
     Started: 09:48:15.847314
    Duration: 33.988
         SLS: certs
     Changed: diff: New file
              mode: '0644'"""
            }]
        })

        m_logger = MagicMock()
        m_print = MagicMock()

        with patch("spacecmd.system.logging", m_logger), patch("spacecmd.system.print", m_print):
            spacecmd.system.do_system_eventdetails(shell, "123456 1")

        assert not shell.help_system_eventdetails.called
        assert not m_logger.warning.called

        assert shell.client.system.getEventDetails.called

        expected = [
            '',
            'Id:              1',
            '',
            'History type:    Apply states',
            'Status:          Completed',
            'Summary:         Apply states [certs] scheduled by (none)',
            '',
            'Created:         20211005T09:47:53',
            'Picked up:       20211005T09:48:03',
            'Completed:       20211005T09:48:18',
            '',
            'Earliest action: 20211005T09:47:53',
            'Result message:  Successfully applied state(s): [certs]',
            'Result code:     0',
            '',
            'Additional info:',
            '    Result:          0',
            """    Detail:          ----------
          ID: /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
    Function: file.managed
        Name: /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
      Result: true
     Comment: File /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT updated
     Started: 09:48:15.847314
    Duration: 33.988
         SLS: certs
     Changed: diff: New file
              mode: '0644'""",

        ]

        for call in m_print.call_args_list:
            assert_expect([call], next(iter(expected)))
            expected.pop(0)
        assert not expected

    def test_do_system_bootstrap(self, shell):
        """
        Test do_system_bootstrap
        """

        shell.help_system_bootstrap = MagicMock()
        shell.client.system.bootstrap = MagicMock()
        shell.client.system.bootstrapWithPrivateSshKey = MagicMock()

        spacecmd.system.do_system_bootstrap(shell, "-H uyuni.example.com -u admin -P secret")

        assert shell.client.system.bootstrap.called
        assert not shell.client.system.bootstrapWithPrivateSshKey.called
        assert_args_expect(shell.client.system.bootstrap.call_args_list,
                           [((shell.session, 'uyuni.example.com', 22, 'admin', 'secret', '', False), {})])

    def test_do_system_bootstrap_with_activation_key(self, shell):
        """
        Test do_system_bootstrap
        """

        shell.help_system_bootstrap = MagicMock()
        shell.client.system.bootstrap = MagicMock()
        shell.client.system.bootstrapWithPrivateSshKey = MagicMock()

        spacecmd.system.do_system_bootstrap(shell, "-H uyuni.example.com -u admin -P secret -a 1-akey")

        assert shell.client.system.bootstrap.called
        assert not shell.client.system.bootstrapWithPrivateSshKey.called
        assert_args_expect(shell.client.system.bootstrap.call_args_list,
                           [((shell.session, 'uyuni.example.com', 22, 'admin', 'secret', '1-akey', False), {})])

    def test_do_system_bootstrap_with_reactivation(self, shell):
        """
        Test do_system_bootstrap
        """

        shell.help_system_bootstrap = MagicMock()
        shell.client.system.bootstrap = MagicMock()
        shell.client.system.bootstrapWithPrivateSshKey = MagicMock()

        spacecmd.system.do_system_bootstrap(shell, "-H uyuni.example.com -u admin -P secret -r 1-re-key")

        assert shell.client.system.bootstrap.called
        assert not shell.client.system.bootstrapWithPrivateSshKey.called
        assert_args_expect(shell.client.system.bootstrap.call_args_list,
                           [((shell.session, 'uyuni.example.com', 22, 'admin', 'secret', '', '1-re-key', False), {})])

    def test_do_system_bootstrap_saltssh(self, shell):
        """
        Test do_system_bootstrap
        """

        shell.help_system_bootstrap = MagicMock()
        shell.client.system.bootstrap = MagicMock()
        shell.client.system.bootstrapWithPrivateSshKey = MagicMock()

        spacecmd.system.do_system_bootstrap(shell, "-H uyuni.example.com -u admin -P secret -a 1-sshkey --saltssh")

        assert shell.client.system.bootstrap.called
        assert not shell.client.system.bootstrapWithPrivateSshKey.called
        assert_args_expect(shell.client.system.bootstrap.call_args_list,
                           [((shell.session, 'uyuni.example.com', 22, 'admin', 'secret', '1-sshkey', True), {})])


    def test_do_system_bootstrap_proxy(self, shell):
        """
        Test do_system_bootstrap
        """

        shell.help_system_bootstrap = MagicMock()
        shell.client.system.bootstrap = MagicMock()
        shell.client.system.bootstrapWithPrivateSshKey = MagicMock()

        spacecmd.system.do_system_bootstrap(shell, "-H uyuni.example.com -u admin -P secret --proxyid 1000010042")

        assert shell.client.system.bootstrap.called
        assert not shell.client.system.bootstrapWithPrivateSshKey.called
        assert_args_expect(shell.client.system.bootstrap.call_args_list,
                           [((shell.session, 'uyuni.example.com', 22, 'admin', 'secret', '', 1000010042, False), {})])


    def test_do_system_bootstrap_all(self, shell):
        """
        Test do_system_bootstrap
        """

        shell.help_system_bootstrap = MagicMock()
        shell.client.system.bootstrap = MagicMock()
        shell.client.system.bootstrapWithPrivateSshKey = MagicMock()

        spacecmd.system.do_system_bootstrap(shell, "-H uyuni.example.com -u admin -P secret -a 1-sshkey -r 1-re-key --proxyid 1000010042 --saltssh")

        assert shell.client.system.bootstrap.called
        assert not shell.client.system.bootstrapWithPrivateSshKey.called
        assert_args_expect(shell.client.system.bootstrap.call_args_list,
                           [((shell.session, 'uyuni.example.com', 22, 'admin', 'secret', '1-sshkey', '1-re-key', 1000010042, True), {})])


    def test_do_system_bootstrap_sshprivkey(self, shell):
        """
        Test do_system_bootstrap
        """

        shell.help_system_bootstrap = MagicMock()
        shell.client.system.bootstrap = MagicMock()
        shell.client.system.bootstrapWithPrivateSshKey = MagicMock()

        with patch("spacecmd.system.open", new_callable=mock_open, read_data="private_ssh_key") as opn:
            spacecmd.system.do_system_bootstrap(shell, "-H uyuni.example.com -u admin -k /tmp/ssh_priv_key")

        assert not shell.client.system.bootstrap.called
        assert shell.client.system.bootstrapWithPrivateSshKey.called
        assert_args_expect(shell.client.system.bootstrapWithPrivateSshKey.call_args_list,
                           [((shell.session, 'uyuni.example.com', 22, 'admin', 'private_ssh_key', '', '', False), {})])


    def test_do_system_bootstrap_sshprivkey_password(self, shell):
        """
        Test do_system_bootstrap
        """

        shell.help_system_bootstrap = MagicMock()
        shell.client.system.bootstrap = MagicMock()
        shell.client.system.bootstrapWithPrivateSshKey = MagicMock()

        with patch("spacecmd.system.open", new_callable=mock_open, read_data="private_ssh_key") as opn:
            spacecmd.system.do_system_bootstrap(shell, "-H uyuni.example.com -u admin -k /tmp/ssh_priv_key -S key_secret")

        assert not shell.client.system.bootstrap.called
        assert shell.client.system.bootstrapWithPrivateSshKey.called
        assert_args_expect(shell.client.system.bootstrapWithPrivateSshKey.call_args_list,
                           [((shell.session, 'uyuni.example.com', 22, 'admin', 'private_ssh_key', 'key_secret', '', False), {})])

    def test_do_system_bootstrap_sshprivkey_activationkey(self, shell):
        """
        Test do_system_bootstrap
        """

        shell.help_system_bootstrap = MagicMock()
        shell.client.system.bootstrap = MagicMock()
        shell.client.system.bootstrapWithPrivateSshKey = MagicMock()

        with patch("spacecmd.system.open", new_callable=mock_open, read_data="private_ssh_key") as opn:
            spacecmd.system.do_system_bootstrap(shell, "-H uyuni.example.com -u admin -k /tmp/ssh_priv_key -a 1-akey")

        assert not shell.client.system.bootstrap.called
        assert shell.client.system.bootstrapWithPrivateSshKey.called
        assert_args_expect(shell.client.system.bootstrapWithPrivateSshKey.call_args_list,
                           [((shell.session, 'uyuni.example.com', 22, 'admin', 'private_ssh_key', '', '1-akey', False), {})])

    def test_do_system_bootstrap_sshprivkey_reactivationkey(self, shell):
        """
        Test do_system_bootstrap
        """

        shell.help_system_bootstrap = MagicMock()
        shell.client.system.bootstrap = MagicMock()
        shell.client.system.bootstrapWithPrivateSshKey = MagicMock()

        with patch("spacecmd.system.open", new_callable=mock_open, read_data="private_ssh_key") as opn:
            spacecmd.system.do_system_bootstrap(shell, "-H uyuni.example.com -u admin -k /tmp/ssh_priv_key -r 1-re-key")

        assert not shell.client.system.bootstrap.called
        assert shell.client.system.bootstrapWithPrivateSshKey.called
        assert_args_expect(shell.client.system.bootstrapWithPrivateSshKey.call_args_list,
                           [((shell.session, 'uyuni.example.com', 22, 'admin', 'private_ssh_key', '', '', '1-re-key', False), {})])


    def test_do_system_bootstrap_sshprivkey_all(self, shell):
        """
        Test do_system_bootstrap
        """

        shell.help_system_bootstrap = MagicMock()
        shell.client.system.bootstrap = MagicMock()
        shell.client.system.bootstrapWithPrivateSshKey = MagicMock()

        with patch("spacecmd.system.open", new_callable=mock_open, read_data="private_ssh_key") as opn:
            spacecmd.system.do_system_bootstrap(shell, "-H uyuni.example.com -u admin -k /tmp/ssh_priv_key -S key_secret -a 1-akey -r 1-re-key --proxyid 1000010042 --saltssh")

        assert not shell.client.system.bootstrap.called
        assert shell.client.system.bootstrapWithPrivateSshKey.called
        assert_args_expect(shell.client.system.bootstrapWithPrivateSshKey.call_args_list,
                           [((shell.session, 'uyuni.example.com', 22, 'admin', 'private_ssh_key', 'key_secret', '1-akey', '1-re-key', 1000010042, True), {})])
