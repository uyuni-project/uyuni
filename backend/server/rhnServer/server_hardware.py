#
# Copyright (c) 2008--2016 Red Hat, Inc.
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
#
# This file contains all the logic necessary to manipulate Hardware
# items - load, reload, instanciate and save
#

import socket
import sys
import time
import uuid

from rhn.UserDictCase import UserDictCase
from spacewalk.common.usix import raise_with_tb
from spacewalk.common.rhnLog import log_debug, log_error
from spacewalk.common.rhnConfig import CFG
from spacewalk.common.rhnException import rhnFault
from spacewalk.common.rhnTB import Traceback
from spacewalk.common import rhnMail
from spacewalk.common import rhnFlags
from spacewalk.server import rhnSQL, rhnVirtualization, rhnUser

# Local imports
from spacewalk.server.rhnServer import server_class

def kudzu_mapping(dict=None):
    """ this is a class we use to get the mapping for a kudzu entry """
    # This is the generic mapping we need
    mapping = {
        'desc': 'description',
    }
    # error handling if we get passed weird stuff.
    if not dict:
        return mapping
    if not type(dict) == type({}) and not isinstance(dict, UserDictCase):
        return mapping
    hw_bus = dict.get("bus")
    # we need to have a bus type to be able to continue
    if not hw_bus:
        return mapping
    hw_bus = hw_bus.lower()
    extra = {}
    if hw_bus == "ddc":
        extra = {
            "id": None,
            "horizsyncmin": "prop1",
            "horizsyncmax": "prop2",
            "vertrefreshmin": "prop3",
            "vertrefreshmax": "prop4",
            "modes": None,
            "mem": None,
        }
    elif hw_bus == "ide":
        extra = {
            "physical": "prop1",
            "logical": "prop2",
        }
    elif hw_bus in ["isapnp", "isa"]:
        extra = {
            "pdeviceid": "prop1",
            "deviceid": "prop2",
            "compat": "prop3",
            "native": None,
            "active": None,
            "cardnum": None,  # XXX: fix me
            "logdev": "prop4",
            "io": "prop2",
            "irq": "prop1",
            "dma": "prop3",
            "mem": "prop4",
        }
    elif hw_bus == "keyboard":
        extra = {}
    elif hw_bus == "psaux":
        extra = {}
    elif hw_bus == "parallel":
        extra = {
            'pnpmfr': 'prop1',
            'pnpdesc': 'prop2',
            'pnpmodel': 'prop3',
            'pnpmodes': 'prop4',
            'pinfo': None,
            'pinfo.xres': None,
            'pinfo.yres': None,
            'pinfo.color': None,
            'pinfo.ascii': None,
        }
    elif hw_bus == "pci":
        extra = {
            'vendorid': 'prop1',
            'deviceid': 'prop2',
            'subvendorid': 'prop3',
            'subdeviceid': 'prop4',
            'network.hwaddr': None,
            'pcibus': None,
            'pcidev': None,
            'pcifn': None,
            'pcidom': None,
        }
    elif hw_bus == "sbus":
        extra = {
            "monitor": "prop1",
            "width": "prop2",
            "height": "prop3",
            "freq": "prop4",
        }
    elif hw_bus == "scsi":
        extra = {
            'host': 'prop1',
            'id': 'prop2',
            'channel': 'prop3',
            'lun': 'prop4',
            'generic': None,
        }
    elif hw_bus == "serial":
        extra = {
            'pnpmfr': 'prop1',
            'pnpdesc': 'prop2',
            'pnpmodel': 'prop3',
            'pnpcompat': "prop4",
        }
    elif hw_bus == "usb":
        extra = {
            "vendorid": "prop1",
            "deviceid": "prop2",
            "usbclass": "prop3",
            "usbbus": "prop4",
            "usblevel": "pciType",
            "usbdev": None,
            "usbprod": None,
            "usbsubclass": None,
            "usbprotocol": None,
            "usbport": None,
            "usbmfr": None,
            "productname": None,
            "productrevision": None,
            'network.hwaddr': None,
        }
    elif hw_bus == "firewire":
        extra = {
            'vendorid': 'prop1',
            'deviceid': 'prop2',
            'subvendorid': 'prop3',
            'subdeviceid': 'prop4',
        }
    elif hw_bus == 'pcmcia':
        extra = {
            'vendorid': 'prop1',
            'deviceid': 'prop2',
            'function': 'prop3',
            'slot': 'prop4',
            'network.hwaddr': None,
        }
    mapping.update(extra)
    return mapping


def cleanse_ip_addr(ip_addr):
    """ Cleans up things like 127.00.00.01 """
    if ip_addr is None:
        return None
    # Make sure it's a string
    ip_addr = str(ip_addr)
    # If the ipaddr is empty, jus return empty str
    if not len(ip_addr):
        return ''
    arr = ip_addr.split('.')
    # lstrip will remove all leading zeros; if multiple zeros are present, it
    # would remove too much, hence the or '0' here.
    return '.'.join([x.lstrip('0') or '0' for x in arr])


