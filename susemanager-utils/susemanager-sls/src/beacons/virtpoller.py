# -*- coding: utf-8 -*-
#
# Copyright (c) 2008--2014 Red Hat, Inc.
# Copyright (c) 2016 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
'''
Watch libvirt and fire events with changes to virtual machines

Author: Michael Calmer <mc@suse.com>
'''

from __future__ import absolute_import
import sys
import os
import logging
log = logging.getLogger(__name__)

try:
    import libvirt  # pylint: disable=import-error
    from libvirt import libvirtError
    HAS_LIBVIRT = True
except ImportError:
    HAS_LIBVIRT = False
    libvirt = None


try:
    import cPickle as pickle
except ImportError:
    import pickle
import time
import traceback
import binascii

CACHE_DATA_PATH = '/var/cache/virt_state.cache'
CACHE_EXPIRE_SECS = 60 * 60 * 6   # 6 hours, in seconds

##
# This structure maps the libvirt state enumeration to labels that
# SUSE Manager understands.
# Reasons we don't care about differences between NOSTATE, RUNNING and BLOCKED:
# 1. technically, the domain is still "running"
# 2. RHN Classic / Red Hat Satellite / Spacewalk are not able to
# display 'blocked' & 'nostate'
#    as valid states
# 3. to avoid 'Abuse of Service' messages: bugs #230106 and #546676

VIRT_STATE_NAME_MAP = ( 'running',  # VIR_DOMAIN_NOSTATE
                        'running',  # VIR_DOMAIN_RUNNING
                        'running',  # VIR_DOMAIN_BLOCKED
                        'paused',   # VIR_DOMAIN_PAUSED
                        'stopped',  # VIR_DOMAIN_SHUTDOWN
                        'stopped',  # VIR_DOMAIN_SHUTOFF
                        'crashed')  # VIR_DOMAIN_CRASHED

class EventType:
    EXISTS      = 'exists'
    REMOVED     = 'removed'
    FULLREPORT  = 'fullreport'

class TargetType:
    SYSTEM      = 'system'
    DOMAIN      = 'domain'

class VirtualizationType:
    PARA  = 'para_virtualized'
    FULLY = 'fully_virtualized'

class PropertyType:
    NAME        = 'name'
    UUID        = 'uuid'
    TYPE        = 'virt_type'
    MEMORY      = 'memory_size'
    VCPUS       = 'vcpus'
    STATE       = 'state'
    IDENTITY    = 'identity'
    ID          = 'id'
    MESSAGE     = 'message'

__virtualname__ = 'virtpoller'


###############################################################################
# PollerStateCache Class
###############################################################################

class PollerStateCache:

    ###########################################################################
    # Public Interface
    ###########################################################################

    def __init__(self, domain_data, cache_file = CACHE_DATA_PATH,
            expire_time = CACHE_EXPIRE_SECS):
        """
        This method creates a new poller state based on the provided domain
        list.  The domain_data list should be in the form returned from
        poller.poll_hypervisor.  That is,

             { uuid : { 'name'        : '...',
                        'uuid'        : '...',
                        'virt_type'   : '...',
                        'memory_size' : '...',
                        'vcpus'       : '...',
                        'state'       : '...' }, ... }
        """
        self.__expire_time = expire_time
        self.__cache_file = cache_file

        # Start by loading the old state, if necessary.
        self._load_state()
        self.__new_domain_data = domain_data

        # Now compare the given domain_data against the one loaded in the old
        # state.
        self._compare_domain_data()

        log.debug("Added: %s"    % repr(self.__added))
        log.debug("Removed: %s"  % repr(self.__removed))
        log.debug("Modified: %s" % repr(self.__modified))

    def save(self):
        """
        Updates the cache on disk with the latest domain data.
        """
        self._save_state()

    def is_expired(self):
        """
        Returns true if this cache is expired.
        """
        if self.__expire_time is None:
            return False
        else:
            return long(time.time()) >= self.__expire_time

    def is_changed(self):
        return self.__added or self.__removed or self.__modified

    def get_added(self):
        """
        Returns a list of uuids for each domain that has been added since the
        last state poll.
        """
        return self.__added

    def get_modified(self):
        """
        Returns a list of uuids for each domain that has been modified since
        the last state poll.
        """
        return self.__modified

    def get_removed(self):
        """
        Returns a list of uuids for each domain that has been removed since
        the last state poll.
        """
        return self.__removed

    ###########################################################################
    # Helper Methods
    ###########################################################################

    def _load_state(self):
        """
        Loads the last hypervisor state from disk.
        """
        # Attempt to open up the cache file.
        cache_file = None
        try:
            cache_file = open(self.__cache_file, 'r')
        except IOError, ioe:
            # Couldn't open the cache file.  That's ok, there might not be one.
            # We'll only complain if debugging is enabled.
            log.debug("Could not open cache file '{0}': {1}".format(
                self.__cache_file, str(ioe)))

        # Now, if a previous state was cached, load it.
        state = {}
        if cache_file:
            try:
                state = pickle.load(cache_file)
            except pickle.PickleError as pe:
                # Strange.  Possibly, the file is corrupt.  We'll load an empty
                # state instead.
                log.debug("Error occurred while loading state: {0}".format(str(pe)))
            except EOFError:
                log.debug("Unexpected EOF. Probably an empty file.")
                cache_file.close()

            cache_file.close()

        if state:
            log.debug("Loaded state: {0}".format(repr(state)))

            self.__expire_time = long(state['expire_time'])

            # If the cache is expired, set the old data to None so we force
            # a refresh.
            if self.is_expired():
                self.__old_domain_data = None
                os.unlink(self.__cache_file)
            else:
                self.__old_domain_data = state['domain_data']

        else:
            self.__old_domain_data = None
            self.__expire_time     = None

    def _save_state(self):
        """
        Saves the given polling state to disk.
        """
        # First, ensure that the proper parent directory is created.
        cache_dir_path = os.path.dirname(self.__cache_file)
        if not os.path.exists(cache_dir_path):
            os.makedirs(cache_dir_path, 0o700)

        state = {}
        state['domain_data'] = self.__new_domain_data
        if self.__expire_time is None or self.is_expired():
            state['expire_time'] = int(time.time()) + CACHE_EXPIRE_SECS
        else:
            state['expire_time'] = self.__expire_time

        # Now attempt to open the file for writing.  We'll just overwrite
        # whatever's already there.  Also, let any exceptions bounce out.
        cache_file = open(self.__cache_file, "wb")
        pickle.dump(state, cache_file)
        cache_file.close()

    def _compare_domain_data(self):
        """
        Compares the old domain_data to the new domain_data.  Returns a tuple
        of lists, relative to the new domain_data:

            (added, removed, modified)
        """
        self.__added    = {}
        self.__removed  = {}
        self.__modified = {}

        # First, figure out the modified and added uuids.
        if self.__new_domain_data:
            for (uuid, new_properties) in self.__new_domain_data.items():
                if not self.__old_domain_data or \
                    not self.__old_domain_data.has_key(uuid):

                    self.__added[uuid] = self.__new_domain_data[uuid]
                else:
                    old_properties = self.__old_domain_data[uuid]
                    if old_properties != new_properties:
                        self.__modified[uuid] = self.__new_domain_data[uuid]

        # Now, figure out the removed uuids.
        if self.__old_domain_data:
            for uuid in self.__old_domain_data.keys():
                if not self.__new_domain_data or \
                    not self.__new_domain_data.has_key(uuid):

                    self.__removed[uuid] = self.__old_domain_data[uuid]


