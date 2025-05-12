#!/usr/bin/python
# pylint: disable=missing-module-docstring

import unittest

# pylint: disable-next=wildcard-import
from config import *


# pylint: disable-next=missing-class-docstring,undefined-variable
class SystemSearchTests(RhnTestCase):

    # pylint: disable-next=invalid-name
    def test_searchByNameAndDescription(self):
        # pylint: disable-next=undefined-variable,undefined-variable
        systems = client.system.search.ip(self.session_key, SYS_SEARCH_NAME_DESCRP)
        # pylint: disable-next=singleton-comparison
        self.assertTrue(systems != None)
        for s in systems:
            self.assertTrue(s.has_key("name"))
            self.assertTrue(s.has_key("ip"))
            self.assertTrue(s.has_key("id"))

    # pylint: disable-next=invalid-name
    def test_searchByIp(self):
        # pylint: disable-next=undefined-variable,undefined-variable
        systems = client.system.search.ip(self.session_key, SYS_SEARCH_IP)
        # pylint: disable-next=singleton-comparison
        self.assertTrue(systems != None)
        for s in systems:
            self.assertTrue(s.has_key("name"))
            self.assertTrue(s.has_key("ip"))
            self.assertTrue(s.has_key("id"))

    # pylint: disable-next=invalid-name
    def test_searchByHostname(self):
        # pylint: disable-next=unused-variable
        term = "redhat.com"
        # pylint: disable-next=undefined-variable,undefined-variable
        systems = client.system.search.hostname(self.session_key, SYS_SEARCH_HOSTNAME)
        # pylint: disable-next=singleton-comparison
        self.assertTrue(systems != None)
        for s in systems:
            self.assertTrue(s.has_key("name"))
            self.assertTrue(s.has_key("hostname"))
            self.assertTrue(s.has_key("id"))

    # pylint: disable-next=invalid-name
    def test_searchByDeviceDescription(self):
        # pylint: disable-next=undefined-variable
        systems = client.system.search.deviceDescription(
            # pylint: disable-next=undefined-variable
            self.session_key,
            # pylint: disable-next=undefined-variable
            SYS_SEARCH_HW_DESCRP,
        )
        # pylint: disable-next=singleton-comparison
        self.assertTrue(systems != None)
        for s in systems:
            self.assertTrue(s.has_key("name"))
            self.assertTrue(s.has_key("id"))
            self.assertTrue(s.has_key("hw_description"))
            self.assertTrue(s.has_key("hw_driver"))

    # pylint: disable-next=invalid-name
    def test_searchByDeviceDriver(self):
        # pylint: disable-next=undefined-variable
        systems = client.system.search.deviceDriver(
            # pylint: disable-next=undefined-variable
            self.session_key,
            # pylint: disable-next=undefined-variable
            SYS_SEARCH_HW_DEVICE_DRIVER,
        )
        # pylint: disable-next=singleton-comparison
        self.assertTrue(systems != None)
        for s in systems:
            self.assertTrue(s.has_key("name"))
            self.assertTrue(s.has_key("id"))
            self.assertTrue(s.has_key("hw_description"))
            self.assertTrue(s.has_key("hw_driver"))

    # pylint: disable-next=invalid-name
    def test_searchByDeviceId(self):
        # pylint: disable-next=undefined-variable
        systems = client.system.search.deviceId(
            # pylint: disable-next=undefined-variable
            self.session_key,
            # pylint: disable-next=undefined-variable
            SYS_SEARCH_HW_DEVICE_ID,
        )
        # pylint: disable-next=singleton-comparison
        self.assertTrue(systems != None)
        for s in systems:
            self.assertTrue(s.has_key("name"))
            self.assertTrue(s.has_key("id"))
            self.assertTrue(s.has_key("hw_description"))
            self.assertTrue(s.has_key("hw_device_id"))
            self.assertTrue(s.has_key("hw_driver"))

    # pylint: disable-next=invalid-name
    def test_searchByDeviceVendorId(self):
        # pylint: disable-next=undefined-variable
        systems = client.system.search.deviceVendorId(
            # pylint: disable-next=undefined-variable
            self.session_key,
            # pylint: disable-next=undefined-variable
            SYS_SEARCH_HW_VENDOR_ID,
        )
        # pylint: disable-next=singleton-comparison
        self.assertTrue(systems != None)
        for s in systems:
            self.assertTrue(s.has_key("name"))
            self.assertTrue(s.has_key("id"))
            self.assertTrue(s.has_key("hw_description"))
            self.assertTrue(s.has_key("hw_driver"))
            self.assertTrue(s.has_key("hw_vendor_id"))


if __name__ == "__main__":
    unittest.main()
