#
# Copyright (c) 2013 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
#

from spacewalk.server import repoindexHandler

def check_password(environ, user, password):
    return repoindexHandler.check_password(environ, user, password)


def application(environ, start_response):
    return repoindexHandler.handle(environ, start_response)
