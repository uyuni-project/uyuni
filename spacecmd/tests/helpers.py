# coding: utf-8
"""
Testing helpers
"""
from io import StringIO


class FileHandleMock(StringIO):
    """
    Filehandle mock
    """
    def __init__(self):
        self._init_params = None
        StringIO.__init__(self)

    def __call__(self, *args, **kwargs):
        self.__init_params = args, kwargs
        return self

    def close(self):
        """
        Bypass closing.
        """

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
