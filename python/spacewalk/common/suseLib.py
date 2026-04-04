#  pylint: disable=missing-module-docstring,invalid-name
# -*- coding: utf-8 -*-
#
# Copyright (c) 2010, 2011, 2012 Novell
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

try:
    import urllib.parse as urlparse
    from io import StringIO
except ImportError:
    import urlparse

    try:
        from cStringIO import StringIO
    except ImportError:
        from StringIO import StringIO

import io
import os
import re
import pycurl

from spacewalk.common.rhnLog import log_debug, log_error
from spacewalk.common.rhnConfig import initCFG, CFG, ConfigParserError

try:
    from spacewalk.server import rhnSQL
except ImportError:
    log_debug(2, "Loading suseLib without rhnSQL")

from rhn.connections import idn_puny_to_unicode

YAST_PROXY = "/root/.curlrc"
SYS_PROXY = "/etc/sysconfig/proxy"


class TransferException(Exception):
    """Transfer Error"""

    def __init__(self, value=None):
        Exception.__init__(self)
        self.value = value

    def __str__(self):
        return f"{self.value}"

    def __unicode__(self):
        # pylint: disable-next=consider-using-f-string
        return "%s" % str(self.value, "utf-8")


class URL(object):
    """URL class that allows modifying the various attributes of a URL"""

    # pylint: disable=R0902

    def __init__(self, url, username=None, password=None):
        u = urlparse.urlsplit(url)
        # pylint can't see inside the SplitResult class
        # pylint: disable=E1103
        self.scheme = u.scheme
        self.username = username or u.username
        self.password = password or u.password
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
            assert len(p) == 1, (
                "The query parameter contains a list of "
                "arguments instead of a single element. "
                f"{key} : {p}"
            )
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

    def getURL(self, stripPw=False):
        """Return the full url as a string"""
        netloc = ""
        path = self.path
        if self.username:
            netloc = self.username
        if self.password and not stripPw:
            netloc = f"{netloc}:{self.password}"
        elif self.password and stripPw:
            netloc = f"{netloc}:<secret>"
        if self.host and netloc:
            netloc = f"{netloc}@{self.host}"
        elif self.host:
            netloc = self.host

        if self.port:
            netloc = f"{netloc}:{self.port}"

        # Default ULN channels URIs are like: uln:///ol7_x86_64_u8_base
        # If not netloc, we fix the path to avoid getting url:/ol7_x86_64_u8_base
        # as results from urlunsplit
        if self.scheme == "uln" and not netloc:
            path = "//" + self.path

        query = self.query
        if stripPw and query != "" and query.find("=") == -1:
            query = "<token>"

        return urlparse.urlunsplit((self.scheme, netloc, path, query, self.fragment))


def _curl_debug(mtype, text):
    if mtype == 0:
        log_debug(4, f"* {text}")
    elif mtype == 1:
        # HEADER_IN
        log_debug(4, f"< {text}")
    elif mtype == 2:
        # HEADER_OUT
        log_debug(4, f"> {text}")
    elif mtype == 3:
        # DATA_IN
        log_debug(5, f"D< {text}")
    elif mtype == 4:
        # DATA_OUT
        log_debug(5, f"D> {text}")
    else:
        log_debug(6, f"{mtype}: {text}")
    return 0


def send(url, sendData=None):
    """Connect to url and return the result as stringIO

    :arg url: the url where the request will be sent
    :kwarg sendData: do a post-request when "sendData" is given.

    Returns the result as stringIO object.

    """
    timeout = 120
    if CFG.is_initialized() and CFG.has_key("TIMEOUT"):
        timeout = CFG.TIMEOUT
    curl = pycurl.Curl()

    curl.setopt(pycurl.CONNECTTIMEOUT, timeout)
    curl.setopt(pycurl.URL, url)
    curl.setopt(pycurl.DEBUGFUNCTION, _curl_debug)
    curl.setopt(pycurl.VERBOSE, True)
    proxy_url, proxy_user, proxy_pass = get_proxy(url)
    if proxy_url:
        curl.setopt(pycurl.PROXY, proxy_url)
    log_debug(2, f"Connect to {url}")
    if sendData is not None:
        curl.setopt(pycurl.POSTFIELDS, sendData)
        if (
            CFG.is_initialized()
            and CFG.has_key("DISABLE_EXPECT")
            and CFG.DISABLE_EXPECT
        ):
            # disable Expect header
            curl.setopt(pycurl.HTTPHEADER, ["Expect:"])

    curl.setopt(pycurl.FOLLOWLOCATION, True)

    response = StringIO()
    curl.setopt(pycurl.WRITEFUNCTION, response.write)

    try:
        curl.perform()
    except pycurl.error as e:
        if e.args[0] == 56:  # Proxy requires authentication
            log_debug(2, e.args[1])
            if not (proxy_user and proxy_pass):
                # pylint: disable-next=raise-missing-from
                raise TransferException(
                    "Proxy requires authentication, "
                    "but reading credentials from "
                    f"{YAST_PROXY} failed."
                )
            curl.setopt(pycurl.PROXYUSERPWD, f"{proxy_user}:{proxy_pass}")
        elif e.args[0] == 60:
            log_error(
                "Peer certificate could not be authenticated "
                "with known CA certificates."
            )
            # pylint: disable-next=raise-missing-from
            raise TransferException(
                "Peer certificate could not be "
                "authenticated with known CA "
                "certificates."
            )
        else:
            log_error(e.args[1])
            raise

    status = curl.getinfo(pycurl.HTTP_CODE)
    if status != 200 and not (URL(url).scheme == "file" and status == 0):
        log_error(
            f"Connecting to {URL(url).getURL(stripPw=True)} has failed with HTTP error code {status}."
        )
        raise TransferException(f"Connection failed with HTTP error {status}.")

    # StringIO.write leaves the cursor at the end of the file
    response.seek(0)
    return response


