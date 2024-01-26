#!/usr/bin/python3
# pylint: disable=missing-module-docstring,invalid-name
import base64
from contextlib import contextmanager
from datetime import datetime, timedelta, timezone
import hashlib
import os
import sys

import click
import jwt
import psycopg2
from psycopg2.extensions import register_adapter
from psycopg2.extras import Json
from rich.console import Console

register_adapter(dict, Json)

console = Console()


@contextmanager
def connect_db(host, port, user, password, db):
    cnx = psycopg2.connect(
        host=host, user=user, password=password, dbname=db, port=port
    )
    try:
        yield cnx
    except psycopg2.DatabaseError as err:
        console.print("[bold red]Database error: %s", err.args)
    finally:
        cnx.close()


def get_system_id(cursor, name):
    cursor.execute("SELECT id from rhnServer WHERE name = %s;", (name,))
    row = cursor.fetchone()
    return row[0] if row else None


def get_sequence_next_value(cursor, name):
    cursor.execute("SELECT nextval(%s);", (name,))
    return cursor.fetchone()[0]


# pylint: disable-next=redefined-builtin
def clone_rhnserver(cursor, id, clone_name):
    new_id = get_sequence_next_value(cursor, "rhn_server_id_seq")

    machine_id = hashlib.md5(clone_name.encode("utf-8")).hexdigest()
    cursor.execute(
        """
        SELECT
            org_id, server_arch_id, os, release, description, info, secret,
            creator_id, auto_update, contact_method_id, running_kernel, last_boot,
            provision_state_id, channels_changed, cobbler_id, hostname,
            payg, maintenance_schedule_id
        FROM rhnserver WHERE id = %s;
        """,
        (id,),
    )
    row = cursor.fetchone()

    cursor.execute(
        """insert into rhnServer (
                id, name, digital_server_id, machine_id, org_id, server_arch_id, os,
                release, description, info, secret, creator_id, auto_update,
                contact_method_id, running_kernel, last_boot, provision_state_id,
                channels_changed, cobbler_id, hostname, payg, maintenance_schedule_id
            ) VALUES (
                %s, %s, %s, %s, %s, %s,
                %s, %s, %s, %s, %s, %s,
                %s, %s, %s, %s, %s,
                %s, %s, %s, %s, %s
            );""",
        (new_id, clone_name, machine_id, machine_id, *row),
    )

    return new_id


def do_clone_using_id(cursor, table, columns, key, old, new, converter=None):
    columns_str = ", ".join(columns)
    cursor.execute(
        # pylint: disable-next=consider-using-f-string
        """
        SELECT {}
        FROM {}
        WHERE {} = %s;
        """.format(
            columns_str, table, key
        ),
        (old,),
    )
    for row in cursor.fetchall():
        # Convert the row values if needed
        if converter:
            (columns, row) = converter(row)

        cursor.execute(
            # pylint: disable-next=consider-using-f-string
            """
            INSERT INTO {} (
                {}, {}
            ) VALUES (%s, {});""".format(
                table,
                key,
                ", ".join(columns),
                ("%s, " * (len(columns)))[:-2],
            ),
            (new, *row),
        )


# pylint: disable-next=redefined-builtin
def clone_suseminioninfo(cursor, id, new_id, clone_name):
    columns = ["os_family", "kernel_live_version", "ssh_push_port"]

    def converter(row):
        return (columns + ["minion_id"], list(row) + [clone_name])

    do_clone_using_id(
        cursor,
        "suseminioninfo",
        columns,
        "server_id",
        id,
        new_id,
        converter=converter,
    )


