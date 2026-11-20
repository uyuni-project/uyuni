#!/usr/bin/python3

# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

# pylint: disable=missing-module-docstring,invalid-name
"""
Populate the database with fake channels, packages, errata and Salt minions
using them. Only database rows are created: no package file exists. Previously
created fake data is removed first, so the script can be run repeatedly.

Everything created is named with a "fake-" prefix (or "ID-fake-" for the
digital server ids), which is what --clean relies on to remove it.
"""

import hashlib
import random
import uuid
from contextlib import contextmanager
from datetime import datetime, timedelta, timezone

import click
import psycopg2
from psycopg2.extras import execute_values

RHN_CONF = "/etc/rhn/rhn.conf"
PREFIX = "fake-"
FAMILY_LABEL = PREFIX + "family"
ADVISORY_TYPES = [
    ("Security Advisory", "SA"),
    ("Bug Fix Advisory", "BA"),
    ("Product Enhancement Advisory", "EA"),
]

# Everything depending on the package type: channel and package archs, the
# server arch, the OS of the minions and the EVR type
KINDS = {
    "rpm": {
        "channel_arch": "channel-x86_64",
        "package_arch": "x86_64",
        "server_arch": "x86_64-redhat-linux",
        "os": "SUSE Linux Enterprise Server",
        "release": "15.6",
        "os_family": "Suse",
    },
    "deb": {
        "channel_arch": "channel-amd64-deb",
        "package_arch": "amd64-deb",
        "server_arch": "amd64-debian-linux",
        "os": "Ubuntu",
        "release": "22.04",
        "os_family": "Debian",
    },
}


def read_rhn_conf(path):
    """Read the key = value settings of rhn.conf, ignoring it if missing"""
    settings = {}
    try:
        with open(path, encoding="utf-8") as conf:
            for line in conf:
                line = line.split("#", 1)[0].strip()
                if "=" in line:
                    key, value = line.split("=", 1)
                    settings[key.strip()] = value.strip()
    except OSError:
        pass
    return settings


def resolve_connection(config, **given):
    """
    Get the connection parameters. In order of precedence: the command line
    flags, the PG* environment variables (both handled by click), the db_*
    settings of the configuration file and then the defaults.
    """
    settings = read_rhn_conf(config)
    keys = {
        "host": ("db_host", "localhost"),
        "port": ("db_port", 5432),
        "user": ("db_user", "spacewalk"),
        "password": ("db_password", "spacewalk"),
        "db": ("db_name", "susemanager"),
    }
    return {
        name: given[name] or settings.get(conf_key) or default
        for name, (conf_key, default) in keys.items()
    }


@contextmanager
def connect_db(host, port, user, password, db):
    cnx = psycopg2.connect(
        host=host, user=user, password=password, dbname=db, port=port
    )
    try:
        yield cnx
    finally:
        cnx.close()


def query_one(cursor, sql, params=()):
    cursor.execute(sql, params)
    return cursor.fetchone()[0]


def next_ids(cursor, sequence, count):
    cursor.execute("SELECT nextval(%s) FROM generate_series(1, %s);", (sequence, count))
    return [row[0] for row in cursor.fetchall()]


def insert_many(cursor, sql, rows):
    """Insert rows using a statement with a single VALUES %s placeholder"""
    if rows:
        execute_values(cursor, sql, rows, page_size=1000)


def create_channels(cursor, org_id, kind, base_count, child_count):
    """Create base channels with their children. Returns {base_id: [child_ids]}"""
    family_id = query_one(
        cursor, "SELECT id FROM rhnChannelFamily WHERE label = %s", (FAMILY_LABEL,)
    )
    arch_id = query_one(
        cursor,
        "SELECT id FROM rhnChannelArch WHERE label = %s",
        (KINDS[kind]["channel_arch"],),
    )

    layout = {}
    for base_idx in range(base_count):
        base_id = insert_channel(
            cursor, org_id, family_id, arch_id, f"{kind}-base-{base_idx}", None
        )
        layout[base_id] = [
            insert_channel(
                cursor,
                org_id,
                family_id,
                arch_id,
                f"{kind}-base-{base_idx}-child-{child_idx}",
                base_id,
            )
            for child_idx in range(child_count)
        ]
    return layout


