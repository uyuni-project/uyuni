# coding: utf-8
"""
Test suite for group module of spacecmd
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.group


class TestSCGroup:
    """
    Test suite for "group" module.
    """

    def test_group_addsystems_noargs(self, shell):
        """
        Test do_group_addsystems without arguments.

        :param shell:
        :return:
        """

        shell.help_group_addsystems = MagicMock()
        shell.get_system_id = MagicMock()
        shell.expand_systems = MagicMock()
        shell.client.systemgroup.addOrRemoveSystems = MagicMock()
        shell.ssm.keys = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.group.print", mprint) as prn, \
            patch("spacecmd.group.logging", logger) as lgr:
            spacecmd.group.do_group_addsystems(shell, "")

        assert not shell.get_system_id.called
        assert not shell.ssm.keys.called
        assert not shell.client.systemgroup.addOrRemoveSystems.called
        assert not shell.expand_systems.called
        assert not mprint.called
        assert not logger.error.called
        assert shell.help_group_addsystems.called

    def test_group_addsystems_ssm_no_systems(self, shell):
        """
        Test do_group_addsystems with SSM argument, without systems.

        :param shell:
        :return:
        """
        shell.help_group_addsystems = MagicMock()
        shell.get_system_id = MagicMock()
        shell.expand_systems = MagicMock()
        shell.client.systemgroup.addOrRemoveSystems = MagicMock()
        shell.ssm.keys = MagicMock(return_value=[])
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.group.print", mprint) as prn, \
            patch("spacecmd.group.logging", logger) as lgr:
            spacecmd.group.do_group_addsystems(shell, "groupname ssm")

        assert not shell.get_system_id.called
        assert not shell.expand_systems.called
        assert not shell.client.systemgroup.addOrRemoveSystems.called
        assert not mprint.called
        assert not logger.error.called
        assert not shell.help_group_addsystems.called
        assert shell.ssm.keys.called

    def test_group_addsystems_expand_no_systems(self, shell):
        """
        Test do_group_addsystems with API call to find systems, without success getting one.

        :param shell:
        :return:
        """
        shell.help_group_addsystems = MagicMock()
        shell.get_system_id = MagicMock()
        shell.expand_systems = MagicMock(return_value=[])
        shell.client.systemgroup.addOrRemoveSystems = MagicMock()
        shell.ssm.keys = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.group.print", mprint) as prn, \
            patch("spacecmd.group.logging", logger) as lgr:
            spacecmd.group.do_group_addsystems(shell, "groupname something*")

        assert not shell.get_system_id.called
        assert not shell.client.systemgroup.addOrRemoveSystems.called
        assert not mprint.called
        assert not logger.error.called
        assert not shell.help_group_addsystems.called
        assert not shell.ssm.keys.called
        assert shell.expand_systems.called

    def test_group_addsystems(self, shell):
        """
        Test do_group_addsystems with API call to find systems.

        :param shell:
        :return:
        """
        shell.help_group_addsystems = MagicMock()
        shell.get_system_id = MagicMock(side_effect=["1000010000", "1000010001"])
        shell.expand_systems = MagicMock(return_value=["one", "two"])
        shell.client.systemgroup.addOrRemoveSystems = MagicMock()
        shell.ssm.keys = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.group.print", mprint) as prn, \
            patch("spacecmd.group.logging", logger) as lgr:
            spacecmd.group.do_group_addsystems(shell, "groupname something*")

        assert not mprint.called
        assert not logger.error.called
        assert not shell.help_group_addsystems.called
        assert not shell.ssm.keys.called
        assert shell.get_system_id.called
        assert shell.client.systemgroup.addOrRemoveSystems.called
        assert shell.expand_systems.called

        assert_args_expect(shell.client.systemgroup.addOrRemoveSystems.call_args_list,
                           [((shell.session, 'groupname', ['1000010000', '1000010001'], True), {})])

    def test_group_removesystems_noargs(self, shell):
        """
        Test do_group_removesystems without arguments.

        :param shell:
        :return:
        """

        shell.help_group_removesystems = MagicMock()
        shell.get_system_id = MagicMock()
        shell.expand_systems = MagicMock()
        shell.client.systemgroup.addOrRemoveSystems = MagicMock()
        shell.ssm.keys = MagicMock()
        shell.user_confirm = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.group.print", mprint) as prn, \
            patch("spacecmd.group.logging", logger) as lgr:
            spacecmd.group.do_group_removesystems(shell, "")

        assert not shell.get_system_id.called
        assert not shell.ssm.keys.called
        assert not shell.client.systemgroup.addOrRemoveSystems.called
        assert not shell.expand_systems.called
        assert not shell.user_confirm.called
        assert not mprint.called
        assert not logger.error.called
        assert shell.help_group_removesystems.called

    def test_group_removesystems_ssm_nosys(self, shell):
        """
        Test do_group_removesystems with SSM and without found systems.

        :param shell:
        :return:
        """

        shell.help_group_removesystems = MagicMock()
        shell.get_system_id = MagicMock()
        shell.expand_systems = MagicMock()
        shell.client.systemgroup.addOrRemoveSystems = MagicMock()
        shell.ssm.keys = MagicMock(return_value=[])
        shell.user_confirm = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.group.print", mprint) as prn, \
            patch("spacecmd.group.logging", logger) as lgr:
            spacecmd.group.do_group_removesystems(shell, "somegroup ssm")

        assert not shell.get_system_id.called
        assert not shell.client.systemgroup.addOrRemoveSystems.called
        assert not shell.expand_systems.called
        assert not shell.user_confirm.called
        assert not logger.error.called
        assert not shell.help_group_removesystems.called
        assert mprint.called
        assert shell.ssm.keys.called

        assert_expect(mprint.call_args_list, "No systems found")

    def test_group_removesystems_nossm_nosys(self, shell):
        """
        Test do_group_removesystems with filters and without found systems.

        :param shell:
        :return:
        """

        shell.help_group_removesystems = MagicMock()
        shell.get_system_id = MagicMock(side_effect=["1000010000", "1000010001"])
        shell.expand_systems = MagicMock(return_value=[])
        shell.client.systemgroup.addOrRemoveSystems = MagicMock()
        shell.ssm.keys = MagicMock()
        shell.user_confirm = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.group.print", mprint) as prn, \
            patch("spacecmd.group.logging", logger) as lgr:
            spacecmd.group.do_group_removesystems(shell, "somegroup somesystem")

        assert not shell.get_system_id.called
        assert not shell.client.systemgroup.addOrRemoveSystems.called
        assert not shell.user_confirm.called
        assert not logger.error.called
        assert not shell.help_group_removesystems.called
        assert not shell.ssm.keys.called
        assert mprint.called
        assert shell.expand_systems.called

        assert_expect(mprint.call_args_list, "No systems found")

    def test_group_removesystems_nossm_sys(self, shell):
        """
        Test do_group_removesystems with filters and found systems.

        :param shell:
        :return:
        """

        shell.help_group_removesystems = MagicMock()
        shell.get_system_id = MagicMock(side_effect=["1000010000", "1000010001"])
        shell.expand_systems = MagicMock(return_value=["one", "two"])
        shell.client.systemgroup.addOrRemoveSystems = MagicMock()
        shell.ssm.keys = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.group.print", mprint) as prn, \
            patch("spacecmd.group.logging", logger) as lgr:
            spacecmd.group.do_group_removesystems(shell, "somegroup somesystem")

        assert not logger.error.called
        assert not shell.help_group_removesystems.called
        assert not shell.ssm.keys.called
        assert shell.get_system_id.called
        assert shell.user_confirm.called
        assert mprint.called
        assert shell.expand_systems.called
        assert shell.client.systemgroup.addOrRemoveSystems.called

        assert_args_expect(shell.client.systemgroup.addOrRemoveSystems.call_args_list,
                           [((shell.session, 'somegroup', ['1000010000', '1000010001'], False), {})])
        assert_list_args_expect(mprint.call_args_list,
                                ["Systems", "-------", "one\ntwo"])

    def test_group_create_noarg(self, shell):
        """
        Test do_group_create without no arguments (fall-back to the interactive mode).

        :param shell:
        :return:
        """
        msg = "Great group for nothing"
        shell.client.systemgroup.create = MagicMock()
        prompter = MagicMock(side_effect=["Jeff", msg])

        with patch("spacecmd.group.prompt_user", prompter):
            spacecmd.group.do_group_create(shell, "")

        assert prompter.called
        assert shell.client.systemgroup.create.called

        assert_args_expect(shell.client.systemgroup.create.call_args_list,
                           [((shell.session, 'Jeff', msg), {})])

    def test_group_create_name_only(self, shell):
        """
        Test do_group_create with name argument (half-fall back to interactive).

        :param shell:
        :return:
        """
        msg = "Great group for nothing"
        shell.client.systemgroup.create = MagicMock()
        prompter = MagicMock(return_value=msg)

        with patch("spacecmd.group.prompt_user", prompter):
            spacecmd.group.do_group_create(shell, "Jeff")

        assert prompter.called
        assert shell.client.systemgroup.create.called

        assert_args_expect(shell.client.systemgroup.create.call_args_list,
                           [((shell.session, 'Jeff', msg), {})])

    def test_group_create_descr_only(self, shell):
        """
        Test do_group_create with all arguments.

        :param shell:
        :return:
        """
        msg = "Great group for nothing"
        shell.client.systemgroup.create = MagicMock()
        prompter = MagicMock(return_value=msg)

        with patch("spacecmd.group.prompt_user", prompter):
            spacecmd.group.do_group_create(shell, "Jeff {}".format(msg))

        assert not prompter.called
        assert shell.client.systemgroup.create.called

        assert_args_expect(shell.client.systemgroup.create.call_args_list,
                           [((shell.session, 'Jeff', msg), {})])

    def test_group_delete_noarg(self, shell):
        """
        Test do_group_delete without no arguments

        :param shell:
        :return:
        """
        shell.client.systemgroup.delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=False)
        shell.help_group_delete = MagicMock()

        spacecmd.group.do_group_delete(shell, "")

        assert not shell.client.systemgroup.delete.called
        assert not shell.user_confirm.called
        assert shell.help_group_delete.called

    def test_group_delete_no_confirm(self, shell):
        """
        Test do_group_delete no confirmation

        :param shell:
        :return:
        """
        shell.client.systemgroup.delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=False)
        shell.help_group_delete = MagicMock()

        spacecmd.group.do_group_delete(shell, "groupone grouptwo groupthree")

        assert not shell.client.systemgroup.delete.called
        assert not shell.help_group_delete.called
        assert shell.user_confirm.called

    def test_group_delete(self, shell):
        """
        Test do_group_delete with confirmation

        :param shell:
        :return:
        """
        shell.client.systemgroup.delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        shell.help_group_delete = MagicMock()

        spacecmd.group.do_group_delete(shell, "groupone grouptwo groupthree")

        assert not shell.help_group_delete.called
        assert shell.client.systemgroup.delete.called
        assert shell.user_confirm.called

        groups = [[((shell.session, "groupone"), {})],
                  [((shell.session, "grouptwo"), {})],
                  [((shell.session, "groupthree"), {})],]
        for call in shell.client.systemgroup.delete.call_args_list:
            assert_args_expect([call], next(iter(groups)))
            groups.pop(0)
        assert not groups
