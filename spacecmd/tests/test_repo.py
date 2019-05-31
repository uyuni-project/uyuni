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

    def test_repo_listfilters_stdout(self, shell):
        """
        Test do_repo_listfilters stdout check

        :param shell:
        :return:
        """
        shell.help_repo_listfilters = MagicMock()
        shell.client.channel.software.listRepoFilters = MagicMock(return_value=[
            {"flag": "+", "filter": "stuff"},
            {"flag": "-", "filter": "other"},
        ])
        mprint = MagicMock()

        with patch("spacecmd.repo.print", mprint):
            out = spacecmd.repo.do_repo_listfilters(shell, "some-filter")

        assert out is None
        assert not shell.help_repo_listfilters.called
        assert shell.client.channel.software.listRepoFilters.called
        assert mprint.called

        assert_list_args_expect(mprint.call_args_list, ['+stuff', '-other'])

    def test_repo_addfilters_noargs(self, shell):
        """
        Test do_repo_addfilters no arguments.

        :param shell:
        :return:
        """
        shell.help_repo_addfilters = MagicMock()
        shell.client.channel.software.addRepoFilter = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.repo.print", mprint):
            out = spacecmd.repo.do_repo_addfilters(shell, "")

        assert out is None
        assert not mprint.called
        assert not shell.client.channel.software.addRepoFilter.called
        assert shell.help_repo_addfilters.called

    def test_repo_addfilters_argcheck_repo_only(self, shell):
        """
        Test do_repo_addfilters check arguments: repo only

        :param shell:
        :return:
        """
        shell.help_repo_addfilters = MagicMock()
        shell.client.channel.software.addRepoFilter = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.repo.print", mprint):
            out = spacecmd.repo.do_repo_addfilters(shell, "some-repo")

        assert out is None
        assert not mprint.called
        assert not shell.client.channel.software.addRepoFilter.called
        assert shell.help_repo_addfilters.called

    def test_repo_addfilters_argcheck_wrong_filter(self, shell):
        """
        Test do_repo_addfilters check arguments: wrong filter syntax

        :param shell:
        :return:
        """
        shell.help_repo_addfilters = MagicMock()
        shell.client.channel.software.addRepoFilter = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_addfilters(shell, "some-repo foo bar")

        assert out is None
        assert not mprint.called
        assert not shell.help_repo_addfilters.called
        assert not shell.client.channel.software.addRepoFilter.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list, 'Each filter must start with + or -')

    def test_repo_addfilters_argcheck_add_filter(self, shell):
        """
        Test do_repo_addfilters add filter

        :param shell:
        :return:
        """
        shell.help_repo_addfilters = MagicMock()
        shell.client.channel.software.addRepoFilter = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_addfilters(shell, "some-repo +emacs")

        assert out is None
        assert not mprint.called
        assert not shell.help_repo_addfilters.called
        assert not logger.error.called
        assert shell.client.channel.software.addRepoFilter.called

    def test_repo_removefilters_insufficient_args(self, shell):
        """
        Test do_repo_removefilters without sufficient arguments.

        :param shell:
        :return:
        """

        for arg in ["", "repo"]:
            shell.help_repo_removefilters = MagicMock()
            shell.client.channel.software.removeRepoFilter = MagicMock()
            mprint = MagicMock()
            logger = MagicMock()

            with patch("spacecmd.repo.print", mprint) as prn, \
                    patch("spacecmd.repo.logging", logger) as lgr:
                out = spacecmd.repo.do_repo_removefilters(shell, arg)

            assert out is None
            assert not mprint.called
            assert not shell.client.channel.software.removeRepoFilter.called
            assert not logger.error.called
            assert shell.help_repo_removefilters.called

    def test_repo_removefilters_wrong_syntax(self, shell):
        """
        Test do_repo_removefilters using wrong syntax.

        :param shell:
        :return:
        """
        shell.help_repo_removefilters = MagicMock()
        shell.client.channel.software.removeRepoFilter = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_removefilters(shell, "repo foo bar")

        assert out is None
        assert not mprint.called
        assert not shell.client.channel.software.removeRepoFilter.called
        assert not shell.help_repo_removefilters.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list, 'Each filter must start with + or -')

    def test_repo_removefilters_remove(self, shell):
        """
        Test do_repo_removefilters remove.

        :param shell:
        :return:
        """
        shell.help_repo_removefilters = MagicMock()
        shell.client.channel.software.removeRepoFilter = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_removefilters(shell, "repo +emacs -vim")

        assert out is None
        assert not mprint.called
        assert not shell.help_repo_removefilters.called
        assert not logger.error.called
        assert shell.client.channel.software.removeRepoFilter.called
        assert shell.client.channel.software.removeRepoFilter.call_count == 2

        assert_args_expect(shell.client.channel.software.removeRepoFilter.call_args_list,
                           [((shell.session, 'repo', {'filter': 'emacs', 'flag': '+'}), {}),
                            ((shell.session, 'repo', {'filter': 'vim', 'flag': '-'}), {})])

    def test_repo_setfilters_noargs(self, shell):
        """
        Test do_repo_setfilters no args.

        :param shell:
        :return:
        """
        for arg in ["", "repo"]:
            shell.help_repo_setfilters = MagicMock()
            shell.client.channel.software.setRepoFilters = MagicMock()
            mprint = MagicMock()
            logger = MagicMock()

            with patch("spacecmd.repo.print", mprint) as prn, \
                    patch("spacecmd.repo.logging", logger) as lgr:
                out = spacecmd.repo.do_repo_setfilters(shell, arg)

            assert out is None
            assert not mprint.called
            assert not logger.error.called
            assert not shell.client.channel.software.setRepoFilters.called
            assert shell.help_repo_setfilters.called

    def test_repo_setfilters_wrong_filters_syntax(self, shell):
        """
        Test do_repo_setfilters with wrong filters syntax

        :param shell:
        :return:
        """
        shell.help_repo_setfilters = MagicMock()
        shell.client.channel.software.setRepoFilters = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_setfilters(shell, "repo foo bar")

        assert out is None
        assert not mprint.called
        assert not shell.client.channel.software.setRepoFilters.called
        assert not shell.help_repo_setfilters.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list, "Each filter must start with + or -")

    def test_repo_setfilters(self, shell):
        """
        Test do_repo_setfilters with wrong filters syntax

        :param shell:
        :return:
        """
        shell.help_repo_setfilters = MagicMock()
        shell.client.channel.software.setRepoFilters = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_setfilters(shell, "repo +emacs -vim")

        assert out is None
        assert not mprint.called
        assert not shell.help_repo_setfilters.called
        assert not logger.error.called
        assert shell.client.channel.software.setRepoFilters.call_count == 1
        assert shell.client.channel.software.setRepoFilters.called

        assert_args_expect(shell.client.channel.software.setRepoFilters.call_args_list,
                           [((shell.session, 'repo',
                              [{'filter': 'emacs', 'flag': '+'},
                               {'filter': 'vim', 'flag': '-'}]), {})])

    def test_repo_clearfilters_noargs(self, shell):
        """
        Test do_repo_clearfilters with no arguments.

        :param shell:
        :return:
        """
        shell.help_repo_clearfilters = MagicMock()
        shell.client.channel.software.clearRepoFilters = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_clearfilters(shell, "")

        assert out is None
        assert not mprint.called
        assert not logger.error.called
        assert not shell.client.channel.software.clearRepoFilters.called
        assert shell.help_repo_clearfilters.called

    def test_repo_clearfilters_not_interactive(self, shell):
        """
        Test do_repo_clearfilters not interactive.

        :param shell:
        :return:
        """
        shell.help_repo_clearfilters = MagicMock()
        shell.client.channel.software.clearRepoFilters = MagicMock()
        shell.user_confirm = MagicMock(return_value=False)
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_clearfilters(shell, "repo --yes")

        assert out is None
        assert not mprint.called
        assert not shell.help_repo_clearfilters.called
        assert not logger.error.called
        assert shell.client.channel.software.clearRepoFilters.called

    def test_repo_clearfilters_interactive(self, shell):
        """
        Test do_repo_clearfilters interactive.

        :param shell:
        :return:
        """
        shell.help_repo_clearfilters = MagicMock()
        shell.client.channel.software.clearRepoFilters = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_clearfilters(shell, "repo")

        assert out is None
        assert not mprint.called
        assert not shell.help_repo_clearfilters.called
        assert not logger.error.called
        assert shell.client.channel.software.clearRepoFilters.called

    def test_repo_delete_noargs(self, shell):
        """
        Test do_repo_delete no arguments.

        :param shell:
        :return:
        """
        shell.help_repo_delete = MagicMock()
        shell.client.channel.software.removeRepo = MagicMock()
        shell.do_repo_list = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_delete(shell, "")

        assert out is None
        assert not mprint.called
        assert not logger.error.called
        assert not shell.client.channel.software.removeRepo.called
        assert shell.help_repo_delete.called

    def test_repo_rename_noargs(self, shell):
        """
        Test do_repo_rename no arguments.

        :param shell:
        :return:
        """
        shell.help_repo_rename = MagicMock()
        shell.client.channel.software.getRepoDetails = MagicMock()
        shell.do_repo_list = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_rename(shell, "")

        assert out is None
        assert not mprint.called
        assert not logger.error.called
        assert not shell.client.channel.software.getRepoDetails.called
        assert shell.help_repo_rename.called

    def test_repo_updateurl_noargs(self, shell):
        """
        Test do_repo_updateurl no arguments.

        :param shell:
        :return:
        """
        for arg in ["", "repo", "http://foo", "http://bar"]:
            shell.help_repo_updateurl = MagicMock()
            shell.client.channel.software.updateRepUrl = MagicMock()
            shell.do_repo_list = MagicMock()
            mprint = MagicMock()
            logger = MagicMock()

            with patch("spacecmd.repo.print", mprint) as prn, \
                    patch("spacecmd.repo.logging", logger) as lgr:
                out = spacecmd.repo.do_repo_updateurl(shell, "")

            assert out is None
            assert not mprint.called
            assert not logger.error.called
            assert not shell.client.channel.software.updateRepUrl.called
            assert shell.help_repo_updateurl.called

    def test_repo_updatessl_interactive(self, shell):
        """
        Test do_repo_updatessl interactive.

        :param shell:
        :return:
        """
        shell.help_repo_rename = MagicMock()
        shell.client.channel.software.updateRepoSsl = MagicMock()
        shell.do_repo_list = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock(side_effect=["name", "ca", "cert", "key"])

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.prompt_user", prompter) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_updatessl(shell, "")

        assert out is None
        assert not mprint.called
        assert not logger.error.called
        assert shell.client.channel.software.updateRepoSsl.called

        assert_args_expect(shell.client.channel.software.updateRepoSsl.call_args_list,
                           [((shell.session, "name", "ca", "cert", "key"), {})])

    def test_repo_updatessl_non_interactive(self, shell):
        """
        Test do_repo_updatessl non-interactive.

        :param shell:
        :return:
        """
        shell.help_repo_rename = MagicMock()
        shell.client.channel.software.updateRepoSsl = MagicMock()
        shell.do_repo_list = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock(return_value="")

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.prompt_user", prompter) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_updatessl(shell, "--name name --ca ca --cert cert --key key")

        assert out is None
        assert not mprint.called
        assert not logger.error.called
        assert shell.client.channel.software.updateRepoSsl.called

        assert_args_expect(shell.client.channel.software.updateRepoSsl.call_args_list,
                           [((shell.session, "name", "ca", "cert", "key"), {})])

    def test_repo_updatessl_missing_name(self, shell):
        """
        Test do_repo_updatessl non-interactive.

        :param shell:
        :return:
        """
        shell.help_repo_rename = MagicMock()
        shell.client.channel.software.updateRepoSsl = MagicMock()
        shell.do_repo_list = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock(return_value="")

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.prompt_user", prompter) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_updatessl(shell, "--ca ca --cert cert --key key")

        assert out is None
        assert not mprint.called
        assert not shell.client.channel.software.updateRepoSsl.called
        assert logger.error.called

        assert_expect(logger.error.call_args_list, "A name is required")

    def test_repo_create_interactive(self, shell):
        """
        Test do_repo_create interactive.

        :param shell:
        :return:
        """
        shell.client.channel.software.createRepo = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock(side_effect=["name", "http://something", "type",
                                          "ca", "cert", "key"])

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.prompt_user", prompter) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_create(shell, "")

        assert out is None
        assert not mprint.called
        assert shell.client.channel.software.createRepo.called
        assert not logger.error.called

        assert_args_expect(shell.client.channel.software.createRepo.call_args_list,
                           [((shell.session, 'name', 'type', 'http://something', 'ca', 'cert', 'key'), {})])

    def test_repo_create_non_interactive(self, shell):
        """
        Test do_repo_create interactive.

        :param shell:
        :return:
        """
        shell.client.channel.software.createRepo = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()
        prompter = MagicMock(return_value="")

        with patch("spacecmd.repo.print", mprint) as prn, \
                patch("spacecmd.repo.prompt_user", prompter) as prn, \
                patch("spacecmd.repo.logging", logger) as lgr:
            out = spacecmd.repo.do_repo_create(shell,
                                               "--n name --t type -u http://something "
                                               "--ca ca --cert cert --key key")

        assert out is None
        assert not mprint.called
        assert shell.client.channel.software.createRepo.called
        assert not logger.error.called
        assert_args_expect(shell.client.channel.software.createRepo.call_args_list,
                           [((shell.session, 'name', 'type', 'http://something', 'ca', 'cert', 'key'), {})])
