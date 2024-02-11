"""
libmod operations
"""
import os

# pylint: disable-next=unused-import
import sys
import gzip
import json
import argparse
import binascii

# pylint: disable-next=unused-import
from typing import Any, Dict, List, Set, Optional
from mgrlibmod import mltypes, mlerrcode, mlresolver

import gi  # type: ignore

gi.require_version("Modulemd", "2.0")
# pylint: disable-next=wrong-import-position
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
        self._mod_index: Modulemd.ModuleIndex = None

        if gi is None or Modulemd is None:
            raise mlerrcode.MlGeneralException("No python libmodulemd was found")

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

    def get_stream_contexts(self, stream: mltypes.MLStreamType) -> List:
        """
        get_stream_contexts -- get all the alternative contexts for a module stream

        :param stream: the specified module stream
        """
        if self._mod_index is None:
            self.index_modules()

        if self._mod_index is None:
            raise mlerrcode.MlGeneralException("Module index not found")

        contexts: List = []
        module = self._mod_index.get_module(stream.name)
        if module:
            stream_name = (
                stream.stream if stream.stream else self.get_default_stream(stream.name)
            )
            for ctx in module.get_all_streams():
                if ctx.get_stream_name() == stream_name:
                    contexts.append(ctx)

        return contexts

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
                        idx.update_from_string(gzmeta.read().decode("utf-8"), False)
                else:
                    idx.update_from_file(path, False)
                mgr.associate_index(idx, 0)
            self._mod_index = mgr.resolve()

    def get_default_stream(self, name: str):
        if self._mod_index is None:
            raise mlerrcode.MlGeneralException("Module index not found")

        module = self._mod_index.get_module(name)

        if not module:
            raise mlerrcode.MlModuleNotFound(
                # pylint: disable-next=consider-using-f-string
                "Module {} not found".format(name)
            ).set_data("streams", [mltypes.MLStreamType(name, "").to_obj()])

        defaults = module.get_defaults()
        if defaults:
            return defaults.get_default_stream()

        return module.get_all_streams()[0].get_stream_name()

    def get_streams_with_defaults(self, streams):
        streams_with_defaults = []
        for s in streams:
            stream_name = s.stream
            if not stream_name:
                stream_name = self.get_default_stream(s.name)
            streams_with_defaults.append(mltypes.MLStreamType(s.name, stream_name))
        return streams_with_defaults

    def _get_pkg_name(self, pkg_name: str) -> str:
        """
        _get_pkg_name -- get package name

        :param pkg_name: package name
        :type pkg_name: str
        :raises e: General exception if name doesn't comply.
        :return: name of the package
        :rtype: str
        """
        try:
            woarch = pkg_name.rsplit(".", 1)[0]
            worel = woarch.rsplit("-", 1)[0]
            wover = worel.rsplit("-", 1)[0]
        except Exception as e:
            # pylint: disable-next=consider-using-f-string
            raise mlerrcode.MlGeneralException("{}: {}".format(e, pkg_name))

        return wover

    def _get_artifact_with_name(self, artifacts, name):
        for artifact in artifacts:
            n = self._get_pkg_name(artifact)
            if name == n:
                return artifact
        return None

    def _get_stream_object(self, m_name, stream) -> Dict:
        s_obj: Dict = {
            "name": m_name,
            "stream": stream.get_stream_name(),
            "version": stream.get_version(),
            "context": stream.get_context(),
            "arch": stream.get_arch(),
        }
        return s_obj

    def get_api_provides(self, streams):
        api_provides: Dict[str, mltypes.MLSet] = {
            "apis": mltypes.MLSet(),
            "packages": mltypes.MLSet(),
            "selected": mltypes.MLSet(),
        }

        for stream in streams:
            if not stream:
                continue

            stream_artifacts = stream.get_rpm_artifacts()

            for rpm in stream.get_rpm_artifacts():
                if not rpm.endswith(".src"):
                    api_provides["packages"].add(rpm)

            for rpm in stream.get_rpm_api():
                artifact = self._get_artifact_with_name(stream_artifacts, rpm)
                if artifact:
                    api_provides["apis"].add(rpm)
                    if not artifact.endswith(".src"):
                        api_provides["packages"].add(artifact)
            api_provides["selected"].add(
                self._get_stream_object(stream.get_module_name(), stream)
            )

        return api_provides


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
        self._result: Dict[str, Dict[str, Dict]] = {}
        self._proc: MLLibmodProc

    def set_repodata(self, repodata: str) -> "MLLibmodAPI":
        """
        set_repodata -- set the repository data from the input JSON.

        :param repodata: JSON string of the input object.
        :type repodata: str
        """
        self.repodata = mltypes.MLInputType(repodata)
        for modulepath in self.repodata.get_paths():
            if not os.path.exists(modulepath):
                raise mlerrcode.MlGeneralException(
                    # pylint: disable-next=consider-using-f-string
                    "File {} not found".format(modulepath)
                )

        self._proc = MLLibmodProc(self.repodata.get_paths())

        return self

    def to_json(self, pretty: bool = False) -> str:
        """
        to_json -- render the last set processed result by 'run' method into the JSON string.

        :return: JSON string
        :rtype: str
        """
        out: str
        if pretty:
            out = json.dumps(self._result, indent=2, sort_keys=True)
        else:
            out = json.dumps(self._result)
        return out

    def run(self) -> "MLLibmodAPI":
        """
        run -- process the logic, based on the input.

        :return: MLLibmodAPI
        """
        fname = self.repodata.get_function()
        # pylint: disable-next=consider-using-f-string
        self._result[fname] = getattr(self, "_function__{}".format(fname))()

        return self

    def _validate_input_streams(self, streams):
        for s1 in streams:
            for s2 in streams:
                if s1.name == s2.name and s1.stream != s2.stream:
                    raise mlerrcode.MlConflictingStreams(
                        "Conflicting streams"
                    ).set_data("streams", [s1.to_obj(), s2.to_obj()])

    def _resolve_stream_dependencies(self) -> List[Modulemd.ModuleStreamV2]:
        """
        _resolve_stream_dependencies -- select all the module dependencies preferring default streams

        :return: List of stream objects
        :rtype: List
        """
        input_streams = self._proc.get_streams_with_defaults(
            self.repodata.get_streams()
        )
        self._validate_input_streams(input_streams)

        resolver = mlresolver.DependencyResolver(self._proc)
        solutions = resolver.resolve(input_streams)
        if not solutions:
            raise mlerrcode.MlDependencyResolutionError(
                "Dependencies cannot be resolved"
            )

        # Return the solution with highest score (most selections with default streams)
        solutions.sort(key=lambda s: s[1], reverse=True)
        # Flatten version lists
        return [i for l in solutions[0][0] for i in l]

    # API functions
    def _function__list_modules(self) -> Dict[str, Dict]:
        """
        _function__all_modules -- lists all available modules.

        :return: list of strings
        :rtype: List[str]
        """
        modules: Dict = {"modules": {}}
        self._proc.index_modules()
        mobj: Dict = {}
        # pylint: disable-next=protected-access
        for m_name in self._proc._mod_index.get_module_names():
            # pylint: disable-next=protected-access
            mod = self._proc._mod_index.get_module(m_name)
            d_mod = mod.get_defaults()
            mobj[m_name] = {
                "default": d_mod.get_default_stream() if d_mod else None,
                "streams": list(
                    set([s.get_stream_name() for s in mod.get_all_streams()])
                ),
            }

        modules["modules"] = mobj
        return modules

    def _function__list_packages(self) -> Dict[str, List[str]]:
        """
        _function__list_packages -- lists all available packages for the module.

        :return: list of packages within 'packages' element
        :rtype: List[str]
        """
        self._proc.index_modules()
        rpms: Dict[str, List[str]] = {"packages": []}
        # pylint: disable-next=protected-access
        for name in self._proc._mod_index.get_module_names():
            # pylint: disable-next=protected-access
            module = self._proc._mod_index.get_module(name)
            for stream in module.get_all_streams():
                rpms["packages"].extend(stream.get_rpm_artifacts())
        return rpms

    def _function__module_packages(self) -> Dict[str, List[str]]:
        """
        _function__module_packages -- get all RPMs from selected streams as a map of package names to package strings.

        :return: structure for module packages
        :rtype: Dict[str, List[str]]
        """
        self._proc.index_modules()
        selected_streams = self._resolve_stream_dependencies()
        return self._proc.get_api_provides(selected_streams)
