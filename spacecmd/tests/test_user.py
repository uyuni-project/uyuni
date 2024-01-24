# coding: utf-8
"""
Test case for spacecmd.user module
"""

from unittest.mock import MagicMock, patch

# pylint: disable-next=unused-import
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.user
from xmlrpc import client as xmlrpclib


class TestSCUser:
    """
    Test suite for "user" module.
    """

    # pylint: disable-next=redefined-outer-name
    def test_user_create_interactive(self, shell):
        """
        Test do_user_create interactive mode.

        :param shell:
        :return:
        """
        shell.client.user.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=1)
        logger = MagicMock()
        getps = MagicMock(return_value="1234567890")
        prompter = MagicMock(
            side_effect=["lksw", "Luke", "Skywalker", "l.skywalker@suse.com"]
        )
        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.logging", logger) as lgr, patch(
            "spacecmd.user.prompt_user",
            prompter
            # pylint: disable-next=unused-variable
        ) as pmt, patch("spacecmd.user.getpass", getps) as gpw:
            spacecmd.user.do_user_create(shell, "")

        assert shell.client.user.create.called
        assert not logger.warning.called
        assert_args_expect(
            shell.client.user.create.call_args_list,
            [
                (
                    (
                        shell.session,
                        "lksw",
                        "1234567890",
                        "Luke",
                        "Skywalker",
                        "l.skywalker@suse.com",
                        1,
                    ),
                    {},
                )
            ],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_create_args(self, shell):
        """
        Test do_user_create parameters/arguments mode.

        :param shell:
        :return:
        """
        shell.client.user.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        logger = MagicMock()
        getps = MagicMock(return_value="1234567890")
        prompter = MagicMock(side_effect=Exception("Should not happen"))
        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.logging", logger) as lgr, patch(
            "spacecmd.user.prompt_user",
            prompter
            # pylint: disable-next=unused-variable
        ) as pmt, patch("spacecmd.user.getpass", getps) as gpw:
            spacecmd.user.do_user_create(
                shell,
                "-u lksw -f Luke -l Skywalker " "-e l.skywalker@suse.com -p 1234567890",
            )

        assert shell.client.user.create.called
        assert not logger.error.called
        assert not logger.warning.called
        assert_args_expect(
            shell.client.user.create.call_args_list,
            [
                (
                    (
                        shell.session,
                        "lksw",
                        "1234567890",
                        "Luke",
                        "Skywalker",
                        "l.skywalker@suse.com",
                        0,
                    ),
                    {},
                )
            ],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_create_no_username(self, shell):
        """
        Test do_user_create, missing user name

        :param shell:
        :return:
        """
        shell.client.user.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        logger = MagicMock()
        getps = MagicMock(return_value="1234567890")
        prompter = MagicMock(side_effect=Exception("Should not happen"))
        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.logging", logger) as lgr, patch(
            "spacecmd.user.prompt_user",
            prompter
            # pylint: disable-next=unused-variable
        ) as pmt, patch("spacecmd.user.getpass", getps) as gpw:
            spacecmd.user.do_user_create(
                shell, "-f Luke -l Skywalker " "-e l.skywalker@suse.com -p 1234567890"
            )

        assert not shell.client.user.create.called
        assert not logger.warning.called
        assert logger.error.called
        assert_expect(logger.error.call_args_list, "A username is required")

    # pylint: disable-next=redefined-outer-name
    def test_user_create_no_first_name(self, shell):
        """
        Test do_user_create, missing first name

        :param shell:
        :return:
        """
        shell.client.user.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        logger = MagicMock()
        getps = MagicMock(return_value="1234567890")
        prompter = MagicMock(side_effect=Exception("Should not happen"))
        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.logging", logger) as lgr, patch(
            "spacecmd.user.prompt_user",
            prompter
            # pylint: disable-next=unused-variable
        ) as pmt, patch("spacecmd.user.getpass", getps) as gpw:
            spacecmd.user.do_user_create(
                shell, "-u lksw -l Skywalker " "-e l.skywalker@suse.com -p 1234567890"
            )

        assert not shell.client.user.create.called
        assert not logger.warning.called
        assert logger.error.called
        assert_expect(logger.error.call_args_list, "A first name is required")

    # pylint: disable-next=redefined-outer-name
    def test_user_create_no_last_name(self, shell):
        """
        Test do_user_create, missing last name

        :param shell:
        :return:
        """
        shell.client.user.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        logger = MagicMock()
        getps = MagicMock(return_value="1234567890")
        prompter = MagicMock(side_effect=Exception("Should not happen"))
        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.logging", logger) as lgr, patch(
            "spacecmd.user.prompt_user",
            prompter
            # pylint: disable-next=unused-variable
        ) as pmt, patch("spacecmd.user.getpass", getps) as gpw:
            spacecmd.user.do_user_create(
                shell, "-u lksw -f Luke " "-e l.skywalker@suse.com -p 1234567890"
            )

        assert not shell.client.user.create.called
        assert not logger.warning.called
        assert logger.error.called
        assert_expect(logger.error.call_args_list, "A last name is required")

    # pylint: disable-next=redefined-outer-name
    def test_user_create_no_email(self, shell):
        """
        Test do_user_create, missing email removeress

        :param shell:
        :return:
        """
        shell.client.user.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        logger = MagicMock()
        getps = MagicMock(return_value="1234567890")
        prompter = MagicMock(side_effect=Exception("Should not happen"))
        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.logging", logger) as lgr, patch(
            "spacecmd.user.prompt_user",
            prompter
            # pylint: disable-next=unused-variable
        ) as pmt, patch("spacecmd.user.getpass", getps) as gpw:
            spacecmd.user.do_user_create(
                shell, "-u lksw -f Luke -l Skywalker " "-p 1234567890"
            )

        assert not shell.client.user.create.called
        assert not logger.warning.called
        assert logger.error.called
        assert_expect(logger.error.call_args_list, "An email address is required")

    # pylint: disable-next=redefined-outer-name
    def test_user_create_no_auth(self, shell):
        """
        Test do_user_create, missing authentication

        :param shell:
        :return:
        """
        shell.client.user.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        logger = MagicMock()
        getps = MagicMock(return_value="1234567890")
        prompter = MagicMock(side_effect=Exception("Should not happen"))
        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.logging", logger) as lgr, patch(
            "spacecmd.user.prompt_user",
            prompter
            # pylint: disable-next=unused-variable
        ) as pmt, patch("spacecmd.user.getpass", getps) as gpw:
            spacecmd.user.do_user_create(
                shell, "-u lksw -f Luke -l Skywalker " "-e l.skywalker@suse.com"
            )

        assert not shell.client.user.create.called
        assert not logger.warning.called
        assert logger.error.called
        assert_expect(logger.error.call_args_list, "A password is required")

    # pylint: disable-next=redefined-outer-name
    def test_user_create_no_password_with_pam(self, shell):
        """
        Test do_user_create, password should be ignored if user opted for PAM

        :param shell:
        :return:
        """
        shell.client.user.create = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        logger = MagicMock()
        getps = MagicMock(return_value="1234567890")
        prompter = MagicMock(side_effect=Exception("Should not happen"))
        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.logging", logger) as lgr, patch(
            "spacecmd.user.prompt_user",
            prompter
            # pylint: disable-next=unused-variable
        ) as pmt, patch("spacecmd.user.getpass", getps) as gpw:
            spacecmd.user.do_user_create(
                shell,
                "-u lksw -f Luke -l Skywalker --pam "
                "-e l.skywalker@suse.com -p 123123123",
            )

        assert not logger.error.called
        assert shell.client.user.create.called
        assert logger.warning.called
        assert_expect(
            logger.warning.call_args_list, "Note: password was ignored due to PAM mode"
        )
        assert_args_expect(
            shell.client.user.create.call_args_list,
            [
                (
                    (
                        shell.session,
                        "lksw",
                        "",
                        "Luke",
                        "Skywalker",
                        "l.skywalker@suse.com",
                        1,
                    ),
                    {},
                )
            ],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_delete_noargs(self, shell):
        """
        Test do_user_delete, no arguments
        :param shell:
        :return:
        """
        shell.client.user.delete = MagicMock()
        shell.help_user_delete = MagicMock()
        shell.user_confirm = MagicMock()

        spacecmd.user.do_user_delete(shell, "")

        assert not shell.client.user.delete.called
        assert not shell.user_confirm.called
        assert shell.help_user_delete.called

    # pylint: disable-next=redefined-outer-name
    def test_user_delete_non_interactive(self, shell):
        """
        Test do_user_delete, non-interactive mode.

        :param shell:
        :return:
        """
        shell.client.user.delete = MagicMock()
        shell.help_user_delete = MagicMock()
        shell.options.yes = True
        shell.user_confirm = MagicMock(return_value=False)

        spacecmd.user.do_user_delete(shell, "pointyhaired")

        assert not shell.help_user_delete.called
        assert not shell.user_confirm.called
        assert shell.client.user.delete.called

    # pylint: disable-next=redefined-outer-name
    def test_user_delete_interactive(self, shell):
        """
        Test do_user_delete, interactive mode.

        :param shell:
        :return:
        """
        shell.client.user.delete = MagicMock()
        shell.help_user_delete = MagicMock()
        shell.options.yes = False
        shell.user_confirm = MagicMock(return_value=True)

        spacecmd.user.do_user_delete(shell, "pointyhaired")

        assert not shell.help_user_delete.called
        assert shell.user_confirm.called
        assert shell.client.user.delete.called

    # pylint: disable-next=redefined-outer-name
    def test_user_disable_noargs(self, shell):
        """
        Test do_user_disable, no arguments
        :param shell:
        :return:
        """
        shell.client.user.disable = MagicMock()
        shell.help_user_disable = MagicMock()

        spacecmd.user.do_user_disable(shell, "")

        assert not shell.client.user.disable.called
        assert shell.help_user_disable.called

    # pylint: disable-next=redefined-outer-name
    def test_user_disable_too_much_arguments(self, shell):
        """
        Test do_user_disable, too much arguments
        :param shell:
        :return:
        """
        shell.client.user.disable = MagicMock()
        shell.help_user_disable = MagicMock()

        spacecmd.user.do_user_disable(shell, "pointyhaired someone-else")

        assert not shell.client.user.disable.called
        assert shell.help_user_disable.called

    # pylint: disable-next=redefined-outer-name
    def test_user_disable(self, shell):
        """
        Test do_user_disable, username
        :param shell:
        :return:
        """
        shell.client.user.disable = MagicMock()
        shell.help_user_disable = MagicMock()

        spacecmd.user.do_user_disable(shell, "pointyhaired")

        assert not shell.help_user_disable.called
        assert shell.client.user.disable.called

    # pylint: disable-next=redefined-outer-name
    def test_user_enable_noargs(self, shell):
        """
        Test do_user_enable, no arguments
        :param shell:
        :return:
        """
        shell.client.user.enable = MagicMock()
        shell.help_user_enable = MagicMock()

        spacecmd.user.do_user_enable(shell, "")

        assert not shell.client.user.enable.called
        assert shell.help_user_enable.called

    # pylint: disable-next=redefined-outer-name
    def test_user_enable_too_much_arguments(self, shell):
        """
        Test do_user_enable, too much arguments
        :param shell:
        :return:
        """
        shell.client.user.enable = MagicMock()
        shell.help_user_enable = MagicMock()

        spacecmd.user.do_user_enable(shell, "pointyhaired someone-else")

        assert not shell.client.user.enable.called
        assert shell.help_user_enable.called

    # pylint: disable-next=redefined-outer-name
    def test_user_enable(self, shell):
        """
        Test do_user_enable, username
        :param shell:
        :return:
        """
        shell.client.user.enable = MagicMock()
        shell.help_user_enable = MagicMock()

        spacecmd.user.do_user_enable(shell, "pointyhaired")

        assert not shell.help_user_enable.called
        assert shell.client.user.enable.called

    # pylint: disable-next=redefined-outer-name
    def test_user_list_no_data_no_users(self, shell):
        """
        Test do_user_list, no data return, no users.

        :param shell:
        :return:
        """
        shell.client.user.listUsers = MagicMock(return_value=[])
        mprint = MagicMock()

        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.print", mprint) as prt:
            out = spacecmd.user.do_user_list(shell, "")

        assert not mprint.called
        assert out is None

    # pylint: disable-next=redefined-outer-name
    def test_user_list_get_data_no_users(self, shell):
        """
        Test do_user_list, data return, no users.

        :param shell:
        :return:
        """
        shell.client.user.listUsers = MagicMock(return_value=[])
        mprint = MagicMock()

        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.print", mprint) as prt:
            out = spacecmd.user.do_user_list(shell, "", doreturn=True)

        assert not mprint.called
        assert out == []

    # pylint: disable-next=redefined-outer-name
    def test_user_list_no_data_with_users(self, shell):
        """
        Test do_user_list, no data return, users found.

        :param shell:
        :return:
        """
        shell.client.user.listUsers = MagicMock(
            return_value=[
                {"login": "pointyhaired"},
                {"login": "someone-else"},
            ]
        )
        mprint = MagicMock()

        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.print", mprint) as prt:
            out = spacecmd.user.do_user_list(shell, "")

        assert mprint.called
        assert out is None
        assert_expect(mprint.call_args_list, "pointyhaired\nsomeone-else")

    # pylint: disable-next=redefined-outer-name
    def test_user_list_get_data_with_users(self, shell):
        """
        Test do_user_list, data return, users found.

        :param shell:
        :return:
        """
        shell.client.user.listUsers = MagicMock(
            return_value=[
                {"login": "pointyhaired"},
                {"login": "someone-else"},
            ]
        )
        mprint = MagicMock()

        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.print", mprint) as prt:
            out = spacecmd.user.do_user_list(shell, "", doreturn=True)

        assert not mprint.called
        assert out == ["pointyhaired", "someone-else"]

    # pylint: disable-next=redefined-outer-name
    def test_user_listavailableroles_no_data_no_roles(self, shell):
        """
        test do_user_listavailableroles, no data return, no roles found.

        :param shell:
        :return:
        """
        shell.client.user.listAssignableRoles = MagicMock(return_value=[])
        mprint = MagicMock()
        logger = MagicMock()

        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.print", mprint) as prt, patch(
            "spacecmd.user.logging",
            logger
            # pylint: disable-next=unused-variable
        ) as lgr:
            out = spacecmd.user.do_user_listavailableroles(shell, "")
        assert out is None
        assert not mprint.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list, "No roles has been found")

    # pylint: disable-next=redefined-outer-name
    def test_user_listavailableroles_no_data_with_roles(self, shell):
        """
        test do_user_listavailableroles, no data return, roles found.

        :param shell:
        :return:
        """
        shell.client.user.listAssignableRoles = MagicMock(
            return_value=["bofh", "coffee"]
        )
        mprint = MagicMock()
        logger = MagicMock()

        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.print", mprint) as prt, patch(
            "spacecmd.user.logging",
            logger
            # pylint: disable-next=unused-variable
        ) as lgr:
            out = spacecmd.user.do_user_listavailableroles(shell, "")
        assert out is None
        assert not logger.error.called
        assert mprint.called

        assert_expect(mprint.call_args_list, "bofh\ncoffee")

    # pylint: disable-next=redefined-outer-name
    def test_user_listavailableroles_with_data_no_roles(self, shell):
        """
        test do_user_listavailableroles, no data return, no roles found.

        :param shell:
        :return:
        """
        shell.client.user.listAssignableRoles = MagicMock(return_value=[])
        mprint = MagicMock()
        logger = MagicMock()

        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.print", mprint) as prt, patch(
            "spacecmd.user.logging",
            logger
            # pylint: disable-next=unused-variable
        ) as lgr:
            out = spacecmd.user.do_user_listavailableroles(shell, "", doreturn=True)
        assert out is not None
        assert not mprint.called
        assert not logger.error.called
        assert out == []

    # pylint: disable-next=redefined-outer-name
    def test_user_listavailableroles_with_data_with_roles(self, shell):
        """
        test do_user_listavailableroles, no data return, roles found.

        :param shell:
        :return:
        """
        shell.client.user.listAssignableRoles = MagicMock(
            return_value=["bofh", "coffee"]
        )
        mprint = MagicMock()
        logger = MagicMock()

        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.print", mprint) as prt, patch(
            "spacecmd.user.logging",
            logger
            # pylint: disable-next=unused-variable
        ) as lgr:
            out = spacecmd.user.do_user_listavailableroles(shell, "", doreturn=True)
        assert out is not None
        assert not logger.error.called
        assert not mprint.called
        assert out == ["bofh", "coffee"]

    # pylint: disable-next=redefined-outer-name
    def test_user_addrole_noargs(self, shell):
        """
        Test do_user_addrole, no arguments
        :param shell:
        :return:
        """
        shell.client.user.addRole = MagicMock()
        shell.help_user_addrole = MagicMock()

        spacecmd.user.do_user_addrole(shell, "")

        assert not shell.client.user.addRole.called
        assert shell.help_user_addrole.called

    # pylint: disable-next=redefined-outer-name
    def test_user_addrole(self, shell):
        """
        Test do_user_addrole, with correct arguments of user and role
        :param shell:
        :return:
        """
        shell.client.user.addRole = MagicMock()
        shell.help_user_addrole = MagicMock()

        spacecmd.user.do_user_addrole(shell, "bofh coffee")

        assert not shell.help_user_addrole.called
        assert shell.client.user.addRole.called
        assert_args_expect(
            shell.client.user.addRole.call_args_list,
            [((shell.session, "bofh", "coffee"), {})],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_removerole_noargs(self, shell):
        """
        Test do_user_removerole, no arguments
        :param shell:
        :return:
        """
        shell.client.user.removeRole = MagicMock()
        shell.help_user_removerole = MagicMock()

        spacecmd.user.do_user_removerole(shell, "")

        assert not shell.client.user.removeRole.called
        assert shell.help_user_removerole.called

    # pylint: disable-next=redefined-outer-name
    def test_user_removerole(self, shell):
        """
        Test do_user_removerole, with correct arguments of user and role
        :param shell:
        :return:
        """
        shell.client.user.removeRole = MagicMock()
        shell.help_user_removerole = MagicMock()

        spacecmd.user.do_user_removerole(shell, "bofh coffee")

        assert not shell.help_user_removerole.called
        assert shell.client.user.removeRole.called
        assert_args_expect(
            shell.client.user.removeRole.call_args_list,
            [((shell.session, "bofh", "coffee"), {})],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_addgroup_noargs(self, shell):
        """
        Test do_user_addgroup, no arguments
        :param shell:
        :return:
        """
        shell.client.user.addAssignedSystemGroups = MagicMock()
        shell.help_user_addgroup = MagicMock()

        spacecmd.user.do_user_addgroup(shell, "")

        assert not shell.client.user.addAssignedSystemGroups.called
        assert shell.help_user_addgroup.called

    # pylint: disable-next=redefined-outer-name
    def test_user_addgroup(self, shell):
        """
        Test do_user_addgroup, with correct arguments of user and groups
        :param shell:
        :return:
        """
        shell.client.user.addAssignedSystemGroups = MagicMock()
        shell.help_user_addgroup = MagicMock()

        spacecmd.user.do_user_addgroup(shell, "bofh coffee teamaker")

        assert not shell.help_user_addgroup.called
        assert shell.client.user.addAssignedSystemGroups.called
        assert_args_expect(
            shell.client.user.addAssignedSystemGroups.call_args_list,
            [((shell.session, "bofh", ["coffee", "teamaker"], False), {})],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_adddefaultgroup_noargs(self, shell):
        """
        Test do_user_adddefaultgroup, no arguments
        :param shell:
        :return:
        """
        shell.client.user.addDefaultSystemGroups = MagicMock()
        shell.help_user_adddefaultgroup = MagicMock()

        spacecmd.user.do_user_adddefaultgroup(shell, "")

        assert not shell.client.user.addDefaultSystemGroups.called
        assert shell.help_user_adddefaultgroup.called

    # pylint: disable-next=redefined-outer-name
    def test_user_adddefaultgroup(self, shell):
        """
        Test do_user_adddefaultgroup, with correct arguments of user and groups
        :param shell:
        :return:
        """
        shell.client.user.addDefaultSystemGroups = MagicMock()
        shell.help_user_adddefaultgroup = MagicMock()

        spacecmd.user.do_user_adddefaultgroup(shell, "bofh coffee teamaker")

        assert not shell.help_user_adddefaultgroup.called
        assert shell.client.user.addDefaultSystemGroups.called
        assert_args_expect(
            shell.client.user.addDefaultSystemGroups.call_args_list,
            [((shell.session, "bofh", ["coffee", "teamaker"]), {})],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_removegroup_noargs(self, shell):
        """
        Test do_user_removegroup, no arguments
        :param shell:
        :return:
        """
        shell.client.user.removeAssignedSystemGroups = MagicMock()
        shell.help_user_removegroup = MagicMock()

        spacecmd.user.do_user_removegroup(shell, "")

        assert not shell.client.user.removeAssignedSystemGroups.called
        assert shell.help_user_removegroup.called

    # pylint: disable-next=redefined-outer-name
    def test_user_removegroup(self, shell):
        """
        Test do_user_removegroup, with correct arguments of user and groups
        :param shell:
        :return:
        """
        shell.client.user.removeAssignedSystemGroups = MagicMock()
        shell.help_user_removegroup = MagicMock()

        spacecmd.user.do_user_removegroup(shell, "bofh coffee teamaker")

        assert not shell.help_user_removegroup.called
        assert shell.client.user.removeAssignedSystemGroups.called
        assert_args_expect(
            shell.client.user.removeAssignedSystemGroups.call_args_list,
            [((shell.session, "bofh", ["coffee", "teamaker"], True), {})],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_removedefaultgroup_noargs(self, shell):
        """
        Test do_user_removedefaultgroup, no arguments
        :param shell:
        :return:
        """
        shell.client.user.removeDefaultSystemGroups = MagicMock()
        shell.help_user_removedefaultgroup = MagicMock()

        spacecmd.user.do_user_removedefaultgroup(shell, "")

        assert not shell.client.user.removeDefaultSystemGroups.called
        assert shell.help_user_removedefaultgroup.called

    # pylint: disable-next=redefined-outer-name
    def test_user_removedefaultgroup(self, shell):
        """
        Test do_user_removedefaultgroup, with correct arguments of user and groups
        :param shell:
        :return:
        """
        shell.client.user.removeDefaultSystemGroups = MagicMock()
        shell.help_user_removedefaultgroup = MagicMock()

        spacecmd.user.do_user_removedefaultgroup(shell, "bofh coffee teamaker")

        assert not shell.help_user_removedefaultgroup.called
        assert shell.client.user.removeDefaultSystemGroups.called
        assert_args_expect(
            shell.client.user.removeDefaultSystemGroups.call_args_list,
            [((shell.session, "bofh", ["coffee", "teamaker"]), {})],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_setfirstname_noargs(self, shell):
        """
        Test do_user_setfirstname without arguments.

        :param shell:
        :return:
        """

        shell.client.user.setDetails = MagicMock()
        shell.help_user_setfirstname = MagicMock()

        spacecmd.user.do_user_setfirstname(shell, "")

        assert not shell.client.user.setDetails.called
        assert shell.help_user_setfirstname.called

    # pylint: disable-next=redefined-outer-name
    def test_user_setfirstname(self, shell):
        """
        Test do_user_setfirstname with data.

        :param shell:
        :return:
        """

        shell.client.user.setDetails = MagicMock()
        shell.help_user_setfirstname = MagicMock()

        spacecmd.user.do_user_setfirstname(shell, "bofh Operator")

        assert not shell.help_user_setfirstname.called
        assert shell.client.user.setDetails.called

        assert_args_expect(
            shell.client.user.setDetails.call_args_list,
            [((shell.session, "bofh", {"first_name": "Operator"}), {})],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_setlastname_noargs(self, shell):
        """
        Test do_user_setlastname without arguments.

        :param shell:
        :return:
        """

        shell.client.user.setDetails = MagicMock()
        shell.help_user_setlastname = MagicMock()

        spacecmd.user.do_user_setlastname(shell, "")

        assert not shell.client.user.setDetails.called
        assert shell.help_user_setlastname.called

    # pylint: disable-next=redefined-outer-name
    def test_user_setlastname(self, shell):
        """
        Test do_user_setlastname with data.

        :param shell:
        :return:
        """

        shell.client.user.setDetails = MagicMock()
        shell.help_user_setlastname = MagicMock()

        spacecmd.user.do_user_setlastname(shell, "bofh Hell")

        assert not shell.help_user_setlastname.called
        assert shell.client.user.setDetails.called

        assert_args_expect(
            shell.client.user.setDetails.call_args_list,
            [((shell.session, "bofh", {"last_name": "Hell"}), {})],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_setemail_noargs(self, shell):
        """
        Test do_user_setemail without arguments.

        :param shell:
        :return:
        """

        shell.client.user.setDetails = MagicMock()
        shell.help_user_setemail = MagicMock()

        spacecmd.user.do_user_setemail(shell, "")

        assert not shell.client.user.setDetails.called
        assert shell.help_user_setemail.called

    # pylint: disable-next=redefined-outer-name
    def test_user_setemail(self, shell):
        """
        Test do_user_setemail with data.

        :param shell:
        :return:
        """

        shell.client.user.setDetails = MagicMock()
        shell.help_user_setemail = MagicMock()

        spacecmd.user.do_user_setemail(shell, "bofh b@op.com")

        assert not shell.help_user_setemail.called
        assert shell.client.user.setDetails.called

        assert_args_expect(
            shell.client.user.setDetails.call_args_list,
            [((shell.session, "bofh", {"email": "b@op.com"}), {})],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_setprefix_noargs(self, shell):
        """
        Test do_user_setprefix without arguments.

        :param shell:
        :return:
        """

        shell.client.user.setDetails = MagicMock()
        shell.help_user_setprefix = MagicMock()

        spacecmd.user.do_user_setprefix(shell, "")

        assert not shell.client.user.setDetails.called
        assert shell.help_user_setprefix.called

    # pylint: disable-next=redefined-outer-name
    def test_user_setprefix_empty(self, shell):
        """
        Test do_user_setprefix with empty prefix.

        :param shell:
        :return:
        """

        shell.client.user.setDetails = MagicMock()
        shell.help_user_setprefix = MagicMock()

        spacecmd.user.do_user_setprefix(shell, "bofh")

        assert not shell.help_user_setprefix.called
        assert shell.client.user.setDetails.called

        assert_args_expect(
            shell.client.user.setDetails.call_args_list,
            [((shell.session, "bofh", {"prefix": " "}), {})],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_setprefix_pref(self, shell):
        """
        Test do_user_setprefix with any prefix.

        :param shell:
        :return:
        """

        shell.client.user.setDetails = MagicMock()
        shell.help_user_setprefix = MagicMock()

        spacecmd.user.do_user_setprefix(shell, "bofh Bst")

        assert not shell.help_user_setprefix.called
        assert shell.client.user.setDetails.called

        assert_args_expect(
            shell.client.user.setDetails.call_args_list,
            [((shell.session, "bofh", {"prefix": "Bst"}), {})],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_setpassword_noargs(self, shell):
        """
        Test do_user_setpassword without arguments.

        :param shell:
        :return:
        """

        shell.client.user.setDetails = MagicMock()
        shell.help_user_setpassword = MagicMock()

        spacecmd.user.do_user_setpassword(shell, "")

        assert not shell.client.user.setDetails.called
        assert shell.help_user_setpassword.called

    # pylint: disable-next=redefined-outer-name
    def test_user_setpassword(self, shell):
        """
        Test do_user_setpassword with data.

        :param shell:
        :return:
        """

        shell.client.user.setDetails = MagicMock()
        shell.help_user_setpassword = MagicMock()

        spacecmd.user.do_user_setpassword(shell, "someuser toto")

        assert not shell.help_user_setpassword.called
        assert shell.client.user.setDetails.called

        assert_args_expect(
            shell.client.user.setDetails.call_args_list,
            [((shell.session, "someuser", {"password": "toto"}), {})],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_details_noargs(self, shell):
        """
        Test do_user_details without arguments.

        :param shell:
        :return:
        """
        shell.help_user_details = MagicMock()
        shell.client.user.getDetails = MagicMock()
        shell.client.user.listRoles = MagicMock()
        shell.client.user.listAssignedSystemGroups = MagicMock()
        shell.client.user.listDefaultSystemGroups = MagicMock()
        shell.client.org.getDetails = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.print", mprint) as prt, patch(
            "spacecmd.user.logging",
            logger
            # pylint: disable-next=unused-variable
        ) as lgr:
            spacecmd.user.do_user_details(shell, "")

        assert not shell.client.user.getDetails.called
        assert not shell.client.user.listRoles.called
        assert not shell.client.user.listAssignedSystemGroups.called
        assert not shell.client.user.listDefaultSystemGroups.called
        assert not shell.client.org.getDetails.called
        assert not mprint.called
        assert not logger.warning.called
        assert shell.help_user_details.called

    # pylint: disable-next=redefined-outer-name
    def test_user_details_invalid_users(self, shell):
        """
        Test do_user_details with invalid/not-found users.

        :param shell:
        :return:
        """
        shell.help_user_details = MagicMock()
        shell.client.user.getDetails = MagicMock(
            side_effect=xmlrpclib.Fault(
                faultCode=42, faultString="User caused disks spinning backwards"
            )
        )
        shell.client.user.listRoles = MagicMock()
        shell.client.user.listAssignedSystemGroups = MagicMock()
        shell.client.user.listDefaultSystemGroups = MagicMock()
        shell.client.org.getDetails = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.print", mprint) as prt, patch(
            "spacecmd.user.logging",
            logger
            # pylint: disable-next=unused-variable
        ) as lgr:
            spacecmd.user.do_user_details(shell, "hairypointed othermissing")

        assert not shell.client.user.listRoles.called
        assert not shell.client.user.listAssignedSystemGroups.called
        assert not shell.client.user.listDefaultSystemGroups.called
        assert not shell.client.org.getDetails.called
        assert not shell.help_user_details.called
        assert not mprint.called
        assert logger.warning.called
        assert logger.debug.called
        assert shell.client.user.getDetails.called

        assert_list_args_expect(
            logger.warning.call_args_list,
            ["hairypointed is not a valid user", "othermissing is not a valid user"],
        )
        assert_list_args_expect(
            logger.debug.call_args_list,
            [
                "Error '42' while getting data about user "
                "'hairypointed': User caused disks spinning backwards",
                "Error '42' while getting data about user "
                "'othermissing': User caused disks spinning backwards",
            ],
        )

    # pylint: disable-next=redefined-outer-name
    def test_user_details_get_data(self, shell):
        """
        Test do_user_details with found users to check the data

        :param shell:
        :return:
        """
        shell.help_user_details = MagicMock()
        shell.client.user.getDetails = MagicMock(
            side_effect=[
                {
                    "first_name": "John",
                    "last_name": "Smith",
                    "email": "j.smith@company.com",
                    "last_login_date": "1999.01.02",
                    "created_date": "1999.01.01",
                    "enabled": False,
                },
                {
                    "first_name": "Bofh",
                    "last_name": "Operator",
                    "email": "bofh@company.com",
                    "last_login_date": "2019.01.01",
                    "created_date": "1980.01.01",
                    "enabled": True,
                },
            ]
        )
        shell.client.user.listRoles = MagicMock(
            side_effect=[
                ["printer", "spectator"],
                ["coffee", "bofh"],
            ]
        )
        shell.client.user.listAssignedSystemGroups = MagicMock(
            side_effect=[
                [{"name": "beer"}, {"name": "schinken"}, {"name": "swimming pool"}],
                [{"name": "butterfly catchers"}, {"name": "chessplayers"}],
            ]
        )
        shell.client.user.listDefaultSystemGroups = MagicMock(
            side_effect=[[{"name": "something"}], []]
        )
        shell.client.org.getDetails = MagicMock(return_value={"name": "company.com"})
        mprint = MagicMock()
        logger = MagicMock()

        # pylint: disable-next=unused-variable
        with patch("spacecmd.user.print", mprint) as prt, patch(
            "spacecmd.user.logging",
            logger
            # pylint: disable-next=unused-variable
        ) as lgr:
            spacecmd.user.do_user_details(shell, "hairypointed bofh")

        assert not shell.help_user_details.called
        assert not logger.warning.called
        assert not logger.debug.called
        assert shell.client.user.listRoles.called
        assert shell.client.user.listAssignedSystemGroups.called
        assert shell.client.user.listDefaultSystemGroups.called
        assert shell.client.org.getDetails.called
        assert shell.client.user.getDetails.called
        assert mprint.called

        assert_list_args_expect(
            mprint.call_args_list,
            [
                "Username:      hairypointed",
                "First Name:    John",
                "Last Name:     Smith",
                "Email Address: j.smith@company.com",
                "Organisation:  company.com",
                "Last Login:    1999.01.02",
                "Created:       1999.01.01",
                "Enabled:       False",
                "",
                "Roles",
                "-----",
                "printer\nspectator",
                "",
                "Assigned Groups",
                "---------------",
                "beer\nschinken\nswimming pool",
                "",
                "Default Groups",
                "--------------",
                "something",
                "----------",
                "Username:      bofh",
                "First Name:    Bofh",
                "Last Name:     Operator",
                "Email Address: bofh@company.com",
                "Organisation:  company.com",
                "Last Login:    2019.01.01",
                "Created:       1980.01.01",
                "Enabled:       True",
                "",
                "Roles",
                "-----",
                "bofh\ncoffee",
                "",
                "Assigned Groups",
                "---------------",
                "butterfly catchers\nchessplayers",
            ],
        )
