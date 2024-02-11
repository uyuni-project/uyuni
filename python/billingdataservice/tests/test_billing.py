#  pylint: disable=missing-module-docstring,unused-import
from unittest.mock import MagicMock, call, patch

import json
import pytest

import os

os.environ["TESTING"] = "1"

# pylint: disable-next=wrong-import-position
import billingdataservice


@patch("billingdataservice.initCFG", MagicMock())
@patch("billingdataservice.rhnSQL.initDB", MagicMock())
@patch("billingdataservice.rhnSQL.Statement", MagicMock())
@pytest.fixture
def client():
    # pylint: disable-next=unused-variable
    testdata1 = [
        {"usage_metric": "managed_systems", "count": "10"},
        {"usage_metric": "monitoring", "count": "5"},
    ]
    # pylint: disable-next=redefined-outer-name
    client = billingdataservice.app.test_client()
    with patch(
        "billingdataservice.rhnSQL.fetchone_dict", MagicMock(side_effect=["123", None])
    ):
        yield client


# pylint: disable-next=redefined-outer-name
def test_index(client):
    """Call index page."""

    rv = client.get("/")
    assert b"online" in rv.data

    rv = client.get("/")
    assert rv.status_code == 503


# pylint: disable-next=redefined-outer-name
def test_metering(client):
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