# pylint: disable-next=redefined-builtin
def clone_susechannelaccesstoken(cursor, id, new_id, secret_key):
    columns = ["token", "created", "expiration", "valid"]

    def row_converter(row):
        token = jwt.decode(
            row[0],
            secret_key,
            algorithms=["HS256"],
            options={"verify_signature": False},
        )
        created = datetime.now(tz=timezone.utc)
        exp = created + timedelta(days=365)
        token["iat"] = created
        token["nbf"] = created
        token["exp"] = exp
        token["jti"] = base64.b64encode(os.urandom(16)).decode("ascii")
        token_id = get_sequence_next_value(cursor, "suse_chan_access_token_id_seq")
        new_row = [
            jwt.encode(token, secret_key, algorithm="HS256"),
            created,
            exp,
            "Y",
            token_id,
        ]
        return (columns + ["id"], new_row)

    do_clone_using_id(
        cursor,
        "susechannelaccesstoken",
        columns,
        "minion_id",
        id,
        new_id,
        converter=row_converter,
    )


# pylint: disable-next=redefined-builtin
def clone_rhnclientcapability(cursor, id, new_id):
    columns = ["capability_name_id", "version"]
    do_clone_using_id(cursor, "rhnclientcapability", columns, "server_id", id, new_id)


# pylint: disable-next=redefined-builtin
def clone_rhncpu(cursor, id, new_id):
    columns = [
        "cpu_arch_id",
        "bogomips",
        "cache",
        "family",
        "mhz",
        "stepping",
        "flags",
        "model",
        "version",
        "vendor",
        "nrcpu",
        "nrsocket",
        "acpiversion",
        "apic",
        "apmversion",
        "chipset",
    ]

    def converter(row):
        return (
            ["id"] + columns,
            [get_sequence_next_value(cursor, "rhn_cpu_id_seq")] + list(row),
        )

    do_clone_using_id(
        cursor, "rhncpu", columns, "server_id", id, new_id, converter=converter
    )


# pylint: disable-next=redefined-builtin
def clone_rhndevice(cursor, id, new_id):
    columns = [
        "class",
        "bus",
        "detached",
        "device",
        "driver",
        "description",
        "pcitype",
        "prop1",
        "prop2",
        "prop3",
        "prop4",
    ]

    def converter(row):
        return (
            ["id"] + columns,
            [get_sequence_next_value(cursor, "rhn_hw_dev_id_seq")] + list(row),
        )

    do_clone_using_id(
        cursor, "rhndevice", columns, "server_id", id, new_id, converter=converter
    )


# pylint: disable-next=redefined-builtin
def clone_susesaltpillar(cursor, id, new_id):
    columns = ["category", "pillar"]

    def converter(row):
        return (
            ["id"] + columns,
            [get_sequence_next_value(cursor, "suse_salt_pillar_id_seq")] + list(row),
        )

    do_clone_using_id(
        cursor,
        "susesaltpillar",
        columns,
        "server_id",
        id,
        new_id,
        converter=converter,
    )


# pylint: disable-next=redefined-builtin
def clone_rhnram(cursor, id, new_id):
    columns = ["ram", "swap"]

    def converter(row):
        return (
            ["id"] + columns,
            [get_sequence_next_value(cursor, "rhn_ram_id_seq")] + list(row),
        )

    do_clone_using_id(
        cursor, "rhnram", columns, "server_id", id, new_id, converter=converter
    )


# pylint: disable-next=redefined-builtin
def clone_rhnserverdmi(cursor, id, new_id):
    columns = [
        "vendor",
        "system",
        "product",
        "bios_vendor",
        "bios_version",
        "bios_release",
        "asset",
        "board",
    ]

    def converter(row):
        return (
            ["id"] + columns,
            [get_sequence_next_value(cursor, "rhn_server_dmi_id_seq")] + list(row),
        )

    do_clone_using_id(
        cursor,
        "rhnserverdmi",
        columns,
        "server_id",
        id,
        new_id,
        converter=converter,
    )


