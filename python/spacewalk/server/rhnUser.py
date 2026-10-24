#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2008--2017 Red Hat, Inc.
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
#
# Stuff for handling Certificates and Servers
#

import hmac
import hashlib
import os as _os
import base64 as _base64

# Global Modules
from rhn.UserDictCase import UserDictCase
from spacewalk.common.rhnLog import log_debug, log_error
from spacewalk.common.rhnConfig import CFG
from spacewalk.common.rhnException import rhnFault, rhnException
from spacewalk.common.rhnTranslate import _

from spacewalk.server import rhnSQL
from spacewalk.server import rhnSession


class User:
    """Main User class"""

    def __init__(self, username, password):
        # compatibilty with the rest of the code
        self.username = username

        # placeholders for the table schemas
        # web_contact
        self.contact = rhnSQL.Row("web_contact", "id")
        self.contact["login"] = username
        self.contact["password"] = password
        # web_customer
        self.customer = rhnSQL.Row("web_customer", "id")
        self.customer["name"] = username
        self.customer["password"] = password
        # web_user_personal_info
        self.__init_info()
        # web_user_contact_permission
        self.__init_perms()
        # web_user_site_info
        self.__init_site()
        self._session = None

    def __init_info(self):
        """init web_user_personal_info"""
        # web_user_personal_info
        self.info = rhnSQL.Row("web_user_personal_info", "web_user_id")
        self.info["first_names"] = "Valued"
        self.info["last_name"] = "Customer"
        self.info["prefix"] = "Mr."

    def __init_perms(self):
        """init web_user_contact_permission"""
        # web_user_contact_permission
        self.perms = rhnSQL.Row("web_user_contact_permission", "web_user_id")
        self.perms["email"] = "Y"
        self.perms["mail"] = "Y"
        self.perms["call"] = "Y"
        self.perms["fax"] = "Y"

    def __init_site(self):
        """init web_user_site_info"""
        # web_user_site_info
        self.site = rhnSQL.Row("web_user_site_info", "id")
        self.site["city"] = "."
        self.site["address1"] = "."
        self.site["country"] = "US"
        self.site["notes"] = "Entry created by Spacewalk registration process"

    def check_password(self, password, allow_read_only=False):
        """simple check for a password that might become more complex sometime"""
        if not allow_read_only and is_user_read_only(self.contact["login"]):
            raise rhnFault(702)
        good_pwd = str(self.contact["password"])
        if CFG.pam_auth_service:
            # a PAM service is defined
            # We have to check the user's rhnUserInfo.use_pam_authentication
            # XXX Should we create yet another __init_blah function?
            # since it's the first time we had to lool at rhnUserInfo,
            # I'll assume it's not something to happen very frequently,
            # so I'll use a query for now
            # - misa
            #
            h = rhnSQL.prepare("""
                select ui.use_pam_authentication
                from web_contact w, rhnUserInfo ui
                where w.login_uc = UPPER(:login)
                and w.id = ui.user_id""")
            h.execute(login=self.contact["login"])
            data = h.fetchone_dict()
            if not data:
                # This should not happen
                # pylint: disable-next=consider-using-f-string
                raise rhnException("No entry found for user %s" % self.contact["login"])
            if data["use_pam_authentication"] == "Y":
                # use PAM
                # pylint: disable-next=import-outside-toplevel
                from . import rhnAuthPAM

                return rhnAuthPAM.check_password(
                    self.contact["login"], password, CFG.pam_auth_service
                )
        # If the entry in rhnUserInfo is 'N', perform regular authentication
        ret = check_password(password, good_pwd)
        if ret and not self.contact["password"].startswith(_PBKDF2_PREFIX):
            # If successfully authenticated and the stored password is not yet
            # using PBKDF2-SHA256, upgrade it transparently on next login.
            self.contact["password"] = encrypt_password(password)
            self.contact.save()
            rhnSQL.commit()
        return ret

    def set_org_id(self, org_id):
        if not org_id:
            raise rhnException("Invalid org_id requested for user", org_id)
        self.contact["org_id"] = int(org_id)
        self.customer.load(int(org_id))

    def getid(self):
        if not self.contact.has_key("id"):
            userid = rhnSQL.Sequence("web_contact_id_seq")()
            self.contact.data["id"] = userid  # kind of illegal, but hey!
        else:
            userid = self.contact["id"]
        return userid

    def set_contact_perm(self, name, value):
        """handling of contact permissions"""
        if not name:
            return -1
        n = name.lower()
        v = "N"
        if value:
            v = "Y"
        if n == "contact_phone":
            self.perms["call"] = v
        elif n == "contact_mail":
            self.perms["mail"] = v
        elif n == "contact_email":
            self.perms["email"] = v
        elif n == "contact_fax":
            self.perms["fax"] = v
        return 0

    def set_info(self, name, value):
        """set a certain value for the userinfo field. This is BUTT ugly."""
        log_debug(3, name, value)
        # translation from what the client send us to real names of the fields
        # in the tables.
        mapping = {"first_name": "first_names", "position": "title", "title": "prefix"}
        if not name:
            return -1
        name = name.lower()
        # pylint: disable-next=unidiomatic-typecheck
        if type(value) == type(""):
            value = value.strip()
        # We have to watch over carefully for different field names
        # being sent from rhn_register
        changed = 0

        # translation
        # pylint: disable-next=consider-iterating-dictionary
        if name in list(mapping.keys()):
            name = mapping[name]
        # Some fields can not have null string values
        if name in [
            "first_names",
            "last_name",
            "prefix",  # personal_info
            "address1",
            "city",
            "country",
        ]:  # site_info
            # we require something of it
            if len(str(value)) == 0:
                return -1
        # fields in personal_info (and some in site)
        if name in [
            "last_name",
            "first_names",
            "company",
            "phone",
            "fax",
            "email",
            "title",
        ]:
            self.info[name] = value[:128]
            changed = 1
        elif name == "prefix":
            values = ["Mr.", "Mrs.", "Ms.", "Dr.", "Hr.", "Sr.", " "]
            # Now populate a dictinary of valid values
            valids = UserDictCase()
            for v in values:  # initialize from good values, with and w/o the dot
                valids[v] = v
                valids[v[:-1]] = v
            # commonly encountered values
            valids["Miss"] = "Miss"
            valids["Herr"] = "Hr."
            valids["Sig."] = "Sr."
            valids["Sir"] = "Mr."
            # Now check it out
            if valids.has_key(value):
                self.info["prefix"] = valids[value]
                changed = 1
            else:
                # pylint: disable-next=consider-using-f-string
                log_error("Unknown prefix value `%s'. Assumed `Mr.' instead" % value)
                self.info["prefix"] = "Mr."
                changed = 1

        # fields in site
        if name in ["phone", "fax", "zip"]:
            self.site[name] = value[:32]
            changed = 1
        elif name in [
            "city",
            "country",
            "alt_first_names",
            "alt_last_name",
            "address1",
            "address2",
            "email",
            "last_name",
            "first_names",
        ]:
            if name == "last_name":
                self.site["alt_last_name"] = value
                changed = 1
            elif name == "first_names":
                self.site["alt_first_names"] = value
                changed = 1
            else:
                self.site[name] = value[:128]
                changed = 1
        elif name in ["state"]:  # stupid people put stupid things in here too
            self.site[name] = value[:60]
            changed = 1
        if not changed:
            # pylint: disable-next=consider-using-f-string
            log_error("SET_INFO: Unknown info `%s' = `%s'" % (name, value))
        return 0

    def get_roles(self):
        user_id = self.getid()

        h = rhnSQL.prepare("""
            select ugt.label as role
              from rhnUserGroup ug,
                   rhnUserGroupType ugt,
                   rhnUserGroupMembers ugm
             where ugm.user_id = :user_id
               and ugm.user_group_id = ug.id
               and ug.group_type = ugt.id
        """)
        h.execute(user_id=user_id)
        return [x["role"] for x in h.fetchall_dict() or []]

    def reload(self, user_id):
        """Reload the current data from the SQL database using the given id"""
        log_debug(3, user_id)

        # If we can not load these we have a fatal condition
        if not self.contact.load(user_id):
            raise rhnException("Could not find contact record for id", user_id)
        if not self.customer.load(self.contact["org_id"]):
            raise rhnException(
                "Could not find org record",
                # pylint: disable-next=consider-using-f-string
                "user_id = %s" % user_id,
                # pylint: disable-next=consider-using-f-string
                "org_id = %s" % self.contact["org_id"],
            )
        # These other ones are non fatal because we can create dummy records
        if not self.info.load(user_id):
            self.__init_info()
        if not self.perms.load(user_id):
            self.__init_perms()
        # The site info is trickier, we need to find it first
        if not self.site.load_sql("web_user_id = :userid", {"userid": user_id}):
            self.__init_site()
        # Fix the username
        self.username = self.contact["login"]
        return 0

    def create_session(self):
        if self._session:
            return self._session

        self.session = rhnSession.generate(web_user_id=self.getid())
        return self.session


