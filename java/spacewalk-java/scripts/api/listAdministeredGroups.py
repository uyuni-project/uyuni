#!/usr/bin/python
# pylint: disable=missing-module-docstring,invalid-name
# pylint: disable-next=wildcard-import
from config import *
from pprint import pprint

# pylint: disable-next=undefined-variable
key = login()
# pylint: disable-next=undefined-variable
pprint(client.user.list_administered_system_groups(key, "admin"))
# pylint: disable-next=undefined-variable
logout(key)
