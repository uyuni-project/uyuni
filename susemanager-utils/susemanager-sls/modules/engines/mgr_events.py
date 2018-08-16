# -*- coding: utf-8 -*-
'''
A simple test engine, not intended for real use but as an example
'''

# Import python libs
from __future__ import absolute_import, print_function, unicode_literals
import logging
import time

# Import salt libs
import salt.utils.event
import salt.utils.json

# Import third-party libs
import tornado.ioloop

log = logging.getLogger(__name__)

SCALING_FACTOR = 0.1
COMMIT_INTERVAL_MAX = 10
COMMIT_INTERVAL_MIN = 0.1
QUEUE_SIZE_LIMIT = 5000

# DELAY FACTOR VALUES
# 0.9 would be approx. averaging time difference between the last 10 events -> higher fluctuation
# 0.98 would be approx. averaging time difference between the last 50 events -> lower fluctuation
DELAY_FACTOR = 0.1


class Responder:
    def __init__(self, event_bus):
        self.queue = []
        self.frequency_average = 0.2
        self.commit_interval = 1 / self.frequency_average
        self.event_bus = event_bus
        self.commit_timer_handle = self.event_bus.io_loop.call_later(self.commit_interval, self.commit)

    def debug_log(self):
        log.debug("####################################################################################")
        log.debug("******** queue_size: {}".format(len(self.queue)))
        log.debug("******** fequency_average: {}".format(self.frequency_average))
        log.debug("******** fequency_commit_interval: {}".format(self.apply_limits(self.commit_interval)))

    @tornado.gen.coroutine
    def add_event_to_queue(self, raw):
        # TODO: Remove next two lines. Just for testing.
        tag, data = self.event_bus.unpack(raw, self.event_bus.serial)
        log.debug("******** Adding event to queue: {}".format(tag))
        self.queue.append(raw)
        # Checking, if we are above the max. queue size.
        if (len(self.queue)) >= QUEUE_SIZE_LIMIT:
            # Unschedule next commit, so that we don't get two commits at the
            # same time. Is re-scheduled after the commit.
            self.event_bus.io_loop.remove_timeout(self.commit_timer_handle)
            self.commit()
	    self.commit_timer_handle = self.event_bus.io_loop.call_later(COMMIT_INTERVAL_MAX, self.commit)
            

    @staticmethod
    def apply_limits(commit_interval):
	return max(min(commit_interval, COMMIT_INTERVAL_MAX), COMMIT_INTERVAL_MIN)

    @tornado.gen.coroutine
    def commit(self):
        """
        Committing to the database.
        """
        log.debug("************** COMMIT! ")
        time_between_events = self.commit_interval / (len(self.queue) or 0.1)
        commit_interval_limited = 1 / time_between_events * SCALING_FACTOR
        self.commit_interval = DELAY_FACTOR * self.commit_interval + (1 - DELAY_FACTOR) * commit_interval_limited
        self.debug_log()
        while len(self.queue) > 0:
            entry = self.queue.pop()
            tag, data = self.event_bus.unpack(entry, self.event_bus.serial)
            ret = {'data': data, 'tag': tag}
            log.debug("******* INSERT INTO suseSaltEvent ({});".format(tag))

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

