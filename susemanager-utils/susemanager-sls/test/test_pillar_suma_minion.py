# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

# -*- coding: utf-8 -*-
"""
:codeauthor:    Michael Calmer <Michael.Calmer@suse.com>
"""

from decimal import Decimal
from unittest.mock import MagicMock, patch

import pytest
import sys

sys.path.append("../modules/pillar")
# pylint: disable-next=wrong-import-position
import os

# pylint: disable-next=wrong-import-position
import suma_minion

suma_minion.__opts__ = {}
suma_minion.__context__ = {}
suma_minion.psycopg2 = MagicMock()

TEST_FORMULA_ORDER = [
    "branch-network",
    "cpu-mitigations",
    "dhcpd",
    "grafana",
    "image-synchronize",
    "locale",
    "prometheus",
    "prometheus-exporters",
    "pxe",
    "saltboot",
    "tftpd",
    "virtualization-host",
    "vsftpd",
    "bind",
]


def cursor_callback(cursor):
    assert cursor is not None


@pytest.fixture(autouse=True)
def data_paths():
    """
    Set the test data paths
    """
    suma_minion.FORMULAS_DATA_PATH = os.path.sep.join([os.path.abspath(""), "data"])
    suma_minion.FORMULA_ORDER_FILE = os.path.sep.join(
        [os.path.abspath(""), "data", "formula_order.json"]
    )
    suma_minion.MANAGER_FORMULAS_METADATA_MANAGER_PATH = os.path.sep.join(
        [os.path.abspath(""), "data", "formulas", "metadata"]
    )


@pytest.mark.parametrize("has_psycopg2", [True, False])
def test_virtual(has_psycopg2):
    """
    Test virtual returns the module name
    """
    with patch("suma_minion.HAS_POSTGRES", has_psycopg2):
        assert suma_minion.__virtual__() == has_psycopg2


def test_formula_pillars_db():
    """
    Test getting the formulas from the database
    """
    minion_id = "suma-refhead-min-sles12sp4.mgr.suse.de"
    pillar = {"group_ids": [9]}

    cursor = MagicMock()
    cursor.fetchall.return_value = [({"formula_order": TEST_FORMULA_ORDER},)]
    pillar = suma_minion.load_global_pillars(cursor, pillar)

    cursor.fetchall.return_value = [("formula-locale", {}), ("formula-tftpd", {})]
    group_formulas, pillar = suma_minion.load_group_pillars(minion_id, cursor, pillar)

    cursor.fetchall.return_value = [("formula-branch-network", {})]
    system_formulas, pillar = suma_minion.load_system_pillars(minion_id, cursor, pillar)

    pillar = suma_minion.formula_pillars(system_formulas, group_formulas, pillar)
    assert "formulas" in pillar
    assert pillar["formulas"] == ["branch-network", "locale", "tftpd"]


def test_reading_postgres_opts_in__get_cursor():
    """
    Test reading proper postgres opts in _get_cursor
    """
    pg_connect_mock = MagicMock(return_value=MagicMock())
    test_opts = {
        "postgres": {
            "host": "test_host",
            "user": "test_user",
            "pass": "test_pass",
            "db": "test_db",
            "port": 1234,
            "sslmode": "disable",
        }
    }
    with patch.object(suma_minion, "__opts__", test_opts), patch(
        "suma_minion.psycopg2.connect", pg_connect_mock
    ), patch.dict(suma_minion.__context__, {}):
        # pylint: disable-next=protected-access
        suma_minion._get_cursor(cursor_callback)
        assert pg_connect_mock.call_args_list[0][1] == {
            "host": "test_host",
            "user": "test_user",
            "password": "test_pass",
            "dbname": "test_db",
            "port": 1234,
            "sslmode": "disable",
        }

    pg_connect_mock.reset_mock()

    with patch.object(suma_minion, "__opts__", {"__master_opts__": test_opts}), patch(
        "suma_minion.psycopg2.connect", pg_connect_mock
    ), patch.dict(suma_minion.__context__, {}):
        # pylint: disable-next=protected-access
        suma_minion._get_cursor(cursor_callback)
        assert pg_connect_mock.call_args_list[0][1] == {
            "host": "test_host",
            "user": "test_user",
            "password": "test_pass",
            "dbname": "test_db",
            "port": 1234,
            "sslmode": "disable",
        }


