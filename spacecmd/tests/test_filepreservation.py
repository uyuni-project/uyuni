# coding: utf-8
"""
Test suite for spacecmd.filepreservation
"""
from mock import MagicMock, patch, mock_open
import spacecmd.filepreservation
from helpers import shell


class TestSCFilePreservation:
    """
    Test class for testing spacecmd file preservation.
    """
    def test_do_filepreservation_list_noreturn(self, shell):
        """
        Test do_filepreservation_list return to the STDOUT
        """
        shell.client.kickstart.filepreservation.listAllFilePreservations = MagicMock(
            return_value=[
                {"name": "list_one"},
                {"name": "list_two"},
                {"name": "list_three"},
            ]
        )

        mprint = MagicMock()
        with patch("spacecmd.filepreservation.print", mprint):
            ret = spacecmd.filepreservation.do_filepreservation_list(shell, "", doreturn=False)
        assert ret is None
        assert mprint.call_args_list[0][0][0] == "list_one\nlist_three\nlist_two"

    def test_do_filepreservation_list_return(self, shell):
        """
        Test do_filepreservation_list return data.
        """
        shell.client.kickstart.filepreservation.listAllFilePreservations = MagicMock(
            return_value=[
                {"name": "list_one"},
                {"name": "list_two"},
                {"name": "list_three"},
            ]
        )

        mprint = MagicMock()
        with patch("spacecmd.filepreservation.print", mprint):
            ret = spacecmd.filepreservation.do_filepreservation_list(shell, "", doreturn=True)
        assert not mprint.called
        assert ret == ['list_one', 'list_two', 'list_three']

    def test_do_filepreservation_create_noargs_prompted_name(self, shell):
        """
        Test do_filepreservation_create no args passed so prompt appears.
        """
        shell.client.kickstart.filepreservation.create = MagicMock()

        mprint = MagicMock()
        prmt = MagicMock(side_effect=["prompted_name", "file_one", "file_two", ""])
        with patch("spacecmd.filepreservation.prompt_user", prmt) as pmt, \
             patch("spacecmd.filepreservation.print", mprint) as mpt:
            spacecmd.filepreservation.do_filepreservation_create(shell, "")

        expectations = [
            'File List', '---------', '', '',
            'File List', '---------', 'file_one', '',
            'File List', '---------', 'file_one\nfile_two', '', '',
            'File List', '---------', 'file_one\nfile_two'
        ]
        for idx, call in enumerate(mprint.call_args_list):
            assert call[0][0] == expectations[idx]

    def test_do_filepreservation_delete_noargs(self, shell):
        """
        Test do_filepreservation_delete no args.
        """
        shell.help_filepreservation_delete = MagicMock()
        shell.client.kickstart.filepreservation.delete = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)

        spacecmd.filepreservation.do_filepreservation_delete(shell, "")

        assert shell.help_filepreservation_delete.called
        assert not shell.client.kickstart.filepreservation.delete.called
        assert not shell.user_confirm.called
