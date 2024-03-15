"""
Read in the roster from Uyuni DB
"""

from collections import namedtuple
import hashlib

# pylint: disable-next=unused-import
import io
import logging

# Import Salt libs
import salt.cache
import salt.config
import salt.loader

try:
    import psycopg2
    from psycopg2.extras import NamedTupleCursor

    HAS_PSYCOPG2 = True
except ImportError:
    HAS_PSYCOPG2 = False

from yaml import dump


__virtualname__ = "uyuni"

log = logging.getLogger(__name__)

Proxy = namedtuple("Proxy", ["hostname", "port"])

JAVA_HOSTNAME = "localhost"
PROXY_SSH_PUSH_USER = "mgrsshtunnel"
PROXY_SSH_PUSH_KEY = (
    "/var/lib/spacewalk/" + PROXY_SSH_PUSH_USER + "/.ssh/id_susemanager_ssh_push"
)
SALT_SSH_CONNECT_TIMEOUT = 180
SSH_KEY_DIR = "/var/lib/salt/.ssh"
SSH_KEY_PATH = SSH_KEY_DIR + "/mgr_ssh_id"
SSH_PRE_FLIGHT_SCRIPT = None
SSH_PUSH_PORT = 22
SSH_PUSH_PORT_HTTPS = 1233
SSH_PUSH_SUDO_USER = None
SSH_USE_SALT_THIN = False
SSL_PORT = 443


# pylint: disable-next=invalid-name
def __virtual__():
    if not HAS_PSYCOPG2:
        return (False, "psycopg2 is not available")

    # pylint: disable-next=undefined-variable
    if __opts__.get("postgres") is None or __opts__.get("uyuni_roster") is None:
        return (False, "Uyuni is not installed or configured")

    return __virtualname__


