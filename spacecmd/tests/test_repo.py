# coding: utf-8
"""
Test suite for spacecmd.repo module
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.repo


class TestSCRepo:
    """
    Test suite for "repo" module.
    """
    def test_repo_list_noret(self, shell):
        """
        Test do_repo_list with no data return.

        :param shell:
        :return:
        """
        shell.client.channel.software.listUserRepos = MagicMock(return_value=[
            {"label": "v-repo-one"}, {"label": "z-repo-two"}, {"label": "a-repo-three"}
        ])
        mprint = MagicMock()
        with patch("spacecmd.repo.print", mprint):
            out = spacecmd.repo.do_repo_list(shell, "", doreturn=False)

        assert shell.client.channel.software.listUserRepos.called
        assert mprint.called
        assert out is None

        assert_expect(mprint.call_args_list, 'a-repo-three\nv-repo-one\nz-repo-two')

    def test_repo_list_ret_data(self, shell):
        """
        Test do_repo_list with data return.

        :param shell:
        :return:
        """
        shell.client.channel.software.listUserRepos = MagicMock(return_value=[
            {"label": "v-repo-one"}, {"label": "z-repo-two"}, {"label": "a-repo-three"}
        ])
        mprint = MagicMock()
        with patch("spacecmd.repo.print", mprint):
            out = spacecmd.repo.do_repo_list(shell, "", doreturn=True)

        assert not mprint.called
        assert shell.client.channel.software.listUserRepos.called
        assert out is not None
        assert len(out) == 3
        assert out == ["v-repo-one", "z-repo-two", "a-repo-three"]

    def test_repo_details_noarg(self, shell):
        """
        Test do_repo_details with no arguments passed.

        :param shell:
        :return:
        """
        shell.client.channel.software.getRepoDetails = MagicMock()
        shell.help_repo_details = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.repo.print", mprint):
            out = spacecmd.repo.do_repo_details(shell, "")
        assert out is None
        assert not mprint.called
        assert not shell.client.channel.software.getRepoDetails.called
        assert shell.help_repo_details.called

    def test_repo_details_no_repos_found(self, shell):
        """
        Test do_repo_details no repos found.

        :param shell:
        :return:
        """
        shell.client.channel.software.getRepoDetails = MagicMock()
        shell.do_repo_list = MagicMock(return_value=[])
        shell.help_repo_details = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.repo.print", mprint):
            out = spacecmd.repo.do_repo_details(shell, "non-existing-repo")

        assert not shell.client.channel.software.getRepoDetails.called
        assert not shell.help_repo_details.called
        assert out is None
        assert mprint.called

        assert_expect(mprint.call_args_list,
                      "No repositories found for 'non-existing-repo' query")

    def test_repo_details_repo_data(self, shell):
        """
        Test do_repo_details for repo data.

        :param shell:
        :return:
        """
        shell.client.channel.software.getRepoDetails = MagicMock(side_effect=[
            {
                "label": "some-repository", "sourceUrl": "http://somehost/somerepo",
                "type": "yum"
            },
            {
                "label": "some-other-repository", "sourceUrl": "file:///tmp/someotherrepo",
                "type": "zypper", "sslCaDesc": "Ca Descr", "sslCertDesc": "Cert descr",
                "sslKeyDesc": "Key descr"
            },
        ])
        shell.do_repo_list = MagicMock(return_value=["some-repo", "some-other-repo"])
        shell.help_repo_details = MagicMock()
        mprint = MagicMock()
        with patch("spacecmd.repo.print", mprint):
            out = spacecmd.repo.do_repo_details(shell, "some*")

        assert not shell.help_repo_details.called
        assert shell.client.channel.software.getRepoDetails.called
        assert out is None
        assert mprint.called

        exp = [
            'Repository Label:                  some-repository',
            'Repository URL:                    http://somehost/somerepo',
            'Repository Type:                   yum',
            'Repository SSL Ca Certificate:     None',
            'Repository SSL Client Certificate: None',
            'Repository SSL Client Key:         None',
            '----------',
            'Repository Label:                  some-other-repository',
            'Repository URL:                    file:///tmp/someotherrepo',
            'Repository Type:                   zypper',
            'Repository SSL Ca Certificate:     Ca Descr',
            'Repository SSL Client Certificate: Cert descr',
            'Repository SSL Client Key:         Key descr'
        ]
        assert_list_args_expect(mprint.call_args_list, exp)

    def test_repo_listfilters_noargs(self, shell):
        """
        Test do_repo_listfilters without arguments

        :param shell:
        :return:
        """
        shell.help_repo_listfilters = MagicMock()
        shell.client.channel.software.listRepoFilters = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.repo.print", mprint):
            out = spacecmd.repo.do_repo_listfilters(shell, "")

        assert out is None
        assert not mprint.called
        assert not shell.client.channel.software.listRepoFilters.called
        assert shell.help_repo_listfilters.called

    def test_repo_listfilters_not_found(self, shell):
        """
        Test do_repo_listfilters no filters found.

        :param shell:
        :return:
        """
        shell.help_repo_listfilters = MagicMock()
        shell.client.channel.software.listRepoFilters = MagicMock(return_value=[])
        mprint = MagicMock()

        with patch("spacecmd.repo.print", mprint):
            out = spacecmd.repo.do_repo_listfilters(shell, "some-filter")

        assert out is None
        assert not shell.help_repo_listfilters.called
        assert shell.client.channel.software.listRepoFilters.called
        assert mprint.called

        assert_expect(mprint.call_args_list, "No filters found")
