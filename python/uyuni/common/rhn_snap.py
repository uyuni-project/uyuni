#!/usr/bin/env python3
# -*- coding: utf-8 -*-
#
# Copyright (c) 2025 SUSE, LLC
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

import sys
import tempfile
import subprocess
import yaml
import os
import re
import shutil
import json
import urllib.request
import urllib.error

from uyuni.common.usix import raise_with_tb
from uyuni.common import checksum
from uyuni.common.rhn_pkg import A_Package, InvalidPackageError

SNAP_CHECKSUM_TYPE = "sha512"


class snap_Header:
    """- like deb_Header"""


    def __init__(self, stream):
        self.packaging = "snap"
        self.signatures = []
        self.is_source = 0
        self.snap_file_path = None

        try:
            self.snap_file_path = getattr(stream, 'name', None)
            if not self.snap_file_path or not os.path.exists(self.snap_file_path):
                raise Exception(f"SNAP file not found: {self.snap_file_path}")
        except Exception:
            e = sys.exc_info()[1]
            raise_with_tb(InvalidPackageError(e), sys.exc_info()[2])

        try:
            try:
                snap_yaml_content = self.get_file(self.snap_file_path, "meta/snap.yaml")
                snap_metadata = yaml.safe_load(snap_yaml_content.read())
                print(f"Debug: Successfully extracted snap.yaml from {self.snap_file_path}")

                self._create_header_from_metadata(snap_metadata)

            except Exception as e:
                print(f"Debug: Failed to extract snap.yaml: {e}")
                self._create_basic_header(stream)

        except Exception:
            e = sys.exc_info()[1]
            raise_with_tb(InvalidPackageError(e), sys.exc_info()[2])

    def get_file(self, snap_file_path, fname):
        """- fixed version"""

        if fname.startswith("./"):
            fname = fname[2:]
        elif fname.startswith("/"):
            fname = fname[1:]

        try:
            temp_dir = tempfile.mkdtemp()

            try:
                result = subprocess.run([
                    'unsquashfs', '-d', temp_dir, '-f', snap_file_path, fname
                ], stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=30)

                if result.returncode == 0:
                    extracted_file_path = os.path.join(temp_dir, fname)

                    if os.path.exists(extracted_file_path):
                        with open(extracted_file_path, 'rb') as f:
                            content = f.read()

                        import io
                        return io.BytesIO(content)
                    else:
                        raise Exception(f"File '{fname}' was not extracted to expected location")
                else:
                    stderr_text = result.stderr.decode() if result.stderr else "Unknown error"
                    raise Exception(f"unsquashfs failed with return code {result.returncode}: {stderr_text}")

            finally:
                shutil.rmtree(temp_dir, ignore_errors=True)

        except subprocess.TimeoutExpired:
            raise Exception(f"Timeout extracting file '{fname}' from SNAP package")
        except FileNotFoundError:
            raise Exception("unsquashfs command not found. Please install squashfs-tools.")
        except Exception as e:
            raise Exception(f"Error extracting '{fname}' from SNAP package: {e}")

    def _create_header_from_metadata(self, snap_metadata):
        """snap.yaml"""

        name = snap_metadata.get("name", "unknown")
        version = snap_metadata.get("version", "1.0")
        summary = snap_metadata.get("summary", "")
        description = snap_metadata.get("description", summary)

        architectures = snap_metadata.get("architectures", ["amd64"])
        arch = f"{architectures[0]}-snap" if architectures else "amd64-snap"
        api_version, api_release = self.get_api_version_release(name)
        if api_version:
            version = api_version
            print(f"Debug: Using API version: {version}")
        if api_release:
            release = api_release
            print(f"Debug: Using API release: {release}")

        self.hdr = {
            "name": name,
            "arch": arch,
            "summary": summary,
            "epoch": "",
            "version": version,
            "release": api_release,
            "description": description,
            "packaging": "snap",

            "snap_base": snap_metadata.get("base", ""),
            "snap_confinement": snap_metadata.get("confinement", ""),
            "snap_grade": snap_metadata.get("grade", ""),
            "snap_architectures": architectures,
        }

        if "apps" in snap_metadata:
            apps = snap_metadata["apps"]
            self.hdr["snap_apps"] = list(apps.keys())

            if apps:
                first_app = list(apps.values())[0]
                if "command" in first_app:
                    self.hdr["snap_command"] = first_app["command"]

        plugs = []
        if "apps" in snap_metadata:
            for app_name, app_config in snap_metadata["apps"].items():
                if "plugs" in app_config:
                    for plug in app_config["plugs"]:
                        plugs.append(f"snap-plug:{plug}")

        if "plugs" in snap_metadata:
            for plug_name, plug_config in snap_metadata["plugs"].items():
                plugs.append(f"snap-plug:{plug_name}")


        if plugs:
            unique_plugs = list(set(plugs))
            self.hdr["requires"] = unique_plugs
            print(f"Debug: Deduplicated requires: {unique_plugs}")

        if "slots" in snap_metadata:
            slots = []
            for slot_name, slot_config in snap_metadata["slots"].items():
                slots.append(f"snap-slot:{slot_name}")
            self.hdr["provides"] = slots

        snap_field_mappings = {
            "assumes": "snap_assumes",
            "license": "snap_license",
            "website": "snap_website",
            "source-code": "snap_source_code",
            "contact": "snap_contact",
            "title": "snap_title",
        }

        for snap_key, hdr_key in snap_field_mappings.items():
            if snap_key in snap_metadata:
                value = snap_metadata[snap_key]
                if isinstance(value, list):
                    self.hdr[hdr_key] = ", ".join(str(v) for v in value)
                else:
                    self.hdr[hdr_key] = str(value)

        print(f"Debug: Created SNAP header from metadata for {name} v{version}")


    def get_api_version_release(self, snap_name):
        """Snapcraft API"""
        if not snap_name or snap_name == "unknown":
            return None, None

        try:
            api_url = f"https://api.snapcraft.io/v2/snaps/info/{snap_name}?fields=channel-map,download,revision,version"

            headers = {
                "User-Agent": "snapd/2.63",
                "Snap-Device-Series": "16",
                "Accept": "application/json",
            }

            print(f"Debug: Fetching API info for '{snap_name}'...")

            request = urllib.request.Request(api_url, headers=headers)
            with urllib.request.urlopen(request, timeout=10) as response:
                if response.status == 200:
                    data = json.loads(response.read().decode('utf-8'))

                    channel_map = data.get("channel-map", [])
                    if channel_map:
                        for channel_info in channel_map:
                            channel = channel_info.get("channel", {})
                            if channel.get("name") == "stable":
                                version = channel_info.get("version")
                                revision = str(channel_info.get("revision", "1"))
                                print(f"Debug: API returned version={version}, release={revision}")
                                return version, revision

                        if channel_map:
                            first = channel_map[0]
                            version = first.get("version")
                            revision = str(first.get("revision", "1"))
                            print(f"Debug: API returned (first channel) version={version}, release={revision}")
                            return version, revision

        except Exception as e:
            print(f"Debug: API request failed: {e}")

        return None, None
    def _create_basic_header(self, stream):
        """ """
        filename = getattr(stream, 'name', 'unknown.snap')
        base_name = os.path.basename(filename)
        if base_name.endswith('.snap'):
            clean_name = base_name[:-5]
        else:
            clean_name = base_name

        clean_name = re.sub(r'[^a-zA-Z0-9._+-]', '_', clean_name)
        if not clean_name:
            clean_name = 'unknown_snap_package'

        self.hdr = {
            "name": clean_name,
            "arch": "amd64-snap",
            "summary": f"SNAP package {clean_name}",
            "epoch": "",
            "version": "1.0",
            "release": "1",
            "description": f"SNAP package {clean_name}",
            "packaging": "snap",
        }

        print(f"Debug: Created basic SNAP header for {clean_name}")

    @staticmethod
    def checksum_type():
        """ """
        return SNAP_CHECKSUM_TYPE

    @staticmethod
    def is_signed():
        """SNAP Snap Store"""
        return 0

    def __getitem__(self, name):
        """ """
        return self.hdr.get(str(name))

    def __setitem__(self, name, item):
        """ """
        self.hdr[name] = item

    def __delitem__(self, name):
        """ """
        del self.hdr[name]

    def __getattr__(self, name):
        """ """
        return getattr(self.hdr, name)

    def __len__(self):
        """ """
        return len(self.hdr)

    def get(self, key, default=None):
        """ """
        return self.hdr.get(key, default)

    def keys(self):
        """ """
        return self.hdr.keys()

    def items(self):
        """ """
        return self.hdr.items()


