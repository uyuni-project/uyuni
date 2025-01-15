# coding: utf-8
"""
Test software channel module.
"""

from mock import Mock, MagicMock, patch, call
import spacecmd.softwarechannel
from helpers import shell, assert_expect, assert_list_args_expect
import pytest
import rpm


def test_softwarechannel_list_doreturn_nolabels(shell):
    """
    Test do_softwarechannel_list no labels, return data.

    :param shell:
    :return:
    """
    shell.client.channel.listAllChannels = MagicMock(return_value=[])
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_list(shell, "", doreturn=True)
    assert out is not None
    assert out == []
    assert not shell.help_softwarechannel_list.called
    assert not shell.client.channel.software.getDetails.called
    assert not shell.list_child_channels.called
    assert not shell.list_base_channels.called

def test_softwarechannel_list_noreturn_nolabels(shell):
    """
    Test do_softwarechannel_list no labels, no return data.

    :param shell:
    :return:
    """
    shell.client.channel.listAllChannels = MagicMock(return_value=[])
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_list(shell, "", doreturn=False)
    assert out is None
    assert not shell.help_softwarechannel_list.called
    assert not shell.client.channel.software.getDetails.called
    assert not shell.list_child_channels.called
    assert not shell.list_base_channels.called

def test_softwarechannel_list_noreturn_labels_std(shell):
    """
    Test do_softwarechannel_list with label, no return data, standard output.

    :param shell:
    :return:
    """
    shell.client.channel.listAllChannels = MagicMock(return_value=[
        {"label": "label-one"}, {"label": "label-last"},
        {"label": "label-two"}, {"label": "label-three"},
    ])

    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_list(
            shell, "", doreturn=False)

    assert out is None
    assert not shell.help_softwarechannel_list.called
    assert not shell.client.channel.software.getDetails.called
    assert not shell.list_child_channels.called
    assert shell.client.channel.listAllChannels.called

    assert_list_args_expect(mprint.call_args_list,
                            ["label-last", "label-one", "label-three", "label-two"])

def test_softwarechannel_list_noreturn_labels_verbose(shell):
    """
    Test do_softwarechannel_list with label, no return data, verbose output.

    :param shell:
    :return:
    """
    shell.client.channel.listAllChannels = MagicMock(return_value=[
        {"label": "test_channel"},
    ])
    shell.client.channel.software.getDetails = MagicMock(side_effect=[
        {"summary": "Summary of test_channel"},
        {"summary": "Summary of child_channel"},
    ])
    shell.list_child_channels = MagicMock(return_value=[
        "child_channel"
    ])

    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_list(
            shell, "-v", doreturn=False)

    assert out is None
    assert not shell.help_softwarechannel_list.called
    assert not shell.list_child_channels.called
    assert shell.client.channel.software.getDetails.called
    assert shell.client.channel.listAllChannels.called

    assert_expect(mprint.call_args_list,
                    'test_channel : Summary of test_channel')

def test_softwarechannel_list_noreturn_labels_verbose_tree(shell):
    """
    Test do_softwarechannel_list with label, no return data, verbose output with tree.

    :param shell:
    :return:
    """
    shell.client.channel.listAllChannels = MagicMock(return_value=[
        {"any_channel": "any_channel"},
    ])
    shell.client.channel.software.getDetails = MagicMock(side_effect=[
        {"summary": "Summary of test_channel"},
        {"summary": "Summary of child_channel"},
    ])
    shell.list_child_channels = MagicMock(return_value=[
        "child_channel"
    ])

    shell.list_base_channels = MagicMock(return_value=[
        "base_channel"
    ])

    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_list(
            shell, "-v -t", doreturn=False)

    assert out is None
    assert not shell.client.channel.listAllChannels.called
    assert not shell.help_softwarechannel_list.called
    assert shell.list_child_channels.called
    assert shell.client.channel.software.getDetails.called

    assert_list_args_expect(mprint.call_args_list,
                            ['base_channel : Summary of test_channel',
                                ' |-child_channel : Summary of child_channel'])

def test_softwarechannel_listmanageablechannels_noarg(shell):
    """
    Test do_softwarechannel_listmanageablechannels without arguments.

    :param shell:
    :return:
    """
    shell.client.channel.listManageableChannels = MagicMock(return_value=[
        {"label": "x_channel"},
        {"label": "z_channel"},
        {"label": "a_channel"},
    ])

    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listmanageablechannels(shell, "")

    assert out is None
    assert not shell.client.channel.software.getDetails.called
    assert shell.client.channel.listManageableChannels.called
    assert_list_args_expect(mprint.call_args_list,
                            ["a_channel", "x_channel", "z_channel"])

