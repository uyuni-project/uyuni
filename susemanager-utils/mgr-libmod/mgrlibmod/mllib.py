"""
libmod operations
"""
import os
import sys
import gzip
import json
import argparse
import binascii

from typing import Any, Dict, List, Set, Optional
from mgrlibmod import mltypes

import gi  # type: ignore

gi.require_version("Modulemd", "2.0")
from gi.repository import Modulemd  # type: ignore


class MLLibmodProc:
    """
    Libmod process.
    """

    RESERVED_STREAMS = ["platform"]

    def __init__(self, metadata: List[str]):
        """
        __init__

        :param metadata: paths of the metadata.
        :type metadata: List[str]
        """
        self.metadata = metadata
        self._mod_index: Modulemd.ModuleIndex = None
        assert gi is not None and Modulemd is not None, "No libmodulemd found"
        self._enabled_stream_modules: Dict = {}
        self._streams: List = []

    def _is_stream_enabled(self, s_type: mltypes.MLStreamType) -> bool:
        """
        _is_stream_enabled -- returns True if stream is enabled. NOTE: "platform" is always enabled.

        :param s_type: stream type object
        :type s_type: MLStreamType
        :return: True, if stream is enabled.
        :rtype: bool
        """
        return s_type.name in self._enabled_stream_modules

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

    def enable_stream(self, s_obj) -> None:
        """
        enable_stream -- enables stream.

        :param s_obj: module type
        :type s_obj: Module object
        """
        self._enabled_stream_modules[s_obj.get_module_name()] = s_obj

    def disable_stream(self, name: str) -> None:
        """
        disable_stream -- disabled stream.

        :param name: stream name
        :type name: str
        """
        if name not in MLLibmodProc.RESERVED_STREAMS:
            self._enabled_stream_modules.pop(name, None)

    def get_module_streams(self, name: str) -> List:
        if self._mod_index is None:
            self.index_modules()
        assert self._mod_index is not None, "Unable to get module streams: module index not found"

        streams: Set = set()
        module = self._mod_index.get_module(name)
        if module:
            for s_obj in module.get_all_streams():
                streams.add(s_obj.get_stream_name())

        return list(streams)

    def get_stream_contexts(self, s_type: mltypes.MLStreamType) -> List:
        if self._mod_index is None:
            self.index_modules()
        assert self._mod_index is not None, "Unable to get stream contexts: module index not found"
        contexts: List = []
        module = self._mod_index.get_module(s_type.name)
        if module:
            for stream in module.get_all_streams():
                if stream.get_stream_name() == s_type.stream:
                    contexts.append(stream)

        return contexts

    def get_stream_dependencies(self, ctx: Modulemd.ModuleStreamV2) -> List[str]:
        """
        get_stream_dependencies -- get stream dependencies.

        :param ctx: module stream context
        :type ctx: Modulemd.ModuleStreamV2
        :return: list of dependency names.
        :rtype: List[str]
        """
        deps: List[str] = []
        s_deps: List[Modulemd.Dependencies] = ctx.get_dependencies() or []
        dep: Modulemd.Dependencies
        for dep in s_deps:
            deps.extend(dep.get_runtime_modules())

        return deps

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

    def get_default_stream(self, name: str):
        assert self._mod_index is not None, "Unable to access module index when resolving default stream"
        module = self._mod_index.get_module(name)
        if not module:
            raise ValueError("Module {} not found".format(name))
        defaults = module.get_defaults()
        if defaults:
            return defaults.get_default_stream()

        return module.get_all_streams()[0].get_stream_name()

    def get_dep_streams(self, s_obj):
        dep = s_obj.get_dependencies()[0]
        all_deps = []  # type: ignore
        for m in dep.get_runtime_modules():
            deps = dep.get_runtime_streams(m)
            if deps:
                all_deps.append((m, deps[0],))
        return all_deps

    def get_actual_stream(self, name: str):
        if name == "platform":
            return "el8"
        if name not in self._enabled_stream_modules:
            return self.get_default_stream(name)
        return self._enabled_stream_modules[name].get_stream_name()

    def list_enabled_streams(self):
        for stream in self._enabled_stream_modules.values():
            print(stream.get_NSVCA())

    def get_rpm_blacklist(self):
        assert self._mod_index is not None, "No module index has been found"
        enabled_packages: Set = set()
        for stream in self._enabled_stream_modules.values():
            enabled_packages = enabled_packages.union(stream.get_rpm_artifacts())

        all_packages: Set = set()
        for name in self._mod_index.get_module_names():
            module = self._mod_index.get_module(name)
            for stream in module.get_all_streams():
                all_packages = all_packages.union(stream.get_rpm_artifacts())

        return list(all_packages.difference((enabled_packages)))

    def get_pkg_name(self, pkg_name: str) -> str:
        """
        get_pkg_name -- get package name

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
            print("%s ** %s" % (e, pkg_name))
            raise e
        return wover

    def get_artifact_with_name(self, artifacts, name):
        for artifact in artifacts:
            n = self.get_pkg_name(artifact)
            if name == n:
                return artifact
        return None

    def _get_api_provides(self) -> Dict[str, mltypes.MLSet]:
        """
        _get_api_provides -- get provided apis.

        :return: List of apis in the list in a fashion of a Set.
        :rtype: Dict[str, mltypes.MLSet]
        """
        api_provides: Dict[str, mltypes.MLSet] = {
            "_other_": mltypes.MLSet(),
        }
        for stream in self._enabled_stream_modules.values():
            if not stream:
                continue
            s_arts = stream.get_rpm_artifacts()
            for rpm in stream.get_rpm_api():
                artifact = self.get_artifact_with_name(s_arts, rpm)
                if artifact:
                    if rpm not in api_provides:
                        api_provides[rpm] = mltypes.MLSet([artifact])
                    else:
                        api_provides[rpm].add(artifact)
                    s_arts.remove(artifact)

        # Add the remaining non-api artifacts
            for artifact in s_arts:
                api_provides["_other_"].add(artifact)
        return api_provides

    def _select_stream(self, m_name, stream) -> Dict:
        s_obj: Dict = {
            "name": m_name,
            "stream": stream.get_stream_name(),
            "version": stream.get_version(),
            "context": stream.get_context(),
            "arch": stream.get_arch(),
        }
        return s_obj

    def get_api_provides(self):
        api_provides: Dict[str, mltypes.MLSet] = {
            "apis": mltypes.MLSet(),
            "packages": mltypes.MLSet(),
            "selected": mltypes.MLSet(),
        }

        anchor_version: int = 0
        anchor_stream: Optional[Modulemd.ModuleStreamV2] = None
        for stream in self._streams:
            version: int = stream.get_version()
            if anchor_version < version:
                anchor_version = version
                anchor_stream = stream

        assert anchor_stream is not None, "Unable to find default stream"

        for stream in self._streams:
            if not stream:
                continue
            if stream.get_context() != anchor_stream.get_context():
                continue

            stream_artifacts = stream.get_rpm_artifacts()

            for rpm in stream.get_rpm_artifacts():
                if not rpm.endswith(".src"):
                    api_provides["packages"].add(rpm)

            for rpm in stream.get_rpm_api():
                artifact = self.get_artifact_with_name(stream_artifacts, rpm)
                if artifact:
                    api_provides["apis"].add(rpm)
                    if not artifact.endswith(".src"):
                        api_provides["packages"].add(artifact)
            api_provides["selected"].add(self._select_stream(stream.get_module_name(), stream))

        return api_provides

    def pick_stream(self, s_type: mltypes.MLStreamType):
        if self._is_stream_enabled(s_type=s_type):
            return

        all_deps = set()  # type: ignore
        allContexts = self.get_stream_contexts(s_type=s_type)
        for c in allContexts:
            all_deps = all_deps.union(self.get_stream_dependencies(c))

        enabledDeps = []
        for d in all_deps:
            enabledDeps.append((d, self.get_actual_stream(d)))

        for ctx in allContexts:
            currDeps = self.get_dep_streams(ctx)
            if all(i in enabledDeps for i in currDeps):
                for dstream in currDeps:
                    self.pick_stream(s_type=mltypes.MLStreamType(name=dstream[0], streamname=dstream[1]))

                self.enable_stream(ctx)
                self._streams.append(ctx)

    def pick_default_stream(self, s_type: mltypes.MLStreamType):
        s_type = mltypes.MLStreamType(s_type.name, self.get_default_stream(s_type.name))
        self.pick_stream(s_type=s_type)


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
            assert os.path.exists(modulepath), "File {} not found".format(modulepath)
        self._proc = MLLibmodProc(self.repodata.get_paths())

        return self

    def to_json(self, pretty:bool = False) -> str:
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
        self._result[fname] = getattr(self, "_function__{}".format(fname))()

        return self

    def _get_repodata_streams(self) -> List[Modulemd.ModuleStreamV2]:
        """
        _get_repodata_streams -- collect all repodata streams and select current default.

        :return: List of stream objects
        :rtype: List
        """
        self._proc._streams.clear()
        self._proc.index_modules()

        for s_type in self.repodata.get_streams():
            try:
                if s_type.stream:
                    self._proc.pick_stream(s_type)
                else:
                    self._proc.pick_default_stream(s_type=s_type)
            except Exception as exc:
                sys.stderr.write("Skipping stream {}\n".format(s_type.name))
        return self._proc._streams

    # Functions
    def _function__list_modules(self) -> Dict[str, Dict]:
        """
        _function__all_modules -- lists all available modules.

        :return: list of strings
        :rtype: List[str]
        """
        modules: Dict = {
            "modules": []
        }
        self._proc.index_modules()
        for m_name in self._proc._mod_index.get_module_names():
            mobj: Dict = {}
            mod = self._proc._mod_index.get_module(m_name)
            d_mod = mod.get_defaults()
            if d_mod is not None:
                mobj[m_name] = {
                    "default": d_mod.get_default_stream(),
                    "streams": d_mod.get_streams_with_default_profiles(),
                }
                modules["modules"].append(mobj)
        return modules

    def _function__list_packages(self) -> Dict[str, List[str]]:
        """
        _function__list_packages -- lists all available packages for the module.

        :return: list of packages within 'packages' element
        :rtype: List[str]
        """
        self._proc.index_modules()
        rpms: Dict[str, List[str]] = {"packages": []}
        for name in self._proc._mod_index.get_module_names():
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
        self._get_repodata_streams()
        return self._proc.get_api_provides()
