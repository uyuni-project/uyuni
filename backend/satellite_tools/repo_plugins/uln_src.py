"""
Copyright (C) 2014 Oracle and/or its affiliates. All rights reserved.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2


This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301, USA.

ULN plugin for spacewalk-repo-sync.
"""
import sys
sys.path.append('/usr/share/rhn/up2date_client')
from rpcServer import RetryServer

from spacewalk.satellite_tools.repo_plugins.yum_src import ContentSource as yum_ContentSource

ULNSRC_CONF = '/etc/rhn/spacewalk-repo-sync/uln.conf'
DEFAULT_UP2DATE_URL = "linux-update.oracle.com"


class ContentSource(yum_ContentSource):
    def __init__(self, url, name):
        if url[:6] != "uln://":
            print "url format error, url must start with uln://"
            return -1
        yum_ContentSource.__init__(self, url, name, False, False, True, ULNSRC_CONF)

    def _authenticate(self, url):
        if url.startswith("uln:///"):
            self.uln_url = "https://" + DEFAULT_UP2DATE_URL
            label = url[7:]
        elif url.startswith("uln://"):
            parts = url[6:].split("/")
            self.uln_url = "https://" + parts[0]
            label = parts[1]
        else:
            print "url format error, url must start with uln://"
            return -1
        self.uln_user = self.yumbase.conf.username
        self.uln_pass = self.yumbase.conf.password
        self.url = self.uln_url + "/XMLRPC/GET-REQ/" + label
        print "The download URL is: " + self.url
        if self.proxy_addr:
            print "Trying proxy " + self.proxy_addr
        s = RetryServer(self.uln_url+"/rpc/api",
                    refreshCallback = None,
                    proxy = self.proxy_addr,
                    username = self.proxy_user,
                    password = self.proxy_pass,
                    timeout = 5)
        self.key = s.auth.login(self.uln_user, self.uln_pass)

    def setup_repo(self, repo):
        repo.http_headers = {'X-ULN-Api-User-Key': self.key}
        yum_ContentSource.setup_repo(self, repo)
