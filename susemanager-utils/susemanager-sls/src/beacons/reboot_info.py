# -*- coding: utf-8 -*-
"""
Watch pending transactions in transactional systems and
fire an event to SUSE Manager indicating if a reboot is needed.
"""

__virtualname__ = "reboot_info"


def __virtual__():  #  pylint: disable=invalid-name
    return __grains__.get("transactional", False)  #  pylint: disable=undefined-variable


def validate(config):  #  pylint: disable=unused-argument
    """
    The absence of this function could cause noisy logging,
    when logging level set to DEBUG or TRACE.
    So we need to have it with no any validation inside.
    """
    return True, "There is nothing to validate"


def beacon(config):  #  pylint: disable=unused-argument
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
    reboot_needed = __salt__["transactional_update.pending_transaction"]()  #  pylint: disable=undefined-variable

    if __context__.get("reboot_needed") != reboot_needed:  #  pylint: disable=undefined-variable
        ret.append({"reboot_needed": reboot_needed})
        __context__["reboot_needed"] = reboot_needed  #  pylint: disable=undefined-variable

    return ret
