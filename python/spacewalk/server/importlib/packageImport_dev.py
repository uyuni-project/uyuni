#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2008--2018 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
#
# Package import process
#

import rpm
import sys
import os.path
from .importLib import (
    GenericPackageImport,
    IncompletePackage,
    Import,
    InvalidArchError,
    InvalidChannelError,
    IncompatibleArchError,
)
from .mpmSource import mpmBinaryPackage
from uyuni.common import rhn_pkg
from spacewalk.common.rhnConfig import cfg_component
from spacewalk.server import taskomatic
from spacewalk.server.rhnServer import server_packages


# pylint: disable-next=missing-class-docstring
class ChannelPackageSubscription(GenericPackageImport):
    def __init__(self, batch, backend, caller=None, strict=0, repogen=True):
        # If strict, the set of packages that was passed in will be the only
        # one in the channels - everything else will be unlinked
        GenericPackageImport.__init__(self, batch, backend)
        self.affected_channels = []
        # A hash keyed on the channel id, and with tuples
        # (added_packages, removed_packages) as values (packages are package
        # ids)
        self.affected_channel_packages = {}
        if not caller:
            self.caller = "backend.(unknown)"
        else:
            self.caller = caller
        self._strict_subscription = strict
        self.repogen = repogen
        self.package_type = None

    def preprocess(self):
        # Processes the package batch to a form more suitable for database
        # operations
        print("DEBUG: ChannelPackageSubscription.preprocess() called")
        for package in self.batch:
            # if package object doesn't have multiple checksums (like satellite-sync objects)
            #   then let's fake it
            if "checksums" not in package:
                
                package["checksums"] = {package["checksum_type"]: package["checksum"]}
                print(package["checksums"])
            if not isinstance(package, IncompletePackage):
                raise TypeError(
                    # pylint: disable-next=consider-using-f-string
                    "Expected an IncompletePackage instance, "
                    "got %s" % package.__class__.__name__
                )
            self._processPackage(package)

    def fix(self):
        print("DEBUG: Starting fix method...")

        # Look up arches and channels
        print("DEBUG: Looking up package arches...")
        print(f"DEBUG: package_arches before lookup: {self.package_arches}")
        self.backend.lookupPackageArches(self.package_arches)
        print(f"DEBUG: Package arches: {self.package_arches}")

        print("DEBUG: Looking up channels...")
        self.backend.lookupChannels(self.channels)
        print(f"DEBUG: Channels: {self.channels}")

        # Initialize self.channel_package_arch_compat
        print("DEBUG: Initializing channel_package_arch_compat...")
        self.channel_package_arch_compat = {}

        for channel, channel_row in list(self.channels.items()):
            if not channel_row:
                print(f"DEBUG: Unsupported channel: {channel}")
                continue
            self.channel_package_arch_compat[channel_row["channel_arch_id"]] = None
        print(f"DEBUG: Channel package arch compat: {self.channel_package_arch_compat}")

        for label, aid in self.package_arches.items():
            print(f"DEBUG: Looking up package arch type for arch ID: {aid}")
            self.package_type = self.backend.lookupPackageArchType(aid)
            print(f"DEBUG: Package type: {self.package_type}")
            if self.package_type:
                break

        print("DEBUG: Looking up channel package arch compatibility...")
        self.backend.lookupChannelPackageArchCompat(self.channel_package_arch_compat)
        print(f"DEBUG: Channel package arch compat after lookup: {self.channel_package_arch_compat}")

        print("DEBUG: Looking up package names...")
        self.backend.lookupPackageNames(self.names)
        print(f"DEBUG: Package names: {self.names}")

        print("DEBUG: Looking up EVRs...")
        try:
            if not self.package_type:
                print("WARNING: Package type is None. Attempting to proceed with default values.")
               


            if not self.evrs:
                print("WARNING: EVRs dictionary is empty. This may cause issues in lookupEVRs.")

            print(f"DEBUG: Calling lookupEVRs with EVRs: {self.evrs} and package_type: {self.package_type}")
            self.backend.lookupEVRs(self.evrs, self.package_type)
            print(f"DEBUG: EVRs after lookup: {self.evrs}")
        except Exception as e:
            print(f"ERROR: Exception occurred during lookupEVRs: {e}")
            print(f"DEBUG: EVRs: {self.evrs}, Package type: {self.package_type}")
            raise

        print("DEBUG: Looking up checksums...")
        self.backend.lookupChecksums(self.checksums)
        print(f"DEBUG: Checksums: {self.checksums}")

        # Fix the package information up, and uniquify the packages too
        print("DEBUG: Checking server configuration for NVREA...")
        with cfg_component("server") as CFG:
            enable_nvrea = CFG.ENABLE_NVREA
        print(f"DEBUG: NVREA enabled: {enable_nvrea}")

        uniqdict = {}
        for package in self.batch:
            if package.ignored:
                print(f"DEBUG: Skipping ignored package: {package}")
                continue

            print(f"DEBUG: Postprocessing package NEVRA...")
            self._postprocessPackageNEVRA(package)

            if not enable_nvrea:
                # NVREA disabled, skip checksum
                nevrao = (
                    package["name_id"],
                    package["evr_id"],
                    package["package_arch_id"],
                    package["org_id"],
                )
            else:
                # NVREA enabled, uniquify based on checksum
                nevrao = (
                    package["name_id"],
                    package["evr_id"],
                    package["package_arch_id"],
                    package["org_id"],
                    package["checksum_id"],
                )
            print(f"DEBUG: NEVRAO for package: {nevrao}")

            if nevrao not in uniqdict:
                print(f"DEBUG: Adding new package to uniqdict: {nevrao}")
                # Uniquify the channel names
                package["channels"] = {}
                # Initialize the channels
                self.__copyChannels(package, package)
                uniqdict[nevrao] = package
            else:
                print(f"DEBUG: Package found twice in the same batch: {nevrao}")
                # Package is found twice in the same batch
                # Are the packages the same?
                self._comparePackages(package, uniqdict[nevrao])
                # Invalidate it
                package.ignored = 1
                firstpackage = uniqdict[nevrao]
                # Copy any new channels
                self.__copyChannels(package, firstpackage)
                # Knowing the id of the referenced package
                package.first_package = firstpackage

        print("DEBUG: Fix method completed.")

    def _comparePackages(self, package1, package2):
        # XXX This should probably do a deep compare of the two packages
        pass

    def submit(self):

        print("channelpacakge subcription package import submit called ")
        self.backend.lookupPackages(self.batch, self.checksums)
        try:
            affected_channels = self.backend.subscribeToChannels(
                self.batch, strict=self._strict_subscription
            )
        except:
            print(f"ERROR: Failed to subscribe to channels. Exception: {e}")
            self.backend.rollback()
            raise
        self.compute_affected_channels(affected_channels)

        if len(self.batch) < 10:
            # update small batch per package
            name_ids = [pkg["name_id"] for pkg in self.batch]
        else:
            # update bigger batch at once
            name_ids = []
        self.backend.update_newest_package_cache(
            caller=self.caller,
            affected_channels=self.affected_channel_packages,
            name_ids=name_ids,
        )
        # Now that channel is updated, schedule the repo generation
        if self.repogen:
            taskomatic.add_to_repodata_queue_for_channel_package_subscription(
                self.affected_channels, self.batch, self.caller
            )
        self.backend.commit()
    # ...existing code...
    def submit(self):
        print("channelpacakge subcription package import submit called ")
        
        # 添加调试信息 - 检查 batch 内容
        print(f"DEBUG: ChannelPackageSubscription.submit() - batch size: {len(self.batch)}")
        for i, pkg in enumerate(self.batch):
            print(f"DEBUG: Package {i}:")
            print(f"  name: {pkg.get('name', 'UNKNOWN')}")
            print(f"  version: {pkg.get('version', 'UNKNOWN')}")
            print(f"  release: {pkg.get('release', 'UNKNOWN')}")
            print(f"  epoch: {pkg.get('epoch', 'UNKNOWN')}")
            print(f"  arch: {pkg.get('arch', 'UNKNOWN')}")
            print(f"  checksum: {pkg.get('checksum', 'UNKNOWN')}")
            print(f"  checksum_type: {pkg.get('checksum_type', 'UNKNOWN')}")
            print(f"  name_id: {pkg.get('name_id', 'UNKNOWN')}")
            print(f"  evr_id: {pkg.get('evr_id', 'UNKNOWN')}")
            print(f"  package_arch_id: {pkg.get('package_arch_id', 'UNKNOWN')}")
            print(f"  checksum_id: {pkg.get('checksum_id', 'UNKNOWN')}")
            print(f"  channels: {pkg.get('channels', 'UNKNOWN')}")
            print(f"  ignored: {getattr(pkg, 'ignored', 'NOT_SET')}")
        
        # 添加调试信息 - 检查 checksums 内容
        print(f"DEBUG: self.checksums content:")
        for checksum_key, checksum_value in self.checksums.items():
            print(f"  {checksum_key}: {checksum_value}")
        
        print("DEBUG: About to call self.backend.lookupPackages()...")
        
        try:
            self.backend.lookupPackages(self.batch, self.checksums)
            print("DEBUG: self.backend.lookupPackages() completed successfully")
        except Exception as e:
            print(f"ERROR: self.backend.lookupPackages() failed: {e}")
            print(f"ERROR: Exception type: {type(e)}")
            
            # 检查数据库中是否真的存在这个包
            print("DEBUG: Checking if package exists in database...")
            for pkg in self.batch:
                print(f"DEBUG: Checking package {pkg.get('name')} with:")
                print(f"  name_id: {pkg.get('name_id')}")
                print(f"  evr_id: {pkg.get('evr_id')}")
                print(f"  package_arch_id: {pkg.get('package_arch_id')}")
                print(f"  checksum_id: {pkg.get('checksum_id')}")
                print(f"  org_id: {pkg.get('org_id')}")
            raise
        
        try:
            print("DEBUG: About to call self.backend.subscribeToChannels()...")
            affected_channels = self.backend.subscribeToChannels(
                self.batch, strict=self._strict_subscription
            )
            print(f"DEBUG: subscribeToChannels() returned: {affected_channels}")
        except Exception as e:
            print(f"ERROR: subscribeToChannels() failed: {e}")
            self.backend.rollback()
            raise
        
        print("DEBUG: About to call self.compute_affected_channels()...")
        self.compute_affected_channels(affected_channels)
        print(f"DEBUG: compute_affected_channels() completed")
        print(f"DEBUG: self.affected_channels: {self.affected_channels}")
        print(f"DEBUG: self.affected_channel_packages: {self.affected_channel_packages}")

        if len(self.batch) < 10:
            # update small batch per package
            name_ids = [pkg["name_id"] for pkg in self.batch]
            print(f"DEBUG: Small batch - name_ids: {name_ids}")
        else:
            # update bigger batch at once
            name_ids = []
            print(f"DEBUG: Large batch - using empty name_ids")
            
        print("DEBUG: About to call self.backend.update_newest_package_cache()...")
        self.backend.update_newest_package_cache(
            caller=self.caller,
            affected_channels=self.affected_channel_packages,
            name_ids=name_ids,
        )
        print("DEBUG: update_newest_package_cache() completed")
        
        # Now that channel is updated, schedule the repo generation
        if self.repogen:
            print("DEBUG: About to call taskomatic.add_to_repodata_queue_for_channel_package_subscription()...")
            taskomatic.add_to_repodata_queue_for_channel_package_subscription(
                self.affected_channels, self.batch, self.caller
            )
            print("DEBUG: taskomatic call completed")
        else:
            print("DEBUG: Skipping taskomatic call (repogen=False)")
            
        print("DEBUG: About to call self.backend.commit()...")
        self.backend.commit()
        print("DEBUG: ChannelPackageSubscription.submit() completed successfully!")
