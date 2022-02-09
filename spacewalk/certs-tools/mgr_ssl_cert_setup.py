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
from datetime import datetime

from spacewalk.common.rhnLog import initLOG, log_time, log_clean
from uyuni.common.fileutils import getUidGid

LOGFILE = "/var/log/rhn/mgr-ssl-cert-setup.log"
PKI_DIR = "/etc/pki/"
SRV_CERT_NAME = "spacewalk.pem"
SRV_KEY_NAME = "spacewalk.key"

APACHE_CRT_NAME = "spacewalk.crt"
APACHE_CRT_FILE = os.path.join(PKI_DIR, "tls", "certs", APACHE_CRT_NAME)
APACHE_KEY_FILE = os.path.join(PKI_DIR, "tls", "private", SRV_KEY_NAME)
PG_KEY_FILE = os.path.join(PKI_DIR, "tls", "private", "pg-" + SRV_KEY_NAME)

JABBER_CRT_NAME = "server.pem"
JABBER_CRT_FILE = os.path.join(PKI_DIR, "spacewalk", "jabberd", JABBER_CRT_NAME)

ROOT_CA_NAME = "RHN-ORG-TRUSTED-SSL-CERT"

ROOT_CA_HTTP_DIR = "/srv/www/htdocs/pub/"
if not os.path.exists(ROOT_CA_HTTP_DIR):
    # Red Hat
    ROOT_CA_HTTP_DIR = "/var/www/html/pub/"

CA_TRUST_DIR = os.path.join(PKI_DIR, "trust", "anchors")
if not os.path.exists(CA_TRUST_DIR):
    # Red Hat
    CA_TRUST_DIR = os.path.join(PKI_DIR, "ca-trust", "anchors")


# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


class CertCheckError(Exception):
    pass


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


def prepareWorkdir(root_ca_file, server_cert_file, server_key_file, intermediate_ca_file, workdir):
    """
    Create a tempdir and put all CAs as single files into it.
    Also add the server certficate to it.
    Create a result dict with all certificates and pre-parsed data
    with the subject_hash as key.
    """
    ret = dict()

    allCAs = [root_ca_file]
    allCAs.extend(intermediate_ca_file)

    isContent = False
    content = []
    for ca in allCAs:
        with open(ca, "r") as f:
            cert = ""
            for line in f:
                if not isContent and line.startswith("-----BEGIN"):
                    isContent = True
                    cert = ""
                if isContent:
                    cert += line
                if isContent and line.startswith("-----END"):
                    content.append(cert)
                    isContent = False

    counter = 0
    for cert in content:
        counter += 1
        cafile = os.path.join(workdir, "cert{0}.pem".format(counter))
        with open(cafile, "w") as icert:
            icert.write(cert)
        data = getCertData(cafile)
        data["file"] = cafile
        shash = data["subject_hash"]
        if shash:
            ret[shash] = data
    # copy server cert and key as well
    shutil.copy(server_key_file, os.path.join(workdir, SRV_KEY_NAME))
    shutil.copy(server_cert_file, os.path.join(workdir, SRV_CERT_NAME))
    data = getCertData(os.path.join(workdir, SRV_CERT_NAME))
    shash = data["subject_hash"]
    if shash:
        ret[shash] = data

    return ret


def isCA(cert):
    out = subprocess.run(
        ["openssl", "x509", "-noout", "-ext", "basicConstraints", "-in", cert],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
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
    now = datetime.now()
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
            "-in",
            cert,
        ],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if out.returncode:
        log_error(
            "Unable to parse the certificate: {}".format(out.stderr.decode("utf-8"))
        )
        return None
    for line in out.stdout.decode("utf-8").splitlines():
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
        elif "subject_hash" not in data:
            data["subject_hash"] = line.strip()
        else:
            data["issuer_hash"] = line.strip()
    data["isca"] = isCA(cert)
    data["file"] = cert
    if data["subject"] == data["issuer"]:
        data["root"] = True
    else:
        data["root"] = False

    return data