class GenericDevice:

    """ A generic device class """
    table = "override-GenericDevice"

    def __init__(self):
        self.id = 0
        self.status = 1  # just added
        self.data = {}
        # default to the hardware seq...
        self.sequence = "rhn_hw_dev_id_seq"
        self._autonull = ("description", "board")

    def getid(self):
        if self.id == 0:
            self.id = rhnSQL.Sequence(self.sequence)()
        return self.id

    def must_save(self):
        if self.id == 0 and self.status == 2:  # deleted new item
            return 0
        if self.status == 0:  # original item, unchanged
            return 0
        return 1

    def save(self, sysid):
        """ save data in the rhnDevice table """
        log_debug(4, self.table, self.status, self.data)
        if not self.must_save():
            return 0
        t = rhnSQL.Table(self.table, "id")
        # check if we have to delete
        if self.status == 2 and self.id:
            # delete the entry
            del t[self.id]
            return 0
        # set description to null if empty
        self._null_columns([self.data], self._autonull)
        # make sure we have a device id
        devid = self.getid()
        for k in list(self.data.keys()):
            if self.data[k] is None:
                del self.data[k]
        self.data["server_id"] = sysid
        t[devid] = self.data
        self.status = 0  # now it is saved
        return 0

    def reload(self, devid):
        """ reload from rhnDevice table based on devid """
        if not devid:
            return -1
        t = rhnSQL.Table(self.table, "id")
        self.data = t[devid]
        # clean up fields we don't want
        if self.data:
            for k in ["created", "modified"]:
                if self.data.has_key(k):
                    del self.data[k]
        self.id = devid
        self.status = 0
        return 0

    def _null_columns(self, params, names=()):
        """ Method searches for empty string in params dict with names
            defined in names list and replaces them with None value which
            is translated to NULL in SQL.

            We do not allow empty strings in database for compatibility
            reasons between Oracle and PostgreSQL.
        """
        # list of dicts
        for param in params:
            for name in names:
                if name in param and param[name] == '':
                    param[name] = None


class Device(GenericDevice):

    """ This is the base Device class that supports instantiation from a
        dictionarry. the __init__ takes the dictionary as its argument,
        together with a list of valid fields to recognize and with a mapping
        for dictionary keys into valid field names for self.data

        The fields are required to know what fields we have in the
        table. The mapping allows transformation from whatever comes in to
        valid fields in the table Looks complicated but it isn't -- gafton
    """

    def __init__(self, fields, dict=None, mapping=None):
        GenericDevice.__init__(self)
        x = {}
        for k in fields:
            x[k] = None
        self.data = UserDictCase(x)
        if not dict:
            return
        # make sure we get a UserDictCase to work with
        if type(dict) == type({}):
            dict = UserDictCase(dict)
        if mapping is None or type(mapping) == type({}):
            mapping = UserDictCase(mapping)
        if not isinstance(dict, UserDictCase) or \
           not isinstance(mapping, UserDictCase):
            log_error("Argument passed is not a dictionary", dict, mapping)
            raise TypeError("Argument passed is not a dictionary",
                            dict, mapping)
        # make sure we have a platform
        for k in list(dict.keys()):
            if dict[k] == '':
                dict[k] = None
            if self.data.has_key(k):
                self.data[k] = dict[k]
                continue
            if mapping.has_key(k):
                # the mapping dict might tell us to lose some fields
                if mapping[k] is not None:
                    self.data[mapping[k]] = dict[k]
            else:
                log_error("Unknown HW key =`%s'" % k,
                          dict.dict(), mapping.dict())
                # The try-except is added just so that we can send e-mails
                try:
                    raise KeyError("Don't know how to parse key `%s''" % k,
                                   dict.dict())
                except:
                    Traceback(mail=1)
                    # Ignore this key
                    continue
        # clean up this data
        try:
            for k in list(self.data.keys()):
                if type(self.data[k]) == type("") and len(self.data[k]):
                    self.data[k] = self.data[k].strip()
                    if not len(self.data[k]):
                        continue
                    if self.data[k][0] == '"' and self.data[k][-1] == '"':
                        self.data[k] = self.data[k][1:-1]
        except IndexError:
            raise_with_tb(IndexError("Can not process data = %s, key = %s" % (
                repr(self.data), k)), sys.exc_info()[2])


class HardwareDevice(Device):

    """ A more specific device based on the Device class """
    table = "rhnDevice"

    def __init__(self, dict=None):
        fields = ['class', 'bus', 'device', 'driver', 'detached',
                  'description', 'pcitype', 'prop1', 'prop2',
                  'prop3', 'prop4']
        # get a processed mapping
        mapping = kudzu_mapping(dict)
        # ... and do little to no work
        Device.__init__(self, fields, dict, mapping)
        # use the hardware id sequencer
        self.sequence = "rhn_hw_dev_id_seq"


