# -*- coding: utf-8 -*-
'''
mgr_events.py is a SaltStack engine that writes selected events to SUSE
Manager's PostgreSQL database. Additionally, it sends notifications via the
LISTEN/NOTIFY mechanism to alert SUSE Manager of newly available events.

mgr_events.py tries to keep the I/O low in high load scenarios. Therefore
events are INSERTed once they come in, but not necessarily COMMITted
immediately.

The algorithm is an implementation of token bucket:
 - a COMMIT costs one token
 - initially, commit_burst tokens are available
 - every commit_interval seconds, one new token is generated
   (up to commit_burst)
 - when an event arrives and there are tokens available it is COMMITted
   immediately
 - when an event arrives but no tokens are available, the event is INSERTed but
   not COMMITted yet. COMMIT will happen as soon as a token is available

.. versionadded:: 2018.3.0

:depends: psycopg2

Minimal configuration example

.. code:: yaml

    engines:
      - mgr_events:
          postgres_db:
              dbname: susemanger
              user: spacewalk
              password: spacewalk
              host: localhost
              notify_channel: suseSaltEvent

Full configuration example

.. code:: yaml

    engines:
      - mgr_events:
          commit_interval: 1
          commit_burst: 100
          postgres_db:
              dbname: susemanger
              user: spacewalk
              password: spacewalk
              host: localhost
              port: 5432
              notify_channel: suseSaltEvent

Most of the values have a sane default. But we still need the login and host
for the PostgreSQL database. Only the `notify_channel` there is optional. The
default for host is 'localhost'.
'''

# Import python libs
from __future__ import absolute_import, print_function, unicode_literals
import logging
import time
import fnmatch
import hashlib

try:
    import psycopg2
    HAS_PSYCOPG2 = True
except ImportError:
    HAS_PSYCOPG2 = False

# Import salt libs
import salt.version
import salt.ext.tornado
import salt.utils.event
import json

log = logging.getLogger(__name__)

DEFAULT_COMMIT_INTERVAL = 1
DEFAULT_COMMIT_BURST = 100

def __virtual__():
    return HAS_PSYCOPG2


