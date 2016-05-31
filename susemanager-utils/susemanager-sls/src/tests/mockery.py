import sys
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
