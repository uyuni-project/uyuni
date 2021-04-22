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
                if (
                    os.path.commonpath([str(resolved), directory_value])
                    == directory_value
                ):
                    return resource.get("id")
    except FileNotFoundError as err:
        log.debug("Failed to get cluster resource name for path, %s: %s", path, err)

    return None


def vm_info(name=None):
    """
    Provide additional virtual machine infos
    """
    infos = __salt__["virt.vm_info"](name)
    all_vms = {
        vm_name: {
            "graphics_type": infos[vm_name].get("graphics", {}).get("type", None),
        }
        for vm_name in infos.keys()
    }

    # Find out which VM is managed by a cluster
    try:
        crm_conf = ElementTree.fromstring(
            subprocess.Popen(
                ["crm", "configure", "show", "xml", "type:primitive"],
                stdout=subprocess.PIPE,
            ).communicate()[0]
        )
        for primitive in crm_conf.findall(".//primitive[@type='VirtualDomain']"):
            config_node = primitive.find(".//nvpair[@name='config']")
            if config_node is None:
                continue
            path = config_node.get("value")
            if path is None:
                continue
            desc = ElementTree.parse(path)
            name_node = desc.find("./name")
            # Don't provide infos on VMs managed by the cluster that aren't running on this node
            if name_node is not None and name_node.text in all_vms:
                all_vms[name_node.text]["cluster_primitive"] = primitive.get("id")
                all_vms[name_node.text]["definition_path"] = path

            # No need to parse more XML files if we already had the ones we're looking for
            if name is not None and name_node == name:
                break
    except FileNotFoundError as err:
        log.debug("Failed to get cluster configuration: %s", err)

    return all_vms


def host_info():
    """
    Provide a few informations on the virtualization host for the UI to use.
    """
    cluster_nodes = []
    try:
        node_name = subprocess.Popen(["crm_node", "-n"], stdout=subprocess.PIPE).communicate()[0].strip().decode()
        crm_conf = ElementTree.fromstring(
            subprocess.Popen(
                ["crm", "configure", "show", "xml", "type:node"],
                stdout=subprocess.PIPE,
            ).communicate()[0]
        )
        cluster_nodes = [
            node.get("uname")
            for node in crm_conf.findall(".//node")
            if node.get("uname") != node_name
        ]
    except FileNotFoundError as err:
        log.debug("Failed to get cluster configuration: %s", err)

    return {
        "hypervisor": __salt__["virt.get_hypervisor"](),
        "cluster_other_nodes": cluster_nodes,
    }
