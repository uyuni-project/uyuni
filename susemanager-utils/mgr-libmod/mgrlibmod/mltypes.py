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


class MLStreamType:
    """
    Stream type
    """
    def __init__(self, name: str, streamname: str):
        self.__name: str = name
        self.__stream: str = streamname
        self.__exc = Exception("This is a read-only property")

    def __repr__(self) -> str:
        return "<{} ({}/{}) at {}>".format(self.__class__.__name__, self.__name, self.__stream, hex(id(self)))

    @property
    def name(self) -> str:
        return self.__name

    @name.setter
    def name(self, v: str) -> None:
        raise self.__exc

    @property
    def stream(self) -> str:
        return self.__stream

    @stream.setter
    def stream(self, v: str) -> None:
        raise self.__exc


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

    def get_streams(self) -> List[MLStreamType]:
        """
        get_streams [summary]

        :return: [description]
        :rtype: List[Tuple[str]]
        """
        obj: Dict = self.to_obj()
        out: List[MLStreamType] = []

        assert obj["streams"] is not None, "Streams should not be null!"
        for str_kw in obj["streams"]:
            assert "name" in str_kw, "No 'name' attribute in the stream parameter {}".format(str_kw)
            assert "stream" in str_kw, "No 'stream' attribute in the stream parameter {}".format(str_kw)
            out.append(MLStreamType(name=str_kw["name"], streamname=str_kw["stream"]))

        return out
