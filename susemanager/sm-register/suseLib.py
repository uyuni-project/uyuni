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

import string
import os
import tempfile
import ConfigParser
import urlparse
from spacewalk.common import log_debug, log_error, rhnFault
from spacewalk.server import rhnSQL

def findProduct(product):
  q_version = ""
  q_release = ""
  q_arch    = ""
  product_id = None
  product_lower['name'] = product['name'].lower()

  log_debug(2, "Search for product: %s" % product)

  if 'version' in product and product['version'] != "":
    q_version = "or sp.version = :version"
    product_lower['version'] = product['version'].lower()
  if 'release' in product and product['release'] != "":
    q_release = "or sp.release = :release"
    product_lower['release'] = product['release'].lower()
  if 'arch' in product and product['arch'] != "":
    q_arch = "or pat.label = :arch"
    product_lower['arch'] = product['arch'].lower()

  h = rhnSQL.prepare("""
    SELECT sp.id, sp.name, sp.version, pat.label as arch, sp.release
      FROM suseProducts sp
 LEFT JOIN rhnPackageArch pat ON pat.id = sp.arch_type_id
     WHERE sp.name = :name
       AND (sp.version IS NULL %s)
       AND (sp.release IS NULL %s)
       AND (sp.arch_type_id IS NULL %s)
  ORDER BY name, version, release, arch
  """ % (q_version, q_release, q_arch))
  apply(h.execute, (), product_lower)
  rs = h.fetchall_dict()

  if not rs:
    log_debug(1, "No Product Found")
    return None

  product_id = rs[0]['id']

  if len(rs) > 1:
    # more than one product matches.
    # search for an exact match or take the first
    for p in rs:
      if p['version'] == product['version'] and \
         p['release'] == product['release'] and \
         p['arch'] == product['arch']:
        product_id = p['id']
        break

  return product_id

def channelForProduct(product):

  product_id = findProduct(product)
  if not product_id:
    return None

  h.rhnSQL.prepare("""
    SELECT c.id, c.label
    FROM rhnChannel c
    JOIN suseProductChannel spc ON spc.channel_id = c.id
    WHERE spc.product_id = :pid
  """)
  h.execute(pid=product_id)
  rs = h.fetchall_dict()
  if not rs:
    log_debug(1, "No Channel Found")
    return None
  ret = []
  for channel in rs:
    ret.append(channel['id'])
    log_debug(1, "Found channel %s with id %d" % (channel['label'], channel['od']))

  return ret

class URL:
  scheme = ""
  username = ""
  password = ""
  host = ""
  port = ""
  path = ""
  query = ""
  fragment = ""

  def __init__(self, url):
    u = urlparse.urlsplit(url)
    self.scheme = u.scheme
    self.username = u.username
    self.password = u.password
    self.host = u.hostname
    self.port = u.port
    self.path = u.path
    self.query = u.query
    self.fragment = u.fragment

  def getURL(self):
    netloc = ""
    if self.username:
      netloc = self.username
    if self.password:
      netloc += ":" + self.password
    if self.host and netloc :
      netloc += "@" + self.host
    elif self.host:
      netloc = self.host

    if self.port:
      netloc += ":" + self.port

    return urlparse.urlunsplit((self.scheme, netloc, self.path, self.query, self.fragment))

