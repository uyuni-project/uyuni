"""
Error codes
"""

from typing import Dict, Any

MLERR_OK = 0
MLERR_GENERAL_ERROR = 1
MLERR_MODULE_NOT_FOUND = 201
MLERR_DEPENDENCY_RESOLUTION_ERROR = 202
MLERR_CONFLICTING_STREAMS = 203
MLERR_REQUEST_ERROR = 301


class MlException(Exception):
    """
    General exception carrier.
    """

    def __init__(self, *args):
        Exception.__init__(self, *args)
        self.data: Dict = {}

    def set_data(self, key: str, val: Any) -> "MlException":
        """
        Add a data key/value to the final output.
        eturns:
            [type] -- [description]
        """
        self.data[key] = val
        return self


class MlGeneralException(MlException):
    """
    General exception
    """

    code = MLERR_GENERAL_ERROR


class MlModuleNotFound(MlException):
    """
    Module was not found exception
    """

    code = MLERR_MODULE_NOT_FOUND


class MlDependencyResolutionError(MlException):
    """
    Dependency resolution error
    """

    code = MLERR_DEPENDENCY_RESOLUTION_ERROR


class MlConflictingStreams(MlException):
    """
    Conflicting streams exception
    """

    code = MLERR_CONFLICTING_STREAMS


class MlRequestError(MlException):
    """
    Wrong request definition
    """

    code = MLERR_REQUEST_ERROR
