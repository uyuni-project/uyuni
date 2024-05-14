"""Utility library for working with files and paths.

Extends uyuni.common.fileutils with server-only functions and can be used as a drop-in
replacement for uyuni.common.fileutils.

Part of spacewalk.common and available on Uyuni servers only.

"""

import os

from spacewalk.common.rhnConfig import cfg_component

# Re-export to allow users to only import spacewalk.common.fileutils
# pylint: disable-next=wildcard-import,unused-wildcard-import
from uyuni.common.fileutils import *


def chown_chmod_path(path, user=None, group="root", chmod=0o0750):
    """Set permissions and owner for path."""

    if user is None:
        with cfg_component(component=None) as cfg:
            user = cfg.get("httpd_user", "wwwrun")

    if not os.path.exists(path):
        raise FileNotFoundError(
            f"*** ERROR: Path doesn't exist (can't set permissions): {path}"
        )

    # If non-root, don't bother to change owners
    if os.getuid() != 0:
        return

    gc = GecosCache()
    uid = gc.getuid(user)
    if uid is None:
        raise OSError(
            f"*** ERROR: user '{user}' doesn't exist. Cannot set permissions properly."
        )

    gid = gc.getgid(group)
    if gid is None:
        raise OSError(
            f"*** ERROR: group '{group}' doesn't exist. Cannot set permissions properly."
        )

    uid_, gid_ = os.stat(path)[4:6]
    if uid_ != uid or gid_ != gid:
        os.chown(path, uid, gid)
    os.chmod(path, chmod)


def create_path(path, user=None, group=None, chmod=0o0755):
    """Advanced makedirs

    Will create the path if it does not exist and configure its permissions.
    Defaults for user/group to the apache user/group as specified in the configuration.
    """
    if not path:
        raise ValueError(f"ERROR: create_path(): Invalid path '{path}'.")

    with cfg_component(component=None) as cfg:
        if user is None:
            user = cfg.get("httpd_user", "wwwrun")
        if group is None:
            group = cfg.get("httpd_group", "www")

    path = cleanupAbsPath(path)
    if not os.path.exists(path):
        makedirs(path, mode=chmod, user=user, group=group)
    elif not os.path.isdir(path):
        raise ValueError(
            f"ERROR: create_path('{path}'): path doesn't lead to a directory"
        )
    else:
        os.chmod(path, chmod)
        uid, gid = getUidGid(user, group)
        try:
            os.chown(path, uid, gid)
        except OSError:
            # Changing permissions failed; ignore the error
            sys.stderr.write(f"Changing owner for {path} failed\n")


# keep old names for backwards compatibility
# pylint: disable=invalid-name
def setPathPerms(path, user=None, group="root", chmod=0o0750):
    """Deprecated, please use chown_chmod_path"""
    return chown_chmod_path(path, user=user, group=group, chmod=chmod)


def createPath(path, user=None, group=None, chmod=int("0755", 8)):
    """Deprecated, please use create_path"""
    return create_path(path, user=user, group=group, chmod=chmod)
