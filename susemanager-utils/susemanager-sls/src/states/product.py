"""
Handles installation of SUSE products using zypper

Only supported with :mod:`zypper <salt.modules.zypper>`
"""

import logging

from salt.utils.versions import version_cmp
from salt.exceptions import CommandExecutionError

log = logging.getLogger(__name__)

__virtualname__ = "product"


# pylint: disable-next=invalid-name
def __virtual__():
    """
    Only work on SUSE platforms with zypper
    """
    # pylint: disable-next=undefined-variable
    if __grains__.get("os_family", "") != "Suse":
        return (False, "Module product: non SUSE OS not supported")

    # Not all versions of SUSE use zypper, check that it is available
    try:
        # pylint: disable-next=undefined-variable
        zypp_info = __salt__["pkg.info_installed"]("zypper")["zypper"]
    except CommandExecutionError:
        return (False, "Module product: zypper package manager not found")

    # Minimum version that supports 'zypper search --provides'
    if version_cmp(zypp_info["version"], "1.8.13") < 0:
        return (False, "Module product: zypper 1.8.13 or greater required")
    return __virtualname__


def _get_missing_products(refresh):
    # Search for not installed products
    products = []
    try:
        products = list(
            # pylint: disable-next=undefined-variable
            __salt__["pkg.search"](
                "product()",
                refresh=refresh,
                match="exact",
                provides=True,
                not_installed_only=True,
            )
        )

        log.debug(
            "The following products are not yet installed: %s", ", ".join(products)
        )

    except CommandExecutionError:
        # No search results
        return None

    # Exclude products that are already provided by another to prevent conflicts
    to_install = []
    for pkg in products:
        try:
            # pylint: disable-next=undefined-variable
            res = list(__salt__["pkg.search"](pkg, match="exact", provides=True))

            if pkg in res:
                res.remove(pkg)
            if not res:
                # No other providers than the package itself
                to_install.append(pkg)
            else:
                log.debug(
                    "The product '%s' is already provided by '%s'. Skipping.",
                    pkg,
                    ", ".join(res),
                )

        except CommandExecutionError:
            # No search results
            # Not provided by any installed package, add it to the list
            to_install.append(pkg)

    return to_install


# pylint: disable-next=unused-argument
def all_installed(name, refresh=False, **kwargs):
    """
    Ensure that all the subscribed products are installed.

    refresh
        force a refresh if set to True.
        If set to False (default) it depends on zypper if a refresh is
        executed.
    """

    ret = {"name": name, "changes": {}, "result": True, "comment": ""}

    to_install = _get_missing_products(refresh)

    if not to_install:
        # All product packages are already installed
        ret["comment"] = "All subscribed products are already installed"
        ret["result"] = True

        log.debug("All products are already installed. Nothing to do.")
        return ret

    # pylint: disable-next=undefined-variable
    return __states__["pkg.installed"](name, pkgs=to_install, no_recommends=True)
