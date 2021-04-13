"""
virt utility functions
"""

import logging
from pathlib import Path
import os.path
import subprocess
from xml.etree import ElementTree

log = logging.getLogger(__name__)

__virtualname__ = "virt_utils"


def __virtual__():
    """
    Only if the virt module is loaded
    """
    return (
        __virtualname__
        if "virt.vm_info" in __salt__
        else (False, "Module virt_utils: virt module can't be loaded")
    )


def get_cluster_filesystem(path):
    """
    Get the cluster filesystem resource containing a path.

    :param path: the path to check
    :return: the matching Filesystem resource name or `None`
    """
    resolved = Path(path).resolve()
    try:
        crm_conf = ElementTree.fromstring(
            subprocess.Popen(
                ["crm", "configure", "show", "xml"], stdout=subprocess.PIPE
            ).communicate()[0]
        )

        for resource in crm_conf.findall(".//primitive[@type='Filesystem']/.."):
            directory = resource.find("./primitive//nvpair[@name='directory']")
            if directory is None:
                continue
            directory_value = directory.get("value")
            if directory_value:
                if os.path.commonpath([str(resolved), directory_value]) == directory_value:
                    return resource.get("id")
    except FileNotFoundError as err:
        log.debug("Failed to get cluster resource name for path, %s: %s", path, err)

    return None
