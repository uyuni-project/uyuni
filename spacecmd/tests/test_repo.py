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
    
