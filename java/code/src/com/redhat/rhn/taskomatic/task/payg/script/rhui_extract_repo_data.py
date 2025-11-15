# pylint: disable=missing-module-docstring
#
# Copyright (c) 2023 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SPDX-License-Identifier: GPL-2.0-only
#
# Please submit bugfixes or comments via https://bugs.opensuse.org/
#

import base64
import glob
import json
import platform
import re
import subprocess

# pylint: disable-next=unused-import
import os
import sys

# pylint: disable-next=unused-import
import traceback
import urllib.request
import urllib.parse as urlparse

PATTERN_SECTION = r"^\[(.+)\]$"
PATTERN_KV = r"\s*=\s*"

ID_DOC_HEADER = "X-RHUI-ID"
ID_SIG_HEADER = "X-RHUI-SIGNATURE"
TOKEN_TTL_HEADER = "X-aws-ec2-metadata-token-ttl-seconds"
TOKEN_HEADER_ID = "X-aws-ec2-metadata-token"
ID_DOC_URL = "http://169.254.169.254/latest/dynamic/instance-identity/document"
ID_SIG_URL = "http://169.254.169.254/latest/dynamic/instance-identity/signature"
TOKEN_URL = "http://169.254.169.254/latest/api/token"
# We do not want to use a proxy to read the Amazon instance metadata, so bypass
# any proxy that might be set, including by http{s}_proxy environment
# variable(s).
proxy_handler = urllib.request.ProxyHandler({})
opener = urllib.request.build_opener(proxy_handler)

cloud_vendor = "unknown"


def system_exit(code, messages=None):
    "Exit with a code and optional message(s). Saved a few lines of code."

    for message in messages:
        print(message, file=sys.stderr)
    sys.exit(code)


def _read_aws_metadata(url, token):
    req = urllib.request.Request(url)
    req.add_header(TOKEN_HEADER_ID, token)
    try:
        with opener.open(req) as response:
            return response.read()
    except urllib.error.URLError as e:
        # pylint: disable-next=consider-using-f-string
        system_exit(3, ["Unable to get aws metadata ({})".format(e)])


def _get_token():
    req = urllib.request.Request(url=TOKEN_URL, data=b"", method="PUT")
    req.add_header(TOKEN_TTL_HEADER, "3600")  # Time to live in seconds
    with opener.open(req) as response:
        return response.read()


def _load_id(token):
    """
    Loads and returns the Amazon metadata for identifying the instance.

    @rtype: string
    """
    return _read_aws_metadata(ID_DOC_URL, token)


def _load_signature(token):
    """
    Loads and returns the signature of hte Amazon identification metadata.

    @rtype: string
    """
    return _read_aws_metadata(ID_SIG_URL, token)


def _get_arch():
    return platform.processor()


def is_rhui_instance():
    return is_rhui


def get_rhui_url(url):
    """
    check the url if it is a RHUI url.
    If yes, return it, otherwise return an empty string
    """
    global cloud_vendor
    surl = url.strip().replace(",", "")
    urlparams = urlparse.urlparse(surl)
    if urlparams.hostname.startswith("rhui") and urlparams.hostname.endswith(
        ".redhat.com"
    ):
        cloud_vendor = "aws"
        return surl
    elif urlparams.hostname.startswith("rhui") and urlparams.hostname.endswith(
        ".microsoft.com"
    ):
        cloud_vendor = "azure"
        return surl
    elif urlparams.hostname.startswith("rhui.") and urlparams.hostname.endswith(
        ".googlecloud.com"
    ):
        cloud_vendor = "gcp"
        return surl
    elif urlparams.hostname.startswith("packages.cloud.google.com"):
        # this contains the package with the certificates
        cloud_vendor = "gcp"
        return surl
    return ""


