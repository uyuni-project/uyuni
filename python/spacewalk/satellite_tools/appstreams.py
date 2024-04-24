"""Module to import AppStream modules from a channel's module metadata to the database.

This module provides functionality to validate and import modules from a channel's
AppStream modules metadata (Modulemd file) into the database.

Usage:
Instantiate ModuleMdImporter with the channel ID and the path to the Modulemd file.
Call validate() to validate the module metadata file.
Call import_module_metadata() to import modules to the database.

Example:
    importer = ModuleMdImporter(channel_id=1, modulemd_file='path/to/modulemd_file.yaml')
    try:
        importer.validate()
    except Exception as e:
        print("Validation failed:", str(e))
    else:
        importer.import_module_metadata()
"""

import re
import gi

# pylint: disable-next=wrong-import-position
gi.require_version("Modulemd", "2.0")
from gi.repository import Modulemd

from spacewalk.satellite_tools.syncLib import log
from spacewalk.server import rhnSQL


class Nevra:
    """Represents a package NEVRA string."""

    def __init__(self, name: str, epoch: str, version: str, release: str, arch: str):
        self.name = name
        self.epoch = epoch
        self.version = version
        self.release = release
        self.arch = arch

    def __repr__(self):
        epoch_str = f"{self.epoch}:" if self.epoch else ""
        return f"{self.name}-{epoch_str}{self.version}-{self.release}.{self.arch}"


class Nsvca:
    """Represents a NSVCA (name, stream, version, context, architecture) string."""

    def __init__(self, module):
        """
        Initializes an Nsvca object.

        Parameters:
        - module (Modulemd.ModuleStreamV2): Module stream object.
        """
        self.name = module.get_module_name()
        self.stream = module.get_stream_name()
        self.version = module.get_version()
        self.context = module.get_context()
        self.arch = module.get_arch()

    def __repr__(self):
        return f"{self.name}:{self.stream}:{self.version}:{self.context}:{self.arch}"


class ModuleMdIndexingError(Exception):
    """Exception raised when indexing module metadata fails."""

    pass


