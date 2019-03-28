# coding: utf-8
"""
Test embeded diskspace check.
"""

import embedded_diskspace_check
from unittest.mock import MagicMock, patch


class TestEmbeddedDiskspaceCheck:
    """
    Test embedded diskspace check.
    """

    @patch("sys.exit", MagicMock())
    @patch("sys.argv", ["dummy", "/tmp", "100000", "/etc", "2000"])
    def test_cli_arguments(self):
        """
        Test command line arguments.
        """
        with patch("embedded_diskspace_check.check", MagicMock()) as edcc:
            embedded_diskspace_check.main()
        args = edcc.call_args_list[0][0][0]
        assert args["/tmp"] == 104857600000
        assert args["/etc"] == 2097152000
