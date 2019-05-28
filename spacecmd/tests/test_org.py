# coding: utf-8
"""
Test suite for "org" plugin
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect, assert_list_args_expect
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
