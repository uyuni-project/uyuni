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

from spacewalk.common.rhnLog import log_debug, log_error
from spacewalk.common.rhnException import rhnFault
from spacewalk.common.rhnTB import Traceback
from spacewalk.server import rhnSQL

class SuseData:
  def __init__(self):
    log_debug(4, "SuseData initialized")
    self.suse_products = {}
    # suse_products["guid"], suse_products["secret"], suse_products["ostarget"], suse_products["products"]

  def get_suse_products(self):
      return self.suse_products

  def add_suse_products(self, suse_products):
      log_debug(1, suse_products)
      if not isinstance(suse_products, dict):
	  log_error("argument type is not  hash: %s" % suse_products)
	  raise TypeError, "This function requires a hash as an argument"
      self.suse_products = suse_products

  def save_suse_products_byid(self, sysid):
      log_debug(1, sysid, self.suse_products )
      if self.suse_products == {}: # nothing loaded
	return 0
      self.create_update_suse_products(self.server["id"],
				       self.suse_products["guid"],
				       self.suse_products["secret"],
				       self.suse_products["ostarget"],
				       self.suse_products["products"])
      return 0

  def create_update_suse_products(self, sysid, guid, secret, ostarget, products):
    log_debug(4, sysid, guid, ostarget, products)

    # search, if a suseServer with this guid exists which is not this server
    # this would indicate a re-registration and we need to remove the old rhnServer
    h = rhnSQL.prepare("""
    SELECT
           rhn_server_id as id
      FROM suseServer
     WHERE guid = :guid
       AND rhn_server_id != :sysid
    """)
    h.execute(sysid = sysid, guid=guid)
    d = h.fetchone_dict()
    if d:
      old_sysid = d['id']
      log_debug(1, "Found duplicate server:", old_sysid)
      delete_server = rhnSQL.Procedure("delete_server")
      try:
        if old_sysid != None:
          delete_server(old_sysid)
      except rhnSQL.SQLError:
        log_error("Error deleting server: %s" % old_sysid)
      # IF we delete rhnServer all reference are deleted too
      #
      # now switch suseServer to new id
      #h = rhnSQL.prepare("""
      #  UPDATE suseServer
      #     SET rhn_server_id = :sysid
      #  WHERE rhn_server_id = :oldsysid
      #""")
      #h.execute(sysid=sysid, oldsysid=old_sysid);

    # remove this guid from suseDelServer list
    h = rhnSQL.prepare("""
      DELETE FROM suseDelServer
      WHERE guid = :guid
    """)
    h.execute(guid=guid)
    #rhnSQL.commit()

    # search if suseServer with ID sysid exists
    h = rhnSQL.prepare("""
      SELECT
        s.rhn_server_id as id,
        s.guid,
        s.secret,
        sot.target as ostarget,
        s.ncc_sync_required
      FROM suseServer s
      LEFT JOIN suseOSTarget sot ON s.ostarget_id = sot.id
      WHERE rhn_server_id = :sysid
    """)
    h.execute(sysid = sysid)
    t = h.fetchone_dict()
    ncc_sync_required = False

    # if not; create new suseServer
    if not t:
      ncc_sync_required = True
      h = rhnSQL.prepare("""
        INSERT INTO suseServer
          (rhn_server_id, guid, secret, ostarget_id)
          values (:sysid, :guid, :secret, 
          (select id from suseOSTarget
           where os = :ostarget))
      """)
      h.execute(sysid=sysid, guid=guid, secret=secret, ostarget=ostarget)
    else:
    # if yes, read values and compare them with the provided data
    # update if needed
      data = {
        'rhn_server_id' : sysid,
        'guid'          : guid,
        'secret'        : secret,
        'ostarget'      : ostarget
      }

      if t['guid'] != guid or t['secret'] != secret or t['ostarget'] != ostarget:
        ncc_sync_required = True
        h = rhnSQL.prepare("""
          UPDATE suseServer
             SET guid = :guid,
                 secret = :secret,
                 ostarget_id = (select id from suseOSTarget where os = :ostarget)
           WHERE rhn_server_id = :rhn_server_id
        """)
        apply(h.execute, (), data)
    # check products
    h = rhnSQL.prepare("""
      SELECT
          suse_installed_product_id as id
        FROM suseServerInstalledProduct
       WHERE rhn_server_id = :sysid
    """)
    h.execute(sysid=sysid)
    existing_products = map(lambda x: x['id'], h.fetchall_dict() or [])

    for product in products:
      sipid = self.get_installed_product_id(product)
      if not sipid:
        continue
      if sipid in existing_products:
        existing_products.remove(sipid)
        continue
      h = rhnSQL.prepare("""
        INSERT INTO suseServerInstalledProduct
        (rhn_server_id, suse_installed_product_id)
        VALUES(:sysid, :sipid)
      """)
      h.execute(sysid=sysid, sipid=sipid)
      ncc_sync_required = True

    for pid in existing_products:
      h = rhnSQL.prepare("""
        DELETE from suseServerInstalledProduct
         WHERE rhn_server_id = :sysid
           AND suse_installed_product_id = :pid
      """)
      h.execute(sysid=sysid, pid=pid)
      ncc_sync_required = True

    if ncc_sync_required:
      # If the data have changed, we set the
      # sync_required flag and reset the errors
      # flag to give the registration another try
      h = rhnSQL.prepare("""
        UPDATE suseServer
           SET ncc_sync_required = 'Y',
               ncc_reg_error = 'N'
        WHERE rhn_server_id = :sysid
      """)
      h.execute(sysid=sysid)
    #rhnSQL.commit()


  def get_installed_product_id(self, product):
    version_query = "sip.version = :version"
    release_query = "sip.release = :release"
    if product['version'] is None or product['version'] == '':
       product['version'] = None
       version_query = "(sip.version is NULL)"
    if product['release'] is None or product['release'] == '':
       product['release'] = None
       release_query = "(sip.release is NULL)"

    h = rhnSQL.prepare("""
      SELECT sip.id
        FROM suseInstalledProduct sip
         JOIN rhnPackageArch rpa ON sip.arch_type_id = rpa.id
       WHERE sip.name = :name
         AND %s
         AND rpa.label = :arch
         AND %s
         AND sip.is_baseproduct = :baseproduct
    """ % (version_query, release_query))
    apply(h.execute, (), product)
    d = h.fetchone_dict()
    if not d:
      # not available yet, so let's create one
      n = rhnSQL.prepare("""
        INSERT INTO suseInstalledProduct
        (id, name, version, arch_type_id, release, is_baseproduct)
        VALUES (sequence_nextval('suse_inst_pr_id_seq'), :name, :version,
               (SELECT id FROM rhnPackageArch WHERE label = :arch),
               :release, :baseproduct)
      """)
      apply(n.execute, (), product)
      apply(h.execute, (), product)
      d = h.fetchone_dict()
      if not d:
        # should never happen
        log_error("Unable to create installed product item %s-%s-%s-%s" % (
          product['name'], product['version'], product['release'], product['arch']))
        return None

    return d['id']