class UyuniRoster:
    """
    The class to instantiate Uyuni connection and data gathering.
    It's used to keep the DB connection, cache object and others in one instance
    to prevent race conditions on loading the module with LazyLoader.
    """

    def __init__(self, db_config, uyuni_roster_config):
        self.config_hash = hashlib.sha256(
            str(uyuni_roster_config).encode(errors="backslashreplace")
        ).hexdigest()
        self.ssh_pre_flight_script = uyuni_roster_config.get("ssh_pre_flight_script")
        self.ssh_push_port_https = uyuni_roster_config.get(
            "ssh_push_port_https", SSH_PUSH_PORT_HTTPS
        )
        self.ssh_push_sudo_user = uyuni_roster_config.get("ssh_push_sudo_user", "root")
        self.ssh_use_salt_thin = uyuni_roster_config.get(
            "ssh_use_salt_thin", SSH_USE_SALT_THIN
        )
        self.ssh_connect_timeout = uyuni_roster_config.get(
            "ssh_connect_timeout", SALT_SSH_CONNECT_TIMEOUT
        )
        self.java_hostname = uyuni_roster_config.get("host", JAVA_HOSTNAME)

        if "port" in db_config:
            # pylint: disable-next=consider-using-f-string
            self.db_connect_str = "dbname='{db}' user='{user}' host='{host}' port='{port}' password='{pass}'".format(
                **db_config
            )
        else:
            self.db_connect_str = (
                # pylint: disable-next=consider-using-f-string
                "dbname='{db}' user='{user}' host='{host}' password='{pass}'".format(
                    **db_config
                )
            )

        log.trace("db_connect dbname: %s", db_config["db"])
        log.trace("db_connect   user: %s", db_config["user"])
        log.trace("db_connect   host: %s", db_config["host"])
        log.debug("ssh_pre_flight_script: %s", self.ssh_pre_flight_script)
        log.debug("ssh_push_port_https: %d", self.ssh_push_port_https)
        log.debug("ssh_push_sudo_user: %s", self.ssh_push_sudo_user)
        log.debug("ssh_use_salt_thin: %s", self.ssh_use_salt_thin)
        log.debug("salt_ssh_connect_timeout: %d", self.ssh_connect_timeout)
        log.debug("java.hostname: %s", self.java_hostname)

        # pylint: disable-next=undefined-variable
        self.cache = salt.cache.Cache(__opts__)
        cache_data = self.cache.fetch("roster/uyuni", "minions")
        if "minions" in cache_data and self.config_hash != cache_data.get(
            "config_hash"
        ):
            log.debug("Flushing the cache as the config has been changed")
            self.cache.flush("roster/uyuni")

        self._init_db()

    def _init_db(self):
        log.trace("_init_db")

        try:
            self.db_connection = psycopg2.connect(
                self.db_connect_str, cursor_factory=NamedTupleCursor
            )
            log.trace("_init_db: done")
        except psycopg2.OperationalError as err:
            # pylint: disable-next=logging-not-lazy
            log.warning(
                # pylint: disable-next=consider-using-f-string
                "Unable to connect to the Uyuni DB: \n%sWill try to reconnect later."
                % (err)
            )

    def _execute_query(self, *args, **kwargs):
        log.trace("_execute_query")

        try:
            cur = self.db_connection.cursor()
            cur.execute(*args, **kwargs)
            log.trace("_execute_query: ret %s", cur)
            return cur
        except psycopg2.OperationalError as err:
            # pylint: disable-next=logging-not-lazy,consider-using-f-string
            log.warning("Error during SQL prepare: %s" % (err))
            log.warning("Trying to reinit DB connection...")
            self._init_db()
            try:
                cur = self.db_connection.cursor()
                cur.execute(*args, **kwargs)
                return cur
            except psycopg2.OperationalError:
                log.warning("Unable to re-establish connection to the Uyuni DB")
                log.trace("_execute_query: ret None")
                return None

    def _get_ssh_options(
        self,
        minion_id=None,
        proxies=None,
        tunnel=False,
        user=None,
        ssh_push_port=SSH_PUSH_PORT,
    ):
        proxy_command = []
        i = 0
        for proxy in proxies:
            proxy_command.append(
                # pylint: disable-next=consider-using-f-string
                "/usr/bin/ssh -p {ssh_port} -i {ssh_key_path} -o StrictHostKeyChecking=no "
                "-o User={ssh_push_user} {in_out_forward} {proxy_host}".format(
                    ssh_port=proxy.port or 22,
                    ssh_key_path=SSH_KEY_PATH if i == 0 else PROXY_SSH_PUSH_KEY,
                    ssh_push_user=PROXY_SSH_PUSH_USER,
                    in_out_forward=(
                        f"-W {minion_id}:{ssh_push_port}"
                        if not tunnel and i == len(proxies) - 1
                        else ""
                    ),
                    proxy_host=proxy.hostname,
                )
            )
            i += 1
        if tunnel:
            proxy_command.append(
                "/usr/bin/ssh -i {pushKey} -o StrictHostKeyChecking=no "
                "-o User={user} -R {pushPort}:{proxy}:{sslPort} {minion} "
                "ssh -i {ownKey} -W {minion}:{sshPort} "
                "-o StrictHostKeyChecking=no -o User={user} {minion}".format(
                    pushKey=PROXY_SSH_PUSH_KEY,
                    user=user,
                    pushPort=self.ssh_push_port_https,
                    proxy=proxies[len(proxies) - 1].hostname,
                    sslPort=SSL_PORT,
                    minion=minion_id,
                    # pylint: disable-next=consider-using-f-string
                    ownKey="{}{}".format(
                        # pylint: disable-next=consider-using-f-string
                        "/root" if user == "root" else "/home/{}".format(user),
                        "/.ssh/mgr_own_id",
                    ),
                    sshPort=ssh_push_port,
                )
            )

        # pylint: disable-next=consider-using-f-string
        return ["ProxyCommand='{}'".format(" ".join(proxy_command))]

    # pylint: disable-next=dangerous-default-value
    def _get_ssh_minion(
        self, minion_id=None, proxies=[], tunnel=False, ssh_push_port=SSH_PUSH_PORT
    ):
        minion = {
            "host": minion_id,
            "user": self.ssh_push_sudo_user,
            "port": ssh_push_port,
            "timeout": self.ssh_connect_timeout,
        }
        if tunnel:
            minion.update({"minion_opts": {"master": minion_id}})
        if self.ssh_pre_flight_script:
            minion.update(
                {
                    "ssh_pre_flight": self.ssh_pre_flight_script,
                    "ssh_pre_flight_args": [
                        proxies[-1].hostname if proxies else self.java_hostname,
                        self.ssh_push_port_https if tunnel else SSL_PORT,
                        1 if self.ssh_use_salt_thin else 0,
                    ],
                }
            )
        if proxies:
            minion.update(
                {
                    "ssh_options": self._get_ssh_options(
                        minion_id=minion_id,
                        proxies=proxies,
                        tunnel=tunnel,
                        user=self.ssh_push_sudo_user,
                        ssh_push_port=ssh_push_port,
                    )
                }
            )
        elif tunnel:
            minion.update(
                {
                    # pylint: disable-next=consider-using-f-string
                    "remote_port_forwards": "%d:%s:%d"
                    % (self.ssh_push_port_https, self.java_hostname, SSL_PORT)
                }
            )

        return minion

    def targets(self):
        cache_data = self.cache.fetch("roster/uyuni", "minions")
        cache_fp = cache_data.get("fp", None)
        query = """
            SELECT ENCODE(SHA256(FORMAT('%s|%s|%s|%s|%s|%s|%s',
                          EXTRACT(EPOCH FROM MAX(S.modified)),
                          COUNT(S.id),
                          EXTRACT(EPOCH FROM MAX(SP.modified)),
                          COUNT(SP.proxy_server_id),
                          EXTRACT(EPOCH FROM MAX(SMI.modified)),
                          COUNT(SMI.server_id),
                          EXTRACT(EPOCH FROM MAX(PI.modified))
                   )::bytea), 'hex') AS fp
                   FROM rhnServer AS S
                   INNER JOIN suseMinionInfo AS SMI ON
                         (SMI.server_id=S.id)
                   LEFT JOIN rhnServerPath AS SP ON
                        (SP.server_id=S.id)
                   LEFT JOIN rhnProxyInfo as PI ON
                        (SP.proxy_server_id = PI.server_id)
                   WHERE S.contact_method_id IN (
                             SELECT SSCM.id
                             FROM suseServerContactMethod AS SSCM
                             WHERE SSCM.label IN ('ssh-push', 'ssh-push-tunnel')
                         )
        """
        h = self._execute_query(query)
        if h is not None:
            row = h.fetchone()
            if row and row.fp:
                log.trace("db cache fingerprint: %s", row.fp)
                new_fp = row.fp
                log.trace("cache check: old:%s new:%s", cache_fp, new_fp)
                if (
                    new_fp == cache_fp
                    and "minions" in cache_data
                    and cache_data["minions"]
                ):
                    log.debug("Returning the cached data")
                    return cache_data["minions"]
                else:
                    log.debug("Invalidate cache")
                    cache_fp = new_fp
        else:
            log.warning(
                "Unable to reconnect to the Uyuni DB. Returning the cached data instead."
            )
            return cache_data["minions"]

        ret = {}

        query = """
            SELECT S.id AS server_id,
                   SMI.minion_id AS minion_id,
                   SMI.ssh_push_port AS ssh_push_port,
                   SSCM.label='ssh-push-tunnel' AS tunnel,
                   SP.hostname AS proxy_hostname,
                   PI.ssh_port AS ssh_port
            FROM rhnServer AS S
            INNER JOIN suseServerContactMethod AS SSCM ON
                  (SSCM.id=S.contact_method_id)
            INNER JOIN suseMinionInfo AS SMI ON
                  (SMI.server_id=S.id)
            LEFT JOIN rhnServerPath AS SP ON
                 (SP.server_id=S.id)
            LEFT JOIN rhnProxyInfo as PI ON
                 (SP.proxy_server_id = PI.server_id)
            WHERE SSCM.label IN ('ssh-push', 'ssh-push-tunnel')
            ORDER BY S.id, SP.position DESC
        """

        h = self._execute_query(query)

        prow = None
        proxies = []

        row = h.fetchone()
        while True:
            if prow is not None and (row is None or row.server_id != prow.server_id):
                ret[prow.minion_id] = self._get_ssh_minion(
                    minion_id=prow.minion_id,
                    proxies=proxies,
                    tunnel=prow.tunnel,
                    ssh_push_port=int(prow.ssh_push_port or SSH_PUSH_PORT),
                )
            proxies = []
            if row is None:
                break
            if row.proxy_hostname:
                proxies.append(Proxy(row.proxy_hostname, row.ssh_port))
            prow = row
            row = h.fetchone()

        self.cache.store(
            "roster/uyuni",
            "minions",
            {"fp": cache_fp, "minions": ret, "config_hash": self.config_hash},
        )

        if log.isEnabledFor(logging.TRACE):
            log.trace("Uyuni DB roster:\n%s", dump(ret))

        return ret


# pylint: disable-next=unused-argument
def targets(tgt, tgt_type="glob", **kwargs):
    """
    Return the targets from the Uyuni DB
    """

    # pylint: disable-next=undefined-variable
    uyuni_roster = __context__.get("roster.uyuni")
    if uyuni_roster is None:
        uyuni_roster = UyuniRoster(
            # pylint: disable-next=undefined-variable
            __opts__.get("postgres"),
            # pylint: disable-next=undefined-variable
            __opts__.get("uyuni_roster"),
        )
        # pylint: disable-next=undefined-variable
        __context__["roster.uyuni"] = uyuni_roster

    # pylint: disable-next=undefined-variable
    return __utils__["roster_matcher.targets"](uyuni_roster.targets(), tgt, tgt_type)
