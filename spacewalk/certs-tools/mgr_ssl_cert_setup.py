#!/usr/bin/python3
#  pylint: disable=missing-module-docstring
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
import re

# pylint: disable-next=unused-import
import shutil

# pylint: disable-next=unused-import
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


FilesContent = namedtuple(
    "FilesContent", ["root_ca", "server_cert", "server_key", "intermediate_cas"]
)


def log_error(msg):
    frame = traceback.extract_stack()[-2]
    log_clean(
        0,
        # pylint: disable-next=consider-using-f-string
        "{0}: {1}.{2}({3}) - {4}".format(log_time(), frame[0], frame[2], frame[1], msg),
    )
    # pylint: disable-next=consider-using-f-string
    sys.stderr.write("{0}\n".format(msg))


def log(msg, level=0):
    frame = traceback.extract_stack()[-2]
    log_clean(
        level,
        # pylint: disable-next=consider-using-f-string
        "{0}: {1}.{2}({3}) - {4}".format(log_time(), frame[0], frame[2], frame[1], msg),
    )
    if level < 1:
        # pylint: disable-next=consider-using-f-string
        sys.stdout.write("{0}\n".format(msg))


# pylint: disable-next=invalid-name
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


# pylint: disable-next=invalid-name
def checkOptions(
    root_ca_file, server_cert_file, server_key_file, intermediate_ca_files
):
    if not root_ca_file:
        log_error("Root CA is required")
        sys.exit(1)
    if not os.path.exists(root_ca_file):
        # pylint: disable-next=consider-using-f-string
        log_error("Root CA: file not found {}".format(root_ca_file))
        sys.exit(1)

    if not server_cert_file:
        log_error("Server Certificate is required")
        sys.exit(1)
    if not os.path.exists(server_cert_file):
        # pylint: disable-next=consider-using-f-string
        log_error("Server Certificate: file not found {}".format(server_cert_file))
        sys.exit(1)

    if not server_key_file:
        log_error("Server Private Key is required")
        sys.exit(1)
    if not os.path.exists(server_key_file):
        # pylint: disable-next=consider-using-f-string
        log_error("Server Private Key: file not found {}".format(server_key_file))
        sys.exit(1)

    for ica in intermediate_ca_files:
        if not os.path.exists(ica):
            # pylint: disable-next=consider-using-f-string
            log_error("Intermediate CA: file not found {}".format(ica))
            sys.exit(1)


# pylint: disable-next=invalid-name
def readAllFiles(
    root_ca_file, server_cert_file, server_key_file, intermediate_ca_files
):

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


# pylint: disable-next=invalid-name
def readSplitCertificates(certfile):

    # pylint: disable-next=invalid-name
    isContent = False
    cert_list = []
    cert = ""
    with open(certfile, "r", encoding="utf-8") as f:
        certs_content = f.read()
        for line in certs_content.splitlines(keepends=True):
            if not isContent and line.startswith("-----BEGIN"):
                # pylint: disable-next=invalid-name
                isContent = True
                cert = ""
            if isContent:
                cert += line
            if isContent and line.startswith("-----END"):
                cert_list.append(cert)
                # pylint: disable-next=invalid-name
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