def insert_channel(cursor, org_id, family_id, arch_id, name, parent_id):
    channel_id = next_ids(cursor, "rhn_channel_id_seq", 1)[0]
    label = PREFIX + name
    cursor.execute(
        """
        INSERT INTO rhnChannel (id, parent_channel, org_id, channel_arch_id, label,
                                basedir, name, summary, description)
        VALUES (%s, %s, %s, %s, %s, '/dev/null', %s, %s, %s);
        """,
        (
            channel_id,
            parent_id,
            org_id,
            arch_id,
            label,
            label,
            f"Fake channel {name}",
            "Fake channel",
        ),
    )
    cursor.execute(
        "INSERT INTO rhnChannelFamilyMembers (channel_id, channel_family_id) VALUES (%s, %s);",
        (channel_id, family_id),
    )
    return channel_id


def create_packages(cursor, org_id, kind, channel_id, channel_idx, count, versions):
    """
    Create the packages of a channel. Returns a list of (name_id, arch_id,
    [(evr_id, package_id) from the oldest to the newest version]).
    """
    arch_id = query_one(
        cursor,
        "SELECT id FROM rhnPackageArch WHERE label = %s",
        (KINDS[kind]["package_arch"],),
    )
    group_id = query_one(cursor, "SELECT id FROM rhnPackageGroup LIMIT 1")
    arch_label = KINDS[kind]["package_arch"]

    package_ids = iter(next_ids(cursor, "rhn_package_id_seq", count * versions))
    packages = []
    rows = []
    for pkg_idx in range(count):
        name = f"{PREFIX}{kind}-{channel_idx}-pkg-{pkg_idx}"
        name_id = query_one(cursor, "SELECT lookup_package_name(%s)", (name,))
        evr_ids = []
        for version in range(versions):
            evr_id = query_one(
                cursor,
                "SELECT lookup_evr(NULL, %s, %s, %s)",
                (f"1.{version}.0", "1", kind),
            )
            digest = hashlib.sha256(
                f"{name}-{version}-{arch_label}".encode()
            ).hexdigest()
            checksum_id = query_one(
                cursor, "SELECT lookup_checksum('sha256', %s)", (digest,)
            )
            package_id = next(package_ids)
            path = f"fake/{name}/1.{version}.0-1.{arch_label}"
            rows.append(
                (
                    package_id,
                    org_id,
                    name_id,
                    evr_id,
                    arch_id,
                    group_id,
                    random.randint(10_000, 50_000_000),
                    checksum_id,
                    path,
                    f"Fake package {name}",
                    "Fake package used for tests",
                )
            )
            evr_ids.append((evr_id, package_id))
        packages.append((name_id, arch_id, evr_ids))

    insert_many(
        cursor,
        """
        INSERT INTO rhnPackage (id, org_id, name_id, evr_id, package_arch_id, package_group,
                                package_size, checksum_id, path, summary, description)
        VALUES %s;
        """,
        rows,
    )
    insert_many(
        cursor,
        "INSERT INTO rhnChannelPackage (channel_id, package_id) VALUES %s;",
        [(channel_id, row[0]) for row in rows],
    )
    return packages


def create_errata(cursor, org_id, channel_id, packages, count, year, first_number):
    """Create errata fixing the versions newer than the oldest one of some packages"""
    candidates = [pkg for pkg in packages if len(pkg[2]) > 1]
    if not candidates:
        return

    erratum_ids = next_ids(cursor, "rhn_errata_id_seq", count)
    errata_rows = []
    package_rows = []
    for idx, erratum_id in enumerate(erratum_ids):
        advisory_type, short = random.choice(ADVISORY_TYPES)
        advisory = f"{PREFIX.upper()}{short}-{year}:{first_number + idx:05d}"
        status = "retracted" if random.random() < 0.1 else "final"
        severity = (
            random.randint(0, 3) if advisory_type == "Security Advisory" else None
        )
        errata_rows.append(
            (
                erratum_id,
                advisory,
                advisory_type,
                advisory,
                1,
                status,
                "Fake product",
                f"Fake {advisory_type.lower()} {advisory}",
                "Update the packages",
                org_id,
                severity,
            )
        )
        for _, _, evr_ids in random.sample(
            candidates, min(len(candidates), random.randint(1, 3))
        ):
            # Never the oldest version: this is what systems are updated from
            _, package_id = random.choice(evr_ids[1:])
            package_rows.append((erratum_id, package_id))

    insert_many(
        cursor,
        """
        INSERT INTO rhnErrata (id, advisory, advisory_type, advisory_name, advisory_rel,
                               advisory_status, product, synopsis, solution, org_id, severity_id)
        VALUES %s;
        """,
        errata_rows,
    )
    insert_many(
        cursor,
        "INSERT INTO rhnErrataPackage (errata_id, package_id) VALUES %s;",
        package_rows,
    )
    insert_many(
        cursor,
        "INSERT INTO rhnChannelErrata (channel_id, errata_id) VALUES %s;",
        [(channel_id, row[0]) for row in errata_rows],
    )


