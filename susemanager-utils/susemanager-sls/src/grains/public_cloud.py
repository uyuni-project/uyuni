# -*- coding: utf-8 -*-                                                                                                                                                      
'''
Copyright (c) 2019 SUSE LLC

This software is licensed to you under the GNU General Public License,
version 2 (GPLv2). There is NO WARRANTY for this software, express or
implied, including the implied warranties of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
along with this software; if not, see
http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

This grain module is only loaded in case of a public cloud instance.

Supported Instances: AWS EC2, Azure and Google Compute Engine instances

Returns a grain called "instance_id" containing the virtual instance ID
according to the Public Cloud provider. The data is gathered using the
internal API available from within the instance.

Author: Pablo Suárez Hernández <psuarezhernandez@suse.com>
Based on: https://docs.saltstack.com/en/latest/ref/grains/all/salt.grains.metadata.html
'''
from __future__ import absolute_import, print_function, unicode_literals

# Import python libs
import os
import socket

# Import salt libs
import salt.utils.http as http

# Internal metadata API information
INTERNAL_API_IP = '169.254.169.254'
HOST = 'http://{0}/'.format(INTERNAL_API_IP)

INSTANCE_ID = None

AMAZON_URL_PATH = 'latest/meta-data/'
AZURE_URL_PATH = 'metadata/instance/compute/'
AZURE_API_ARGS = '?api-version=2017-08-01&format=text'
GOOGLE_URL_PATH = 'computeMetadata/v1/instance/'


def __virtual__():
    global INSTANCE_ID
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.settimeout(0.1)
    result = sock.connect_ex((INTERNAL_API_IP, 80))
    if result != 0:
        return False
    ret = http.query(os.path.join(HOST, AMAZON_URL_PATH), status=True)
    if ret.get('status') == 200 and "instance-id" in ret['body']:
        INSTANCE_ID = http.query(os.path.join(HOST, AMAZON_URL_PATH, 'instance-id'))['body']
        return True
    ret = http.query(os.path.join(HOST, AZURE_URL_PATH) + AZURE_API_ARGS, status=True, header_dict={"Metadata":"true"})
    if ret.get('status') == 200 and "vmId" in ret['body']:
        INSTANCE_ID = http.query(os.path.join(HOST, AZURE_URL_PATH, 'vmId') + AZURE_API_ARGS, header_dict={"Metadata":"true"})['body']
        return True
    ret = http.query(os.path.join(HOST, GOOGLE_URL_PATH), status=True, header_dict={"Metadata-Flavor": "Google"})
    if ret.get('status') == 200 and "id" in ret['body']:
        INSTANCE_ID = http.query(os.path.join(HOST, GOOGLE_URL_PATH, 'id'), header_dict={"Metadata-Flavor": "Google"})['body']
        return True
    return False


def instance_id():
    global INSTANCE_ID
    ret = {}
    if INSTANCE_ID:
        ret['instance_id'] = INSTANCE_ID
    return ret