def auth_username_password(username, password):
    # hrm.  it'd be nice to move importlib.userAuth stuff here
    user = search(username)

    if not user:
        raise rhnFault(2, _("Invalid username/password combination"))

    if not user.check_password(password):
        raise rhnFault(2, _("Invalid username/password combination"))

    return user


def session_reload(session_string):
    log_debug(4, session_string)
    session = rhnSession.load(session_string)
    web_user_id = session.uid
    if not web_user_id:
        raise rhnSession.InvalidSessionError("No user associated with session")

    u = User("", "")
    ret = u.reload(web_user_id)
    if ret != 0:
        # Something horked
        raise rhnFault(10)
    return u


def get_user_id(username):
    """search for an userid"""
    username = str(username)
    h = rhnSQL.prepare("""
    select w.id from web_contact w
    where w.login_uc = upper(:username)
    """)
    h.execute(username=username)
    data = h.fetchone_dict()
    if data:
        return data["id"]
    return None


def search(user):
    """search the database for a user"""
    log_debug(3, user)
    userid = get_user_id(user)
    if not userid:  # no user found
        return None
    ret = User(user, "")
    # pylint: disable-next=unnecessary-negation
    if ret.reload(userid) != 0:
        # something horked during reloading entry from database
        # we can not realy say that the entry does not exist...
        raise rhnFault(10)
    return ret


