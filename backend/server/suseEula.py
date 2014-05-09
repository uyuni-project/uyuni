# -*- coding: utf-8 -*-
#
# Copyright (c) 2014 SUSE
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

import hashlib
from spacewalk.common.rhnException import rhnFault
from spacewalk.server import rhnSQL

def find_or_create_eula(eula):
    """Return the id of the eula inside of the suseEulas table.

       A new entry inside of the suseEulas table is added only when needed.
    """
    _query_find = """
        SELECT id
          FROM suseEulas
         WHERE checksum = :checksum
    """
    checksum = hashlib.new("sha256", eula).hexdigest()

    h = rhnSQL.prepare(_query_find)
    h.execute(checksum=checksum)
    ret = h.fetchone_dict()

    if ret:
        return ret['id']
    else:
        h = rhnSQL.prepare("""
            INSERT INTO suseEulas (id, text, checksum)
            VALUES (nextval('suse_eulas_id_seq'), :text, :checksum)
        """)

        h.execute(text=eula, checksum=checksum)

        h = rhnSQL.prepare(_query_find)
        h.execute(checksum=checksum)
        ret = h.fetchone_dict()
        if ret:
            return ret['id']
        else:
            raise rhnFault(50, "Unable to add new EULA to the database", explain=0)

def get_eula_by_id(id):
    """ Return the text of the EULA, None if the EULA is not found """
    h = rhnSQL.prepare("SELECT text from suseEulas WHERE id = :id")
    h.execute(id=id)
    match = h.fetchone_dict()
    if match:
        return str(match['text'])
    else:
        return None

def get_eula_by_checksum(checksum):
    """ Return the text of the EULA, None if the EULA is not found """
    h = rhnSQL.prepare("SELECT text from suseEulas WHERE checksum = :checksum")
    h.execute(checksum=checksum)
    match = h.fetchone_dict()
    if match:
        return str(match['text'])
    else:
        return None

