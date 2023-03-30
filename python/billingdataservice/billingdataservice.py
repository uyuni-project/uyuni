#
# Copyright (c) 2023 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

import json
from flask import Flask, abort
from spacewalk.server import rhnSQL
from spacewalk.common.rhnConfig import initCFG

# disable the development warning banner for newer Flask versions
# where maybe the environment variable WERKZEUG_RUN_MAIN does not
# work anymore
#
#from flask import cli
#cli.show_server_banner = lambda *_: None

initCFG('server.susemanager')
rhnSQL.initDB()

app = Flask(__name__)

@app.route("/")
def index():
    result = rhnSQL.fetchone_dict(rhnSQL.Statement("select '1' || '2' || '3' as testing from dual"))
    if result:
        return "online"
    abort(503) #Service Unavailable

@app.route("/metering")
def metering():
    st = rhnSQL.Statement("""
    SELECT r.dimension, r.count
      FROM susePaygDimensionResult r
     WHERE r.computation_id = (SELECT c.id
                                 FROM susePaygDimensionComputation c
                                WHERE c.success = true
                             ORDER BY c.timestamp DESC
                                LIMIT 1)
    """)
    result = rhnSQL.fetchall_dict(st) or []
    return json.dumps({ "dimensions" : result})
