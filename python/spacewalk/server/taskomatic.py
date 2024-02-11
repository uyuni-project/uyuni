"""
Module for taskomatic related functions (inserting into queues, etc)
"""

try:
    # pylint: disable-next=unused-import
    import xmlrpc.client as xmlrpclib
except ImportError:
    # pylint: disable-next=unused-import
    import xmlrpclib
from spacewalk.server import rhnSQL

# see TaskoXmlRpcHandler.java for available methods
TASKOMATIC_XMLRPC_URL = "http://localhost:2829/RPC2"


class RepodataQueueEntry(object):
    def __init__(self, channel, client, reason, force=False, bypass_filters=False):
        self.channel = channel
        self.client = client
        self.reason = reason
        self.force = force
        self.bypass_filters = bypass_filters


# pylint: disable-next=missing-class-docstring
class RepodataQueue(object):
    def _boolean_as_char(boolean):
        if boolean:
            return "Y"
        else:
            return "N"

    _boolean_as_char = staticmethod(_boolean_as_char)

    def add(self, entry):
        h = rhnSQL.prepare(
            """
            insert into rhnRepoRegenQueue
                (id, channel_label, client, reason, force, bypass_filters,
                 next_action, created, modified)
            values (
                sequence_nextval('rhn_repo_regen_queue_id_seq'),
                :channel, :client, :reason, :force, :bypass_filters,
                current_timestamp, current_timestamp, current_timestamp
            )
        """
        )

        h.execute(
            channel=entry.channel,
            client=entry.client,
            reason=entry.reason,
            force=self._boolean_as_char(entry.force),
            bypass_filters=self._boolean_as_char(entry.bypass_filters),
        )


def add_to_repodata_queue(channel, client, reason, force=False, bypass_filters=False):
    if reason == "":
        reason = None
    entry = RepodataQueueEntry(channel, client, reason, force, bypass_filters)
    queue = RepodataQueue()
    queue.add(entry)


# XXX not the best place for this...


def add_to_repodata_queue_for_channel_package_subscription(
    affected_channels, batch, caller
):
    tmpreason = []
    for package in batch:
        tmpreason.append(package.short_str())

    reason = " ".join(tmpreason)

    for channel in affected_channels:
        # don't want to cause an error for the db
        add_to_repodata_queue(channel, caller, reason[:128])


def add_to_erratacache_queue(channel, priority=0):
    h = rhnSQL.prepare(
        """
    insert into rhnTaskQueue
           (id, org_id, task_name, task_data, priority, earliest)
           select nextval('rhn_task_queue_id_seq'),
                  coalesce(c.org_id, 1),
                  'update_errata_cache_by_channel',
                  c.id,
                  :priority,
                  current_timestamp
             from rhnChannel c
            where c.label = :label
    """
    )
    h.execute(label=channel, priority=priority)
    rhnSQL.commit()


def add_to_system_overview_update_queue(sid):
    h = rhnSQL.prepare(
        """
    INSERT INTO rhnTaskQueue (id, org_id, task_name, task_data, priority, earliest)
    VALUES (
        nextval('rhn_task_queue_id_seq'),
        (select org_id from rhnserver where id = :sid),
        'update_system_overview',
        :sid,
        0,
        current_timestamp
    )
    """
    )
    h.execute(sid=sid)
    rhnSQL.commit()
