# -*- coding: utf-8 -*-
"""
SUSE Manager Action Chains module for Salt

"""
from __future__ import absolute_import

import logging
import os

# pylint: disable-next=unused-import
import sys
import salt.config
import salt.syspaths
import yaml
from salt.utils.yamlloader import SaltYamlSafeLoader

# Prevent issues due 'salt.utils.fopen' deprecation
try:
    from salt.utils import fopen
# pylint: disable-next=bare-except
except:
    from salt.utils.files import fopen

from salt.exceptions import CommandExecutionError

log = logging.getLogger(__name__)

__virtualname__ = "mgractionchains"

SALT_ACTIONCHAIN_BASE = "actionchains"


# pylint: disable-next=invalid-name
def __virtual__():
    """
    This module is always enabled while 'state.sls' is available.
    """
    return (
        __virtualname__
        # pylint: disable-next=undefined-variable
        if "state.sls" in __salt__
        else (False, "state.sls is not available")
    )


def _calculate_sls(actionchain_id, machine_id, chunk):
    # pylint: disable-next=consider-using-f-string
    return "{0}.actionchain_{1}_{2}_{3}".format(
        SALT_ACTIONCHAIN_BASE, actionchain_id, machine_id, chunk
    )


def _get_ac_storage_filenamepath():
    """
    Calculate the filepath to the '_mgractionchains.conf' which is placed
    by default in /etc/salt/minion.d/
    """
    # pylint: disable-next=undefined-variable
    config_dir = __opts__.get("conf_dir", None)
    # pylint: disable-next=undefined-variable
    if config_dir is None and "conf_file" in __opts__:
        # pylint: disable-next=undefined-variable
        config_dir = os.path.dirname(__opts__["conf_file"])
    if config_dir is None:
        config_dir = salt.syspaths.CONFIG_DIR

    minion_d_dir = os.path.join(
        config_dir,
        os.path.dirname(
            # pylint: disable-next=undefined-variable
            __opts__.get(
                "default_include", salt.config.DEFAULT_MINION_OPTS["default_include"]
            )
        ),
    )

    return os.path.join(minion_d_dir, "_mgractionchains.conf")


def check_reboot_required(target_sls):
    """
    Used this function for transactional update system.
    Check if the sls file contains reboot_required paramer in schedule_next_chuck.
    If it exists and set to true, the system is reboot when the sls file execution is completed
    :param target_sls: sls filename
    :return: True if the system requires a reboot at the end of the transaction
    """
    # pylint: disable-next=undefined-variable
    sls_file_on_minion = __salt__["cp.cache_file"](
        # pylint: disable-next=consider-using-f-string
        "{0}{1}.sls".format(
            "salt://actionchains/", target_sls.replace("actionchains.", "")
        )
    )
    current_state_info = _read_sls_file(sls_file_on_minion)
    if not current_state_info or not "schedule_next_chunk" in current_state_info:
        # schedule_next_chunk contains information about how to restart the action chain after a reboot, so it's present
        # only if there's a reboot action or a salt upgrade. If there's no action that perform a reboot, schedule_next_chunk
        # it's not present.
        return False
    if not "mgrcompat.module_run" in current_state_info["schedule_next_chunk"]:
        log.error(
            'Cannot check if reboot is needed as "schedule_next_chunk" is not containing expected attributes.'
        )
        return False

    list_param = current_state_info["schedule_next_chunk"]["mgrcompat.module_run"]

    for dic in list_param:
        if "reboot_required" in dic:
            return dic["reboot_required"]
    return False


def _read_next_ac_chunk(clear=True):
    """
    Read and remove the content of '_mgractionchains.conf' file. Return the parsed YAML.
    """
    f_storage_filename = _get_ac_storage_filenamepath()
    ret = _read_sls_file(f_storage_filename)
    if ret is None:
        return None
    if clear:
        os.remove(f_storage_filename)
    return ret


