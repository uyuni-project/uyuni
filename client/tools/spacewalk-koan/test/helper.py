# -*- coding: utf-8 -*-
#
# Copyright (C) 2015 SUSE LINUX Products GmbH, Nuernberg, Germany.
#   This library is free software; you can redistribute it and/or modify
# it only under the terms of version 2.1 of the GNU Lesser General Public
# License as published by the Free Software Foundation.
#
#   This library is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
# details.
#
#   You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA


import os

def read_data_from_fixture(filename):
    with open(path_to_fixture(filename), 'r') as file:
        return file.read()


def path_to_fixture(filename):
    return os.path.join(os.path.dirname(os.path.realpath(__file__)),
                        "fixtures", filename)



