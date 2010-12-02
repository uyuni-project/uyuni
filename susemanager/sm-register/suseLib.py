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
from spacewalk.common import log_debug, log_error, rhnFault
from spacewalk.server import rhnSQL

def channelForProduct(product):
  q_version = ""
  q_release = ""
  q_arch    = ""
  product_id = None

  if version in product and product['version'] != "":
    q_version = "version = :version or"
  if release in product and product['release'] != "":
    q_release = "release = :release or"
  if arch in product and product['arch'] != "":
    q_arch = "arch = :arch or"

  h = rhnSQL.prepare("""
    SELECT id, name, version, arch, release
    FROM suseProducts
    WHERE name = :name
      AND ( %s version IS NULL)
      AND ( %s release IS NULL)
      AND ( %s arch IS NULL)
  """ % (q_version, q_release, q_arch)
  )
  h.execute(product)
  rs = h.fetchall_dict()
  if not rs:
    log_debug(1, "No Channel Found")
    return None
  if len(rs) > 1:
    # more than one product matches.
    # search for an exact match or take the first
    for p in rs:
      if p['version'] == product['version'] and \
         p['release'] == product['release'] and \
         p['arch'] == product['arch']:
           product_id = p['id']
           break
  else:
    product_id = rs[0]['id']

  h.rhnSQL.prepare("""
    SELECT c.id, c.label
    FROM rhnChannel c
    JOIN suseProductChannel as spc ON spc.channel_id = c.id
    WHERE spc.product_id = :pid
  """)
  h.execute(product)
  rs = h.fetchall_dict()
  if not rs:
    log_debug(1, "No Channel Found")
    return None
  ret = []
  for channel in rs:
    ret.append(channel['id'])
    log_debug(1, "Found channel %s with id %d" % (channel['label'], channel['od']))

  return ret













