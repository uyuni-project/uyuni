from unittest.mock import mock_open, patch

from ..grains import boot_time


def test_boot_time():
    proc_stat = """cpu  1 2 3 4
btime 1783549184
processes 123
"""

    with patch("builtins.open", mock_open(read_data=proc_stat)):
        assert boot_time.boot_time() == {"boot_time": 1783549184}


def test_boot_time_missing():
    proc_stat = """cpu  1 2 3 4
processes 123
"""

    with patch("builtins.open", mock_open(read_data=proc_stat)):
        assert boot_time.boot_time() == {}


def test_boot_time_invalid():
    proc_stat = """btime invalid
"""

    with patch("builtins.open", mock_open(read_data=proc_stat)):
        assert boot_time.boot_time() == {}


def test_boot_time_file_error():
    with patch("builtins.open", side_effect=OSError):
        assert boot_time.boot_time() == {}