def test_softwarechannel_listmanageablechannels_default_verbose(shell):
    """
    Test do_softwarechannel_listmanageablechannels with verbose arg (all).

    :param shell:
    :return:
    """
    shell.client.channel.listManageableChannels = MagicMock(return_value=[
        {"label": "x_channel"},
        {"label": "z_channel"},
        {"label": "b_channel"},
        {"label": "a_channel"},
    ])
    shell.client.channel.software.getDetails = MagicMock(side_effect=[
        {"summary": "A summary"},
        {"summary": "B summary"},
        {"summary": "X summary"},
        {"summary": "Z summary"},
    ])

    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listmanageablechannels(
            shell, "--verbose")

    assert out is None
    assert shell.client.channel.software.getDetails.called
    assert shell.client.channel.listManageableChannels.called
    assert_list_args_expect(mprint.call_args_list,
                            ["a_channel : A summary",
                                "b_channel : B summary",
                                "x_channel : X summary",
                                "z_channel : Z summary"])

def test_softwarechannel_listmanageablechannels_data_sparse(shell):
    """
    Test do_softwarechannel_listmanageablechannels data out, short.

    :param shell:
    :return:
    """
    shell.client.channel.listManageableChannels = MagicMock(return_value=[
        {"label": "x_channel"},
        {"label": "z_channel"},
        {"label": "a_channel"},
    ])

    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listmanageablechannels(shell, "", doreturn=True)

    assert out is not None
    assert not shell.client.channel.software.getDetails.called
    assert shell.client.channel.listManageableChannels.called
    assert out == ["a_channel", "x_channel", "z_channel"]

def test_softwarechannel_listmanageablechannels_data_verbose(shell):
    """
    Test do_softwarechannel_listmanageablechannels with verbose arg (all).

    :param shell:
    :return:
    """
    shell.client.channel.listManageableChannels = MagicMock(return_value=[
        {"label": "x_channel"},
        {"label": "z_channel"},
        {"label": "b_channel"},
        {"label": "a_channel"},
    ])
    shell.client.channel.software.getDetails = MagicMock(side_effect=[
        {"summary": "A summary"},
        {"summary": "B summary"},
        {"summary": "X summary"},
        {"summary": "Z summary"},
    ])

    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listmanageablechannels(
            shell, "--verbose", doreturn=True)

    assert out is not None
    assert not shell.client.channel.software.getDetails.called
    assert shell.client.channel.listManageableChannels.called
    assert out == ["a_channel", "b_channel", "x_channel", "z_channel"]

def test_listchildchannels(shell):
    """
    Test do_softwarechannel_listchildchannels noargs.

    :param shell:
    :return:
    """
    shell.list_child_channels = MagicMock(return_value=["x_child_channel", "z_child_channel",
                                                        "b_child_channel", "a_child_channel",])
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        spacecmd.softwarechannel.do_softwarechannel_listchildchannels(shell, "")

    assert not shell.client.channel.software.getDetails.called
    assert_list_args_expect(mprint.call_args_list,
                            ['a_child_channel\nb_child_channel\nx_child_channel\nz_child_channel'])

def test_listchildchannels_verbose(shell):
    """
    Test do_softwarechannel_listchildchannels verbose.

    :param shell:
    :return:
    """
    shell.list_child_channels = MagicMock(return_value=["x_child_channel", "z_child_channel",
                                                        "b_child_channel", "a_child_channel",])
    shell.client.channel.software.getDetails = MagicMock(side_effect=[
        {"summary": "A summary"},
        {"summary": "B summary"},
        {"summary": "X summary"},
        {"summary": "Z summary"},
    ])
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        spacecmd.softwarechannel.do_softwarechannel_listchildchannels(shell, "--verbose")

    assert shell.client.channel.software.getDetails.called
    assert_list_args_expect(mprint.call_args_list,
                            ['a_child_channel : A summary',
                                'b_child_channel : B summary',
                                'x_child_channel : X summary',
                                'z_child_channel : Z summary'])

def test_listsystems_noargs(shell):
    """
    Test do_softwarechannel_listsystems no args.

    :param shell:
    :return:
    """
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listsystems(shell, "")

    assert out is None
    assert not mprint.called
    assert not shell.client.channel.software.listSubscribedSystems.called
    assert shell.help_softwarechannel_listsystems.called

