#!/usr/bin/python
# -*- coding: utf-8 -*-

import grp
import os
import os.path
import pwd
import shutil
import yaml
import hashlib
from spacewalk.common.rhnConfig import initCFG, CFG

initCFG('java')

thread_pool_size = CFG.salt_event_thread_pool_size

# To be moved into a config file in future
cert_location = "/etc/pki/trust/anchors"
if not os.path.isdir(cert_location):
    cert_location = "/etc/pki/ca-trust/source/anchors"

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

os.umask(0o66)

with open("/etc/salt/master.d/susemanager_engine.conf", "w") as f:
    f.write(yaml.safe_dump(mgr_events_config, default_flow_style=False, allow_unicode=True))
    os.fchown(f.fileno(), pwd.getpwnam("salt").pw_uid, grp.getgrnam("salt").gr_gid)
    os.fchmod(f.fileno(), 0o640)

with open("/etc/salt/master.d/susemanager-users.txt", "w") as f:
    f.write("admin:" + secret_hash)
    os.fchown(f.fileno(), pwd.getpwnam("salt").pw_uid, grp.getgrnam("salt").gr_gid)
    os.fchmod(f.fileno(), 0o400)

if not os.path.isdir("/etc/salt/pki/api"):
    os.mkdir("/etc/salt/pki/api")
    os.chown("/etc/salt/pki/api", pwd.getpwnam("salt").pw_uid, grp.getgrnam("salt").gr_gid)
    os.chmod("/etc/salt/pki/api", 0o750)

if (not all([os.path.isfile(f) for f in ["/etc/salt/pki/api/salt-api.crt", "/etc/pki/trust/anchors/salt-api.crt", "/etc/salt/pki/api/salt-api.key"]])):
    os.system("openssl req -newkey rsa:4096 -x509 -sha256 -days 3650 -nodes -out /etc/salt/pki/api/salt-api.crt -keyout /etc/salt/pki/api/salt-api.key -subj '/CN=localhost'")
    os.chown("/etc/salt/pki/api/salt-api.crt", pwd.getpwnam("salt").pw_uid, grp.getgrnam("salt").gr_gid)
    os.chmod("/etc/salt/pki/api/salt-api.crt", 0o600)
    os.chown("/etc/salt/pki/api/salt-api.key", pwd.getpwnam("salt").pw_uid, grp.getgrnam("salt").gr_gid)
    os.chmod("/etc/salt/pki/api/salt-api.key", 0o600)
    shutil.copyfile("/etc/salt/pki/api/salt-api.crt", cert_location + "/salt-api.crt")
    # Detect CA management tool.
    if os.system("update-ca-certificates"):
        print('Using "update-ca-trust" instead of "update-ca-certificates".')
        os.system("update-ca-trust")

