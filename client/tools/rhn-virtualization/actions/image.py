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


# import subprocess
import hashlib 
import pycurl

log = up2dateLog.initLog()
IMAGE_BASE_PATH = "/var/lib/libvirt/images/"

KVM_CREATE_TEMPLATE = """
<domain type='kvm'>
  <name>%(name)s</name>
  <uuid>%(uuid)s</uuid>
  <memory>%(mem_kb)s</memory>
  <vcpu>%(vcpus)s</vcpu>
  <os>
    <type arch='%(arch)s' machine='pc-0.12'>hvm</type>
    <boot dev='hd'/>
  </os>
  <clock offset='utc'/>
  <on_poweroff>destroy</on_poweroff>
  <on_reboot>restart</on_reboot>
  <on_crash>restart</on_crash>
  <devices>
    <emulator>/usr/bin/qemu-kvm</emulator>
    <disk type='file' device='disk'>
      <driver name='qemu' type='%(imageType)s'/>
      <source file='%(disk)s'/>
      <target dev='hda' bus='ide'/>
      <address type='drive' controller='0' bus='0' unit='0'/>
    </disk>
    <controller type='ide' index='0'/>
    <interface type='bridge'>
<!--
      <mac address=''/>
-->
      <source bridge='%(virtBridge)s'/>
    </interface>
    <serial type='pty'>
      <target port='0'/>
    </serial>
    <console type='pty'>
      <target port='0'/>
    </console>
    <input type='mouse' bus='ps2'/>
    <graphics type='vnc' port='-1' autoport='yes' keymap='en-us'/>
    <video>
      <model type='cirrus' vram='9216' heads='1'/>
    </video>
  </devices>
</domain>
"""


XEN_CREATE_TEMPLATE = """
<domain type='xen'>
  <name>%(name)s</name>
  <uuid>%(uuid)s</uuid>
  <memory>%(mem_kb)s</memory>
  <vcpu>%(vcpus)s</vcpu>
<!--
  <currentMemory>307200</currentMemory>
-->
  <bootloader>/usr/bin/pygrub</bootloader>
  <os>
    <type arch='%(arch)s' machine='xenpv'>linux</type>
  </os>
  <clock offset='utc'/>
  <on_poweroff>destroy</on_poweroff>
  <on_reboot>restart</on_reboot>
  <on_crash>restart</on_crash>
  <devices>
    <disk type='file' device='disk'>
      <driver name='tap' type='aio'/>
      <source file='%(disk)s'/>
      <target dev='xvda' bus='xen'/>
      <address type='drive' controller='0' bus='0' unit='0'/>
    </disk>
<!--
     <disk type='file' device='disk'>
      <driver name='tap' type='aio'/>
      <source file='/var/lib/xen/images/rhel5pv.img'/>
      <target dev='xvda' bus='xen'/>
    </disk>
-->
    <interface type='bridge'>
<!--
      <mac address='00:16:3e:60:36:ba'/>
-->
      <source bridge='%(virtBridge)s'/>
    </interface>
    <console type='pty'>
      <target port='0'/>
    </console>
    <input type='mouse' bus='xen'/>
    <graphics type='vnc' port='-1' autoport='yes' listen='0.0.0.0'/>
  </devices>
</domain>
"""

# mark this module as acceptable
__rhnexport__ = [
    'deploy'
]