def accessible(url):
    """Try if url is accessible

    :arg url: the url which is tried to access

    Returns True if url is accessible, otherwise False.

    """
    timeout = 120
    if CFG.is_initialized() and CFG.has_key("TIMEOUT"):
        timeout = CFG.TIMEOUT
    curl = pycurl.Curl()

    curl.setopt(pycurl.CONNECTTIMEOUT, timeout)
    curl.setopt(pycurl.URL, url)
    curl.setopt(pycurl.DEBUGFUNCTION, _curl_debug)
    curl.setopt(pycurl.VERBOSE, True)
    proxy_url, proxy_user, proxy_pass = get_proxy(url)
    if proxy_url:
        curl.setopt(pycurl.PROXY, proxy_url)
    log_debug(2, f"Connect to {url}")

    curl.setopt(pycurl.FOLLOWLOCATION, True)
    curl.setopt(pycurl.NOBODY, True)

    try:
        curl.perform()
    except pycurl.error as e:
        if e.args[0] == 56:  # Proxy requires authentication
            log_debug(2, e.args[1])
            if not (proxy_user and proxy_pass):
                # pylint: disable-next=raise-missing-from
                raise TransferException(
                    "Proxy requires authentication, "
                    "but reading credentials from "
                    f"{YAST_PROXY} failed."
                )
            curl.setopt(pycurl.PROXYUSERPWD, f"{proxy_user}:{proxy_pass}")

    status = curl.getinfo(pycurl.HTTP_CODE)
    # OK or file
    if status == 200 or (URL(url).scheme == "file" and status == 0):
        return True
    return False


def get_proxy(url=None):
    """Return proxy information as a (url, username, password) tuple

    Returns None if no proxy URL/credentials could be read.

    If the url parameter is provided, a check against no_proxy will be made.
    (server.satellite.no_proxy)
    In case this connection should not use a proxy, the values returned
    are None for url, username and password.

    Order of lookup (https_proxy is always preferred over http_proxy):
    1. rhn.conf (server.satellite.http_proxy)
    2. .curlrc (--proxy, --proxy-user)

    """
    proxyurl, username, password = (
        _get_proxy_from_rhn_conf() or _get_proxy_from_yast() or (None, None, None)
    )
    if not url or (url and _useProxyFor(url)):
        return (proxyurl, username, password)
    return (None, None, None)


def findProduct(product):
    q_version = ""
    q_release = ""
    q_arch = ""
    product_id = None
    product_lower = {}
    product_lower["name"] = product["name"].lower()

    log_debug(2, f"Search for product: {product}")

    if product.get("version"):
        q_version = "or sp.version = :version"
        product_lower["version"] = product["version"].lower()
    if product.get("release"):
        q_release = "or sp.release = :release"
        product_lower["release"] = product["release"].lower()
    if product.get("arch"):
        q_arch = "or pat.label = :arch"
        product_lower["arch"] = product["arch"].lower()

    h = rhnSQL.prepare(f"""
    SELECT sp.id, sp.name, sp.version, pat.label as arch, sp.release
       FROM suseProducts sp
       LEFT JOIN rhnPackageArch pat ON pat.id = sp.arch_type_id
    WHERE sp.name = :name
       AND (sp.version IS NULL {q_version})
       AND (sp.release IS NULL {q_release})
       AND (sp.arch_type_id IS NULL {q_arch})
    ORDER BY name, version, release, arch
    """)
    h.execute(**product_lower)
    rs = h.fetchall_dict()

    if not rs:
        log_debug(1, "No Product Found")
        return None

    product_id = rs[0]["id"]

    if len(rs) > 1:
        # more than one product matches.
        # search for an exact match or take the first
        for p in rs:
            if (
                p["version"] == product["version"]
                and p["release"] == product["release"]
                and p["arch"] == product["arch"]
            ):
                product_id = p["id"]
                break

    return product_id