# ...existing code...
    def compute_affected_channels(self, affected_channels):
        # Fill the list of affected channels
        self.affected_channel_packages.clear()
        self.affected_channel_packages.update(affected_channels)
        for channel_label, channel_row in list(self.channels.items()):
            channel_id = channel_row["id"]
            if channel_id in affected_channels:
                affected_channels[channel_id] = channel_label
        self.affected_channels = list(affected_channels.values())

    def _processPackage(self, package):
        GenericPackageImport._processPackage(self, package)

        # Process channels
        channels = []
        channelHash = {}
        for channel in package["channels"]:
            channelName = channel["label"]
            if channelName not in channelHash:
                channels.append(channelName)
                channelHash[channelName] = None
            self.channels[channelName] = None
        # Replace the channel list with the uniquified list
        package.channels = channels

        # Process weak dependencies
        weak_dep_tags = (
            "recommends",
            "suggests",
            "supplements",
            "enhances",
            "breaks",
            "predepends",
        )
        for tag in weak_dep_tags:
            if tag not in package or not isinstance(package[tag], list):
                package[tag] = []  # Initialize as an empty list if not present

        # Process capabilities
        capabilities_tags = (
            "provides",
            "requires",
            "conflicts",
            "obsoletes",
            "recommends",
            "suggests",
            "supplements",
            "enhances",
            "breaks",
            "predepends",
        )
        for tag in capabilities_tags:
            depList = package.get(tag, [])
            if not isinstance(depList, list):
                depList = []  # Ensure depList is a list

            for dep in depList:
                if isinstance(dep, str):
                    # If dep is a string, treat it as a simple capability
                    capability = (dep, "")
                elif isinstance(dep, dict):
                    # If dep is a dictionary, extract name and version
                    capability = (
                        self._fix_encoding(dep.get("name", "")),
                        self._fix_encoding(dep.get("version", "")),
                    )
                else:
                    continue  # Skip invalid entries

                if capability not in self.capabilities:
                    self.capabilities[capability] = None

    # Copies the channels from one package to the other
    def __copyChannels(self, sourcePackage, destPackage):
        dpHash = destPackage["channels"]
        for schannelName in sourcePackage.channels:
            # Check if the package is compatible with the channel
            channel = self.channels[schannelName]
            if not channel:
                # Unknown channel
                sourcePackage.ignored = 1
                raise InvalidChannelError(
                    channel,
                    # pylint: disable-next=consider-using-f-string
                    "Unsupported channel %s" % schannelName,
                )
            # Check channel-package compatibility
            charch = channel["channel_arch_id"]
            archCompat = self.channel_package_arch_compat[charch]
            if not archCompat:
                # Invalid architecture
                sourcePackage.ignored = 1
                raise InvalidArchError(
                    charch,
                    # pylint: disable-next=consider-using-f-string
                    "Invalid channel architecture %s" % charch,
                )

            # Now check if the source package's arch is compatible with the
            # current channel
            if sourcePackage["package_arch_id"] not in archCompat:
                sourcePackage.ignored = 1
                raise IncompatibleArchError(
                    sourcePackage.arch,
                    charch,
                    # pylint: disable-next=consider-using-f-string
                    "Package arch %s incompatible with channel %s"
                    % (sourcePackage.arch, schannelName),
                )

            dpHash[channel["id"]] = schannelName

        destPackage.channels = list(dpHash.values())