def _read_sls_file(filename):
    if not os.path.isfile(filename):
        # pylint: disable-next=logging-format-interpolation,consider-using-f-string
        log.debug("File {0} does not exists".format(filename))
        return None
    ret = None
    try:
        with fopen(filename, "r") as f:
            ret = yaml.load(f.read(), Loader=SaltYamlSafeLoader)
        return ret
    except (IOError, yaml.scanner.ScannerError) as exc:
        # pylint: disable-next=consider-using-f-string
        err_str = "Error processing YAML from '{0}': {1}".format(filename, exc)
        log.error(err_str)
        # pylint: disable-next=raise-missing-from
        raise CommandExecutionError(err_str)


def _add_boot_time(next_chunk, prefix):
    """
    Add the current boot time to the next_chunk dict
    """
    # pylint: disable-next=undefined-variable
    uptime = __salt__["status.uptime"]()
    # pylint: disable-next=consider-using-f-string
    next_chunk["{0}_boot_time".format(prefix)] = uptime["since_iso"]


def _persist_next_ac_chunk(next_chunk):
    """
    Persist next_chunk to execute as YAML in '_mgractionchains.conf'
    """
    _add_boot_time(next_chunk, "persist")
    f_storage_filename = _get_ac_storage_filenamepath()
    try:
        f_storage_dir = os.path.dirname(f_storage_filename)
        if not os.path.exists(f_storage_dir):
            os.makedirs(f_storage_dir)
        with fopen(f_storage_filename, "w") as f_storage:
            f_storage.write(yaml.dump(next_chunk))
    except (IOError, yaml.scanner.ScannerError) as exc:
        # pylint: disable-next=consider-using-f-string
        err_str = "Error writing YAML from '{0}': {1}".format(f_storage_filename, exc)
        log.error(err_str)
        # pylint: disable-next=raise-missing-from
        raise CommandExecutionError(err_str)


def start(actionchain_id):
    """
    Start the execution of the given SUSE Manager Action Chain

    actionchain_id
        The SUSE Manager Actionchain ID to execute on this minion.

    CLI Example:

    .. code-block:: bash

        salt '*' mgractionchains.start 123
    """
    if os.path.isfile(_get_ac_storage_filenamepath()):
        msg = (
            # pylint: disable-next=consider-using-f-string
            "Action Chain '{0}' cannot be started. There is already another "
            "Action Chain being executed. Please check file '{1}'".format(
                actionchain_id, _get_ac_storage_filenamepath()
            )
        )
        log.error(msg)
        raise CommandExecutionError(msg)
    # pylint: disable-next=undefined-variable
    target_sls = _calculate_sls(actionchain_id, __grains__["machine_id"], 1)
    log.debug(
        # pylint: disable-next=logging-format-interpolation,consider-using-f-string
        "Starting execution of SUSE Manager Action Chains ID "
        "'{0}' -> Target SLS: {1}".format(actionchain_id, target_sls)
    )
    try:
        # pylint: disable-next=undefined-variable
        __salt__["saltutil.sync_states"]()
        # pylint: disable-next=undefined-variable
        __salt__["saltutil.sync_modules"]()
    # pylint: disable-next=broad-exception-caught,unused-variable
    except Exception as exc:
        log.error(
            "There was an error while syncing custom states and execution modules"
        )

    # pylint: disable-next=undefined-variable
    transactional_update = __grains__.get("transactional")
    reboot_required = False
    inside_transaction = False
    if transactional_update:
        reboot_required = check_reboot_required(target_sls)
        inside_transaction = os.environ.get("TRANSACTIONAL_UPDATE")

    if transactional_update and not inside_transaction:
        # pylint: disable-next=undefined-variable
        ret = __salt__["transactional_update.sls"](
            target_sls, queue=True, activate_transaction=False
        )
    else:
        # pylint: disable-next=undefined-variable
        ret = __salt__["state.sls"](target_sls, queue=True)

    if reboot_required:
        # pylint: disable-next=undefined-variable
        __salt__["transactional_update.reboot"]()

    if isinstance(ret, list):
        raise CommandExecutionError(ret)
    return ret


