# -*- coding: utf-8 -*-
'''
SUSE Manager CaaSP Cluster Manager module for Salt

'''
from __future__ import absolute_import

from salt.exceptions import CommandExecutionError

import logging
import subprocess
import salt.utils.path
import salt.utils.stringutils
import salt.utils.timed_subprocess

log = logging.getLogger(__name__)

__virtualname__ = 'caasp'

DEFAULT_TIMEOUT = 1200


def __virtual__():
    '''
    This module is always enabled while 'skuba' CLI tools is available.
    '''
    return __virtualname__ if salt.utils.path.which('skuba') else (False, 'skuba is not available')


def _call_skuba(skuba_cluster_path, cmd_args, timeout=DEFAULT_TIMEOUT, **kwargs):
    log.debug("Calling Skuba CLI: 'skuba {}' - Timeout: {}".format(cmd_args, timeout))
    try:
        skuba_proc = salt.utils.timed_subprocess.TimedProc(
            ["skuba"] + cmd_args.split(),
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            timeout=timeout,
            cwd=skuba_cluster_path,
        )
        skuba_proc.run()
        return skuba_proc
    except Exception as exc:
        error_msg = "Unexpected error while calling skuba: {}".format(exc)
        log.error(error_msg)
        raise CommandExecutionError(error_msg)


def _sanitize_skuba_output_values(items):
    ret = []
    for i in items:
        if i.lower() == 'no':
            ret.append(False)
        elif i.lower() == 'yes':
            ret.append(True)
        else:
            ret.append(i)
    return ret


def list_nodes(skuba_cluster_path, timeout=DEFAULT_TIMEOUT, **kwargs):
    skuba_proc = _call_skuba(skuba_cluster_path, "cluster status")
    if skuba_proc.process.returncode != 0 or skuba_proc.stderr:
        error_msg = "Unexpected error {} at skuba when listing nodes: {}".format(
                skuba_proc.process.returncode,
                salt.utils.stringutils.to_str(skuba_proc.stderr))
        log.error(error_msg)
        raise CommandExecutionError(error_msg)

    skuba_proc_lines = salt.utils.stringutils.to_str(skuba_proc.stdout).splitlines()

    ret = {}
    try:
      headers = [x.strip().lower() for x in skuba_proc_lines[0].split('  ') if x]
      name_idx = headers.index('name')
      headers.remove('name')
      for line in skuba_proc_lines[1:]:
          items = [x.strip() for x in line.split('  ') if x]
          node_name = items.pop(name_idx)
          node_zip = zip(headers, _sanitize_skuba_output_values(items))
          ret[node_name] = dict(node_zip)
    except Exception as exc:
        error_msg = "Unexpected error while parsing skuba output: {}".format(exc)
        log.error(error_msg)
        raise CommandExecutionError(error_msg)

    return ret


def remove_node(skuba_cluster_path, node_name, timeout=DEFAULT_TIMEOUT, **kwargs):
    cmd_args = "node remove {}".format(node_name)
    skuba_proc = _call_skuba(skuba_cluster_path, cmd_args)
    if skuba_proc.process.returncode != 0:
        error_msg = "Unexpected error {} at skuba when removing a node: {}".format(
                skuba_proc.process.returncode,
                salt.utils.stringutils.to_str(skuba_proc.stderr))
        log.error(error_msg)

    ret = {
        'stdout': salt.utils.stringutils.to_str(skuba_proc.stdout),
        'stderr': salt.utils.stringutils.to_str(skuba_proc.stderr),
        'success': not skuba_proc.process.returncode,
        'retcode': skuba_proc.process.returncode,
    }
    return ret


def add_node(skuba_cluster_path, node_name, role, target, timeout=DEFAULT_TIMEOUT, **kwargs):
    cmd_args = "node join --role {} --target {} {}".format(role, target, node_name)
    skuba_proc = _call_skuba(skuba_cluster_path, cmd_args)
    if skuba_proc.process.returncode != 0:
        error_msg = "Unexpected error {} at skuba when adding a new node: {}".format(
                skuba_proc.process.returncode,
                salt.utils.stringutils.to_str(skuba_proc.stderr))
        log.error(error_msg)

    ret = {
        'stdout': salt.utils.stringutils.to_str(skuba_proc.stdout),
        'stderr': salt.utils.stringutils.to_str(skuba_proc.stderr),
        'success': not skuba_proc.process.returncode,
        'retcode': skuba_proc.process.returncode,
    }
    return ret


def upgrade_cluster(skuba_cluster_path, timeout=DEFAULT_TIMEOUT, **kwargs):
    skuba_proc = _call_skuba(skuba_cluster_path, "cluster upgrade plan")
    if skuba_proc.process.returncode != 0:
        error_msg = "Unexpected error {} at skuba when upgrading the cluster: {}".format(
                skuba_proc.process.returncode,
                salt.utils.stringutils.to_str(skuba_proc.stderr))
        log.error(error_msg)

    ret = {
        'stdout': salt.utils.stringutils.to_str(skuba_proc.stdout),
        'stderr': salt.utils.stringutils.to_str(skuba_proc.stderr),
        'success': not skuba_proc.process.returncode,
        'retcode': skuba_proc.process.returncode,
    }
    return ret