# pylint: disable-next=redefined-builtin
def clone_rhnserverfqdn(cursor, id, new_id):
    columns = ["name", "is_primary"]

    def converter(row):
        return (
            ["id"] + columns,
            [get_sequence_next_value(cursor, "rhn_serverfqdn_id_seq")] + list(row),
        )

    do_clone_using_id(
        cursor,
        "rhnserverfqdn",
        columns,
        "server_id",
        id,
        new_id,
        converter=converter,
    )


# pylint: disable-next=redefined-builtin
def clone_rhnserverhistory(cursor, id, new_id):
    columns = ["summary", "details"]

    def converter(row):
        return (
            ["id"] + columns,
            [get_sequence_next_value(cursor, "rhn_event_id_seq")] + list(row),
        )

    do_clone_using_id(
        cursor,
        "rhnserverhistory",
        columns,
        "server_id",
        id,
        new_id,
        converter=converter,
    )


# pylint: disable-next=redefined-builtin
def clone_rhnserverneededcache(cursor, id, new_id):
    columns = ["errata_id", "package_id", "channel_id"]
    do_clone_using_id(cursor, "rhnserverneededcache", columns, "server_id", id, new_id)


# pylint: disable-next=redefined-builtin
def clone_rhnservernetinterface(cursor, id, new_id):
    columns = ["name", "hw_addr", "module", "is_primary"]

    def converter(row):
        return (
            ["id"] + columns,
            [get_sequence_next_value(cursor, "rhn_srv_net_iface_id_seq")] + list(row),
        )

    do_clone_using_id(
        cursor,
        "rhnservernetinterface",
        columns,
        "server_id",
        id,
        new_id,
        converter=converter,
    )


# pylint: disable-next=redefined-builtin
def clone_rhnserverpackage(cursor, id, new_id):
    columns = ["name_id", "evr_id", "package_arch_id", "installtime"]
    do_clone_using_id(cursor, "rhnserverpackage", columns, "server_id", id, new_id)


# pylint: disable-next=redefined-builtin
def clone_rhnvirtualinstance(cursor, name, clone_name, id, new_id):
    cursor.execute(
        """
        SELECT id, host_system_id, virtual_system_id, uuid, confirmed
        FROM rhnvirtualinstance
        WHERE host_system_id = %s OR virtual_system_id = %s;
        """,
        (id, id),
    )
    columns = ["name", "instance_type", "memory_size", "vcpus", "state"]
    for row in cursor.fetchall():
        (instance_id, host_id, virtual_id, uuid, confirmed) = row
        new_instance_id = get_sequence_next_value(cursor, "rhn_vi_id_seq")
        host_id = host_id if host_id != id else new_id
        virtual_id = virtual_id if virtual_id != id else new_id

        # Clone the virtual instance row
        cursor.execute(
            """
            INSERT INTO rhnvirtualinstance (
                id, host_system_id, virtual_system_id, uuid, confirmed
            ) VALUES (%s, %s, %s, %s, %s);""",
            (new_instance_id, host_id, virtual_id, uuid, confirmed),
        )

        # Clone the matching virtual instance info row
        def converter(row):
            new_row = list(row)
            if new_row[0] == name:
                new_row[0] = clone_name
            return (columns, new_row)

        do_clone_using_id(
            cursor,
            "rhnvirtualinstanceinfo",
            columns,
            "instance_id",
            instance_id,
            new_instance_id,
            converter,
        )


def clone_suseserverstaterevision(cursor, new_id):
    rev_id = get_sequence_next_value(cursor, "suse_state_revision_id_seq")
    cursor.execute("INSERT INTO susestaterevision (id) VALUES (%s)", (rev_id,))
    cursor.execute(
        "INSERT INTO suseserverstaterevision (server_id, state_revision_id) VALUES (%s, %s);",
        (new_id, rev_id),
    )