class CPUDevice(Device):

    """ A class for handling CPU - mirrors the rhnCPU structure """
    table = "rhnCPU"

    def __init__(self, dict=None):
        fields = ['cpu_arch_id',  'architecture', 'bogomips', 'cache',
                  'family', 'mhz', 'stepping', 'flags', 'model',
                  'version', 'vendor', 'nrcpu', 'acpiVersion',
                  'apic', 'apmVersion', 'chipset', 'nrsocket']
        mapping = {
            "bogomips": "bogomips",
            "cache": "cache",
            "model": "model",
            "platform": "architecture",
            "type": "vendor",
            "model_rev": "stepping",
            "model_number": "family",
            "model_ver": "version",
            "model_version": "version",
            "speed": "mhz",
            "count": "nrcpu",
            "socket_count": "nrsocket",
            "other": "flags",
            "desc": None,
            'class': None,
        }
        # now instantiate this class
        Device.__init__(self, fields, dict, mapping)
        self.sequence = "rhn_cpu_id_seq"
        if not dict:
            return
        if self.data.get("cpu_arch_id") is not None:
            return  # all fine, we have the arch
        # if we don't have an architecture, guess it
        if not self.data.has_key("architecture"):
            log_error("hash does not have a platform member: %s" % dict)
            raise AttributeError("Expected a hash value for member `platform'")
        # now extract the arch field, which has to come out of rhnCpuArch
        arch = self.data["architecture"]
        row = rhnSQL.Table("rhnCpuArch", "label")[arch]
        if row is None or not row.has_key("id"):
            log_error("Can not find arch %s in rhnCpuArch" % arch)
            raise AttributeError("Invalid architecture for CPU: `%s'" % arch)
        self.data["cpu_arch_id"] = row["id"]
        del self.data["architecture"]
        if self.data.has_key("nrcpu"):  # make sure this is a number
            try:
                self.data["nrcpu"] = int(self.data["nrcpu"])
            except:
                self.data["nrcpu"] = 1
            if self.data["nrcpu"] == 0:
                self.data["nrcpu"] = 1

