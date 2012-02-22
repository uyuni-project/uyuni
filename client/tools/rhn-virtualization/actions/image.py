#!/usr/bin/env python
import os
import sys
import time
import random
import stat
import string
import struct
import re
sys.path.append("/usr/share/rhn/")
import virtualization.support as virt_support
from virtualization.util import hyphenize_uuid

sys.path.append("/usr/share/rhn/")

from up2date_client import up2dateLog
from up2date_client import config
from up2date_client import rhnserver
from up2date_client import up2dateAuth

import hashlib
import pycurl
import base64

log = up2dateLog.initLog()
IMAGE_BASE_PATH = "/var/lib/libvirt/images/"
STUDIO_KVM_TEMPLATE = "/etc/sysconfig/rhn/studio-kvm-template.xml"
STUDIO_XEN_TEMPLATE = "/etc/sysconfig/rhn/studio-xen-template.xml"

KVM_CREATE_TEMPLATE = ""
XEN_CREATE_TEMPLATE = ""

if os.path.isfile(STUDIO_KVM_TEMPLATE):
        f = open(STUDIO_KVM_TEMPLATE, 'r')
        KVM_CREATE_TEMPLATE = f.read()
        f.close()

if os.path.isfile(STUDIO_XEN_TEMPLATE):
        f = open(STUDIO_XEN_TEMPLATE, 'r')
        XEN_CREATE_TEMPLATE = f.read()
        f.close()


# mark this module as acceptable
__rhnexport__ = [
    'deploy'
]

# download and extract tar.gz file with image
def _getImage(imageName,serverUrl,proxySetting):
    log.log_debug(serverUrl)

    # get the file via pycurl
    c = pycurl.Curl()
    c.setopt(pycurl.URL, serverUrl)

    # proxy settings
    if proxySetting["proxyServer"] != None and proxySetting["proxyServer"] != "":
        server = proxySetting["proxyServer"]
        # proxy-host.com:8080
        c.setopt(pycurl.PROXY, server )
        if proxySetting["proxyUser"] != None and proxySetting["proxyUser"] != "":
            user     = proxySetting["proxyUser"]
            password = base64.b64decode( proxySetting["proxyPass"] )
            c.setopt(pycurl.PROXYUSERPWD, "%s:%s" % (user,password) )

    ## /var/lib/libvirt/images
    filePath = "/%s/%s" % (IMAGE_BASE_PATH, imageName)
    f = open(filePath, 'w')
    c.setopt(pycurl.WRITEFUNCTION, f.write)
    c.setopt(pycurl.SSL_VERIFYPEER, 0)
    c.perform()
    # FIXME: throw an exception if != 200?
    log.log_debug("curl got HTTP code: %s" % c.getinfo(pycurl.HTTP_CODE))
    f.close()
    return c.getinfo(pycurl.HTTP_CODE)

def _generate_uuid():
    """Generate a random UUID and return it."""

    uuid_list = [ random.randint(0, 255) for _ in range(0, 16) ]
    return ("%02x" * 16) % tuple(uuid_list)

def _connect_to_hypervisor():
    """
    Connects to the hypervisor.
    """
    # First, attempt to import libvirt.  If we don't have that, we can't do
    # much else.
    try:
        import libvirt
    except ImportError, ie:
        raise VirtLibNotFoundException, \
              "Unable to locate libvirt: %s" % str(ie)

    # Attempt to connect to the hypervisor.
    connection = None
    try:
        connection = libvirt.open(None)
    except Exception, e:
        raise VirtualizationKickstartException, \
              "Could not connect to hypervisor: %s" % str(e)

    return connection

#
# this is not nice but tarfile.py does not support
# sparse file writing :(
#
def _extractTar( source, dest ):
    param = "xf"
    if not os.path.exists( source ):
        log.log_debug("file not found: %s" % source)
        raise Exception("file not found: %s" % source)

    if not os.path.exists( dest ):
        log.log_debug("path not found: %s" % dest)
        raise Exception("path not found: %s" % dest)

    if( source.endswith("gz") ):
        param = param + "z"
    elif( source.endswith("bz2") ):
        param = param + "j"

    cmd = "tar %s %s -C %s " % ( param, source, dest )
    log.log_debug(cmd)
    if os.system( cmd ) != 0:
        log.log_debug( "%s failed" % cmd )
        raise Exception("%s failed" % cmd)

    return 0

def _md5(path):
    f = open(path, "rb")
    sum = hashlib.md5()
    while 1:
        block = f.read(128)
        if not block:
            break
        sum.update(block)
    f.close()
    return sum.hexdigest()