# pylint: disable-next=invalid-name
def isCA(cert):
    # pylint: disable-next=subprocess-run-check
    out = subprocess.run(
        ["openssl", "x509", "-noout", "-ext", "basicConstraints"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=cert.encode("utf-8"),
    )
    if out.returncode:
        log_error(
            # pylint: disable-next=consider-using-f-string
            "Unable to parse the certificate: {}".format(out.stderr.decode("utf-8"))
        )
        return False
    for line in out.stdout.decode("utf-8").splitlines():
        if "CA:TRUE" in line.upper():
            return True
    return False


# pylint: disable-next=invalid-name
def isValid(startdate, enddate, subject):
    #  Not Before: Nov 12 14:36:13 2021 GMT
    #  Not After : Sep  1 14:36:13 2024 GMT

    start = datetime.strptime(startdate, "%b %d %H:%M:%S %Y %Z")
    end = datetime.strptime(enddate, "%b %d %H:%M:%S %Y %Z")
    now = datetime.utcnow()
    if now < start:
        # pylint: disable-next=consider-using-f-string
        raise CertCheckError("Certificate '{}' not yet valid".format(subject))
    if now > end:
        # pylint: disable-next=consider-using-f-string
        raise CertCheckError("Certificate '{}' is expired".format(subject))


# pylint: disable-next=invalid-name
def getCertData(cert):
    data = dict()
    # pylint: disable-next=subprocess-run-check
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
            "-ext",
            "subjectKeyIdentifier,authorityKeyIdentifier",
        ],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=cert.encode("utf-8"),
    )
    if out.returncode:
        log_error(
            # pylint: disable-next=consider-using-f-string
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
            elif nextval == "authorityKeyIdentifier" and re.match(
                r"^\s+[0-9A-Fa-f]{2}:.+$", line
            ):
                data["authorityKeyIdentifier"] = line.strip().upper()
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


# pylint: disable-next=invalid-name
def getCertWithText(cert):
    # pylint: disable-next=subprocess-run-check
    out = subprocess.run(
        ["openssl", "x509", "-text"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=cert.encode("utf-8"),
    )
    if out.returncode:
        # pylint: disable-next=consider-using-f-string
        log_error("Invalid Certificate: {}".format(out.stderr.decode("utf-8")))
        return None
    return out.stdout.decode("utf-8")


# pylint: disable-next=invalid-name
def getPrivateKey(key):
    # set an invalid password to prevent asking in case of an encrypted one
    # pylint: disable-next=subprocess-run-check
    out = subprocess.run(
        ["openssl", "pkey", "-passin", "pass:invalid", "-text", "-noout"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=key.encode("utf-8"),
    )

    if out.returncode:
        # pylint: disable-next=consider-using-f-string
        log_error("Invalid or encrypted Key: {}".format(out.stderr.decode("utf-8")))
        return None
    return out.stdout.decode("utf-8")


# pylint: disable-next=invalid-name
def checkKeyBelongToCert(key, cert):
    # pylint: disable-next=subprocess-run-check
    out = subprocess.run(
        ["openssl", "pkey", "-pubout"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=key.encode("utf-8"),
    )
    if out.returncode:
        # pylint: disable-next=consider-using-f-string
        log_error("Invalid Key: {}".format(out.stderr.decode("utf-8")))
        raise CertCheckError("Invalid Key")
    # pylint: disable-next=invalid-name
    keyPubKey = out.stdout.decode("utf-8")
    # pylint: disable-next=subprocess-run-check
    out = subprocess.run(
        ["openssl", "x509", "-noout", "-pubkey"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=cert.encode("utf-8"),
    )
    if out.returncode:
        # pylint: disable-next=consider-using-f-string
        log_error("Invalid Cert file: {}".format(out.stderr.decode("utf-8")))
        raise CertCheckError("Invalid Certificate")

    # pylint: disable-next=invalid-name
    certPubKey = out.stdout.decode("utf-8")
    if keyPubKey != certPubKey:
        log_error("The provided key does not belong to the server certificate")
        # pylint: disable-next=consider-using-f-string
        log("{} vs. {}".format(keyPubKey, certPubKey), 1)
        raise CertCheckError("Key does not belong to Certificate")


# pylint: disable-next=invalid-name
def checkCompleteCAChain(server_cert_content, certData):
    # pylint: disable-next=invalid-name
    foundRootCA = False
    if len(certData.keys()) == 0:
        raise CertCheckError("No CAs found")

    # pylint: disable-next=invalid-name
    serverCertHash = None
    for h, data in certData.items():
        if data["content"] == server_cert_content:
            # pylint: disable-next=invalid-name
            serverCertHash = h
            break

    if certData[serverCertHash]["isca"]:
        raise CertCheckError("Server Certificate must not be a CA")

    subject = certData[serverCertHash]["subject"]
    ihash = certData[serverCertHash]["issuer_hash"]
    # pylint: disable-next=invalid-name
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
        # pylint: disable-next=invalid-name
        keyId = certData[ihash]["subjectKeyIdentifier"]
        if not (keyId and issuerKeyId and keyId == issuerKeyId):
            raise CertCheckError(
                # pylint: disable-next=consider-using-f-string
                "Incomplete CA Chain. Key Identifiers do not match. Unable to find issuer of '{}'".format(
                    subject
                )
            )
        if not certData[ihash]["isca"]:
            raise CertCheckError("CA missing basic constraints extension")

        subject = certData[ihash]["subject"]
        nexthash = certData[ihash]["issuer_hash"]
        # pylint: disable-next=invalid-name
        issuerKeyId = certData[ihash]["authorityKeyIdentifier"]
        isValid(certData[ihash]["startdate"], certData[ihash]["enddate"], subject)

        if nexthash == ihash:
            # Found Root CA, we can exit
            # pylint: disable-next=invalid-name
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
            # pylint: disable-next=consider-using-f-string
            "Incomplete CA Chain. Unable to find issuer of '{}'".format(subject)
        )


# pylint: disable-next=invalid-name
def generateApacheCert(server_cert_content, certData):
    return generateCertWithChainFile(server_cert_content, certData)


# pylint: disable-next=invalid-name
def generateCertWithChainFile(serverCert, certData):
    # pylint: disable-next=invalid-name
    retContent = ""

    if len(certData.keys()) == 0:
        log_error("No CA found in Hash")
        return ""

    # pylint: disable-next=invalid-name
    serverCertHash = None
    for h, data in certData.items():
        if data["content"] == serverCert:
            # pylint: disable-next=invalid-name
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
    # pylint: disable-next=invalid-name
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
        # pylint: disable-next=invalid-name
        retContent += cert
    return retContent


# pylint: disable-next=invalid-name
def deployApache(apache_cert_content, server_key_content):
    if os.path.exists(APACHE_KEY_FILE):
        os.remove(APACHE_KEY_FILE)
    if os.path.exists(APACHE_CRT_FILE):
        os.remove(APACHE_CRT_FILE)
    # pylint: disable-next=unspecified-encoding
    with open(APACHE_KEY_FILE, "w", encoding="utf-8") as f:
        f.write(server_key_content)
    os.chmod(APACHE_KEY_FILE, int("0600", 8))
    # pylint: disable-next=unspecified-encoding
    with open(APACHE_CRT_FILE, "w", encoding="utf-8") as f:
        f.write(apache_cert_content)
    # exists on server and proxy
    os.system("/usr/bin/spacewalk-setup-httpd")
    log(
        """After changing the server certificate please execute:
$> spacewalk-service stop """
    )


# pylint: disable-next=invalid-name
def deployPg(server_key_content):
    pg_uid, pg_gid = getUidGid("postgres", "postgres")
    if pg_uid and pg_gid:
        # deploy only the key with different permissions
        # the certificate is the same as for apache
        if os.path.exists(PG_KEY_FILE):
            os.remove(PG_KEY_FILE)
        # pylint: disable-next=unspecified-encoding
        with open(PG_KEY_FILE, "w", encoding="utf-8") as f:
            f.write(server_key_content)
        os.chmod(PG_KEY_FILE, int("0600", 8))
        os.chown(PG_KEY_FILE, pg_uid, pg_gid)

        log("""$> systemctl restart postgresql.service """)


# pylint: disable-next=invalid-name
def deployCAInDB(certData):
    if not os.path.exists("/usr/bin/rhn-ssl-dbstore"):
        # not a Uyuni Server - skip deploying into DB
        return

    # pylint: disable-next=unused-variable
    for h, ca in certData.items():
        if ca["root"]:
            # pylint: disable-next=subprocess-run-check
            out = subprocess.run(
                ["/usr/bin/rhn-ssl-dbstore", "--ca-cert", "-"],
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                input=ca["content"].encode("utf-8"),
            )
            if out.returncode:
                log_error(
                    # pylint: disable-next=consider-using-f-string
                    "Failed to upload CA Certificate to DB: {}".format(
                        out.stderr.decode("utf-8")
                    )
                )
                raise OSError("Failed to upload CA Certificate to DB")
            break


# pylint: disable-next=invalid-name
def deployCAUyuni(certData):
    # pylint: disable-next=unused-variable
    for h, ca in certData.items():
        if ca["root"]:
            if os.path.exists(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME)):
                os.remove(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME))
            with open(
                os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME), "w", encoding="utf-8"
            ) as f:
                f.write(ca["content"])
            os.chmod(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME), int("0644", 8))

            if os.path.exists(os.path.join(CA_TRUST_DIR, PKI_ROOT_CA_NAME)):
                os.remove(os.path.join(CA_TRUST_DIR, PKI_ROOT_CA_NAME))
            with open(
                os.path.join(CA_TRUST_DIR, PKI_ROOT_CA_NAME), "w", encoding="utf-8"
            ) as f:
                f.write(ca["content"])
            os.chmod(os.path.join(CA_TRUST_DIR, PKI_ROOT_CA_NAME), int("0644", 8))

            # SALT_CA_DIR exists only on the server, ignore on proxies
            if os.path.exists(SALT_CA_DIR):
                if os.path.exists(os.path.join(SALT_CA_DIR, ROOT_CA_NAME)):
                    os.remove(os.path.join(SALT_CA_DIR, ROOT_CA_NAME))
                with open(
                    os.path.join(SALT_CA_DIR, ROOT_CA_NAME), "w", encoding="utf-8"
                ) as f:
                    f.write(ca["content"])
                os.chmod(os.path.join(SALT_CA_DIR, ROOT_CA_NAME), int("0644", 8))
            break
    # in case a systemd timer try to do the same
    time.sleep(3)
    os.system("/usr/share/rhn/certs/update-ca-cert-trust.sh")
    log(
        """$> spacewalk-service start

As the CA certificate has been changed, please deploy the CA to all registered clients.
On salt-managed clients, you can do this by applying the highstate."""
    )


