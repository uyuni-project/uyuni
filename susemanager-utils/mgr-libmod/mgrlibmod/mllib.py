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

    def set_repodata(self, repodata: str) -> MLLibmodAPI:
        """
        set_repodata sets the repository data from the input JSON.

        :param repodata: JSON string of the input object.
        :type repodata: str
        """
        return self

    def get_all_modules(self):
        pass

    def get_all_packages(self):
        pass