class NetIfaceInformation(Device):
    key_mapping = {
        'hwaddr': 'hw_addr',
        'module': 'module',
    }

    def __init__(self, dict=None):
        log_debug(4, dict)
        self.ifaces = {}
        self.db_ifaces = []
        # parameters which are not allowed to be empty and set to NULL
        self._autonull = ('hw_addr', 'module')
        if not dict:
            return
        for name, info in list(dict.items()):
            if name == 'class':
                # Ignore it
                continue
            if not isinstance(info, type({})):
                raise rhnFault(53, "Unexpected format for interface %s" %
                               name)
            vdict = {}
            for key, mapping in list(self.key_mapping.items()):
                # Look at the mapping first; if not found, look for the key
                if mapping in info:
                    k = mapping
                else:
                    k = key
                if k not in info:
                    raise rhnFault(53, "Unable to find required field %s"
                                   % key)
                val = info[k]
                vdict[mapping] = val
            if 'ipaddr' in info and info['ipaddr']:
                vdict['ipv4'] = NetIfaceAddress4(
                    [{'ipaddr': info['ipaddr'], 'broadcast': info['broadcast'], 'netmask': info['netmask']}])
            if 'ipv6' in info and info['ipv6']:
                vdict['ipv6'] = NetIfaceAddress6(info["ipv6"])
            self.ifaces[name] = vdict

    def __str__(self):
        return "<%s Class at %d: %s>\n" % (
            self.__class__.__name__,
            id(self), {
                "self.ifaces": self.ifaces,
                "self.db_ifaces": self.db_ifaces,
            })
    __repr__ = __str__

    def save(self, server_id):
        log_debug(4, self.ifaces)
        self.reload(server_id)
        log_debug(4, "Interfaces in DB", self.db_ifaces)

        # Compute updates, deletes and inserts
        inserts = []
        updates = []
        deletes = []

        ifaces = self.ifaces.copy()
        for iface in self.db_ifaces:
            name = iface['name']
            if name not in self.ifaces:
                # To be deleted
                deletes.append({'server_id': server_id, 'name': name})
                continue

            uploaded_iface = ifaces[name].copy()
            del ifaces[name]
            if _hash_eq(uploaded_iface, iface):
                # Same value
                continue
            uploaded_iface.update({'name': name, 'server_id': server_id})
            if 'ipv4' in uploaded_iface:
                del(uploaded_iface['ipv4'])
            if 'ipv6' in uploaded_iface:
                del(uploaded_iface['ipv6'])
            updates.append(uploaded_iface)

        # Everything else in self.ifaces has to be inserted
        for name, info in list(ifaces.items()):
            iface = {}
            iface['name'] = name
            iface['server_id'] = server_id
            iface['hw_addr'] = info['hw_addr']
            iface['module'] = info['module']
            inserts.append(iface)

        log_debug(4, "Deletes", deletes)
        log_debug(4, "Updates", updates)
        log_debug(4, "Inserts", inserts)

        self._update(updates)
        self._insert(inserts)
        ifaces = self.ifaces.copy()
        for name, info in list(ifaces.items()):
            if not 'ipv6' in info:
                info['ipv6'] = NetIfaceAddress6()
            info['ipv6'].save(self.get_server_id(server_id, name))
            if not 'ipv4' in info:
                info['ipv4'] = NetIfaceAddress4()
            info['ipv4'].save(self.get_server_id(server_id, name))
        # delete address (if any) of deleted interaces
        for d in deletes:
            interface = NetIfaceAddress6()
            interface.save(self.get_server_id(server_id, d['name']))
            interface = NetIfaceAddress4()
            interface.save(self.get_server_id(server_id, d['name']))
        self._delete(deletes)
        return 0

    def get_server_id(self, server_id, name):
        """ retrieve id for given server_id and name """
        h = rhnSQL.prepare("select id from rhnServerNetInterface where server_id=:server_id and name=:name")
        h.execute(server_id=server_id, name=name)
        row = h.fetchone_dict()
        if row:
            return row['id']
        else:
            return None

    def _insert(self, params):
        q = """insert into rhnServerNetInterface
            (%s) values (%s)"""
        self._null_columns(params, self._autonull)

        columns = list(self.key_mapping.values()) + ['server_id', 'name']
        columns.sort()
        bind_params = ", ".join([':' + x for x in columns])
        h = rhnSQL.prepare(q % (", ".join(columns), bind_params))
        return _dml(h, params)

    def _delete(self, params):
        q = """delete from rhnServerNetInterface
            where %s"""

        columns = ['server_id', 'name']
        wheres = ['%s = :%s' % (x, x) for x in columns]
        h = rhnSQL.prepare(q % " and ".join(wheres))
        return _dml(h, params)

    def _update(self, params):
        q = """update rhnServerNetInterface
            set %s
            where %s"""
        self._null_columns(params, self._autonull)

        wheres = ['server_id', 'name']
        wheres = ['%s = :%s' % (x, x) for x in wheres]
        wheres = " and ".join(wheres)

        updates = list(self.key_mapping.values())
        updates.sort()
        updates = ['%s = :%s' % (x, x) for x in updates]
        updates = ", ".join(updates)

        h = rhnSQL.prepare(q % (updates, wheres))
        return _dml(h, params)

    def reload(self, server_id):
        h = rhnSQL.prepare("""
            select *
            from rhnServerNetInterface
            where server_id = :server_id
        """)
        h.execute(server_id=server_id)
        self.db_ifaces = []
        while 1:
            row = h.fetchone_dict()
            if not row:
                break
            hval = {'primary_id': row['id'], 'name': row['name'], 'server_id': server_id}
            for key in list(self.key_mapping.values()):
                hval[key] = row[key]
            hval['ipv4'] = NetIfaceAddress4()
            hval['ipv4'].reload(hval['primary_id'])
            hval['ipv6'] = NetIfaceAddress6()
            hval['ipv6'].reload(hval['primary_id'])
            self.db_ifaces.append(hval)

        self.status = 0
        return 0

class FQDNInformation(Device):
    """ This is a wrapper class for the FQDN information (rhnServerFQDN) """
    table = "rhnServerFQDN"

    def __init__(self, dict=None):
        fields = ["name"]
        unique = ["name"]
        mapping = {'class': None}
        Device.__init__(self, fields, dict, mapping)
        self._autonull = ('name')
        self.sequence = "rhn_serverfqdn_id_seq"

