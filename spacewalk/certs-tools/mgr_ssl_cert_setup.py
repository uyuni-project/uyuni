#!/usr/bin/python3  #  pylint: disable=missing-module-docstring
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
import shutil  #  pylint: disable=unused-import
import tempfile  #  pylint: disable=unused-import
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
        "{0}: {1}.{2}({3}) - {4}".format(log_time(), frame[0], frame[2], frame[1], msg),  #  pylint: disable=consider-using-f-string
    )
    sys.stderr.write("{0}\n".format(msg))  #  pylint: disable=consider-using-f-string


def log(msg, level=0):
    frame = traceback.extract_stack()[-2]
    log_clean(
        level,
        "{0}: {1}.{2}({3}) - {4}".format(log_time(), frame[0], frame[2], frame[1], msg),  #  pylint: disable=consider-using-f-string
    )
    if level < 1:
        sys.stdout.write("{0}\n".format(msg))  #  pylint: disable=consider-using-f-string


def processCommandline():  #  pylint: disable=invalid-name
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


def checkOptions(  #  pylint: disable=invalid-name
    root_ca_file, server_cert_file, server_key_file, intermediate_ca_files
):
    if not root_ca_file:
        log_error("Root CA is required")
        sys.exit(1)
    if not os.path.exists(root_ca_file):
        log_error("Root CA: file not found {}".format(root_ca_file))  #  pylint: disable=consider-using-f-string
        sys.exit(1)

    if not server_cert_file:
        log_error("Server Certificate is required")
        sys.exit(1)
    if not os.path.exists(server_cert_file):
        log_error("Server Certificate: file not found {}".format(server_cert_file))  #  pylint: disable=consider-using-f-string
        sys.exit(1)

    if not server_key_file:
        log_error("Server Private Key is required")
        sys.exit(1)
    if not os.path.exists(server_key_file):
        log_error("Server Private Key: file not found {}".format(server_key_file))  #  pylint: disable=consider-using-f-string
        sys.exit(1)

    for ica in intermediate_ca_files:
        if not os.path.exists(ica):
            log_error("Intermediate CA: file not found {}".format(ica))  #  pylint: disable=consider-using-f-string
            sys.exit(1)


def readAllFiles(  #  pylint: disable=invalid-name
    root_ca_file, server_cert_file, server_key_file, intermediate_ca_files
):
    allFiles = [root_ca_file, server_cert_file, server_key_file]  #  pylint: disable=invalid-name
    allFiles.extend(intermediate_ca_files)

    contents = []
    for input_file in allFiles:
        with open(input_file, "r") as f:  #  pylint: disable=unspecified-encoding
            contents.append(f.read())

    return FilesContent(
        root_ca=contents[0],
        server_cert=contents[1],
        server_key=contents[2],
        intermediate_cas=contents[3:],
    )


def prepareData(root_ca_content, server_cert_content, intermediate_ca_content):  #  pylint: disable=invalid-name
    """
    Create a result dict with all certificates and pre-parsed data
    with the subject_hash as key.
    """
    ret = dict()

    allCAs = [root_ca_content]  #  pylint: disable=invalid-name
    allCAs.extend(intermediate_ca_content)

    isContent = False  #  pylint: disable=invalid-name
    content = []
    for ca in allCAs:
        cert = ""
        for line in ca.splitlines(keepends=True):
            if not isContent and line.startswith("-----BEGIN"):
                isContent = True  #  pylint: disable=invalid-name
                cert = ""
            if isContent:
                cert += line
            if isContent and line.startswith("-----END"):
                content.append(cert)
                isContent = False  #  pylint: disable=invalid-name

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


