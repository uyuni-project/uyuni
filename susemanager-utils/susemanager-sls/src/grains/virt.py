"""
Grains for virtualization hosts
"""

import subprocess
from xml.etree import ElementTree
import salt.modules.virt


def __virtual__():
    return salt.modules.virt.__virtual__()


def features():
    """returns the features map of the virt module"""
    try:
        in_cluster = subprocess.check_call(["crm", "status"]) == 0
    except Exception:
        in_cluster = False

    try:
        ra_conf = ElementTree.fromstring(
            subprocess.Popen(
                ["crm_resource", "--show-metadata", "ocf:heartbeat:VirtualDomain"],
                stdout=subprocess.PIPE,
            ).communicate()[0]
        )
        start_resources_ra = (
            ra_conf.find(".//parameter[@name='start_resources']") is not None
        )
    except Exception:
        start_resources_ra = False

    return {
        "virt_features": {
            "enhanced_network": "network_update" in salt.modules.virt.__dict__,
            "cluster": in_cluster,
            "resource_agent_start_resources": start_resources_ra,
        },
    }
