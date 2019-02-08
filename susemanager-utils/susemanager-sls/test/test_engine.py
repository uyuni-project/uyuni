import logging
import pytest
import psycopg2
import shlex
import subprocess
from mgr_events import Responder, DEFAULT_COMMIT_BURST
from mock import MagicMock, patch, call
from sqlalchemy import create_engine
from sqlalchemy_utils import database_exists, create_database, drop_database


ch = logging.StreamHandler()
ch.setLevel(logging.DEBUG)
log = logging.getLogger('mgr_events')
log.setLevel(logging.DEBUG)
log.addHandler(ch)


@pytest.fixture(scope="session")
def postgres(request):
    proc = subprocess.Popen(shlex.split("su postgres -c \"pg_ctl -D ~/data -l ~/logfile start\""))
    def finalizer():
        subprocess.Popen(shlex.split("su postgres -c \"pg_ctl stop -D /var/lib/pgsql/data\""))
    request.addfinalizer(finalizer)
    outs, errs = proc.communicate(timeout=15)
    yield proc


@pytest.fixture(scope="session")
def db_engine(postgres):
    return create_engine("postgresql://postgres@/test")


@pytest.fixture
def db_connection(db_engine):
    if not database_exists(db_engine.url):
        create_database(db_engine.url)
    with psycopg2.connect(user='postgres', host="localhost", dbname="test") as connection:
        yield connection
    drop_database(db_engine.url)


def new_connection():
    return psycopg2.connect(user='postgres', host="localhost", dbname="test")


@pytest.fixture
def create_tables(db_connection):
    sql = """CREATE TABLE suseSaltEvent (
        id SERIAL PRIMARY KEY,
        minion_id CHARACTER VARYING(256),
        data TEXT NOT NULL
    );"""
    db_connection.cursor().execute(sql)
    db_connection.commit()


def delete_table(conn, table):
    conn.cursor().execute("DELETE FROM %s" % table)
    conn.commit()


@pytest.fixture
def responder(db_connection, create_tables):
    with patch('mgr_events.psycopg2') as mock_psycopg2:
        mock_psycopg2.connect.return_value = db_connection
        return Responder(
            MagicMock(),  # mock event_bus
            {
                'postgres_db': {
                     'dbname': 'tests',
                     'user': 'postgres',
                     'password': '',
                     'host': 'localhost',
                     'notify_channel': 'suseSaltEvent'
                 }
            }
        )


def test_connection_recovery_on_insert(db_connection, responder):
    disposable_connection = new_connection()
    responder.connection = disposable_connection
    responder._insert('salt/minion/1/start', {'value': 1})
    responder.connection.close()
    with patch('mgr_events.psycopg2') as mock_psycopg2:
        mock_psycopg2.connect.return_value = db_connection
        responder._insert('salt/minion/2/start', {'value': 2})
    responder.connection.commit()
    responder.cursor.execute("SELECT * FROM suseSaltEvent")
    resp = responder.cursor.fetchall()
    assert len(resp) == 2


def test_connection_recovery_on_commit(db_connection, responder):
    responder.connection = new_connection()
    responder._insert('salt/minion/1/start', {'value': 1})
    responder.connection.close()
    with patch('mgr_events.psycopg2') as mock_psycopg2:
        mock_psycopg2.connect.return_value = db_connection
        responder.attempt_commit()
    responder.connection.commit()
    responder.cursor.execute("SELECT * FROM suseSaltEvent")
    resp = responder.cursor.fetchall()
    assert len(resp) == 1


def test_insert_start_event(responder, db_connection):
    responder.event_bus.unpack.return_value = ('salt/minion/12345/start', {'value': 1})
    responder.add_event_to_queue('')
    responder.cursor.execute("SELECT * FROM suseSaltEvent;")
    resp = responder.cursor.fetchall()
    assert resp
    assert responder.tokens == DEFAULT_COMMIT_BURST - 1


def test_insert_job_return_event(responder):
    responder.event_bus.unpack.return_value = ('salt/job/12345/ret/6789', {'value': 1})
    responder.add_event_to_queue('')
    responder.cursor.execute("SELECT * FROM suseSaltEvent;")
    resp = responder.cursor.fetchall()
    assert resp
    assert responder.tokens == DEFAULT_COMMIT_BURST - 1


def test_commit_scheduled_on_init(responder):
    assert responder.event_bus.io_loop.call_later.call_count == 1


def test_commit_empty_queue(responder):
    responder.counter = 0
    with patch.object(responder, 'event_bus', MagicMock()):
        with patch.object(responder, 'connection') as mock_connection:
            mock_connection.closed = False
            responder.attempt_commit()
            assert responder.connection.commit.call_count == 0
        assert responder.tokens == DEFAULT_COMMIT_BURST


def test_postgres_notification(responder):
    with patch.object(responder, 'cursor'):
        responder._insert('salt/minion/1/start', {'value': 1})
        assert responder.counter == 0
        assert responder.tokens == DEFAULT_COMMIT_BURST -1
        assert responder.cursor.execute.mock_calls[-1:] == [call('NOTIFY suseSaltEvent;')]

def test_add_token(responder):
    responder.tokens = 0
    responder.add_token()
    assert responder.tokens == 1

def test_add_token_max(responder):
    responder.add_token()
    assert responder.tokens == DEFAULT_COMMIT_BURST

def test_commit_avoidance_without_tokens(responder):
    with patch.object(responder, 'cursor'):
        with patch.object(responder, 'connection') as mock_connection:
            mock_connection.closed = False
            responder.tokens = 0
            responder._insert('salt/minion/1/start', {'id': 'testminion', 'value': 1})
            assert responder.counter == 1
            assert responder.tokens == 0
            assert responder.connection.commit.call_count == 0
            assert responder.cursor.execute.mock_calls == [call('INSERT INTO suseSaltEvent (minion_id, data) VALUES (%s, %s);', ('testminion', '{"tag": "salt/minion/1/start", "data": {"id": "testminion", "value": 1}}',))]


def test_postgres_connect(db_connection, responder):
    disposable_connection = new_connection()
    disposable_connection.close()
    responder.connection = disposable_connection
    with patch('mgr_events.time') as mock_time:
        with patch('mgr_events.psycopg2') as mock_psycopg2:
            mock_psycopg2.connect.side_effect = [psycopg2.OperationalError, db_connection] 
            mock_psycopg2.OperationalError = psycopg2.OperationalError
            responder.db_keepalive()
            assert mock_psycopg2.connect.call_count == 2
    mock_time.sleep.assert_called_once_with(5)


def test_postgres_connect_with_port(responder):
    responder.config['postgres_db']['port'] = '1234'
    with patch('mgr_events.psycopg2') as mock_psycopg2:
        responder._connect_to_database()
        mock_psycopg2.connect.assert_called_once_with(u"dbname='tests' user='postgres' host='localhost' port='1234' password=''")
