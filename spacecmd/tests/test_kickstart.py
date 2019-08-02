# coding: utf-8
"""
Kickstart API calls unit tests.

NOTE: This module is quite rarely used within Uyuni/SLE,
      only mostly for cloning, manual editing of the cobbler profiles
      and then deleting them.
"""
import os
from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.kickstart


class TestSCKickStart:
    """
    Test kickstart.
    """
    def test_kickstart_clone_interactive_no_profiles(self, shell):
        """
        Test do_kickstart_clone interactive. No profiles found.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr:
            spacecmd.kickstart.do_kickstart_clone(shell, "")

        assert not mprint.called
        assert not shell.client.kickstart.cloneProfile.called
        assert logger.error.called
        assert_expect(logger.error.call_args_list,
                      "No kickstart profiles available")

    def test_kickstart_clone_interactive_wrong_profile_entered(self, shell):
        """
        Test do_kickstart_clone interactive. Wrong profile has been entered.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock(side_effect=[
            "posix_compliance_problem", "POSIX"])
        shell.do_kickstart_list = MagicMock(return_value=[
            "default_kickstart_profile", "some_other_profile"])
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr, \
                patch("spacecmd.kickstart.prompt_user", prompter) as pmt:
            spacecmd.kickstart.do_kickstart_clone(shell, "")

        assert not shell.client.kickstart.cloneProfile.called
        assert mprint.called
        assert prompter.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list,
                      "Kickstart profile you've entered was not found")
        assert_list_args_expect(mprint.call_args_list,
                                ['', 'Kickstart Profiles', '------------------',
                                 'default_kickstart_profile\nsome_other_profile', ''])
        assert_args_expect(prompter.call_args_list,
                           [(('Original Profile:',), {"noblank": True}),
                            (('Cloned Profile:',), {"noblank": True})])

    def test_kickstart_clone_arg_wrong_profile_entered(self, shell):
        """
        Test do_kickstart_clone with args. Wrong profile has been entered.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock()
        shell.do_kickstart_list = MagicMock(return_value=[
            "default_kickstart_profile", "some_other_profile"])
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr, \
                patch("spacecmd.kickstart.prompt_user", prompter) as pmt:
            spacecmd.kickstart.do_kickstart_clone(shell, "-n posix_compliance_problem -c POSIX")

        assert not prompter.called
        assert not shell.client.kickstart.cloneProfile.called
        assert not mprint.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list,
                      "Kickstart profile you've entered was not found")

    def test_kickstart_clone_arg_no_name_entered(self, shell):
        """
        Test do_kickstart_clone with args. No kickstart profile name entered.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock()
        shell.do_kickstart_list = MagicMock(return_value=[
            "default_kickstart_profile", "some_other_profile"])
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr, \
                patch("spacecmd.kickstart.prompt_user", prompter) as pmt:
            spacecmd.kickstart.do_kickstart_clone(shell, "-c POSIX")

        assert not prompter.called
        assert not shell.client.kickstart.cloneProfile.called
        assert not mprint.called
        assert logger.error.called
        assert_expect(logger.error.call_args_list, "The Kickstart name is required")

    def test_kickstart_clone_arg_no_target_entered(self, shell):
        """
        Test do_kickstart_clone with args. No kickstart target profile name entered.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock()
        shell.do_kickstart_list = MagicMock(return_value=[
            "default_kickstart_profile", "some_other_profile"])
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr, \
                patch("spacecmd.kickstart.prompt_user", prompter) as pmt:
            spacecmd.kickstart.do_kickstart_clone(shell, "-n whatever_profile")

        assert not prompter.called
        assert not shell.client.kickstart.cloneProfile.called
        assert not mprint.called
        assert logger.error.called
        assert_expect(logger.error.call_args_list, "The Kickstart clone name is required")

    def test_kickstart_clone_args(self, shell):
        """
        Test do_kickstart_clone with args.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock()
        shell.do_kickstart_list = MagicMock(return_value=[
            "default_kickstart_profile", "some_other_profile"])
        name, clone = "default_kickstart_profile", "new_default_profile"
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr, \
                patch("spacecmd.kickstart.prompt_user", prompter) as pmt:
            spacecmd.kickstart.do_kickstart_clone(
                shell, "-n {} -c {}".format(name, clone))

        assert not prompter.called
        assert not mprint.called
        assert not logger.error.called
        assert shell.client.kickstart.cloneProfile.called

        assert_args_expect(shell.client.kickstart.cloneProfile.call_args_list,
                           [((shell.session, name, clone), {})])

    def test_kickstart_listscripts_noarg(self, shell):
        """
        Test do_kickstart_listscripts without arguments.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr:
            spacecmd.kickstart.do_kickstart_listscripts(shell, "")

        assert not shell.client.kickstart.profile.listScripts.called
        assert shell.help_kickstart_listscripts.called

    def test_kickstart_listscripts_no_scripts(self, shell):
        """
        Test do_kickstart_listscripts list scripts for the specified profile. No scripts attached.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        shell.client.kickstart.profile.listScripts = MagicMock(return_value=[])
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr:
            spacecmd.kickstart.do_kickstart_listscripts(shell, "some_profile")

        assert not shell.help_kickstart_listscripts.called
        assert not mprint.called
        assert shell.client.kickstart.profile.listScripts.called
        assert logger.error.called
        assert_args_expect(logger.error.call_args_list,
                           [(("No scripts has been found for profile '%s'",
                              "some_profile"), {})])

    def test_kickstart_listscripts_scripts_found(self, shell):
        """
        Test do_kickstart_listscripts list scripts for the specified profile.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        shell.client.kickstart.profile.listScripts = MagicMock(
            return_value=[
                {"id": 1, "script_type": "shell",
                 "chroot": "/dev/null", "interpreter": "/bin/bash",
                 "contents": """#!/bin/bash
