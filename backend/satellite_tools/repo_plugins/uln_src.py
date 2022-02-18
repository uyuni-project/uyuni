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

# pylint: disable=E0012, C0413
import sys
sys.path.append('/usr/share/rhn')

from spacewalk.satellite_tools.repo_plugins.yum_src import ContentSource as yum_ContentSource
from spacewalk.satellite_tools.syncLib import RhnSyncException
from spacewalk.satellite_tools.ulnauth import ULNAuth

class ContentSource(yum_ContentSource):

    def __init__(self, url, name, insecure=False, interactive=True, yumsrc_conf=ULNAuth.ULN_CONF_PATH,
                 org=1, channel_label="", no_mirrors=False,
                 ca_cert_file=None, client_cert_file=None, client_key_file=None, channel_arch="",
                 http_headers=None):
        if url[:6] != "uln://":
            raise RhnSyncException("url format error, url must start with uln://")
        # Make sure baseurl ends with / and urljoin will work correctly
        if url[-1] != '/':
            url += '/'
        yum_ContentSource.__init__(self, url=url, name=name, insecure=insecure,
                                   interactive=interactive, yumsrc_conf=yumsrc_conf, org=org,
                                   channel_label=channel_label, no_mirrors=no_mirrors,
                                   ca_cert_file=ca_cert_file, client_cert_file=client_cert_file,
                                   client_key_file=client_key_file)
        self.uln_token = None

    def _authenticate(self, url):
        self._url_orig = url
        self._uln_auth = ULNAuth()
        self.uln_token = self._uln_auth.authenticate(url)
        self.http_headers = {'X-ULN-Api-User-Key': self.uln_token}
        hostname, label = self._uln_auth.get_hostname(self.url)
        self._load_proxy_settings(hostname)
        print(("The download URL is: " + self._uln_auth.url + "/XMLRPC/GET-REQ/" + label))
        if self.proxy_url:
            print(("Trying proxy " + self.proxy_url))

    # pylint: disable=arguments-differ
    def setup_repo(self, repo, *args, **kwargs):
        yum_ContentSource.setup_repo(self, repo, *args, uln_repo=True, **kwargs)

    # Get download parameters for threaded downloader
    def set_download_parameters(self, params, relative_path, target_file, checksum_type=None,
                                checksum_value=None, bytes_range=None):
        yum_ContentSource.set_download_parameters(self,
                                                  params=params,
                                                  relative_path=relative_path,
                                                  target_file=target_file,
                                                  checksum_type=checksum_type,
                                                  checksum_value=checksum_value,
                                                  bytes_range=bytes_range)
        hostname, label = self._uln_auth.get_hostname(self.url)
        params['urls'] = [self._uln_auth.url + "/XMLRPC/GET-REQ/" + label]
