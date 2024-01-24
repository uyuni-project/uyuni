#  pylint: disable=missing-module-docstring
import sys
import os

try:
    # pylint: disable-next=unused-import
    from cStringIO import StringIO
except ImportError:
    from io import StringIO
from unittest.mock import MagicMock


def setup_environment():
    """
    Mock the environment.
    :return:
    """
    if "salt" not in sys.modules or not isinstance(sys.modules["salt"], MagicMock):
        sys.modules["salt"] = MagicMock()
        sys.modules["salt.cache"] = MagicMock()
        sys.modules["salt.config"] = MagicMock()
        sys.modules["salt.utils"] = MagicMock()
        sys.modules["salt.utils.versions"] = MagicMock()
        sys.modules["salt.utils.odict"] = MagicMock()
        sys.modules["salt.utils.minions"] = MagicMock()
        sys.modules["salt.utils.network"] = MagicMock()
        sys.modules["salt.modules"] = MagicMock()
        sys.modules["salt.modules.cmdmod"] = MagicMock()
        sys.modules["salt.modules.virt"] = MagicMock()
        sys.modules["salt.states"] = MagicMock()
        sys.modules["salt.exceptions"] = MagicMock(CommandExecutionError=Exception)


def get_test_data(filename):
    """
    Get a test data.

    :param filename:
    :return:
    """
    # pylint: disable-next=unspecified-encoding
    return open(os.path.sep.join([os.path.abspath(""), "data", filename]), "r").read()
