# -*- coding: utf-8 -*-
'''
SUSE Manager CaaSP Cluster Manager module for Salt

'''
from __future__ import absolute_import


import logging
import os
import subprocess

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
    This module requires that 'skuba' and 'kubectl' CLI tools are available.
    '''
    if not which('skuba'):
        return (False, 'skuba is not available in the minion')
    if not which('kubectl'):
        return (False, 'kubectl is not available in the minion')
    else:
        return __virtualname__


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


def _call_kubectl(kubectl_config_path,
                  cmd_args,
                  timeout=DEFAULT_TIMEOUT,
                  **kwargs):

    newenv = os.environ
    newenv['KUBECONFIG'] = os.path.join(kubectl_config_path, 'admin.conf')

    log.debug("Calling kubectl CLI: 'kubectl {}' - KUBECONFIG: {} - Timeout: {}".format(cmd_args, newenv['KUBECONFIG'], timeout))
    try:
        kubectl_proc = salt.utils.timed_subprocess.TimedProc(
            ["kubectl"] + cmd_args.split(),
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            timeout=timeout,
            cwd=kubectl_config_path,
            env=newenv,
        )
        kubectl_proc.run()
        return kubectl_proc
    except Exception as exc:
        error_msg = "Unexpected error while calling kubectl: {}".format(exc)
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
    # We need to query kubectl to retrieve the machine-id
    kubectl_proc = _call_kubectl(skuba_cluster_path, "get nodes -o json", timeout=timeout)
    if kubectl_proc.process.returncode != 0 or kubectl_proc.stderr:
        error_msg = "Unexpected error {} at kubectl when getting nodes: {}".format(
                kubectl_proc.process.returncode,
                salt.utils.stringutils.to_str(kubectl_proc.stderr))
        log.error(error_msg)
        raise CommandExecutionError(error_msg)

    kubectl_response = salt.utils.yaml.safe_load(kubectl_proc.stdout)

    for node in kubectl_response.get('items', []):
        node_name = node['metadata']['name']
        if node_name in ret.keys():
            ret[node_name]['machine-id'] = node['status']['nodeInfo']['machineID']
            ret[node_name]['internal-ips'] = list(map(lambda x: x['address'],
                                                  filter(lambda x: x['type'] == "InternalIP",
                                                         node['status']['addresses'])))
        else:
            error_msg = "Node returned from Kubernetes API not known to skuba: {}".format(node.metadata.name)
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


def _upgrade_cluster_plan(skuba_cluster_path,
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


def upgrade_cluster(skuba_cluster_path,
                    verbosity=None,
                    timeout=DEFAULT_TIMEOUT,
                    plan=False,
                    **kwargs):

    if plan:
        return _upgrade_cluster_plan(skuba_cluster_path=skuba_cluster_path,
                                     verbosity=verbosity,
                                     timeout=timeout,
                                     **kwargs)

    # Perform the cluster upgrade procedure.
    # 1. Upgrade addons
    # 2. Upgrade all nodes
    # 3. Upgrade addons
    ret = {
        'success' : True,
        'retcode' : 0,
        'stage0_upgrade_addons': {},
        'stage1_upgrade_nodes': {},
        'stage2_upgrade_addons': {},
    }

    ret['stage0_upgrade_addons'] = upgrade_addons(skuba_cluster_path=skuba_cluster_path,
                                                  verbosity=verbosity,
                                                  timeout=timeout,
                                                  plan=plan,
                                                  **kwargs)

    if not ret['stage0_upgrade_addons']['success']:
        ret['success'] = False
        return ret

    nodes = list_nodes(skuba_cluster_path=skuba_cluster_path,
                       timeout=timeout,
                       **kwargs)

    # Ensure master nodes are upgraded first
    for node, _ in sorted(nodes.items(), key=lambda x: 0 if x[1].get('role') == 'master' else 1):
        if not nodes[node]['internal-ips']:
            log.error('No internal-ips defined for node: {}. Cannot proceed upgrading this node!'.format(node))
            continue

        ret['stage1_upgrade_nodes'][node] = upgrade_node(skuba_cluster_path=skuba_cluster_path,
                                                         target=nodes[node]['internal-ips'][0],
                                                         verbosity=verbosity,
                                                         timeout=timeout,
                                                         plan=plan,
                                                         **kwargs)

        if not ret['stage1_upgrade_nodes'][node]['success']:
            ret['success'] = False

    ret['stage2_upgrade_addons'] = upgrade_addons(skuba_cluster_path=skuba_cluster_path,
                                                  verbosity=verbosity,
                                                  timeout=timeout,
                                                  plan=plan,
                                                  **kwargs)

    if not ret['stage2_upgrade_addons']['success']:
        ret['success'] = False

    if not ret['success']:
        ret['retcode'] = 1

    return ret


def upgrade_addons(skuba_cluster_path,
                   verbosity=None,
                   timeout=DEFAULT_TIMEOUT,
                   plan=False,
                   **kwargs):

    cmd_args = "addon upgrade {}".format("plan" if plan else "apply")

    if verbosity:
        cmd_args += " --verbosity {}".format(verbosity)

    skuba_proc = _call_skuba(skuba_cluster_path, cmd_args, timeout=timeout)
    if skuba_proc.process.returncode != 0:
        error_msg = "Unexpected error {} at skuba when upgrading addons: {}".format(
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
                 node_name=None,
                 target=None,
                 port=None,
                 sudo=None,
                 user=None,
                 verbosity=None,
                 timeout=DEFAULT_TIMEOUT,
                 plan=False,
                 **kwargs):

    if plan and not node_name:
        error_msg = "The 'node_name' argument is required if plan=True"
        log.error(error_msg)
        raise CommandExecutionError(error_msg)
    elif not plan and not target:
        error_msg = "The 'target' argument is required without plan=True"
        log.error(error_msg)
        raise CommandExecutionError(error_msg)

    if plan:
        cmd_args = "node upgrade plan {}".format(node_name)
    else:
        cmd_args = "node upgrade apply --target {}".format(target)

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
        error_msg = "Unexpected error {} at skuba when upgrading node: {}".format(
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


def cluster_init(cluster_name,
                 cluster_basedir,
                 target,
                 cloud_provider=None,
                 strict_capability_defaults=False,
                 verbosity=None,
                 timeout=DEFAULT_TIMEOUT,
                 **kwargs):

    cmd_args = "cluster init --control-plane {} {}".format(target, cluster_name)

    if cloud_provider:
        cmd_args += " --cloud-provider {}".format(cloud_provider)
    if strict_capability_defaults:
        cmd_args += " --strict-capability-defaults"
    if verbosity:
        cmd_args += " --verbosity {}".format(verbosity)

    skuba_proc = _call_skuba(cluster_basedir, cmd_args, timeout=timeout)
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


def _join_return_dicts(ret1, ret2):
    ret = merge_list(ret1, ret2)

    # Join multiple 'stdout' and 'stderr' outputs
    # after merging the two output dicts
    if isinstance(ret['stdout'], list):
        ret['stdout'] = ''.join(ret['stdout'])
    if isinstance(ret['stderr'], list):
        ret['stderr'] = ''.join(ret['stderr'])

    # We only need the latest 'success' and 'retcode'
    # values after merging the two output dicts.
    ret['success'] = ret['success'][1]
    ret['retcode'] = ret['retcode'][1]

    return ret

def create_cluster(cluster_name,
                   cluster_basedir,
                   first_node_name,
                   target,
                   cloud_provider=None,
                   strict_capability_defaults=False,
                   load_balancer=None,
                   verbosity=None,
                   timeout=DEFAULT_TIMEOUT,
                   **kwargs):

    ret = cluster_init(cluster_name=cluster_name,
                       cluster_basedir=cluster_basedir,
                       target=load_balancer if load_balancer else target,
                       cloud_provider=cloud_provider,
                       strict_capability_defaults=strict_capability_defaults,
                       verbosity=verbosity,
                       timeout=timeout,
                       **kwargs)

    if not ret['success']:
        return ret

    ret = _join_return_dicts(ret, master_bootstrap(node_name=first_node_name,
                                                   skuba_cluster_path=os.path.join(cluster_basedir, cluster_name),
                                                   target=target,
                                                   verbosity=verbosity,
                                                   timeout=timeout,
                                                   **kwargs))

    return ret
