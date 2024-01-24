#  pylint: disable=missing-module-docstring,invalid-name
# Retrieve action method name given queued action information.
#
# Client code for Update Agent
#
# Copyright (c) 2008--2016 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
#
# An allowable xmlrpc method is retrieved given a base location, a
# hierarchical route to the class/module, and method name.
#

import string
import os
import sys
from uyuni.common.usix import ClassType, raise_with_tb

# pylint: disable-next=deprecated-module
from distutils.sysconfig import get_python_lib


class GetMethodException(Exception):

    """Exception class"""

    pass


def sanity(methodNameComps):
    """Verifies if all the components have proper names."""
    # Allowed characters in each string
    alpha = string.ascii_lowercase + string.ascii_uppercase
    allowedChars = alpha + string.digits + "_"
    for comp in methodNameComps:
        # pylint: disable-next=use-implicit-booleaness-not-len
        if not len(comp):
            raise GetMethodException("Empty method component")
        for c in comp:
            if c not in allowedChars:
                raise GetMethodException(
                    # pylint: disable-next=consider-using-f-string
                    "Invalid character '%s' in the method name"
                    % c
                )
        # Can only begin with a letter
        if comp[0] not in alpha:
            raise GetMethodException(
                "Method names should start with an alphabetic character"
            )


def getMethod(methodName, baseClass):
    """Retreive method given methodName, path to base of tree, and class/module
    route/label.
    """
    # First split the method name
    methodNameComps = ["spacewalk"] + baseClass.split(".") + methodName.split(".")
    # Sanity checks
    sanity(methodNameComps)
    # Build the path to the file
    path = get_python_lib()
    for index in range(len(methodNameComps)):
        comp = methodNameComps[index]
        # pylint: disable-next=consider-using-f-string
        path = "%s/%s" % (path, comp)
        # If this is a directory, fine...
        if os.path.isdir(path):
            # Okay, go on
            continue
        # Try to load this as a file
        for extension in ["py", "pyc", "pyo"]:
            # pylint: disable-next=consider-using-f-string
            if os.path.isfile("%s.%s" % (path, extension)):
                # Yes, this is a file
                break
        else:
            # No dir and no file. Die
            # pylint: disable-next=consider-using-f-string
            raise GetMethodException("Action %s could not be found" % methodName)
        break
    else:
        # Only directories. This can't happen
        raise GetMethodException("Very wrong")

    # The position of the file
    fIndex = index + 1
    # Now build the module name
    modulename = ".".join(methodNameComps[:fIndex])
    # And try to import it
    try:
        actions = __import__(modulename)
    except ImportError:
        raise_with_tb(
            # pylint: disable-next=consider-using-f-string
            GetMethodException("Could not import module %s" % modulename),
            sys.exc_info()[2],
        )

    className = actions
    # Iterate through the list of components and try to load that specific
    # module/method
    for index in range(1, len(methodNameComps)):
        comp = methodNameComps[index]
        if index < fIndex:
            # This is a directory or a file we have to load
            if not hasattr(className, comp):
                # Hmmm... Not there
                raise GetMethodException(
                    # pylint: disable-next=consider-using-f-string
                    "Class %s has no attribute %s"
                    % (".".join(methodNameComps[:index]), comp)
                )
            className = getattr(className, comp)
            # print type(className)
            continue
        # A file or method
        # We look for the special __rhnexport__ array
        if not hasattr(className, "__rhnexport__"):
            raise GetMethodException(
                # pylint: disable-next=consider-using-f-string
                "Class %s is not valid"
                % ".".join(methodNameComps[:index])
            )
        export = getattr(className, "__rhnexport__")
        if comp not in export:
            raise GetMethodException(
                # pylint: disable-next=consider-using-f-string
                "Class %s does not export '%s'"
                % (".".join(methodNameComps[:index]), comp)
            )
        className = getattr(className, comp)
        # pylint: disable-next=unidiomatic-typecheck
        if type(className) is ClassType:
            # Try to instantiate it
            className = className()
        # print type(className)

    return className


# -----------------------------------------------------------------------------
if __name__ == "__main__":
    # Two valid ones and a bogus one
    methods = [
        "a.b.c.d.e.f",
        "a.b.c.d.e.foo.h",
        "a.b.c.d.e.g.h",
        "a.b.d.d.e.g.h",
        "a.b.d.d._e.g.h",
        "a.b.d.d.e_.g.h",
        "a.b.d.d.e-.g.h",
        "a.b.d.d..g.h",
    ]

    for m in methods:
        # pylint: disable-next=consider-using-f-string
        print(("----Running method %s: " % m))
        try:
            method = getMethod(m, "Actions")
        except GetMethodException:
            e = sys.exc_info()[1]
            # pylint: disable-next=consider-using-f-string
            print(("Error getting the method %s: %s" % (m, e.args)))
        else:
            method()
# -----------------------------------------------------------------------------
