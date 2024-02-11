#  pylint: disable=missing-module-docstring
# -*- coding: utf-8 -*-
#
# Copyright (c) 2010 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

from spacewalk.common.rhnLog import log_debug, log_error

# pylint: disable-next=unused-import
from spacewalk.common.rhnException import rhnFault

# pylint: disable-next=unused-import
from spacewalk.common.rhnTB import Traceback
from spacewalk.server import rhnSQL


# pylint: disable-next=missing-class-docstring
class SuseData:
    def __init__(self):
        log_debug(4, "SuseData initialized")
        # format:
        # suse_products [{ 'name' = ..., 'version' = ..., 'release' = ..., 'arch' = ..., 'baseproduct' = ...},
        #               ...]
        self.suse_products = []

    def get_suse_products(self):
        if len(self.suse_products) == 0:
            self.suse_products = []
            self.load_suse_products()
        return self.suse_products

    def load_suse_products(self):
        log_debug(1, "load suse_products")
        if not self.server["id"]:
            return
        h = rhnSQL.prepare(
            """
          SELECT sip.name,
                 sip.version,
                 sip.release,
                 rpa.label arch,
                 sip.is_baseproduct baseproduct
            FROM suseInstalledProduct sip
             JOIN rhnPackageArch rpa ON sip.arch_type_id = rpa.id
             JOIN suseServerInstalledProduct ssip ON sip.id = ssip.suse_installed_product_id
           WHERE ssip.rhn_server_id = :server_id
      """
        )
        h.execute(server_id=self.server["id"])
        self.suse_products = h.fetchall_dict() or []

    def add_suse_products(self, suse_products):
        log_debug(1, suse_products)
        if isinstance(suse_products, dict):
            self.suse_products = suse_products["products"]
        elif isinstance(suse_products, list):
            self.suse_products = suse_products

    def save_suse_products_byid(self, sysid):
        log_debug(1, sysid, self.suse_products)
        if len(self.suse_products) == 0:  # nothing loaded
            return 0
        self.create_update_suse_products(self.server["id"], self.suse_products)
        return 0

    def create_update_suse_products(self, sysid, products):
        log_debug(4, sysid, products)

        # check products
        h = rhnSQL.prepare(
            """
      SELECT
          suse_installed_product_id as id
        FROM suseServerInstalledProduct
       WHERE rhn_server_id = :sysid
    """
        )
        h.execute(sysid=sysid)
        existing_products = [x["id"] for x in h.fetchall_dict() or []]

        for product in products:
            sipid = self.get_installed_product_id(product)
            if not sipid:
                continue
            if sipid in existing_products:
                existing_products.remove(sipid)
                continue
            h = rhnSQL.prepare(
                """
        INSERT INTO suseServerInstalledProduct
        (rhn_server_id, suse_installed_product_id)
        VALUES(:sysid, :sipid)
      """
            )
            h.execute(sysid=sysid, sipid=sipid)

        for pid in existing_products:
            h = rhnSQL.prepare(
                """
        DELETE from suseServerInstalledProduct
         WHERE rhn_server_id = :sysid
           AND suse_installed_product_id = :pid
      """
            )
            h.execute(sysid=sysid, pid=pid)

    def get_installed_product_id(self, product):
        version_query = "sip.version = :version"
        release_query = "sip.release = :release"
        if product["version"] is None or product["version"] == "":
            product["version"] = None
            version_query = "(sip.version is NULL)"
        if product["release"] is None or product["release"] == "":
            product["release"] = None
            release_query = "(sip.release is NULL)"

        h = rhnSQL.prepare(
            # pylint: disable-next=consider-using-f-string
            """
      SELECT sip.id
        FROM suseInstalledProduct sip
         JOIN rhnPackageArch rpa ON sip.arch_type_id = rpa.id
       WHERE sip.name = :name
         AND %s
         AND rpa.label = :arch
         AND %s
         AND sip.is_baseproduct = :baseproduct
    """
            % (version_query, release_query)
        )
        h.execute(*(), **product)
        d = h.fetchone_dict()
        if not d:
            # not available yet, so let's create one
            n = rhnSQL.prepare(
                """
        INSERT INTO suseInstalledProduct
        (id, name, version, arch_type_id, release, is_baseproduct)
        VALUES (sequence_nextval('suse_inst_pr_id_seq'), :name, :version,
               (SELECT id FROM rhnPackageArch WHERE label = :arch),
               :release, :baseproduct)
      """
            )
            n.execute(*(), **product)
            h.execute(*(), **product)
            d = h.fetchone_dict()
            if not d:
                # should never happen
                log_error(
                    # pylint: disable-next=consider-using-f-string
                    "Unable to create installed product item %s-%s-%s-%s"
                    % (
                        product["name"],
                        product["version"],
                        product["release"],
                        product["arch"],
                    )
                )
                return None

        return d["id"]
