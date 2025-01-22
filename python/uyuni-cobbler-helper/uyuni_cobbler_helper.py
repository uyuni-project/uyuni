import psycopg2
import configparser


def channels(distrotree):
    cnx = _connect_db()
    cursor = cnx.cursor()

    query = """SELECT CT.name, CT.label, CP.product
FROM rhnkickstartabletree T,
     rhnchanneltreeview CT,
     rhnchannel C,
     rhnchannelproduct CP
WHERE T.channel_id = CT.parent_or_self_id
  AND CT.id = C.id
  AND C.channel_product_id = CP.id
  AND T.label = %s;"""

    cursor.execute(query, (distrotree,))
    channels = {}
    for row in cursor.fetchall():
        channels[row[1]] = {"name": row[0], "product": row[2]}

    cnx.close()
    return channels


def _connect_db():
    config = configparser.ConfigParser()
    with open("/etc/rhn/rhn.conf", "r") as fd:
        content = "[default]\n" + fd.read()
        config.read_string(content)

    # pylint: disable-next=undefined-variable
    return psycopg2.connect(
        host=config.get("default", "db_host"),
        user=config.get("default", "db_user"),
        password=config.get("default", "db_password"),
        dbname=config.get("default", "db_name"),
        port=int(config.get("default", "db_port")),
    )
