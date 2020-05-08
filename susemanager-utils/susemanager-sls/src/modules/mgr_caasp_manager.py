# -*- coding: utf-8 -*-
'''
SUSE Manager CaaSP Cluster Manager module for Salt

'''
from __future__ import absolute_import


import logging
import os
import os.path
import subprocess
from urllib3.exceptions import HTTPError
from kubernetes.client.rest import ApiException
import kubernetes
import kubernetes.client

import salt.utils.stringutils
import salt.utils.timed_subprocess

try:
    from salt.utils.path import which
except ImportError:
    from salt.utils import which

from salt.utils.dictupdate import merge_list
from salt.exceptions import CommandExecutionError


log = logging.getLogger(__name__)

__virtualname__ = 'caasp'

DEFAULT_TIMEOUT = 1200


def __virtual__():
    '''
    This module is always enabled while 'skuba' CLI tools is available.
    '''
    return __virtualname__ if which('skuba') else (False, 'skuba is not available')


def _call_skuba(skuba_cluster_path,
                cmd_args,
                timeout=DEFAULT_TIMEOUT,
                **kwargs):

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
        elif i.lower() == '<none>':
            ret.append(None)
        else:
            ret.append(i)
    return ret


def list_nodes(skuba_cluster_path,
               timeout=DEFAULT_TIMEOUT,
               **kwargs):
    skuba_proc = _call_skuba(skuba_cluster_path, "cluster status", timeout=timeout)
    if skuba_proc.process.returncode != 0 or skuba_proc.stderr:
        error_msg = "Unexpected error {} at skuba when listing nodes: {}".format(
                skuba_proc.process.returncode,
                salt.utils.stringutils.to_str(skuba_proc.stderr))
        log.error(error_msg)
        raise CommandExecutionError(error_msg)

    skuba_proc_lines = salt.utils.stringutils.to_str(skuba_proc.stdout).splitlines()

    ret = {}
    try:
        # The first line of skuba output are the headers
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

    # The following is a hack to enrich skuba result with the machine-id of every node
    # We need to query k8s API to retrieve the machine-id
    kubernetes.config.load_kube_config(skuba_cluster_path + os.path.sep + 'admin.conf')
    try:
        kubeapi_response = kubernetes.client.CoreV1Api().list_node()
        for node in kubeapi_response.items:
            if node.metadata.name in ret.keys():
                ret[node.metadata.name]['machine-id'] = node.status.node_info.machine_id
            else:
                error_msg = "Node returned from Kubernetes API not known to skuba: {}".format(node.metadata.name)
                log.error(error_msg)
    except (ApiException, HTTPError) as exc:
        error_msg = "Exception while querying k8s API: {}".format(exc)
        log.error(error_msg)

    return ret


def remove_node(skuba_cluster_path,
                node_name,
                drain_timeout=None,
                verbosity=None,
                timeout=DEFAULT_TIMEOUT,
                **kwargs):

    cmd_args = "node remove {}".format(node_name)

    if drain_timeout:
        cmd_args += " --drain-timeout {}".format(drain_timeout)
    if verbosity:
        cmd_args += " --verbosity {}".format(verbosity)

    skuba_proc = _call_skuba(skuba_cluster_path, cmd_args, timeout=timeout)
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


def add_node(skuba_cluster_path,
             node_name,
             role,
             target,
             ignore_preflight_errors=None,
             port=None,
             sudo=None,
             user=None,
             verbosity=None,
             timeout=DEFAULT_TIMEOUT,
             **kwargs):

    cmd_args = "node join --role {} --target {} {}".format(role, target, node_name)

    if ignore_preflight_errors:
        cmd_args += " --ignore-preflight-errors {}".format(ignore_preflight_errors)
    if port:
        cmd_args += " --port {}".format(port)
    if sudo:
        cmd_args += " --sudo"
    if user:
        cmd_args += " --user {}".format(user)
    if verbosity:
        cmd_args += " --verbosity {}".format(verbosity)

    skuba_proc = _call_skuba(skuba_cluster_path, cmd_args, timeout=timeout)
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


