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
    def test_do_filepreservation_list_return(self, shell):
        """
        Test do_filepreservation_list no arguments passed.
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
