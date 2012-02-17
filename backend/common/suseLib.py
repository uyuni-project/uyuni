# -*- coding: utf-8 -*-
#
# Copyright (c) 2010 Novell
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

import re
import urlparse

import pycurl
# prevent build dependency cycles
# pylint: disable=W0611
from suseRegister.info import getProductProfile, parseProductProfileFile
from spacewalk.common.rhnLog import log_debug, log_error
from spacewalk.server import rhnSQL
try:
    from cStringIO import StringIO
except ImportError:
    from StringIO import StringIO

YAST_PROXY = "/root/.curlrc"

class TransferException(Exception):
    """Transfer Error"""
    def __init__(self, value=None):
        Exception.__init__(self)
        self.value = value

    def __str__(self):
        return "%s" % self.value

    def __unicode__(self):
        return '%s' % unicode(self.value, "utf-8")


def send(url, sendData=None):
    """Connect to ncc and return the XML document as stringIO

    :arg url: the url where the request will be sent
    :kwarg sendData: do a post-request when "sendData" is given.

    Returns the XML document as stringIO object.

    """
    connect_retries = 10
    try_counter = connect_retries
    curl = pycurl.Curl()

    curl.setopt(pycurl.URL, url)
    log_debug(2, "Connect to %s" % url)
    if sendData is not None:
        curl.setopt(pycurl.POSTFIELDS, sendData)

    # We implement our own redirection-following, because pycurl
    # 7.19 doesn't POST after it gets redirected. Ideally we'd be
    # using pycurl.POSTREDIR here, but that's in 7.21.
    curl.setopt(pycurl.FOLLOWLOCATION, False)

    response = StringIO()
    curl.setopt(pycurl.WRITEFUNCTION, response.write)

    try_counter = connect_retries
    while try_counter:
        try_counter -= 1
        try:
            curl.perform()
        except pycurl.error, e:
            if e[0] == 56: # Proxy requires authentication
                log_debug(2, e[1])
                proxy_credentials = get_proxy_credentials()
                curl.setopt(pycurl.PROXYUSERPWD, proxy_credentials)
            elif e[0] == 60:
                log_error("Peer certificate could not be authenticated "
                          "with known CA certificates.")
                raise TransferException("Peer certificate could not be "
                                        "authenticated with known CA "
                                        "certificates.")
            else:
                log_error(e[1])
                raise

        status = curl.getinfo(pycurl.HTTP_CODE)
        if status == 200 or (URL(url).scheme == "file" and status == 0): # OK or file
            break
        elif status in (301, 302): # redirects
            url = curl.getinfo(pycurl.REDIRECT_URL)
            log_debug(2, "Got redirect to %s" % url)
            curl.setopt(pycurl.URL, url)
    else:
        log_error("Connecting to %s has failed after %s "
                  "tries with HTTP error code %s." %
                  (url, connect_retries, status))
        raise TransferException("Connection failed after %s tries with "
                                "HTTP error %s." % (connect_retries, status))

    # StringIO.write leaves the cursor at the end of the file
    response.seek(0)
    return response

def get_proxy_credentials():
    """Return proxy credentials as a string in the form username:password"""
    try:
        f = open(YAST_PROXY)
    except IOError:
        log_error("Proxy requires authentication. "
                  "Could not open the file %s in order to get the "
                  "credentials." % YAST_PROXY)
        raise
    contents = f.read()
    f.close()

    try:
        creds = re.search('^[\s-]+proxy-user\s*=?\s*"([^:]+:.+)"\s*$',
                          contents, re.M).group(1)
    except AttributeError:
        log_error("Proxy requires authentication. "
                  "Failed reading credentials from %s"
                  % YAST_PROXY)
        raise TransferException("Proxy requires authentication. "
                                "Failed reading credentials from "
                                "%s" % YAST_PROXY)
    creds = re.sub('\\\\"', '"', creds)
    return creds

def findProduct(product):
    q_version = ""
    q_release = ""
    q_arch    = ""
    product_id = None
    product_lower = {}
    product_lower['name'] = product['name'].lower()

    log_debug(2, "Search for product: %s" % product)

    if 'version' in product and product['version'] != "":
        q_version = "or sp.version = :version"
        product_lower['version'] = product['version'].lower()
    if 'release' in product and product['release'] != "":
        q_release = "or sp.release = :release"
        product_lower['release'] = product['release'].lower()
    if 'arch' in product and product['arch'] != "":
        q_arch = "or pat.label = :arch"
        product_lower['arch'] = product['arch'].lower()

    h = rhnSQL.prepare("""
    SELECT sp.id, sp.name, sp.version, pat.label as arch, sp.release
       FROM suseProducts sp
       LEFT JOIN rhnPackageArch pat ON pat.id = sp.arch_type_id
    WHERE sp.name = :name
       AND (sp.version IS NULL %s)
       AND (sp.release IS NULL %s)
       AND (sp.arch_type_id IS NULL %s)
    ORDER BY name, version, release, arch
    """ % (q_version, q_release, q_arch))
    h.execute(**product_lower)
    rs = h.fetchall_dict()

    if not rs:
        log_debug(1, "No Product Found")
        return None

    product_id = rs[0]['id']

    if len(rs) > 1:
        # more than one product matches.
        # search for an exact match or take the first
        for p in rs:
            if (p['version'] == product['version'] and
                p['release'] == product['release'] and
                p['arch'] == product['arch']):
                product_id = p['id']
                break

    return product_id

