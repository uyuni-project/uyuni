# -*- coding: utf-8 -*-
#
# Copyright (c) 2011 Novell
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

from spacewalk.common.rhnLog import log_debug
from spacewalk.server import rhnSQL
from spacewalk.server.rhnLib import InvalidAction

# the "exposed" functions
__rhnexport__ = ['deploy']

# returns the values for deploying a virtual machine with an image
#
# file_name, checksum, mem_kb, vcpus, imageType
#
def deploy(serverId, actionId, dry_run=0):
    log_debug(3)
    statement = """
        select si.file_name, si.checksum, aid.mem_kb, aid.vcpus, si.image_type
          from rhnActionImageDeploy aid
	  join suseImages si ON aid.image_id = si.id
         where si.status = 'DONE'
	   and aid.action_id = :action_id"""
    h = rhnSQL.prepare(statement)
    h.execute(action_id = actionId)
    row = h.fetchone_dict()
    if not row:
        # No image for this action
        raise InvalidAction("image.deploy: No image found for action id "
            "%s and server %s" % (actionId, serverId))

    file_name = row['file_name']
    checksum  = row['checksum']
    mem_kb    = row['mem_kb']
    vcpus     = row['vcpus']
    if row['image_type'] == 'vmx':
	image_type = 'vmdk'
    elif row['image_type'] == 'xen':
	image_type = 'xen'
    else:
        raise InvalidAction("image.deploy: invalid image_type")


    return (file_name, checksum, mem_kb, vcpus, image_type)

