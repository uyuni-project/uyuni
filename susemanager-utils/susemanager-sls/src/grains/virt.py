import salt.modules.virt


def __virtual__():
    return salt.modules.virt.__virtual__()


def features():
    """returns the features map of the virt module"""
    return {
        "virt_features": {
            "enhanced_network": "network_update" in salt.modules.virt.__dict__,
        },
    }
