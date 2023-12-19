"""Collection of context managers for Uyuni."""
from contextlib import contextmanager
from spacewalk.common.rhnConfig import CFG, initCFG


@contextmanager
def cfg_component(component, root=None, filename=None):
    """Context manager for rhnConfig.

    :param comp: The configuration component to use in this context
    :param root: Root directory location of configuration files, optional
    :param filename: Configuration file, optional

    There is a common pattern when using rhnConfig that consists of the following steps:
    1. save current component: old = CFG.getComponent()
    2. set CFG to another component: initCFG('my_component')
    3. Read / Set configuration values
    4. set CFG back to the previous component

    This pattern can now be expressed using the ``with`` statement:

    with cfg_component('my_component') as CFG:
        print(CFG.my_value)
    """
    previous = CFG.getComponent()
    initCFG(component=component, root=root, filename=filename)
    try:
        yield CFG
    finally:
        initCFG(previous)
