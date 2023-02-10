#!/usr/bin/python3
#
# Copyright (c) 2021 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

from mock import Mock, patch
from queue import Queue

from spacewalk.satellite_tools.download import (
    DownloadThread,
    ThreadedDownloader,
    PyCurlFileObjectThread,
    pycurl,
)
from urlgrabber.grabber import URLGrabberOptions

class NoKeyErrorsDict(dict):
    """Like a dict that is only accessed by .get(key)"""

    def __getitem__(self, key):
        return super().get(key)


# this initCFG also operates on spacewalk.common.rhnConfig.CFG
@patch("uyuni.common.context_managers.initCFG", Mock())
@patch("spacewalk.satellite_tools.download.log", Mock())  # no logging
@patch("urlgrabber.grabber.PyCurlFileObject._do_grab", Mock())  # no downloads
@patch("urlgrabber.grabber.PyCurlFileObject.close", Mock())  # no need to close files
@patch("spacewalk.satellite_tools.download.os.path.isfile", Mock(return_value=False))
def test_reposync_timeout_minrate_are_passed_to_curl():
    # only provide needed params with dummy data, the rest is "None"
    params = NoKeyErrorsDict({"http_headers": dict(), "urls": ["http://example.com"]})

    CFG = Mock()
    CFG.REPOSYNC_TIMEOUT = 42
    CFG.REPOSYNC_MINRATE = 42
    CFG.REPOSYNC_DOWNLOAD_THREADS = 42 # Throws ValueError if not defined

    curl_spy = Mock()

    with patch(
        "spacewalk.satellite_tools.download.pycurl.Curl", Mock(return_value=curl_spy)
    ), patch("uyuni.common.context_managers.CFG", CFG):

        td = ThreadedDownloader(force=True)
        td.add(params)
        td.run()

        curl_spy.setopt.assert_any_call(pycurl.LOW_SPEED_LIMIT, 42)
        curl_spy.setopt.assert_any_call(pycurl.LOW_SPEED_TIME, 42)


@patch("uyuni.common.context_managers.initCFG", Mock())
@patch("spacewalk.satellite_tools.download.log", Mock())  # no logging
@patch("urlgrabber.grabber.PyCurlFileObject._do_grab", Mock())  # no downloads
@patch("urlgrabber.grabber.PyCurlFileObject.close", Mock())  # no need to close files
@patch("spacewalk.satellite_tools.download.os.path.isfile", Mock(return_value=False))
def test_reposync_configured_http_proxy_passed_to_urlgrabber():
    http_proxy = 'http://proxy.example.com:8080'
    opts = URLGrabberOptions(proxy=None, proxies={'http': http_proxy, 'https': http_proxy, 'ftp': http_proxy})
    url = "https://download.opensuse.org"

    CFG = Mock()
    CFG.http_proxy = http_proxy
    CFG.REPOSYNC_TIMEOUT = 42
    CFG.REPOSYNC_MINRATE = 42
    CFG.REPOSYNC_DOWNLOAD_THREADS = 42 # Throws ValueError if not defined

    curl_spy = Mock()

    with patch(
        "spacewalk.satellite_tools.download.pycurl.Curl", Mock(return_value=curl_spy)
    ), patch("uyuni.common.context_managers.CFG", CFG):

        pycurlobj = PyCurlFileObjectThread(url, "file.rpm", opts, curl_spy, None)
        assert pycurlobj.opts.proxy == http_proxy


def test_reposync_download_thread_fetch_url_proxy_pass():
    http_proxy = "http://proxy.example.com:8080"
    proxies = {"http": http_proxy, "https": http_proxy, "ftp": http_proxy}
    queue = Queue()
    queue.put(
        {
            "ssl_ca_cert": None,
            "ssl_client_cert": None,
            "ssl_client_key": None,
            "bytes_range": None,
            "proxy": http_proxy,
            "proxy_username": "user",
            "proxy_password": "password",
            "proxies": proxies,
            "http_headers": {},
            "timeout": None,
            "minrate": None,
            "urls": [],
            "relative_path": "",
            "urlgrabber_logspec": None,
        }
    )
    parent_mock = Mock()
    parent_mock.retries = 0
    url_grabber_opts_mock = Mock()
    url_grabber_opts_mock.return_value.retrycodes = [-1]
    with patch(
        "spacewalk.satellite_tools.download.URLGrabberOptions", url_grabber_opts_mock
    ):
        DownloadThread(parent_mock, queue).run()
        assert url_grabber_opts_mock.mock_calls[1].kwargs["proxies"] == proxies
        assert url_grabber_opts_mock.mock_calls[1].kwargs["retrycodes"] == [-1, 14]
        assert "proxy" not in url_grabber_opts_mock.mock_calls[1].kwargs
        assert "username" not in url_grabber_opts_mock.mock_calls[1].kwargs
        assert "password" not in url_grabber_opts_mock.mock_calls[1].kwargs