# pylint: disable-next=missing-class-docstring
class PackageImport(ChannelPackageSubscription):
    def __init__(self, batch, backend, caller=None, update_last_modified=0):
        ChannelPackageSubscription.__init__(self, batch, backend, caller=caller)
        self.ignoreUploaded = 1
        self._update_last_modified = update_last_modified
        self.capabilities = {}
        self.groups = {}
        self.sourceRPMs = {}
        self.changelog_data = {}
        self.suseProdfile_data = {}
        self.suseEula_data = {}
        self.extraTags = {}

    def _skip_tag(self, package, tag):
        # Allow all tags in case of DEB packages
        if package["arch"] and package["arch"].endswith("deb"):
            return False
        # See if the installed version of RPM understands a given tag
        # Assumed attr-format in RPM is 'RPMTAG_<UPPERCASETAG>'
        return not hasattr(rpm, "RPMTAG_" + tag.upper())

    def _processPackage(self, package):
        print(f"DEBUG: PackageImport._processPackage() called for package:{package.get('name', 'UNKNOWN')}")

        try:
            ChannelPackageSubscription._processPackage(self, package)
            print(f"DEBUG: ChannelPackageSubscription._processPackage() completed successfully")

        except Exception as e:
            print(f"DEBUG: ChannelPackageSubscription._processPackage() FAILED: {e}")
            raise

        # Process package groups
        try:
            print(f"DEBUG: Processing package groups...")
            group = self._fix_encoding(package["package_group"]).strip()
            print(f"DEBUG: Package group processed: '{group}'")
            if group not in self.groups:
                self.groups[group] = None
            sourceRPM = package["source_rpm"]
            print(f"DEBUG: Source RPM: {sourceRPM}")
            if (sourceRPM is not None) and (sourceRPM not in self.sourceRPMs):
                self.sourceRPMs[sourceRPM] = None
            # Change copyright to license
            # XXX
            package["copyright"] = self._fix_encoding(package["license"])
            print(f"DEBUG: Basic package info processing completed")
        except Exception as e:
            print(f"DEBUG: Basic package info processing FAILED: {e}")
            raise

        # Process weak dependency tags
        try:
            print(f"DEBUG: Processing weak dependency tags...")
            for tag in (
                "recommends",
                "suggests",
                "supplements",
                "enhances",
                "breaks",
                "predepends",
            ):
                print(f"DEBUG: Checking weak dep tag: {tag}")
                if (
                    self._skip_tag(package, tag)
                    or tag not in package
                    # pylint: disable-next=unidiomatic-typecheck
                    or type(package[tag]) != type([])
                ):
                    # older spacewalk server do not export weak deps.
                    # and older RPM doesn't know about them either
                    # lets create an empty list
                    package[tag] = []
                    print(f"DEBUG: Set empty list for weak dep tag: {tag}")
            print(f"DEBUG: Weak dependency tags processing completed")
        except Exception as e:
            print(f"DEBUG: Weak dependency tags processing FAILED: {e}")
            raise

        # Creates all the data structures needed to insert capabilities
        try:
            print(f"DEBUG: Processing capabilities...")
            for tag in (
                "provides",
                "requires",
                "conflicts",
                "obsoletes",
                "recommends",
                "suggests",
                "supplements",
                "enhances",
                "breaks",
                "predepends",
            ):
                print(f"DEBUG: Processing capabilities for tag: {tag}")
                depList = package[tag]
                print(f"DEBUG: depList for {tag} - type: {type(depList)}, value: {depList}")

                # pylint: disable-next=unidiomatic-typecheck
                if type(depList) != type([]):
                    sys.stderr.write(
                        # pylint: disable-next=consider-using-f-string
                        "!!! packageImport.PackageImport._processPackage: "
                        "erronous depList for '%s', converting to []\n" % tag
                    )
                    depList = []

                if depList is None:
                    print(f"DEBUG: depList is None for {tag}, skipping iteration")
                    continue

                for i, dep in enumerate(depList):
                    print(f"DEBUG: Processing dep {i} for tag {tag}: {dep}")
                    if dep is None:
                        print(f"DEBUG: dep {i} is None, skipping")
                        continue
                    nv = []
                    for f in ("name", "version"):
                        nv.append(self._fix_encoding(dep[f]))
                        del dep[f]
                    nv = tuple(nv)
                    dep["capability"] = nv
                    if nv not in self.capabilities:
                        self.capabilities[nv] = None
            print(f"DEBUG: Capabilities processing completed")
        except Exception as e:
            print(f"DEBUG: Capabilities processing FAILED: {e}")
            print(f"DEBUG: Exception type: {type(e)}")
            raise

        # Check whether package is a PTF or part of PTF
        try:
            print(f"DEBUG: Processing PTF check...")
            package["is_ptf"] = False
            package["is_part_of_ptf"] = False
            provides_list = package.get("provides", [])
            print(f"DEBUG: provides list - type: {type(provides_list)}, length: {len(provides_list) if provides_list else 0}")

            if provides_list is None:
                print(f"DEBUG: provides list is None, skipping PTF check")
            else:
                for cap in provides_list:
                    if cap and "capability" in cap and cap["capability"]:
                        if cap["capability"][0] == "ptf()":
                            package["is_ptf"] = True
                        elif cap["capability"][0] == "ptf-package()":
                            package["is_part_of_ptf"] = True
            print(f"DEBUG: PTF check completed")
        except Exception as e:
            print(f"DEBUG: PTF check FAILED: {e}")
            raise

        # Process files too
        try:
            print(f"DEBUG: Processing files...")
            fileList = package["files"]
            print(f"DEBUG: fileList - type: {type(fileList)}, length: {len(fileList) if fileList else 0}")

            if fileList is None:
                print(f"DEBUG: fileList is None, this might be the problem!")
                fileList = []
                package["files"] = []

            for i, f in enumerate(fileList):
                print(f"DEBUG: Processing file {i}: {f}")
                if f is None:
                    print(f"DEBUG: file {i} is None, skipping")
                    continue
                filename = self._fix_encoding(f["name"])
                nv = (filename, "")
                del f["name"]
                f["capability"] = nv
                if nv not in self.capabilities:
                    self.capabilities[nv] = None
                f["checksum"] = self._fix_encoding(f["checksum"])
                fchecksumTuple = (f["checksum_type"], f["checksum"])
                if fchecksumTuple not in self.checksums:
                    self.checksums[fchecksumTuple] = None
            print(f"DEBUG: Files processing completed")
        except Exception as e:
            print(f"DEBUG: Files processing FAILED: {e}")
            print(f"DEBUG: Exception type: {type(e)}")
            raise

        # Uniquify changelog entries
        try:
            print(f"DEBUG: Processing changelog...")
            changelog_list = package.get("changelog", [])
            print(f"DEBUG: changelog - type: {type(changelog_list)}, length: {len(changelog_list) if changelog_list else 0}")

            if changelog_list is None:
                print(f"DEBUG: changelog is None, setting to empty list")
                changelog_list = []
                package["changelog"] = []

            unique_package_changelog_hash = set()
            unique_package_changelog = []
            for changelog in changelog_list:
                if changelog is None:
                    print(f"DEBUG: changelog entry is None, skipping")
                    continue
                changelog_name = self._fix_encoding(changelog["name"][:128])
                changelog_time = self._fix_encoding(changelog["time"])
                changelog_text = self._fix_encoding(changelog["text"])[:3000]
                key = (changelog_name, changelog_time, changelog_text)
                if key not in unique_package_changelog_hash:
                    self.changelog_data[key] = None
                    changelog["name"] = changelog_name
                    changelog["text"] = changelog_text
                    changelog["time"] = changelog_time
                    unique_package_changelog.append(changelog)
                    unique_package_changelog_hash.add(key)
            package["changelog"] = unique_package_changelog
            print(f"DEBUG: Changelog processing completed")
        except Exception as e:
            print(f"DEBUG: Changelog processing FAILED: {e}")
            raise

        # fix encoding issues in package summary and description
        try:
            print(f"DEBUG: Processing summary and description...")
            package["description"] = self._fix_encoding(package["description"])
            package["summary"] = self._fix_encoding(package["summary"])
            package["summary"] = package["summary"].rstrip() if package["summary"] else ""
            print(f"DEBUG: Summary and description processing completed")
        except Exception as e:
            print(f"DEBUG: Summary and description processing FAILED: {e}")
            raise

        # Process product files
        try:
            print(f"DEBUG: Processing product files...")
            product_files = package.get("product_files")
            print(f"DEBUG: product_files - type: {type(product_files)}, value: {product_files}")

            if product_files is not None:
                for prodFile in product_files:
                    if prodFile is None:
                        print(f"DEBUG: prodFile is None, skipping")
                        continue
                    evrtuple = (prodFile["epoch"], prodFile["version"], prodFile["release"])
                    evr = {evrtuple: None}
                    archhash = {prodFile["arch"]: None}
                    self.backend.lookupEVRs(evr, "rpm")
                    self.backend.lookupPackageArches(archhash)
                    prodFile["evr"] = evr[evrtuple]
                    prodFile["package_arch_id"] = archhash[prodFile["arch"]]
                    key = (
                        prodFile["name"],
                        prodFile["evr"],
                        prodFile["package_arch_id"],
                        prodFile["vendor"],
                        prodFile["summary"],
                        prodFile["description"],
                    )
                    self.suseProdfile_data[key] = None
            print(f"DEBUG: Product files processing completed")
        except Exception as e:
            print(f"DEBUG: Product files processing FAILED: {e}")
            raise

        # Process EULAs
        try:
            print(f"DEBUG: Processing EULAs...")
            eulas = package.get("eulas")
            print(f"DEBUG: eulas - type: {type(eulas)}, value: {eulas}")

            if eulas is not None:
                for eula in eulas:
                    if eula is None:
                        print(f"DEBUG: eula is None, skipping")
                        continue
                    key = (eula["text"], eula["checksum"])
                    self.suseEula_data[key] = None
            print(f"DEBUG: EULAs processing completed")
        except Exception as e:
            print(f"DEBUG: EULAs processing FAILED: {e}")
            raise

        # Process extra tags
        try:
            print(f"DEBUG: Processing extra tags...")
            extra_tags = package.get("extra_tags")
            print(f"DEBUG: extra_tags - type: {type(extra_tags)}, value: {extra_tags}")

            if extra_tags is not None:
                for tag in extra_tags:
                    if tag is None:
                        print(f"DEBUG: tag is None, skipping")
                        continue
                    self.extraTags[tag["name"]] = None
            print(f"DEBUG: Extra tags processing completed")
        except Exception as e:
            print(f"DEBUG: Extra tags processing FAILED: {e}")
            raise

        print(f"DEBUG: PackageImport._processPackage() completed successfully for package: {package.get('name', 'UNKNOWN')}")

    def fix(self): # type: ignore
        # If capabilities are available, process them
        if self.capabilities:
            try:
                print("DEBUG: Processing capabilities...")
                self.backend.processCapabilities(self.capabilities)
            except Exception as e:
                print(f"ERROR: Failed to process capabilities: {e}")
                self.backend.rollback()
                raise
            # Since this is the bulk of the work, commit
            print("DEBUG: Committidefng capabilities...")
            self.backend.commit()

        print("DEBUG: Processing changelog data...")
        self.backend.processChangeLog(self.changelog_data)
        print("DEBUG: Processing SUSE product files...")
        self.backend.processSuseProductFiles(self.suseProdfile_data)
        print("DEBUG: Processing SUSE EULAs...")
        self.backend.processSuseEulas(self.suseEula_data)

        print("DEBUG: Calling ChannelPackageSubscription.fix...")
        ChannelPackageSubscription.fix(self)

        print("DEBUG: Looking up source RPMs...")
        self.backend.lookupSourceRPMs(self.sourceRPMs)
        print("DEBUG: Looking up package groups...")
        self.backend.lookupPackageGroups(self.groups)
        print("DEBUG: Processing extra tags...")
        self.backend.processExtraTags(self.extraTags)

        # Postprocess the gathered information
        print("DEBUG: Postprocessing gathered information...")
        self.__postprocess()


    def subscribeToChannels(self):
        affected_channels = self.backend.subscribeToChannels(self.batch)
        # Fill the list of affected channels
        self.compute_affected_channels(affected_channels)

        name_ids = [pkg["name_id"] for pkg in self.batch]
        self.backend.update_newest_package_cache(
            caller=self.caller,
            affected_channels=self.affected_channel_packages,
            name_ids=name_ids,
        )
        taskomatic.add_to_repodata_queue_for_channel_package_subscription(
            self.affected_channels, self.batch, self.caller
        )
        self.backend.commit()
    def submit(self):
        upload_force = self.uploadForce
        if not upload_force and self._update_last_modified:
            # # Force it just a little bit - kind of hacky
            upload_force = 0.5

        print(f"DEBUG: Submit - About to call processPackages with {len(self.batch)} packages")
        for i, pkg in enumerate(self.batch):
            print(f"  Package {i}: {pkg.get('name', 'UNKNOWN')} - checksum: {pkg.get('checksum', 'UNKNOWN')}")

        try:
            print(f"DEBUG: Submit - Calling backend.processPackages()")
            self.backend.processPackages(
                self.batch,
                uploadForce=upload_force,
                forceVerify=self.forceVerify,
                ignoreUploaded=self.ignoreUploaded,
                transactional=self.transactional,
            )
            print(f"DEBUG: Submit - processPackages() completed successfully!")

            print(f"DEBUG: Submit - Verifying packages in database...")
            for i, pkg in enumerate(self.batch):
                if hasattr(pkg, 'id') and pkg.id:
                    print(f"  Package {i} got ID: {pkg.id}")

                    try:
                        h = self.backend.dbmodule.prepare("SELECT id, name FROM rhnPackage WHERE id = :pkg_id")
                        h.execute(pkg_id=pkg.id)
                        row = h.fetchone_dict()
                        if row:
                            print(f"  ✅ Package {i} confirmed in database: ID={row['id']}")
                        else:
                            print(f"  ❌ Package {i} NOT FOUND in database with ID={pkg.id}")
                    except Exception as db_error:
                        print(f"  ⚠️  Database verification failed for package{pkg.id}: {db_error}")
                else:
                    print(f"  ❌ Package {i} has no ID assigned!")

            self._import_signatures()
            print(f"DEBUG: Submit - _import_signatures() completed")

        except Exception as e:
            self.backend.commit()
        print(f"DEBUG: Submit - backend.commit() completed")

        if not self._update_last_modified:
            # Go though the list of objects and clear out the ones that have a
            # force of 0.5
            for p in self.batch:
                if p.diff and p.diff.level == 0.5:
                    # Ignore this difference completely
                    p.diff = None
                    # Leave p.diff_result in place

        print(f"DEBUG: Submit - All operations completed successfully!")
    def __postprocess(self):
        # Gather the IDs we've found

        for package in self.batch:
            if package.ignored:
                # Skip it
                continue
            # Only deal with packages
            self.__postprocessPackage(package)

    def __postprocessPackage(self, package):
            """populate the columns foo_id with id numbers from appropriate hashes"""
            package["package_group"] = self.groups[
                self._fix_encoding(package["package_group"]).strip()
            ]
            source_rpm = package["source_rpm"]
            if source_rpm is not None:
                source_rpm = self.sourceRPMs[source_rpm]
            else:
                source_rpm = ""
            package["source_rpm_id"] = source_rpm
            package["checksum_id"] = self.checksums[
                (package["checksum_type"], package["checksum"])
            ]

            # Postprocess the dependency information
            for tag in (
                "provides",
                "requires",
                "conflicts",
                "obsoletes",
                "files",
                "recommends",
                "suggests",
                "supplements",
                "enhances",
                "breaks",
                "predepends",
            ):
                for entry in package[tag]:
                    nv = entry["capability"]
                    entry["capability_id"] = self.capabilities[nv]
            for c in package["changelog"]:
                c["changelog_data_id"] = self.changelog_data[
                    (c["name"], c["time"], c["text"])
                ]
            if package["product_files"] is not None:
                for p in package["product_files"]:
                    p["prodfile_id"] = self.suseProdfile_data[
                        (
                            p["name"],
                            p["evr"],
                            p["package_arch_id"],
                            p["vendor"],
                            p["summary"],
                            p["description"],
                        )
                    ]
            if package["eulas"] is not None:
                for e in package["eulas"]:
                    e["eula_id"] = self.suseEula_data[(e["text"], e["checksum"])]
            fileList = package["files"]
            for f in fileList:
                f["checksum_id"] = self.checksums[(f["checksum_type"], f["checksum"])]
            if "extra_tags" in package and package["extra_tags"] is not None:
                for t in package["extra_tags"]:
                    t["key_id"] = self.extraTags[t["name"]]

    def _comparePackages(self, package1, package2):
        if (
            package1["checksum_type"] == package2["checksum_type"]
            and package1["checksum"] == package2["checksum"]
        ):
            return
        # XXX Handle this better
        # pylint: disable-next=broad-exception-raised
        raise Exception("Different packages in the same batch")

    # pylint: disable-next=redefined-builtin
    def _cleanup_object(self, object):
        ChannelPackageSubscription._cleanup_object(self, object)
        if object.ignored:
            object.id = object.first_package.id

    def _import_signatures(self):
        with cfg_component("server.satellite") as CFG:
            mount_point = CFG.MOUNT_POINT
        for package in self.batch:
            # skip missing files and mpm packages
            if package["path"] and not isinstance(package, mpmBinaryPackage):
                full_path = os.path.join(mount_point, package["path"])
                if os.path.exists(full_path):
                    header = rhn_pkg.get_package_header(filename=full_path)
                    server_packages.processPackageKeyAssociations(
                        header, package["checksum_type"], package["checksum"]
                    )


