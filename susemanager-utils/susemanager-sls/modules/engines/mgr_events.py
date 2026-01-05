# -*- coding: utf-8 -*-

# SPDX-FileCopyrightText: 2018-2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

"""
mgr_events.py is a SaltStack engine that writes selected events to SUSE
Manager's PostgreSQL database. Additionally, it sends notifications via the
LISTEN/NOTIFY mechanism to alert SUSE Multi-Linux Manager of newly available events.

mgr_events.py tries to keep the I/O low in high load scenarios. Therefore
events are INSERTed with the separate thread without blocking the event bus
and COMMITted every `commit_interval`.

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
"""

# Import python libs
from __future__ import absolute_import, print_function, unicode_literals
import json
import logging
import re
import threading
import time

try:
    import psycopg2

    HAS_PSYCOPG2 = True
except ImportError:
    HAS_PSYCOPG2 = False

# Import salt libs
import salt.ext.tornado
import salt.utils.event

log = logging.getLogger(__name__)

DEFAULT_COMMIT_INTERVAL = 1
DEFAULT_COMMIT_BURST = 100


# pylint: disable-next=invalid-name
def __virtual__():
    return HAS_PSYCOPG2


# pylint: disable-next=missing-class-docstring
class Responder:
    def __init__(self, event_bus, config):
        self.config = config
        self.config.setdefault("commit_interval", DEFAULT_COMMIT_INTERVAL)
        self.config.setdefault("commit_burst", DEFAULT_COMMIT_BURST)
        self.config.setdefault("postgres_db", {})
        self.config["postgres_db"].setdefault("host", "localhost")
        self.config["postgres_db"].setdefault("notify_channel", "suseSaltEvent")
        self._commit_interval = config["commit_interval"]
        self._commit_burst = config["commit_burst"]
        self._thread_pool_size = config["events"]["thread_pool_size"]
        self.counter = 0
        self.counters = [0] * (self._thread_pool_size + 1)
        # Pass salt job returns: salt/job/*/ret/*
        self.re_job_ret = re.compile(r"salt\/job\/\d+\/ret\/.*")
        # Pass all additional significant events
        self.re_additional = [
            re.compile(r"salt\/minion\/([^\/]+)\/start"),
            re.compile(r"salt\/beacon\/.*"),
            re.compile(r"salt\/batch\/([^\/]+)\/start"),
            re.compile(r"suse\/manager\/(image_deployed|image_synced|pxe_update)"),
            re.compile(r"suse\/systemid\/generate"),
            re.compile(r"suse\/proxy\/backup_finished"),
        ]
        self._queue = []
        self.event_bus = event_bus
        self._connect_to_database()
        self._queue_thread = threading.Thread(target=self.queue_thread)
        self._queue_thread.start()
        self._insert_exceptions = 0

    def _connect_to_database(self):
        db_config = self.config.get("postgres_db")
        if "port" in db_config:
            # pylint: disable-next=consider-using-f-string
            conn_string = "dbname='{dbname}' user='{user}' host='{host}' port='{port}' password='{password}'".format(
                **db_config
            )
        else:
            # pylint: disable-next=consider-using-f-string
            conn_string = "dbname='{dbname}' user='{user}' host='{host}' password='{password}'".format(
                **db_config
            )
        log.debug("connecting to database")
        while True:
            try:
                self.connection = psycopg2.connect(conn_string)
                break
            except psycopg2.OperationalError as err:
                log.error("DB Error: %s", err)
                log.error("Retrying in 5 seconds.")
                time.sleep(5)
        self.cursor = self.connection.cursor()

    def match_additional_events(self, tag):
        for p in self.re_additional:
            if p.match(tag):
                return True
        return False

    def _insert(self, tag, data):
        if (
            # Pass all salt/job/*/ret/* events
            self.re_job_ret.match(tag)
            and not (
                # Except mine updates
                data.get("fun") == "mine.update"
                or (
                    # And presence test.ping returns
                    data.get("fun") == "test.ping"
                    and data.get("metadata", {}).get("batch-mode")
                )
            )
        ) or self.match_additional_events(tag):
            # Pass all significant events also
            try:
                queue = self._get_queue(data.get("id"))
                log.debug("Adding event to queue %d -> %s", queue, tag)
                self.cursor.execute(
                    "INSERT INTO suseSaltEvent (minion_id, data, queue) VALUES (%s, %s, %s);",
                    (data.get("id"), json.dumps({"tag": tag, "data": data}), queue),
                )
                self.counter += 1
                self.counters[queue] += 1
            # pylint: disable-next=broad-exception-caught
            except Exception as err:
                log.error("Error while inserting data to the table: %s", err)
                try:
                    self.connection.commit()
                # pylint: disable-next=broad-exception-caught
                except Exception as err2:
                    log.error("Error commiting: %s", err2)
                    self.connection.close()
                raise
            finally:
                log.debug("Cursor query: %s", self.cursor.query)
        else:
            log.debug("Discarding event -> %s", tag)

    def _get_queue(self, minion_id):
        if minion_id:
            self.cursor.execute(
                """
                  SELECT COALESCE (
                      (SELECT MAX(queue)
                       FROM   suseSaltEvent
                       WHERE  minion_id = %s),
                      (SELECT X.queue
                       FROM   (SELECT   Q.queue,
                                        (SELECT COUNT(*) FROM suseSaltEvent sa where Q.queue = sa.queue) as count
                               FROM     (SELECT generate_series(1, %s) queue) Q
                               ORDER BY count, Q.queue
                               LIMIT 1) X
                      )
                  ) queue;""",
                (minion_id, int(self.config["events"]["thread_pool_size"])),
            )
            row = self.cursor.fetchone()
            if row is not None:
                return int(row[0])
        return 0

    def trace_log(self):
        if self.counter or self._queue:
            log.trace("queues sizes [%d] -> %s", len(self._queue), self.counters)

    def push_events_from_queue(self):
        while self._queue and (
            self.counter < self._commit_burst or self._commit_burst == 0
        ):
            tag, data = self._queue.pop(0)
            try:
                self._insert(tag, data)
            # pylint: disable-next=broad-exception-caught
            except Exception:
                # Exception while inserting the data, put the data back to the queue
                # and repeat the attempt of pushing it with the next cycle of queue_thread
                # The exception was logged before in _insert
                # The event could cause exception on inserting by its payload.
                # We need to prevent the situation when such event is getting stuck
                # the processing the rest part of the queue.
                # The amount of attempts to be pushed to the DB is limited
                self._insert_exceptions += 1
                if self._insert_exceptions < 5:
                    self._queue.insert(0, (tag, data))
                else:
                    # Reset the counter on droping the event from the queue
                    log.error(
                        "The event '%s' was dropped as reached the maximum attempts to be pushed",
                        tag,
                    )
                    self._insert_exceptions = 0
                break
            else:
                # Reset the counter if there was no exception
                self._insert_exceptions = 0

    @salt.ext.tornado.gen.coroutine
    def add_event_to_queue(self, raw):
        try:
            tag, data = self.event_bus.unpack(raw)
            self._queue.append((tag, data))
        # pylint: disable-next=broad-exception-caught
        except Exception as e:
            log.warning("Unable to unpack the event data: %s", e)

    def db_keepalive(self):
        if self.connection.closed:
            log.error("Diconnected from database. Trying to reconnect...")
            self._connect_to_database()

    def queue_thread(self):
        while True:
            try:
                self.db_keepalive()
                self.push_events_from_queue()
                self.trace_log()
                self.attempt_commit()
            # pylint: disable-next=broad-exception-caught
            except Exception as e:
                log.error(
                    "Exception while processing the events queue: %s", e, exc_info=True
                )
            time.sleep(self._commit_interval)

    def attempt_commit(self):
        """
        Committing to the database.
        """
        if self.counter > 0:
            log.debug("DB: commit")
            self.cursor.execute(
                # pylint: disable-next=consider-using-f-string
                "NOTIFY {}, '{}';".format(
                    self.config["postgres_db"]["notify_channel"],
                    ",".join([str(counter) for counter in self.counters]),
                )
            )
            self.connection.commit()
            self.counter = 0
            self.counters = [0] * (self._thread_pool_size + 1)


def start(**config):
    """
    Listen to events and write them to the Postgres database
    """
    io_loop = salt.ext.tornado.ioloop.IOLoop(make_current=False)
    io_loop.make_current()
    event_bus = salt.utils.event.get_master_event(
        # pylint: disable-next=undefined-variable
        __opts__,
        # pylint: disable-next=undefined-variable
        __opts__["sock_dir"],
        listen=True,
        io_loop=io_loop,
    )
    responder = Responder(event_bus, config)
    event_bus.set_event_handler(responder.add_event_to_queue)
    io_loop.start()
