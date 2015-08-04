#
# Copyright (c) 2014 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SUSE Products Import

from importLib import GenericPackageImport
from spacewalk.satellite_tools import syncCache

class SuseProductsImport(GenericPackageImport):
    def __init__(self, batch, backend):
        GenericPackageImport.__init__(self, batch, backend)
        self._cache = syncCache.ShortPackageCache()
        self._data = []

    def preprocess(self):
        archs = {}
        for item in self.batch:
            if item['arch'] not in archs:
                archs[item['arch']] = None
                self.backend.lookupPackageArches(archs)
            item['arch_type_id'] = archs[item['arch']]
            if item['release'] == 'None':
                item['release'] = None
            if item['version'] == 'None':
                item['version'] = None
            self._data.append(item)

    def fix(self):
        pass

    def submit(self):
        try:
            self.backend.processSuseProducts(self._data)
        except:
            self.backend.rollback()
            raise
        self.backend.commit()

class SuseProductChannelsImport(GenericPackageImport):
    def __init__(self, batch, backend):
        GenericPackageImport.__init__(self, batch, backend)
        self._cache = syncCache.ShortPackageCache()
        self._data = []

    def preprocess(self):
        pid_trans = {}
        for item in self.batch:
            channel = {}
            intpid = item['product_id']
            if intpid not in pid_trans:
                pid_trans[intpid] = self.backend.lookupSuseProductIdByProductId(intpid)
            item['product_id'] = pid_trans[intpid]
            channel[item['channel_label']] = None
            self.backend.lookupChannels(channel)
            if channel[item['channel_label']]:
                item['channel_id'] = channel[item['channel_label']]['id']
            else:
                item['channel_id'] = None
            if item['parent_channel_label'] == 'None':
                item['parent_channel_label'] = None
            self._data.append(item)

    def fix(self):
        pass

    def submit(self):
        try:
            self.backend.processSuseProductChannels(self._data)
        except:
            self.backend.rollback()
            raise
        self.backend.commit()

class SuseUpgradePathsImport(GenericPackageImport):
    def __init__(self, batch, backend):
        GenericPackageImport.__init__(self, batch, backend)
        self._cache = syncCache.ShortPackageCache()
        self._data = []

    def preprocess(self):
        pid_trans = {}
        for item in self.batch:
            if item['from_product_id'] not in pid_trans:
                pid_trans[item['from_product_id']] = self.backend.lookupSuseProductIdByProductId(item['from_product_id'])
            item['from_pdid'] = pid_trans[item['from_product_id']]
            if item['to_product_id'] not in pid_trans:
                pid_trans[item['to_product_id']] = self.backend.lookupSuseProductIdByProductId(item['to_product_id'])
            item['to_pdid'] = pid_trans[item['to_product_id']]
            self._data.append(item)

    def fix(self):
        pass

    def submit(self):
        try:
            self.backend.processSuseUpgradePaths(self._data)
        except:
            self.backend.rollback()
            raise
        self.backend.commit()

class SuseSubscriptionsImport(GenericPackageImport):
    def __init__(self, batch, backend):
        GenericPackageImport.__init__(self, batch, backend)
        self._cache = syncCache.ShortPackageCache()
        self._sub_data = []
        self._ent_data = []

    def preprocess(self):
        ent_labels = self.backend.getEntitlementLabels()
        for item in self.batch:
            item['org_id'] = 1
            if item['system_entitlement'] == "1":
                if item['label'] in ent_labels:
                    item['group_type'] = ent_labels[item['label']]
                    self._ent_data.append(item)
                # else: skip this entitlement. It does not exist here
            else:
                families = {}
                families[item['label']] = None
                self.backend.lookupChannelFamilies(families)
                item['channel_family_id'] = families[item['label']]
                self._sub_data.append(item)

    def fix(self):
        self.backend.calcEntMaxMembers(self._ent_data)

    def submit(self):
        try:
            self.backend.processSuseSubscriptions(self._sub_data)
            self.backend.processSuseEntitlements(self._ent_data)
        except:
            self.backend.rollback()
            raise
        self.backend.commit()

class ClonedChannelsImport(GenericPackageImport):
    def __init__(self, batch, backend):
        GenericPackageImport.__init__(self, batch, backend)
        self._cache = syncCache.ShortPackageCache()
        self._data = []

    def preprocess(self):
        pid_trans = {}
        for item in self.batch:
            channel = {}
            channel[item['orig']] = None
            self.backend.lookupChannels(channel)
            if channel[item['orig']]:
                item['orig_id'] = channel[item['orig']]['id']
            else:
                # orig channel not synced - skip
                continue
            channel = {}
            channel[item['clone']] = None
            self.backend.lookupChannels(channel)
            if channel[item['clone']]:
                item['id'] = channel[item['clone']]['id']
            else:
                # channel not synced - skip
                continue
            self._data.append(item)

    def fix(self):
        pass

    def submit(self):
        try:
            self.backend.processClonedChannels(self._data)
        except:
            self.backend.rollback()
            raise
        self.backend.commit()
