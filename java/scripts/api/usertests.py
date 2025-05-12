#!/usr/bin/python
# pylint: disable=missing-module-docstring

import unittest

# pylint: disable-next=wildcard-import
from config import *
from random import randint


# pylint: disable-next=missing-class-docstring,undefined-variable
class UserTests(RhnTestCase):

    def setUp(self):
        # pylint: disable-next=undefined-variable
        RhnTestCase.setUp(self)
        # pylint: disable-next=consider-using-f-string
        self.test_user = "TestUser%s" % randint(1, 1000000)
        # pylint: disable-next=undefined-variable
        client.user.create(
            self.session_key,
            self.test_user,
            "testpassword",
            "Test",
            "User",
            "TestUser@example.com",
        )

        self.test_group_names = []
        self.test_group_ids = []
        # pylint: disable-next=consider-using-f-string
        self.test_group_names.append("Test Group %s" % randint(1, 100000))
        # pylint: disable-next=consider-using-f-string
        self.test_group_names.append("Test Group %s" % randint(1, 100000))
        # pylint: disable-next=consider-using-f-string
        self.test_group_names.append("Test Group %s" % randint(1, 100000))

        for group_name in self.test_group_names:
            # pylint: disable-next=undefined-variable
            group = client.systemgroup.create(
                self.session_key, group_name, "Fake Description"
            )
            self.test_group_ids.append(group["id"])

    def tearDown(self):
        # pylint: disable-next=undefined-variable
        client.user.delete(self.session_key, self.test_user)

        for group_name in self.test_group_names:
            # pylint: disable-next=undefined-variable
            client.systemgroup.delete(self.session_key, group_name)

        # pylint: disable-next=undefined-variable
        RhnTestCase.tearDown(self)

    def test_add_assigned_system_groups(self):
        # pylint: disable-next=undefined-variable
        groups = client.user.listAssignedSystemGroups(self.session_key, self.test_user)
        self.assertEquals(0, len(groups))

        # pylint: disable-next=undefined-variable
        ret = client.user.addAssignedSystemGroups(
            self.session_key, self.test_user, self.test_group_ids, False
        )
        self.assertEquals(1, ret)

        # pylint: disable-next=undefined-variable
        groups = client.user.listAssignedSystemGroups(self.session_key, self.test_user)
        self.assertEquals(len(self.test_group_ids), len(groups))

    def test_add_assigned_system_groups_and_set_default(self):
        # pylint: disable-next=undefined-variable
        groups = client.user.listAssignedSystemGroups(self.session_key, self.test_user)
        self.assertEquals(0, len(groups))
        # pylint: disable-next=undefined-variable
        groups = client.user.listDefaultSystemGroups(self.session_key, self.test_user)
        self.assertEquals(0, len(groups))

        # pylint: disable-next=undefined-variable
        ret = client.user.addAssignedSystemGroups(
            self.session_key, self.test_user, self.test_group_ids, True
        )
        self.assertEquals(1, ret)

        # pylint: disable-next=undefined-variable
        groups = client.user.listAssignedSystemGroups(self.session_key, self.test_user)
        self.assertEquals(len(self.test_group_ids), len(groups))
        # pylint: disable-next=undefined-variable
        groups = client.user.listDefaultSystemGroups(self.session_key, self.test_user)
        self.assertEquals(len(self.test_group_ids), len(groups))

    def test_add_assigned_system_group(self):
        # pylint: disable-next=undefined-variable
        groups = client.user.listAssignedSystemGroups(self.session_key, self.test_user)
        self.assertEquals(0, len(groups))

        # pylint: disable-next=undefined-variable
        ret = client.user.addAssignedSystemGroup(
            self.session_key, self.test_user, self.test_group_ids[0], False
        )
        self.assertEquals(1, ret)

        # pylint: disable-next=undefined-variable
        groups = client.user.listAssignedSystemGroups(self.session_key, self.test_user)
        self.assertEquals(1, len(groups))


if __name__ == "__main__":
    unittest.main()