# pylint: disable-next=redefined-builtin
def clone(conn, id, name, clone_name, secret_key):
    """
    Create a single clone
    """
    # Wrap in a transaction
    with conn:
        cursor = conn.cursor()

        try:
            # rhnserver
            new_id = clone_rhnserver(cursor, id, clone_name)

            # suseminioninfo
            clone_suseminioninfo(cursor, id, new_id, clone_name)

            # susechannelaccesstoken
            clone_susechannelaccesstoken(cursor, id, new_id, secret_key)

            # rhnclientcapability
            clone_rhnclientcapability(cursor, id, new_id)

            clone_rhncpu(cursor, id, new_id)
            clone_rhndevice(cursor, id, new_id)
            clone_susesaltpillar(cursor, id, new_id)
            clone_rhnram(cursor, id, new_id)
            do_clone_using_id(
                cursor, "rhnserverchannel", ["channel_id"], "server_id", id, new_id
            )
            clone_rhnserverdmi(cursor, id, new_id)
            clone_rhnserverfqdn(cursor, id, new_id)
            do_clone_using_id(
                cursor,
                "rhnservergroupmembers",
                ["server_group_id"],
                "server_id",
                id,
                new_id,
            )
            clone_rhnserverhistory(cursor, id, new_id)
            do_clone_using_id(
                cursor,
                "rhnserverinfo",
                ["checkin", "checkin_counter"],
                "server_id",
                id,
                new_id,
            )
            clone_rhnserverneededcache(cursor, id, new_id)
            clone_rhnservernetinterface(cursor, id, new_id)
            clone_rhnserverpackage(cursor, id, new_id)
            do_clone_using_id(
                cursor, "rhnuserserverperms", ["user_id"], "server_id", id, new_id
            )
            clone_rhnvirtualinstance(cursor, name, clone_name, id, new_id)
            do_clone_using_id(
                cursor,
                "suseserverinstalledproduct",
                ["suse_installed_product_id"],
                "rhn_server_id",
                id,
                new_id,
            )
            clone_suseserverstaterevision(cursor, new_id)

            cursor.execute(
                """
                INSERT INTO rhntaskqueue (org_id, task_name, task_data, priority)
                VALUES (1, 'update_system_overview', %s, 0)
            """,
                (new_id,),
            )

        # pylint: disable-next=broad-exception-caught
        except Exception:
            console.print_exception()
            conn.rollback()


@click.command()
@click.option(
    "-n", "--name", required=True, type=str, help="Name of the system to clone"
)
@click.option("-c", "--count", default=1, type=int, help="Number  of clones to create")
def copy(name, count):
    """
    Create clones of a given system directly in Uyuni database
    """
    rhn_conf = {}
    try:
        with open("/etc/rhn/rhn.conf", "r", encoding="utf-8") as f:
            rhn_conf = {
                e[0].strip(): e[1].strip()
                for e in [line.split("#")[0].strip().split("=") for line in f]
                if len(e) == 2
            }
    # pylint: disable-next=bare-except
    except:
        console.print("[bold red]This script needs to read the server rhn.conf")
        sys.exit(1)

    host = rhn_conf["db_host"]
    port = rhn_conf["db_port"]
    user = rhn_conf["db_user"]
    password = rhn_conf["db_password"]
    db = rhn_conf["db_name"]

    with connect_db(host, port, user, password, db) as conn:
        with conn:
            cursor = conn.cursor()
            system_id = get_system_id(cursor, name)
            if not system_id:
                console.print("[bold red] No such system " + name)
                sys.exit(1)

        with console.status("Creating clones...", spinner="clock"):
            number_size = len(str(count))
            for n in range(count):
                # pylint: disable-next=consider-using-f-string
                clone_name = "{}-{}".format(name, str(n).zfill(number_size))
                # pylint: disable-next=consider-using-f-string
                console.print("Creating {}".format(clone_name))
                clone(conn, system_id, name, clone_name, rhn_conf["server.secret_key"])


if __name__ == "__main__":
    # pylint: disable-next=no-value-for-parameter
    copy()
