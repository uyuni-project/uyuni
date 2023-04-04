from unittest.mock import MagicMock, call, patch

import json
import pytest

import os
os.environ['TESTING'] = "1"

import billingdataservice

@patch("billingdataservice.initCFG", MagicMock())
@patch("billingdataservice.rhnSQL.initDB", MagicMock())
@patch("billingdataservice.rhnSQL.Statement", MagicMock())
@pytest.fixture
def client():
    testdata1 = [{"dimension": "1", "count": "10"}, {"dimension": "2", "count": "5"}]
    client = billingdataservice.app.test_client()
    with patch(
            "billingdataservice.rhnSQL.fetchone_dict",
            MagicMock(side_effect=["123", None])
    ), patch(
            "billingdataservice.rhnSQL.fetchall_dict",
            MagicMock(side_effect=[testdata1])
    ):
        yield client

def test_index(client):
    """Call index page."""

    rv = client.get('/')
    assert b'online' in rv.data

    rv = client.get('/')
    assert rv.status_code == 503

def test_metering(client):
    """Call metering API"""

    rv = client.get('/metering')
    assert rv.status_code == 200
    r = json.loads(rv.data)
    assert "dimensions" in r
    for dim in r["dimensions"]:
        assert "dimension" in dim
        if dim["dimension"] == "1":
            assert dim["count"] == "10"
        elif dim["dimension"] == "2":
            assert dim["count"] == "5"
        else:
            assert False