def test_using_context_in__get_cursor():
    """
    Test using context to store postgres postgres connection in  _get_cursor
    """
    pg_connect_mock = MagicMock(return_value=MagicMock())
    test_opts = {
        "postgres": {
            "host": "test_host",
            "user": "test_user",
            "pass": "test_pass",
            "db": "test_db",
            "port": 1234,
            "sslmode": "disable",
        }
    }
    with patch.object(
        suma_minion, "__opts__", {"id": "foobar_master", **test_opts}
    ), patch("suma_minion.psycopg2.connect", pg_connect_mock), patch.dict(
        suma_minion.__context__, {}
    ):
        # Check if it creates new connection if it's not in the context
        # pylint: disable-next=protected-access
        suma_minion._get_cursor(cursor_callback)

        assert pg_connect_mock.call_args_list[0][1] == {
            "host": "test_host",
            "user": "test_user",
            "password": "test_pass",
            "dbname": "test_db",
            "port": 1234,
            "sslmode": "disable",
        }

        pg_connect_mock.reset_mock()

        # Check if it reuses the connection from the context
        # pylint: disable-next=protected-access
        suma_minion._get_cursor(cursor_callback)

        assert pg_connect_mock.call_args_list[0][1] == {
            "host": "test_host",
            "user": "test_user",
            "password": "test_pass",
            "dbname": "test_db",
            "port": 1234,
            "sslmode": "disable",
        }
        # pylint: disable-next=unnecessary-negation
        assert not "suma_minion_cnx" in suma_minion.__context__

    pg_connect_mock.reset_mock()

    with patch.object(suma_minion, "__opts__", test_opts), patch(
        "suma_minion.psycopg2.connect", pg_connect_mock
    ), patch.dict(suma_minion.__context__, {}):
        # Check if it creates new connection if it's not in the context
        # pylint: disable-next=protected-access
        suma_minion._get_cursor(cursor_callback)
        assert pg_connect_mock.call_args_list[0][1] == {
            "host": "test_host",
            "user": "test_user",
            "password": "test_pass",
            "dbname": "test_db",
            "port": 1234,
            "sslmode": "disable",
        }

        pg_connect_mock.reset_mock()

        # Check if it reuses the connection from the context
        # pylint: disable-next=protected-access
        suma_minion._get_cursor(cursor_callback)

        pg_connect_mock.assert_not_called()

        assert "suma_minion_cnx" in suma_minion.__context__

    pg_connect_mock.reset_mock()

    pg_cnx_mock = MagicMock()
    pg_cnx_mock.cursor = MagicMock(side_effect=[True, Exception])

    with patch.object(suma_minion, "__opts__", test_opts), patch(
        "suma_minion.psycopg2.connect", pg_connect_mock
    ), patch.object(suma_minion.psycopg2, "InterfaceError", Exception), patch.dict(
        suma_minion.__context__, {"suma_minion_cnx": pg_cnx_mock}
    ):
        # Check if it reuses the connection from the context
        # pylint: disable-next=protected-access
        suma_minion._get_cursor(cursor_callback)

        pg_cnx_mock.cursor.assert_called_once()

        pg_connect_mock.assert_not_called()

        # Check if it tries to recoonect if the connection in the context is not alive
        # pylint: disable-next=protected-access
        suma_minion._get_cursor(cursor_callback)

        assert pg_connect_mock.call_args_list[0][1] == {
            "host": "test_host",
            "user": "test_user",
            "password": "test_pass",
            "dbname": "test_db",
            "port": 1234,
            "sslmode": "disable",
        }


def test_clm_pillar():
    """
    Test loading the CLM project, environment, version and message
    """
    cursor = MagicMock()
    cursor.fetchall.return_value = [
        ("rhel9-base", "prod", Decimal("3"), "Security update", True)
    ]
    pillar = suma_minion.load_clm_pillar("minion1", cursor, {"org_id": 1})

    assert pillar == {
        "org_id": 1,
        "clm": {
            "project": "rhel9-base",
            "environment": "prod",
            "version": 3,
            "message": "Security update",
        },
    }
    assert cursor.execute.call_args[0][1] == {
        "minion_id": "minion1",
        "projects": None,
    }


def test_clm_pillar_not_in_clm():
    """
    Test the pillar is unchanged if the minion uses no CLM channels
    """
    cursor = MagicMock()
    cursor.fetchall.return_value = []

    assert suma_minion.load_clm_pillar("minion1", cursor, {"org_id": 1}) == {
        "org_id": 1
    }


@pytest.mark.parametrize(
    "opts,expected",
    [
        ({"clm_pillar_projects": "rhel9-base"}, ["rhel9-base"]),
        (
            {"clm_pillar_projects": ["rhel9-base", "sles15sp6-base"]},
            ["rhel9-base", "sles15sp6-base"],
        ),
        ({"__master_opts__": {"clm_pillar_projects": ["rhel9-base"]}}, ["rhel9-base"]),
        ({"clm_pillar_projects": []}, None),
    ],
)
def test_clm_pillar_projects_filter(opts, expected):
    """
    Test the CLM projects configured on the master are passed to the query
    """
    cursor = MagicMock()
    cursor.fetchall.return_value = []
    with patch.object(suma_minion, "__opts__", opts):
        suma_minion.load_clm_pillar("minion1", cursor, {})

    assert cursor.execute.call_args[0][1]["projects"] == expected


def test_clm_pillar_several_environments():
    """
    Test the environment of the base channel wins over child channels
    """
    cursor = MagicMock()
    cursor.fetchall.return_value = [
        ("rhel9-base", "prod", Decimal("3"), None, True),
        ("rhel9-extras", "dev", Decimal("7"), "Add tools", False),
    ]
    with patch.object(suma_minion.log, "warning") as warning_mock:
        pillar = suma_minion.load_clm_pillar("minion1", cursor, {})

    assert pillar["clm"] == {
        "project": "rhel9-base",
        "environment": "prod",
        "version": 3,
        "message": "",
    }
    warning_mock.assert_called_once()


def test_clm_pillar_db_error():
    """
    Test a failing CLM query leaves the pillar unchanged and rolls back
    """
    cursor = MagicMock()
    cursor.execute.side_effect = Exception("relation does not exist")
    with patch.object(suma_minion.psycopg2, "Error", Exception):
        pillar = suma_minion.load_clm_pillar("minion1", cursor, {"org_id": 1})

    assert pillar == {"org_id": 1}
    cursor.connection.rollback.assert_called_once()