def test_listsystems_noargs_channel_no_data(shell):
    """
    Test do_softwarechannel_listsystems one channel, no data.

    :param shell:
    :return:
    """
    shell.client.channel.software.listSubscribedSystems = MagicMock(return_value=[
        {"name": "one.acme.lan"},
        {"name": "two.acme.lan"},
        {"name": "zetta.acme.lan"},
        {"name": "third.zoo.lan"},
    ])
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listsystems(shell, "my_channel")

    assert not shell.help_softwarechannel_listsystems.called
    assert out is None
    assert mprint.called
    assert shell.client.channel.software.listSubscribedSystems.called
    assert_list_args_expect(mprint.call_args_list,
                            ['one.acme.lan\nthird.zoo.lan\ntwo.acme.lan\nzetta.acme.lan'])

def test_listsystems_noargs_channel_data_return(shell):
    """
    Test do_softwarechannel_listsystems one channel, data return.

    :param shell:
    :return:
    """
    shell.client.channel.software.listSubscribedSystems = MagicMock(return_value=[
        {"name": "one.acme.lan"},
        {"name": "two.acme.lan"},
        {"name": "zetta.acme.lan"},
        {"name": "third.zoo.lan"},
    ])
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listsystems(shell, "my_channel", doreturn=True)

    assert not shell.help_softwarechannel_listsystems.called
    assert not mprint.called
    assert out is not None
    assert out == ['one.acme.lan', 'third.zoo.lan', 'two.acme.lan', 'zetta.acme.lan']

def test_listpackages_noargs_nodata(shell):
    """
    Test do_softwarechannel_listpackages no args. No data return.

    :param shell:
    :return:
    """
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listpackages(shell, "")

    assert out is None
    assert not shell.client.channel.software.listLatestPackages.called
    assert shell.help_softwarechannel_listpackages.called

def test_listpackages_too_much_args_nodata(shell):
    """
    Test do_softwarechannel_listpackages with too much much arguments.

    :param shell:
    :return:
    """
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listpackages(
            shell, "one_channel other_channel somemore_channels")

    assert out is None
    assert not shell.client.channel.software.listLatestPackages.called
    assert shell.help_softwarechannel_listpackages.called

def test_listpackages_one_channel_no_data(shell):
    """
    Test do_softwarechannel_listpackages with one channel. No data return.

    :param shell:
    :return:
    """
    shell.client.channel.software.listLatestPackages = MagicMock(
        return_value=[
            {"name": "emacs", "version": "42.0",
                "release": "9", "epoch": "", "arch": "x86_64"},
            {"name": "emacs-nox", "version": "42.0",
                "release": "10", "epoch": "", "arch_label": "x86_64"},
            {"name": "tiff", "version": "1.0",
                "release": "11", "epoch": "3", "arch": "amd64"},
        ]
    )
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listpackages(shell, "one_channel")

    assert out is None
    assert not shell.help_softwarechannel_listpackages.called
    assert shell.client.channel.software.listLatestPackages.called
    assert_list_args_expect(mprint.call_args_list,
                            ['emacs-42.0-9.x86_64\nemacs-nox-42.0-10.x86_64\ntiff-1.0-11:3.x86_64'])

def test_listpackages_one_channel_with_data(shell):
    """
    Test do_softwarechannel_listpackages with one channel. With data return.

    :param shell:
    :return:
    """
    shell.client.channel.software.listLatestPackages = MagicMock(
        return_value=[
            {"name": "emacs", "version": "42.0",
                "release": "9", "epoch": "", "arch": "x86_64"},
            {"name": "emacs-nox", "version": "42.0",
                "release": "10", "epoch": "", "arch_label": "x86_64"},
            {"name": "tiff", "version": "1.0",
                "release": "11", "epoch": "3", "arch": "amd64"},
        ]
    )
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listpackages(
            shell, "one_channel", doreturn=True)

    assert out is not None
    assert not shell.help_softwarechannel_listpackages.called
    assert shell.client.channel.software.listLatestPackages.called
    assert out == ['emacs-42.0-9.x86_64', 'emacs-nox-42.0-10.x86_64',
                    'tiff-1.0-11:3.x86_64']

def test_listallpackages_noargs_nodata(shell):
    """
    Test do_softwarechannel_listallpackages no args. No data return.

    :param shell:
    :return:
    """
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listallpackages(shell, "")

    assert out is None
    assert not shell.client.channel.software.listAllPackages.called
    assert shell.help_softwarechannel_listallpackages.called

