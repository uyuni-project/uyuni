#
# Copyright (c) 2014 Novell, Inc.
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