# pylint: disable-next=invalid-name
def checks(server_key_content, server_cert_content, certData):
    """
    Perform different checks on the input data
    """
    checkCompleteCAChain(server_cert_content, certData)

    if not getPrivateKey(server_key_content):
        raise CertCheckError("Unable to read the server key. Is it maybe encrypted?")

    checkKeyBelongToCert(server_key_content, server_cert_content)


# pylint: disable-next=invalid-name
def getContainersSetup(
    root_ca_content, intermediate_ca_content, server_cert_content, server_key_content
):
    if not root_ca_content:
        raise CertCheckError("Root CA is required")
    if not server_cert_content:
        raise CertCheckError("Server Certificate is required")
    if not server_key_content:
        raise CertCheckError("Server Private Key is required")

    # pylint: disable-next=invalid-name
    certData = prepareData(
        root_ca_content, server_cert_content, intermediate_ca_content
    )
    checks(server_key_content, server_cert_content, certData)
    apache_cert_content = generateApacheCert(server_cert_content, certData)
    if not apache_cert_content:
        raise CertCheckError("Failed to generate certificates")
    return apache_cert_content


def _main():
    """main routine"""

    options = processCommandline()
    checkOptions(
        options.root_ca_file,
        options.server_cert_file,
        options.server_key_file,
        options.intermediate_ca_file,
    )

    files_content = readAllFiles(
        options.root_ca_file,
        options.server_cert_file,
        options.server_key_file,
        options.intermediate_ca_file,
    )
    if options.check_only:
        getContainersSetup(
            files_content.root_ca,
            files_content.intermediate_cas,
            files_content.server_cert,
            files_content.server_key,
        )
        sys.exit(0)

    # pylint: disable-next=invalid-name
    certData = prepareData(
        files_content.root_ca, files_content.server_cert, files_content.intermediate_cas
    )
    checks(files_content.server_key, files_content.server_cert, certData)
    apache_cert_content = generateApacheCert(files_content.server_cert, certData)
    if not apache_cert_content:
        log_error("Failed to generate certificate for Apache")
        sys.exit(1)

    deployApache(apache_cert_content, files_content.server_key)
    deployPg(files_content.server_key)
    deployCAUyuni(certData)
    deployCAInDB(certData)


def main():
    """main routine wrapper (exception handler)

    1  general error
    """

    # pylint: disable-next=invalid-name
    def writeError(e):
        # pylint: disable-next=consider-using-f-string
        log_error("\nERROR: %s\n" % e)
        log(traceback.format_exc(None), 1)

    ret = 0
    try:
        ret = _main() or 0
    # pylint: disable-next=broad-exception-caught
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