class NetIfaceAddress(Device):
    key_mapping = {
        'netmask': 'netmask',
        'address': 'address',
    }
    unique = ['address']  # to be overriden by child
    table = 'rhnServerNetAddress'  # to be overriden by child

    def __init__(self, list_ifaces=None):
        log_debug(4, list_ifaces)
        self.ifaces = {}
        self.db_ifaces = []
        # parameters which are not allowed to be empty and set to NULL
        self._autonull = ('address', 'netmask')
        self.sequence = "rhn_srv_net_iface_id_seq"
        if not list_ifaces:
            return
        for info in list_ifaces:
            if not isinstance(info, type({})):
                raise rhnFault(53, "Unexpected format for interface %s" %
                               info)
            vdict = {}
            for key, mapping in list(self.key_mapping.items()):
                # Look at the mapping first; if not found, look for the key
                if mapping in info:
                    k = mapping
                else:
                    k = key
                if k not in info:
                    raise rhnFault(53, "Unable to find required field %s"
                                   % (key))
                val = info[k]
                if mapping in ['ip_addr', 'netmask', 'broadcast', 'address']:
                    # bugzilla: 129840 kudzu (rhpl) will sometimes pad octets
                    # with leading zeros, causing confusion; clean those up
                    val = self.cleanse_ip_addr(val)
                vdict[mapping] = val
            self.ifaces[vdict['address']] = vdict

    def __str__(self):
        return "<%s Class at %d: %s>\n" % (
            self.__class__.__name__,
            id(self), {
                "self.ifaces": self.ifaces,
                "self.db_ifaces": self.db_ifaces,
            })
    __repr__ = __str__

    def cleanse_ip_addr(self, val):
        """ to be overriden by child """
        return val

    def save(self, interface_id):
        log_debug(4, self.ifaces)
        self.reload(interface_id)
        log_debug(4, "Net addresses in DB", self.db_ifaces)

        # Compute updates, deletes and inserts
        inserts = []
        updates = []
        deletes = []
        ifaces = self.ifaces.copy()
        for iface in self.db_ifaces:
            address = iface['address']
            if iface['address'] not in self.ifaces:
                # To be deleted
                # filter out params, which are not used in query
                iface = dict((column, iface[column]) for column in self.unique)
                deletes.append(iface)
                continue
            uploaded_iface = ifaces[address]
            del ifaces[address]
            # FIXME this is inefficient for IPv4 as it row is present it will be always update
            if _hash_eq(uploaded_iface, iface):
                # Same value
                continue
            uploaded_iface.update({'interface_id': interface_id})
            updates.append(uploaded_iface)

        # Everything else in self.ifaces has to be inserted
        for name, iface in list(ifaces.items()):
            iface['address'] = iface['address']
            iface['interface_id'] = interface_id
            inserts.append(iface)

        log_debug(4, "Deletes", deletes)
        log_debug(4, "Updates", updates)
        log_debug(4, "Inserts", inserts)

        self._delete(deletes)
        self._update(updates)
        self._insert(inserts)

    def _insert(self, params):
        q = """insert into %s
            (%s) values (%s)"""
        self._null_columns(params, self._autonull)

        columns = list(self.key_mapping.values()) + ['interface_id']
        columns.sort()
        bind_params = ", ".join([':' + x for x in columns])
        h = rhnSQL.prepare(q % (self.table, ", ".join(columns), bind_params))
        return _dml(h, params)

    def _delete(self, params):
        q = """delete from %s
            where %s"""

        columns = self.unique
        wheres = ['%s = :%s' % (x, x) for x in columns]
        h = rhnSQL.prepare(q % (self.table, " and ".join(wheres)))
        return _dml(h, params)

    def _update(self, params):
        q = """update %s
            set %s
            where %s"""
        self._null_columns(params, self._autonull)

        wheres = self.unique
        wheres = ['%s = :%s' % (x, x) for x in wheres]
        wheres = " and ".join(wheres)

        updates = list(self.key_mapping.values())
        updates.sort()
        updates = ['%s = :%s' % (x, x) for x in updates]
        updates = ", ".join(updates)

        h = rhnSQL.prepare(q % (self.table, updates, wheres))
        return _dml(h, params)

    def reload(self, interface_id):
        h = rhnSQL.prepare("""
            select *
            from %s
            where interface_id = :interface_id
            order by interface_id
        """ % self.table)
        h.execute(interface_id=interface_id)
        self.db_ifaces = []
        while 1:
            row = h.fetchone_dict()
            if not row:
                break
            hval = {'interface_id': row['interface_id']}
            for key in list(self.key_mapping.values()):
                hval[key] = row[key]
            self.db_ifaces.append(hval)

        self.status = 0
        return 0


class NetIfaceAddress6(NetIfaceAddress):

    """ IPv6 Network interface """
    key_mapping = {
        'netmask': 'netmask',
        'addr': 'address',
        'scope': 'scope',
    }
    table = 'rhnServerNetAddress6'
    unique = ['interface_id', 'address', 'scope']

    def __init__(self, addr_dict=None):
        NetIfaceAddress.__init__(self, addr_dict)
        self._autonull = ('address', 'netmask', 'scope')


class NetIfaceAddress4(NetIfaceAddress):

    """ IPv4 Network interface """
    key_mapping = {
        'netmask': 'netmask',
        'ipaddr': 'address',
        'broadcast': 'broadcast',
    }
    table = 'rhnServerNetAddress4'
    unique = ['interface_id']

    def __init__(self, addr_dict=None):
        NetIfaceAddress.__init__(self, addr_dict)
        self._autonull = ('address', 'netmask', 'broadcast')

    def cleanse_ip_addr(self, val):
        return cleanse_ip_addr(val)


def _hash_eq(h1, h2):
    """ Compares two hashes and return 1 if the first is a subset of the second """
    log_debug(5, h1, h2)
    for k, v in list(h1.items()):
        if k not in h2:
            return 0
        if h2[k] != v:
            return 0
    return 1


