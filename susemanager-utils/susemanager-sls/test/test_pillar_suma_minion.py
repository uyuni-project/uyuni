# -*- coding: utf-8 -*-
"""
:codeauthor:    Michael Calmer <Michael.Calmer@suse.com>
"""

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
        }
    }
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
        }