def _parse_repositories():
    # pylint: disable-next=global-variable-undefined
    global is_rhui
    # pylint: disable-next=global-variable-undefined
    global repo_dict
    is_rhui = False
    repo_dict = {}

    try:
        repos_out = subprocess.check_output(
            ["yum", "repolist", "--all", "-v"],
            stderr=subprocess.PIPE,
            universal_newlines=True,
        )
    except subprocess.CalledProcessError as e:
        system_exit(
            # pylint: disable-next=consider-using-f-string
            2,
            # pylint: disable-next=consider-using-f-string
            ["Got error when getting repo processed URL(error {}):".format(e)],
        )
    repo_id = ""
    repo_url = ""
    for line in repos_out.split("\n"):
        if line.startswith("Repo-id"):
            ident = (line.split(":", 1)[1]).strip()
            repo_id = ident.split("/")[0]
            if "rhui-" in repo_id:
                is_rhui = True
        elif line.startswith("Repo-mirrors"):
            repo_url = get_rhui_url(line.split(":", 1)[1])
        elif repo_url == "" and line.startswith("Repo-baseurl"):
            # pylint: disable-next=anomalous-backslash-in-string
            repo_url = get_rhui_url(re.split("\s+", line)[2])
        elif line.strip() == "":
            if repo_id != "" and repo_url != "":
                repo_dict[repo_id] = {"url": repo_url}
            repo_id = ""
            repo_url = ""
    if repo_id != "" and repo_url != "":
        repo_dict[repo_id] = {"url": repo_url}

    # parse the repositories to get the matching certificates
    for repofile in glob.glob("/etc/yum.repos.d/*.repo"):
        # pylint: disable-next=unspecified-encoding
        with open(repofile, "r") as r:
            ident = ""
            state = 0
            for line in r:
                s = line.strip()
                if re.match(PATTERN_SECTION, s):
                    ident = re.findall(PATTERN_SECTION, s)[0]
                    state = 1
                    if "$basearch" in ident:
                        ident = ident.replace("$basearch", _get_arch())
                elif state == 1 and ident and ident in repo_dict:
                    if "=" in s:
                        k, v = re.split(PATTERN_KV, s, 1)
                        if k in ["sslclientcert", "sslclientkey", "sslcacert"]:
                            repo_dict[ident][k] = v
                            if "sslcacert" not in repo_dict[ident]:
                                repo_dict[ident]["sslcacert"] = "Bundle"  # default
                    elif s == "":
                        ident = ""
                        state = 0
    return repo_dict


def _get_rhui_info():
    # Retrieve the Amazon metadata
    try:
        token = _get_token()
        id_doc = _load_id(token)
        id_sig = _load_signature(token)
        id_doc_header = ""
        id_sig_header = ""

        if id_doc and id_sig:
            # Encode it so it can be inserted as an HTTP header
            # Signature does not need to be encoded, it already is.
            id_doc_header = base64.urlsafe_b64encode(id_doc).decode()
            id_sig_header = base64.urlsafe_b64encode(id_sig).decode()

        return {ID_DOC_HEADER: id_doc_header, ID_SIG_HEADER: id_sig_header}
    # pylint: disable-next=unused-variable
    except urllib.error.URLError as e:
        # not AWS cloud ?
        return {}


def _get_certificate_info():
    """
    Return a dict with all RHUI certificates and keys using
    the path as key
    """
    # pylint: disable-next=invalid-name
    usedCrts = set()
    for d in repo_dict.values():
        for k, v in d.items():
            if k.startswith("ssl"):
                usedCrts.add(v)

    certs = {}
    for crt in glob.glob("/etc/pki/rhui/*.*"):
        if crt not in usedCrts:
            # get only the certificates we really need
            continue
        # pylint: disable-next=unspecified-encoding
        with open(crt, "r") as c:
            certs[crt] = c.read()

    for crt in glob.glob("/etc/pki/rhui/**/*.*"):
        if crt not in usedCrts:
            # get only the certificates we really need
            continue
        # pylint: disable-next=unspecified-encoding
        with open(crt, "r") as c:
            certs[crt] = c.read()

    if "Bundle" in usedCrts:
        # pylint: disable-next=unspecified-encoding
        with open("/etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem", "r") as c:
            certs["Bundle"] = c.read()
    return certs


def load_instance_info():
    header_auth = _get_rhui_info()
    certs = _get_certificate_info()

    return {
        "type": "RHUI",
        "header_auth": header_auth,
        "certs": certs,
        "repositories": repo_dict,
    }


def main():
    _parse_repositories()
    if not is_rhui_instance():
        system_exit(1, ["instance is not connection to RHUI"])

    rhui_data = load_instance_info()
    print(json.dumps(rhui_data))


if __name__ == "__main__":
    try:
        main()
        sys.exit(0)
    except KeyboardInterrupt:
        system_exit(9, ["User interrupted process."])
    except SystemExit as e:
        sys.exit(e.code)
    # pylint: disable-next=broad-exception-caught
    except Exception as e:
        # traceback.print_exc()
        # pylint: disable-next=consider-using-f-string
        system_exit(9, ["ERROR: {}".format(e)])

# Error codes
# 1- system is not a RHUI instance
# 2- error returning existing repositories
# 3- error when getting processed URL and Header from RHUI
# 6- CA file for cloud RMT server not found
# 9- generic error