# pylint: disable-next=redefined-builtin
def next(
    actionchain_id,
    chunk,
    next_action_id=None,
    current_action_id=None,
    ssh_extra_filerefs=None,
    reboot_required=False,
):
    """
    Persist the next Action Chain chunk to be executed by the 'resume' method.

    next_chunk
        The next target SLS to be executed.

    CLI Example:

    .. code-block:: bash

        salt '*' mgractionchains.next actionchains.actionchain_123_machineid_2
    """
    yaml_dict = {
        # pylint: disable-next=undefined-variable
        "next_chunk": _calculate_sls(actionchain_id, __grains__["machine_id"], chunk)
    }
    yaml_dict["actionchain_id"] = actionchain_id
    if next_action_id:
        yaml_dict["next_action_id"] = next_action_id
    if current_action_id:
        yaml_dict["current_action_id"] = current_action_id
    if ssh_extra_filerefs:
        yaml_dict["ssh_extra_filerefs"] = ssh_extra_filerefs
    if reboot_required:
        yaml_dict["reboot_required"] = reboot_required
    _persist_next_ac_chunk(yaml_dict)
    return yaml_dict


def get_pending_resume():
    """
    Get information about any pending action chain chunk execution.
    """
    next_chunk = _read_next_ac_chunk(False)
    if next_chunk:
        _add_boot_time(next_chunk, "current")
    return next_chunk or {}


def resume():
    """
    Continue the execution of a SUSE Manager Action Chain.
    This will trigger the execution of the next chunk SLS file stored on '_mgractionchains.conf'

    This method is called by the Salt Reactor as a response to the 'minion/start/event'.
    """
    ac_resume_info = _read_next_ac_chunk()
    if not ac_resume_info:
        return {}
    # pylint: disable-next=unidiomatic-typecheck
    if type(ac_resume_info) != dict:
        err_str = (
            # pylint: disable-next=consider-using-f-string
            "Not able to resume Action Chain execution! Malformed "
            "'_mgractionchains.conf' found: {0}".format(ac_resume_info)
        )
        log.error(err_str)
        raise CommandExecutionError(err_str)
    next_chunk = ac_resume_info.get("next_chunk")
    log.debug(
        # pylint: disable-next=logging-format-interpolation,consider-using-f-string
        "Resuming execution of SUSE Manager Action Chain -> Target SLS: "
        "{0}".format(next_chunk)
    )

    # pylint: disable-next=undefined-variable
    transactional_update = __grains__.get("transactional")
    reboot_required = False
    inside_transaction = False
    if transactional_update:
        reboot_required = ac_resume_info.get("reboot_required")
        inside_transaction = os.environ.get("TRANSACTIONAL_UPDATE")

    if transactional_update and not inside_transaction:
        # pylint: disable-next=undefined-variable
        ret = __salt__["transactional_update.sls"](
            next_chunk, queue=True, activate_transaction=False
        )
    else:
        # pylint: disable-next=undefined-variable
        ret = __salt__["state.sls"](next_chunk, queue=True)

    if reboot_required:
        # pylint: disable-next=undefined-variable
        __salt__["transactional_update.reboot"]()

    if isinstance(ret, list):
        raise CommandExecutionError(ret)
    return ret


def clean(actionchain_id=None, current_action_id=None, reboot_required=None):
    """
    Clean execution of an Action Chain by removing '_mgractionchains.conf'.
    """
    _read_next_ac_chunk()
    yaml_dict = {}
    yaml_dict["success"] = True
    if actionchain_id:
        yaml_dict["actionchain_id"] = actionchain_id
    if current_action_id:
        yaml_dict["current_action_id"] = current_action_id
    if reboot_required:
        yaml_dict["reboot_required"] = reboot_required
    return yaml_dict
