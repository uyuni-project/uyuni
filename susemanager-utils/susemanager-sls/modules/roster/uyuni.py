# -*- coding: utf-8 -*-
"""
Read in the roster from Uyuni DB
"""
from __future__ import absolute_import, print_function, unicode_literals

from contextlib import redirect_stderr
from yaml import dump

import hashlib
import io
import logging

# Import Salt libs
import salt.cache
import salt.config
import salt.loader

log = logging.getLogger(__name__)

try:
    from spacewalk.common.rhnConfig import CFG, initCFG
    from spacewalk.server import rhnSQL

    HAS_UYUNI = True
except ImportError:
    HAS_UYUNI = False
except TypeError:
    log.warning("Unable to read Uyuni config file: /etc/rhn/rhn.conf")
    HAS_UYUNI = False


__virtualname__ = "uyuni"

cache = None

COBBLER_HOST = "localhost"
PROXY_SSH_PUSH_USER = "mgrsshtunnel"
PROXY_SSH_PUSH_KEY = (
    "/var/lib/spacewalk/" + PROXY_SSH_PUSH_USER + "/.ssh/id_susemanager_ssh_push"
)
SALT_SSH_CONNECT_TIMEOUT = 180
SSH_KEY_DIR = "/srv/susemanager/salt/salt_ssh"
SSH_KEY_PATH = SSH_KEY_DIR + "/mgr_ssh_id"
SSH_PUSH_PORT = 22
SSH_PUSH_PORT_HTTPS = 1233
SSH_PUSH_SUDO_USER = None
SSL_PORT = 443


def __virtual__():
    global cache
    global SSH_PUSH_PORT_HTTPS
    global SALT_SSH_CONNECT_TIMEOUT
    global COBBLER_HOST

    if HAS_UYUNI:
        initCFG("web")
        try:
            SSH_PUSH_PORT_HTTPS = int(CFG.SSH_PUSH_PORT_HTTPS)
        except (AttributeError, ValueError):
            log.debug("Unable to get `ssh_push_port_https`. Fallback to default.")
        try:
            SSH_PUSH_SUDO_USER = CFG.SSH_PUSH_SUDO_USER
        except AttributeError:
            log.debug("Unable to get `ssh_push_sudo_user`. Fallback to default.")
        initCFG("java")
        try:
            SALT_SSH_CONNECT_TIMEOUT = int(CFG.SALT_SSH_CONNECT_TIMEOUT)
        except (AttributeError, ValueError):
            log.debug("Unable to get `salt_ssh_connect_timeout`. Fallback to default.")
        # Hacky solution to prevent output to the stderr about missing file
        with redirect_stderr(io.StringIO()) as f:
            initCFG("cobbler")
            s = f.getvalue()
            if s:
                log.debug("initCFG stderr: %s" % s)
            try:
                COBBLER_HOST = CFG.HOST
            except AttributeError:
                log.debug("Unable to get `cobbler.host`. Fallback to default.")
        log.debug("ssh_push_port_https: %d" % (SSH_PUSH_PORT_HTTPS))
        log.debug("salt_ssh_connect_timeout: %d" % (SALT_SSH_CONNECT_TIMEOUT))
        log.debug("cobbler.host: %s" % (COBBLER_HOST))

        _initDB()

        cache = salt.cache.Cache(__opts__)

    return (HAS_UYUNI and __virtualname__, "Uyuni is not installed on the system")


def _initDB():
    try:
        rhnSQL.initDB()
    except rhnSQL.sql_base.SQLConnectError as e:
        log.warning(
            "Unable to connect to the Uyuni DB: \n%s\nWill try to connect later." % (e)
        )


def _prepareSQL(*args, **kwargs):
    try:
        return rhnSQL.prepare(*args, **kwargs)
    except SystemError as e:
        log.warning("Error during SQL prepare: %s" % (e))
        log.warning("Trying to reinit DB connection...")
        _initDB()
        try:
            return rhnSQL.prepare(*args, **kwargs)
        except SystemError as e:
            log.warning("Unable to re-establish connection to the Uyuni DB")
            return None


def _getSSHOptions(
    minion_id=None, proxies=None, tunnel=False, user=None, ssh_push_port=SSH_PUSH_PORT
):
    proxyCommand = "ProxyCommand='"
    i = 0
    for proxy in proxies:
        proxyCommand += (
            "/usr/bin/ssh -i %s -o StrictHostKeyChecking=no -o User=%s %s %s "
            % (
                SSH_KEY_PATH if i == 0 else PROXY_SSH_PUSH_KEY,
                PROXY_SSH_PUSH_USER,
                "-W %s:%s" % (minion_id, ssh_push_port)
                if not tunnel and i == len(proxies) - 1
                else "",
                proxy,
            )
        )
        i += 1
    if tunnel:
        proxyCommand += (
            "/usr/bin/ssh -i {pushKey} -o StrictHostKeyChecking=no "
            "-o User={user} -R {pushPort}:{proxy}:{sslPort} {minion} "
            "ssh -i {ownKey} -W {minion}:{sshPort} "
            "-o StrictHostKeyChecking=no -o User={user} {minion}".format(
                pushKey=PROXY_SSH_PUSH_KEY,
                user=user,
                pushPort=SSH_PUSH_PORT_HTTPS,
                proxy=proxies[len(proxies) - 1],
                sslPort=SSL_PORT,
                minion=minion_id,
                ownKey="{}{}".format(
                    "/root" if user == "root" else "/home/{}".format(user),
                    "/.ssh/mgr_own_id",
                ),
                sshPort=ssh_push_port,
            )
        )
    proxyCommand += "'"

    return [proxyCommand]


