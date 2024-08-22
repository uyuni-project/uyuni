#!/usr/bin/python3
#
# Copyright (c) 2021, SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#


## language imports
import os
import sys
import argparse
import traceback
import shutil
import tempfile
import time
import subprocess
from collections import namedtuple
from datetime import datetime

from spacewalk.common.rhnLog import initLOG, log_time, log_clean
from uyuni.common.fileutils import getUidGid

LOGFILE = "/var/log/rhn/mgr-ssl-cert-setup.log"
PKI_DIR = "/etc/pki/"
SRV_KEY_NAME = "spacewalk.key"

APACHE_CRT_NAME = "spacewalk.crt"
APACHE_CRT_FILE = os.path.join(PKI_DIR, "tls", "certs", APACHE_CRT_NAME)
APACHE_KEY_FILE = os.path.join(PKI_DIR, "tls", "private", SRV_KEY_NAME)
PG_KEY_FILE = os.path.join(PKI_DIR, "tls", "private", "pg-" + SRV_KEY_NAME)

JABBER_CRT_FILE = os.path.join(PKI_DIR, "spacewalk", "jabberd", "server.pem")

ROOT_CA_NAME = "RHN-ORG-TRUSTED-SSL-CERT"
PKI_ROOT_CA_NAME = "LOCAL-" + ROOT_CA_NAME

ROOT_CA_HTTP_DIR = "/srv/www/htdocs/pub/"
if not os.path.exists(ROOT_CA_HTTP_DIR):
    # Red Hat
    ROOT_CA_HTTP_DIR = "/var/www/html/pub/"

CA_TRUST_DIR = os.path.join(PKI_DIR, "trust", "anchors")
if not os.path.exists(CA_TRUST_DIR):
    # Red Hat
    CA_TRUST_DIR = os.path.join(PKI_DIR, "ca-trust", "source", "anchors")

SALT_CA_DIR = "/usr/share/susemanager/salt/certs/"

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


class CertCheckError(Exception):
    pass


FilesContent = namedtuple("FilesContent", ["root_ca", "server_cert", "server_key", "intermediate_cas"])

def log_error(msg):
    frame = traceback.extract_stack()[-2]
    log_clean(
        0,
        "{0}: {1}.{2}({3}) - {4}".format(log_time(), frame[0], frame[2], frame[1], msg),
    )
    sys.stderr.write("{0}\n".format(msg))


def log(msg, level=0):
    frame = traceback.extract_stack()[-2]
    log_clean(
        level,
        "{0}: {1}.{2}({3}) - {4}".format(log_time(), frame[0], frame[2], frame[1], msg),
    )
    if level < 1:
        sys.stdout.write("{0}\n".format(msg))


def processCommandline():
    usage = "%(prog)s [options] [command]"
    parser = argparse.ArgumentParser(usage=usage)
    parser.add_argument("-r", "--root-ca-file", help="Path to the Root CA")
    parser.add_argument(
        "-i",
        "--intermediate-ca-file",
        action="append",
        default=[],
        help="Path to an intermediate CA",
    )
    parser.add_argument(
        "-s", "--server-cert-file", help="Path to the Server Certificate"
    )
    parser.add_argument(
        "-k", "--server-key-file", help="Path to the Server Private Key"
    )
    parser.add_argument("--check-only", "-c", action="store_true")
    parser.add_argument("--verbose", "-v", action="count", default=0)

    options = parser.parse_args()
    initLOG(LOGFILE, options.verbose or 1)

    log(sys.argv, 1)
    return options


def checkOptions(root_ca_file, server_cert_file, server_key_file, intermediate_ca_files):
    if not root_ca_file:
        log_error("Root CA is required")
        sys.exit(1)
    if not os.path.exists(root_ca_file):
        log_error("Root CA: file not found {}".format(root_ca_file))
        sys.exit(1)

    if not server_cert_file:
        log_error("Server Certificate is required")
        sys.exit(1)
    if not os.path.exists(server_cert_file):
        log_error(
            "Server Certificate: file not found {}".format(server_cert_file)
        )
        sys.exit(1)

    if not server_key_file:
        log_error("Server Private Key is required")
        sys.exit(1)
    if not os.path.exists(server_key_file):
        log_error(
            "Server Private Key: file not found {}".format(server_key_file)
        )
        sys.exit(1)

    for ica in intermediate_ca_files:
        if not os.path.exists(ica):
            log_error("Intermediate CA: file not found {}".format(ica))
            sys.exit(1)


