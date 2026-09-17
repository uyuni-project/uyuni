# pylint: disable=missing-module-docstring
#   rhn-client-tools - RHN support tools and libraries
#
# Copyright (C) 2006--2012 Red Hat, Inc.
#
# SPDX-License-Identifier: GPL-2.0-only
import sys

# Adjust path so we can see the src modules running from branch as well
# as test dir:
sys.path.insert(0, "./src/")
sys.path.insert(0, "../src/")
sys.path.insert(0, "../../src/")
