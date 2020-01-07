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

    @abstractmethod
    def _to_obj(self) -> Dict:
        """
        Object getter
        """

    def to_json(self) -> str:
        """
        Serialise to JSON.
        """
        return json.dumps(self._to_obj())


class MLPackageType(MLAnyType):
    """
    Package type input.
    """
    def __init__(self):
        """
        Constructor
        """
        self.rpms: Dict[str, None] = {}

    def add_package(self, package: str) -> None:
        """
        Add package to the type.
        """
        self.rpms[package] = None

    def _to_obj(self) -> Dict:
        """
        To object for JSON
        """
        return {
            "rpms": list(self.rpms.keys())
        }
