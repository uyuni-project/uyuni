#
# Copyright (c) 2013 Novell, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
import sys
from wsgi import wsgiRequest

from rhn.connections import idn_ascii_to_pune

from spacewalk.common.rhnConfig import CFG, initCFG
from spacewalk.common.rhnLog import log_debug, log_error, initLOG
from spacewalk.common import apache

def check_password(environ, user, password):
    initCFG('server.susemanager')

    if user != CFG.mirrcred_user or password != CFG.mirrcred_pass:
        return False
    return True

def handle(environ, start_response):
    initCFG('server.susemanager')
    initLOG('/var/log/rhn/rhn_server_sat.log', CFG.DEBUG)

    req = wsgiRequest.WsgiRequest(environ, start_response)

    if CFG.ALLOWED_ISS_SLAVES:
        if not isinstance(CFG.ALLOWED_ISS_SLAVES, list):
            allowed_iss_slaves = [CFG.ALLOWED_ISS_SLAVES]
        else:
            allowed_iss_slaves = CFG.ALLOWED_ISS_SLAVES
        allowed_iss_slaves = [idn_ascii_to_pune(x) for x in allowed_iss_slaves]
    else:
        allowed_iss_slaves = []

    if req.get_remote_host() not in allowed_iss_slaves:
        log_error('ISS Slave [%s] not alowed' % req.server.server_hostname)
        req.send_http_header(status=apache.HTTP_FORBIDDEN)
        return req.output

    # dummy only. We use this only as an authentication test
    req.write('<repoindex/>')
    req.send_http_header()
    return req.output

