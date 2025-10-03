"""
cobbler helper module for kickstart macros
"""

import psycopg2
import configparser


def channels(distrotree):
    cnx = _connect_db()
    cursor = cnx.cursor()

    query = """SELECT CT.name, CT.label, CP.product
FROM rhnkickstartabletree T,
     rhnchanneltreeview CT,
     rhnchannel C
     LEFT JOIN rhnchannelproduct CP ON C.channel_product_id = CP.id
WHERE T.channel_id = CT.parent_or_self_id
  AND CT.id = C.id
  AND T.label = %s;"""

    cursor.execute(query, (distrotree,))
    result = {}
    for name, label, product in cursor.fetchall():
        result[label] = {"name": name, "product": product}

    cnx.close()
    return result


def _connect_db():
    config = configparser.ConfigParser()
    with open("/etc/rhn/rhn.conf", "r", encoding="utf-8") as fd:
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


def get_ssl_ca_cert():
    with open(
        "/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT", "r", encoding="utf-8"
    ) as fd:
        content = fd.read()
        return content
