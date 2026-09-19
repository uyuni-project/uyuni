#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2008--2016 Red Hat, Inc.
# Copyright (c) 2025 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
# implements a bunch of functions needed by rhnServer modules
#

from functools import reduce

# pylint: disable-next=wrong-import-position
from spacewalk.common.rhnLog import log_debug, log_error

# pylint: disable-next=wrong-import-position
from spacewalk.server import rhnSQL

# Do not import server.apacheAuth in this module, or the secret generation
# script will traceback - since it would try to import rhnSecret which doesn't
# exist


# pylint: disable-next=invalid-name,dangerous-default-value
def getServerID(server, fields=[]):
    """Given a textual digitalid (old style or new style) or simply an ID
    try to search in the database and return the numeric id (thus doing
    validation in case you pass a numeric ID already)

    If found, it will return a dictionary with at least an "id" member

    Additional fields can be requested by passing an array of strings
    with field names from rhnServer
    check if all chars of a string are in a set
    """

    def check_chars(s):
        # pylint: disable-next=possibly-used-before-assignment
        return reduce(lambda a, b: a and b in "0123456789", s, 1)

    log_debug(4, server, fields)
    if not type(server) in [type(""), type(0)]:
        return None

    # pylint: disable-next=unidiomatic-typecheck
    if type(server) == type(0):
        search_id = server  # will search by number
    elif server[:7] == "SERVER-":  # old style certificate
        search_id = server
    elif server[:3] == "ID-":  # new style id, extract the numeric id
        tmp_id = server[3:]
        if not tmp_id or check_chars(tmp_id) == 0:
            # invalid certificate, after ID- we have non numbers
            return None
        search_id = int(tmp_id)
    else:
        # this is string. if all are numbers, then try to convert to int
        if check_chars(server) == 0:
            # throughly invalid id, whet the heck do we do?
            # pylint: disable-next=consider-using-f-string
            log_error("Invalid server ID passed in search: %s" % server)
            return None
        # otherwise try as int
        try:
            search_id = int(server)
        except ValueError:
            return None

    # Now construct the extra stuff for the case when additional fields
    # are requested
    xfields = ""
    archdb = ""
    archjoin = ""
    # look at the fields
    fields = [f.lower() for f in fields]
    for k in fields:
        if k == "id":  # already there
            continue
        if k == "arch":
            archdb = ", rhnServerArch sa"
            archjoin = "and s.server_arch_id = sa.id"
            # pylint: disable-next=consider-using-f-string
            xfields = "%s, a.label arch" % xfields
            continue
        # pylint: disable-next=consider-using-f-string
        xfields = "%s, s.%s" % (xfields, k)
    # ugliness is over

    # Now build the search
    # pylint: disable-next=unidiomatic-typecheck
    if type(search_id) == type(0):
        h = rhnSQL.prepare(
            # pylint: disable-next=consider-using-f-string
            """
        select s.id %s from rhnServer s %s
        where s.id = :p1 %s
        """
            % (xfields, archdb, archjoin)
        )
    else:  # string
        h = rhnSQL.prepare(
            # pylint: disable-next=consider-using-f-string
            """
        select s.id %s from rhnServer s %s
        where s.digital_server_id = :p1 %s
        """
            % (xfields, archdb, archjoin)
        )
    h.execute(p1=search_id)
    row = h.fetchone_dict()
    if row is None or row["id"] is None:  # not found
        return None
    return row


# pylint: disable-next=invalid-name
def getServerSecret(server):
    """retrieve the server secret using the great getServerID function"""
    row = getServerID(server, ["secret"])
    if row is None:
        return None
    return row["secret"]


###############################
# Server Class Helper functions
###############################


def checkin(server_id, commit=1):
    """checkin - update the last checkin time"""
    log_debug(3, server_id)
    h = rhnSQL.prepare("""
    update rhnServerInfo
    set checkin = current_timestamp, checkin_counter = checkin_counter + 1
    where server_id = :server_id
    """)
    h.execute(server_id=server_id)
    if commit:
        rhnSQL.commit()
    return 1


# pylint: disable-next=unused-argument
def set_qos(server_id):
    pass


# pylint: disable-next=unused-argument
def throttle(server):
    """throttle - limits access to free users if a throttle file exists
    NOTE: We don't throttle anybody. Just stub.
    """
    # server_id = server['id']
    # log_debug(3, server_id)
    #
    # Are we throttling?
    # throttlefile = "/usr/share/rhn/throttle"
    # if not os.path.exists(throttlefile):
    #    # We don't throttle anybody
    #    return
    return


def check_entitlement(server_id, want_array=False):
    h = rhnSQL.prepare(
        """select server_id, label, is_base from rhnServerEntitlementView where server_id = :server_id order by is_base DESC"""
    )
    # h = rhnSQL.prepare("""select server_id, label from rhnServerEntitlementView where server_id = :server_id""")
    h.execute(server_id=server_id)

    # if I read the old code correctly, this should do about the same thing.
    # Basically "entitled? yay/nay" -akl.  UPDATE 12/08/06: akl says "nay".
    # It's official
    rows = h.fetchall_dict()
    ents = {}
    ents_array = []

    if rows:
        for row in rows:
            ents[row["label"]] = row["label"]
            ents_array.append(row["label"])
        if want_array:
            return ents_array
        return ents

    if want_array:
        return ents_array
    # Empty dictionary - will act as False
    return ents


def check_entitlement_by_machine_id(machine_id):
    h = rhnSQL.prepare("""
    select e.label from rhnServer s, rhnServerEntitlementView e
    where s.machine_id=:machine_id and s.id=e.server_id
    """)
    h.execute(machine_id=machine_id)
    rows = h.fetchall_dict()
    return [row["label"] for row in rows] if rows else []
