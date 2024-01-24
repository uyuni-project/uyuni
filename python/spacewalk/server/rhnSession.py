#  pylint: disable=missing-module-docstring,invalid-name
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
#
# Session management
#


import hashlib
import time
import sys

from spacewalk.common.rhnConfig import CFG
from uyuni.common.usix import raise_with_tb

from . import rhnSQL


class InvalidSessionError(Exception):
    pass


class ExpiredSessionError(Exception):
    pass


# pylint: disable-next=missing-class-docstring
class Session:
    def __init__(self, session_id=None):
        self.session_id = session_id
        self.expires = None
        self.uid = None
        self.duration = None

    def generate(self, duration=None, web_user_id=None):
        # Grabs a session ID
        self.session_id = rhnSQL.Sequence("pxt_id_seq").next()
        self.duration = int(duration or CFG.SESSION_LIFETIME)
        self.web_user_id(web_user_id)
        return self

    def _get_secrets(self):
        # Reads the four secrets from the config file
        return list(
            map(
                # pylint: disable-next=consider-using-f-string
                lambda x, cfg=CFG: getattr(cfg, "session_secret_%s" % x),
                list(range(1, 5)),
            )
        )

    def get_secrets(self):
        # Validates the secrets from the config file
        secrets = self._get_secrets()
        if len(secrets) != len([_f for _f in secrets if _f]):
            # the list of secrets has unset items
            # pylint: disable-next=broad-exception-raised
            raise Exception("Secrets not set in the config file")
        return secrets

    def digest(self):
        if self.session_id is None:
            raise ValueError("session id not supplied")

        secrets = self.get_secrets()

        ctx = hashlib.new("sha256")
        ctx.update(
            ":".join(secrets[:2] + [str(self.session_id)] + secrets[2:]).encode()
        )

        return ctx.hexdigest()

    def get_session(self):
        # pylint: disable-next=consider-using-f-string
        return "%sx%s" % (self.session_id, self.digest())

    def web_user_id(self, uid=None):
        if uid:
            self.uid = uid
        return self.uid

    def load(self, session):
        arr = session.split("x", 1)
        if len(arr) != 2:
            raise InvalidSessionError("Invalid session string")

        digest = arr[1]
        if len(digest) != 64:
            raise InvalidSessionError("Invalid session string (wrong length)")

        try:
            self.session_id = int(arr[0])
        except ValueError:
            raise_with_tb(
                InvalidSessionError("Invalid session identifier"), sys.exc_info()[2]
            )

        if digest != self.digest():
            raise InvalidSessionError("Bad session checksum")

        h = rhnSQL.prepare(
            """
            select web_user_id, expires, value
              from pxtSessions
             where id = :session_id
        """
        )
        h.execute(session_id=self.session_id)

        row = h.fetchone_dict()
        if row:
            # Session is stored in the DB
            if time.time() < row["expires"]:
                # And it's not expired yet - good to go
                self.expires = row["expires"]
                self.uid = row["web_user_id"]
                return self

            # Old session - clean it up
            h = rhnSQL.prepare(
                """
                    delete from pxtSessions where id = :session_id
            """
            )
            h.execute(session_id=self.session_id)
            rhnSQL.commit()

        raise ExpiredSessionError("Session not found")

    def save(self):
        expires = int(time.time()) + self.duration

        h = rhnSQL.prepare(
            """
                insert into PXTSessions (id, web_user_id, expires, value)
                values (:id, :web_user_id, :expires, :value)
        """
        )
        h.execute(
            id=self.session_id, web_user_id=self.uid, expires=expires, value="RHNAPP"
        )
        rhnSQL.commit()
        return self


def load(session_string):
    return Session().load(session_string)


def generate(web_user_id=None, duration=None):
    return Session().generate(web_user_id=web_user_id, duration=duration).save()
