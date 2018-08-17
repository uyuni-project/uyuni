# -*- coding: utf-8 -*-
'''
A simple test engine, not intended for real use but as an example
'''

# Import python libs
from __future__ import absolute_import, print_function, unicode_literals
import logging
import time
import psycopg2

# Import salt libs
import salt.utils.event
import salt.utils.json

# Import third-party libs
import tornado.ioloop

log = logging.getLogger(__name__)

SCALING_FACTOR = 0.1
COMMIT_INTERVAL_MAX = 5
COMMIT_INTERVAL_MIN = 1

# DELAY FACTOR VALUES
# 0.9 would be approx. averaging time difference between the last 10 events -> higher fluctuation
# 0.98 would be approx. averaging time difference between the last 50 events -> lower fluctuation
DELAY_FACTOR = 0.1


class Responder:
    def __init__(self, event_bus):
        self.counter = 0
        self.commit_interval = COMMIT_INTERVAL_MIN
        self.event_bus = event_bus
        self.commit_timer_handle = self.event_bus.io_loop.call_later(self.commit_interval, self.commit)
        self.timer = time.time()
        self._connect_to_database()

    def _connect_to_database(self):
        self.connection = psycopg2.connect("dbname='susemanager' user='spacewalk' host='localhost' password='spacewalk'")
        self.cursor = self.connection.cursor()

    def _insert(self, tag, data):
        self.cursor.execute(
            'INSERT INTO suseSaltEvent (tag, data) VALUES (%s, %s);',
            (tag, salt.utils.json.dumps(data))
        )
        log.debug("******** Adding event to queue: {}".format(tag))
        log.debug(self.cursor.query)
        self.counter += 1

    def debug_log(self):
        log.debug("####################################################################################")
        log.debug("******** queue_size: {}".format(self.counter))
        log.debug("******** fequency_commit_interval: {}".format(self.apply_limits(self.commit_interval)))

    @tornado.gen.coroutine
    def add_event_to_queue(self, raw):
        tag, data = self.event_bus.unpack(raw, self.event_bus.serial)
        self._insert(tag, data)

    @staticmethod
    def apply_limits(commit_interval):
	return max(min(commit_interval, COMMIT_INTERVAL_MAX), COMMIT_INTERVAL_MIN)

    @tornado.gen.coroutine
    def commit(self):
        """
        Committing to the database.
        """
        log.debug("************** COMMIT! ")
        time_between_events = self.commit_interval / (self.counter or 0.1)
        commit_interval_limited = 1 / time_between_events * SCALING_FACTOR
        self.commit_interval = DELAY_FACTOR * self.commit_interval + (1 - DELAY_FACTOR) * commit_interval_limited
        self.debug_log()
        self.connection.commit()
        self.counter = 0
        self.commit_timer_handle = self.event_bus.io_loop.call_later(
        self.apply_limits(self.commit_interval), self.commit)


def start():
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
    responder = Responder(event_bus)
    event_bus.set_event_handler(responder.add_event_to_queue)
    io_loop.start()
