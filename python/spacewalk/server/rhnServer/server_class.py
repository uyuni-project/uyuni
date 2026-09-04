#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2008--2016 Red Hat, Inc.
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
# Stuff for handling Servers
#

# system modules

from spacewalk.common.rhnException import rhnException, rhnFault
from spacewalk.common.rhnLog import log_debug, log_error
from spacewalk.server import rhnSQL, rhnUser

# Local Modules
from . import server_lib
from .server_certificate import Certificate


class Server:
    """Main Server class"""

    def __init__(self):
        self.user = None
        # Use the handy TableRow
        self.server = rhnSQL.Row("rhnServer", "id")
        self.server["release"] = ""
        self.server["os"] = "SUSE Linux"
        self.addr = {}
        # We only get this passed in when we create a new
        # entry. Usually a reload will create a dummy entry first and
        # then call self.loadcert()
        self.cert = None
        # Also, at this point we know that this is a real server
        self.type = "REAL"

        # custom info values
        self.custom_info = None

        # uuid
        self.uuid = None
        self.virt_uuid = None
        self.registration_number = None

    def __repr__(self):
        # misa: looks like id can return negative numbers, so use %d
        # instead of %x
        # For the gory details,
        # http://mail.python.org/pipermail/python-dev/2005-February/051559.html
        # pylint: disable-next=consider-using-f-string
        return "<Server Class at %d: %s>\n" % (
            id(self),
            {
                "self.cert": self.cert,
                "self.server": self.server.data,
            },
        )

    __str__ = __repr__

    # return the id of this system
    def getid(self):
        if not self.server.has_key("id"):
            sysid = rhnSQL.Sequence("rhn_server_id_seq")()
            # pylint: disable-next=consider-using-f-string
            self.server["digital_server_id"] = "ID-%09d" % sysid
            # we can't reset the id column, so we need to poke into
            # internals. kind of illegal, but it works...
            self.server.data["id"] = (sysid, 0)
        else:
            sysid = self.server["id"]
        return sysid

    def reload(self, server, reload_all=0):
        # pylint: disable-next=consider-using-f-string
        log_debug(4, server, "reload_all = %d" % reload_all)

        if not self.server.load(int(server)):
            log_error("Could not find server record for reload", server)
            raise rhnFault(29, "Could not find server record in the database")
        self.cert = None
        # it is lame that we have to do this
        h = rhnSQL.prepare("""
        select label from rhnServerArch where id = :archid
        """)
        h.execute(archid=self.server["server_arch_id"])
        data = h.fetchone_dict()
        if not data:
            raise rhnException(
                "Found server with invalid numeric " "architecture reference",
                self.server.data,
            )
        # we don't know this one anymore (well, we could look for, but
        # why would we do that?)
        self.user = None

        return 0

    # Use the values we find in the cert to cause a reload of this
    # server from the database.
    def loadcert(self, cert, load_user=1):
        log_debug(4, cert)
        # certificate is presumed to be already verified
        if not isinstance(cert, Certificate):
            return -1
        # reload the whole thing based on the cert data
        server = cert["system_id"]
        row = server_lib.getServerID(server)
        if row is None:
            return -1
        sid = row["id"]
        # standard reload based on an ID
        ret = self.reload(sid)
        if not ret == 0:
            return ret

        # the reload() will never be able to fill in the username.  It
        # would require from the database standpoint insuring that for
        # a given server we can have only one owner at any given time.
        # cert includes it and it's valid because it has been verified
        # through checksuming before we got here

        self.user = None

        # Load the user if at all possible. If it's not possible,
        # self.user will be None, which should be a handled case wherever
        # self.user is used.
        if load_user:
            # Load up the username associated with this profile
            self.user = rhnUser.search(cert["username"])

        # 4/27/05 wregglej - Commented out this block because it was causing problems
        # with rhn_check/up2date when the user that registered the system was deleted.
        #    if not self.user:
        #        log_error("Invalid username for server id",
        #                  cert["username"], server, cert["profile_name"])
        #        raise rhnFault(9, "Invalid username '%s' for server id %s" %(
        #            cert["username"], server))

        # XXX: make sure that the database thinks that the server
        # registrnt is the same as this certificate thinks. The
        # certificate passed checksum checks, but it never hurts to be
        # too careful now with satellites and all.
        return 0

    # Is this server entitled?
    def check_entitlement(self):
        if not self.server.has_key("id"):
            return None
        log_debug(3, self.server["id"])

        return server_lib.check_entitlement(self.server["id"])

    def checkin(self, commit=1):
        """convenient wrapper for these thing until we clean the code up"""
        if not self.server.has_key("id"):
            return 0  # meaningless if rhnFault not raised
        return server_lib.checkin(self.server["id"], commit)

    def throttle(self):
        """convenient wrapper for these thing until we clean the code up"""
        if not self.server.has_key("id"):
            return 1  # meaningless if rhnFault not raised
        return server_lib.throttle(self.server)

    def set_qos(self):
        """convenient wrapper for these thing until we clean the code up"""
        if not self.server.has_key("id"):
            return 1  # meaningless if rhnFault not raised
        return server_lib.set_qos(self.server["id"])
