# -*- coding: utf-8 -*-
"""
Watch pending transactions in transactional systems and
fire an event to SUSE Manager indicating if a reboot is needed.
"""

__virtualname__ = "reboot_info"


def __virtual__():
    return __grains__.get("transactional", False)


def validate(config):
    """
    The absence of this function could cause noisy logging,
    when logging level set to DEBUG or TRACE.
    So we need to have it with no any validation inside.
    """
    return True, "There is nothing to validate"


def beacon(config):
    """
    Monitor pending transactions of transactional update
    to verify whether a reboot is required. When the reboot
    needed status changes, it fires a new event.

    Example Config

    .. code-block:: yaml

        beacons:
          reboot_info:
            interval: 5

    """
    ret = []
    reboot_needed = __salt__["transactional_update.pending_transaction"]()

    if __context__.get("reboot_needed") != reboot_needed:
        ret.append({"reboot_needed": reboot_needed})
        __context__["reboot_needed"] = reboot_needed

    return ret
