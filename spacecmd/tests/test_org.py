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
