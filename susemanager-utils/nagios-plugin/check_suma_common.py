#!/usr/bin/python
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

import sys
from spacewalk.server import rhnSQL
from spacewalk.common.rhnConfig import initCFG

#############################################################################

def resultExit(code, msg=None):
    rhnSQL.closeDB()
    if msg:
        print(msg)
    sys.exit(code)

#############################################################################

def check_one_arg(args):
    argc = len(args)
    if argc != 2:
        resultExit(3, "%s: Wrong argument count" % args[0])
    return(args[1])
    
def get_system_ID_by_name(systemname):
    initCFG('server.susemanager')
    rhnSQL.initDB()
    
    sql = rhnSQL.prepare("""select id from rhnServer where name = :sname""")
    sql.execute(sname = systemname)
    system = sql.fetchall_dict()
    
    if system:
        if len(system) > 1:
            resultExit(3, "System name \"%s\" not unique" % systemname)
        else:
            return(system[0]['id'])
    else:
        resultExit(3, "Unknown System: \"%s\"" % systemname)
    
