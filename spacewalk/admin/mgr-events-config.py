#!/usr/bin/python
# -*- coding: utf-8 -*-

import grp
import os
import pwd
import yaml
from spacewalk.common.rhnConfig import initCFG, CFG

initCFG()

config = {
    "engines": [{
        "mgr_events": {
            "postgres_db": {
                "host": CFG.db_host,
                "port": CFG.db_port,
                "dbname": CFG.db_name,
                "user": CFG.db_user,
                "password": CFG.db_password
            }
        }
    }]
}

with open("/etc/salt/master.d/susemanager_engine.conf", "w") as f:
    f.write(yaml.safe_dump(config, default_flow_style=False, allow_unicode=True))
    os.fchown(f.fileno(), pwd.getpwnam("salt").pw_uid, grp.getgrnam("salt").gr_gid)
    os.fchmod(f.fileno(), 0o600)