echo 'Hello there!'
                 """},
                {"id": 2, "script_type": "shell",
                 "chroot": "/dev/null", "interpreter": "/bin/bash",
                 "contents": """#!/bin/bash
echo 'some more hello'
                 """
                 },
            ])
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr:
            spacecmd.kickstart.do_kickstart_listscripts(shell, "some_profile")

        assert not shell.help_kickstart_listscripts.called
        assert not logger.error.called
        assert mprint.called
        assert shell.client.kickstart.profile.listScripts.called

        assert_list_args_expect(mprint.call_args_list,
                                ['ID:          1', 'Type:        shell',
                                 'Chroot:      /dev/null', 'Interpreter: /bin/bash', '',
                                 'Contents', '--------', "#!/bin/bash\necho 'Hello there!'\n                 ",
                                 '----------', 'ID:          2', 'Type:        shell',
                                 'Chroot:      /dev/null', 'Interpreter: /bin/bash', '',
                                 'Contents', '--------', "#!/bin/bash\necho 'some more hello'\n                 "]
                                )

    def test_kickstart_list_nodata(self, shell):
        """
        Test do_kickstart_list. Return no data, print to STDOUT.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        shell.client.kickstart.listKickstarts = MagicMock(return_value=[
            {"name": "default_kickstart"}, {"name": "whatever_kickstart"},
            {"name": "some_profile_kickstart"}
        ])
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr:
            out = spacecmd.kickstart.do_kickstart_list(shell, "", doreturn=False)

        assert not logger.error.called
        assert out is None
        assert mprint.called
        assert_expect(mprint.call_args_list,
                      'default_kickstart\nsome_profile_kickstart\nwhatever_kickstart')

    def test_kickstart_list_nodata_noprofiles(self, shell):
        """
        Test do_kickstart_list. Return no data, print to STDOUT. No profiles has been found.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        shell.client.kickstart.listKickstarts = MagicMock(return_value=[])
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr:
            out = spacecmd.kickstart.do_kickstart_list(shell, "", doreturn=False)

        assert not mprint.called
        assert logger.error.called
        assert out is None
        assert_expect(logger.error.call_args_list,
                      "No kickstart profiles available")

    def test_kickstart_list_data(self, shell):
        """
        Test do_kickstart_list. Return data, no printing to STDOUT.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        shell.client.kickstart.listKickstarts = MagicMock(return_value=[
            {"name": "default_kickstart"}, {"name": "whatever_kickstart"},
            {"name": "some_profile_kickstart"}
        ])
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr:
            out = spacecmd.kickstart.do_kickstart_list(shell, "", doreturn=True)

        assert not mprint.called
        assert not logger.error.called
        assert out is not None
        assert len(out) == 3
        assert out == ["default_kickstart", "some_profile_kickstart", "whatever_kickstart"]

    def test_kickstart_list_data_no_profiles(self, shell):
        """
        Test do_kickstart_list. Return data, no printing to STDOUT. No profiles found.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        logger = MagicMock()
        shell.client.kickstart.listKickstarts = MagicMock(return_value=[])
        with patch("spacecmd.kickstart.print", mprint) as prt, \
                patch("spacecmd.kickstart.logging", logger) as lgr:
            out = spacecmd.kickstart.do_kickstart_list(shell, "", doreturn=True)

        assert not mprint.called
        assert not logger.error.called
        assert out is not None
        assert out == []

    def test_kickstart_delete_noargs(self, shell):
        """
        Test do_kickstart_delete without arguments.

        :param shell:
        :return:
        """
        logger = MagicMock()
        with patch("spacecmd.kickstart.logging", logger) as lgr:
            spacecmd.kickstart.do_kickstart_delete(shell, "")

        assert not logger.error.called
        assert not logger.debug.called
        assert not shell.do_kickstart_list.called
        assert not shell.client.kickstart.deleteProfile.called
        assert shell.help_kickstart_delete.called

    def test_kickstart_delete_invalid_profile(self, shell):
        """
        Test do_kickstart_delete invalid profile (not found).

        :param shell:
        :return:
        """
        shell.do_kickstart_list = MagicMock(return_value=[
            "first_profile", "second_profile", "third_profile"
        ])
        logger = MagicMock()
        with patch("spacecmd.kickstart.logging", logger) as lgr:
            spacecmd.kickstart.do_kickstart_delete(shell, "fourth_profile")

        assert not shell.client.kickstart.deleteProfile.called
        assert logger.error.called
        assert logger.debug.called
        assert shell.do_kickstart_list.called
        assert shell.help_kickstart_delete.called

        assert_expect(logger.error.call_args_list,
                      'No valid kickstart labels passed as arguments!')
        assert_expect(logger.debug.call_args_list,
                      'Got labels to delete of []')
        assert_args_expect(shell.do_kickstart_list.call_args_list,
                           [(('', True), {})])

    def test_kickstart_delete_some_invalid_profile(self, shell):
        """
        Test do_kickstart_delete invalid profile (not found).

        :param shell:
        :return:
        """
        shell.do_kickstart_list = MagicMock(return_value=[
            "first_profile", "second_profile", "third_profile"
        ])
        logger = MagicMock()
        with patch("spacecmd.kickstart.logging", logger) as lgr:
            spacecmd.kickstart.do_kickstart_delete(
                shell, "fourth_profile zero_profile first_profile second_profile")

        assert not shell.client.kickstart.deleteProfile.called
        assert not shell.help_kickstart_delete.called
        assert logger.error.called
        assert shell.do_kickstart_list.called
        assert logger.debug.called

        assert_expect(logger.debug.call_args_list,
                      "Got labels to delete of ['first_profile', 'second_profile']")
        assert_args_expect(logger.error.call_args_list,
                           [(('The following kickstart labels are invalid:',
                              'fourth_profile, zero_profile'), {})])

    def test_kickstart_delete_profile_all_yes(self, shell):
        """
        Test do_kickstart_delete profile, yes=true. This should not cause interactive prompt.

        :param shell:
        :return:
        """
        shell.options.yes = True
        shell.do_kickstart_list = MagicMock(return_value=[
            "first_profile", "second_profile", "third_profile"
        ])
        logger = MagicMock()
        with patch("spacecmd.kickstart.logging", logger) as lgr:
            spacecmd.kickstart.do_kickstart_delete(
                shell, "first_profile second_profile")

        assert not shell.help_kickstart_delete.called
        assert not logger.error.called
        assert not shell.user_confirm.called
        assert shell.client.kickstart.deleteProfile.called
        assert shell.do_kickstart_list.called
        assert logger.debug.called

        assert_args_expect(shell.client.kickstart.deleteProfile.call_args_list,
                           [((shell.session, "first_profile"), {}),
                            ((shell.session, "second_profile"), {})])

    def test_kickstart_delete_profile_interactive(self, shell):
        """
        Test do_kickstart_delete profile, yes=false. Should start interactive confirmation prompt.

        :param shell:
        :return:
        """
        shell.options.yes = False
        shell.user_confirm = MagicMock(return_value=True)
        shell.do_kickstart_list = MagicMock(return_value=[
            "first_profile", "second_profile", "third_profile"
        ])
        logger = MagicMock()
        with patch("spacecmd.kickstart.logging", logger) as lgr:
            spacecmd.kickstart.do_kickstart_delete(
                shell, "first_profile second_profile")

        assert not shell.help_kickstart_delete.called
        assert not logger.error.called
        assert shell.user_confirm.called
        assert shell.client.kickstart.deleteProfile.called
        assert shell.do_kickstart_list.called
        assert logger.debug.called

        assert_args_expect(shell.client.kickstart.deleteProfile.call_args_list,
                           [((shell.session, "first_profile"), {}),
                            ((shell.session, "second_profile"), {})])

    def test_kickstart_listcryptokeys_noargs(self, shell):
        """
        Test do_kickstart_listcryptokeys no args.

        :param shell:
        :return:
        """
        mprint = MagicMock()
        with patch("spacecmd.kickstart.print", mprint) as prt:
            data = spacecmd.kickstart.do_kickstart_listcryptokeys(shell, "")

        assert not shell.kickstart.profile.system.listKeys.called
        assert not mprint.called
        assert data is None
        assert shell.help_kickstart_listcryptokeys.called

    def test_kickstart_listcryptokeys_no_cryptokeys(self, shell):
        """
        Test do_kickstart_listcryptokeys no cryptokeys.

        :param shell:
        :return:
        """
        shell.client.kickstart.profile.system.listKeys = MagicMock(return_value=[
            {"description": "c_key"},
            {"description": "b_key"},
            {"description": "a_key"},
            {"description": "z_key"},
            {"description": "x_key"},
        ])
        mprint = MagicMock()
        with patch("spacecmd.kickstart.print", mprint) as prt:
            data = spacecmd.kickstart.do_kickstart_listcryptokeys(shell, "some_profile")

        assert not shell.help_kickstart_listcryptokeys.called
        assert mprint.called
        assert data is None
        assert shell.client.kickstart.profile.system.listKeys.called

        assert_expect(mprint.call_args_list,
                      'a_key\nb_key\nc_key\nx_key\nz_key')

    def test_kickstart_listcryptokeys_no_cryptokeys(self, shell):
        """
        Test do_kickstart_listcryptokeys no cryptokeys.

        :param shell:
        :return:
        """
        shell.client.kickstart.profile.system.listKeys = MagicMock(return_value=[
            {"description": "c_key"},
            {"description": "b_key"},
            {"description": "a_key"},
            {"description": "z_key"},
            {"description": "x_key"},
        ])
        mprint = MagicMock()
        with patch("spacecmd.kickstart.print", mprint) as prt:
            data = spacecmd.kickstart.do_kickstart_listcryptokeys(
                shell, "some_profile", doreturn=True)

        assert not shell.help_kickstart_listcryptokeys.called
        assert not mprint.called
        assert data is not None
        assert data == ['a_key', 'b_key', 'c_key', 'x_key', 'z_key']
        assert shell.client.kickstart.profile.system.listKeys.called
