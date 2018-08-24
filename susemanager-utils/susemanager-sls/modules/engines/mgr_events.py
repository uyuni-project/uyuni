# -*- coding: utf-8 -*-
'''
A simple test engine, not intended for real use but as an example
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
import salt.utils.json

# Import third-party libs
import tornado.ioloop

log = logging.getLogger(__name__)

DEFAULT_SCALING_FACTOR = 0.1
DEFAULT_COMMIT_INTERVAL_MAX = 5
DEFAULT_COMMIT_INTERVAL_MIN = 1

# DELAY FACTOR VALUES
# 0.9 would be approx. averaging time difference between the last 10 events -> higher fluctuation
# 0.98 would be approx. averaging time difference between the last 50 events -> lower fluctuation
DEFAULT_DELAY_FACTOR = 0.1


def __virtual__():
    return HAS_PSYCOPG2


class Responder:
    def __init__(self, event_bus, config):
        self.config = config
        self.config.setdefault('commit_interval_min', DEFAULT_COMMIT_INTERVAL_MIN)
        self.config.setdefault('commit_interval_max', DEFAULT_COMMIT_INTERVAL_MAX)
        self.config.setdefault('scaling_factor', DEFAULT_SCALING_FACTOR)
        self.config.setdefault('delay_factor', DEFAULT_DELAY_FACTOR)
        self.counter = 0
        self.commit_interval = config['commit_interval_min']
        self.event_bus = event_bus
        self.commit_timer_handle = self.event_bus.io_loop.call_later(self.commit_interval, self.commit)
        self.timer = time.time()
        self._connect_to_database()

    def _connect_to_database(self):
        db_config = self.config.get('postgres_db')
        conn_string = "dbname='{dbname}' user='{user}' host='{host}' password='{password}'".format(**db_config)
        log.debug(conn_string)
        self.connection = psycopg2.connect(conn_string)
        self.cursor = self.connection.cursor()

    def _insert(self, tag, data):
        if any([
            fnmatch.fnmatch(tag, "salt/minion/*/start"),
            fnmatch.fnmatch(tag, "salt/job/*/ret/*"),
            fnmatch.fnmatch(tag, "salt/beacon/*"),
            fnmatch.fnmatch(tag, "suse/systemid/generate")
        ]):
            try:
                self.cursor.execute(
                    'INSERT INTO suseSaltEvent (tag, data) VALUES (%s, %s);',
                    (tag, salt.utils.json.dumps(data),)
                )
                log.debug("******** Adding event to queue: %s", tag)
                log.debug(self.cursor.query)
            except Exception as exp:
                # FIXME: Too broad!!!
                self.connection.rollback()
            self.counter += 1
        else:
            log.debug("******** DISCARDING: %s", tag)

    def debug_log(self):
        log.debug("####################################################################################")
        log.debug("******** queue_size: %s", self.counter)
        log.debug("******** fequency_commit_interval: %s", self.apply_limits(self.commit_interval))

    @tornado.gen.coroutine
    def add_event_to_queue(self, raw):
        tag, data = self.event_bus.unpack(raw, self.event_bus.serial)
        self._insert(tag, data)

    def apply_limits(self, commit_interval):
        return max(min(commit_interval, self.config['commit_interval_max']), self.config['commit_interval_min'])

    @tornado.gen.coroutine
    def commit(self):
        """
        Committing to the database.
        """
        log.debug("************** COMMIT! ")
        time_between_events = self.commit_interval / (self.counter or 0.1)
        commit_interval_limited = 1 / time_between_events * self.config['scaling_factor']
        self.commit_interval = self.config['delay_factor'] * self.commit_interval + (1 - self.config['delay_factor']) * commit_interval_limited
        self.debug_log()
        self.connection.commit()
        self.cursor.execute(
            'NOTIFY {};'.format(self.config['postgres_db']['notify_channel'])
        )
        self.counter = 0
        self.commit_timer_handle = self.event_bus.io_loop.call_later(
        self.apply_limits(self.commit_interval), self.commit)


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
    log.debug('mgr_events engine started')
    responder = Responder(event_bus, config)
    event_bus.set_event_handler(responder.add_event_to_queue)
    io_loop.start()
