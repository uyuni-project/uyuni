"""
Error codes
"""

MLERR_OK = 0
MLERR_GENERAL_ERROR = 1
MLERR_MODULE_NOT_FOUND = 201
MLERR_STREAM_NOT_FOUND = 202
MLERR_REQUEST_ERROR = 301


class MlGeneralException(Exception):
    """
    General exception
    """
    code = MLERR_GENERAL_ERROR


class MlModuleNotFound(Exception):
    """
    Module was not found exception
    """
    code = MLERR_MODULE_NOT_FOUND


class MlStreamNotFound(Exception):
    """
    Stream was not found exception
    """
    code = MLERR_STREAM_NOT_FOUND

class MlRequestError(Exception):
    """
    Wrong request definition
    """
    code = MLERR_REQUEST_ERROR