# pylint: disable-next=missing-class-docstring
class SourcePackageImport(Import):
    # pylint: disable-next=unused-argument
    def __init__(self, batch, backend, caller=None, update_last_modified=0):
        Import.__init__(self, batch, backend)
        self._update_last_modified = update_last_modified
        self.ignoreUploaded = 1
        self.sourceRPMs = {}
        self.groups = {}
        self.checksums = {}

    def preprocess(self):
        for package in self.batch:
            self._processPackage(package)

    def fix(self):
        self.backend.lookupSourceRPMs(self.sourceRPMs)
        self.backend.lookupPackageGroups(self.groups)
        self.backend.lookupChecksums(self.checksums)
        self.__postprocess()
        # Uniquify the packages
        uniqdict = {}
        for package in self.batch:
            # Unique key
            key = (package["org_id"], package["source_rpm_id"])
            if key not in uniqdict:
                uniqdict[key] = package
                continue
            else:
                self._comparePackages(package, uniqdict[key])
                # And invalidate it
                package.ignored = 1
                package.first_package = uniqdict[key]

    def submit(self):
        upload_force = self.uploadForce
        if not upload_force and self._update_last_modified:
            # # Force it just a little bit - kind of hacky
            upload_force = 0.5
        try:
            self.backend.processSourcePackages(
                self.batch,
                uploadForce=upload_force,
                forceVerify=self.forceVerify,
                ignoreUploaded=self.ignoreUploaded,
                transactional=self.transactional,
            )
        except:
            # Oops
            self.backend.rollback()
            raise
        self.backend.commit()
        if not self._update_last_modified:
            # Go though the list of objects and clear out the ones that have a
            # force of 0.5
            for p in self.batch:
                if p.diff and p.diff.level == 0.5:
                    # Ignore this difference completely
                    p.diff = None
                    # Leave p.diff_result in place

    def _comparePackages(self, package1, package2):
        if (
            package1["checksum_type"] == package2["checksum_type"]
            and package1["checksum"] == package2["checksum"]
        ):
            return
        # XXX Handle this better
        # pylint: disable-next=broad-exception-raised
        raise Exception("Different packages in the same batch")

    def _processPackage(self, package):
        Import._processPackage(self, package)
        # Fix the arch
        package.arch = "src"
        package.source_rpm = package["source_rpm"]
        group = self._fix_encoding(package["package_group"]).strip()
        if group not in self.groups:
            self.groups[group] = None
        sourceRPM = package["source_rpm"]
        if not sourceRPM:
            # Should not happen
            # pylint: disable-next=broad-exception-raised
            raise Exception("Source RPM %s does not exist")
        self.sourceRPMs[sourceRPM] = None

        checksumTuple = (package["checksum_type"], package["checksum"])
        if checksumTuple not in self.checksums:
            self.checksums[checksumTuple] = None

        sigchecksumTuple = (package["sigchecksum_type"], package["sigchecksum"])
        if sigchecksumTuple not in self.checksums:
            self.checksums[sigchecksumTuple] = None

    def __postprocess(self):
        # Gather the IDs we've found

        for package in self.batch:
            if package.ignored:
                # Skip it
                continue
            # Only deal with packages
            self.__postprocessPackage(package)

    def __postprocessPackage(self, package):
        # Set the ids
        package["package_group"] = self.groups[
            self._fix_encoding(package["package_group"]).strip()
        ]
        package["source_rpm_id"] = self.sourceRPMs[package["source_rpm"]]
        package["checksum_id"] = self.checksums[
            (package["checksum_type"], package["checksum"])
        ]
        package["sigchecksum_id"] = self.checksums[
            (package["sigchecksum_type"], package["sigchecksum"])
        ]

    # pylint: disable-next=redefined-builtin
    def _cleanup_object(self, object):
        Import._cleanup_object(self, object)
        if object.ignored:
            object.id = object.first_package.id


def packageImporter(batch, backend, source=0, caller=None):
    if source:
        return SourcePackageImport(batch, backend, caller=caller)
    return PackageImport(batch, backend, caller=caller)