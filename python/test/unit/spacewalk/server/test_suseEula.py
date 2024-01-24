#!/usr/bin/python3
#  pylint: disable=missing-module-docstring,invalid-name
import hashlib
from unittest.mock import MagicMock, call, patch

import pytest
from spacewalk.server import suseEula


@pytest.mark.parametrize(
    "sql_return,expected",
    [({"text": "This is the EULA."}, "This is the EULA."), ({}, None)],
)
def test_get_eula(sql_return, expected):
    db_mock = MagicMock()
    db_mock.fetchone_dict = MagicMock(return_value=sql_return)
    with patch(
        "spacewalk.server.suseEula.rhnSQL.prepare", MagicMock(return_value=db_mock)
    ):
        eula = suseEula.get_eula_by_checksum("mocked away")
        assert eula == expected
        assert isinstance(eula, (type(None), str))

        eula = suseEula.get_eula_by_id("mocked away")
        assert eula == expected
        assert isinstance(eula, (type(None), str))


def test_find_or_create_eula():
    eula = "This is a potentially new EULA."
    checksum = hashlib.new("sha256", eula.encode("utf-8", "ignore")).hexdigest()

    db_mock = MagicMock()

    # eula is found
    db_mock.fetchone_dict = MagicMock(return_value={"id": 42})
    with patch(
        "spacewalk.server.suseEula.rhnSQL.prepare", MagicMock(return_value=db_mock)
    ):
        assert suseEula.find_or_create_eula(eula) == 42

    # eula is created
    db_mock.fetchone_dict = MagicMock(side_effect=[None, {"id": 43}])
    db_mock.execute = MagicMock()
    with patch(
        "spacewalk.server.suseEula.rhnSQL.prepare", MagicMock(return_value=db_mock)
    ):
        assert suseEula.find_or_create_eula(eula) == 43
        assert call(id=43, text=eula, checksum=checksum) in db_mock.execute.mock_calls