def get_salt_group(cursor, org_id):
    cursor.execute(
        """
        SELECT sg.id FROM rhnServerGroup sg
          JOIN rhnServerGroupType sgt ON sgt.id = sg.group_type
         WHERE sg.org_id = %s AND sgt.label = 'salt_entitled';
        """,
        (org_id,),
    )
    row = cursor.fetchone()
    if row:
        return row[0]
    group_id = next_ids(cursor, "rhn_server_group_id_seq", 1)[0]
    cursor.execute(
        """
        INSERT INTO rhnServerGroup (id, name, description, group_type, org_id)
        SELECT %s, sgt.name, sgt.name, sgt.id, %s FROM rhnServerGroupType sgt
         WHERE sgt.label = 'salt_entitled';
        """,
        (group_id, org_id),
    )
    return group_id


# pylint: disable-next=too-many-locals
def create_systems(
    cursor,
    org_id,
    kind,
    count,
    first_number,
    channels,
    install_ratio,
    awol_ratio,
    uptodate_ratio,
):
    """
    Create Salt minions subscribed to a random base channel of the layout and to
    some of its children, with some of the packages of those channels installed.

    channels is {base_id: [child_ids]}, channel_packages is {channel_id: packages}
    """
    layout, channel_packages = channels
    spec = KINDS[kind]
    server_arch_id = query_one(
        cursor, "SELECT id FROM rhnServerArch WHERE label = %s", (spec["server_arch"],)
    )
    group_id = get_salt_group(cursor, org_id)
    server_ids = next_ids(cursor, "rhn_server_id_seq", count)
    now = datetime.now(timezone.utc)

    servers, infos, fqdns, minions, interfaces = [], [], [], [], []
    subscriptions, installed = [], []
    interface_ids = next_ids(cursor, "rhn_srv_net_iface_id_seq", count)
    fqdn_ids = next_ids(cursor, "rhn_serverfqdn_id_seq", count)
    for idx, server_id in enumerate(server_ids):
        number = first_number + idx
        hostname = f"{PREFIX}{kind}-minion-{number}.example.org"
        machine_id = uuid.uuid4().hex
        servers.append(
            (
                server_id,
                org_id,
                f"ID-{PREFIX}{number}",
                server_arch_id,
                spec["os"],
                spec["release"],
                uuid.uuid4().hex,
                hostname,
                hostname,
                machine_id,
            )
        )
        awol = random.random() < awol_ratio
        up_to_date = random.random() < uptodate_ratio
        checkin = now - (
            timedelta(days=30) if awol else timedelta(minutes=random.randint(1, 600))
        )
        infos.append((server_id, checkin))
        fqdns.append((fqdn_ids[idx], hostname, server_id))
        minions.append((server_id, hostname, spec["os_family"]))
        interfaces.append(
            (
                interface_ids[idx],
                server_id,
                "eth0",
                f"52:54:00:{number >> 16 & 255:02x}:{number >> 8 & 255:02x}:{number & 255:02x}",
            )
        )

        base_id = random.choice(list(layout))
        subscribed = [base_id] + [c for c in layout[base_id] if random.random() < 0.7]
        installed_names = set()
        for channel_id in subscribed:
            subscriptions.append((server_id, channel_id))
            for name_id, arch_id, evr_ids in channel_packages[channel_id]:
                if name_id in installed_names or random.random() > install_ratio:
                    continue
                installed_names.add(name_id)
                # The last version is the newest: nothing is left to update
                evr_id, _ = evr_ids[-1] if up_to_date else random.choice(evr_ids)
                installed.append((server_id, name_id, evr_id, arch_id))

    insert_many(
        cursor,
        """
        INSERT INTO rhnServer (id, org_id, digital_server_id, server_arch_id, os, release,
                               secret, name, hostname, machine_id, last_boot)
        VALUES %s;
        """,
        [row + (int(now.timestamp()),) for row in servers],
    )
    insert_many(
        cursor, "INSERT INTO rhnServerInfo (server_id, checkin) VALUES %s;", infos
    )
    insert_many(
        cursor,
        "INSERT INTO rhnServerFQDN (id, name, server_id, is_primary) VALUES %s;",
        [row + ("Y",) for row in fqdns],
    )
    insert_many(
        cursor,
        "INSERT INTO rhnServerNetInterface (id, server_id, name, hw_addr, is_primary) VALUES %s;",
        [row + ("Y",) for row in interfaces],
    )
    insert_many(
        cursor,
        "INSERT INTO suseMinionInfo (server_id, minion_id, os_family) VALUES %s;",
        minions,
    )
    insert_many(
        cursor,
        "INSERT INTO rhnServerGroupMembers (server_id, server_group_id) VALUES %s;",
        [(server_id, group_id) for server_id in server_ids],
    )
    cursor.execute(
        "UPDATE rhnServerGroup SET current_members = current_members + %s WHERE id = %s;",
        (count, group_id),
    )
    insert_many(
        cursor,
        "INSERT INTO rhnServerChannel (server_id, channel_id) VALUES %s;",
        subscriptions,
    )
    insert_many(
        cursor,
        "INSERT INTO rhnServerPackage (server_id, name_id, evr_id, package_arch_id) VALUES %s;",
        installed,
    )
    return server_ids


