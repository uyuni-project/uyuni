#!/usr/bin/python
# pylint: disable=missing-module-docstring

import unittest

from random import randint

# pylint: disable-next=wildcard-import
from config import *

# Should have at least 100 free slots to run these tests, which *should*
# return all of those back to the default satellite org once finished.
CHANNEL_FAMILY_LABEL = "rhel-server"

SATELLITE_ORG_ID = 1


# pylint: disable-next=missing-class-docstring,undefined-variable
class OrgTests(RhnTestCase):

    def setUp(self):
        # pylint: disable-next=undefined-variable
        RhnTestCase.setUp(self)

        # Create a test org that will be deleted in teardown:
        self.random_int = randint(1, 1000000)
        # pylint: disable-next=consider-using-f-string
        self.org_name = "Test Org %s" % self.random_int
        # pylint: disable-next=undefined-variable
        self.org = client.org.create(
            self.session_key,
            self.org_name,
            # pylint: disable-next=consider-using-f-string
            "admin%s" % self.random_int,
            "password",
            "Mr.",
            "Fake",
            "Admin",
            "fake@example.com",
            False,
        )
        self.org_id = self.org["id"]

    def tearDown(self):
        # pylint: disable-next=undefined-variable,unused-variable
        result = client.org.delete(self.session_key, self.org_id)
        # pylint: disable-next=undefined-variable
        RhnTestCase.tearDown(self)

    def test_create_org(self):
        self.assertTrue(self.org.has_key("id"))
        self.assertTrue(self.org.has_key("name"))
        self.assertTrue(self.org.has_key("systems"))
        self.assertTrue(self.org.has_key("active_users"))
        self.assertTrue(self.org.has_key("system_groups"))
        self.assertTrue(self.org.has_key("activation_keys"))
        self.assertTrue(self.org.has_key("kickstart_profiles"))

    def test_delete_no_such_org(self):
        # pylint: disable-next=undefined-variable
        self.assertRaises(Exception, client.org.delete, self.session_key, -1)

    # pylint: disable-next=unused-private-member
    def __find_count_for_org(self, results, org_id):
        for count in results:
            if count["org_id"] == org_id:
                return count
        # pylint: disable-next=consider-using-f-string
        self.fail("Unable to find org id: %s" % org_id)

    # pylint: disable-next=unused-private-member
    def __find_count_for_entitlement(self, results, channel_family_label):
        for count in results:
            if count["label"] == channel_family_label:
                return count
        # pylint: disable-next=consider-using-f-string
        self.fail("Unable to find channel family: %s" % channel_family_label)


if __name__ == "__main__":
    unittest.main()
