# coding: utf-8
"""
Test suite for spacecmd.package module.
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect
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
            'Name:    emacs',
            'Version: 24.5',
            'Release: 42',
            'Epoch:   1',
            'Arch:    x86',
            '',
            'File:    emacs.rpm',
            'Path:    /tmp',
            'Size:    2000',
            'MD5:     aaa0919fe05c15583b688fed115d1ab8',
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
            'Name:    emacs-x11',
            'Version: 24.5.7',
            'Release: 42.1',
            'Epoch:   2',
            'Arch:    x86',
            '',
            'File:    emacs-x11.rpm',
            'Path:    /tmp',
            'Size:    22000',
            'MD5:     9d188ed99c1114eba7a8e499798da47c',
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
            'Name:    emacs-data',
            'Version: 24.5.7',
            'Release: 42.1',
            'Epoch:   2',
            'Arch:    x86',
            '',
            'File:    emacs-x11.rpm',
            'Path:    /tmp',
            'Size:    22000',
            'MD5:     9d188ed99c1114eba7a8e499798da47c',
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
            'Name:    emacs-melpa',
            'Version: 24.5.7',
            'Release: 42.1',
            'Epoch:   2',
            'Arch:    x86',
            '',
            'File:    emacs-x11.rpm',
            'Path:    /tmp',
            'Size:    22000',
            'MD5:     9d188ed99c1114eba7a8e499798da47c',
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