def isCA(cert):  #  pylint: disable=invalid-name
    out = subprocess.run(  #  pylint: disable=subprocess-run-check
        ["openssl", "x509", "-noout", "-ext", "basicConstraints"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=cert.encode("utf-8"),
    )
    if out.returncode:
        log_error(
            "Unable to parse the certificate: {}".format(out.stderr.decode("utf-8"))  #  pylint: disable=consider-using-f-string
        )
        return False
    for line in out.stdout.decode("utf-8").splitlines():
        if "CA:TRUE" in line.upper():
            return True
    return False


def isValid(startdate, enddate, subject):  #  pylint: disable=invalid-name
    #  Not Before: Nov 12 14:36:13 2021 GMT
    #  Not After : Sep  1 14:36:13 2024 GMT

    start = datetime.strptime(startdate, "%b %d %H:%M:%S %Y %Z")
    end = datetime.strptime(enddate, "%b %d %H:%M:%S %Y %Z")
    now = datetime.utcnow()
    if now < start:
        raise CertCheckError("Certificate '{}' not yet valid".format(subject))  #  pylint: disable=consider-using-f-string
    if now > end:
        raise CertCheckError("Certificate '{}' is expired".format(subject))  #  pylint: disable=consider-using-f-string


def getCertData(cert):  #  pylint: disable=invalid-name
    data = dict()
    out = subprocess.run(  #  pylint: disable=subprocess-run-check
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
            "Unable to parse the certificate: {}".format(out.stderr.decode("utf-8"))  #  pylint: disable=consider-using-f-string
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


def getCertWithText(cert):  #  pylint: disable=invalid-name
    out = subprocess.run(  #  pylint: disable=subprocess-run-check
        ["openssl", "x509", "-text"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=cert.encode("utf-8"),
    )
    if out.returncode:
        log_error("Invalid Certificate: {}".format(out.stderr.decode("utf-8")))  #  pylint: disable=consider-using-f-string
        return None
    return out.stdout.decode("utf-8")


def getPrivateKey(key):  #  pylint: disable=invalid-name
    # set an invalid password to prevent asking in case of an encrypted one
    out = subprocess.run(  #  pylint: disable=subprocess-run-check
        ["openssl", "pkey", "-passin", "pass:invalid", "-text", "-noout"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=key.encode("utf-8"),
    )

    if out.returncode:
        log_error("Invalid or encrypted Key: {}".format(out.stderr.decode("utf-8")))  #  pylint: disable=consider-using-f-string
        return None
    return out.stdout.decode("utf-8")


def checkKeyBelongToCert(key, cert):  #  pylint: disable=invalid-name
    out = subprocess.run(  #  pylint: disable=subprocess-run-check
        ["openssl", "pkey", "-pubout"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=key.encode("utf-8"),
    )
    if out.returncode:
        log_error("Invalid Key: {}".format(out.stderr.decode("utf-8")))  #  pylint: disable=consider-using-f-string
        raise CertCheckError("Invalid Key")
    keyPubKey = out.stdout.decode("utf-8")  #  pylint: disable=invalid-name
    out = subprocess.run(  #  pylint: disable=subprocess-run-check
        ["openssl", "x509", "-noout", "-pubkey"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        input=cert.encode("utf-8"),
    )
    if out.returncode:
        log_error("Invalid Cert file: {}".format(out.stderr.decode("utf-8")))  #  pylint: disable=consider-using-f-string
        raise CertCheckError("Invalid Certificate")

    certPubKey = out.stdout.decode("utf-8")  #  pylint: disable=invalid-name
    if keyPubKey != certPubKey:
        log_error("The provided key does not belong to the server certificate")
        log("{} vs. {}".format(keyPubKey, certPubKey), 1)  #  pylint: disable=consider-using-f-string
        raise CertCheckError("Key does not belong to Certificate")


def checkCompleteCAChain(server_cert_content, certData):  #  pylint: disable=invalid-name,invalid-name
    foundRootCA = False  #  pylint: disable=invalid-name
    if len(certData.keys()) == 0:
        raise CertCheckError("No CAs found")

    serverCertHash = None  #  pylint: disable=invalid-name
    for h, data in certData.items():
        if data["content"] == server_cert_content:
            serverCertHash = h  #  pylint: disable=invalid-name
            break

    if certData[serverCertHash]["isca"]:
        raise CertCheckError("Server Certificate must not be a CA")

    subject = certData[serverCertHash]["subject"]
    ihash = certData[serverCertHash]["issuer_hash"]
    issuerKeyId = certData[serverCertHash]["authorityKeyIdentifier"]  #  pylint: disable=invalid-name

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
        keyId = certData[ihash]["subjectKeyIdentifier"]  #  pylint: disable=invalid-name
        if not (keyId and issuerKeyId and keyId == issuerKeyId):
            raise CertCheckError(
                "Incomplete CA Chain. Key Identifiers do not match. Unable to find issuer of '{}'".format(  #  pylint: disable=line-too-long,consider-using-f-string
                    subject
                )
            )
        if not certData[ihash]["isca"]:
            raise CertCheckError("CA missing basic constraints extension")

        subject = certData[ihash]["subject"]
        nexthash = certData[ihash]["issuer_hash"]
        issuerKeyId = certData[ihash]["authorityKeyIdentifier"]  #  pylint: disable=invalid-name
        isValid(certData[ihash]["startdate"], certData[ihash]["enddate"], subject)

        if nexthash == ihash:
            # Found Root CA, we can exit
            foundRootCA = True  #  pylint: disable=invalid-name
            if not certData[ihash]["root"]:
                raise CertCheckError("Root CA has different issuer")
            break
        else:
            if certData[ihash]["root"]:
                raise CertCheckError("Intermediate CA has subject equals to issuer")

        ihash = nexthash

    if not foundRootCA:
        raise CertCheckError(
            "Incomplete CA Chain. Unable to find issuer of '{}'".format(subject)  #  pylint: disable=consider-using-f-string
        )


def generateApacheCert(server_cert_content, certData):  #  pylint: disable=invalid-name,invalid-name
    return generateCertWithChainFile(server_cert_content, certData)


def generateCertWithChainFile(serverCert, certData):  #  pylint: disable=invalid-name,invalid-name,invalid-name
    retContent = ""  #  pylint: disable=invalid-name

    if len(certData.keys()) == 0:
        log_error("No CA found in Hash")
        return ""

    serverCertHash = None  #  pylint: disable=invalid-name
    for h, data in certData.items():
        if data["content"] == serverCert:
            serverCertHash = h  #  pylint: disable=invalid-name
            break

    ihash = certData[serverCertHash]["issuer_hash"]
    if not ihash or ihash not in certData:
        log_error("No CA found for server certificate")
        return ""
    cert = getCertWithText(serverCert)
    if not cert:
        log_error("Unable to get the server certificate")
        return ""
    retContent += cert  #  pylint: disable=invalid-name
    while ihash in certData:
        nexthash = certData[ihash]["issuer_hash"]
        cert = getCertWithText(certData[ihash]["content"])
        if not cert:
            return ""
        if nexthash == ihash:
            # Found Root CA, we can exit
            break
        ihash = nexthash
        retContent += cert  #  pylint: disable=invalid-name
    return retContent


def deployApache(apache_cert_content, server_key_content):  #  pylint: disable=invalid-name
    if os.path.exists(APACHE_KEY_FILE):
        os.remove(APACHE_KEY_FILE)
    if os.path.exists(APACHE_CRT_FILE):
        os.remove(APACHE_CRT_FILE)
    with open(APACHE_KEY_FILE, "w") as f:  #  pylint: disable=unspecified-encoding
        f.write(server_key_content)
    os.chmod(APACHE_KEY_FILE, int("0600", 8))
    with open(APACHE_CRT_FILE, "w") as f:  #  pylint: disable=unspecified-encoding
        f.write(apache_cert_content)
    # exists on server and proxy
    os.system("/usr/bin/spacewalk-setup-httpd")
    log(
        """After changing the server certificate please execute:
$> spacewalk-service stop """
    )


def deployPg(server_key_content):  #  pylint: disable=invalid-name
    pg_uid, pg_gid = getUidGid("postgres", "postgres")
    if pg_uid and pg_gid:
        # deploy only the key with different permissions
        # the certificate is the same as for apache
        if os.path.exists(PG_KEY_FILE):
            os.remove(PG_KEY_FILE)
        with open(PG_KEY_FILE, "w") as f:  #  pylint: disable=unspecified-encoding
            f.write(server_key_content)
        os.chmod(PG_KEY_FILE, int("0600", 8))
        os.chown(PG_KEY_FILE, pg_uid, pg_gid)

        log("""$> systemctl restart postgresql.service """)


def deployCAInDB(certData):  #  pylint: disable=invalid-name,invalid-name
    if not os.path.exists("/usr/bin/rhn-ssl-dbstore"):
        # not a Uyuni Server - skip deploying into DB
        return

    for h, ca in certData.items():  #  pylint: disable=unused-variable
        if ca["root"]:
            out = subprocess.run(  #  pylint: disable=subprocess-run-check
                ["/usr/bin/rhn-ssl-dbstore", "--ca-cert", "-"],
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                input=ca["content"].encode("utf-8"),
            )
            if out.returncode:
                log_error(
                    "Failed to upload CA Certificate to DB: {}".format(  #  pylint: disable=consider-using-f-string
                        out.stderr.decode("utf-8")
                    )
                )
                raise OSError("Failed to upload CA Certificate to DB")
            break


def deployCAUyuni(certData):  #  pylint: disable=invalid-name,invalid-name
    for h, ca in certData.items():  #  pylint: disable=unused-variable
        if ca["root"]:
            if os.path.exists(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME)):
                os.remove(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME))
            with open(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME), "w") as f:  #  pylint: disable=unspecified-encoding
                f.write(ca["content"])
            os.chmod(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME), int("0644", 8))

            if os.path.exists(os.path.join(CA_TRUST_DIR, PKI_ROOT_CA_NAME)):
                os.remove(os.path.join(CA_TRUST_DIR, PKI_ROOT_CA_NAME))
            with open(os.path.join(CA_TRUST_DIR, PKI_ROOT_CA_NAME), "w") as f:  #  pylint: disable=unspecified-encoding
                f.write(ca["content"])
            os.chmod(os.path.join(CA_TRUST_DIR, PKI_ROOT_CA_NAME), int("0644", 8))
            break
    # in case a systemd timer try to do the same
    time.sleep(3)
    os.system("/usr/share/rhn/certs/update-ca-cert-trust.sh")
    log(
        """$> spacewalk-service start

As the CA certificate has been changed, please deploy the CA to all registered clients.
On salt-managed clients, you can do this by applying the highstate."""
    )


def checks(server_key_content, server_cert_content, certData):  #  pylint: disable=invalid-name
    """
    Perform different checks on the input data
    """
    if not getPrivateKey(server_key_content):
        raise CertCheckError("Unable to read the server key. Is it maybe encrypted?")

    checkKeyBelongToCert(server_key_content, server_cert_content)

    checkCompleteCAChain(server_cert_content, certData)


def getContainersSetup(  #  pylint: disable=invalid-name
    root_ca_content, intermediate_ca_content, server_cert_content, server_key_content
):
    if not root_ca_content:
        raise CertCheckError("Root CA is required")
    if not server_cert_content:
        raise CertCheckError("Server Certificate is required")
    if not server_key_content:
        raise CertCheckError("Server Private Key is required")

    certData = prepareData(  #  pylint: disable=invalid-name
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

    certData = prepareData(  #  pylint: disable=invalid-name
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

    def writeError(e):  #  pylint: disable=invalid-name
        log_error("\nERROR: %s\n" % e)  #  pylint: disable=consider-using-f-string
        log(traceback.format_exc(None), 1)

    ret = 0
    try:
        ret = _main() or 0
    except Exception as e:  #  pylint: disable=broad-exception-caught
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
