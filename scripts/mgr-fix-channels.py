#! /usr/bin/python

import sys
from spacewalk.common.rhnConfig import CFG, initCFG
from spacewalk.server import rhnSQL
from spacewalk.common.rhnLog import initLOG, log_debug, log_error, log_stderr

initCFG('server.susemanager')
initLOG('/var/log/rhn/mgr-fix-channels.log', CFG.DEBUG or 1)
rhnSQL.initDB()

_query_snapshot_server_channels = rhnSQL.prepare("""
    SELECT X.server_id, rsc.channel_id
      FROM (select server_id, max(id) as snapshot_id from rhnSnapshot group by server_id) X
      JOIN rhnSnapshotChannel rsc ON rsc.snapshot_id = X.snapshot_id
     WHERE NOT EXISTS (SELECT distinct 1 FROM rhnServerChannel sc where sc.server_id = X.server_id)
  ORDER BY X.server_id, rsc.channel_id
""")
_query_snapshot_server_channels.execute()
data = _query_snapshot_server_channels.fetchall_dict() or {}

subscribe_channel = rhnSQL.Procedure("rhn_channel.subscribe_server")
rhnSQL.clear_log_id()
last_system_id = 0
for row in data:
    if last_system_id and last_system_id != row['server_id']:
        rhnSQL.commit()
    last_system_id = row['server_id']
    try:
        subscribe_channel(row['server_id'], row['channel_id'])
        msg = "Subscribed channel '%s' to server '%s'" % (row['channel_id'], row['server_id'])
        log_debug(1, msg)
        log_stderr(msg)
    except rhnSQL.SQLSchemaError, e:
        log_error("Failed to subscribe channel '%s' to server '%s'" %
                (row['channel_id'], row['server_id']))
        log_debug(1, "Reason: %s" % e)

rhnSQL.commit()
log_debug(1, "Finished")
log_stderr("Finished")
