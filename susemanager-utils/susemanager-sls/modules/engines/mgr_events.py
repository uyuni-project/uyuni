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

class Responder:
    def __init__(self, event_bus):
        self.event_bus = event_bus
        self.last_commit_time = time.time()
        self.commit_scheduled = False

    @tornado.gen.coroutine
    def check(self, raw):
        tag, data = self.event_bus.unpack(raw, self.event_bus.serial)
        ret = {'data': data, 'tag': tag}
        log.debug("INSERT INTO suseSaltEvent ({});".format(tag))
        time_since_commit = time.time() - self.last_commit_time
        if time_since_commit >= 1:
            self.commit()
        else:
            if not self.commit_scheduled:
                log.debug("Delaying commit by {} seconds!".format(1-time_since_commit))
                self.event_bus.io_loop.call_later(1 - time_since_commit, self.commit)
                self.commit_scheduled = True

    @tornado.gen.coroutine
    def commit(self):
        log.debug("COMMITTING!")
        self.last_commit_time = time.time()
        self.commit_scheduled = False

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
    event_bus.set_event_handler(responder.check)

    io_loop.start()