###############################################################################
### beacon                                                                  ###
###############################################################################

def __virtual__():
    return HAS_LIBVIRT and __virtualname__ or False


def validate(config):
    '''
    Validate the beacon configuration.
    '''
    if not isinstance(config, dict):
        return False, ('Configuration for virtpoller '
                       'beacon must be a dictionary.')
    else:
        return True, 'Configuration validated'


def beacon(config):
    '''
    polls the hypervisor for information about the currently
    running set of domains.

    Example Config

    .. code-block:: yaml

        beacons:
          virtpoller:
            expire_time: 21600
            cache_file: '/var/cache/virt_state.cache'
            interval: 320
    '''

    ret = []

    if not libvirt:
        log.trace("no libvirt")
        return ret

    try:
        conn = libvirt.openReadOnly(None)
    except libvirt.libvirtError, lve:
        log.error("Warning: Could not retrieve virtualization information! libvirtd service needs to be running.")
        conn = None

    if not conn:
        # No connection to hypervisor made
        return ret

    domains = conn.listAllDomains(0)

    state = {}
    for domain in domains:
        uuid = binascii.hexlify(domain.UUID())
        # SEE: http://libvirt.org/html/libvirt-libvirt.html#virDomainInfo
        # for more info.
        domain_info = domain.info()

        # Set the virtualization type.  We can tell if the domain is fully virt
        # by checking the domain's OSType() attribute.
        virt_type = VirtualizationType.PARA
        if domain.OSType().lower() == 'hvm':
            virt_type = VirtualizationType.FULLY

        # we need to filter out the small per/minute KB changes
        # that occur inside a vm.  To do this we divide by 1024 to
        # drop our precision down to megabytes with an int then
        # back up to KB
        memory = int(domain_info[2] / 1024);
        memory = memory * 1024;
        properties = {
            PropertyType.NAME   : domain.name(),
            PropertyType.UUID   : uuid,
            PropertyType.TYPE   : virt_type,
            PropertyType.MEMORY : str(memory), # current memory
            PropertyType.VCPUS  : domain_info[3],
            PropertyType.STATE  : VIRT_STATE_NAME_MAP[domain_info[0]] }

        state[uuid] = properties

    poller_state = PollerStateCache(state,
                                    cache_file = config.get('cache_file', CACHE_DATA_PATH),
                                    expire_time = config.get('expire_time', CACHE_EXPIRE_SECS))

    plan = []
    if poller_state.is_changed():
        added    = poller_state.get_added()
        removed  = poller_state.get_removed()
        modified = poller_state.get_modified()

        if poller_state.is_expired():
            item = {'time': int(time.time()),
                    'event_type': EventType.FULLREPORT,
                    'target_type': TargetType.DOMAIN }
            plan.append(item)

        for (uuid, data) in added.items():
            item = {'time': int(time.time()),
                    'event_type': EventType.EXISTS,
                    'target_type': TargetType.DOMAIN,
                    'guest_properties': data}
            plan.append(item)

        for (uuid, data) in modified.items():
            item = {'time': int(time.time()),
                    'event_type': EventType.EXISTS,
                    'target_type': TargetType.DOMAIN,
                    'guest_properties': data}
            plan.append(item)

        for (uuid, data) in removed.items():
            item = {'time': int(time.time()),
                    'event_type': EventType.REMOVED,
                    'target_type': TargetType.DOMAIN,
                    'guest_properties': data}
            plan.append(item)

    poller_state.save()
    if len(plan) > 0:
        ret.append({'plan': plan})
    return ret
