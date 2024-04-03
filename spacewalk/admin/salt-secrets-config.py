#!/usr/bin/python3
#  pylint: disable=missing-module-docstring,invalid-name
# -*- coding: utf-8 -*-

import grp
import io
import os
import os.path
import pwd
import shutil
import yaml
import hashlib

from contextlib import redirect_stderr
from uyuni.common.context_managers import cfg_component


uyuni_roster_config = {}

with cfg_component("java") as CFG:
    thread_pool_size = CFG.salt_event_thread_pool_size
    try:
        uyuni_roster_config.update(
            {
                "ssh_connect_timeout": CFG.SALT_SSH_CONNECT_TIMEOUT,
            }
        )
    except (AttributeError, ValueError):
        pass

# To be moved into a config file in future
cert_location = "/etc/pki/trust/anchors"
if not os.path.isdir(cert_location):
    cert_location = "/etc/pki/ca-trust/source/anchors"

with cfg_component(component=None) as CFG:
    mgr_events_config = {
        "engines": [
            {
                "mgr_events": {
                    "postgres_db": {
                        "host": CFG.db_host,
                        "port": CFG.db_port,
                        "dbname": CFG.db_name,
                        "user": CFG.db_user,
                        "password": CFG.db_password,
                    },
                    "events": {"thread_pool_size": thread_pool_size},
                }
            }
        ]
    }

    salt_postgres_pillar = {
        "postgres": {
            "host": CFG.db_host,
            "port": CFG.db_port,
            "db": CFG.db_name,
            "user": CFG.db_user,
            "pass": CFG.db_password,
        }
    }

with cfg_component("web") as CFG:
    uyuni_roster_config.update(
        {
            "ssh_push_port_https": CFG.SSH_PUSH_PORT_HTTPS,
            "ssh_pre_flight_script": CFG.SSH_SALT_PRE_FLIGHT_SCRIPT,
            "ssh_use_salt_thin": CFG.SSH_USE_SALT_THIN == "true",
        }
    )
    if CFG.SSH_PUSH_SUDO_USER:
        uyuni_roster_config.update(
            {
                "ssh_push_sudo_user": CFG.SSH_PUSH_SUDO_USER,
            }
        )

with redirect_stderr(io.StringIO()) as f, cfg_component("java") as CFG:
    try:
        uyuni_roster_config.update(
            {
                "host": CFG.HOSTNAME,
            }
        )
    except AttributeError:
        pass

with cfg_component("server") as CFG:
    secret_hash = hashlib.sha512(CFG.secret_key.encode("ascii")).hexdigest()

os.umask(0o66)

# pylint: disable-next=unspecified-encoding
with open("/etc/salt/master.d/susemanager_engine.conf", "w") as f:
    f.write(
        yaml.safe_dump(mgr_events_config, default_flow_style=False, allow_unicode=True)
    )
    os.fchown(f.fileno(), pwd.getpwnam("salt").pw_uid, grp.getgrnam("salt").gr_gid)
    os.fchmod(f.fileno(), 0o640)

# pylint: disable-next=unspecified-encoding
with open("/etc/salt/master.d/susemanager_db.conf", "w") as f:
    f.write(
        yaml.safe_dump(
            salt_postgres_pillar, default_flow_style=False, allow_unicode=True
        )
    )
    os.fchown(f.fileno(), pwd.getpwnam("salt").pw_uid, grp.getgrnam("salt").gr_gid)
    os.fchmod(f.fileno(), 0o640)

# pylint: disable-next=unspecified-encoding
with open("/etc/salt/master.d/uyuni_roster.conf", "w") as f:
    uyuni_roster_cfg = {"uyuni_roster": uyuni_roster_config}
    if "ssh_pre_flight_script" in uyuni_roster_config:
        uyuni_roster_cfg.update({"ssh_run_pre_flight": True})
    f.write(
        yaml.safe_dump(uyuni_roster_cfg, default_flow_style=False, allow_unicode=True)
    )
    os.fchown(f.fileno(), pwd.getpwnam("salt").pw_uid, grp.getgrnam("salt").gr_gid)
    os.fchmod(f.fileno(), 0o640)

# pylint: disable-next=unspecified-encoding
with open("/etc/salt/master.d/susemanager-users.txt", "w") as f:
    f.write("admin:" + secret_hash)
    os.fchown(f.fileno(), pwd.getpwnam("salt").pw_uid, grp.getgrnam("salt").gr_gid)
    os.fchmod(f.fileno(), 0o400)

if not os.path.isdir("/etc/salt/pki/api"):
    os.mkdir("/etc/salt/pki/api")
    os.chown(
        "/etc/salt/pki/api", pwd.getpwnam("salt").pw_uid, grp.getgrnam("salt").gr_gid
    )
    os.chmod("/etc/salt/pki/api", 0o750)

if not all(
    [
        os.path.isfile(f)
        for f in [
            "/etc/salt/pki/api/salt-api.crt",
            "/etc/pki/trust/anchors/salt-api.crt",
            "/etc/salt/pki/api/salt-api.key",
        ]
    ]
):
    os.system(
        "openssl req -newkey rsa:4096 -x509 -sha256 -days 3650 -nodes -out /etc/salt/pki/api/salt-api.crt -keyout /etc/salt/pki/api/salt-api.key -subj '/CN=localhost'"
    )
    os.chown(
        "/etc/salt/pki/api/salt-api.crt",
        pwd.getpwnam("salt").pw_uid,
        grp.getgrnam("salt").gr_gid,
    )
    os.chmod("/etc/salt/pki/api/salt-api.crt", 0o600)
    os.chown(
        "/etc/salt/pki/api/salt-api.key",
        pwd.getpwnam("salt").pw_uid,
        grp.getgrnam("salt").gr_gid,
    )
    os.chmod("/etc/salt/pki/api/salt-api.key", 0o600)
    shutil.copyfile("/etc/salt/pki/api/salt-api.crt", cert_location + "/salt-api.crt")
    os.system("/usr/share/rhn/certs/update-ca-cert-trust.sh")
