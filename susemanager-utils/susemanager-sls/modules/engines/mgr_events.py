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

FREQUENCY_MIN = 0.2
FREQUENCY_MAX = 10
ATTENUATION_FACTOR = 0.9


class Responder:
    def __init__(self, event_bus):
        self.queue = []
        self.time_of_last_event = time.time()
        self.frequency_average = FREQUENCY_MIN
        self.commit_interval = 1 / self.frequency_average
        self.event_bus = event_bus
        self.event_bus.io_loop.call_later(self.commit_interval, self.commit)

    def debug_log(self):
        log.debug("####################################################################################")
        log.debug("******** time_of_last_event: {}".format(self.time_of_last_event))
        log.debug("******** fequency_average: {}".format(self.frequency_average))
        log.debug("******** fequency_commit_interval: {}".format(self.commit_interval))

    @tornado.gen.coroutine
    def add_event_to_queue(self, raw):
        # TODO: Remove next two lines. Just for testing.
        tag, data = self.event_bus.unpack(raw, self.event_bus.serial)
        log.debug("******** Adding event to queue: {}".format(tag))
        self.queue.append(raw)
        current_time = time.time()
        self.frequency_average = ATTENUATION_FACTOR * self.frequency_average + (1 - ATTENUATION_FACTOR) * (current_time - self.time_of_last_event)
        self.commit_interval = min(0.5 / self.frequency_average, FREQUENCY_MAX)
        self.time_of_last_event = current_time
        self.debug_log()

    @tornado.gen.coroutine
    def commit(self):
        """
        Committing to the database.
        """
        while len(self.queue) > 0:
            entry = self.queue.pop()
            tag, data = self.event_bus.unpack(entry, self.event_bus.serial)
            ret = {'data': data, 'tag': tag}
            log.debug("******* INSERT INTO suseSaltEvent ({});".format(tag))
        log.debug("************** COMMIT! ")
        # Slowly decrease (speed up) the `commit_interval` again. This is
        # important for cases where new events come in very slowly.
        self.commit_interval = max(0.9 * self.commit_interval, 0.1)
        self.event_bus.io_loop.call_later(self.commit_interval, self.commit)


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