def _dml(statement, params):
    log_debug(5, params)
    if not params:
        return 0
    params = _transpose(params)
    rowcount = statement.executemany(**params)
    log_debug(5, "Affected rows", rowcount)
    return rowcount


def _transpose(hasharr):
    """ Transpose the array of hashes into a hash of arrays """
    if not hasharr:
        return {}
    keys = list(hasharr[0].keys())
    result = {}
    for k in keys:
        result[k] = []

    for hval in hasharr:
        for k in keys:
            if k in hval:
                result[k].append(hval[k])
            else:
                result[k].append(None)

    return result


class MemoryInformation(Device):

    """ Memory information """
    table = "rhnRAM"

    def __init__(self, dict=None):
        fields = ["ram", "swap"]
        mapping = {"class": None}
        Device.__init__(self, fields, dict, mapping)
        # use our own sequence
        self.sequence = "rhn_ram_id_seq"
        if not dict:
            return
        # Sometimes we get sent a NNNNL number and we need to strip the L
        for k in fields:
            if not self.data.has_key(k):
                continue
            if self.data[k] in [None, "None", ""]:
                self.data[k] = -1
            self.data[k] = str(self.data[k])
            if self.data[k][-1] == 'L':
                self.data[k] = self.data[k][:-1]


class DMIInformation(Device):

    """ DMI information """
    table = "rhnServerDMI"

    def __init__(self, dict=None):
        fields = ["vendor", "system", "product", "asset", "board",
                  "bios_vendor", "bios_version", "bios_release"]
        mapping = {"class": None}
        Device.__init__(self, fields, dict, mapping)
        # use our own sequence
        self.sequence = "rhn_server_dmi_id_seq"
        self._autonull = ("vendor", "system", "product", "asset", "board",
                          "bios_vendor", "bios_version", "bios_release")
        if not dict:
            return

        # deal with hardware with insanely long dmi strings...
        for key, value in list(self.data.items()):
            # Some of the values may be None
            if value and isinstance(value, type("")):
                self.data[key] = value[:256]


class InstallInformation(Device):

    """ Install information """
    table = "rhnServerInstallInfo"

    def __init__(self, dict=None):
        fields = ['install_method', 'iso_status', 'mediasum']
        mapping = {
            'class': None,
            'installmethod': 'install_method',
            'isostatus': 'iso_status',
            'mediasum': 'mediasum',
        }
        Device.__init__(self, fields, dict, mapping)
        self.sequence = 'rhn_server_install_info_id_seq'

class SystemInformation():
    def __init__(self, hw=None, guest=None):
        log_debug(4, hw, guest)
        if not hw or "identifier" not in hw or not guest:
            # incomplete data
            log_debug(1, "incomplete data")
            return
        host = rhnSQL.Row("rhnServer", "digital_server_id")
        host.load(hw['identifier'])
        hid = host.get('id')
        guest.user = rhnUser.User("", "")
        guest.user.reload(guest.server['creator_id'])
        guestid = guest.getid()
        if not hid:
            # create a new host entry
            host = server_class.Server(guest.user, hw.get('arch'))
            host.server["name"] = hw.get('name')
            host.server["os"] = hw.get('os')
            host.server["release"] = hw.get('type')
            host.server["last_boot"] = time.time()
            host.default_description()
            host.virt_type = rhnVirtualization.VirtualizationType.FULLY
            host.virt_uuid = None
            fake_token = False
            if not rhnFlags.test("registration_token"):
                # we need to fake it
                rhnFlags.set("registration_token", 'fake')
                fake_token = True
            host.save(1, None)
            host.server["digital_server_id"] = hw['identifier']
            entitle_server = rhnSQL.Procedure("rhn_entitlements.entitle_server")
            entitle_server(host.getid(), 'foreign_entitled')
            host.save(1, None)
            if fake_token:
                rhnFlags.set("registration_token", None)

            hid = host.getid()
            host.reload(hid)
            log_debug(4, "New host created: ", host)
        else:
            host = server_class.Server(None)
            host.reload(hid)
            host.checkin(commit=0)
            log_debug(4, "Found host: ", host)
        host.reload_hardware()
        hostcpu = host.hardware_by_class(CPUDevice)
        if hostcpu and len(hostcpu) > 0:
            hostcpu = hostcpu[0].data
        else:
            hostcpu = None
        if not hostcpu or str(hostcpu.get('nrsocket')) != hw.get('total_ifls'):
            # update only if the number has changed
            log_debug(1, "update host cpu:", hw.get('total_ifls'))
            cpu = {
                'class' : 'CPU',
                'desc' : 'Processor',
                'count' : hw.get('total_ifls'),
                'model_ver' : '',
                'speed' : '0',
                'cache' : '',
                'model_number' : '',
                'bogomips' : '',
                'socket_count' : hw.get('total_ifls'),
                'platform' : hw.get('arch'),
                'other' : '',
                'model_rev' : '',
                'model' : hw.get('arch'),
                'type' : hw.get('type')}
            host.delete_hardware()
            host.add_hardware(cpu)
            host.save_hardware()

        h = rhnSQL.prepare("""
            select host_system_id
              from rhnVirtualInstance
             where virtual_system_id = :guestid""")
        h.execute(guestid=guestid)
        row = h.fetchone_dict()
        if not row or not row['host_system_id']:
            self._insert_virtual_instance(hid, None, fakeuuid=False)
            self._insert_virtual_instance(hid, guestid, fakeuuid=True)
        elif row['host_system_id'] != hid:
            log_debug(4, "update_virtual_instance", hid, guestid)
            q_update = rhnSQL.prepare("""
                UPDATE rhnVirtualInstance
                   SET host_system_id = :host_id
                 WHERE virtual_system_id = :guest_id
                   AND host_system_id = :old_host_id
            """)
            q_update.execute(host_id=hid, guest_id=guestid, old_host_id=row['host_system_id'])


    def _insert_virtual_instance(self, hostid, guestid, fakeuuid=True):
        log_debug(4, hostid, guestid, fakeuuid)
        viid = rhnSQL.Sequence('rhn_vi_id_seq')()
        q_insert = rhnSQL.prepare("""
            INSERT INTO rhnVirtualInstance
                (id, host_system_id, virtual_system_id, uuid, confirmed)
            VALUES
                (:viid, :host_id, :guest_id, :uuid, 1)
        """)
        fake_uuid = None
        if fakeuuid:
            fake_uuid = str(uuid.uuid4())
            fake_uuid = fake_uuid.replace('-', '').strip()
        q_insert.execute(viid=viid, host_id=hostid, guest_id=guestid, uuid=fake_uuid)

        q_insert = rhnSQL.prepare("""
            INSERT INTO rhnVirtualInstanceInfo
                (instance_id, state, instance_type)
            VALUES
                (:viid,
                 (
                     SELECT rvis.id
                     FROM rhnVirtualInstanceState rvis
                     WHERE rvis.label = 'unknown'
                 ),
                 (
                     SELECT rvit.id
                     FROM rhnVirtualInstanceType rvit
                     WHERE rvit.label = :virt_type
                 ))
        """)
        q_insert.execute(viid=viid, virt_type='fully_virtualized')