def _getSSHMinion(
    minion_id=None, proxies=[], tunnel=False, ssh_push_port=SSH_PUSH_PORT
):
    user = SSH_PUSH_SUDO_USER if SSH_PUSH_SUDO_USER else "root"
    minion = {
        "host": minion_id,
        "user": user,
        "port": ssh_push_port,
        "timeout": SALT_SSH_CONNECT_TIMEOUT,
    }
    if tunnel:
        minion.update({"minion_opts": {"master": minion_id}})
    if proxies:
        minion.update(
            {
                "ssh_options": _getSSHOptions(
                    minion_id=minion_id,
                    proxies=proxies,
                    tunnel=tunnel,
                    user=user,
                    ssh_push_port=ssh_push_port,
                )
            }
        )
    elif tunnel:
        minion.update(
            {
                "remote_port_forwards": "%d:%s:%d"
                % (SSH_PUSH_PORT_HTTPS, COBBLER_HOST, SSL_PORT)
            }
        )

    return minion


def targets(tgt, tgt_type="glob", **kwargs):
    """
    Return the targets from the Uyuni DB
    """

    ret = {}

    cache_data = cache.fetch("roster/uyuni", "minions")
    cache_fp = cache_data.get("fp", None)
    query = """
        SELECT FORMAT('%s|%s|%s|%s',
                      EXTRACT(EPOCH FROM MAX(S.modified)),
                      COUNT(S.id),
                      EXTRACT(EPOCH FROM MAX(SP.modified)),
                      COUNT(SP.proxy_server_id)
               ) AS fp
               FROM rhnServer AS S
               INNER JOIN suseServerContactMethod AS SSCM ON
                     (SSCM.id=S.contact_method_id)
               INNER JOIN suseMinionInfo AS SMI ON
                     (SMI.server_id=S.id)
               LEFT JOIN rhnServerPath AS SP ON
                    (SP.server_id=S.id)
               WHERE S.contact_method_id IN (
                         SELECT SSCM.id
                         FROM suseServerContactMethod AS SCCM
                         WHERE SSCM.label IN ('ssh-push', 'ssh-push-tunnel')
                     )
    """
    h = _prepareSQL(query)
    if h is not None:
        h.execute()
        row = h.fetchone_dict()
        if row and "fp" in row:
            new_fp = hashlib.sha256(row["fp"].encode()).hexdigest()
            if new_fp == cache_fp and "minions" in cache_data and cache_data["minions"]:
                ret = cache_data["minions"]
                log.debug("Return the cached data")
                return __utils__["roster_matcher.targets"](ret, tgt, tgt_type)
            else:
                log.debug("Invalidate cache")
                cache_fp = new_fp
                ret = {}
    else:
        log.warning(
            "Unable to reconnect to the Uyuni DB. Returning cached data instead."
        )
        return __utils__["roster_matcher.targets"](ret, tgt, tgt_type)

    query = """
        SELECT S.id AS server_id,
               SMI.minion_id AS minion_id,
               SMI.ssh_push_port AS ssh_push_port,
               SSCM.label='ssh-push-tunnel' AS tunnel,
               SP.hostname AS proxy_hostname
        FROM rhnServer AS S
        INNER JOIN suseServerContactMethod AS SSCM ON
              (SSCM.id=S.contact_method_id)
        INNER JOIN suseMinionInfo AS SMI ON
              (SMI.server_id=S.id)
        LEFT JOIN rhnServerPath AS SP ON
             (SP.server_id=S.id)
        WHERE SSCM.label IN ('ssh-push', 'ssh-push-tunnel')
        ORDER BY S.id, SP.position DESC
    """

    h = _prepareSQL(query)
    h.execute()

    prow = None
    proxies = []

    row = h.fetchone_dict()
    while True:
        if prow is not None and (row is None or row["server_id"] != prow["server_id"]):
            ret[prow["minion_id"]] = _getSSHMinion(
                minion_id=prow["minion_id"],
                proxies=proxies,
                tunnel=prow["tunnel"],
                ssh_push_port=prow["ssh_push_port"],
            )
            proxies = []
        if row is None:
            break
        if row["proxy_hostname"]:
            proxies.append(row["proxy_hostname"])
        prow = row
        row = h.fetchone_dict()

    cache.store("roster/uyuni", "minions", {"fp": cache_fp, "minions": ret})
    log.trace("Uyuni DB roster:\n%s" % dump(ret))

    return __utils__["roster_matcher.targets"](ret, tgt, tgt_type)