class ModuleMdImporter:
    """
    Imports a channel's AppStream modules from its Modulemd file to the database.

    Attributes:
    - channel_id (int): ID of the channel.
    - modulemd_file (str): Path to the channel's Modulemd file.
    """

    def __init__(self, channel_id: int, modulemd_file: str):
        """
        Initializes a ModuleMdImporter object.

        Parameters:
        - channel_id (str): ID of the channel.
        - modulemd_file (str): Path to the Modulemd file.
        """
        self.channel_id = channel_id
        self.modulemd_file = modulemd_file

    def validate(self):
        """Validates the module metadata file for parsing."""
        log(2, "  Validating module metadata file.")
        self._index_modulemd()
        if not self.modulemd_index.get_module_names():
            raise ModuleMdIndexingError("No module data exists in the metadata file.")

    def import_module_metadata(self):
        """Imports modules from the Modulemd file to the database."""
        if not self.modulemd_index:
            self._index_modulemd()
        rhnSQL.initDB()
        no_total_added = 0
        no_total_pkgs = 0
        log(0, "  Importing module metadata:")
        log(0, f"    Modules in repo: {len(self._get_modules())}")
        for module in self._get_modules():
            for stream in module.get_all_streams():
                log(2, f"Processing '{stream.get_NSVCA()}'")
                nsvca = Nsvca(stream)
                module_id = self._insert_module(nsvca)
                if not module_id:
                    continue

                log(2, "  Importing module APIs.")
                self._insert_module_apis(stream.get_rpm_api(), module_id)

                artifacts = stream.get_rpm_artifacts()
                no_stream_pkgs = len(artifacts)
                no_stream_added = 0
                no_total_pkgs += no_stream_pkgs
                for rpm in stream.get_rpm_artifacts():
                    nevra = self._parse_rpm_name(rpm)

                    log(2, "  Importing module packages.")
                    pid = self._insert_module_package(nevra, module_id)
                    if pid:
                        log(3, f"{nevra} is pid={pid}")
                        no_stream_added += 1
                        no_total_added += 1
                    else:
                        log(
                            2,
                            f"{nevra} is not in the repository or is already imported.",
                        )

                log(
                    0,
                    f"{no_stream_added} new package(s) imported for module {stream.get_NSVCA()}.",
                )

        rhnSQL.commit()
        log(2, f"{no_total_added} of {no_total_pkgs} packages matched.")

    def _index_modulemd(self):
        """Indexes the Modulemd file."""
        idx = Modulemd.ModuleIndex.new()
        try:
            idx.update_from_file(self.modulemd_file, False)
        except gi.repository.GLib.GError as e:
            raise ModuleMdIndexingError(
                f"An error occurred while indexing the module metadata from file '{self.modulemd_file}'."
            ) from e
        self.modulemd_index = idx

    def _get_modules(self):
        """
        Retrieves modules from the Modulemd index.

        Returns:
        list: List of Modulemd.ModuleStreamV2 objects.
        """
        return [
            self.modulemd_index.get_module(name)
            for name in self.modulemd_index.get_module_names()
        ]

    def _insert_module(self, module: Nsvca):
        """
        Inserts a module into the database.

        Parameters:
        - module (Nsvca): Module to insert.

        Returns:
        int: ID of the inserted module.
        """
        q_insert_module = rhnSQL.prepare(
            """
            INSERT INTO suseAppstream (id, channel_id, name, stream, version, context, arch)
            VALUES (sequence_nextval('suse_as_module_seq'), :chid, :n, :s, :v, :c, :a)
            ON CONFLICT (channel_id, name, stream, version, context, arch)
            DO UPDATE SET channel_id = :chid, name = :n, stream = :s, version = :v, context = :c, arch = :a
            RETURNING id
        """
        )

        q_insert_module.execute(
            chid=self.channel_id,
            n=module.name,
            s=module.stream,
            v=module.version,
            c=module.context,
            a=module.arch,
        )

        # Return the newly inserted module ID
        row = q_insert_module.fetchone()
        return row[0] if row else None

    def _insert_module_apis(self, apis, module_id: int):
        """
        Inserts API entries for a module into the database.

        Parameters:
        - apis (list(str)): List of API entries to insert.
        - module_id (int): ID of the module that the APIs belong to.
        """
        sql = "INSERT INTO suseAppstreamApi (rpm, module_id) VALUES (:rpm, :module_id) ON CONFLICT DO NOTHING"
        rhnSQL.prepare(sql).executemany(rpm=apis, module_id=[module_id] * len(apis))

    def _insert_module_package(self, pkg: Nevra, module_id: int):
        """
        Inserts a modular package relation into the database.

        Parameters:
        - pkg (Nevra): Package to insert.
        - module_id (int): ID of the module that the package belongs to.

        Returns:
        int: ID of the inserted package or None if the package doesn't exist in the database.
        """
        # Epoch '0' is not consistent in the DB. Make sure to match both '0' and NULL
        if pkg.epoch is None or pkg.epoch == "0":
            q_epoch_predicate = "(epoch IS NULL OR epoch = '0')"
        else:
            q_epoch_predicate = "epoch = :epoch"

        q_insert_module_pkg = rhnSQL.prepare(
            f"""
            WITH package AS (
                SELECT p.id, pn.name, pe.epoch, pe.version, pe.release, pa.label as arch
                FROM rhnPackage p
                    JOIN rhnPackageName pn ON p.name_id = pn.id
                    JOIN rhnPackageEvr pe ON p.evr_id = pe.id
                    JOIN rhnPackageArch pa ON p.package_arch_id = pa.id)
            INSERT INTO suseAppstreamPackage (package_id, module_id)
                SELECT p.id, :module_id FROM package p
                 WHERE p.name = :pname
                   AND {q_epoch_predicate}
                   AND p.version = :version
                   AND p.release = :release
                   AND p.arch = :parch
                ON CONFLICT DO NOTHING
                RETURNING package_id
        """
        )

        q_insert_module_pkg.execute(
            pname=pkg.name,
            epoch=pkg.epoch,
            version=pkg.version,
            release=pkg.release,
            parch=pkg.arch,
            module_id=module_id,
        )

        # Return the package ID if exists or None otherwise
        row = q_insert_module_pkg.fetchone()
        return row[0] if row else None

    @staticmethod
    def _parse_rpm_name(nevra):
        """
        Parses a NEVRA string into a Nevra object.

        Parameters:
        - nevra (str): NEVRA string to parse.

        Returns:
        Nevra: Nevra object containing parsed information.
        """
        pattern = re.compile(
            r"(?P<name>[a-zA-Z0-9._+-]+)-"
            r"(?P<epoch>\d+:)?"
            r"(?P<version>[a-zA-Z0-9._-]+)-"
            r"(?P<release>[a-zA-Z0-9._+-]+)\."
            r"(?P<arch>[a-zA-Z0-9._-]+)"
        )

        match = pattern.match(nevra)
        if match:
            name = match.group("name")
            epoch = match.group("epoch").rstrip(":") if match.group("epoch") else None
            version = match.group("version")
            release = match.group("release")
            arch = match.group("arch")

            # Return the components as a Nevra object
            return Nevra(name, epoch, version, release, arch)
        else:
            raise ValueError(f"The value {nevra} cannot be parsed as a NEVRA string.")