def readAllFiles(root_ca_file, server_cert_file, server_key_file, intermediate_ca_files):

    intermediate_cas = []
    clist = readSplitCertificates(root_ca_file)
    root_ca = clist[0]
    intermediate_cas.extend(clist[1:])

    clist = readSplitCertificates(server_cert_file)
    server_cert = clist[0]
    intermediate_cas.extend(clist[1:])

    for ica in intermediate_ca_files:
        clist = readSplitCertificates(ica)
        intermediate_cas.extend(clist)

    server_key = ""
    with open(server_key_file, "r", encoding="utf-8") as f:
        server_key = f.read()

    return FilesContent(
        root_ca=root_ca,
        server_cert=server_cert,
        server_key=server_key,
        intermediate_cas=intermediate_cas,
    )


def readSplitCertificates(certfile):

    isContent = False
    cert_list = []
    cert = ""
    with open(certfile, "r", encoding="utf-8") as f:
        certs_content = f.read()
        for line in certs_content.splitlines(keepends=True):
            if not isContent and line.startswith("-----BEGIN"):
                isContent = True
                cert = ""
            if isContent:
                cert += line
            if isContent and line.startswith("-----END"):
                cert_list.append(cert)
                isContent = False
    return cert_list


# pylint: disable-next=invalid-name
def prepareData(root_ca_content, server_cert_content, intermediate_ca_content):
    """
    Create a result dict with all certificates and pre-parsed data
    with the subject_hash as key.
    """
    ret = dict()

    # pylint: disable-next=invalid-name
    content = [root_ca_content]
    content.extend(intermediate_ca_content)

    for cert in content:
        data = getCertData(cert)
        data["content"] = cert
        shash = data["subject_hash"]
        if shash:
            ret[shash] = data
    data = getCertData(server_cert_content)
    shash = data["subject_hash"]
    if shash:
        ret[shash] = data

    return ret


