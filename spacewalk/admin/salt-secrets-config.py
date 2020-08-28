#!/usr/bin/python
# -*- coding: utf-8 -*-

import grp
import os
import pwd
import yaml
import hashlib
from spacewalk.common.rhnConfig import initCFG, CFG

initCFG('java')

thread_pool_size = CFG.salt_event_thread_pool_size

initCFG()

mgr_events_config = {
    "engines": [{
        "mgr_events": {
            "postgres_db": {
                "host": CFG.db_host,
                "port": CFG.db_port,
                "dbname": CFG.db_name,
                "user": CFG.db_user,
                "password": CFG.db_password
            },
            "events": {
                "thread_pool_size": thread_pool_size
            }
        }
    }]
}

initCFG('server')

secret_hash = hashlib.sha512(CFG.secret_key.encode('ascii')).hexdigest()

with open("/etc/salt/master.d/susemanager_engine.conf", "w") as f:
    f.write(yaml.safe_dump(mgr_events_config, default_flow_style=False, allow_unicode=True))
    os.fchown(f.fileno(), pwd.getpwnam("salt").pw_uid, grp.getgrnam("salt").gr_gid)
    os.fchmod(f.fileno(), 0o640)

with open("/etc/salt/master.d/susemanager-users.txt", "w") as f:
    f.write("admin:" + secret_hash)
    os.fchown(f.fileno(), pwd.getpwnam("salt").pw_uid, grp.getgrnam("salt").gr_gid)
    os.fchmod(f.fileno(), 0o400)