def findAllExtensionProductsOf(baseId, rootId):
    vals = {"baseId": baseId, "rootID": rootId}
    h = rhnSQL.prepare("""
        SELECT distinct ext.id, ext.release, ext.version, pa.label arch, ext.name, ext.base baseproduct
                FROM SUSEProductExtension pe
                JOIN SUSEProducts base on base.id = pe.base_pdid
                JOIN SUSEProducts ext on pe.ext_pdid = ext.id
                JOIN rhnpackagearch pa on pa.id = ext.arch_type_id
            WHERE base.id = :baseId and pe.root_pdid = :rootId and pe.recommended = 'Y';
        """)
    h.execute(**vals)
    rs = h.fetchall_dict()
    return rs or []


def channelForProduct(product, parent_id=None, org_id=None, user_id=None):
    """Find mandatory Channels for a given product and ostarget.

    If parent_id is None, a base channel is requested.
    Otherwise only channels are returned which have this id
    as parent channel.
    org_id and user_id are used to check for permissions.

    """
    product_id = findProduct(product)
    if not product_id:
        return None

    vals = {"pid": product_id, "org_id": org_id, "user_id": user_id}
    parent_statement = " IS NULL "
    if parent_id:
        parent_statement = " = :parent_id "
        vals["parent_id"] = parent_id

    h = rhnSQL.prepare(f"""
        SELECT ca.label arch,
        c.id,
        c.parent_channel,
        c.org_id,
        c.label,
        c.name,
        c.summary,
        c.description,
        TO_CHAR(c.last_modified at time zone 'UTC', 'YYYYMMDDHH24MISS') last_modified,
        -- If user_id is null, then the channel is subscribable
        rhn_channel.loose_user_role_check(c.id, :user_id, 'subscribe') subscribable
        FROM rhnChannel c
        JOIN suseProductChannel spc ON spc.channel_id = c.id
        JOIN rhnChannelArch ca ON c.channel_arch_id = ca.id
        WHERE spc.product_id = :pid
          AND spc.mandatory = 'Y'
          AND c.parent_channel {parent_statement}""")
    h.execute(**vals)
    rs = h.fetchall_dict()
    if not rs:
        log_debug(1, "No Channel Found")
        return None
    ret = []
    for channel in rs:
        subscribable = channel["subscribable"]
        del channel["subscribable"]

        if not subscribable:
            # Not allowed to subscribe to this channel
            continue

        ret.append(channel)
        log_debug(1, f'Found channel {channel["label"]} with id {channel["id"]}')

    # pylint: disable-next=use-implicit-booleaness-not-comparison
    if ret == []:
        ret = None
    return ret


def get_mirror_credentials():
    """Return a list of mirror credential tuples (user, pass)

    N.B. The config values will be read from the global configuration:
     server.susemanager.mirrcred_user
     server.susemanager.mirrcred_pass
     server.susemanager.mirrcred_user_1
     server.susemanager.mirrcred_pass_1
     etc.

    The credentials are read sequentially, when the first value is found
    to be missing, the process is aborted and the list of credentials
    that have been read so far are returned. For example if
    server.susemanager.mirrcred_pass_1 can not be read, only the first
    pair of default mirrcreds will be returned, even though
    mirrcred_user_2, mirrcred_pass_2 etc. might still exist.

    """
    comp = CFG.getComponent()
    initCFG("server.susemanager")

    creds = []

    # the default values should at least always be there
    if not CFG["mirrcred_user"] or not CFG["mirrcred_pass"]:
        initCFG(comp)
        raise ConfigParserError(
            "Could not read default mirror credentials: "
            "server.susemanager.mirrcred_user, "
            "server.susemanager.mirrcred_pass."
        )

    creds.append((CFG["mirrcred_user"], CFG["mirrcred_pass"]))

    # increment the credentials number, until we can't read one
    n = 1
    while True:
        try:
            creds.append((CFG[f"mirrcred_user_{n}"], CFG[f"mirrcred_pass_{n}"]))
        except (KeyError, AttributeError):
            break
        n += 1
    initCFG(comp)
    return creds


