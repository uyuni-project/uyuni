# SPDX-FileCopyrightText: 2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0
# pylint: disable=missing-module-docstring

import glob
import os
import re

__virtualname__ = "sap"
__grains__ = {}
SAP_BASE_PATH = "/usr/sap"
SAP_REGEX = re.compile(r"/usr/sap/([A-Z][A-Z0-9]{2})/([A-Z]+)(\d{2})\b")


# pylint: disable-next=invalid-name
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
    if not os.path.exists(SAP_BASE_PATH):
        return []

    for dir_path in glob.iglob(
        f"{SAP_BASE_PATH}/[A-Z][A-Z0-9][A-Z0-9]/[A-Z0-9]*[0-9][0-9]/"
    ):
        match = SAP_REGEX.match(dir_path)
        if match:
            system_id = match.group(1)
            instance_type = match.group(2)
            sap_systems.append({"system_id": system_id, "instance_type": instance_type})

    sap_systems.sort(key=lambda x: (x["system_id"], x["instance_type"]))

    return sap_systems
