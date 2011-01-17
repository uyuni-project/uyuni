
"""
Module for taskomatic related functions (inserting into queues, etc)
"""

import xmlrpclib
from spacewalk.server import rhnSQL

# see TaskoXmlRpcHandler.java for available methods
TASKOMATIC_XMLRPC_URL = 'http://localhost:2829/RPC2'

class RepodataQueueEntry(object):

    def __init__(self, channel, client, reason, force=False,
            bypass_filters=False):
        self.channel = channel
        self.client = client
        self.reason = reason
        self.force = force
        self.bypass_filters = bypass_filters


class RepodataQueue(object):

    def _boolean_as_char(boolean):
        if boolean:
            return 'Y'
        else:
            return 'N'

    _boolean_as_char = staticmethod(_boolean_as_char)

    def add(self, entry):
        h = rhnSQL.prepare("""
            insert into rhnRepoRegenQueue
                (id, channel_label, client, reason, force, bypass_filters,
                 next_action, created, modified)
            values (
                sequence_nextval('rhn_repo_regen_queue_id_seq'),
                :channel, :client, :reason, :force, :bypass_filters,
                current_timestamp, current_timestamp, current_timestamp
            )
        """)

        h.execute(channel=entry.channel, client=entry.client,
            reason=entry.reason, force=self._boolean_as_char(entry.force),
            bypass_filters=self._boolean_as_char(entry.bypass_filters))

def add_to_repodata_queue(channel, client, reason, force=False,
        bypass_filters=False):
    if reason == '':
        reason = None
    entry = RepodataQueueEntry(channel, client, reason, force, bypass_filters)
    queue = RepodataQueue()
    queue.add(entry)

# XXX not the best place for this...
def add_to_repodata_queue_for_channel_package_subscription(affected_channels,
        batch, caller):

        tmpreason = []
        for package in batch:
            tmpreason.append(package.short_str())

        reason = " ".join(tmpreason)

        for channel in affected_channels:
            # don't want to cause an error for the db
            add_to_repodata_queue(channel, caller, reason[:128])


def schedule_single_sat_repo_sync(channel_id):
    """ Schedule a non-recurring satellite (non-organizational) repo sync
        for channel identified by rhnChannel.ID.

        Repo sync is normally an organizational task scheduled by the following
        XML-RPC call:
            xmlrpcclient.tasko.scheduleSingleBunchRun(
                org_id, 'repo-sync-bunch', {'channel_id':channel_id}).
        SUSE however needs to create its default non-organizational channels
        out of multiple NCC repos when doing sm-ncc-sync, thus the repo sync
        task for the default channels must be executed without an org_id (as
        a satellite task). Special XMLRPC method was created for this:
            xmlrpcclient.tasko.scheduleSingleSatRepoSync(channel_id)

        The method returns the start date of the scheduled job or None
        if the scheduling failed.
    """
    client = xmlrpclib.Server(TASKOMATIC_XMLRPC_URL)

    try:
        return client.tasko.scheduleSingleSatRepoSync(channel_id)
    except xmlrpclib.Fault, e:
        print "Error scheduling repo sync task: %s" % e
    return None
