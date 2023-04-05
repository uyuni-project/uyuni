# coding: utf-8
"""
Test suite for spacecmd.package module.
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect, assert_list_args_expect
import spacecmd.package


class TestSCPackage:
    """
    Test suite for package module.
    """
    def test_package_details_noargs(self, shell):
        """
        Test do_package_details with no arguments call.

        :param shell:
        :return:
        """
        shell.help_package_details = MagicMock()
        shell.get_package_id = MagicMock()
        shell.do_package_search = MagicMock()
        shell.client.packages.listProvidingChannels = MagicMock()
        shell.client.system.listSystemsWithPackage = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.report.print", mprint) as prn:
            spacecmd.package.do_package_details(shell, "")

        assert not shell.get_package_id.called
        assert not shell.do_package_search.called
        assert not shell.client.packages.listProvidingChannels.called
        assert not shell.client.system.listSystemsWithPackage.called
        assert shell.help_package_details.called

    def test_package_details_package(self, shell):
        """
        Test do_package_details with an argument of the package name.

        :param shell:
        :return:
        """
        shell.help_package_details = MagicMock()
        shell.do_package_search = MagicMock(side_effect=[
            ["emacs", "emacs-x11"],
        ])
        shell.get_package_id = MagicMock(return_value=[
            "emacs", "emacs-1"  # IDs are bogus here
        ])
        shell.client.packages.listProvidingChannels = MagicMock(side_effect=[
            [
                {
                    "label": "base-channel"
                },
                {
                    "label": "emacs-channel"
                },
            ],
            [
                {
                    "label": "base-channel"
                },
                {
                    "label": "emacs-channel"
                },
                {
                    "label": "x11-stuff-channel"
                },
            ],
            [], [], []
        ])
        shell.client.system.listSystemsWithPackage = MagicMock(return_value=[
            "system-a", "system-b", "system-c"
        ])
        shell.client.packages.getDetails = MagicMock(side_effect=[
            {
                "name": "emacs", "version": "24.5", "release": "42", "epoch": "1",
                "arch_label": "x86", "file": "emacs.rpm", "path": "/tmp", "size": "2000",
                "checksum_type": "md5", "checksum": "aaa0919fe05c15583b688fed115d1ab8",
                "description": "Better editor than Vim"
            },
            {
                "name": "emacs-x11", "version": "24.5.7", "release": "42.1", "epoch": "2",
                "arch_label": "x86", "file": "emacs-x11.rpm", "path": "/tmp", "size": "22000",
                "checksum_type": "md5", "checksum": "9d188ed99c1114eba7a8e499798da47c",
                "description": "Better editor than Vim, using X11"
            },
            {
                "name": "emacs-data", "version": "24.5.7", "release": "42.1", "epoch": "2",
                "arch_label": "x86", "file": "emacs-x11.rpm", "path": "/tmp", "size": "22000",
                "checksum_type": "md5", "checksum": "9d188ed99c1114eba7a8e499798da47c",
                "description": "Better editor than Vim, using X11"
            },
            {
                "name": "emacs-melpa", "version": "24.5.7", "release": "42.1", "epoch": "2",
                "arch_label": "x86", "file": "emacs-x11.rpm", "path": "/tmp", "size": "22000",
                "checksum_type": "md5", "checksum": "9d188ed99c1114eba7a8e499798da47c",
                "description": "Better editor than Vim, using X11"
            },
            {
                "name": "emacs-el", "version": "24.5.7", "release": "42.1", "epoch": "2",
                "arch_label": "x86", "file": "emacs-x11.rpm", "path": "/tmp", "size": "22000",
                "checksum_type": "md5", "checksum": "9d188ed99c1114eba7a8e499798da47c",
                "description": "Better editor than Vim, using X11"
            },
        ])

        shell.SEPARATOR = "###"

        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_details(shell, "emacs")

        assert not shell.help_package_details.called
        assert not logger.warning.called
        assert shell.do_package_search.called
        assert shell.get_package_id.called
        assert shell.client.packages.listProvidingChannels.called
        assert shell.client.system.listSystemsWithPackage.called

        exp = [
            'Name:      emacs',
            'Version:   24.5',
            'Release:   42',
            'Epoch:     1',
            'Arch:      x86',
            '',
            'File:      emacs.rpm',
            'Path:      /tmp',
            'Size:      2000',
            'Retracted: No',
            'MD5:       aaa0919fe05c15583b688fed115d1ab8',
            '',
            'Installed Systems: 3',
            '',
            'Description',
            '-----------',
            'Better editor than Vim',
            '',
            'Available From Channels',
            '-----------------------',
            'base-channel\nemacs-channel',
            '',
            'Name:      emacs-x11',
            'Version:   24.5.7',
            'Release:   42.1',
            'Epoch:     2',
            'Arch:      x86',
            '',
            'File:      emacs-x11.rpm',
            'Path:      /tmp',
            'Size:      22000',
            'Retracted: No',
            'MD5:       9d188ed99c1114eba7a8e499798da47c',
            '',
            'Installed Systems: 3',
            '',
            'Description',
            '-----------',
            'Better editor than Vim, using X11',
            '',
            'Available From Channels',
            '-----------------------',
            'base-channel\nemacs-channel\nx11-stuff-channel',
            '',
            '###',
            'Name:      emacs-data',
            'Version:   24.5.7',
            'Release:   42.1',
            'Epoch:     2',
            'Arch:      x86',
            '',
            'File:      emacs-x11.rpm',
            'Path:      /tmp',
            'Size:      22000',
            'Retracted: No',
            'MD5:       9d188ed99c1114eba7a8e499798da47c',
            '',
            'Installed Systems: 3',
            '',
            'Description',
            '-----------',
            'Better editor than Vim, using X11',
            '',
            'Available From Channels',
            '-----------------------',
            '',
            '',
            'Name:      emacs-melpa',
            'Version:   24.5.7',
            'Release:   42.1',
            'Epoch:     2',
            'Arch:      x86',
            '',
            'File:      emacs-x11.rpm',
            'Path:      /tmp',
            'Size:      22000',
            'Retracted: No',
            'MD5:       9d188ed99c1114eba7a8e499798da47c',
            '',
            'Installed Systems: 3',
            '',
            'Description',
            '-----------',
            'Better editor than Vim, using X11',
            '',
            'Available From Channels',
            '-----------------------',
            '',
            ''
        ]
        for call in mprint.call_args_list:
            assert_expect([call], next(iter(exp)))
            exp.pop(0)
        assert not exp

    def test_package_details_multiple_packages(self, shell):
        """
        Test do_package_details with two arguments of package names.

        :param shell:
        :return:
        """
        shell.help_package_details = MagicMock()
        shell.do_package_search = MagicMock(side_effect=[
            ["emacs-data", "emacs-x11"]
        ])
        shell.get_package_id = MagicMock(return_value=[
            "id1" # IDs are bogus here
        ])
        shell.client.packages.listProvidingChannels = MagicMock(side_effect=[
            [
                {
                    "label": "base-channel"
                },
                {
                    "label": "emacs-channel"
                },
            ],
            [
                {
                    "label": "base-channel"
                },
                {
                    "label": "emacs-channel"
                },
                {
                    "label": "x11-stuff-channel"
                },
            ]
        ])
        shell.client.system.listSystemsWithPackage = MagicMock(return_value=[
            "system-a", "system-b", "system-c"
        ])
        shell.client.packages.getDetails = MagicMock(side_effect=[
            {
                "name": "emacs-data", "version": "24.5.7", "release": "42.1", "epoch": "2",
                "arch_label": "x86", "file": "emacs-x11.rpm", "path": "/tmp", "size": "22000",
                "checksum_type": "md5", "checksum": "9d188ed99c1114eba7a8e499798da47c",
                "description": "Better editor than Vim, using X11", "part_of_retracted_patch": True
            },
            {
                "name": "emacs-x11", "version": "24.5.7", "release": "42.1", "epoch": "2",
                "arch_label": "x86", "file": "emacs-x11.rpm", "path": "/tmp", "size": "22000",
                "checksum_type": "md5", "checksum": "9d188ed99c1114eba7a8e499798da47c",
                "description": "Better editor than Vim, using X11"
            },
        ])

        shell.SEPARATOR = "###"

        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.package.print", mprint) as prn, \
                patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_details(shell, "emacs-data emacs-x11")

        assert not shell.help_package_details.called
        assert not logger.warning.called
        assert shell.do_package_search.called
        assert shell.get_package_id.called
        assert shell.client.packages.listProvidingChannels.called
        assert shell.client.system.listSystemsWithPackage.called

        exp = [
            'Name:      emacs-data',
            'Version:   24.5.7',
            'Release:   42.1',
            'Epoch:     2',
            'Arch:      x86',
            '',
            'File:      emacs-x11.rpm',
            'Path:      /tmp',
            'Size:      22000',
            'Retracted: Yes',
            'MD5:       9d188ed99c1114eba7a8e499798da47c',
            '',
            'Installed Systems: 3',
            '',
            'Description',
            '-----------',
            'Better editor than Vim, using X11',
            '',
            'Available From Channels',
            '-----------------------',
            'base-channel\nemacs-channel',
            '',
            '###',
            'Name:      emacs-x11',
            'Version:   24.5.7',
            'Release:   42.1',
            'Epoch:     2',
            'Arch:      x86',
            '',
            'File:      emacs-x11.rpm',
            'Path:      /tmp',
            'Size:      22000',
            'Retracted: No',
            'MD5:       9d188ed99c1114eba7a8e499798da47c',
            '',
            'Installed Systems: 3',
            '',
            'Description',
            '-----------',
            'Better editor than Vim, using X11',
            '',
            'Available From Channels',
            '-----------------------',
            'base-channel\nemacs-channel\nx11-stuff-channel',
            ''
        ]
        for call in mprint.call_args_list:
            assert_expect([call], next(iter(exp)))
            exp.pop(0)
        assert not exp

    def test_package_search_noargs(self, shell):
        """
        Test do_package_search without arguments.
        """
        shell.help_package_search = MagicMock()
        shell.client.packages.search.advanced = MagicMock()

        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            out = spacecmd.package.do_package_search(shell, "", doreturn=False)

        assert out is None
        assert not logger.debug.called
        assert not shell.client.packages.search.advanced.called
        assert shell.help_package_search.called

    def test_package_search(self, shell):
        """
        Test do_package_search with arguments of standard fields
        """
        shell.help_package_search = MagicMock()
        shell.get_package_names = MagicMock(return_value=[
            "emacs-x11", "emacs-melpa", "emacs-nox", "vim", "pico", "gedit", "sed"
        ])
        shell.client.packages.search.advanced = MagicMock()

        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            out = spacecmd.package.do_package_search(shell, "emacs*", doreturn=False)

        assert not shell.help_package_search.called
        assert not logger.debug.called
        assert not shell.client.packages.search.advanced.called
        assert out is None
        assert mprint.called
        assert_expect(mprint.call_args_list, 'emacs-melpa\nemacs-nox\nemacs-x11')

    def test_package_search_multiple_packages(self, shell):
        """
        Test do_package_search with multiple arguments of standard fields
        """
        shell.help_package_search = MagicMock()
        shell.get_package_names = MagicMock(return_value=[
            "emacs-x11", "emacs-melpa", "emacs-nox", "vim", "pico", "gedit", "sed"
        ])
        shell.client.packages.search.advanced = MagicMock()

        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.package.print", mprint) as prn, \
                patch("spacecmd.package.logging", logger) as lgr:
            out = spacecmd.package.do_package_search(shell, "emacs-melpa emacs-x11", doreturn=False)

        assert not shell.help_package_search.called
        assert not logger.debug.called
        assert not shell.client.packages.search.advanced.called
        assert out is None
        assert mprint.called
        assert_expect(mprint.call_args_list, 'emacs-melpa\nemacs-x11')

    def test_package_search_advanced(self, shell):
        """
        Test do_package_search with arguments of advanced fields.
        """
        shell.help_package_search = MagicMock()
        shell.get_package_names = MagicMock(return_value=[
            "emacs-x11", "emacs-melpa", "emacs-nox", "vim", "pico", "gedit", "sed"
        ])
        shell.client.packages.search.advanced = MagicMock(return_value=[
            {"name": "emacs-x11", "version": "24.5", "release": "1", "epoch": "", "arch": "x86_64", "arch_label": "x86_64"},
            {"name": "emacs-melpa", "version": "16.7", "release": "2", "epoch": "", "arch": "noarch", "arch_label": "noarch"},
            {"name": "emacs-nox", "version": "24.5.2", "release": "3", "epoch": "", "arch": "x86_64", "arch_label": "x86_64"},
        ])

        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.package.print", mprint) as prn, \
                patch("spacecmd.package.logging", logger) as lgr:
            out = spacecmd.package.do_package_search(
                shell, "name:emacs*", doreturn=False)

        assert not shell.help_package_search.called
        assert logger.debug.called
        assert shell.client.packages.search.advanced.called
        assert out is None
        assert mprint.called
        assert_expect(mprint.call_args_list,
                      'emacs-melpa-16.7-2.noarch\nemacs-nox-24.5.2-3.x86_64\nemacs-x11-24.5-1.x86_64')

    def test_package_search_advanced_wrong_fields(self, shell):
        """
        Test do_package_search with arguments of advanced fields.
        """
        shell.help_package_search = MagicMock()
        shell.get_package_names = MagicMock(return_value=[])
        shell.client.packages.search.advanced = MagicMock(return_value=[])

        logger = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.package.print", mprint) as prn, \
                patch("spacecmd.package.logging", logger) as lgr:
            out = spacecmd.package.do_package_search(
                shell, "millenium:emacs*", doreturn=False)

        assert not logger.debug.called
        assert not shell.client.packages.search.advanced.called
        assert out is None
        assert shell.help_package_search.called

    def test_package_search_advanced_check_fields(self, shell):
        """
        Test do_package_search check advanced fields.
        """
        for field in('name:', 'epoch:', 'version:', 'release:',
                     'arch:', 'description:', 'summary:'):
            shell.help_package_search = MagicMock()
            shell.get_package_names = MagicMock(return_value=[])
            shell.client.packages.search.advanced = MagicMock(return_value=[])

            logger = MagicMock()
            mprint = MagicMock()

            with patch("spacecmd.package.print", mprint) as prn, \
                    patch("spacecmd.package.logging", logger) as lgr:
                out = spacecmd.package.do_package_search(
                    shell, "{}emacs*".format(field), doreturn=True)

            assert not shell.help_package_search.called
            assert logger.debug.called
            assert shell.client.packages.search.advanced.called
            assert out is not None

    def test_package_remove_noarg(self, shell):
        """
        Test do_package_remove with no arguments passed.

            :param shell:
        """
        shell.help_package_remove = MagicMock()
        shell.get_package_names = MagicMock()
        shell.get_package_id = MagicMock()
        shell.client.packages.removePackage = MagicMock()
        shell.generate_package_cache = MagicMock()
        shell.user_configm = MagicMock(return_value=True)
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_remove(shell, "")

        assert not shell.get_package_names.called
        assert not shell.get_package_id.called
        assert not shell.client.packages.removePackage.called
        assert not shell.generate_package_cache.called
        assert not shell.user_configm.called
        assert shell.help_package_remove.called

    def test_package_remove_no_pkg_found(self, shell):
        """
        Test do_package_remove with no valid packages (packages not found).

            :param shell:
        """
        shell.help_package_remove = MagicMock()
        shell.get_package_names = MagicMock(return_value=[])
        shell.get_package_id = MagicMock()
        shell.client.packages.removePackage = MagicMock()
        shell.generate_package_cache = MagicMock()
        shell.user_configm = MagicMock(return_value=True)
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_remove(shell, "i-do-not-exist")

        assert not shell.get_package_id.called
        assert not shell.client.packages.removePackage.called
        assert not shell.generate_package_cache.called
        assert not shell.user_configm.called
        assert not shell.help_package_remove.called
        assert shell.get_package_names.called
        assert logger.debug.called
        assert mprint.called

        assert_expect(mprint.call_args_list, "No packages found to remove")

    def test_package_remove_specific_pkg_aborted(self, shell):
        """
        Test do_package_remove with unconfirmed valid packages.

            :param shell:
        """
        shell.help_package_remove = MagicMock()
        shell.get_package_names = MagicMock(return_value=["vim", "vim-plugins", "vim-data", "gvim", "gvim-ext",
                                                          "pico", "pico-data", "emacs", "emacs-nox", "xemacs"])
        shell.get_package_id = MagicMock()
        shell.client.packages.removePackage = MagicMock()
        shell.generate_package_cache = MagicMock()
        shell.user_confirm = MagicMock(return_value=False)
        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_remove(shell, "vim* gvim pico")

        assert not shell.get_package_id.called
        assert not shell.client.packages.removePackage.called
        assert not shell.generate_package_cache.called
        assert not shell.help_package_remove.called
        assert not logger.debug.called
        assert shell.user_confirm.called
        assert shell.get_package_names.called
        assert mprint.called

        assert mprint.call_args_list[-1][0][0] == "No packages has been removed"

    def test_package_remove_specific_pkg_accepted(self, shell):
        """
        Test do_package_remove with unconfirmed valid packages.

            :param shell:
        """
        shell.help_package_remove = MagicMock()
        shell.get_package_names = MagicMock(return_value=["vim", "vim-plugins", "vim-data", "gvim", "gvim-ext",
                                                          "pico", "pico-data", "emacs", "emacs-nox", "xemacs"])
        shell.get_package_id = MagicMock(return_value=["bogus-id"])
        shell.client.packages.removePackage = MagicMock()
        shell.generate_package_cache = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        mprint = MagicMock()
        logger = MagicMock()
        mocks_order = MagicMock()
        mocks_order.mprint, mocks_order.removePackage = mprint, shell.client.packages.removePackage

        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_remove(shell, "vim* gvim pico")

        assert not shell.help_package_remove.called
        assert not logger.debug.called
        assert not logger.error.called
        assert shell.get_package_id.called
        assert shell.client.packages.removePackage.called
        assert shell.user_confirm.called
        assert shell.get_package_names.called
        assert shell.generate_package_cache.called
        assert mprint.called

        exp = [
            'Packages',
            '--------',
            'gvim\npico\nvim\nvim-data\nvim-plugins'
        ]
        for call in mprint.call_args_list:
            assert_expect([call], next(iter(exp)))
            exp.pop(0)
        assert not exp

        # mprint is called first and removePackage the last
        assert mocks_order.mock_calls[0][0] == 'mprint'
        assert mocks_order.mock_calls[-1][0] == 'removePackage'

    def test_package_listorphans_noarg(self, shell):
        """
        Test do_package_listorphans without arguments.

            :param shell:
        """
        shell.client.channel.software.listPackagesWithoutChannel = MagicMock(return_value=[
            {"name": "vim", "version": "0.1", "release": "42", "epoch": "5", "arch": "AMD64", "arch_label": "amd64"},
            {"name": "vim-data", "version": "0.2", "release": "43", "epoch": "", "arch": "AMD64", "arch_label": "amd64"},
            {"name": "vim-plugins", "version": "1.17", "release": "16", "epoch": "", "arch": "AMD64", "arch_label": "amd64"},
        ])
        mprint = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn:
            out = spacecmd.package.do_package_listorphans(shell, "", doreturn=False)
        assert out is None
        assert mprint.called
        assert shell.client.channel.software.listPackagesWithoutChannel.called
        assert_expect(mprint.call_args_list, "vim-0.1-42:5.x86_64\nvim-data-0.2-43.x86_64\nvim-plugins-1.17-16.x86_64")

    def test_package_listorphans_return(self, shell):
        """
        Test do_package_listorphans without arguments.

            :param shell:
        """
        shell.client.channel.software.listPackagesWithoutChannel = MagicMock(return_value=[
            {"name": "vim", "version": "0.1", "release": "42", "epoch": "5", "arch": "AMD64", "arch_label": "amd64"},
            {"name": "vim-data", "version": "0.2", "release": "43", "epoch": "", "arch": "AMD64", "arch_label": "amd64"},
            {"name": "vim-plugins", "version": "1.17", "release": "16", "epoch": "", "arch": "AMD64", "arch_label": "amd64"},
        ])
        mprint = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn:
            out = spacecmd.package.do_package_listorphans(shell, "", doreturn=True)
        assert out is not None
        assert not mprint.called
        assert shell.client.channel.software.listPackagesWithoutChannel.called
        assert out == ['vim-0.1-42:5.x86_64', 'vim-data-0.2-43.x86_64', 'vim-plugins-1.17-16.x86_64']

    def test_package_removeorphans_noconfirm(self, shell):
        """
        Test do_package_removeorphans without confirmation.

            :param shell:
        """
        shell.client.channel.software.listPackagesWithoutChannel = MagicMock(return_value=[
            {"name": "vim", "version": "0.1", "release": "42", "epoch": "5", "arch": "AMD64", "arch_label": "amd64"},
            {"name": "vim-data", "version": "0.2", "release": "43", "epoch": "", "arch": "AMD64", "arch_label": "amd64"},
            {"name": "vim-plugins", "version": "1.17", "release": "16", "epoch": "", "arch": "AMD64", "arch_label": "amd64"},
        ])
        shell.client.packages.removePackage = MagicMock()
        shell.user_confirm = MagicMock(return_value=False)
        mprint = MagicMock()

        with patch("spacecmd.package.print", mprint) as prn:
            out = spacecmd.package.do_package_removeorphans(shell, "")

        assert not shell.client.packages.removePackage.called
        assert mprint.called
        assert out is 1
        assert shell.client.channel.software.listPackagesWithoutChannel.called

        assert mprint.call_args_list[-1][0][0] == "No packages were removed"

    def test_package_removeorphans_confirm(self, shell):
        """
        Test do_package_removeorphans with confirmation.

            :param shell:
        """
        shell.client.channel.software.listPackagesWithoutChannel = MagicMock(return_value=[
            {"name": "vim", "version": "0.1", "release": "42", "epoch": "5", "arch": "AMD64", "arch_label": "amd64"},
            {"name": "vim-data", "version": "0.2", "release": "43", "epoch": "", "arch": "AMD64", "arch_label": "amd64"},
            {"name": "vim-plugins", "version": "1.17", "release": "16", "epoch": "", "arch": "AMD64", "arch_label": "amd64"},
        ])
        shell.client.packages.removePackage = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        mprint = MagicMock()

        with patch("spacecmd.package.print", mprint) as prn:
            out = spacecmd.package.do_package_removeorphans(shell, "")

        assert shell.client.packages.removePackage.called
        assert mprint.called
        assert out is 0
        assert shell.client.channel.software.listPackagesWithoutChannel.called

        exp = [
            'Packages',
            '--------',
            'vim-0.1-42:5.x86_64\nvim-data-0.2-43.x86_64\nvim-plugins-1.17-16.x86_64'
        ]

        for call in mprint.call_args_list:
            assert_expect([call], next(iter(exp)))
            exp.pop(0)
        assert not exp

    def test_package_listinstallsystems_noarg(self, shell):
        """
        Test do_package_listinstallsystems without arguments.

            :param self:
            :param shell:
        """
        shell.help_package_listinstalledsystems = MagicMock()
        shell.do_package_search = MagicMock()
        shell.get_package_id = MagicMock()
        shell.client.system.listSystemsWithPackage = MagicMock()
        shell.SEPARATOR = "-" * 10

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_listinstalledsystems(shell, "")

        assert not shell.do_package_search.called
        assert not shell.get_package_id.called
        assert not shell.client.system.listSystemsWithPackage.called
        assert not mprint.called
        assert not logger.warning.called
        assert shell.help_package_listinstalledsystems.called

    def test_package_listinstallsystems_package_not_found(self, shell):
        """
        Test do_package_listinstallsystems with not found package

            :param self:
            :param shell:
        """
        shell.help_package_listinstalledsystems = MagicMock()
        shell.do_package_search = MagicMock(return_value=[])
        shell.get_package_id = MagicMock()
        shell.client.system.listSystemsWithPackage = MagicMock()
        shell.SEPARATOR = "-" * 10

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_listinstalledsystems(shell, "darth-vader")

        assert not shell.get_package_id.called
        assert not shell.client.system.listSystemsWithPackage.called
        assert not shell.help_package_listinstalledsystems.called
        assert not mprint.called
        assert logger.warning.called
        assert shell.do_package_search.called

        assert_expect(logger.warning.call_args_list, "No packages found")

    def test_package_listinstallsystems_few_packages(self, shell):
        """
        Test do_package_listinstallsystems with few packages.

            :param self:
            :param shell:
        """
        shell.help_package_listinstalledsystems = MagicMock()
        shell.do_package_search = MagicMock(return_value=["emacs", "xemacs", "spacemacs"])
        shell.get_package_id = MagicMock(side_effect=[
            ["emacs-id-1", "emacs-id-2", "emacs-id-3"],
            ["xemacs-id-1", "xemacs-id-2", "xemacs-id-3"],
            ["spacemacs-id-1", "spacemacs-id-2", "spacemacs-id-3"]
        ])
        shell.client.system.listSystemsWithPackage = MagicMock(side_effect=[
            [
                {"name": "web.foo.com", "id": 1000010000},
                {"name": "bar.foo.com", "id": 1000010001},
                {"name": "fred.foo.com", "id": 1000010002},
            ],
            [
                {"name": "web.foo.com", "id": 1000010000},
                {"name": "bar.foo.com", "id": 1000010001},
            ],
            [
                {"name": "web.foo.com", "id": 1000010000},
            ],
            [
                {"name": "web.foo.com", "id": 1000010000},
                {"name": "bar.foo.com", "id": 1000010001},
                {"name": "fred.foo.com", "id": 1000010002},
            ],
            [
                {"name": "web.foo.com", "id": 1000010000},
                {"name": "bar.foo.com", "id": 1000010001},
            ],
            [
                {"name": "web.foo.com", "id": 1000010000},
            ],
            [
                {"name": "web.foo.com", "id": 1000010000},
                {"name": "bar.foo.com", "id": 1000010001},
                {"name": "fred.foo.com", "id": 1000010002},
            ],
            [
                {"name": "web.foo.com", "id": 1000010000},
                {"name": "bar.foo.com", "id": 1000010001},
            ],
            [
                {"name": "web.foo.com", "id": 1000010000},
            ],
        ])

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_listinstalledsystems(shell, "darth-vader")

        assert not shell.help_package_listinstalledsystems.called
        assert not logger.warning.called
        assert shell.get_package_id.called
        assert shell.client.system.listSystemsWithPackage.called
        assert mprint.called
        assert shell.do_package_search.called

        exp = [
            'emacs',
            '-----',
            'bar.foo.com : 1000010001\nbar.foo.com : 1000010001\nfred.foo.com : 1000010002\n'
            'web.foo.com : 1000010000\nweb.foo.com : 1000010000\nweb.foo.com : 1000010000',
            '----------',
            'xemacs',
            '------',
            'bar.foo.com : 1000010001\nbar.foo.com : 1000010001\nfred.foo.com : 1000010002\n'
            'web.foo.com : 1000010000\nweb.foo.com : 1000010000\nweb.foo.com : 1000010000',
            '----------',
            'spacemacs',
            '---------',
            "bar.foo.com : 1000010001\nbar.foo.com : 1000010001\nfred.foo.com : 1000010002\n"
            "web.foo.com : 1000010000\nweb.foo.com : 1000010000\nweb.foo.com : 1000010000"
        ]

        for call in mprint.call_args_list:
            assert_expect([call], next(iter(exp)))
            exp.pop(0)
        assert not exp

    def test_package_listerrata_noargs(self, shell):
        """
        Test do_package_listerrata without args.
            :param shell:
            :param args:
        """
        shell.do_package_search = MagicMock()
        shell.client.packages.listProvidingErrata = MagicMock()
        shell.get_package_id = MagicMock()
        shell.help_package_listerrata = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_listerrata(shell, "")

        assert not shell.do_package_search.called
        assert not shell.client.packages.listProvidingErrata.called
        assert not shell.get_package_id.called
        assert shell.help_package_listerrata.called

    def test_package_listerrata_not_found_packages(self, shell):
        """
        Test do_package_listerrata with invalid package names.

            :param shell:
            :param args:
        """
        shell.do_package_search = MagicMock(return_value=[])
        shell.client.packages.listProvidingErrata = MagicMock()
        shell.get_package_id = MagicMock()
        shell.help_package_listerrata = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_listerrata(shell, "iron-man")

        assert not shell.help_package_listerrata.called
        assert not shell.client.packages.listProvidingErrata.called
        assert not shell.get_package_id.called
        assert not mprint.called
        assert logger.warning.called
        assert shell.do_package_search.called
        assert_expect(logger.warning.call_args_list, "No packages found")

    def test_package_listerrata_packages(self, shell):
        """
        Test do_package_listerrata with a package names.

            :param shell:
            :param args:
        """
        shell.do_package_search = MagicMock(return_value=[
            "emacs", "xemacs", "emacs-nox"
        ])
        shell.client.packages.listProvidingErrata = MagicMock(side_effect=[
            [
                {"advisory": "RHBA-2019:4231"},
                {"advisory": "CVE-2019:123-4"},
            ],
            [
                {"advisory": "RHBA-2019:4231"},
                {"advisory": "RHBA-2019:4232"},
                {"advisory": "RHBA-2019:4233"},
            ],
            [
                {"advisory": "CVE-2018:152-5"}
            ]
        ])
        shell.get_package_id = MagicMock(return_value=[
            "bogus-package-id"
        ])
        shell.help_package_listerrata = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_listerrata(shell, "emacs")

        assert not logger.warning.called
        assert not shell.help_package_listerrata.called
        assert shell.client.packages.listProvidingErrata.called
        assert shell.get_package_id.called
        assert mprint.called
        assert shell.do_package_search.called

        expectations = [
            'emacs',
            '-----',
            'CVE-2019:123-4\nRHBA-2019:4231',
            '----------',
            'xemacs',
            '------',
            'RHBA-2019:4231\nRHBA-2019:4232\nRHBA-2019:4233',
            '----------',
            'emacs-nox',
            '---------',
            'CVE-2018:152-5'
        ]

        assert_list_args_expect(mprint.call_args_list, expectations)

    def test_package_listerrata_multiple_packages(self, shell):
        """
        Test do_package_listerrata with two package names.

            :param shell:
            :param args:
        """
        shell.do_package_search = MagicMock(return_value=[
            "xemacs", "emacs-nox"
        ])
        shell.client.packages.listProvidingErrata = MagicMock(side_effect=[
            [
                {"advisory": "RHBA-2019:4231"},
                {"advisory": "RHBA-2019:4232"},
                {"advisory": "RHBA-2019:4233"},
            ],
            [
                {"advisory": "CVE-2018:152-5"}
            ]
        ])
        shell.get_package_id = MagicMock(return_value=[
            "bogus-package-id"
        ])
        shell.help_package_listerrata = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
                patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_listerrata(shell, "xemacs emacs-nox")

        assert not logger.warning.called
        assert not shell.help_package_listerrata.called
        assert shell.client.packages.listProvidingErrata.called
        assert shell.get_package_id.called
        assert mprint.called
        assert shell.do_package_search.called

        expectations = [
            'xemacs',
            '------',
            'RHBA-2019:4231\nRHBA-2019:4232\nRHBA-2019:4233',
            '----------',
            'emacs-nox',
            '---------',
            'CVE-2018:152-5'
        ]

        assert_list_args_expect(mprint.call_args_list, expectations)

    def test_package_listdependencies_noargs(self, shell):
        """
        Test do_packge_listdependencies without arguments.
            :param self:
            :param shell:
        """
        shell.do_package_search = MagicMock()
        shell.help_package_listdependencies = MagicMock()
        shell.get_package_id = MagicMock()
        shell.client.packages.list_dependencies = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_listdependencies(shell, "")

        assert not shell.do_package_search.called
        assert not shell.get_package_id.called
        assert not shell.client.packages.list_dependencies.called
        assert not mprint.called
        assert not logger.warning.called
        assert shell.help_package_listdependencies.called

    def test_package_listdependencies_no_packages_found(self, shell):
        """
        Test do_packge_listdependencies no packages found.
            :param self:
            :param shell:
        """
        shell.do_package_search = MagicMock(return_value=[])
        shell.help_package_listdependencies = MagicMock()
        shell.get_package_id = MagicMock()
        shell.client.packages.list_dependencies = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_listdependencies(shell, "thor")

        assert not shell.get_package_id.called
        assert not shell.client.packages.list_dependencies.called
        assert not mprint.called
        assert not shell.help_package_listdependencies.called
        assert shell.do_package_search.called
        assert logger.warning.called

        assert_expect(logger.warning.call_args_list, "No packages found")

    def test_package_listdependencies_invalid_package(self, shell):
        """
        Test do_packge_listdependencies with invalid packages
            :param self:
            :param shell:
        """
        shell.do_package_search = MagicMock(return_value=[
            "vi", "vim", "gvim", "xvim"
        ])
        shell.help_package_listdependencies = MagicMock()
        shell.get_package_id = MagicMock(return_value=[None])
        shell.client.packages.list_dependencies = MagicMock()

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_listdependencies(shell, "bad-editor")

        assert not shell.client.packages.list_dependencies.called
        assert not mprint.called
        assert not shell.help_package_listdependencies.called
        assert shell.do_package_search.called
        assert logger.warning.called
        assert shell.get_package_id.called

        expectations = [
            'vi is not a valid package',
            'vim is not a valid package',
            'gvim is not a valid package',
            'xvim is not a valid package'
        ]
        assert_list_args_expect(logger.warning.call_args_list,
                                expectations)

    def test_package_listdependencies_packages(self, shell):
        """
        Test do_package_listdependencies with packages
            :param self:
            :param shell:
        """
        shell.do_package_search = MagicMock(return_value=[
            "emacs", "xemacs", "vim", "emacs-x11"  # One should fail here, obviously
        ])
        shell.help_package_listdependencies = MagicMock()
        shell.get_package_id = MagicMock(side_effect=[
            ["1"], ["2"], [None], ["3"]
        ])
        shell.client.packages.list_dependencies = MagicMock(side_effect=[
            [
                {
                    "dependency": "libxml2.so.2",
                    "dependency_type": "requires",
                    "dependency_modifier": "> 1.0"
                },
                {
                    "dependency": "mimehandler(application/x-shellscript)",
                    "dependency_type": "provides",
                    "dependency_modifier": ""
                },
            ],
            [
                {
                    "dependency": "libasound.so.2",
                    "dependency_type": "requires",
                    "dependency_modifier": ""
                },
                {
                    "dependency": "emacs_program",
                    "dependency_type": "provides",
                    "dependency_modifier": "= 24.3-19.2"
                },
            ],
            [
                {
                    "dependency": "libc.so.6",
                    "dependency_type": "requires",
                    "dependency_modifier": ""
                },
                {
                    "dependency": "rpmlib (CompressedFileNames)",
                    "dependency_type": "requires",
                    "dependency_modifier": "<= 3.0.4-1"
                },
                {
                    "dependency": "rpmlib (PayloadFilesHavePrefix)",
                    "dependency_type": "requires",
                    "dependency_modifier": "<= 4.0-1"
                },
                {
                    "dependency": "rpmlib (PayloadIsLzma)",
                    "dependency_type": "requires",
                    "dependency_modifier": "<= 4.4.6-1"
                },
            ],
        ])

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
            patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_listdependencies(shell, "emacs")

        assert not shell.help_package_listdependencies.called
        assert shell.client.packages.list_dependencies.called
        assert mprint.called
        assert shell.do_package_search.called
        assert logger.warning.called
        assert shell.get_package_id.called

        expectations = [
            "Package Name: emacs",
            "Dependency: libxml2.so.2 Type: requires Modifier: > 1.0",
            "Dependency: mimehandler(application/x-shellscript) Type: provides Modifier: ",
            "----------",
            "----------",
            "Package Name: xemacs",
            "Dependency: libasound.so.2 Type: requires Modifier: ",
            "Dependency: emacs_program Type: provides Modifier: = 24.3-19.2",
            "----------",
            "----------",
            "Package Name: emacs-x11",
            "Dependency: libc.so.6 Type: requires Modifier: ",
            "Dependency: rpmlib (CompressedFileNames) Type: requires Modifier: <= 3.0.4-1",
            "Dependency: rpmlib (PayloadFilesHavePrefix) Type: requires Modifier: <= 4.0-1",
            "Dependency: rpmlib (PayloadIsLzma) Type: requires Modifier: <= 4.4.6-1",
            "----------"]
        assert_list_args_expect(mprint.call_args_list, expectations=expectations)
        assert_expect(logger.warning.call_args_list, "vim is not a valid package")

    def test_package_listdependencies_multiple_packages(self, shell):
        """
        Test do_package_listdependencies with two packages
            :param self:
            :param shell:
        """
        shell.do_package_search = MagicMock(return_value=[
            "emacs", "xemacs", "vim", "emacs-x11"
        ])
        shell.help_package_listdependencies = MagicMock()
        shell.get_package_id = MagicMock(side_effect=[
            ["1"], ["2"], ["3"], ["4"]
        ])
        shell.client.packages.list_dependencies = MagicMock(side_effect=[
            [
                {
                    "dependency": "libxml2.so.2",
                    "dependency_type": "requires",
                    "dependency_modifier": "> 1.0"
                },
                {
                    "dependency": "mimehandler(application/x-shellscript)",
                    "dependency_type": "provides",
                    "dependency_modifier": ""
                },
            ],
            [
                {
                    "dependency": "libasound.so.2",
                    "dependency_type": "requires",
                    "dependency_modifier": ""
                },
                {
                    "dependency": "emacs_program",
                    "dependency_type": "provides",
                    "dependency_modifier": "= 24.3-19.2"
                },
            ],
            [
                {
                    "dependency": "libacl.so.1",
                    "dependency_type": "requires",
                    "dependency_modifier": ""
                },
                {
                    "dependency": "libc.so.6",
                    "dependency_type": "requires",
                    "dependency_modifier": ""
                },
            ],
            [
                {
                    "dependency": "libc.so.6",
                    "dependency_type": "requires",
                    "dependency_modifier": ""
                },
                {
                    "dependency": "rpmlib (CompressedFileNames)",
                    "dependency_type": "requires",
                    "dependency_modifier": "<= 3.0.4-1"
                },
                {
                    "dependency": "rpmlib (PayloadFilesHavePrefix)",
                    "dependency_type": "requires",
                    "dependency_modifier": "<= 4.0-1"
                },
                {
                    "dependency": "rpmlib (PayloadIsLzma)",
                    "dependency_type": "requires",
                    "dependency_modifier": "<= 4.4.6-1"
                },
            ],
        ])

        mprint = MagicMock()
        logger = MagicMock()
        with patch("spacecmd.package.print", mprint) as prn, \
                patch("spacecmd.package.logging", logger) as lgr:
            spacecmd.package.do_package_listdependencies(shell, "emacs vim")

        assert not shell.help_package_listdependencies.called
        assert shell.client.packages.list_dependencies.called
        assert mprint.called
        assert shell.do_package_search.called
        assert not logger.warning.called
        assert shell.get_package_id.called

        expectations = [
            "Package Name: emacs",
            "Dependency: libxml2.so.2 Type: requires Modifier: > 1.0",
            "Dependency: mimehandler(application/x-shellscript) Type: provides Modifier: ",
            "----------",
            "----------",
            "Package Name: xemacs",
            "Dependency: libasound.so.2 Type: requires Modifier: ",
            "Dependency: emacs_program Type: provides Modifier: = 24.3-19.2",
            "----------",
            "----------",
            "Package Name: vim",
            "Dependency: libacl.so.1 Type: requires Modifier: ",
            "Dependency: libc.so.6 Type: requires Modifier: ",
            "----------",
            "----------",
            "Package Name: emacs-x11",
            "Dependency: libc.so.6 Type: requires Modifier: ",
            "Dependency: rpmlib (CompressedFileNames) Type: requires Modifier: <= 3.0.4-1",
            "Dependency: rpmlib (PayloadFilesHavePrefix) Type: requires Modifier: <= 4.0-1",
            "Dependency: rpmlib (PayloadIsLzma) Type: requires Modifier: <= 4.4.6-1",
            "----------"]
        assert_list_args_expect(mprint.call_args_list, expectations=expectations)