# download and extract tar.gz file with image
def _getImage(imageName,orgID,checksum):

        spacewalk_auth_headers = ['X-RHN-Server-Id',
                                  'X-RHN-Auth-User-Id',
                                  'X-RHN-Auth',
                                  'X-RHN-Auth-Server-Time',
                                  'X-RHN-Auth-Expire-Offset']

        if not os.geteuid() == 0:
            # you can't access auth data if you are not root
            log.log_debug("Can't access server without root access")
            return 42

        auth_headers = {}
        login_info = up2dateAuth.getLoginInfo()
        for k,v in login_info.items():
            if k in spacewalk_auth_headers:
	        if not v:
		    v = "\nX-libcurl-Empty-Header-Workaround: *"
                    log.log_debug("*****%s=%s\n" % (k,v))
                auth_headers[k] = v

        cfg = config.initUp2dateConfig()
	# serverURL may be a list in the config file, so by default, grab the
	# first element.
	if type(cfg['serverURL']) == type([]):
	    serverUrl = cfg['serverURL'][0]
	else:
	    serverUrl = cfg['serverURL']

        url = "%s/GET-REQ/%s?head_requests=no" % (serverUrl,'datafile/getVImage/%s/%s/%s' % (orgID, checksum, imageName) )
        log.log_debug(url)

        # get the file via pycurl
        c = pycurl.Curl()
        c.setopt(pycurl.URL, url)
        headers = []
        for k in auth_headers:
            headers.append( ("%s:%s" % (k, auth_headers[k])) )
        c.setopt(pycurl.HTTPHEADER, headers)
	## /var/lib/libvirt/images
        filePath = "/%s/%s" % (IMAGE_BASE_PATH, imageName)
        f = open(filePath, 'w')
        c.setopt(pycurl.WRITEFUNCTION, f.write)
        c.setopt(pycurl.SSL_VERIFYPEER, 0)
        c.perform()
        f.close()

        _extractTar( filePath, IMAGE_BASE_PATH )

	return 42

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
        log.log_debug("file not found: ", source)
        return -1

    if not os.path.exists( dest ):
        log.log_debug("path not found: ", dest)
        return -1

    if( source.endswith("gz") ):
        param = param + "z"
    elif( source.endswith("bz2") ):
        param = param + "j"

    cmd = "tar %s %s -C %s " % ( param, source, dest )
    log.log_debug(cmd)
    if os.system( cmd ) != 0:
        log.log_debug( "%s failed" % cmd )

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
def deploy(fileName, checksum, memKB="524288", vCPUs="1", imageType="vmdk", virtBridge="xenbr0", extraParams="",cache_only=None):
    """start and connect a local image with SUSE Manager"""

    # fileName = workshop_test_sles11sp1.i686-0.0.1.vmx.tar.gz
    nameParts = fileName.split('.',1)

    # nameParts[0] = workshop_test_sles11sp1
    # nameParts[1] = i686-0.0.1.vmx.tar.gz
    imageName = nameParts[0]
    m = re.search( '([^-]+)-(\d+\.\d+\.\d+)\.([^.]+)', nameParts[1] )

    imageArch = m.group(1) 
    imageVer  = m.group(2)
    imageType = m.group(3)

    if len(imageName) < 1:
        log.log_debug("invalid image name")
    if len(imageArch) < 1:
        log.log_debug("invalid image arch")

    if not _imageExists(IMAGE_BASE_PATH+fileName, checksum):
        _getImage(fileName,"1",checksum)
    if not _imageExists(IMAGE_BASE_PATH+fileName, checksum):
        log.log_debug("fetching the image failed")

    # image exists in /var/lib/libvirt/images/image-name now

    connection = _connect_to_hypervisor()
    uuid = _generate_uuid()
    studioFileExtension = "vmdk"
    if imageType == "xen":
        studioFileExtension = "raw"
    fileName = imageName + "-" + imageVer + "/" + imageName + "." + imageArch + "-" + imageVer + "." + studioFileExtension
    # FIXME
    imagePath = IMAGE_BASE_PATH + "/" + fileName
    log.log_debug("working on image in %s" % imagePath)
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
    domain = connection.defineXML(create_xml)
    domain.create()
    virt_support.refresh()

    return (0, "image deployed and started", {})

# just for testing
if __name__ == "__main__":

    print "Transaction args:"
    deploy("workshop_test_sles11sp1.i686-0.0.1.vmx.tar.gz", "f7c59ca83c5ffdff5e0455add9fea51f")