class SNAP_Package(A_Package):
    """ - like DEB_Package"""

    def __init__(self, input_stream=None):
        A_Package.__init__(self, input_stream)
        self.header_data = tempfile.NamedTemporaryFile()
        self.checksum_type = SNAP_CHECKSUM_TYPE

    def read_header(self):
        """- same as DEB_Package.read_header()"""
        self._stream_copy(self.input_stream, self.header_data)
        self.header_end = self.header_data.tell()

        try:
            self.header_data.seek(0, 0)
            self.header = snap_Header(self.header_data)
        except Exception:
            e = sys.exc_info()[1]
            raise_with_tb(InvalidPackageError(e), sys.exc_info()[2])

    def save_payload(self, output_stream):
        """ - same as DEB_Package.save_payload()"""
        c_hash = checksum.getHashlibInstance(self.checksum_type, False)
        if output_stream:
            output_start = output_stream.tell()

        self._stream_copy(self.header_data, output_stream, c_hash)
        self.checksum = c_hash.hexdigest()

        if output_stream:
            self.payload_stream = output_stream
            self.payload_size = output_stream.tell() - output_start

    @staticmethod
    def checksum_type():
        """ """
        return SNAP_CHECKSUM_TYPE

    def get_package_size(self):
        """ """
        try:
            if hasattr(self.header_data, 'name'):
                return os.path.getsize(self.header_data.name)
            return self.header_data.tell()
        except (OSError, AttributeError):
            return 0
