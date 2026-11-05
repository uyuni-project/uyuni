# -*- coding: utf-8 -*-

# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

"""
Tests for the Salt ports in the minion configuration states.

The pillar of the actions is stored as JSON and read back by the server
with every number being a float: the states must cope with 30005.0.
"""

from pathlib import Path

import pytest
import yaml

jinja2 = pytest.importorskip("jinja2")

BOOTSTRAP_DIR = Path(__file__).resolve().parents[2] / "salt" / "bootstrap"


def render_susemanager_conf(pillar):
    template = jinja2.Environment().from_string(
        (BOOTSTRAP_DIR / "susemanager.conf").read_text()
    )
    return yaml.safe_load(template.render(pillar=pillar))


def test_susemanager_conf_ports_are_integers():
    conf = render_susemanager_conf(
        {
            "mgr_server": "proxy.example.com",
            "mgr_salt_publish_port": 30005.0,
            "mgr_salt_request_port": 30006.0,
        }
    )

    # 30005.0 == 30005, but Salt does not accept a float port
    assert isinstance(conf["publish_port"], int) and conf["publish_port"] == 30005
    assert isinstance(conf["master_port"], int) and conf["master_port"] == 30006


def test_susemanager_conf_without_ports_keeps_the_salt_defaults():
    for pillar in (
        {"mgr_server": "server.example.com"},
        {
            "mgr_server": "server.example.com",
            "mgr_salt_publish_port": "",
            "mgr_salt_request_port": "",
        },
    ):
        conf = render_susemanager_conf(pillar)

        assert "publish_port" not in conf
        assert "master_port" not in conf


def test_set_proxy_checks_integer_ports():
    checked = []

    def connect(unused_host, port, **unused_kwargs):
        checked.append(port)
        return {"result": True}

    pillar = {
        "mgr_server": "proxy.example.com",
        "mgr_salt_publish_port": 30005.0,
        "mgr_salt_request_port": 30006.0,
    }
    salt = {
        "pillar.get": lambda key, default=None: pillar.get(key, default),
        "network.connect": connect,
        "file.search": lambda *args: True,
    }
    template = jinja2.Environment(extensions=["jinja2.ext.do"]).from_string(
        (BOOTSTRAP_DIR / "set_proxy.sls").read_text()
    )
    template.render(
        pillar=pillar,
        salt=salt,
        grains={"pythonexecutable": "/usr/bin/python3"},
    )

    # socket.getaddrinfo, used by network.connect, only accepts int or str ports
    assert checked == [30005, 30006]
    assert all(isinstance(port, int) for port in checked)
