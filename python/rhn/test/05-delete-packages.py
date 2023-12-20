#!/usr/bin/python  #  pylint: disable=missing-module-docstring,invalid-name
#
# tests uploads over SSL
#
#
# USAGE:  $0 SERVER SYSTEMID
# OUTPUT: return code = 0


import sys

sys.path.append("..")
from rhn.rpclib import Server  #  pylint: disable=wrong-import-position

SERVER = "xmlrpc.rhn.redhat.com"
HANDLER = "/XMLRPC"
system_id_file = "/etc/sysconfig/rhn/systemid"
try:
    SERVER = sys.argv[1]
    system_id_file = sys.argv[2]
except:  #  pylint: disable=bare-except
    pass


def get_test_server_https():
    global SERVER, HANDLER  #  pylint: disable=global-variable-not-assigned,global-variable-not-assigned
    return Server("https://%s%s" % (SERVER, HANDLER))  #  pylint: disable=consider-using-f-string


if __name__ == "__main__":
    systemid = open(system_id_file).read()  #  pylint: disable=unspecified-encoding

    s = get_test_server_https()

    # Generate a huge list of packages to "delete"
    packages = []
    for i in range(3000):
        packages.append(["package-%d" % i, "1.1", "1", ""])  #  pylint: disable=consider-using-f-string

    result = s.registration.delete_packages(systemid, packages[:1000])
    sys.exit(result)
