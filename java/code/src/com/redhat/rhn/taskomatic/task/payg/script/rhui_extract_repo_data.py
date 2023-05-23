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
# Please submit bugfixes or comments via https://bugs.opensuse.org/
#

import base64
import glob
import json
import subprocess
import sys
import urllib.request

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
        system_exit(3, ["Unable to get aws metadata ({})".format(e)])

def _get_token():
    req = urllib.request.Request(url=TOKEN_URL,
        data=b'', method='PUT')
    req.add_header(TOKEN_TTL_HEADER, '3600') # Time to live in seconds
    try:
        with opener.open(req) as response:
            return response.read()
    except urllib.error.URLError as e:
        system_exit(3, ["Unable to get token ({})".format(e)])


def _load_id(token):
    '''
    Loads and returns the Amazon metadata for identifying the instance.

    @rtype: string
    '''
    return _read_aws_metadata(ID_DOC_URL, token)


def _load_signature(token):
    '''
    Loads and returns the signature of hte Amazon identification metadata.

    @rtype: string
    '''
    return _read_aws_metadata(ID_SIG_URL, token)


def is_rhui_instance():
    return is_rhui

def _parse_repositories():
    global is_rhui
    global repo_dict
    is_rhui = False
    repo_dict = {}

    try:
        repos_out = subprocess.check_output(["yum", "repolist", "--all", "-v"], stderr=subprocess.PIPE, universal_newlines=True)
    except subprocess.CalledProcessError as e:
        system_exit(2, ["Got error when getting repo processed URL(error {}):".format(e)])
    repo_id = ""
    repo_url = ""
    for line in repos_out.split("\n"):
        if line.startswith("Repo-id"):
            repo_id = line.split(":", 1)[1]
            if "rhui-" in repo_id:
                is_rhui = True
        elif line.startswith("Repo-mirrors"):
            repo_url = line.split(":", 1)[1]
        elif repo_url == "" and line.startswith("Repo-baseurl"):
            repo_url = line.split(":", 1)[1]
        elif line.strip() == "":
            if (repo_id.strip() != "" and repo_url.strip() != ""):
                repo_dict[repo_id.strip()] = repo_url.strip()
            repo_id = ""
            repo_url = ""

def _get_rhui_info():
    # Retrieve the Amazon metadata
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


def _get_certificate_info():
    client_cert = ""
    client_key = ""
    ca_cert = ""
    crt = glob.glob("/etc/pki/rhui/product/content-*.crt")
    if (len(crt) != 1):
        system_exit(6, "RHUI Client Certificate not found")
    with open(crt[0], "r") as c:
        client_cert = c.read()

    crt = glob.glob("/etc/pki/rhui/content-*.key")
    if (len(crt) != 1):
        system_exit(6, "RHUI Client Key not found")
    with open(crt[0], "r") as c:
        client_key = c.read()

    crt = glob.glob("/etc/pki/rhui/*.crt")
    if (len(crt) != 1):
        system_exit(6, "RHUI CA Certificate not found")
    with open(crt[0], "r") as c:
        ca_cert = c.read()

    return client_cert, client_key, ca_cert


def load_instance_info():
    header_auth = _get_rhui_info()
    client_cert_data, client_key_data, ca_cert_data = _get_certificate_info()

    return { "type": "RHUI",
             "header_auth": header_auth,
             "client_cert": client_cert_data,
             "client_key": client_key_data,
             "ca_cert": ca_cert_data,
             "repositories": repo_dict}

def main():
    _parse_repositories()
    if not is_rhui_instance():
        system_exit(1, ["instance is not connection to RHUI"])

    rhui_data = load_instance_info()
    print(json.dumps(rhui_data))


if __name__ == '__main__':
    try:
        main()
        sys.exit(0)
    except KeyboardInterrupt:
        system_exit(9, ["User interrupted process."])
    except SystemExit as e:
        sys.exit(e.code)
    except Exception as e:
        system_exit(9, ["ERROR: {}".format(e)])

# Error codes
# 1- system is not a RHUI instance
# 2- error returning existing repositories
# 3- error when getting processed URL and Header from RHUI 
# 6- CA file for cloud RMT server not found
# 9- generic error