class MachineInformation:

    def __init__(self, sysid, name=None, data=None):
        machine_id = data.get("machine_id") if data and "machine_id" in data else None
        if not name:
            name = "unknown"
        log_debug(4, "updating machine_id", sysid, name, machine_id)
        if not self.__check_if_machine_id_exists(machine_id):
            s_update = rhnSQL.prepare("""
                        UPDATE rhnServer
                           SET machine_id = :machine_id
                        WHERE id = :server_id
                        """)
            s_update.execute(machine_id=machine_id, server_id=sysid)
        else:
            # Send an alert email in case of machine_id collition.
            log_error("Unable to update machine id: machine id is not unique")
            self.__send_machine_id_alert_email(machine_id, sysid, name)

    def __check_if_machine_id_exists(self, machine_id):
        if machine_id:
            h = rhnSQL.prepare('SELECT id FROM rhnServer WHERE machine_id = :machine_id')
            h.execute(machine_id=machine_id)
            return bool(h.fetchall_dict())
        return False

    def __send_machine_id_alert_email(self, machine_id, sysid, name):
        log_debug(1, "Sending alert email about multiple machine_id")
        hostname = socket.getfqdn()

        to = CFG.TRACEBACK_MAIL
        fr = to
        if isinstance(to, type([])):
            fr = to[0].strip()
            to = ', '.join([s.strip() for s in to])

        headers = {
            "Subject" : "SUSE Manager multiple 'machine_id' detected",
            "From"    : "{0} <{1}>".format(hostname, fr),
            "To"      : to,
        }
        body = MACHINE_ID_EMAIL_TEMPLATE.format(machine_id, sysid, name)
        rhnMail.send(headers, body)


