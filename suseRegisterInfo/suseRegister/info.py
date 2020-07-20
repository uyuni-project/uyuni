# -*- coding: utf-8 -*-
#
# Copyright (c) 2010-2014 Novell
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

import os
import tempfile
import time
import xml.dom.minidom
from xml.dom import Node
from subprocess import Popen, PIPE
try:
    import uuid
except ImportError:
    uuid = None

def getInstalledProducts():
    ret = []
    try:
        ret = getSUSEInstalledProducts()
    except:
        ret = getRedHatLikeProducts()
    return ret

def getRedHatLikeProducts():
    """
     1) if a RES release package (sles_es-release) is installed it is a RES.
     2) if /etc/oracle-release exists it is a OracleLinux
     3) is it a centos system? check if /etc/centos-release file exists
     4) finally we can say it is a original RHEL (maybe:-)
    """
    ret = None
    if packageExistsWhatprovides("sles_es-release-server"):
        ret = parseReleaseInfo()
        if ret:
            ret['name'] = "RES"
    elif os.path.exists('/etc/oracle-release'):
        ret = parseReleaseInfo(release='/etc/oracle-release')
        if ret:
            ret['name'] = "OracleLinux"
    elif os.path.exists('/etc/centos-release'):
        ret = parseReleaseInfo(release='/etc/centos-release')
        if ret:
            ret['name'] = "CentOS"
    else:
        ret = parseReleaseInfo()
        if ret:
            ret['name'] = "rhel-base"
    if not ret:
        raise Exception("Getting installed products failed")

    result = []
    result.append(ret)
    return result

def packageExistsWhatprovides(pkg):
    r = Popen(['rpm', '-q', '--whatprovides', pkg])
    r.communicate()
    return r.returncode == 0

def parseReleaseInfo(release='/etc/redhat-release'):
    if not os.path.exists(release):
        return None

    try:
        name = Popen(['/usr/lib/suseRegister/bin/parse_release_info', '-si', '--file', release], stdout=PIPE).communicate()[0]
        version = Popen(['/usr/lib/suseRegister/bin/parse_release_info', '-sr', '--file', release], stdout=PIPE).communicate()[0]
        release = Popen(['/usr/lib/suseRegister/bin/parse_release_info', '-sc', '--file', release], stdout=PIPE).communicate()[0]
        arch = Popen(['uname', '-m'], stdout=PIPE).communicate()[0]
    except:
        return None

    if '.' in version:
        version = version[:version.find('.')]
    p = {
        'name' : name,
        'version' : version.strip(),
        'release' : release.strip(),
        'arch' : arch.strip(),
        'baseproduct' : 'Y' }
    return p


def getSUSEInstalledProducts():
    """Return information about the installed products on a SUSE system

       return a list of products
    """
    my_env = os.environ
    my_env["ZYPP_READONLY_HACK"] = "1"
    productProfileFile = ""
    try:
        productProfile = Popen(['zypper', '-x', '--no-refresh', '--quiet', '--non-interactive', 'products', '--installed-only'],
                                          stdout=PIPE, env=my_env).communicate()[0]
    except:
        raise Exception("Getting installed products failed")

    dom = xml.dom.minidom.parseString(productProfile)
    products = dom.getElementsByTagName("product")
    ret = []
    for product in products:
        p = {
            'name' : product.getAttribute('name'),
            'version' : product.getAttribute('version'),
            'release' : product.getAttribute('release'),
            'arch' : product.getAttribute('arch'),
            'baseproduct' : 'N' }
        if product.getAttribute('isbase') in ("1", "true", "yes"):
            p['baseproduct'] = 'Y'
        ret.append(p)
    return ret

def getOsTarget():
    ostarget= ""
    try:
        ostarget = getSUSEOsTarget()
    except:
        try:
            ostarget = Popen(['uname', '-m'], stdout=PIPE).communicate()[0]
        except:
            raise Exception("Getting ostarget failed")
    return ostarget.strip()

def getSUSEOsTarget():
    """Returns the ostarget string"""
    ostarget = ""
    try:
        ostarget = Popen(['zypper', 'targetos'], stdout=PIPE).communicate()[0]
    except:
        raise Exception("Getting ostarget failed")

    return ostarget.strip()

def parseSystemID(systemidFile):
    guid = ""
    secret = ""
    f = open(systemidFile, 'r')
    for line in f:
        if line[0] == "#":
            continue
        if line.startswith("username="):
            guid = line[9:]
        elif line.startswith("password="):
            secret = line[9:]
    f.close()
    return { 'guid' : guid.strip(), 'secret' : secret.strip() }

def getSystemID():

    sccCredentialsFile = '/etc/zypp/credentials.d/SCCcredentials'
    nccCredentialsFile = '/etc/zypp/credentials.d/NCCcredentials'
    nccCredentialsFileRH = '/etc/NCCcredentials'
    zmdDeviceFile = '/etc/zmd/deviceid'
    zmdSecretFile = '/etc/zmd/secret'

    if os.path.exists(sccCredentialsFile):
        return parseSystemID(sccCredentialsFile)
    elif os.path.exists(nccCredentialsFile):
        return parseSystemID(nccCredentialsFile)
    elif os.path.exists(nccCredentialsFileRH):
        return parseSystemID(nccCredentialsFileRH)
    elif os.path.exists(zmdDeviceFile) and os.path.exists(zmdSecretFile):
        f = open(zmdDeviceFile, 'r')
        guid = f.readline()
        guid = guid.strip()
        f.close()
        f = open(zmdSecretFile, 'r')
        secret = f.readline()
        secret = secret.strip()
        f.close()
    elif uuid is None:
        guid = Popen(['uuidgen'], stdout=PIPE).communicate()[0]
        guid = guid.replace('-', '').strip()

        time.sleep(1)
        secret = Popen(['uuidgen'], stdout=PIPE).communicate()[0]
        secret = guid.replace('-', '').strip()
    else:
        guid = str(uuid.uuid4())
        guid = guid.replace('-', '').strip()

        time.sleep(1)
        secret = str(uuid.uuid4())
        secret = secret.replace('-', '').strip()

    if os.path.exists('/etc/zypp/'):
        if not os.path.exists('/etc/zypp/credentials.d'):
            os.makedirs('/etc/zypp/credentials.d')
        f = open(nccCredentialsFile, 'w')
    else:
        f = open(nccCredentialsFileRH, 'w')
    f.write("username=%s\npassword=%s\n" % (guid, secret))
    f.close()
    return { 'guid' : guid, 'secret' : secret }

def getProductProfile():
    """ Return information about the installed Products"""
    ret = getSystemID()
    ret['ostarget'] = getOsTarget()
    ret['products'] = getInstalledProducts()
    return ret

