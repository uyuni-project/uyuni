#!/usr/bin/python
# pylint: disable=missing-module-docstring

import unittest

from random import randint

# pylint: disable-next=wildcard-import
from config import *


# pylint: disable-next=missing-class-docstring,undefined-variable
class ConfigChannel(RhnTestCase):

    def setUp(self):
        # pylint: disable-next=undefined-variable
        RhnTestCase.setUp(self)

    def tearDown(self):
        # pylint: disable-next=undefined-variable
        RhnTestCase.tearDown(self)

    def test_schedule_file_comparisons(self):

        random_int = randint(1, 1000000)
        # pylint: disable-next=consider-using-f-string
        channel_label = "apitest_channel%s" % random_int
        # pylint: disable-next=consider-using-f-string
        channel_name = "apitest channel%s" % random_int
        channel_description = "channel description"

        # pylint: disable-next=undefined-variable,unused-variable
        channel_details = client.configchannel.create(
            self.session_key, channel_label, channel_name, channel_description
        )
        #        print channel_details

        path = "/tmp/test_file.sh"
        path_info = {
            "contents": "echo hello",
            "owner": "root",
            "group": "root",
            "permissions": "644",
            "macro-start-delimiter": "{|",
            "macro-end-delimiter": "|}",
        }
        # pylint: disable-next=undefined-variable
        client.configchannel.createOrUpdatePath(
            self.session_key, channel_label, path, False, path_info
        )

        # pylint: disable-next=invalid-name,undefined-variable
        actionId = client.configchannel.scheduleFileComparisons(
            # pylint: disable-next=undefined-variable
            self.session_key,
            channel_label,
            path,
            # pylint: disable-next=undefined-variable
            [SERVER_ID],
        )

        # pylint: disable-next=undefined-variable
        action_details = client.schedule.listInProgressSystems(
            self.session_key, actionId
        )
        #        print action_details

        self.assertTrue(len(action_details) > 0)

        # clean up from test
        # pylint: disable-next=undefined-variable
        client.configchannel.deleteChannels(self.session_key, [channel_label])


if __name__ == "__main__":
    unittest.main()