def test_listallpackages_too_much_args_nodata(shell):
    """
    Test do_softwarechannel_listallpackages with too much much arguments.

    :param shell:
    :return:
    """
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listallpackages(
            shell, "one_channel other_channel somemore_channels")

    assert out is None
    assert not shell.client.channel.software.listAllPackages.called
    assert shell.help_softwarechannel_listallpackages.called

def test_listallpackages_one_channel_no_data(shell):
    """
    Test do_softwarechannel_listallpackages with one channel. No data return.

    :param shell:
    :return:
    """
    shell.client.channel.software.listAllPackages = MagicMock(
        return_value=[
            {"name": "emacs", "version": "42.0",
                "release": "9", "epoch": "", "arch": "x86_64"},
            {"name": "emacs-nox", "version": "42.0",
                "release": "10", "epoch": "", "arch_label": "x86_64"},
            {"name": "tiff", "version": "1.0",
                "release": "11", "epoch": "3", "arch": "amd64"},
        ]
    )
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listallpackages(shell, "one_channel")

    assert out is None
    assert not shell.help_softwarechannel_listallpackages.called
    assert shell.client.channel.software.listAllPackages.called
    assert_list_args_expect(mprint.call_args_list,
                            ['emacs-42.0-9.x86_64\nemacs-nox-42.0-10.x86_64\ntiff-1.0-11:3.x86_64'])

def test_listallpackages_one_channel_with_data(shell):
    """
    Test do_softwarechannel_listallpackages with one channel. With data return.

    :param shell:
    :return:
    """
    shell.client.channel.software.listAllPackages = MagicMock(
        return_value=[
            {"name": "emacs", "version": "42.0",
                "release": "9", "epoch": "", "arch": "x86_64"},
            {"name": "emacs-nox", "version": "42.0",
                "release": "10", "epoch": "", "arch_label": "x86_64"},
            {"name": "tiff", "version": "1.0",
                "release": "11", "epoch": "3", "arch": "amd64"},
        ]
    )
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listallpackages(
            shell, "one_channel", doreturn=True)

    assert out is not None
    assert not shell.help_softwarechannel_listallpackages.called
    assert shell.client.channel.software.listAllPackages.called
    assert out == ['emacs-42.0-9.x86_64', 'emacs-nox-42.0-10.x86_64',
                    'tiff-1.0-11:3.x86_64']

@pytest.mark.skipif(not hasattr(rpm, "labelCompare"), reason="Full RPM bindings required")
def test_filter_latest_packages():
    """
    Test filter_latest_packages function.

    :return:
    """
    data = [
        {"name": "emacs", "version": "42.0",
            "release": "9", "epoch": "", "arch": "x86_64"},
        {"name": "emacs", "version": "42.0",
            "release": "10", "epoch": "", "arch_label": "x86_64"},
        {"name": "emacs", "version": "42.0",
            "release": "8", "epoch": "", "arch": "x86_64"},
        {"name": "emacs", "version": "42.1",
            "release": "7", "epoch": "", "arch": "x86_64"},
        {"name": "emacs", "version": "41.9",
            "release": "11", "epoch": "", "arch_label": "x86_64"},
    ]
    out = list(spacecmd.softwarechannel.filter_latest_packages(data))
    assert len(out) == 1
    res = out[0]

    assert res["release"] == "7"
    assert res["version"] == "42.1"
    assert res["name"] == "emacs"
    assert res["arch"] == "x86_64"
    assert res["epoch"] == ""

def test_listlatestpackages_noargs_nodata(shell):
    """
    Test do_softwarechannel_listlatestpackages without args. No data return.

    :return:
    """
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listlatestpackages(shell, "")

    assert out is None
    assert not shell.client.channel.software.listAllPackages.called
    assert shell.help_softwarechannel_listlatestpackages.called

def test_listlatestpackages_wrongargs_nodata(shell):
    """
    Test do_softwarechannel_listlatestpackages with wrong amount of args. No data return.

    :return:
    """
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listlatestpackages(
            shell, "one two three")

    assert out is None
    assert not shell.client.channel.software.listAllPackages.called
    assert shell.help_softwarechannel_listlatestpackages.called