def isCA(cert):
    out = subprocess.run(
        ["openssl", "x509", "-noout", "-ext", "basicConstraints"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=cert.encode("utf-8"),
    )
    if out.returncode:
        log_error(
            "Unable to parse the certificate: {}".format(out.stderr.decode("utf-8"))
        )
        return False
    for line in out.stdout.decode("utf-8").splitlines():
        if "CA:TRUE" in line.upper():
            return True
    return False


def isValid(startdate, enddate, subject):
    #  Not Before: Nov 12 14:36:13 2021 GMT
    #  Not After : Sep  1 14:36:13 2024 GMT

    start = datetime.strptime(startdate, "%b %d %H:%M:%S %Y %Z")
    end = datetime.strptime(enddate, "%b %d %H:%M:%S %Y %Z")
    now = datetime.utcnow()
    if now < start:
        raise CertCheckError("Certificate '{}' not yet valid".format(subject))
    if now > end:
        raise CertCheckError("Certificate '{}' is expired".format(subject))


def getCertData(cert):
    data = dict()
    out = subprocess.run(
        [
            "openssl",
            "x509",
            "-noout",
            "-subject",
            "-subject_hash",
            "-startdate",
            "-enddate",
            "-issuer",
            "-issuer_hash",
            "-modulus",
            "-ext", "subjectKeyIdentifier,authorityKeyIdentifier"
        ],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=cert.encode("utf-8"),
    )
    if out.returncode:
        log_error(
            "Unable to parse the certificate: {}".format(out.stderr.decode("utf-8"))
        )
        return None
    nextval = ""
    for line in out.stdout.decode("utf-8").splitlines():
        if line.strip() == "":
            continue
        if line.startswith("subject="):
            data["subject"] = line[8:].strip()
        elif line.startswith("issuer="):
            data["issuer"] = line[7:].strip()
        elif line.startswith("notBefore="):
            data["startdate"] = line[10:].strip()
        elif line.startswith("notAfter="):
            data["enddate"] = line[9:].strip()
        elif line.startswith("Modulus="):
            data["modulus"] = line[8:].strip()
        elif line.startswith("X509v3 Subject Key Identifier"):
            nextval = "subjectKeyIdentifier"
        elif line.startswith("X509v3 Authority Key Identifier"):
            nextval = "authorityKeyIdentifier"
        elif line.startswith("    "):
            if nextval == "subjectKeyIdentifier":
                data["subjectKeyIdentifier"] = line.strip().upper()
            elif nextval == "authorityKeyIdentifier" and line.startswith("    keyid:"):
                data["authorityKeyIdentifier"] = line[10:].strip().upper()
        elif "subject_hash" not in data:
            # subject_hash comes first without key to identify it
            data["subject_hash"] = line.strip()
        else:
            # second issue_hash without key to identify this value
            data["issuer_hash"] = line.strip()
    data["isca"] = isCA(cert)
    data["content"] = cert
    if data["subject"] == data["issuer"]:
        data["root"] = True
        # Some Root CAs might not have their authorityKeyIdentifier set to themself
        if data["isca"] and "authorityKeyIdentifier" not in data:
            data["authorityKeyIdentifier"] = data["subjectKeyIdentifier"]
    else:
        data["root"] = False

    return data


def getCertWithText(cert):
    out = subprocess.run(
        ["openssl", "x509", "-text"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=cert.encode("utf-8"),
    )
    if out.returncode:
        log_error("Invalid Certificate: {}".format(out.stderr.decode("utf-8")))
        return None
    return out.stdout.decode("utf-8")


def getPrivateKey(key):
    # set an invalid password to prevent asking in case of an encrypted one
    out = subprocess.run(
        ["openssl", "pkey", "-passin", "pass:invalid", "-text", "-noout"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=key.encode("utf-8")
    )

    if out.returncode:
        log_error("Invalid or encrypted Key: {}".format(out.stderr.decode("utf-8")))
        return None
    return out.stdout.decode("utf-8")


def checkKeyBelongToCert(key, cert):
    out = subprocess.run(
        ["openssl", "pkey", "-pubout"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=key.encode("utf-8"),
    )
    if out.returncode:
        log_error("Invalid Key: {}".format(out.stderr.decode("utf-8")))
        raise CertCheckError("Invalid Key")
    keyPubKey = out.stdout.decode("utf-8")
    out = subprocess.run(
        ["openssl", "x509", "-noout", "-pubkey"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=cert.encode("utf-8"),
    )
    if out.returncode:
        log_error("Invalid Cert file: {}".format(out.stderr.decode("utf-8")))
        raise CertCheckError("Invalid Certificate")

    certPubKey = out.stdout.decode("utf-8")
    if keyPubKey != certPubKey:
        log_error("The provided key does not belong to the server certificate")
        log("{} vs. {}".format(keyPubKey, certPubKey), 1)
        raise CertCheckError("Key does not belong to Certificate")


def checkCompleteCAChain(server_cert_content, certData):
    foundRootCA = False
    if len(certData.keys()) == 0:
        raise CertCheckError("No CAs found")

    serverCertHash = None
    for h, data in certData.items():
        if data["content"] == server_cert_content:
            serverCertHash = h
            break

    if certData[serverCertHash]["isca"]:
        raise CertCheckError("Server Certificate must not be a CA")

    subject = certData[serverCertHash]["subject"]
    ihash = certData[serverCertHash]["issuer_hash"]
    issuerKeyId = certData[serverCertHash]["authorityKeyIdentifier"]

    if not ihash or ihash not in certData:
        raise CertCheckError("No CA found for server certificate")

    cert = getCertWithText(certData[serverCertHash]["content"])
    if not cert:
        raise CertCheckError("Unable to parse the server certificate")

    isValid(
        certData[serverCertHash]["startdate"],
        certData[serverCertHash]["enddate"],
        certData[serverCertHash]["subject"],
    )

    while ihash in certData:
        keyId = certData[ihash]["subjectKeyIdentifier"]
        if not (keyId and issuerKeyId and keyId == issuerKeyId):
            raise CertCheckError(
                "Incomplete CA Chain. Key Identifiers do not match. Unable to find issuer of '{}'".format(subject)
            )
        if not certData[ihash]["isca"]:
            raise CertCheckError("CA missing basic constraints extension")

        subject = certData[ihash]["subject"]
        nexthash = certData[ihash]["issuer_hash"]
        issuerKeyId = certData[ihash]["authorityKeyIdentifier"]
        isValid(certData[ihash]["startdate"], certData[ihash]["enddate"], subject)

        if nexthash == ihash:
            # Found Root CA, we can exit
            foundRootCA = True
            if not certData[ihash]["root"]:
                raise CertCheckError("Root CA has different issuer")
            break
        else:
            if certData[ihash]["root"]:
                raise CertCheckError("Intermediate CA has subject equals to issuer")

        ihash = nexthash

    if not foundRootCA:
        raise CertCheckError(
            "Incomplete CA Chain. Unable to find issuer of '{}'".format(subject)
        )


def generateJabberCert(server_cert_content, server_key_content, certData):
    certWithChain = generateCertWithChainFile(server_cert_content, certData)
    return certWithChain + server_key_content


def generateApacheCert(server_cert_content, certData):
    return generateCertWithChainFile(server_cert_content, certData)


def generateCertWithChainFile(serverCert, certData):
    retContent = ""

    if len(certData.keys()) == 0:
        log_error("No CA found in Hash")
        return ""

    serverCertHash = None
    for h, data in certData.items():
        if data["content"] == serverCert:
            serverCertHash = h
            break

    ihash = certData[serverCertHash]["issuer_hash"]
    if not ihash or ihash not in certData:
        log_error("No CA found for server certificate")
        return ""
    cert = getCertWithText(serverCert)
    if not cert:
        log_error("Unable to get the server certificate")
        return ""
    retContent += cert
    while ihash in certData:
        nexthash = certData[ihash]["issuer_hash"]
        cert = getCertWithText(certData[ihash]["content"])
        if not cert:
            return ""
        if nexthash == ihash:
            # Found Root CA, we can exit
            break
        ihash = nexthash
        retContent += cert
    return retContent


def deployApache(apache_cert_content, server_key_content):
    if os.path.exists(APACHE_KEY_FILE):
        os.remove(APACHE_KEY_FILE)
    if os.path.exists(APACHE_CRT_FILE):
        os.remove(APACHE_CRT_FILE)
    with open(APACHE_KEY_FILE, "w", encoding="utf-8") as f:
        f.write(server_key_content)
    os.chmod(APACHE_KEY_FILE, int("0600", 8))
    with open(APACHE_CRT_FILE, "w", encoding="utf-8") as f:
        f.write(apache_cert_content)
    # exists on server and proxy
    os.system("/usr/bin/spacewalk-setup-httpd")
    log(
"""After changing the server certificate please execute:
$> spacewalk-service stop """)


def deployJabberd(jabber_cert_content):
    j_uid, j_gid = getUidGid("jabber", "jabber")
    if j_uid and j_gid:
        if os.path.exists(JABBER_CRT_FILE):
            os.remove(JABBER_CRT_FILE)
        with open(JABBER_CRT_FILE, "w") as f:
            f.write(jabber_cert_content)
        os.chmod(JABBER_CRT_FILE, int("0600", 8))
        os.chown(JABBER_CRT_FILE, j_uid, j_gid)


def deployPg(server_key_content):
    pg_uid, pg_gid = getUidGid("postgres", "postgres")
    if pg_uid and pg_gid:
        # deploy only the key with different permissions
        # the certificate is the same as for apache
        if os.path.exists(PG_KEY_FILE):
            os.remove(PG_KEY_FILE)
        with open(PG_KEY_FILE, "w", encoding="utf-8") as f:
            f.write(server_key_content)
        os.chmod(PG_KEY_FILE, int("0600", 8))
        os.chown(PG_KEY_FILE, pg_uid, pg_gid)

        log("""$> systemctl restart postgresql.service """)

def deployCAInDB(certData):
    if not os.path.exists("/usr/bin/rhn-ssl-dbstore"):
        # not a Uyuni Server - skip deploying into DB
        return

    for h, ca in certData.items():
        if ca["root"]:
            out = subprocess.run(
                ["/usr/bin/rhn-ssl-dbstore", "--ca-cert", "-"],
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                input=ca["content"].encode("utf-8"),
            )
            if out.returncode:
                log_error("Failed to upload CA Certificate to DB: {}".format(out.stderr.decode("utf-8")))
                raise OSError("Failed to upload CA Certificate to DB")
            break


def deployCAUyuni(certData):
    for h, ca in certData.items():
        if ca["root"]:
            if os.path.exists(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME)):
                os.remove(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME))
            with open(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME), "w") as f:
                f.write(ca["content"])
            os.chmod(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME), int("0644", 8))

            if os.path.exists(os.path.join(CA_TRUST_DIR, PKI_ROOT_CA_NAME)):
                os.remove(os.path.join(CA_TRUST_DIR, PKI_ROOT_CA_NAME))
            with open(os.path.join(CA_TRUST_DIR, PKI_ROOT_CA_NAME), "w") as f:
                f.write(ca["content"])
            os.chmod(os.path.join(CA_TRUST_DIR, PKI_ROOT_CA_NAME), int("0644", 8))

            # SALT_CA_DIR exists only on the server, ignore on proxies
            if os.path.exists(SALT_CA_DIR):
                if os.path.exists(os.path.join(SALT_CA_DIR, ROOT_CA_NAME)):
                    os.remove(os.path.join(SALT_CA_DIR, ROOT_CA_NAME))
                with open(os.path.join(SALT_CA_DIR, ROOT_CA_NAME), "w") as f:
                    f.write(ca["content"])
                os.chmod(os.path.join(SALT_CA_DIR, ROOT_CA_NAME), int("0644", 8))
            break
    # in case a systemd timer try to do the same
    time.sleep(3)
    os.system("/usr/share/rhn/certs/update-ca-cert-trust.sh")
    log(
"""$> spacewalk-service start

As the CA certificate has been changed, please deploy the CA to all registered clients.
On salt-managed clients, you can do this by applying the highstate.""")


def checks(server_key_content,server_cert_content, certData):
    """
    Perform different checks on the input data
    """
    checkCompleteCAChain(server_cert_content, certData)

    if not getPrivateKey(server_key_content):
        raise CertCheckError("Unable to read the server key. Is it maybe encrypted?")

    checkKeyBelongToCert(server_key_content, server_cert_content)


def getContainersSetup(root_ca_content, intermediate_ca_content, server_cert_content, server_key_content):
    if not root_ca_content:
        raise CertCheckError("Root CA is required")
    if not server_cert_content:
        raise CertCheckError("Server Certificate is required")
    if not server_key_content:
        raise CertCheckError("Server Private Key is required")

    certData = prepareData(
            root_ca_content,
            server_cert_content,
            intermediate_ca_content)
    checks(server_key_content,server_cert_content, certData)
    apache_cert_content = generateApacheCert(server_cert_content, certData)
    if not apache_cert_content:
        raise CertCheckError("Failed to generate certificates")
    return apache_cert_content


def _main():
    """main routine"""

    options = processCommandline()
    checkOptions(options.root_ca_file, options.server_cert_file, options.server_key_file, options.intermediate_ca_file)

    files_content = readAllFiles(
        options.root_ca_file,
        options.server_cert_file,
        options.server_key_file,
        options.intermediate_ca_file,
    )
    if options.check_only:
        getContainersSetup(files_content.root_ca, files_content.intermediate_cas, files_content.server_cert, files_content.server_key)
        sys.exit(0)

    certData = prepareData(
            files_content.root_ca,
            files_content.server_cert,
            files_content.intermediate_cas)
    checks(files_content.server_key, files_content.server_cert, certData)
    apache_cert_content = generateApacheCert(files_content.server_cert, certData)
    if not apache_cert_content:
        log_error("Failed to generate certificate for Apache")
        sys.exit(1)
    jabber_cert_content = generateJabberCert(files_content.server_cert, files_content.server_key, certData)
    if not jabber_cert_content:
        log_error("Failed to generate certificate for Jabberd")
        sys.exit(1)

    deployApache(apache_cert_content, files_content.server_key)
    deployPg(files_content.server_key)
    deployJabberd(jabber_cert_content)
    deployCAUyuni(certData)
    deployCAInDB(certData)


def main():
    """main routine wrapper (exception handler)

    1  general error
    """

    def writeError(e):
        log_error("\nERROR: %s\n" % e)
        log(traceback.format_exc(None), 1)

    ret = 0
    try:
        ret = _main() or 0
    except Exception as e:
        writeError(e)
        ret = 1

    return ret


# -------------------------------------------------------------------------------
if __name__ == "__main__":
    sys.stderr.write(
        "\nWARNING: intended to be wrapped by another executable\n"
        "           calling program.\n"
    )
    sys.exit(abs(main() or 0))
# ===============================================================================
