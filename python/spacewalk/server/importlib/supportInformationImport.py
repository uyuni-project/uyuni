#
# Copyright (c) 2012 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Support Information Import

from .importLib import GenericPackageImport
from spacewalk.satellite_tools import syncCache

class SupportInformationImport(GenericPackageImport):
    def __init__(self, batch, backend):
        GenericPackageImport.__init__(self, batch, backend)
        self._cache = syncCache.ShortPackageCache()
        self._data = []

    def preprocess(self):
        channelLabels = {}
        keywords = {}
        for item in self.batch:
            if item['channel'] not in channelLabels:
                channelLabels[item['channel']] = None
                self.backend.lookupChannels(channelLabels)
            if item['keyword'] not in keywords:
                keywords[item['keyword']] = self.backend.lookupKeyword(item['keyword'])
            pkg = self._cache.cache_get(item['pkgid'])
            if not pkg:
                continue
            if not (channelLabels[item['channel']] and channelLabels[item['channel']]['id']):
                continue
            self.backend.lookupPackageIdFromPackage(pkg)
            if pkg.id is None:
                continue

            item['package_id'] = pkg.id
            item['channel_id'] = channelLabels[item['channel']]['id']
            item['keyword_id'] = keywords[item['keyword']]
            self._data.append(item)

    def fix(self):
        pass

    def submit(self):
        try:
            self.backend.processSupportInformation(self._data)
        except:
            self.backend.rollback()
            raise
        self.backend.commit()