def upgrade_cluster(skuba_cluster_path,
                    verbosity=None,
                    timeout=DEFAULT_TIMEOUT,
                    **kwargs):

    cmd_args = "cluster upgrade plan"

    if verbosity:
        cmd_args += " --verbosity {}".format(verbosity)

    skuba_proc = _call_skuba(skuba_cluster_path, cmd_args, timeout=timeout)
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


def upgrade_node(skuba_cluster_path,
                 verbosity=None,
                 timeout=DEFAULT_TIMEOUT,
                 plan=False,
                 **kwargs):

    cmd_args = "node upgrade {}".format("plan" if plan else "apply")

    if verbosity:
        cmd_args += " --verbosity {}".format(verbosity)

    skuba_proc = _call_skuba(skuba_cluster_path, cmd_args, timeout=timeout)
    if skuba_proc.process.returncode != 0:
        error_msg = "Unexpected error {} at skuba when upgrading the node: {}".format(
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


def cluster_init(name,
                 base_path,
                 target,
                 cloud_provider=None,
                 strict_capability_defaults=False,
                 verbosity=None,
                 timeout=DEFAULT_TIMEOUT,
                 **kwargs):

    cmd_args = "cluster init --control-plane {} {}".format(target, name)

    if cloud_provider:
        cmd_args += " --cloud-provider {}".format(cloud_provider)
    if strict_capability_defaults:
        cmd_args += " --strict-capability-defaults"
    if verbosity:
        cmd_args += " --verbosity {}".format(verbosity)

    skuba_proc = _call_skuba(base_path, cmd_args, timeout=timeout)
    if skuba_proc.process.returncode != 0:
        error_msg = "Unexpected error {} at skuba when initializing the cluster: {}".format(
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


def master_bootstrap(node_name,
                     skuba_cluster_path,
                     target,
                     ignore_preflight_errors=None,
                     port=None,
                     sudo=None,
                     user=None,
                     verbosity=None,
                     timeout=DEFAULT_TIMEOUT,
                     **kwargs):

    cmd_args = "node bootstrap --target {} {}".format(target, node_name)

    if ignore_preflight_errors:
        cmd_args += " --ignore-preflight-errors {}".format(ignore_preflight_errors)
    if port:
        cmd_args += " --port {}".format(port)
    if sudo:
        cmd_args += " --sudo"
    if user:
        cmd_args += " --user {}".format(user)
    if verbosity:
        cmd_args += " --verbosity {}".format(verbosity)

    skuba_proc = _call_skuba(skuba_cluster_path, cmd_args, timeout=timeout)
    if skuba_proc.process.returncode != 0:
        error_msg = "Unexpected error {} at skuba when bootstrapping the node: {}".format(
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


def create_cluster(name,
                   base_path,
                   first_node_name,
                   target,
                   cloud_provider=None,
                   strict_capability_defaults=False,
                   load_balancer=None,
                   verbosity=None,
                   timeout=DEFAULT_TIMEOUT,
                   **kwargs):

    ret = cluster_init(name=name,
                       base_path=base_path,
                       target=load_balancer if load_balancer else target,
                       cloud_provider=cloud_provider,
                       strict_capability_defaults=strict_capability_defaults,
                       verbosity=verbosity,
                       timeout=timeout,
                       **kwargs)

    if not ret['success']:
        return ret

    ret = merge_list(ret, master_bootstrap(node_name=first_node_name,
                                           skuba_cluster_path=os.path.join(base_path, name),
                                           target=target,
                                           verbosity=verbosity,
                                           timeout=timeout,
                                           **kwargs))

    # Join multiple 'stdout' and 'stderr' outputs
    # after mergint the two output dicts
    if isinstance(ret['stdout'], list):
        ret['stdout'] = ''.join(ret['stdout'])
    if isinstance(ret['stderr'], list):
        ret['stderr'] = ''.join(ret['stderr'])

    # We only need the latest 'success' and 'retcode'
    # values after merging the two output dicts.
    ret['success'] = ret['success'][1]
    ret['retcode'] = ret['retcode'][1]

    return ret
