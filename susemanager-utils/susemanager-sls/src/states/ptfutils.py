"""
PTF Utility states
"""

from salt.exceptions import CommandExecutionError
from salt.states.pkg import _find_remove_targets

__virtualname__ = 'ptfutils'


def __virtual__():
    '''
    This module is always enabled when 'pkg.removeptf' is available.
    '''
    if 'pkg.removeptf' in __salt__:
        return __virtualname__
    return (False, "Removing PTFs not supported by execution module")


def removed(name, pkgs=None, **kwargs):
    '''
    Verify that a ptf package is not installed, calling ``pkg.removeptf`` if necessary
    to remove the packages.
    It will install the latest normal package versions available in the repositories

    name
        The name of the package to be removed.

    Multiple Package Options:

    pkgs
        A list of ptf packages to remove. Must be passed as a python list. The
        ``name`` parameter will be ignored if this option is passed.

    '''
    action = "remove"
    try:
        pkg_params = __salt__["pkg_resource.parse_targets"](
            name, pkgs, normalize=True
        )[0]
    except MinionError as exc:
        return {
            "name": name,
            "changes": {},
            "result": False,
            "comment": "An error was encountered while parsing targets: {}".format(exc),
        }
    targets = _find_remove_targets(
        name, version, pkgs, normalize, ignore_epoch=ignore_epoch, **kwargs
    )
    if isinstance(targets, dict) and "result" in targets:
        return targets
    elif not isinstance(targets, list):
        return {
            "name": name,
            "changes": {},
            "result": False,
            "comment": "An error was encountered while checking targets: {}".format(
                targets
            ),
        }
    targets.sort()

    if not targets:
        return {
            "name": name,
            "changes": {},
            "result": True,
            "comment": "None of the targeted packages are installed",
        }

    if __opts__["test"]:

        _changes = {}
        _changes.update({x: {"new": "{}d".format(action), "old": ""} for x in targets})

        return {
            "name": name,
            "changes": _changes,
            "result": None,
            "comment": "The following ptf packages will be {}d: {}.".format(
                action, ", ".join(targets)
            ),
        }

    changes = __salt__["pkg.removeptf"](name, pkgs=pkgs, **kwargs)
    new = __salt__["pkg.list_pkgs"](versions_as_list=True, **kwargs)
    failed = []
    for param in pkg_params:
        if param in new:
            failed.append(param)

    failed.sort()

    if failed:
        return {
            "name": name,
            "changes": changes,
            "result": False,
            "comment": "The following packages failed to {}: {}.".format(
                action, ", ".join(failed)
            ),
        }

    comments = []
    not_installed = sorted([x for x in pkg_params if x not in targets])
    if not_installed:
        comments.append(
            "The following packages were not installed: {}".format(
                ", ".join(not_installed)
            )
        )
        comments.append(
            "The following packages were {}d: {}.".format(action, ", ".join(targets))
        )
    else:
        comments.append("All targeted packages were {}d.".format(action))

    return {
        "name": name,
        "changes": changes,
        "result": True,
        "comment": " ".join(comments),
    }