class Hardware:

    """ Support for the hardware items """

    def __init__(self):
        self.__hardware = {}
        self.__loaded = 0
        self.__changed = 0

    def hardware_by_class(self, device_class):
        return self.__hardware[device_class]

    def add_hardware(self, hardware):
        """ add new hardware """
        log_debug(4, hardware)
        if not hardware:
            return -1
        if type(hardware) == type({}):
            hardware = UserDictCase(hardware)
        if not isinstance(hardware, UserDictCase):
            log_error("argument type is not  hash: %s" % hardware)
            raise TypeError("This function requires a hash as an argument")
        # validation is important
        hw_class = hardware.get("class")
        if hw_class is None:
            return -1
        hw_class = hw_class.lower()

        class_type = None

        if hw_class in ["video", "audio", "audio_hd", "usb", "other", "hd", "floppy",
                        "mouse", "modem", "network", "cdrom", "scsi",
                        "unspec", "scanner", "tape", "capture", "raid",
                        "socket", "keyboard", "printer", "firewire", "ide"]:
            class_type = HardwareDevice
        elif hw_class == "cpu":
            class_type = CPUDevice
        elif hw_class == "memory":
            class_type = MemoryInformation
        elif hw_class == "dmi":
            class_type = DMIInformation
        elif hw_class == "installinfo":
            class_type = InstallInformation
        elif hw_class == "netinterfaces":
            class_type = NetIfaceInformation
        elif hw_class == "fqdn":
            class_type = FQDNInformation
        elif hw_class == "sysinfo":
            # special case: we got info about a virtual host
            # where this system is running on
            SystemInformation(hardware, self)
            return 0
        elif hw_class == "machineinfo":
            MachineInformation(self.server["id"], self.server["name"], hardware)
            return 0
        else:
            log_error("UNKNOWN CLASS TYPE `%s'" % hw_class)
            # Same trick: try-except and raise the exception so that Traceback
            # can send the e-mail
            try:
                raise KeyError("Unknown class type `%s' for hardware '%s'" % (
                    hw_class, hardware))
            except:
                Traceback(mail=1)
                return

        # create the new device
        new_dev = class_type(hardware)

        if class_type in self.__hardware:
            _l = self.__hardware[class_type]
        else:
            _l = self.__hardware[class_type] = []
        _l.append(new_dev)
        self.__changed = 1
        return 0

    def delete_hardware(self, sysid=None):
        """ This function deletes all hardware. """
        log_debug(4, sysid)
        if not self.__loaded:
            self.reload_hardware_byid(sysid)
        hardware = self.__hardware
        if hardware == {}:
            # nothing to delete
            return 0
        self.__changed = 1

        self.__reset_machine_id(sysid)

        for device_type in list(hardware.keys()):
            for hw in hardware[device_type]:
                hw.status = 2  # deleted

            # filter out the hardware that was just added and then
            # deleted before saving
            hardware[device_type] = [a for a in hardware[device_type] if not (a.status == 2 and hasattr(a, "id") and a.id == 0)]
        return 0

    def __reset_machine_id(self, sysid):
        """ Sets rhnServer.machine_id to null """
        MachineInformation(sysid)

    def save_hardware_byid(self, sysid):
        """Save the hardware list """
        log_debug(3, sysid, "changed = %s" % self.__changed)
        hardware = self.__hardware
        if hardware == {}:  # nothing loaded
            return 0
        if not self.__changed:
            return 0
        for device_type, hw_list in list(hardware.items()):
            for hw in hw_list:
                hw.save(sysid)
        self.__changed = 0
        return 0

    def __load_from_db(self, DevClass, sysid):
        """ Load a certain hardware class from the database """
        if DevClass not in self.__hardware:
            self.__hardware[DevClass] = []

        h = rhnSQL.prepare("select id from %s where server_id = :sysid" % DevClass.table)
        h.execute(sysid=sysid)
        rows = h.fetchall_dict() or []

        for device in rows:
            dev_id = device['id']
            dev = DevClass()
            dev.reload(dev_id)
            self.__hardware[DevClass].append(dev)

    def reload_hardware_byid(self, sysid):
        """ load all hardware devices for a server """
        log_debug(4, sysid)
        if not sysid:
            return -1
        self.__hardware = {}  # discard what was already loaded
        # load from all hardware databases
        self.__load_from_db(HardwareDevice, sysid)
        self.__load_from_db(CPUDevice, sysid)
        self.__load_from_db(DMIInformation, sysid)
        self.__load_from_db(MemoryInformation, sysid)
        self.__load_from_db(InstallInformation, sysid)
        self.__load_from_db(FQDNInformation, sysid)

        net_iface_info = NetIfaceInformation()
        net_iface_info.reload(sysid)
        self.__hardware[NetIfaceInformation] = [net_iface_info]

        # now set the flag
        self.__changed = 0
        self.__loaded = 1
        return 0


MACHINE_ID_EMAIL_TEMPLATE = """A non-unique machine_id has been detected: "{0}"

This is expected behavior when using cloned images of systems.
Solution: Generate a new machine_id for system "{2} ({1})"

For SLES12+ and RHEL7:
  # rm -f /var/lib/dbus/machine-id
  # rm -f /etc/machine-id
  # dbus-uuidgen --ensure
  # systemd-machine-id-setup
  # rhn-profile-sync


For SLES11 and RHEL5/RHEL6:
  # rm /var/lib/dbus/machine-id
  # dbus-uuidgen --ensure
  # rhn-profile-sync


For more information on generating a new machine_id for traditional systems refer to:
https://www.suse.com/documentation/suse-manager-3/book_suma_best_practices_31/data/bp_chapt_suma3_troubleshooting_registering_cloned_traditional_systems.html
"""