def is_user_disabled(user):
    log_debug(3, user)
    username = str(user)
    h = rhnSQL.prepare("""
    select 1 from rhnWebContactDisabled
    where login_uc = upper(:username)
    """)
    h.execute(username=username)
    row = h.fetchone_dict()
    if row:
        return 1
    return 0


def is_user_read_only(user):
    log_debug(3, user)
    username = str(user)
    h = rhnSQL.prepare("""
    select 1 from web_contact
    where login_uc = upper(:username)
    and read_only = 'Y'
    """)
    h.execute(username=username)
    row = h.fetchone_dict()
    if row:
        return 1
    return 0


_PBKDF2_ITERATIONS = 600000
_PBKDF2_MIN_ITERATIONS = _PBKDF2_ITERATIONS
_PBKDF2_MAX_ITERATIONS = 2_000_000
_PBKDF2_PREFIX = "$pbkdf2-sha256$"


def _b64_pad(s):
    return s + "=" * (-len(s) % 4)


def check_password(key, pwd1):
    """Validates the given key against the encrypted password pwd1"""
    # We don't trust the origin for key, so stringify it
    key = str(key)
    if len(key) == 0:
        # No zero-length passwords accepted
        return 0

    if pwd1.startswith(_PBKDF2_PREFIX):  # PBKDF2-SHA256 password
        # format: $pbkdf2-sha256$<iterations>$<b64salt>$<b64hash>
        try:
            _, _, iter_s, b64salt, b64hash = pwd1.split("$")
            iterations = int(iter_s)
            if not _PBKDF2_MIN_ITERATIONS <= iterations <= _PBKDF2_MAX_ITERATIONS:
                raise ValueError("iteration count out of range")
            raw_salt = _base64.b64decode(_b64_pad(b64salt))
            dk = hashlib.pbkdf2_hmac("sha256", key.encode(), raw_salt, iterations)
            stored = _base64.b64decode(_b64_pad(b64hash))
            if hmac.compare_digest(dk, stored):
                return 1
        except (ValueError, TypeError):
            log_debug(4, "PBKDF2 password verification failed")
            return 0
    elif pwd1.find("$5") == 0:  # legacy SHA-256 crypt(3) password
        if pwd1 == encrypt_password(key, pwd1):
            return 1

    log_debug(4, "Encrypted password doesn't match")
    return 0  # invalid


def encrypt_password(key, salt=None):
    """Hash key with PBKDF2-SHA256. The salt param is kept only for legacy crypt(3) verification."""
    if salt and str(salt).find("$5$") == 0:
        # Legacy read-side path for existing SHA-256 crypt(3) hashes ($5$…).
        import crypt as _crypt  # pylint: disable=import-outside-toplevel,deprecated-module

        return _crypt.crypt(key, str(salt))

    # Stored format: $pbkdf2-sha256$<iterations>$<b64salt>$<b64hash>
    # 32-byte salt + 32-byte key → 43 base64 chars each (padding stripped).
    # Total length ≤ 109 chars, fits in the 110-char web_contact.password column.
    raw_salt = _os.urandom(32)
    dk = hashlib.pbkdf2_hmac("sha256", key.encode(), raw_salt, _PBKDF2_ITERATIONS)
    b64salt = _base64.b64encode(raw_salt).decode().rstrip("=")
    b64hash = _base64.b64encode(dk).decode().rstrip("=")
    return f"{_PBKDF2_PREFIX}{_PBKDF2_ITERATIONS}${b64salt}${b64hash}"
