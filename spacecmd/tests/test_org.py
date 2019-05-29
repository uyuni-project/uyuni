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