def isAllowedSlave(hostname):
    rhnSQL.initDB()
    if not rhnSQL.fetchone_dict(
        "select 1 from rhnISSSlave where slave = :hostname and enabled = 'Y'",
        hostname=idn_puny_to_unicode(hostname),
    ):
        log_error(f"Server '{hostname}' is not enabled for ISS.")
        return False
    return True


def hasISSSlaves():
    rhnSQL.initDB()
    if rhnSQL.fetchone_dict("select 1 from rhnISSSlave where enabled = 'Y'"):
        return True
    return False


def hasISSMaster():
    rhnSQL.initDB()
    if rhnSQL.fetchone_dict("select 1 from rhnISSMaster where is_current_master = 'Y'"):
        return True
    return False


def getISSCurrentMaster():
    rhnSQL.initDB()
    master = rhnSQL.fetchone_dict(
        "select label from rhnISSMaster where is_current_master = 'Y'"
    )
    if not master:
        return None
    return master["label"]


def _parse_curl_proxy_credentials(text):
    """Parse proxy credentials from the string :text:

    Return a (username, password) tuple or (None, None).

    """
    try:
        user_pass = re.search(
            r'^[\s-]+proxy-user(?:\s+|=)"([^:]+:[^\n]+)"\s*$', text, re.M
        ).group(1)
    except AttributeError:
        return (None, None)

    return re.sub('\\\\"', '"', user_pass).split(":")


def _parse_curl_proxy_url(text):
    try:
        return re.search(r'^[\s-]+proxy(?:\s+|=)"([^\n]+)"\s*$', text, re.M).group(1)
    except AttributeError:
        return None


def _get_proxy_from_yast():
    """Return a tuple of (url, user, pass) proxy information from YaST

    Returns None instead of a tuple if there was no proxy url. user,
    pass can be None.

    """
    f = None
    try:
        try:
            # pylint: disable-next=unspecified-encoding
            f = open(YAST_PROXY)
            contents = f.read()
        except IOError:
            log_debug(5, "Couldn't open " + YAST_PROXY)
            return None
    finally:
        if f:
            f.close()

    proxy_url = _parse_curl_proxy_url(contents)
    if not proxy_url:
        log_debug(1, "Could not read proxy URL from " + YAST_PROXY)
        return None

    username, password = _parse_curl_proxy_credentials(contents)

    return (proxy_url, username, password)


def _get_proxy_from_rhn_conf():
    """Return a tuple of (url, user, pass) proxy information from rhn config

    Returns None instead of a tuple if there was no proxy url. user,
    pass can be None.

    """
    comp = CFG.getComponent()
    if not CFG.has_key("http_proxy"):
        initCFG("server.satellite")
    result = None
    if CFG.http_proxy:
        # CFG.http_proxy format is <hostname>[:<port>] in 1.7
        url = f"http://{CFG.http_proxy}"
        # CFG.http_proxy_password can be a list in case of legitimate
        # commas "," are part of the password. If so, we need to
        # rebuilt the original password.
        result = (
            url,
            CFG.http_proxy_username,
            (
                CFG.http_proxy_password
                if not isinstance(CFG.http_proxy_password, list)
                else ",".join(CFG.http_proxy_password)
            ),
        )
    initCFG(comp)
    log_debug(2, "Could not read proxy URL from rhn config.")
    return result


# pylint complains because this method has too many return statements.
# pylint: disable=R0911


def _useProxyFor(url):
    """Return True if a proxy should be used for given url, otherwise False.

    This function uses server.satellite.no_proxy variable to check for
    hosts or domains which should not be connected via a proxy.

    server.satellite.no_proxy is a comma seperated list.
    Either an exact match, or the previous character
    is a '.', so host is within the same domain.
    A leading '.' in the pattern is ignored.
    See also 'man curl'

    """
    u = urlparse.urlsplit(url)
    # pylint can't see inside the SplitResult class
    # pylint: disable=E1103
    if u.scheme == "file" or not u.hostname:
        return False
    hostname = u.hostname.lower()
    if hostname in ["localhost", "127.0.0.1", "::1"]:
        return False
    comp = CFG.getComponent()
    if not CFG.has_key("no_proxy"):
        initCFG("server.satellite")
    if not CFG.has_key("no_proxy"):
        initCFG(comp)
        return True
    noproxy = CFG.no_proxy
    initCFG(comp)
    if not noproxy:
        return True
    if not isinstance(noproxy, list):
        if noproxy == "*":
            # just an asterisk disables all.
            return False
        noproxy = [noproxy]

    # No proxy: Either an exact match, or the previous character
    # is a '.', so host is within the same domain.
    # A leading '.' in the pattern is ignored. Some implementations
    # need '.foo.ba' to prevent 'foo.ba' from matching 'xfoo.ba'.
    for domain in noproxy:
        domain = domain.lower()
        if domain[0] == ".":
            domain = domain[1:]
        if hostname.endswith(domain) and (
            len(hostname) == len(domain)
            or hostname[len(hostname) - len(domain) - 1] == "."
        ):
            return False
    return True