def getCertWithText(cert):
    out = subprocess.run(
        ["openssl", "x509", "-text", "-in", cert],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if out.returncode:
        log_error("Invalid Certificate: {}".format(out.stderr.decode("utf-8")))
        return None
    return out.stdout.decode("utf-8")


def getRsaKey(key):
    # set an invalid password to prevent asking in case of an encrypted one
    out = subprocess.run(
        ["openssl", "rsa", "-passin", "pass:invalid", "-in", key],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if out.returncode:
        log_error("Invalid RSA Key: {}".format(out.stderr.decode("utf-8")))
        return None
    return out.stdout.decode("utf-8")


def checkKeyBelongToCert(key, cert):
    out = subprocess.run(
        ["openssl", "rsa", "-noout", "-modulus", "-in", key],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if out.returncode:
        log_error("Invalid RSA Key: {}".format(out.stderr.decode("utf-8")))
        raise CertCheckError("Invalid Key")
    keyModulus = out.stdout.decode("utf-8")
    out = subprocess.run(
        ["openssl", "x509", "-noout", "-modulus", "-in", cert],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if out.returncode:
        log_error("Invalid Cert file: {}".format(out.stderr.decode("utf-8")))
        raise CertCheckError("Invalid Certificate")

    certModulus = out.stdout.decode("utf-8")
    if keyModulus != certModulus:
        log_error("The provided key does not belong to the server certificate")
        raise CertCheckError("Key does not belong to Certificate")


def checkCompleteCAChain(workdir, certData):
    foundRootCA = False
    if len(certData.keys()) == 0:
        raise CertCheckError("No CAs found")

    serverCertHash = None
    for h, data in certData.items():
        if data["file"] == os.path.join(workdir, SRV_CERT_NAME):
            serverCertHash = h
            break

    if certData[serverCertHash]["isca"]:
        raise CertCheckError("Server Certificate must not be a CA")

    subject = certData[serverCertHash]["subject"]
    ihash = certData[serverCertHash]["issuer_hash"]
    if not ihash or ihash not in certData:
        raise CertCheckError("No CA found for server certificate")

    cert = getCertWithText(certData[serverCertHash]["file"])
    if not cert:
        raise CertCheckError("Unable to parse the server certificate")

    isValid(
        certData[serverCertHash]["startdate"],
        certData[serverCertHash]["enddate"],
        certData[serverCertHash]["subject"],
    )

    while ihash in certData:
        if not certData[ihash]["isca"]:
            raise CertCheckError("CA missing basic constraints extension")

        subject = certData[ihash]["subject"]
        nexthash = certData[ihash]["issuer_hash"]
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


def generateJabberCert(workdir, certData):
    certWithChain = generateCertWithChainFile(
        os.path.join(workdir, SRV_CERT_NAME), certData
    )
    with open(os.path.join(workdir, JABBER_CRT_NAME), "w") as out:
        key = getRsaKey(os.path.join(workdir, SRV_KEY_NAME))
        if not key:
            return False
        out.write(certWithChain)
        out.write(key)
    os.chmod(os.path.join(workdir, JABBER_CRT_NAME), int("0600", 8))
    return True


def generateApacheCert(workdir, certData):
    certWithChain = generateCertWithChainFile(
        os.path.join(workdir, SRV_CERT_NAME), certData
    )
    with open(os.path.join(workdir, APACHE_CRT_NAME), "w") as out:
        out.write(certWithChain)
    return True


def generateCertWithChainFile(serverCert, certData):
    retContent = ""

    if len(certData.keys()) == 0:
        log_error("No CA found in Hash")
        return ""

    serverCertHash = None
    for h, data in certData.items():
        if data["file"] == serverCert:
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
        cert = getCertWithText(certData[ihash]["file"])
        if not cert:
            return ""
        if nexthash == ihash:
            # Found Root CA, we can exit
            break
        ihash = nexthash
        retContent += cert
    return retContent


def deployApache(workdir):
    if os.path.exists(APACHE_KEY_FILE):
        os.remove(APACHE_KEY_FILE)
    if os.path.exists(APACHE_CRT_FILE):
        os.remove(APACHE_CRT_FILE)
    shutil.copy(os.path.join(workdir, SRV_KEY_NAME), APACHE_KEY_FILE)
    if os.path.exists(os.path.join(workdir, APACHE_CRT_NAME)):
        shutil.copy(os.path.join(workdir, APACHE_CRT_NAME), APACHE_CRT_FILE)
    # exists on server and proxy
    os.system("/usr/bin/spacewalk-setup-httpd")


def deployJabberd(workdir):
    j_uid, j_gid = getUidGid("jabber", "jabber")
    if j_uid and j_gid:
        if os.path.exists(JABBER_CRT_FILE):
            os.remove(JABBER_CRT_FILE)
        if os.path.exists(os.path.join(workdir, JABBER_CRT_NAME)):
            shutil.copy(os.path.join(workdir, JABBER_CRT_NAME), JABBER_CRT_FILE)
            os.chmod(JABBER_CRT_FILE, int("0600", 8))
            os.chown(JABBER_CRT_FILE, j_uid, j_gid)
        else:
            log_error("Certificate for Jabberd not found")
            sys.exit(1)


def deployPg(workdir):
    pg_uid, pg_gid = getUidGid("postgres", "postgres")
    if pg_uid and pg_gid:
        # deploy only the key with different permissions
        # the certificate is the same as for apache
        if os.path.exists(PG_KEY_FILE):
            os.remove(PG_KEY_FILE)
        shutil.copy(os.path.join(workdir, SRV_KEY_NAME), PG_KEY_FILE)
        os.chmod(PG_KEY_FILE, int("0600", 8))
        os.chown(PG_KEY_FILE, pg_uid, pg_gid)


def deployCAUyuni(certData):
    for h, ca in certData.items():
        if ca["root"]:
            if os.path.exists(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME)):
                os.remove(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME))
            if os.path.exists(os.path.join(CA_TRUST_DIR, ROOT_CA_NAME)):
                os.remove(os.path.join(CA_TRUST_DIR, ROOT_CA_NAME))
            shutil.copy(ca["file"], os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME))
            os.chmod(os.path.join(ROOT_CA_HTTP_DIR, ROOT_CA_NAME), int("0644", 8))
            # TODO: or symlink?
            shutil.copy(ca["file"], os.path.join(CA_TRUST_DIR, ROOT_CA_NAME))
            os.chmod(os.path.join(CA_TRUST_DIR, ROOT_CA_NAME), int("0644", 8))
            break
    # in case a systemd timer try to do the same
    time.sleep(3)
    if os.path.exists("/usr/sbin/update-ca-certificates"):
        os.system("/usr/sbin/update-ca-certificates")
    else:
        os.system("update-ca-trust extract")


def checks(workdir, certData):
    """
    Perform different checks on the input data
    """
    if not getRsaKey(os.path.join(workdir, SRV_KEY_NAME)):
        raise CertCheckError("Unable to read the server key. Encrypted?")

    checkKeyBelongToCert(
        os.path.join(workdir, SRV_KEY_NAME), os.path.join(workdir, SRV_CERT_NAME)
    )

    checkCompleteCAChain(workdir, certData)


def _main():
    """main routine"""

    options = processCommandline()
    checkOptions(options.root_ca_file, options.server_cert_file, options.server_key_file, options.intermediate_ca_file)

    with tempfile.TemporaryDirectory() as workdir:
        certData = prepareWorkdir(
                options.root_ca_file,
                options.server_cert_file,
                options.server_key_file,
                options.intermediate_ca_file,
                workdir)
        checks(workdir, certData)
        ret = generateApacheCert(workdir, certData)
        if not ret:
            sys.exit(1)
        ret = generateJabberCert(workdir, certData)
        if not ret:
            sys.exit(1)

        deployApache(workdir)
        deployPg(workdir)
        deployJabberd(workdir)
        deployCAUyuni(certData)


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
