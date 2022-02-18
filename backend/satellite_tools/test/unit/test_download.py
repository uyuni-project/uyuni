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

from spacewalk.satellite_tools.download import ThreadedDownloader, pycurl


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
