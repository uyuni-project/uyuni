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
import urlparse
from wsgi import wsgiRequest
import xml.etree.ElementTree as etree
from xml.parsers.expat import ExpatError

from rhn.connections import idn_ascii_to_pune

from spacewalk.common.rhnConfig import CFG, initCFG
from spacewalk.common.rhnLog import log_debug, log_error, initLOG
from spacewalk.common import apache, suseLib

LISTSUBSCRIPTION_CACHE = '/var/cache/rhn/ncc-data/subscriptions.xml'
PRODUCTDATA_CACHE = '/var/cache/rhn/ncc-data/productdata.xml'

def handle(environ, start_response):
    initCFG('server.susemanager')
    initLOG('/var/log/rhn/rhn_server_sat.log', CFG.DEBUG)

    req = wsgiRequest.WsgiRequest(environ, start_response)

    if not suseLib.isAllowedSlave(req.get_remote_host()):
        log_error('ISS Slave [%s] not alowed' % req.get_remote_host())
        req.send_http_header(status=apache.HTTP_FORBIDDEN)
        return req.output

    req_body = req.read()

    try:
        req_xml = etree.fromstring(req_body)
    except ExpatError:
        log_error("Invalid XML document")
        log_debug(2, "Request: %s" % req_body)
        req.send_http_header(status=apache.HTTP_INTERNAL_SERVER_ERROR)
        return req.output

    root = req_xml
    user = None
    passwd = None
    ident = None
    for child in list(root):
        if child.tag.endswith('authuser'):
            user = child.text
        elif child.tag.endswith('authpass'):
            passwd = child.text
        elif child.tag.endswith('smtguid'):
            ident = child.text
        elif child.tag.endswith('register') or child.tag.endswith('de-register'):
            for subreg in child:
                if subreg.tag.endswith('authuser'):
                    user = subreg.text
                elif subreg.tag.endswith('authpass'):
                    passwd = subreg.text
                elif subreg.tag.endswith('smtguid'):
                    ident = subreg.text

    if not (user and passwd and ident):
        log_error("Invalid authentication")
        log_debug(1, "User: %s Ident: %s" % (user, ident))
        req.send_http_header(status=apache.HTTP_FORBIDDEN)
        return req.output

    if user != CFG.mirrcred_user or passwd != CFG.mirrcred_pass:
        log_error("Invalid username and password from %s" % ident)
        req.send_http_header(status=apache.HTTP_FORBIDDEN)
        return req.output
    try:
        if root.tag.endswith('productdata'):
            with open(PRODUCTDATA_CACHE, 'r') as f:
                req.write(f.read())
        elif root.tag.endswith('listsubscriptions'):
            with open(LISTSUBSCRIPTION_CACHE, 'r') as f:
                req.write(f.read())
        elif root.tag.endswith('bulkop'):
            # authentication is ok, lets forward the request to the parent
            if CFG.ISS_PARENT:
                url = suseLib.URL('https://%s' % (CFG.ISS_PARENT))
            else:
                url = suseLib.URL(CFG.reg_url)
            u = urlparse.urlsplit(req.uri)
            # pylint can't see inside the SplitResult class
            # pylint: disable=E1103
            url.path = u.path
            url.query = u.query
            new_url = url.getURL()
            try:
                log_debug(4, "Request:", req_body)
                f = suseLib.send(new_url, req_body)
            except:
                req.send_http_header(status=apache.HTTP_INTERNAL_SERVER_ERROR)
                return req.output
            resp = f.read()
            log_debug(4, "Response:", resp)
            req.write(resp)
        else:
            log_error("Unsupported request '%s' from %s" % (root.tag, ident))
            req.send_http_header(status=apache.HTTP_BAD_REQUEST)
            return req.output
    except Exception, e:
        log_error("Accessing cache file failed: %s" % e)
        req.send_http_header(status=apache.HTTP_INTERNAL_SERVER_ERROR)
        return req.output

    req.send_http_header()
    return req.output


