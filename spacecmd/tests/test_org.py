# coding: utf-8
"""
Test suite for "org" plugin
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.org


class TestSCOrg:
    """
    Test suite for package module.
    """
    def test_org_create_interactive_password_mistmatch(self, shell):
        """
        Test do_org_create interactive, password mistmatch.

            :param self:
            :param shell:
        """

        shell.user_confirm = MagicMock(return_value=False)
        shell.client.org.create = MagicMock()
        prompt_user = MagicMock(side_effect=[
            "Stark Industries", "ironman", "Dr.", "Tony", "Stark", "t.stark@si.biz",
        ])
        mgetpass = MagicMock(side_effect=[
            "123", "3456", "donotmatch", "dontmatch", "foo", "foo"
        ])
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.prompt_user", prompt_user) as pmu, \
            patch("spacecmd.org.getpass", mgetpass) as gtps, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_create(shell, "")

        assert not mprint.called
        assert shell.user_confirm.called
        assert shell.client.org.create.called
        assert prompt_user.called
        assert logger.warning.called

        expectations = [
            (('Organization Name:',), {"noblank": True}),
            (('Username:',), {"noblank": True}),
            (('Prefix (Dr., Mr., Miss, Mrs., Ms.):',), {"noblank": True}),
            (('First Name:',), {"noblank": True}),
            (('Last Name:',), {"noblank": True}),
            (('Email:',), {"noblank": True})
        ]

        for call in prompt_user.call_args_list:
            args, kw = call
            e_args, e_kw = next(iter(expectations))
            assert args == e_args
            assert e_kw == kw
            expectations.pop(0)
        assert not expectations

        assert_list_args_expect(logger.warning.call_args_list,
                                ["Password must be at least 5 characters",
                                 "Passwords don't match", ])

    def test_org_delete_noargs(self, shell):
        """
        Test do_org_delete without arguments.

            :param self:
            :param shell:
        """
        shell.help_org_delete = MagicMock()
        shell.get_org_id = MagicMock()
        shell.client.org.delete = MagicMock()

        spacecmd.org.do_org_delete(shell, "")

        assert not shell.get_org_id.called
        assert not shell.client.org.delete.called
        assert shell.help_org_delete.called

    def test_org_delete_no_org_found(self, shell):
        """
        Test do_org_delete org not found (None).

            :param self:
            :param shell:
        """
        shell.help_org_delete = MagicMock()
        shell.get_org_id = MagicMock(return_value=None)
        shell.client.org.delete = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_delete(shell, "ACME-Enterprises")

        assert not shell.client.org.delete.called
        assert not shell.help_org_delete.called
        assert shell.get_org_id.called
        assert logger.warning.called
        assert mprint.called

        assert_expect(mprint.call_args_list,
                      "Organisation 'ACME-Enterprises' was not found")
        expectations = [
            ('No organisation found for the name %s', 'ACME-Enterprises')
        ]
        for call in logger.warning.call_args_list:
            args, kw = call
            assert args == next(iter(expectations))
            expectations.pop(0)
        assert not expectations

    def test_org_delete_no_confirm(self, shell):
        """
        Test do_org_delete org not confirmed

            :param self:
            :param shell:
        """
        shell.help_org_delete = MagicMock()
        shell.get_org_id = MagicMock(return_value=1)
        shell.client.org.delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=False)

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_delete(shell, "ACME-Enterprises")

        assert not shell.client.org.delete.called
        assert not shell.help_org_delete.called
        assert not logger.warning.called
        assert not mprint.called
        assert shell.get_org_id.called
        assert shell.user_confirm.called

    def test_org_delete_confirm(self, shell):
        """
        Test do_org_delete org confirmed

            :param self:
            :param shell:
        """
        shell.help_org_delete = MagicMock()
        shell.get_org_id = MagicMock(return_value=1)
        shell.client.org.delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_delete(shell, "ACME-Enterprises")

        assert not shell.help_org_delete.called
        assert not logger.warning.called
        assert not mprint.called
        assert shell.get_org_id.called
        assert shell.user_confirm.called
        assert shell.client.org.delete.called

    def test_org_rename_noargs(self, shell):
        """
        Test do_org_rename without arguments.

            :param self:
            :param shell:
        """
        shell.help_org_rename = MagicMock()
        shell.get_org_id = MagicMock()
        shell.client.org.updateName = MagicMock()

        spacecmd.org.do_org_rename(shell, "")

        assert not shell.get_org_id.called
        assert not shell.client.org.updateName.called
        assert shell.help_org_rename.called

    def test_org_rename_no_org_found(self, shell):
        """
        Test do_org_rename org not found (None).

            :param self:
            :param shell:
        """
        shell.help_org_rename = MagicMock()
        shell.get_org_id = MagicMock(return_value=None)
        shell.client.org.updateName = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_rename(shell, "ACME-Enterprises Big-Stuff")

        assert not shell.client.org.updateName.called
        assert not shell.help_org_rename.called
        assert shell.get_org_id.called
        assert logger.warning.called
        assert mprint.called

        assert_expect(mprint.call_args_list,
                      "Organisation 'ACME-Enterprises' was not found")
        expectations = [
            ('No organisation found for the name %s', 'ACME-Enterprises')
        ]
        for call in logger.warning.call_args_list:
            args, kw = call
            assert args == next(iter(expectations))
            expectations.pop(0)
        assert not expectations

    def test_org_rename(self, shell):
        """
        Test do_org_rename org

            :param self:
            :param shell:
        """
        shell.help_org_delete = MagicMock()
        shell.get_org_id = MagicMock(return_value=1)
        shell.client.org.delete = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_rename(shell, "ACME-Enterprises Big-Stuff")

        assert not shell.help_org_delete.called
        assert not logger.warning.called
        assert not mprint.called
        assert shell.get_org_id.called
        assert shell.client.org.updateName.called

    def test_org_addtrust_noarg(self, shell):
        """
        Test do_org_addtrust without arguments

        :param shell:
        :return:
        """
        shell.help_org_addtrust = MagicMock()
        shell.get_org_id = MagicMock()
        shell.client.org.trusts.addTrust = MagicMock()

        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_addtrust(shell, "")

        assert not shell.get_org_id.called
        assert not shell.client.org.trusts.addTrust.called
        assert shell.help_org_addtrust.called

    def test_org_addtrust_no_src_org(self, shell):
        """
        Test do_org_addtrust, source org not found

        :param shell:
        :return:
        """
        shell.help_org_addtrust = MagicMock()
        shell.get_org_id = MagicMock(side_effect=[None, 0])
        shell.client.org.trusts.addTrust = MagicMock()

        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_addtrust(shell, "trust me")

        assert not shell.client.org.trusts.addTrust.called
        assert not shell.help_org_addtrust.called
        assert shell.get_org_id.called
        assert mprint.called
        assert logger.warning.called
        assert_expect(mprint.call_args_list, "Organisation 'trust' was not found")
        assert_args_expect(logger.warning.call_args_list,
                           [
                               (('No organisation found for the name %s', 'trust',), {})
                           ])

    def test_org_addtrust_no_dst_org(self, shell):
        """
        Test do_org_addtrust, destination org not found

        :param shell:
        :return:
        """
        shell.help_org_addtrust = MagicMock()
        shell.get_org_id = MagicMock(side_effect=[0, None])
        shell.client.org.trusts.addTrust = MagicMock()

        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_addtrust(shell, "trust someone")

        assert not shell.client.org.trusts.addTrust.called
        assert not shell.help_org_addtrust.called
        assert shell.get_org_id.called
        assert mprint.called
        assert logger.warning.called
        assert_expect(mprint.call_args_list, "Organisation 'someone' to trust for, was not found")
        assert_args_expect(logger.warning.call_args_list,
                           [
                               (('No trust organisation found for the name %s', 'someone',), {})
                           ])

    def test_org_removetrust_noarg(self, shell):
        """
        Test for do_org_removetrust without arguments.

        :param shell:
        :return:
        """
        shell.help_org_removetrust = MagicMock()
        shell.get_org_id = MagicMock(side_effect=[None, 0])
        shell.client.org.trusts.listSystemsAffected = MagicMock()

        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_removetrust(shell, "")

        assert not shell.client.org.trusts.removeTrust.called
        assert not shell.get_org_id.called
        assert not logger.warning.called
        assert not mprint.called
        assert shell.help_org_removetrust.called

    def test_org_removetrust_no_src(self, shell):
        """
        Test for do_org_removetrust source org not found.

        :param shell:
        :return:
        """
        shell.help_org_removetrust = MagicMock()
        shell.get_org_id = MagicMock(side_effect=[None, 0])
        shell.client.org.trusts.listSystemsAffected = MagicMock()

        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_removetrust(shell, "trust bad-guys")

        assert not shell.client.org.trusts.removeTrust.called
        assert not shell.help_org_removetrust.called
        assert shell.get_org_id.called
        assert logger.warning.called
        assert mprint.called

        assert_expect(mprint.call_args_list, "Organisation 'trust' was not found")
        assert_args_expect(logger.warning.call_args_list,
                           [(('No organisation found for the name %s', 'trust'), {})])

    def test_org_removetrust_no_dst(self, shell):
        """
        Test for do_org_removetrust destination org not found.

        :param shell:
        :return:
        """
        shell.help_org_removetrust = MagicMock()
        shell.get_org_id = MagicMock(side_effect=[0, None])
        shell.client.org.trusts.listSystemsAffected = MagicMock()

        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_removetrust(shell, "trust bad-guys")

        assert not shell.client.org.trusts.removeTrust.called
        assert not shell.help_org_removetrust.called
        assert shell.get_org_id.called
        assert logger.warning.called
        assert mprint.called

        assert_expect(mprint.call_args_list, "Organisation 'bad-guys' to trust for, was not found")
        assert_args_expect(logger.warning.call_args_list,
                           [(('No trust organisation found for the name %s', 'bad-guys'), {})])

    def test_org_trustdetails_noarg(self, shell):
        """
        Test for do_org_trustdetails no arguments.

        :param shell:
        :return:
        """
        shell.help_org_trustdetails = MagicMock()
        shell.get_org_id = MagicMock()
        shell.client.org.trusts.getDetails = MagicMock()
        shell.client.org.trusts.listChannelsConsumed = MagicMock()
        shell.client.org.trusts.listChannelsProvided = MagicMock()

        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_trustdetails(shell, "")

        assert not shell.get_org_id.called
        assert not shell.client.org.trusts.getDetails.called
        assert not shell.client.org.trusts.listChannelsConsumed.called
        assert not shell.client.org.trusts.listChannelsProvided.called
        assert shell.help_org_trustdetails.called

    def test_org_trustdetails_no_org_found(self, shell):
        """
        Test for do_org_trustdetails no org found.

        :param shell:
        :return:
        """
        shell.help_org_trustdetails = MagicMock()
        shell.get_org_id = MagicMock(return_value=None)
        shell.client.org.trusts.getDetails = MagicMock()
        shell.client.org.trusts.listChannelsConsumed = MagicMock()
        shell.client.org.trusts.listChannelsProvided = MagicMock()

        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_trustdetails(shell, "notfound")

        assert not shell.client.org.trusts.getDetails.called
        assert not shell.client.org.trusts.listChannelsConsumed.called
        assert not shell.client.org.trusts.listChannelsProvided.called
        assert not shell.help_org_trustdetails.called
        assert shell.get_org_id.called
        assert mprint.called
        assert logger.warning.called

        assert_expect(mprint.call_args_list,
                      "Trusted organisation 'notfound' was not found")
        assert_args_expect(logger.warning.call_args_list,
                           [(('No trusted organisation found for the name %s',
                              'notfound'), {})])

    def test_org_trustdetails(self, shell):
        """
        Test do_or_trustdetails

        :param shell:
        :return:
        """
        shell.help_org_trustdetails = MagicMock()
        shell.get_org_id = MagicMock(return_value=1)
        shell.client.org.trusts.getDetails = MagicMock(return_value={
            "trusted_since": "Mi 29. Mai 15:02:26 CEST 2019",
            "systems_migrated_from": 3,
            "systems_migrated_to": 8
        })
        shell.client.org.trusts.listChannelsConsumed = MagicMock(return_value=[
            {"name": "base_channel"},
            {"name": "special_channel"},
        ])
        shell.client.org.trusts.listChannelsProvided = MagicMock(return_value=[
            {"name": "base_channel"},
            {"name": "suse_channel"},
            {"name": "rh_channel"},
        ])

        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_trustdetails(shell, "myorg")

        assert not shell.help_org_trustdetails.called
        assert not logger.warning.called
        assert shell.client.org.trusts.getDetails.called
        assert shell.client.org.trusts.listChannelsConsumed.called
        assert shell.client.org.trusts.listChannelsProvided.called
        assert shell.get_org_id.called
        assert mprint.called

        exp = [
           'Trusted Organization:   myorg',
           'Trusted Since:          Mi 29. Mai 15:02:26 CEST 2019',
           'Systems Migrated From:  3', 'Systems Migrated To:    8', '',
           'Channels Consumed', '-----------------',
           'base_channel\nspecial_channel', '',
           'Channels Provided', '-----------------',
            'base_channel\nrh_channel\nsuse_channel'
        ]

        assert_list_args_expect(mprint.call_args_list, exp)

    def test_org_list_noret(self, shell):
        """
        Test do_org_list no data return.

        :param shell:
        :return:
        """
        shell.client.org.listOrgs = MagicMock(return_value=[
            {"name": "suse"},
            {"name": "rh"},
            {"name": "other"},
        ])
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint):
            out = spacecmd.org.do_org_list(shell, "", doreturn=False)

        assert out is None
        assert mprint.called
        assert_expect(mprint.call_args_list, 'other\nrh\nsuse')

    def test_org_list_data_ret(self, shell):
        """
        Test do_org_list with data return.

        :param shell:
        :return:
        """
        shell.client.org.listOrgs = MagicMock(return_value=[
            {"name": "suse"},
            {"name": "rh"},
            {"name": "other"},
        ])
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint):
            out = spacecmd.org.do_org_list(shell, "", doreturn=True)

        assert out is not None
        assert out == ["suse", "rh", "other"]
        assert not mprint.called

    def test_org_listtrusts_noargs(self, shell):
        """
        Test do_org_listtrusts without arguments.

        :param shell:
        :return:
        """
        shell.help_org_listtrusts = MagicMock()
        shell.get_org_id = MagicMock()
        shell.client.org.trusts.listTrusts = MagicMock()

        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_listtrusts(shell, "")

        assert not shell.get_org_id.called
        assert not shell.client.org.trusts.listTrusts.called
        assert shell.help_org_listtrusts.called

    def test_org_listtrusts_no_org(self, shell):
        """
        Test do_org_listtrusts org not found.

        :param shell:
        :return:
        """
        shell.help_org_listtrusts = MagicMock()
        shell.get_org_id = MagicMock(return_value=None)
        shell.client.org.trusts.listTrusts = MagicMock()

        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_listtrusts(shell, "notfound")

        assert not shell.client.org.trusts.listTrusts.called
        assert not shell.help_org_listtrusts.called
        assert shell.get_org_id.called
        assert mprint.called
        assert logger.warning.called

        assert_expect(mprint.call_args_list, "Organisation 'notfound' was not found")
        assert_args_expect(logger.warning.call_args_list,
                           [(('No organisation found for the name %s', 'notfound'), {})])

    def test_org_listtrusts_no_trusts(self, shell):
        """
        Test do_org_listtrusts trust orgs were not found.

        :param shell:
        :return:
        """
        shell.help_org_listtrusts = MagicMock()
        shell.get_org_id = MagicMock(return_value=1)
        shell.client.org.trusts.listTrusts = MagicMock(return_value=[])

        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_listtrusts(shell, "notfound")

        assert not shell.help_org_listtrusts.called
        assert shell.client.org.trusts.listTrusts.called
        assert shell.get_org_id.called
        assert mprint.called
        assert logger.warning.called

        assert_expect(mprint.call_args_list, "No trust organisation has been found")
        assert_expect(logger.warning.call_args_list, "No trust organisation has been found")

    def test_org_listtrusts(self, shell):
        """
        Test do_org_listtrusts output.

        :param shell:
        :return:
        """
        shell.help_org_listtrusts = MagicMock()
        shell.get_org_id = MagicMock(return_value=1)
        shell.client.org.trusts.listTrusts = MagicMock(return_value=[
            {"orgName": "suse", "trustEnabled": True},
            {"orgName": "west", "trustEnabled": True},
            {"orgName": "acme", "trustEnabled": True},
            {"orgName": "ubuntu", "trustEnabled": False},
        ])

        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_listtrusts(shell, "notfound")

        assert not logger.warning.called
        assert not shell.help_org_listtrusts.called
        assert shell.client.org.trusts.listTrusts.called
        assert shell.get_org_id.called
        assert mprint.called

        assert_list_args_expect(mprint.call_args_list, ["acme", "suse", "west"])

    def test_org_listusers_noargs(self, shell):
        """
        Test do_org_listusers without arguments.

        :param shell:
        :return:
        """
        shell.help_org_listusers = MagicMock()
        shell.client.org.listUsers = MagicMock()
        shell.get_org_id = MagicMock()
        logger = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.org.print", mprint) as prn, \
            patch("spacecmd.org.logging", logger) as lgr:
            spacecmd.org.do_org_listusers(shell, "")

        assert not shell.client.org.listUsers.called
        assert not shell.get_org_id.called
        assert not mprint.called
        assert not logger.warning.called
        assert shell.help_org_listusers.called


