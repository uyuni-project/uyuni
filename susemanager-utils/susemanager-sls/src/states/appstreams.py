'''
Salt Custom State for managing AppStreams configuration

This custom state provides functionality for managing AppStreams modules,
enabling or disabling them as needed.

The 'enabled' and 'disabled' states ensure that specified AppStreams modules
are respectively enabled or disabled.
'''

def enabled(name, appstreams):
    '''
    Ensure that the appstreams are enabled.

    :param str name
        The name of the state

    :param list appstreams:
        A list of appstreams to enable in the format module_name:stream

    '''
    if __opts__["test"]:
        return _test_mode(name, appstreams, "enable")

    if isinstance(appstreams, list) and len(appstreams) == 0:
        return {
            "name": name,
            "changes": {},
            "result": True,
            "comment": "No AppStreams to enable provided",
        }

    result, comment, changes = __salt__['appstreams.enable'](appstreams)
    return {
        "name": name,
        "changes": changes,
        "result": result,
        "comment": comment
    }

def disabled(name, appstreams):
    '''
    Ensure that the appstreams are disabled.

    :param str name
        The name of the state

    :param list appstreams:
        A list of appstreams to disable

    '''
    if __opts__["test"]:
        return _test_mode(name, appstreams, "disable")

    if isinstance(appstreams, list) and len(appstreams) == 0:
        return {
            "name": name,
            "changes": {},
            "result": True,
            "comment": "No AppStreams to disable provided",
        }

    result, comment, changes = __salt__['appstreams.disable'](appstreams)
    return {
        "name": name,
        "changes": changes,
        "result": result,
        "comment": comment
    }

def _test_mode(name, appstreams, action):
    action_name = f"{action}d"
    comment = f"The following appstreams would be {action_name}: {appstreams}"
    changes = {}
    if appstreams:
        changes = { "ret": {} }
        changes["ret"][action_name] = appstreams
    else:
        comment = f"No AppStreams to {action} provided"
    return {
        "name": name,
        "changes": changes,
        "result": None,
        "comment": comment,
    }