def _imageExists(name, md5Sum):
    return os.path.exists( name ) and md5Sum == _md5( name )

# download/extract and start a new image
# imageName = myImage.x86_64.
#
def deploy(downloadURL, proxyURL="", proxyUser="", proxyPass="", memKB=524288, vCPUs=1, imageType="vmdk", virtBridge="xenbr0", extraParams="",cache_only=None):
    """start and connect a local image with SUSE Manager"""

    proxySettings = { 'proxyServer' : proxyURL,
                      'proxyUser'   : proxyUser,
                      'proxyPass'   : proxyPass }

    urlParts  = downloadURL.split('/')
    fileName  = urlParts[-1]
    checksum  = urlParts[-2]

    # fileName = workshop_test_sles11sp1.i686-0.0.1.vmx.tar.gz
    # fileName = Just_enough_OS_openSUSE_12.1.x86_64-0.0.1.xen.tar.gz
    m = re.search( '(.*)\.(x86_64|i\d86)-(\d+\.\d+\.\d+)\.(xen|vmx)', fileName )

    imageName = m.group(1)
    imageArch = m.group(2)
    imageVer  = m.group(3)
    imageType = m.group(4)

    log.log_debug( "name=%s arch=%s ver=%s type=%s" % (imageName,imageArch,imageVer,imageType) )

    if len(imageName) < 1:
        log.log_debug("invalid image name")
        return (1, "invalid image name: name=%s arch=%s ver=%s type=%s" % (imageName,imageArch,imageVer,imageType), {})
    if len(imageArch) < 1:
        log.log_debug("invalid image arch")
        return (1, "invalid image arch: name=%s arch=%s ver=%s type=%s" % (imageName,imageArch,imageVer,imageType), {})

    http_response_code = -1
    if not _imageExists(IMAGE_BASE_PATH+fileName, checksum):
        try:
            http_response_code = _getImage(fileName,downloadURL,proxySettings)
        except Exception as e:
            return ( 1, "getting the image failed with: %s" % e )
    if not _imageExists(IMAGE_BASE_PATH+fileName, checksum):
        log.log_debug("image file is not there. HTTP Code is: %s" % http_response_code)
        return (1, "image file is not there: %s" % IMAGE_BASE_PATH+fileName, {})
    try:
        _extractTar( IMAGE_BASE_PATH+fileName, IMAGE_BASE_PATH )
    except Exception as e:
        return (1, "extracting the image tarball failed with: %s" % e, {})

    # image exists in /var/lib/libvirt/images/image-name now

    connection = _connect_to_hypervisor()
    uuid = _generate_uuid()
    # FIXME: check for the extensions. There might be more
    studioFileExtension = "vmdk"
    if imageType == "xen":
        studioFileExtension = "raw"
    fileName = imageName + "-" + imageVer + "/" + imageName + "." + imageArch + "-" + imageVer + "." + studioFileExtension
    # FIXME
    imagePath = IMAGE_BASE_PATH + "/" + fileName
    log.log_debug("working on image in %s" % imagePath)
    if not os.path.exists( imagePath ):
        return (1, "extracted image not found at %s" % imagePath, {})
    if imageArch in ( 'i386', 'i486', 'i568' ):
        imageArch = 'i686'

    create_params = { 'name'           : imageName,
                      'arch'           : imageArch,
                      'extra'          : extraParams,
                      'mem_kb'         : memKB,
                      'vcpus'          : vCPUs,
                      'uuid'           : uuid,
                      'disk'           : imagePath,
                      'imageType'      : imageType,
                      'virtBridge'     : virtBridge,
#                     'mac'            : mac,
#                     'syslog'         : syslog 
                    }
    create_xml = ""
    if imageType == "xen":
        create_xml = XEN_CREATE_TEMPLATE % create_params
    else:
        create_xml = KVM_CREATE_TEMPLATE % create_params
    log.log_debug("libvirt XML: %s" % create_xml)
    domain = None
    try:
        domain = connection.defineXML(create_xml)
    except Exception, e:
        return (1, "failed to pass XML to libvirt: %s" % e, {})

    domain.create()
    virt_support.refresh()

    return (0, "image '%s' deployed and started" % imageName, {})

# just for testing
if __name__ == "__main__":

    deploy("workshop_test_sles11sp1.i686-0.0.1.vmx.tar.gz", "f7c59ca83c5ffdff5e0455add9fea51f")

