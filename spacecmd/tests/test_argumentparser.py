# SPDX-FileCopyrightText: 2026 Red Hat, Inc.
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-3.0-or-later

# coding: utf-8
"""
Test argument parser.
"""
import pytest
import spacecmd.argumentparser
from helpers import exc2str


class TestSCArgumentParser:
    """
    Test argument parser subclass.
    """

    def test_argparse_raise_exception(self):
        """
        Test argparse raise exception.
        """
        msg = "not enough memory, get system upgrade"
        argparse = spacecmd.argumentparser.SpacecmdArgumentParser()
        with pytest.raises(Exception) as exc:
            argparse.error(msg)
        assert msg in exc2str(exc)
