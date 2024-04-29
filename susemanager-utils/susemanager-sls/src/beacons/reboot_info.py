# -*- coding: utf-8 -*-
"""
Watch system status and fire an event to SUSE Manager indicating 
when a reboot is required.
"""

import logging


log = logging.getLogger(__name__)


__virtualname__ = "reboot_info"


# pylint: disable-next=invalid-name
def __virtual__():
    """
    Run on Debian, Suse and RedHat systems.
    """
    # pylint: disable-next=undefined-variable
    return __grains__["os_family"] in ["Debian", "Suse", "RedHat"]


# pylint: disable-next=unused-argument
def validate(config):
    """
    The absence of this function could cause noisy logging,
    when logging level set to DEBUG or TRACE.
    So we need to have it with no any validation inside.
    """
    return True, "There is nothing to validate"


# pylint: disable-next=unused-argument
def beacon(config):
    """
    Monitor system status to verify whether a reboot
    is required. The first time it detects that a reboot
    is necessary, it fires an event.

    Example Config

    .. code-block:: yaml

        beacons:
          reboot_info:
            interval: 5

    """
    ret = []

    # pylint: disable-next=undefined-variable
    try:
        result = __salt__["reboot_info.reboot_required"]()
    except KeyError:
        # reboot_info salt module could be not yet in synchronized
        return ret
    except Exception as e:  # pylint: disable=broad-except
        log.error(
            "Error while executing 'reboot_info.reboot_required': %s",
            e,
            exc_info=True,
        )
        return ret
    reboot_needed = result.get("reboot_required", False)

    # pylint: disable-next=undefined-variable
    if reboot_needed and not __context__.get("reboot_needed", False):
        ret.append({"reboot_needed": reboot_needed})

    # pylint: disable-next=undefined-variable
    __context__["reboot_needed"] = reboot_needed
    return ret
