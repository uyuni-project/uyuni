# -*- coding: utf-8 -*-
'''
mgr_events.py is a SaltStack engine that writes selected events to the
SUSE Manager (PostgreSQL) database and also sends a notifications to a
PostgreSQL pub/sub channel. This allow SUSE Manager to only query for
events if some are coming in.

mgr_events.py tries to keep the I/O low in high load scenarios. Therefore
events are INSERTed once they come in, but only commited in a load
dependend commit interval. This commit interval gets recalculated every
time we commit and takes the amount of events INSERTed into account. This can
be tweaked with the `delay_factor`. A shorter `delay_factor` takes less history
into account when calculating the commit interval.
The formula to approximate how many previous values are considered
given a delay_factor value is: 1/(1-delay_factor).

+--------------+-------------------------------+
| delay_factor | approx no. of previous values |
+--------------+-------------------------------+
|     0.98     |             50                |
+--------------+-------------------------------+
|     0.9      |             10                |
+--------------+-------------------------------+
|     0.5      |              2                |
+--------------+-------------------------------+
|     0.01     |              1                |
+--------------+-------------------------------+

Since we are by default operating in a seconds scale, we need the
`scaling_factor` to get this down a little bit.
`commit_interval_max` and `commit_interval_min` are the maximum and minimum
in seconds. Everything above and below will be capped.

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

try:
    import psycopg2
    HAS_PSYCOPG2 = True
except ImportError:
    HAS_PSYCOPG2 = False

# Import salt libs
import salt.utils.event
import json

# Import third-party libs
import tornado.ioloop

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
        self.counter = 0
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
            fnmatch.fnmatch(tag, "suse/systemid/generate")
        ]):
            log.debug("%s: Adding event to queue -> %s", __name__, tag)
            try:
                self.cursor.execute(
                    'INSERT INTO suseSaltEvent (data) VALUES (%s);',
                    (json.dumps({'tag': tag, 'data': data}),)
                )
                self.counter += 1
                self.attempt_commit()
            except Exception as err:
                log.error("%s: %s", __name__, err)
            finally:
                log.debug("%s: %s", __name__, self.cursor.query)
        else:
            log.debug("%s: Discarding event -> %s", __name__, tag)

    def debug_log(self):
        log.debug("%s: queue_size -> %s", __name__, self.counter)
        log.debug("%s: tokens -> %s", __name__, self.tokens)

    @tornado.gen.coroutine
    def add_event_to_queue(self, raw):
        tag, data = self.event_bus.unpack(raw, self.event_bus.serial)
        self._insert(tag, data)

    def db_keepalive(self):
        if self.connection.closed:
            log.error("%s: Diconnected from database. Trying to reconnect...", __name__)
            self._connect_to_database()

    @tornado.gen.coroutine
    def add_token(self):
        self.tokens = min(self.tokens + 1, self.config['commit_burst'])
        self.attempt_commit()
        self.debug_log()
        self.event_bus.io_loop.call_later(self.config['commit_interval'], self.add_token)

    def attempt_commit(self):
        """
        Committing to the database.
        """
        self.db_keepalive()
        if self.tokens > 0 and self.counter > 0:
            log.debug("%s: commit", __name__)
            self.cursor.execute(
                'NOTIFY {};'.format(self.config['postgres_db']['notify_channel'])
            )
            self.connection.commit()
            self.counter = 0
            self.tokens -=1

def start(**config):
    '''
    Listen to events and write them to the Postgres database
    '''
    io_loop = tornado.ioloop.IOLoop()
    event_bus = salt.utils.event.get_master_event(
            __opts__,
            __opts__['sock_dir'],
            listen=True,
            io_loop=io_loop)
    responder = Responder(event_bus, config)
    event_bus.set_event_handler(responder.add_event_to_queue)
    io_loop.start()
