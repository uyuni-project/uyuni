#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2022 SUSE LLC
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


def find_or_create_eula(eula: str):
    """Return the id of the eula inside of the suseEula table.

    A new entry inside of the suseEula table is added only when needed.
    """
    _query_find = """
        SELECT id
          FROM suseEula
         WHERE checksum = :checksum
    """
    checksum = hashlib.new("sha256", eula.encode("utf-8", "ignore")).hexdigest()

    h = rhnSQL.prepare(_query_find)
    h.execute(checksum=checksum)
    ret = h.fetchone_dict()

    if ret:
        return ret["id"]
    else:
        _query_create_eula_id = """
            SELECT sequence_nextval('suse_eula_id_seq') AS id
            FROM dual
        """
        h = rhnSQL.prepare(_query_create_eula_id)
        h.execute(checksum=checksum)
        ret = h.fetchone_dict()
        # pylint: disable-next=redefined-builtin
        id = None
        if ret:
            id = ret["id"]
        else:
            raise rhnFault(50, "Unable to add new EULA to the database", explain=0)

        blob_map = {"text": "text"}
        h = rhnSQL.prepare(
            """
                INSERT INTO suseEula (id, text, checksum)
                VALUES (:id, :text, :checksum)
            """,
            blob_map=blob_map,
        )
        h.execute(id=id, text=eula, checksum=checksum)

        return id


# pylint: disable-next=redefined-builtin
def get_eula_by_id(id):
    """Return the text of the EULA, None if the EULA is not found"""
    h = rhnSQL.prepare("SELECT text from suseEula WHERE id = :id")
    h.execute(id=id)
    match = h.fetchone_dict()
    if match:
        return str(match["text"])
    else:
        return None


def get_eula_by_checksum(checksum):
    """Return the text of the EULA, None if the EULA is not found"""
    h = rhnSQL.prepare("SELECT text from suseEula WHERE checksum = :checksum")
    h.execute(checksum=checksum)
    match = h.fetchone_dict()
    if match:
        return str(match["text"])
    else:
        return None
