"""
libmod operations
"""
import os
import gzip
import argparse
import binascii

from typing import Any, Dict, List
from mgrlibmod import mltypes

import gi  # type: ignore

gi.require_version("Modulemd", "2.0")
from gi.repository import Modulemd  # type: ignore


class MLLibmodProc:
    """
    Libmod process.
    """

    def __init__(self, metadata: List[str]):
        """
        __init__

        :param metadata: paths of the metadata.
        :type metadata: List[str]
        """
        self.metadata = metadata
        self._mod_index = None
        assert gi is not None and Modulemd is not None, "No libmodulemd found"

    def _is_meta_compressed(self, path: str) -> bool:
        """
        _is_meta_compressed -- detect if metafile is plain text YAML or compressed.

        :param path: path to the meta file.
        :type path: str
        :return: True, if meta is GNU Zip compressed.
        :rtype: bool
        """
        with open(path, "rb") as metafile:
            return binascii.hexlify(metafile.read(2)) == b"1f8b"  # Almost reliable :-)

    def index_modules(self) -> None:
        """
        index_modules -- loads given metadata and indexes modules from there.
        """
        if self._mod_index is None:
            mgr: Modulemd.ModuleIndex = Modulemd.ModuleIndexMerger.new()
            for path in self.metadata:
                idx = Modulemd.ModuleIndex.new()
                if self._is_meta_compressed(path):
                    with gzip.open(path) as gzmeta:
                        idx.update_from_string(gzmeta.read().decode("utf-8"), True)
                else:
                    idx.update_from_file(path, True)
                mgr.associate_index(idx, 0)
            self._mod_index = mgr.resolve()


class MLLibmodAPI:
    """
    Libmod API operations.
    """

    def __init__(self, opts: argparse.Namespace):
        """
        __init__

        :param opts: Parsed opts namespace.
        :type opts: argparse.Namespace
        """
        self._opts = opts
        self.repodata: mltypes.MLInputType
        self._result: Dict[str, Dict[str, Dict]] = {
            "module_packages": {},
            "list_packages": {},
            "list_modules": {},
        }
        self._proc: MLLibmodProc

    def set_repodata(self, repodata: str) -> "MLLibmodAPI":
        """
        set_repodata -- set the repository data from the input JSON.

        :param repodata: JSON string of the input object.
        :type repodata: str
        """
        self.repodata = mltypes.MLInputType(repodata)
        for modulepath in self.repodata.get_paths():
            assert os.path.exists(modulepath), "File {} not found".format(modulepath)
        self._proc = MLLibmodProc(self.repodata.get_paths())

        return self

    # Functions
    def _get_all_modules(self):
        pass

    def _get_all_packages(self):
        pass

    def _get_module_packages(self):
        """
        _get_module_packages -- get all RPMs from selected streams as a map of package names to package strings.
        """
        self._proc.index_modules()
        for s_obj in self.repodata.get_streams():
            try:
                print(s_obj)
            except Exception as exc:
                print("Skipping stream", s_obj)

        # index = createModuleIndex(metadataPaths)
        # for (name, stream) in selectedStreams:
        #    try:
        #        if stream:
        #            pickStream(name, stream)
        #        else:
        #            pickDefaultStream(name)
        #    except Exception as e:
        #        print(e)
        #        print("Skipping {}:{}".format(name, stream))
        # return getApiProvides()

    # API
    def to_json(self) -> str:
        """
        to_json -- render the last set processed result by 'run' method into the JSON string.

        :return: JSON string
        :rtype: str
        """
        return ""

    def run(self) -> "MLLibmodAPI":
        """
        run -- process the logic, based on the input.

        :return: MLLibmodAPI
        """
        f = self.repodata.get_function()
        self._result[f] = getattr(self, "_get_{}".format(f))()

        return self
