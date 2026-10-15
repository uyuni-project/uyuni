#!/usr/bin/python3
#  pylint: disable=missing-module-docstring,invalid-name
from contextlib import contextmanager
from unittest.mock import MagicMock, patch

import pytest
from spacewalk.server.db_config import DBConfig


@contextmanager
def db_row(row):
    db_mock = MagicMock()
    db_mock.fetchone_dict = MagicMock(return_value=row)
    with patch("spacewalk.server.db_config.rhnSQL.initDB"), patch(
        "spacewalk.server.db_config.rhnSQL.prepare", MagicMock(return_value=db_mock)
    ):
        yield db_mock


@pytest.mark.parametrize(
    "row,expected",
    [
        ({"value": None, "default_value": "4"}, 4),
        ({"value": "8", "default_value": "4"}, 8),
        ({"value": "", "default_value": "4"}, None),
        ({"value": None, "default_value": None}, None),
        ({"value": None, "default_value": "1.5"}, 1.5),
        ({"value": None, "default_value": "text"}, "text"),
    ],
)
def test_get(row, expected):
    with db_row(row) as db_mock:
        cfg = DBConfig()
        assert cfg.get("SOME_KEY") == expected
        assert cfg.SOME_KEY == expected
        assert cfg.some_key == expected
        db_mock.execute.assert_called_with(key="SOME_KEY")


def test_get_default():
    with db_row({"value": "8", "default_value": "4"}):
        assert DBConfig().get_default("SOME_KEY") == 4


def test_string_keys_are_not_converted():
    with db_row({"value": None, "default_value": "123"}):
        assert DBConfig().get("PSW_CHECK_SPECIAL_CHARACTERS") == "123"


@pytest.mark.parametrize(
    "row,expected",
    [
        ({"value": None, "default_value": "true"}, True),
        ({"value": "0", "default_value": "true"}, False),
        ({"value": "yes", "default_value": "false"}, True),
        ({"value": None, "default_value": None}, False),
    ],
)
def test_get_bool(row, expected):
    with db_row(row):
        assert DBConfig().get_bool("SOME_KEY") == expected


def test_get_list():
    with db_row({"value": None, "default_value": "a, b,,c"}):
        assert DBConfig().get_list("SOME_KEY") == ["a", "b", "c"]
    with db_row({"value": None, "default_value": None}):
        assert DBConfig().get_list("SOME_KEY") == []


def test_missing_key():
    with db_row(None):
        with pytest.raises(AttributeError):
            _ = DBConfig().get("MISSING_KEY")
        with pytest.raises(AttributeError):
            _ = DBConfig().MISSING_KEY
