"""
Input types for the mgr-libmod
"""
from typing import List, Tuple, Any, AnyStr, Union, Optional, Dict
import collections
import json
from abc import ABC, abstractmethod


class MLAnyType(ABC):
    """
    Base MLType mix-in.
    """
    def __init__(self, data: str):
        """
        Constructor

        :param data: JSON string
        :type data: str
        """
        self._json = json.loads(data)

    @abstractmethod
    def _to_obj(self) -> Dict:
        """
        _to_obj is an object getter.

        :return: Object for JSON serialisation.
        :rtype: Dict
        """

    def to_json(self) -> str:
        """
        to_json serialises type to JSON.

        :return: valid JSON unicode string.
        :rtype: str
        """
        return json.dumps(self._to_obj())


class MLPackageType(MLAnyType):
    """
    Package type input.
    """
    def __init__(self, data: str):
        """
        Constructor
        """
        MLAnyType.__init__(self, data)
        self.rpms: Dict[str, None] = {}

    def add_package(self, package: str) -> None:
        """
        add_package adds package name to the type.

        :param package: package name
        :type package: str
        :return: None
        :rtype: None
        """
        self.rpms[package] = None

    def _to_obj(self) -> Dict:
        """
        _to_obj serialise object to the dictionary.

        :return: object as a dictionary.
        :rtype: Dict
        """
        return {
            "rpms": list(self.rpms.keys())
        }


class MLInputType(MLAnyType):
    """
    Input type.
    """
