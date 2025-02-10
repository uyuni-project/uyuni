import os
import re

__virtualname__ = "sap"
__grains__ = {}
SAP_REGEX = re.compile(r"/usr/sap/([A-Z][A-Z0-9]{2})/([A-Z]+)(\d{2})\b")

def __virtual__():
    """
    Only load the module if the operating system is SLES.
    """
    if __grains__.get("os_family") == "Suse":
        return True
    return (False, "This module is only available on SLES systems.")

def get_workloads():
    """
    Detect SAP workloads based on filesystem structure.

    Returns:
        list: List of detected SAP systems with their system ID and instance types, or an empty list if none are found.
    """
    sap_systems = []
    base_path = "/usr/sap"
    if not os.path.exists(base_path):
        return []

    for root, dirs, files in os.walk(base_path):
        for dir_name in dirs:
            dir_path = os.path.join(root, dir_name)
            match = SAP_REGEX.match(dir_path)
            if match:
                system_id = match.group(1)
                instance_type = match.group(2)
                sap_systems.append({"system_id": system_id, "instance_type": instance_type})

    return sap_systems
