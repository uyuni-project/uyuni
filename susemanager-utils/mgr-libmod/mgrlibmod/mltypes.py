"""
Input types for the mgr-libmod
"""
from typing import List, Tuple, Any, AnyStr, Union, Optional, Dict, Set, cast
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
        self._obj = json.loads(data)

    @abstractmethod
    def to_obj(self) -> Dict:
        """
        to_obj is an object getter.

        :return: Object for JSON serialisation.
        :rtype: Dict
        """

    def to_json(self) -> str:
        """
        to_json serialises type to JSON.

        :return: valid JSON unicode string.
        :rtype: str
        """
        return json.dumps(self.to_obj())


class MLPackageType(MLAnyType):
    """
    Package type input.
    """

    def __init__(self):
        """
        Constructor
        """
        self.rpms: Set = set()

    def add_package(self, package: str) -> None:
        """
        add_package adds package name to the type.

        :param package: package name
        :type package: str
        :return: None
        :rtype: None
        """
        self.rpms.add(package)

    def to_obj(self) -> Dict:
        """
        to_obj serialise object to the dictionary.

        :return: object as a dictionary.
        :rtype: Dict
        """
        return {"rpms": list(self.rpms)}


class MLInputType(MLAnyType):
    """
    Input type.
    """

    def to_obj(self) -> Dict:
        return cast(Dict, self._obj)

    def get_function(self) -> str:
        """
        get_function -- return function name.

        :return: function name to process.
        :rtype: str
        """
        obj = self.to_obj()
        assert "function" in obj, "Unknown API function. Please define one."

        return obj["function"]

    def get_paths(self) -> List[str]:
        """
        get_paths get paths section from the YAML

        :return: array of paths
        :rtype: List[str]
        """
        obj = self.to_obj()
        assert "paths" in obj, "No paths has been found in the input request."

        paths: List[str] = obj.get("paths", [])
        assert bool(paths), "Paths should not be empty. At least one path is required"

        return paths

    def get_streams(self) -> Tuple[Dict[str, str], ...]:
        """
        get_streams [summary]

        :return: [description]
        :rtype: List[Tuple[str]]
        """
        obj: Dict = self.to_obj()
        out: List[Tuple[str, ...]] = []

        assert obj["streams"] is not None, "Streams should not be null!"

        return tuple(obj["streams"])