class Responder:
    def __init__(self, event_bus, config):
        self.config = config
        self.config.setdefault('commit_interval', DEFAULT_COMMIT_INTERVAL)
        self.config.setdefault('commit_burst', DEFAULT_COMMIT_BURST)
        self.config.setdefault('postgres_db', {})
        self.config['postgres_db'].setdefault('host', 'localhost')
        self.config['postgres_db'].setdefault('notify_channel', 'suseSaltEvent')
        self.counters = [0 for i in range(config['events']['thread_pool_size'] + 1)]
        self.tokens = config['commit_burst']
        self.event_bus = event_bus
        self._connect_to_database()
        self.event_bus.io_loop.call_later(config['commit_interval'], self.add_token)

    def _connect_to_database(self):
        db_config = self.config.get('postgres_db')
        if 'port' in db_config:
            conn_string = "dbname='{dbname}' user='{user}' host='{host}' port='{port}' password='{password}'".format(**db_config)
        else:
            conn_string = "dbname='{dbname}' user='{user}' host='{host}' password='{password}'".format(**db_config)
        log.debug("%s: connecting to database", __name__)
        while True:
            try:
                self.connection = psycopg2.connect(conn_string)
                break
            except psycopg2.OperationalError as err:
                log.error("%s: %s", __name__, err)
                log.error("%s: Retrying in 5 seconds.", __name__)
                time.sleep(5)
        self.cursor = self.connection.cursor()

    def _insert(self, tag, data):
        self.db_keepalive()
        if any([
            fnmatch.fnmatch(tag, "salt/minion/*/start"),
            fnmatch.fnmatch(tag, "salt/job/*/ret/*"),
            fnmatch.fnmatch(tag, "salt/beacon/*"),
            fnmatch.fnmatch(tag, "salt/engines/libvirt_events/*/domain/lifecycle"),
            fnmatch.fnmatch(tag, "salt/engines/libvirt_events/*/pool/lifecycle"),
            fnmatch.fnmatch(tag, "salt/engines/libvirt_events/*/network/lifecycle"),
            fnmatch.fnmatch(tag, "salt/engines/libvirt_events/*/pool/refresh"),
            fnmatch.fnmatch(tag, "salt/batch/*/start"),
            fnmatch.fnmatch(tag, "suse/manager/image_deployed"),
            fnmatch.fnmatch(tag, "suse/manager/image_synced"),
            fnmatch.fnmatch(tag, "suse/manager/pxe_update"),
            fnmatch.fnmatch(tag, "suse/systemid/generate")
        ]) and not self._is_salt_mine_event(tag, data) and not self._is_presence_ping(tag, data):
            queue = 0
            if 'id' in data:
                hash_sum = hashlib.md5(data.get("id").encode(self.connection.encoding)).hexdigest()[0:8]
                queue = int(hash_sum, 16) % self.config['events']['thread_pool_size'] + 1
            log.debug("%s: Adding event to queue %d -> %s", __name__, queue, tag)
            try:
                self.cursor.execute(
                    'INSERT INTO suseSaltEvent (minion_id, data, queue) VALUES (%s, %s, %s);',
                    (data.get("id"), json.dumps({'tag': tag, 'data': data}), queue)
                )
                self.counters[queue] += 1
                self.attempt_commit()
            except Exception as err:
                log.error("%s: %s", __name__, err)
                try:
                    self.connection.commit()
                except Exception as err2:
                    log.error("%s: Error commiting: %s", __name__, err2)
                    self.connection.close()
            finally:
                log.debug("%s: %s", __name__, self.cursor.query)
        else:
            log.debug("%s: Discarding event -> %s", __name__, tag)

    def trace_log(self):
        log.trace("%s: queues sizes -> %s", __name__, self.counters)
        log.trace("%s: tokens -> %s", __name__, self.tokens)

    def _is_salt_mine_event(self, tag, data):
        return fnmatch.fnmatch(tag, "salt/job/*/ret/*") and self._is_salt_mine_update(data)

    def _is_salt_mine_update(self, data):
        return data.get("fun") == "mine.update"

    def _is_presence_ping(self, tag, data):
        return fnmatch.fnmatch(tag, "salt/job/*/ret/*") and self._is_test_ping(data) and self._is_batch_mode(data)

    def _is_test_ping(self, data):
        return data.get("fun") == "test.ping"

    def _is_batch_mode(self, data):
        return data.get("metadata", {}).get("batch-mode")

    @salt.ext.tornado.gen.coroutine
    def add_event_to_queue(self, raw):
        # FIXME: Drop once we only use Salt >= 3004
        if salt.version.SaltStackVersion(*salt.version.__version_info__).major < 3004:
            tag, data = self.event_bus.unpack(raw, self.event_bus.serial)
        else:
            tag, data = self.event_bus.unpack(raw)
        self._insert(tag, data)

    def db_keepalive(self):
        if self.connection.closed:
            log.error("%s: Diconnected from database. Trying to reconnect...", __name__)
            self._connect_to_database()

    @salt.ext.tornado.gen.coroutine
    def add_token(self):
        self.tokens = min(self.tokens + 1, self.config['commit_burst'])
        self.attempt_commit()
        self.trace_log()
        self.event_bus.io_loop.call_later(self.config['commit_interval'], self.add_token)

    def attempt_commit(self):
        """
        Committing to the database.
        """
        self.db_keepalive()
        if self.tokens > 0 and sum(self.counters) > 0:
            log.debug("%s: commit", __name__)
            self.cursor.execute(
                "NOTIFY {}, '{}';".format(
                    self.config['postgres_db']['notify_channel'],
                    ",".join([str(counter) for counter in self.counters]))
            )
            self.connection.commit()
            self.counters = [0 for i in range(0, self.config['events']['thread_pool_size'] + 1)]
            self.tokens -=1

def start(**config):
    '''
    Listen to events and write them to the Postgres database
    '''
    io_loop = salt.ext.tornado.ioloop.IOLoop(make_current=False)
    io_loop.make_current()
    event_bus = salt.utils.event.get_master_event(
            __opts__,
            __opts__['sock_dir'],
            listen=True,
            io_loop=io_loop)
    responder = Responder(event_bus, config)
    event_bus.set_event_handler(responder.add_event_to_queue)
    io_loop.start()
