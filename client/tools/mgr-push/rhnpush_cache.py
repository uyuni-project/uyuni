# pylint: disable=missing-module-docstring
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
# rhnpush_cache.py
#
# Classes that control the caching of usernames and passwords,
# along with the retrieval of the username and password.
#
# UserInfo - Instantiations of this class are pickled.
#            Cache won't be valid after a certain amount of time.
#
# CacheManager - Controls access to the cache.

import os
from rhnpush import utils

# This is the class that contains the session.


# pylint: disable-next=missing-class-docstring
class RHNPushSession:

    def __init__(self):
        self.location = os.path.join(utils.get_home_dir(), ".rhnpushcache")
        self.session = None

    # pylint: disable-next=invalid-name
    def setSessionString(self, session):
        self.session = session

    # pylint: disable-next=invalid-name
    def getSessionString(self):
        return self.session

    # pylint: disable-next=invalid-name
    def readSession(self):
        # pylint: disable-next=unspecified-encoding
        sessionfile = open(self.location, "r")
        self.session = sessionfile.read()
        sessionfile.close()

    # pylint: disable-next=invalid-name
    def writeSession(self):
        # pylint: disable-next=unspecified-encoding
        sessionfile = open(self.location, "w")
        sessionfile.write(self.session)
        sessionfile.close()
