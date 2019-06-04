# coding: utf-8
"""
Testing helpers
"""
import time
import pytest
import hashlib
from mock import MagicMock
from io import StringIO


class FileHandleMock(StringIO):
    """
    Filehandle mock
    """
    def __init__(self):
        self._init_params = None
        StringIO.__init__(self)
        self._closed = False

    def __call__(self, *args, **kwargs):
        self.__init_params = args, kwargs
        return self

    def close(self):
        """
        Bypass closing.
        """
        self._closed = True

    def get_content(self):
        """
        Get file content.
        """
        self.seek(0)
        return self.read()

    def get_init_args(self):
        """
        Get initial arguments.
        """
        return self.__init_params[0]

    def get_init_kwargs(self):
        """
        Get initial keywords.
        """
        return self.__init_params[1]


@pytest.fixture
def shell():
    """
    Create fake shell.
    """
    base = MagicMock()
    base.session = hashlib.sha256(str(time.time()).encode("utf-8")).hexdigest()
    base.client = MagicMock()
    base.client.activationkey = MagicMock()
    base.do_activationkey_list = MagicMock(return_value="do_activation_list")
    base.SEPARATOR = "-" * 10

    return base


def assert_expect(calls, *expectations):
    """
    Check expectations.
    Function accepts a list of calls of some mock and the corresponding expectations.
    Result is counted as passed, when calls are identical to the expectations and
    no more, no less. Otherwise an assertion error is raised.

    :param calls: Mock's call_args_list
    :param expectations: expectations array.
    """
    expectations = list(expectations)
    for call in calls:
        expectation = next(iter(expectations))
        assert call[0][0] == expectation, "Expected '{}', got '{}'".format(expectation, call[0][0])
        expectations.pop(0)
    assert not expectations


def assert_list_args_expect(calls, expectations):
    """
    Check expectations over the array.

    :param calls: Mock's call_args_list
    :param expectations: multi-expectations array per argument
    """
    for call in calls:
        assert_expect([call], next(iter(expectations)))
        expectations.pop(0)
    assert not expectations


def assert_args_expect(calls, expectations):
    """
    Check call-per-arg.

    Expectation args format:

      [
        (args, kw,)
      ]

    :param calls: Mock calls
    :param expectations: Argument list. Check its format above.
    :return:
    """
    for call in calls:
        args, kw = call
        _args, _kw = next(iter(expectations))
        assert args == _args, "{} is not as expected {}".format(str(args), str(_args))
        assert kw == _kw, "{} is not as expected {}".format(str(kw), str(_kw))
        expectations.pop(0)
    assert not expectations
