"""AppStreams module for SUSE Manager"""

import re
import subprocess
import shutil
import logging

log = logging.getLogger(__name__)


def _get_enabled_module_names():
    # Run the DNF command to list enabled modules
    command = ["dnf", "module", "list", "--enabled", "--quiet"]
    result = subprocess.run(command, capture_output=True, text=True)

    # Check if the command was successful
    if result.returncode == 0:
        try:
            # Split the text output by lines
            lines = result.stdout.splitlines()

            # Find the index where the actual module information starts
            start_index = next((i for i, line in enumerate(lines) if "Name" in line and "Stream" in line), None)

            # Find the index where the module information ends
            end_index = next((i for i, line in enumerate(lines) if not line), len(lines))

            # Extract module names
            if start_index is not None:
                module_names = [
                    f"{parts[0]}:{parts[1]}"
                    for line in lines[start_index + 1:end_index] # Skip the header line
                    for parts in [line.split()]
                ]
                return module_names
            else:
                log.error("Error: Unable to find module information in the output.")

        except (IndexError, ValueError) as e:
            log.error(f"Error parsing output: {e}")

    else:
        log.error(f"Error running DNF command: {result.stderr}")

def _parse_nsvca(module_info_output):
    attrs = {
        "name": re.compile(r'^Name\s+:\s+(\S+).*$'),
        "stream": re.compile(r'^Stream\s+:\s+(\S+).*$'),
        "version": re.compile(r'^Version\s+:\s+(\S+).*$'),
        "context": re.compile(r'^Context\s+:\s+(\S+).*$'),
        "architecture": re.compile(r'^Architecture\s+:\s+(\S+).*$'),
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
    # Run the DNF command to get module info for all modules
    command = ["dnf", "module", "info", "--quiet"] + module_names
    result = subprocess.run(command, capture_output=True, text=True)

    # Check if the command was successful
    if result.returncode == 0:
        # Split the output into lines
        module_info_output = result.stdout.splitlines()

        # Parse NSVCA information for each module
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

    else:
        log.error(f"Error running DNF command: {result.stderr}")
        return []

def _is_dnf_available():
    return shutil.which("dnf") is not None

def get_enabled_modules():
    if not _is_dnf_available():
        return []

    enabled_module_names = _get_enabled_module_names()
    if enabled_module_names:
        return _get_module_info(enabled_module_names)
    else:
        return []
