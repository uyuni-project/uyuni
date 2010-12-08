# -*- coding: utf-8 -*-
#
# Copyright (c) 2010 Novell
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

import os
import tempfile
import ConfigParser

def getProductProfile():
  """ Return information about the installed from suse_register_info """
  productProfileFile = tempfile.NamedTemporaryFile(prefix='sreg-info-')
  ret = os.system("/usr/lib/suseRegister/bin/suse_register_info --outfile %s" % productProfileFile.name)
  if ret != 0:
    raise Exception("Executing suse_register_info failed.")
  return parseProductProfileFile(productProfileFile)

def parseProductProfileFile(infile):
  """ Parse a product profile from file (e.g. created by suse_register_info) """
  config = ConfigParser.ConfigParser()
  config.read(infile.name)
  ret = { 'products' : [] }
  for section in config.sections():
    if section == 'system':
      for key,val in config.items(section):
        ret[key] = val
    else:
      product = { 'baseproduct' : 'N' }
      for key,val in config.items(section):
        product[key] = val
      ret['products'].append(product)
  return ret
