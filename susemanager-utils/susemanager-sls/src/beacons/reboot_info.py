# -*- coding: utf-8 -*-
"""
Watch system status and fire an event to SUSE Manager indicating 
when a reboot is required.
"""

__virtualname__ = "reboot_info"


def __virtual__():
    '''
    Run on Debian, Suse and RedHat systems.
    '''
    return __grains__['os_family'] in ['Debian', 'Suse', 'RedHat']


def validate(config):
    """
    The absence of this function could cause noisy logging,
    when logging level set to DEBUG or TRACE.
    So we need to have it with no any validation inside.
    """
    return True, "There is nothing to validate"


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

    result = __salt__["reboot_info.reboot_required"]()
    reboot_needed = result.get("reboot_required", False)

    if reboot_needed and not __context__.get("reboot_needed", False):
        ret.append({"reboot_needed": reboot_needed})

    __context__["reboot_needed"] = reboot_needed
    return ret