def channelForProduct(product, ostarget, parent_id=None, org_id=None,
                      user_id=None):
    """Find Channels for a given product and ostarget.

    If parent_id is None, a base channel is requested.
    Otherwise only channels are returned which have this id
    as parent channel.
    org_id and user_id are used to check for permissions.

    """
    product_id = findProduct(product)
    if not product_id:
        return None

    vals = {
        'pid'      : product_id,
        'ostarget' : ostarget,
        'org_id'   : org_id,
        'user_id'  : user_id
        }
    parent_statement = " IS NULL "
    if parent_id:
        parent_statement = " = :parent_id "
        vals['parent_id'] = parent_id


    h = rhnSQL.prepare("""
        SELECT ca.label arch,
        c.id,
        c.parent_channel,
        c.org_id,
        c.label,
        c.name,
        c.summary,
        c.description,
        to_char(c.last_modified, 'YYYYMMDDHH24MISS') last_modified,
        rhn_channel.available_chan_subscriptions(c.id, :org_id) available_subscriptions,
        -- If user_id is null, then the channel is subscribable
        rhn_channel.loose_user_role_check(c.id, :user_id, 'subscribe') subscribable
        FROM rhnChannel c
        JOIN suseProductChannel spc ON spc.channel_id = c.id
        JOIN suseOSTarget sot ON sot.channel_arch_id = c.channel_arch_id
        JOIN rhnChannelArch ca ON c.channel_arch_id = ca.id
        WHERE spc.product_id = :pid
          AND sot.os = :ostarget
          AND c.parent_channel %s""" % parent_statement)
    h.execute(**vals)
    rs = h.fetchall_dict()
    if not rs:
        log_debug(1, "No Channel Found")
        return None
    ret = []
    for channel in rs:
        subscribable = channel['subscribable']
        del channel['subscribable']

        if not subscribable:
            # Not allowed to subscribe to this channel
            continue

        ret.append(channel)
        log_debug(1, "Found channel %s with id %d" % (channel['label'], channel['id']))

    if ret == []:
        ret = None
    return ret

class URL(object):
    """URL class that allows modifying the various attributes of a URL"""
    # pylint: disable=R0902
    def __init__(self, url):
        u = urlparse.urlsplit(url)
        # pylint can't see inside the SplitResult class
        # pylint: disable=E1103
        self.scheme = u.scheme
        self.username = u.username
        self.password = u.password
        self.host = u.hostname
        self.port = u.port
        self.path = u.path
        self.query = u.query
        self.fragment = u.fragment

        if self.query:
            self._parse_query()

    def get_query_param(self, key, default=None):
        """Return a query parameter

        Note: this assumes that the parameter contains at most a single
        element (not a list).

        """
        # paramsdict has a list of elements as values, but we assume the
        # most common case where the list has only one element
        # pylint: disable=E1101
        p = self.paramsdict.get(key, default)
        if p:
            assert len(p) == 1, ("The query parameter contains a list of "
                                 "arguments instead of a single element. "
                                 "%s : %s" % (key, p))
            p = p[0]
        return p

    def __setattr__(self, attr, value):
        if attr == "query":
            self.__dict__[attr] = value.lstrip("?")
            self._parse_query()
	# pylint: disable=E1101
        elif attr == "paramsdict":
            # query should be used instead
            raise AttributeError("can't set attribute")
        else:
            self.__dict__[attr] = value

    def _parse_query(self):
        """Parse self.query and populate self.paramsdict

        self.paramsdict is a dict of key: [value1, value2, value3]

        """
        self.__dict__["paramsdict"] = urlparse.parse_qs(self.query)

    def getURL(self):
        """Return the full url as a string"""
        netloc = ""
        if self.username:
            netloc = self.username
        if self.password:
            netloc = '%s:%s' % (netloc, self.password)
        if self.host and netloc :
            netloc = '%s@%s' % (netloc, self.host)
        elif self.host:
            netloc = self.host

        if self.port:
            netloc = '%s:%s' % (netloc, self.port)

        return urlparse.urlunsplit((self.scheme, netloc, self.path,
                                    self.query, self.fragment))
