# pylint: disable=missing-module-docstring
import unittest

# pylint: disable-next=wildcard-import
from config import *


class ApiTests(unittest.TestCase):
    def test_version(self):
        # pylint: disable-next=undefined-variable
        self.assertEquals("5.0.0", client.api.system_version())
        # pylint: disable-next=undefined-variable
        self.assertEquals("5.0.0 Java", client.api.get_version())


if __name__ == "__main__":
    unittest.main()
