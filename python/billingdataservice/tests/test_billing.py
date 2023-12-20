from unittest.mock import MagicMock, call, patch  #  pylint: disable=missing-module-docstring,unused-import

import json
import pytest

import os

os.environ["TESTING"] = "1"

import billingdataservice  #  pylint: disable=wrong-import-position


@patch("billingdataservice.initCFG", MagicMock())
@patch("billingdataservice.rhnSQL.initDB", MagicMock())
@patch("billingdataservice.rhnSQL.Statement", MagicMock())
@pytest.fixture
def client():
    testdata1 = [  #  pylint: disable=unused-variable
        {"usage_metric": "managed_systems", "count": "10"},
        {"usage_metric": "monitoring", "count": "5"},
    ]
    client = billingdataservice.app.test_client()  #  pylint: disable=redefined-outer-name
    with patch(
        "billingdataservice.rhnSQL.fetchone_dict", MagicMock(side_effect=["123", None])
    ):
        yield client


def test_index(client):  #  pylint: disable=redefined-outer-name
    """Call index page."""

    rv = client.get("/")
    assert b"online" in rv.data

    rv = client.get("/")
    assert rv.status_code == 503


def test_metering(client):  #  pylint: disable=redefined-outer-name
    """Call metering API"""
    mock_fetchall_dict = MagicMock(
        name="mock2",
        return_value=[
            {"usage_metric": "managed_systems", "count": "10"},
            {"usage_metric": "monitoring", "count": "5"},
        ],
    )
    mock_cursor = MagicMock(name="mock1")
    mock_cursor.fetchall_dict = mock_fetchall_dict

    with patch(
        "billingdataservice.rhnSQL.prepare", MagicMock(return_value=mock_cursor)
    ):
        rv = client.get("/metering")
        assert rv.status_code == 200
        r = json.loads(rv.data)
        assert "usage_metrics" in r
        for dim in r["usage_metrics"]:
            assert "usage_metric" in dim
            if dim["usage_metric"] == "managed_systems":
                assert dim["count"] == "10"
            elif dim["usage_metric"] == "monitoring":
                assert dim["count"] == "5"
            else:
                assert False
