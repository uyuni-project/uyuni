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

try:
    import psycopg2

    HAS_PSYCOPG2 = True
except ImportError:
    HAS_PSYCOPG2 = False


__virtualname__ = "uyuni"

log = logging.getLogger(__name__)

cache = None

DB_CONNECT_STR = None
DB_CONNECTION = None
DB_CURSOR = None

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
    global SSH_PUSH_SUDO_USER
    global SALT_SSH_CONNECT_TIMEOUT
    global COBBLER_HOST
    global DB_CONNECT_STR

    if not HAS_PSYCOPG2:
        return (False, "psycopg2 is not available")

    db_config = __opts__.get("postgres")
    uyuni_roster_config = __opts__.get("uyuni_roster")

    if db_config and uyuni_roster_config:
        SSH_PUSH_PORT_HTTPS = uyuni_roster_config.get(
            "ssh_push_port_https", SSH_PUSH_PORT_HTTPS
        )
        SSH_PUSH_SUDO_USER = uyuni_roster_config.get(
            "ssh_push_sudo_user", SSH_PUSH_SUDO_USER
        )
        SALT_SSH_CONNECT_TIMEOUT = uyuni_roster_config.get(
            "ssh_connect_timeout", SALT_SSH_CONNECT_TIMEOUT
        )
        COBBLER_HOST = uyuni_roster_config.get("host", COBBLER_HOST)

        if "port" in db_config:
            DB_CONNECT_STR = "dbname='{db}' user='{user}' host='{host}' port='{port}' password='{pass}'".format(
                **db_config
            )
        else:
            DB_CONNECT_STR = (
                "dbname='{db}' user='{user}' host='{host}' password='{pass}'".format(
                    **db_config
                )
            )

        log.trace("db_connect string: %s" % DB_CONNECT_STR)
        log.debug("ssh_push_port_https: %d" % SSH_PUSH_PORT_HTTPS)
        log.debug("ssh_push_sudo_user: %s" % SSH_PUSH_SUDO_USER)
        log.debug("salt_ssh_connect_timeout: %d" % SALT_SSH_CONNECT_TIMEOUT)
        log.debug("cobbler.host: %s" % COBBLER_HOST)

        _initDB()

        cache = salt.cache.Cache(__opts__)

        return __virtualname__

    return (False, "Uyuni is not installed on the system")


def _initDB():
    global DB_CONNECT_STR
    global DB_CONNECTION
    global DB_CURSOR

    try:
        DB_CONNECTION = psycopg2.connect(DB_CONNECT_STR)
        DB_CURSOR = DB_CONNECTION.cursor()
    except psycopg2.OperationalError as err:
        log.warning(
            "Unable to connect to the Uyuni DB: \n%sWill try to reconnect later."
            % (err)
        )


def _executeQuery(*args, **kwargs):
    global DB_CONNECTION
    global DB_CURSOR

    if DB_CURSOR is None:
        _initDB()
        if DB_CURSOR is None:
            return None

    try:
        DB_CURSOR.execute(*args, **kwargs)
        return DB_CURSOR
    except psycopg2.OperationalError as err:
        log.warning("Error during SQL prepare: %s" % (err))
        log.warning("Trying to reinit DB connection...")
        _initDB()
        try:
            DB_CURSOR.execute(*args, **kwargs)
            return DB_CURSOR
        except psycopg2.OperationalError:
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
               INNER JOIN suseMinionInfo AS SMI ON
                     (SMI.server_id=S.id)
               LEFT JOIN rhnServerPath AS SP ON
                    (SP.server_id=S.id)
               WHERE S.contact_method_id IN (
                         SELECT SSCM.id
                         FROM suseServerContactMethod AS SSCM
                         WHERE SSCM.label IN ('ssh-push', 'ssh-push-tunnel')
                     )
    """
    h = _executeQuery(query)
    if h is not None:
        row = h.fetchone()
        if row and row[0]:
            new_fp = hashlib.sha256(row[0].encode()).hexdigest()
            if new_fp == cache_fp and "minions" in cache_data and cache_data["minions"]:
                log.debug("Return the cached data")
                return __utils__["roster_matcher.targets"](
                    cache_data["minions"], tgt, tgt_type
                )
            else:
                log.debug("Invalidate cache")
                cache_fp = new_fp
                ret = {}
    else:
        log.warning(
            "Unable to reconnect to the Uyuni DB. Returning cached data instead."
        )
        return __utils__["roster_matcher.targets"](cache_data["minions"], tgt, tgt_type)

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

    h = _executeQuery(query)

    prow = None
    proxies = []

    row = h.fetchone()
    while True:
        if prow is not None and (row is None or row[0] != prow[0]):
            ret[prow[1]] = _getSSHMinion(
                minion_id=prow[1],
                proxies=proxies,
                tunnel=prow[3],
                ssh_push_port=int(prow[2]),
            )
            proxies = []
        if row is None:
            break
        if row[4]:
            proxies.append(row[4])
        prow = row
        row = h.fetchone()

    cache.store("roster/uyuni", "minions", {"fp": cache_fp, "minions": ret})
    log.trace("Uyuni DB roster:\n%s" % dump(ret))

    return __utils__["roster_matcher.targets"](ret, tgt, tgt_type)
