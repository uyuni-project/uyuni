# -*- coding: utf-8 -*-
#
# Copyright (c) 2010 Novell
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
import urllib
import xml.etree.ElementTree as etree
from xml.etree import cElementTree

from spacewalk.server import rhnSQL
from spacewalk.common import rhnLog, suseLib
from spacewalk.common.rhnConfig import CFG, initCFG
from spacewalk.common.rhnLog import log_debug, log_error
from spacewalk.common.rhnTB import fetchTraceback

from spacewalk.server.rhnSQL.const import ORACLE, POSTGRESQL
default_log_location = '/var/log/rhn/'

iterparse = cElementTree.iterparse

#
# TODO:
# * try to find other regdata like timezone, processor, machine
#
# DONE:
# * update database after registration
# * check products known by NCC
# * add ncc_reg_error to schema
# * create NCCcredentials file if it does not exist
# * find virtual host guid, if available
# * de-register
# * ask for mirror credentials and ncc email during setup and add them to rhn.conf


class Register:
  ns = "http://www.novell.com/xml/center/regsvc-1_0"
  guid = None

  def __init__(self):
    initCFG('server.susemanager')
    db_string = CFG.DEFAULT_DB
    rhnSQL.initDB(db_string)
    log_filename = 'mgr-register.log'
    rhnLog.initLOG(default_log_location + log_filename, CFG.DEBUG)

  def main(self):
    # reset error for all server which modified date is
    # 14 days ago
    h = rhnSQL.prepare("""
      UPDATE suseServer
         SET ncc_reg_error = 'N'
       WHERE modified < current_timestamp - numtodsinterval(14 * 86400, 'second')
    """)
    h.execute()

    self.perform_deregister()

    h = rhnSQL.prepare("""
      SELECT s.rhn_server_id as rhnserverid,
             s.guid, 
             s.secret, 
             sot.target as ostarget,
             sot.os as ostargetbak
        FROM suseServer s
        JOIN suseOSTarget sot on s.ostarget_id = sot.id
       WHERE ncc_sync_required = 'Y'
         AND ncc_reg_error = 'N'
    """)
    h.execute()
    res = h.fetchall_dict()

    if not res:
      # nothing to register. Exit
      return

    counter = 0
    root = etree.Element('bulkop', attrib={
                           'xmlns' : self.ns,
                           'client_version' : '1.2.3',
                           'lang' : 'en',
                           })
    for server in res:
      if counter >= 10:
        rhnSQL.commit()
        self.send(root)
        # create new root element for the next registration
        root = etree.Element('bulkop', attrib={
          'xmlns' : self.ns,
          'client_version' : '1.2.3',
          'lang' : 'en',
          })
        counter = 0

      if not self.build_register_xml(server, root):
        continue

      counter += 1
    
      h = rhnSQL.prepare("""
        UPDATE suseServer
           SET ncc_sync_required = 'N'
         WHERE guid = :guid
      """)
      h.execute(guid=server['guid'])

    rhnSQL.commit()
    self.send(root)
    rhnSQL.commit()

  def perform_deregister(self):
    h = rhnSQL.prepare("""
    SELECT guid
    FROM suseDelServer
    """)
    h.execute()
    res = h.fetchall_dict()
    
    if not res:
      # nothing to de-register. Exit
      return

    counter = 0
    root = etree.Element('bulkop', attrib={
                         'xmlns' : self.ns,
                         'client_version' : '1.2.3',
                         'lang' : 'en',
                         })
    for server in res:
      counter += 1
      if counter > 10:
        rhnSQL.commit()
        self.send(root)
        # create new root element for the next registration
        root = etree.Element('bulkop', attrib={
                             'xmlns' : self.ns,
                             'client_version' : '1.2.3',
                             'lang' : 'en',
                             })
        counter = 1

      self.build_deregister_xml(server['guid'], root)
      h = rhnSQL.prepare("""
        DELETE FROM suseDelServer
        WHERE guid = :guid
      """)
      h.execute(guid=server['guid'])

      rhnSQL.commit()
      self.send(root)
      rhnSQL.commit()

  def send(self, root):
    xml = etree.tostring(root)
    log_debug(2, "SEND: %s" % xml)

    regurl = suseLib.URL(CFG.reg_url)
    regurl.query = "command=bulkop&lang=en&version=1.0"
    new_url = regurl.getURL()

    f = suseLib.send(new_url, xml)

    # parse the answer
    for event, elem in iterparse(f):
      if elem.tag != ("{%s}bulkstatus" % (self.ns)):
        continue
      for child in elem:
        if child.tag == ("{%s}status" % (self.ns)):
          self._parse_status(child)

  def _parse_status(self, elem):
    operation = elem.attrib.get("operation")
    result = elem.attrib.get("result")
    guid = None
    msg = ""

    for child in elem:
      if child.tag == ("{%s}guid" % (self.ns)):
        guid = child.text
      if child.tag == ("{%s}message" % (self.ns)):
        msg = child.text

    if not guid:
      log_error("No GUID")
      return
    if operation not in ['register', 'de-register']:
      log_error("Unknown bulk operation '%s'." % operation)
      return

    if result == "warning":
      log_debug(1, "WARNING: Operation: %s[%s] : %s" % (operation, guid, msg))
      return

    if result == "error":
      log_error("Operation %s[%s] failed: %s" % (operation, guid, msg))
      if operation != "register":
        return
      h = rhnSQL.prepare("""
        UPDATE suseServer
           SET ncc_reg_error = 'Y',
               ncc_sync_required = 'Y'
         WHERE guid = :guid
      """)
      h.execute(guid=guid)
    else:
      # success
      log_debug(1, "Operation '%s' successful: %s" % (operation, guid))

  def build_deregister_xml(self, guid, root):
    register_elem = etree.SubElement(root, 'de-register')
    x = etree.SubElement(register_elem, 'guid')
    x.text = guid
    x = etree.SubElement(register_elem, 'authuser')
    x.text = str(CFG.mirrcred_user)
    x = etree.SubElement(register_elem, 'authpass')
    x.text = str(CFG.mirrcred_pass)
    x = etree.SubElement(register_elem, 'smtguid')
    x.text = str(self.get_guid())

  def build_register_xml(self, server, root):
    h = rhnSQL.prepare("""
      SELECT sip.name, sip.version, at.label as arch, sip.release
        FROM suseInstalledProduct sip
        JOIN suseServerInstalledProduct ssip ON sip.id = ssip.suse_installed_product_id
        JOIN rhnPackageArch at ON sip.arch_type_id = at.id
        WHERE ssip.rhn_server_id = :rhnserverid
    """)
    h.execute(rhnserverid=server['rhnserverid'])
    products = h.fetchall_dict()

    found_registerable_product = False
    for prod in products:
      if self.is_registerable(prod):
        found_registerable_product = True
        break

    if not found_registerable_product:
      return False

    register_elem = etree.SubElement(root, 'register',
                                     attrib={'force' : 'batch'})
    x = etree.SubElement(register_elem, 'guid')
    x.text = server['guid']
    x = etree.SubElement(register_elem, 'secret')
    x.text = server['secret']
    (virttype, hostguid) = self.get_virtual_info(server['rhnserverid'])

    if virttype and hostguid:
      x = etree.SubElement(register_elem, 'host',
                           attrib={'type' : virttype})
      x.text = str(hostguid)
    elif virttype:
      x = etree.SubElement(register_elem, 'host',
                           attrib={'type' : virttype})
      x.text = 'Y'
    else:
      etree.SubElement(register_elem, 'host')

    x = etree.SubElement(register_elem, 'authuser')
    x.text = str(CFG.mirrcred_user)
    x = etree.SubElement(register_elem, 'authpass')
    x.text = str(CFG.mirrcred_pass)
    x = etree.SubElement(register_elem, 'smtguid')
    x.text = str(self.get_guid())

    for prod in products:
      if not self.is_registerable(prod):
        continue
      if prod['release'] is None:
        prod['release'] = ''
      x = etree.SubElement(register_elem, 'product',
                           attrib={'version' : prod['version'],
                                   'release' : prod['release'],
                                   'arch'    : prod['arch']
                                  })
      x.text = prod['name']

    x = etree.SubElement(register_elem, 'param', attrib={'id': 'email'})
    x.text = CFG.mirrcred_email
    x = etree.SubElement(register_elem, 'param', attrib={'id': 'ostarget'})
    x.text = server['ostarget']
    x = etree.SubElement(register_elem, 'param', attrib={'id': 'ostarget-bak'})
    x.text = server['ostargetbak']
    return True

  def get_virtual_info(self, systemid):
    virttype = None
    hostguid = None
    v = rhnSQL.prepare("""
      SELECT vit.label, vi.host_system_id
        FROM rhnvirtualinstancetype vit
         JOIN rhnvirtualinstanceinfo vii ON vii.instance_type = vit.id
         JOIN rhnvirtualinstance vi ON vi.id = vii.instance_id
        WHERE vi.virtual_system_id = :systemid
    """)
    v.execute(systemid=systemid)
    vt = v.fetchone_dict() or None

    if vt and vt['label'] == "virtualbox":
      virttype = "VirtualBox"
    elif vt and vt['label'] == "qemu":
      virttype = "KVM"
    elif vt and vt['label'] == "vmware":
      virttype = "VMWare"
    elif vt and vt['label'] == "hyperv":
      virttype = "Microsoft"
    elif vt and vt['label'] == "fully_virtualized":
      virttype = "Xen"
    elif vt and vt['label'] == "para_virtualized":
      virttype = "Xen"

    if vt and vt['host_system_id']:
      h = rhnSQL.prepare("""
        SELECT guid
          FROM suseServer
         WHERE rhn_server_id = :hostid
      """)
      h.execute(hostid=vt['host_system_id'])
      host = h.fetchone_dict() or None
      hostguid = '0'
      if host and host['guid']:
        hostguid = host['guid']
    return (virttype, hostguid)


  def is_registerable(self, prod):
    if suseLib.findProduct(prod):
      return True
    return False

  def get_guid(self):
    """read guid of this host"""
    if self.guid:
      return self.guid

    res = suseLib.getProductProfile()
    self.guid = res['guid']
    return self.guid

  def reset_errors(self):
    h = rhnSQL.prepare("""
          UPDATE suseServer 
             SET ncc_reg_error='N'
        """)
    h.execute()
