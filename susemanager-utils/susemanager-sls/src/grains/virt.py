"""
Grains for virtualization hosts
"""

import logging
import re
import subprocess
import salt.modules.virt

try:
    from salt.utils.path import which_bin as _which_bin
except ImportError:
    from salt.utils import which_bin as _which_bin

from xml.etree import ElementTree


log = logging.getLogger(__name__)


# pylint: disable-next=invalid-name
def __virtual__():
    return salt.modules.virt.__virtual__() and _which_bin(["libvirtd"]) is not None


def features():
    """returns the features map of the virt module"""
    try:
        in_cluster = subprocess.check_call(["crm", "status"]) == 0
    # pylint: disable-next=broad-exception-caught
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
    # pylint: disable-next=broad-exception-caught
    except Exception:
        start_resources_ra = False

    libvirt_version = -1
    try:
        version_out = subprocess.Popen(
            ["libvirtd", "-V"], stdout=subprocess.PIPE
        ).communicate()[0]
        # pylint: disable-next=anomalous-backslash-in-string
        matcher = re.search(b"(\d+)\.(\d+)\.(\d+)", version_out)
        if matcher:
            libvirt_version = 0
            for idx in range(len(matcher.groups())):
                libvirt_version += int(matcher.group(idx + 1)) * 1000 ** (
                    len(matcher.groups()) - idx - 1
                )
    except OSError:
        log.error("libvirtd is not installed or is not in the PATH")

    return {
        "virt_features": {
            "enhanced_network": "network_update" in salt.modules.virt.__dict__,
            "cluster": in_cluster,
            "resource_agent_start_resources": start_resources_ra,
            # Libvirt has the firmware='efi' support since 5.2, but vital fixes came in 5.3 only
            "uefi_auto_loader": libvirt_version >= 5003000,
        },
    }
