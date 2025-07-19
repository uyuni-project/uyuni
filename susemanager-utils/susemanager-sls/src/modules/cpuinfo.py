"""
CPU Architecture Metadata Detection Module for SaltStack

This module provides functionality to collect extended CPU architecture-specific metadata from the target system.

It detects the system architecture and gathers relevant hardware details based on the architecture, such as information
for PowerPC (ppc64), ARM (arm64), and IBM Z (s390) systems.

References:
    - Most of the code was ported from
    https://github.com/SUSE/connect-ng/blob/main/internal/collectors/cpu.go
"""

import logging
import re
import subprocess

log = logging.getLogger(__name__)

def arch_specs():
    """
    Collect extended CPU architecture-specific metadata.
    """
    specs = {}
    arch = _get_architecture()

    if arch in ["ppc64", "ppc64le"]:
        _add_ppc64_extras(specs)
    elif arch in ["arm64", "aarch64"]:
        _add_arm64_extras(specs)
    elif arch.startswith("s390"):
        _add_z_systems_extras(specs)

    return specs

def _get_architecture():
    """
    Detect the system architecture.
    """
    try:
        return subprocess.check_output(["uname", "-m"], stderr=subprocess.PIPE).decode().strip()
    except (FileNotFoundError, subprocess.CalledProcessError, UnicodeDecodeError):
        log.warning("Failed to determine system architecture. Falling back to 'unknown'.", exc_info=True)
        return "unknown"

def _add_ppc64_extras(specs):
    _add_device_tree(specs)

    lparcfg_content = _read_file("/proc/ppc64/lparcfg")
    if lparcfg_content:
        match = re.search(r"shared_processor_mode\s*=\s*(\d+)", lparcfg_content)
        if match:
            specs["lpar_mode"] = "shared" if match.group(1) == "1" else "dedicated"

def _add_arm64_extras(specs):
    _add_device_tree(specs)
    try:
        output = subprocess.check_output(["dmidecode", "-t", "processor"], stderr=subprocess.PIPE).decode()
        specs["family"] = _exact_string_match("Family", output)
        specs["manufacturer"] = _exact_string_match("Manufacturer", output)
        specs["signature"] = _exact_string_match("Signature", output)
    except (OSError):
        log.warning("Failed to retrieve arm64 CPU details.", exc_info=True)

def _add_z_systems_extras(specs):
    """
    Collect extended metadata for z systems based on `read_values -s`.
    """
    try:
        output = subprocess.check_output(["read_values", "-s"], stderr=subprocess.PIPE).decode()
        if "VM00" in output:
            specs["hypervisor"] = "zvm"
            specs["type"] = _exact_string_match("Type", output)
            specs["layer_type"] = _exact_string_match("VM00 Name", output)
        elif "LPAR" in output:
            specs["hypervisor"] = "lpar"
            specs["type"] = _exact_string_match("Type", output)
            specs["layer_type"] = _exact_string_match("LPAR Name", output)

        specs["sockets"] = _exact_string_match("Sockets", output)
        
    except (FileNotFoundError, subprocess.CalledProcessError):
        log.warning("Failed to retrieve z system CPU details.", exc_info=True)

def _add_device_tree(specs):
    """
    Attempts to read the device tree from predefined paths.
    """
    device_tree_paths = [
        "/sys/firmware/devicetree/base/compatible",
        "/sys/firmware/devicetree/base/hypervisor/compatible",
    ]
    for path in device_tree_paths:
        content = _read_file(path)
        if content:
            specs["device_tree"] = content.replace("\x00", "").strip()
            break

def _exact_string_match(key, text):
    """
    Extract a value based on a key in the text.
    """
    match = re.search(rf"{key}\s*:\s*(.*)", text)
    return match.group(1).strip() if match else ""

def _read_file(path):
    """
    Helper to read a file and return its content.
    """
    try:
        with open(path, "r", errors="replace") as f:
            return f.read()
    except FileNotFoundError:
        return ""
