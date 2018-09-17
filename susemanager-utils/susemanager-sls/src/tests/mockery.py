import sys
import os
try:
    from cStringIO import StringIO
except ImportError:
    from io import StringIO
from mock import MagicMock


def setup_environment():
    '''
    Mock the environment.
    :return:
    '''
    sys.modules['salt'] = MagicMock()
    sys.modules['salt.utils'] = MagicMock()
    sys.modules['salt.modules'] = MagicMock()
    sys.modules['salt.modules.cmdmod'] = MagicMock()
    sys.modules['salt.exceptions'] = MagicMock(CommandExecutionError=Exception)


def get_test_data(filename):
    '''
    Get a test data.

    :param filename:
    :return:
    '''
    return open(os.path.sep.join([os.path.abspath(''), 'data', filename]), 'r').read()