def refresh_caches(cursor, server_ids):
    for server_id in server_ids:
        cursor.execute("SELECT rhn_cache.update_perms_for_server(%s);", (server_id,))
        cursor.execute("SELECT rhn_server.update_needed_cache(%s);", (server_id,))
        cursor.execute("SELECT update_system_overview(%s);", (server_id,))


def populate(cursor, org_id, options):
    cursor.execute(
        """
        INSERT INTO rhnChannelFamily (id, name, label, org_id)
        VALUES (nextval('rhn_channel_family_id_seq'), 'Fake family', %s, %s);
        """,
        (FAMILY_LABEL, org_id),
    )
    cursor.execute(
        """
        INSERT INTO rhnPrivateChannelFamily (channel_family_id, org_id)
        SELECT id, %s FROM rhnChannelFamily WHERE label = %s;
        """,
        (org_id, FAMILY_LABEL),
    )

    server_ids = []
    channel_idx = 0
    system_number = 0
    errata_number = 1
    for kind in KINDS:
        layout = create_channels(
            cursor, org_id, kind, options["base_channels"], options["child_channels"]
        )
        channel_packages = {}
        for channel_id in [
            c for base, children in layout.items() for c in [base, *children]
        ]:
            packages = create_packages(
                cursor,
                org_id,
                kind,
                channel_id,
                channel_idx,
                options["packages"],
                options["versions"],
            )
            channel_packages[channel_id] = packages
            create_errata(
                cursor,
                org_id,
                channel_id,
                packages,
                options["errata"],
                datetime.now().year,
                errata_number,
            )
            errata_number += options["errata"]
            channel_idx += 1
            print(f"Created channel {channel_id} with {len(packages)} packages")

        count = options[f"systems_{kind}"]
        server_ids += create_systems(
            cursor,
            org_id,
            kind,
            count,
            system_number,
            (layout, channel_packages),
            options["install_ratio"],
            options["awol_ratio"],
            options["uptodate_ratio"],
        )
        system_number += count

    print(f"Refreshing the caches of {len(server_ids)} systems")
    refresh_caches(cursor, server_ids)


