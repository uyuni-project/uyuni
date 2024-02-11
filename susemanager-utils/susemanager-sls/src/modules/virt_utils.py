"""
virt utility functions
"""

import logging
import os
import os.path
import re
import subprocess
from xml.etree import ElementTree

try:
    import libvirt
except ImportError:
    pass

try:
    import virt_tuner
except ImportError:
    virt_tuner = None

from salt.exceptions import CommandExecutionError

log = logging.getLogger(__name__)

__virtualname__ = "virt_utils"


# pylint: disable-next=invalid-name
def __virtual__():
    """
    Only if the virt module is loaded
    """
    return (
        __virtualname__
        # pylint: disable-next=undefined-variable
        if "virt.vm_info" in __salt__
        else (False, "Module virt_utils: virt module can't be loaded")
    )


def get_cluster_filesystem(path):
    """
    Get the cluster filesystem resource containing a path.

    :param path: the path to check
    :return: the matching Filesystem resource name or `None`
    """
    resolved = os.readlink(path)
    if not resolved.endswith("/"):
        resolved += "/"
    try:
        crm_conf = ElementTree.fromstring(
            subprocess.Popen(
                ["crm", "configure", "show", "xml"], stdout=subprocess.PIPE
            ).communicate()[0]
        )

        for resource in crm_conf.findall(".//resources/*"):
            if resource.find(".//primitive[@type='Filesystem']/") is not None:
                directory = resource.find(".//primitive//nvpair[@name='directory']")
                if directory is None:
                    continue
                directory_value = directory.get("value")
                if directory_value and resolved.startswith(directory_value):
                    return resource.get("id")
    except OSError as err:
        log.debug("Failed to get cluster resource name for path, %s: %s", path, err)

    return None


def vm_info(name=None):
    """
    Provide additional virtual machine infos
    """
    try:
        # pylint: disable-next=undefined-variable
        infos = __salt__["virt.vm_info"](name)
        all_vms = {}
        for vm_name in infos.keys():
            all_vms[vm_name] = {
                "graphics_type": infos[vm_name].get("graphics", {}).get("type", None),
            }
    # pylint: disable-next=unused-variable
    except CommandExecutionError as err:
        all_vms = {}

    # Find out which VM is managed by a cluster
    try:
        crm_status = ElementTree.fromstring(
            subprocess.Popen(
                ["crm_mon", "-1", "--output-as", "xml"],
                stdout=subprocess.PIPE,
            ).communicate()[0]
        )
        resource_states = {}
        for resource in crm_status.findall(
            ".//resources/resource[@resource_agent='ocf::heartbeat:VirtualDomain']"
        ):
            resource_states[resource.get("id")] = resource.get("active") == "true"
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
            # Provide infos on VMs managed by the cluster running on this node or not running at all
            if (
                name_node is not None
                and name_node.text in all_vms
                or not resource_states[primitive.get("id")]
            ):
                if name_node.text not in all_vms:
                    all_vms[name_node.text] = {}
                all_vms[name_node.text]["cluster_primitive"] = primitive.get("id")
                all_vms[name_node.text]["definition_path"] = path
                # Provide the UUID if possible since this will allow matching the VM with the DB record
                uuid_node = desc.find("uuid")
                if uuid_node is not None:
                    all_vms[name_node.text]["uuid"] = uuid_node.text

                # Report CPU and Memory since we may not have them in the database
                vcpu_node = desc.find("vcpu")
                if vcpu_node is not None and vcpu_node.text is not None:
                    all_vms[name_node.text]["vcpus"] = int(vcpu_node.text)
                mem_node = desc.find("./memory")
                if mem_node is not None and mem_node.text is not None:
                    all_vms[name_node.text]["memory"] = _convert_unit(
                        int(mem_node.text), mem_node.get("unit", "KiB")
                    )

                graphics_node = desc.find(".//devices/graphics")
                if graphics_node is not None:
                    all_vms[name_node.text]["graphics_type"] = graphics_node.get("type")

            # No need to parse more XML files if we already had the ones we're looking for
            if name is not None and name_node == name:
                break
    except OSError as err:
        log.debug("Failed to get cluster configuration: %s", err)

    return all_vms


def host_info():
    """
    Provide a few informations on the virtualization host for the UI to use.
    """
    cluster_nodes = []
    try:
        node_name = (
            subprocess.Popen(["crm_node", "-n"], stdout=subprocess.PIPE)
            .communicate()[0]
            .strip()
            .decode()
        )
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
    except OSError as err:
        log.debug("Failed to get cluster configuration: %s", err)

    return {
        # pylint: disable-next=undefined-variable
        "hypervisor": __salt__["virt.get_hypervisor"](),
        "cluster_other_nodes": cluster_nodes,
    }


def _convert_unit(value, unit):
    """
    Convert a size with unit into MiB
    """
    dec = False
    if re.match(r"[kmgtpezy]b$", unit.lower()):
        dec = True
    elif not re.match(r"(b|[kmgtpezy](ib)?)$", unit.lower()):
        return None
    power = "bkmgtpezy".index(unit.lower()[0])
    return int(value * (10 ** (power * 3) if dec else 2 ** (power * 10)) / (1024**2))


def vm_definition(uuid):
    """
    Get the result of virt.vm_info and the XML definition in one shot from the UUID.
    Assumes the regular form of UUID with the dashes, not the one from the DB
    """
    cnx = None
    try:
        cnx = libvirt.open()
        domain = cnx.lookupByUUIDString(uuid)
        name = domain.name()
        return {
            # pylint: disable-next=undefined-variable
            "definition": __salt__["virt.get_xml"](name),
            # pylint: disable-next=undefined-variable
            "info": __salt__["virt.vm_info"](name)[name],
        }
    except libvirt.libvirtError:
        # The VM is not defined in libvirt, may be it is defined in the cluster
        try:
            crm_conf = ElementTree.fromstring(
                subprocess.Popen(
                    ["crm", "configure", "show", "xml", "type:primitive"],
                    stdout=subprocess.PIPE,
                ).communicate()[0]
            )
            for primitive in crm_conf.findall(".//primitive[@type='VirtualDomain']"):
                config_node = primitive.find(".//nvpair[@name='config']")
                if config_node is not None:
                    config_path = config_node.get("value")
                    if config_path is not None:
                        # pylint: disable-next=unspecified-encoding
                        with open(config_path, "r") as desc_fd:
                            desc_content = desc_fd.read()
                        desc = ElementTree.fromstring(desc_content)
                        uuid_node = desc.find("./uuid")
                        if uuid_node is not None and uuid_node.text == uuid:
                            return {"definition": desc_content}
        except OSError:
            # May be this is not a cluster node
            pass
        finally:
            if cnx:
                cnx.close()
        return {}


def virt_tuner_templates():
    """
    Get the virt-tuner templates names
    """
    if virt_tuner:
        return sorted(list(virt_tuner.templates.keys()))
    return []


def domain_parameters(cpu, mem, template):
    """
    Return the VM parameters with the potential virt-tuner template applied
    """
    params = {"cpu": cpu, "mem": mem}
    if virt_tuner and template in virt_tuner.templates:
        template_params = virt_tuner.templates[template].function()
        params.update(template_params)
    return params
