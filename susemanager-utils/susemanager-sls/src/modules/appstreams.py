# SPDX-FileCopyrightText: 2024-2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

"""AppStreams module for SUSE Multi-Linux Manager"""

import re
import subprocess
import logging

try:
    from salt.utils.path import which as _which
except ImportError:
    from salt.utils import which as _which

log = logging.getLogger(__name__)


# pylint: disable-next=invalid-name
def __virtual__():
    """
    Only works on RH-like systems having 'dnf' available
    """
    # pylint: disable-next=superfluous-parens
    if not (_which("dnf")):
        return (False, "dnf is not available on the system")
    return True


def _get_enabled_module_names():
    # Run the DNF command to list enabled modules
    command = ["dnf", "module", "list", "--enabled", "--quiet"]
    # pylint: disable-next=subprocess-run-check
    result = subprocess.run(command, capture_output=True, text=True)

    # Check if the command was successful
    if result.returncode == 0:
        try:
            # Split the text output by lines
            lines = result.stdout.splitlines()

            # Find the indexes where the actual module information starts
            start_indexes = [
                i for i, line in enumerate(lines) if "Name" in line and "Stream" in line
            ]
            all_module_names = []

            if start_indexes:
                for start_index in start_indexes:
                    # Find the index where the module information ends
                    end_index = next(
                        (
                            i
                            for i, line in enumerate(lines)
                            if not line and i > start_index
                        ),
                        len(lines),
                    )

                    # Extract module names
                    module_names = [
                        f"{parts[0]}:{parts[1]}"
                        for line in lines[
                            start_index + 1 : end_index
                        ]  # Skip the header line
                        for parts in [line.split()]
                    ]
                    all_module_names += module_names
                return all_module_names
            else:
                log.error("Error: Unable to find module information in the output.")

        except (IndexError, ValueError) as e:
            # pylint: disable-next=logging-fstring-interpolation
            log.error(f"Error parsing output: {e}")

    else:
        # pylint: disable-next=logging-fstring-interpolation
        log.error(f"Error running DNF command: {result.stderr}")


def _parse_nsvca(module_info_output):
    attrs = {
        "name": re.compile(r"^Name\s+:\s+(\S+)"),
        "stream": re.compile(r"^Stream\s+:\s+(\S+)"),
        "version": re.compile(r"^Version\s+:\s+(\S+)"),
        "context": re.compile(r"^Context\s+:\s+(\S+)"),
        "architecture": re.compile(r"^Architecture\s+:\s+(\S+)"),
    }
    result = {}

    for line in module_info_output:
        for attr, regex in attrs.items():
            if result.get(attr):
                continue

            match = regex.match(line)
            if match:
                result[attr] = match.group(1)

    return result if result.keys() == attrs.keys() else None


def _get_module_info(module_names):
    # Run the DNF command to get module info for all active modules
    # Parse all modules if no active ones are present
    command = ["dnf", "module", "info", "--quiet"] + module_names
    # pylint: disable-next=subprocess-run-check
    result = subprocess.run(command, capture_output=True, text=True)

    if result.returncode != 0:
        # pylint: disable-next=logging-fstring-interpolation
        log.error(f"Error running DNF command: {result.stderr}")
        return []

    # Active modules are marked with [a]
    # Example output
    # Name             : perl-IO-Socket-SSL
    # Stream           : 2.066 [d][e][a]
    # Version          : 8090020231016070024
    # Context          : 88fd4976
    # Architecture     : x86_64
    # Profiles         : common [d]
    # Default profiles : common
    # Repo             : susemanager:rockylinux8-x86_64-appstream
    # Summary          : Perl library for transparent TLS
    # Description      : IO::Socket::SSL is a drop-in replacement for ...
    # Requires         : perl:[5.26]
    #                  : platform:[el8]
    # Artifacts        : perl-IO-Socket-SSL-0:2.066-4.module+el8.9.0+1517+e71a7a62.noarch

    module_info_output = []

    for module in re.findall(r"(Name\s+:.*?)(?=\n\s*\n|$)", result.stdout, re.DOTALL):
        if re.search(r"Stream\s+:.*\[a\]", module):
            module_info_output += module.splitlines()

    # Parse all modules, if no active ones were found
    if not module_info_output:
        module_info_output = result.stdout.splitlines()

    nsvca_info_list = []
    current_module_info = []
    for line in module_info_output:
        # Check if the line starts with "Name" to identify the beginning of a new module info
        if line.startswith("Name"):
            if current_module_info:
                nsvca_info = _parse_nsvca(current_module_info)
                if nsvca_info:
                    nsvca_info_list.append(nsvca_info)
            # Start collecting info for the new module
            current_module_info = [line]
        else:
            current_module_info.append(line)

    # Parse NSVCA information for the last module
    if current_module_info:
        nsvca_info = _parse_nsvca(current_module_info)
        if nsvca_info:
            nsvca_info_list.append(nsvca_info)

    return nsvca_info_list


def _execute_action(action, appstreams):
    """
    Execute the specified action (enable/disable) for the given appstreams.
    action
        The action to perform (either "enable" or "disable")
    appstreams
        List or string of appstreams to perform the action on

    Returns:
        Tuple: (result, comment, changes)
    """
    if isinstance(appstreams, str):
        appstreams = [appstreams]

    cmd = ["dnf", "module", action, "-y"] + appstreams
    return subprocess.run(cmd, check=False, capture_output=True)


def enable(appstreams):
    """
    Enable the specified appstreams using dnf.
    appstreams
        List or string of appstreams to enable

    Returns:
        Tuple: (result, comment, changes)
    """
    result = True
    comment = ""
    changes = {}

    before = get_enabled_modules()
    cmd_result = _execute_action("enable", appstreams)
    if cmd_result.returncode == 0:
        after = get_enabled_modules()
        enabled = [m for m in after if m not in before]
        if enabled:
            comment = "AppStreams enabled."
            changes = {"enabled": enabled}
        else:
            comment = "Nothing changed."
    else:
        result = False
        comment = cmd_result.stderr.decode("utf-8").strip()

    return result, comment, changes


def disable(appstreams):
    """
    Disable the specified appstreams using dnf.
    appstreams
        List or string of appstreams to disable

    Returns:
        Tuple: (result, comment, changes)
    """
    result = True
    comment = ""
    changes = {}

    before = get_enabled_modules()
    cmd_result = _execute_action("disable", appstreams)
    if cmd_result.returncode == 0:
        after = get_enabled_modules()
        disabled = [m for m in before if m not in after]
        if disabled:
            comment = "AppStreams disabled."
            changes = {"disabled": disabled}
        else:
            comment = "Nothing changed."
    else:
        result = False
        comment = cmd_result.stderr.decode("utf-8").strip()

    return result, comment, changes


def get_enabled_modules():
    enabled_module_names = _get_enabled_module_names()
    return _get_module_info(enabled_module_names) if enabled_module_names else []
