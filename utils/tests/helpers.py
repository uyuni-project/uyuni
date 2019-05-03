# coding: utf-8
"""
Helpers for the test suite.
"""
import os


def symlink_source(script_name: str, mod_name: str, path: str = None) -> None:
    """
    Create a symlink for Python source that contains no .py extension.

    :param script_name: script name
    :param mod_name: name of the module
    :param path: path to that script.
    :return: None
    """
    if path is None:
        path = os.path.abspath(os.path.join(os.path.dirname(__file__), os.pardir))

    s_link_path = os.path.join(path, "tests", "{}.py".format(mod_name))
    if not os.path.exists(s_link_path):
        os.symlink(os.path.join(path, script_name), s_link_path)


def unsymlink_source(mod_name: str, path: str = None) -> None:
    """
    Remove symlink for Python source that contains no .py extension.

    :param mod_name: name of the symlink without an extension
    :param path:
    :return:
    """
    if path is None:
        path = os.path.abspath(os.path.join(os.path.dirname(__file__), os.pardir))
    mod_path = os.path.join(path, "tests", "{}.py".format(mod_name))

    if os.path.exists(mod_path):
        os.unlink(mod_path)