def get_content_type(
    url: str,
    certfile: str = None,
    keyfile: str = None,
    cafile: str = None,
    proxies: dict = None,
    headers: dict = None,
):
    """
    Makes an HTTPS GET request to the given URL and return the Content-Type header.

    Args:
        url (str): The URL to make the request to.
        certfile (str, optional): Path to the client certificate file (e.g., 'client.pem').
        keyfile (str, optional): Path to the client's private key file (e.g., 'client_key.pem').
        cafile (str, optional): Path to a file containing concatenated CA certificates in PEM format.
                                If provided, pycurl will use these CAs to verify the server's certificate.
                                If not provided, default system CAs will be used.
        proxies (dict, optional): A dictionary of proxies. Keys are protocol names (e.g., 'http', 'https')
                                  and values are the proxy URLs (e.g., 'http://your.proxy.com:8080').
                                  PycURL typically uses a single proxy setting, so if both http and https
                                  proxies are given, the 'https' proxy will be prioritized for HTTPS requests.
        headers (dict, optional): A dictionary of custom HTTP headers to send with the request.
    """
    c = pycurl.Curl()
    buffer = (
        io.BytesIO()
    )  # Buffer to store the response body (not used, but required by pycurl)

    try:
        c.setopt(pycurl.URL, url)
        c.setopt(
            pycurl.WRITEFUNCTION, buffer.write
        )  # Dummy write function, content not used but needed

        # Set custom headers
        if headers:
            header_list = [f"{key}: {value}" for key, value in headers.items()]
            c.setopt(pycurl.HTTPHEADER, header_list)

        # Client certificate authentication
        if certfile and keyfile:
            if not os.path.exists(certfile):
                log_error(f"Error: Client certificate file not found at '{certfile}'")
                return ""
            if not os.path.exists(keyfile):
                log_error(f"Error: Client private key file not found at '{keyfile}'")
                return ""
            try:
                c.setopt(pycurl.SSLCERT, certfile)
                c.setopt(pycurl.SSLKEY, keyfile)
            except pycurl.error as pycurl_err:
                log_error(
                    f"PycURL SSL Error loading client certificate or key: {pycurl_err}"
                )
                log_error(
                    "Please ensure your certfile and keyfile are valid and in PEM format."
                )
                return ""

        # CA bundle for server certificate validation
        if cafile:
            if not os.path.exists(cafile):
                log_error(f"Error: CA bundle file not found at '{cafile}'")
                return ""
            c.setopt(pycurl.CAINFO, cafile)
        else:
            # For pycurl, it's often good practice to explicitly verify peer even if no custom CAINFO
            c.setopt(pycurl.SSL_VERIFYPEER, 1)
            c.setopt(pycurl.SSL_VERIFYHOST, 2)

        # Proxy configuration
        if proxies:
            # PycURL typically takes a single proxy.
            # We'll try to use the 'https' proxy if available for https URLs, otherwise 'http'.
            proxy_url = None
            if url.startswith("https://") and "https" in proxies:
                proxy_url = proxies["https"]
            elif "http" in proxies:
                proxy_url = proxies["http"]

            if proxy_url:
                c.setopt(pycurl.PROXY, proxy_url)
            else:
                log_error(
                    "Warning: No suitable proxy found in the 'proxies' dictionary."
                )

        # Perform the request
        c.perform()

        # Get Content-Type from info
        try:
            content_type = c.getinfo(pycurl.CONTENT_TYPE)
        except TypeError:
            content_type = None

        if content_type:
            # PycURL content_type might be bytes, decode it
            if isinstance(content_type, bytes):
                content_type = content_type.decode("utf-8")
            return content_type

    except pycurl.error as e:
        # pycurl.error contains the error code and string message
        error_code, error_string = e.args
        log_error(f"PycURL Error occurred for {url}: ({error_code}) {error_string}")
        if (
            error_code == pycurl.E_SSL_CACERT
            or error_code == pycurl.E_SSL_CERTPROBLEM
            or error_code == pycurl.E_PEER_FAILED_VERIFICATION
        ):
            log_error("This might be an SSL/TLS certificate validation error.")
    finally:
        c.close()  # Always close the curl handle
    return ""
