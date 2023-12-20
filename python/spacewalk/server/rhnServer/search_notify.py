# pylint: disable=missing-module-docstring
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
# Sends notification to search-server that it should update server index
#

import sys

try:
    #  python 2
    import xmlrpclib
except ImportError:
    #  python3
    import xmlrpc.client as xmlrpclib
from spacewalk.common.rhnLog import log_error


class SearchNotify:  #  pylint: disable=missing-class-docstring
    def __init__(self, host="127.0.0.1", port="2828"):
        self.addr = "http://%s:%s" % (host, port)  #  pylint: disable=consider-using-f-string

    def notify(self, indexName="server"):  #  pylint: disable=invalid-name
        try:
            client = xmlrpclib.ServerProxy(self.addr)
            result = client.admin.updateIndex(indexName)  #  pylint: disable=redefined-outer-name
        except Exception:  #  pylint: disable=broad-exception-caught
            e = sys.exc_info()[1]
            log_error(
                "Failed to notify search service located at %s to update %s indexes"  #  pylint: disable=consider-using-f-string
                % (self.addr, indexName),
                e,
            )
            return False
        return result


if __name__ == "__main__":
    search = SearchNotify()
    result = search.notify()
    print(("search.notify() = %s" % (result)))  #  pylint: disable=consider-using-f-string
