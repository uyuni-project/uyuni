# Retrieve action method name given queued action information. pylint: disable=missing-module-docstring,invalid-name
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
from distutils.sysconfig import get_python_lib  #  pylint: disable=deprecated-module


class GetMethodException(Exception):

    """Exception class"""

    pass


def sanity(methodNameComps):  #  pylint: disable=invalid-name
    """Verifies if all the components have proper names."""
    # Allowed characters in each string
    alpha = string.ascii_lowercase + string.ascii_uppercase
    allowedChars = alpha + string.digits + "_"  #  pylint: disable=invalid-name
    for comp in methodNameComps:
        if not len(comp):  #  pylint: disable=use-implicit-booleaness-not-len
            raise GetMethodException("Empty method component")
        for c in comp:
            if c not in allowedChars:
                raise GetMethodException(
                    "Invalid character '%s' in the method name" % c  #  pylint: disable=consider-using-f-string
                )
        # Can only begin with a letter
        if comp[0] not in alpha:
            raise GetMethodException(
                "Method names should start with an alphabetic character"
            )


def getMethod(methodName, baseClass):  #  pylint: disable=invalid-name,invalid-name,invalid-name
    """Retreive method given methodName, path to base of tree, and class/module
    route/label.
    """
    # First split the method name
    methodNameComps = ["spacewalk"] + baseClass.split(".") + methodName.split(".")  #  pylint: disable=invalid-name
    # Sanity checks
    sanity(methodNameComps)
    # Build the path to the file
    path = get_python_lib()
    for index in range(len(methodNameComps)):
        comp = methodNameComps[index]
        path = "%s/%s" % (path, comp)  #  pylint: disable=consider-using-f-string
        # If this is a directory, fine...
        if os.path.isdir(path):
            # Okay, go on
            continue
        # Try to load this as a file
        for extension in ["py", "pyc", "pyo"]:
            if os.path.isfile("%s.%s" % (path, extension)):  #  pylint: disable=consider-using-f-string
                # Yes, this is a file
                break
        else:
            # No dir and no file. Die
            raise GetMethodException("Action %s could not be found" % methodName)  #  pylint: disable=consider-using-f-string
        break
    else:
        # Only directories. This can't happen
        raise GetMethodException("Very wrong")

    # The position of the file
    fIndex = index + 1  #  pylint: disable=invalid-name
    # Now build the module name
    modulename = ".".join(methodNameComps[:fIndex])
    # And try to import it
    try:
        actions = __import__(modulename)
    except ImportError:
        raise_with_tb(
            GetMethodException("Could not import module %s" % modulename),  #  pylint: disable=consider-using-f-string
            sys.exc_info()[2],
        )

    className = actions  #  pylint: disable=invalid-name
    # Iterate through the list of components and try to load that specific
    # module/method
    for index in range(1, len(methodNameComps)):
        comp = methodNameComps[index]
        if index < fIndex:
            # This is a directory or a file we have to load
            if not hasattr(className, comp):
                # Hmmm... Not there
                raise GetMethodException(
                    "Class %s has no attribute %s"  #  pylint: disable=consider-using-f-string
                    % (".".join(methodNameComps[:index]), comp)
                )
            className = getattr(className, comp)  #  pylint: disable=invalid-name
            # print type(className)
            continue
        # A file or method
        # We look for the special __rhnexport__ array
        if not hasattr(className, "__rhnexport__"):
            raise GetMethodException(
                "Class %s is not valid" % ".".join(methodNameComps[:index])  #  pylint: disable=consider-using-f-string
            )
        export = getattr(className, "__rhnexport__")
        if comp not in export:
            raise GetMethodException(
                "Class %s does not export '%s'"  #  pylint: disable=consider-using-f-string
                % (".".join(methodNameComps[:index]), comp)
            )
        className = getattr(className, comp)  #  pylint: disable=invalid-name
        if type(className) is ClassType:  #  pylint: disable=unidiomatic-typecheck
            # Try to instantiate it
            className = className()  #  pylint: disable=invalid-name
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
        print(("----Running method %s: " % m))  #  pylint: disable=consider-using-f-string
        try:
            method = getMethod(m, "Actions")
        except GetMethodException:
            e = sys.exc_info()[1]
            print(("Error getting the method %s: %s" % (m, e.args)))  #  pylint: disable=consider-using-f-string
        else:
            method()
# -----------------------------------------------------------------------------
