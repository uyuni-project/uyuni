"""
Input types for the mgr-libmod
"""
# pylint: disable-next=unused-import
from typing import List, Tuple, Any, AnyStr, Union, Optional, Dict, Set, cast

# pylint: disable-next=unused-import
import collections
import json
from abc import ABC, abstractmethod
from mgrlibmod import mlerrcode


# pylint: disable-next=missing-class-docstring
class MLSet(list):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

    def __append(self, obj: Any) -> None:
        super().append(obj)

    def append(self, obj: Any) -> None:
        self.add(obj)

    def add(self, obj: Any) -> None:
        if obj not in self:
            self.__append(obj)


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

    def to_json(self, pretty: bool = False) -> str:
        """
        to_json serialises type to JSON.

        :return: valid JSON unicode string.
        :rtype: str
        """
        if pretty:
            out = json.dumps(self.to_obj(), indent=2, sort_keys=True)
        else:
            out = json.dumps(self.to_obj())
        return out


class MLPackageType(MLAnyType):
    """
    Package type input.
    """

    # pylint: disable-next=super-init-not-called
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
        # pylint: disable-next=consider-using-f-string
        return "<{} ({}/{}) at {}>".format(
            self.__class__.__name__, self.__name, self.__stream, hex(id(self))
        )

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

    def to_obj(self) -> Dict:
        return {"name": self.__name, "stream": self.__stream}


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
        if "function" not in obj:
            raise mlerrcode.MlRequestError("Unknown API function. Please define one")

        return obj["function"]

    def get_paths(self) -> List[str]:
        """
        get_paths get paths section from the YAML.

        :return: array of paths
        :rtype: List[str]
        """
        obj = self.to_obj()
        if "paths" not in obj:
            raise mlerrcode.MlRequestError(
                "No paths has been found in the input request"
            )

        paths: List[str] = obj.get("paths", [])
        if not bool(paths):
            raise mlerrcode.MlRequestError(
                "Paths should not be empty. At least one path is required"
            )

        return paths

    def get_streams(self) -> List[MLStreamType]:
        """
        get_streams [summary]

        :return: [description]
        :rtype: List[Tuple[str]]
        """
        obj: Dict = self.to_obj()
        out: List[MLStreamType] = []

        if obj["streams"] is None:
            raise mlerrcode.MlRequestError("Streams should not be null")

        for str_kw in obj["streams"]:
            if "name" not in str_kw:
                raise mlerrcode.MlRequestError(
                    # pylint: disable-next=consider-using-f-string
                    "No 'name' attribute in the stream parameter {}".format(str_kw)
                )
            out.append(
                MLStreamType(name=str_kw["name"], streamname=str_kw.get("stream") or "")
            )

        return out


class MLErrorType(MLAnyType):
    """
    Error response
    """

    # pylint: disable-next=super-init-not-called
    def __init__(self, exc: Exception = None):
        """
        Constructor.
        """
        self._obj: Dict = {
            "error_code": 0,
            "data": {},
            "exception": "",
        }

        if exc is not None:
            self.exc = exc
            self.error_code = getattr(exc, "code", mlerrcode.MLERR_GENERAL_ERROR)
            self.data = getattr(exc, "data", {})

    @property
    def error_code(self) -> int:
        return self._obj["error_code"]

    @error_code.setter
    def error_code(self, value: int) -> None:
        self._obj["error_code"] = value

    @property
    def exc(self) -> Exception:
        return Exception(self._obj["exception"])

    @exc.setter
    def exc(self, ex: Exception) -> None:
        self._obj["exception"] = str(ex)

    @property
    def data(self) -> Dict:
        return self._obj["data"]

    @data.setter
    def data(self, d: Dict) -> None:
        self._obj["data"] = d

    def to_obj(self) -> Dict:
        """
        Return JSON object
        """
        return cast(Dict, self._obj)