def clean(cursor):
    """Remove everything created by this script, based on the fake- naming"""
    cursor.execute(
        "SELECT id FROM rhnServer WHERE digital_server_id LIKE %s;", (f"ID-{PREFIX}%",)
    )
    server_ids = [row[0] for row in cursor.fetchall()]
    # suseSystemOverview is not covered by delete_server
    cursor.execute("DELETE FROM suseSystemOverview WHERE id = ANY(%s);", (server_ids,))
    for server_id in server_ids:
        cursor.execute("SELECT delete_server(%s);", (server_id,))
    print(f"Removed {len(server_ids)} systems")

    channels = "(SELECT id FROM rhnChannel WHERE label LIKE %s)"
    errata = "(SELECT id FROM rhnErrata WHERE advisory LIKE %s)"
    like = (f"{PREFIX}%",)
    upper_like = (f"{PREFIX.upper()}%",)
    for sql, params in (
        (f"DELETE FROM rhnChannelErrata WHERE channel_id IN {channels}", like),
        (f"DELETE FROM rhnErrataPackage WHERE errata_id IN {errata}", upper_like),
        (f"DELETE FROM rhnChannelErrata WHERE errata_id IN {errata}", upper_like),
        ("DELETE FROM rhnErrata WHERE advisory LIKE %s", upper_like),
        (f"DELETE FROM rhnChannelPackage WHERE channel_id IN {channels}", like),
        (
            "DELETE FROM rhnPackage WHERE path LIKE 'fake/%%' AND id NOT IN "
            "(SELECT package_id FROM rhnChannelPackage)",
            (),
        ),
        (f"DELETE FROM rhnChannelFamilyMembers WHERE channel_id IN {channels}", like),
    ):
        cursor.execute(sql, params)
    # Children first, then the base channels
    cursor.execute(
        "DELETE FROM rhnChannel WHERE label LIKE %s AND parent_channel IS NOT NULL;",
        like,
    )
    cursor.execute("DELETE FROM rhnChannel WHERE label LIKE %s;", like)
    cursor.execute(
        "DELETE FROM rhnPrivateChannelFamily WHERE channel_family_id IN "
        "(SELECT id FROM rhnChannelFamily WHERE label = %s);",
        (FAMILY_LABEL,),
    )
    cursor.execute("DELETE FROM rhnChannelFamily WHERE label = %s;", (FAMILY_LABEL,))
    cursor.execute(
        "DELETE FROM rhnPackageName WHERE name LIKE %s AND id NOT IN "
        "(SELECT name_id FROM rhnPackage UNION SELECT name_id FROM rhnServerPackage);",
        like,
    )


@click.command(help=__doc__)
@click.option(
    "--host", envvar="PGHOST", help="Database host [default: db_host or localhost]"
)
@click.option(
    "--port", envvar="PGPORT", type=int, help="Database port [default: db_port or 5432]"
)
@click.option(
    "--user", envvar="PGUSER", help="Database user [default: db_user or spacewalk]"
)
@click.option(
    "--password",
    envvar="PGPASSWORD",
    help="Database password [default: db_password or spacewalk]",
)
@click.option(
    "--db",
    envvar="PGDATABASE",
    help="Database name [default: db_name or susemanager]",
)
@click.option(
    "--config",
    default=RHN_CONF,
    show_default=True,
    help="Configuration file providing the db_* settings not set otherwise",
)
@click.option(
    "--org-id", default=1, show_default=True, help="Organization owning the fake data"
)
@click.option(
    "--base-channels",
    default=1,
    show_default=True,
    help="Base channels per package type",
)
@click.option(
    "--child-channels",
    default=2,
    show_default=True,
    help="Child channels per base channel",
)
@click.option(
    "--packages", default=20, show_default=True, help="Package names per channel"
)
@click.option(
    "--versions", default=3, show_default=True, help="Versions of each package"
)
@click.option("--errata", default=5, show_default=True, help="Errata per channel")
@click.option(
    "--systems-rpm",
    default=20,
    show_default=True,
    help="Salt minions using RPM channels",
)
@click.option(
    "--systems-deb",
    default=5,
    show_default=True,
    help="Salt minions using DEB channels",
)
@click.option(
    "--install-ratio",
    default=0.6,
    show_default=True,
    help="Share of channel packages installed on a system",
)
@click.option(
    "--awol-ratio",
    default=0.1,
    show_default=True,
    help="Share of systems not checking in",
)
@click.option(
    "--uptodate-ratio",
    default=0.3,
    show_default=True,
    help="Share of systems with the newest version of their packages",
)
@click.option(
    "--seed", default=None, type=int, help="Random seed for reproducible data"
)
@click.option(
    "--clean",
    "clean_only",
    is_flag=True,
    help="Only remove the data created by this script",
)
# pylint: disable-next=too-many-arguments,too-many-positional-arguments
def main(host, port, user, password, db, config, org_id, clean_only, seed, **options):
    random.seed(seed)
    connection = resolve_connection(
        config, host=host, port=port, user=user, password=password, db=db
    )
    with connect_db(**connection) as cnx:
        try:
            with cnx.cursor() as cursor:
                clean(cursor)
                if not clean_only:
                    populate(cursor, org_id, options)
            cnx.commit()
        except psycopg2.DatabaseError as err:
            cnx.rollback()
            raise click.ClickException(
                f"Database error, nothing changed: {err}"
            ) from err


if __name__ == "__main__":
    main()  # pylint: disable=no-value-for-parameter