def test_listlatestpackages_channel_packages(shell):
    """
    Test do_softwarechannel_listlatestpackages with channel supplied.

    :return:
    """
    shell.client.channel.software.listAllPackages = MagicMock(
        return_value=[
            {"name": "emacs", "version": "42.0",
                "release": "9", "epoch": "", "arch": "x86_64"},
            {"name": "emacs-nox", "version": "42.0",
                "release": "10", "epoch": "", "arch_label": "x86_64"},
            {"name": "tiff", "version": "1.0",
                "release": "11", "epoch": "3", "arch": "amd64"},
        ]
    )
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listlatestpackages(
            shell, "some_channel")

    assert out is None
    assert not shell.help_softwarechannel_listlatestpackages.called
    assert shell.client.channel.software.listAllPackages.called

    assert_list_args_expect(mprint.call_args_list,
                            ['emacs-42.0-9.x86_64\nemacs-nox-42.0-10.x86_64\ntiff-1.0-11:3.x86_64'])

def test_listlatestpackages_channel_packages_as_data(shell):
    """
    Test do_softwarechannel_listlatestpackages with channel supplied. Return as data.

    :return:
    """
    shell.client.channel.software.listAllPackages = MagicMock(
        return_value=[
            {"name": "emacs", "version": "42.0",
                "release": "9", "epoch": "", "arch": "x86_64"},
            {"name": "emacs-nox", "version": "42.0",
                "release": "10", "epoch": "", "arch_label": "x86_64"},
            {"name": "tiff", "version": "1.0",
                "release": "11", "epoch": "3", "arch": "amd64"},
        ]
    )
    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_listlatestpackages(
            shell, "some_channel", doreturn=True)

    assert out is not None
    assert not shell.help_softwarechannel_listlatestpackages.called
    assert shell.client.channel.software.listAllPackages.called

    assert out == ['emacs-42.0-9.x86_64', 'emacs-nox-42.0-10.x86_64', 'tiff-1.0-11:3.x86_64']

def test_softwarechannel_diff(shell):
    """
    Test that do_softwarechannel_diff function prints correct output
    :param shell: SpacewalkShell
    :return: None
    """

    shell.dump_softwarechannel = Mock(
        side_effect=[["hwdata-0.314-10.9.1.noarch"], ["koan-2.4.2-6.34.noarch"]]
    )

    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_diff(
            shell, "source_channel target_channel"
        )

    assert out is None
    assert_list_args_expect(
        mprint.call_args_list,
        [
            "--- source_channel\n",
            "+++ target_channel\n",
            "@@ -1 +1 @@\n",
            "-hwdata-0.314-10.9.1.noarch",
            "+koan-2.4.2-6.34.noarch",
        ],
    )

def test_softwarechannel_errata_diff(shell):
    """
    Test that do_softwarechannel_errata_diff function prints correct output
    :param shell: SpacewalkShell
    :return: None
    """

    shell.dump_softwarechannel_errata = Mock(
        side_effect=[
            ["SUSE-12-2021-607 important: Security update for python-Jinja2"],
            ["SUSE-12-2021-3660 Recommended update for NetworkManager"],
        ]
    )

    mprint = MagicMock()
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_errata_diff(
            shell, "source_channel target_channel"
        )

    assert out is None
    assert_list_args_expect(
        mprint.call_args_list,
        [
            "--- source_channel\n",
            "+++ target_channel\n",
            "@@ -1 +1 @@\n",
            "-SUSE-12-2021-607 important: Security update for python-Jinja2",
            "+SUSE-12-2021-3660 Recommended update for NetworkManager",
        ],
    )


def test_softwarechannel_removepackages(shell):
    packages_list = [
        {
            "id": 21708,
            "name": "emacs",
            "version": "42.0",
            "release": "9",
            "epoch": "",
            "arch": "x86_64",
        },
        {
            "id": 21742,
            "name": "emacs-nox",
            "version": "42.0",
            "release": "10",
            "epoch": "",
            "arch_label": "x86_64",
        },
        {
            "id": 12969,
            "name": "tiff",
            "version": "1.0",
            "release": "11",
            "epoch": "3",
            "arch": "amd64",
        },
    ]

    mprint = MagicMock()
    shell.client.channel.software.listAllPackages = MagicMock(
        return_value=packages_list
    )
    with patch("spacecmd.softwarechannel.print", mprint):
        out = spacecmd.softwarechannel.do_softwarechannel_removepackages(
            shell, "some_channel emacs-42.0-9.x86_64"
        )
    assert out == 0
    # It's not good to check output, but the actual call includes so many mocked
    # functions that it's close to meaningless to check those
    assert call("emacs-42.0-9.x86_64") in mprint.call_args_list
