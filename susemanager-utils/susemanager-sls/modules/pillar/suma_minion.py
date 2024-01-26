# -*- coding: utf-8 -*-
"""
Retrieve SUSE Manager pillar data for a minion_id.
- Adds generated SUSE Manager pillar data.
- Adds formula pillar data.

.. code-block:: yaml

    ext_pillar:
      - suma_minion: True

"""

# Import python libs
from __future__ import absolute_import
from enum import Enum
import os
import logging
import yaml
import salt.utils.dictupdate
import salt.utils.stringutils

try:
    import psycopg2

    HAS_POSTGRES = True
except ImportError:
    HAS_POSTGRES = False

# SUSE Manager formulas paths:
MANAGER_FORMULAS_METADATA_MANAGER_PATH = "/usr/share/susemanager/formulas/metadata"
MANAGER_FORMULAS_METADATA_STANDALONE_PATH = "/usr/share/salt-formulas/metadata"
CUSTOM_FORMULAS_METADATA_PATH = "/srv/formula_metadata"
FORMULA_PREFIX = "formula-"


def find_path(path_list):
    """
    Find the first existing path in a list.
    """
    for path in path_list:
        if os.path.isdir(path):
            return path
    return path_list[0]


formulas_metadata_cache = dict()


# Fomula group subtypes
class EditGroupSubtype(Enum):
    PRIMITIVE_LIST = "PRIMITIVE_LIST"
    PRIMITIVE_DICTIONARY = "PRIMITIVE_DICTIONARY"
    LIST_OF_DICTIONARIES = "LIST_OF_DICTIONARIES"
    DICTIONARY_OF_DICTIONARIES = "DICTIONARY_OF_DICTIONARIES"


# Set up logging
log = logging.getLogger(__name__)


# pylint: disable-next=invalid-name
def __virtual__():
    """
    Ensure the pillar module name.
    """
    return HAS_POSTGRES


def _is_salt_ssh(opts):
    """Check if this pillar is computed for Salt SSH.

    Only in salt/client/ssh/__init__.py, the master_opts are moved into
    opts[__master_opts__], which we use to detect Salt SSH usage.
    """
    return "__master_opts__" in opts


def _get_cursor(func):
    def _connect_db():
        options = {
            "host": "localhost",
            "user": "",
            "pass": "",
            "db": "susemanager",
            "port": 5432,
        }
        # pylint: disable-next=undefined-variable
        options.update(__opts__.get("__master_opts__", __opts__).get("postgres", {}))
        return psycopg2.connect(
            host=options["host"],
            user=options["user"],
            password=options["pass"],
            dbname=options["db"],
            port=options["port"],
        )

    # pylint: disable-next=undefined-variable
    if "suma_minion_cnx" in __context__:
        # pylint: disable-next=undefined-variable
        cnx = __context__["suma_minion_cnx"]
        log.debug("Reusing DB connection from the context")
    else:
        try:
            cnx = _connect_db()
            log.debug("Connected to the DB")
            # pylint: disable-next=undefined-variable
            if not _is_salt_ssh(__opts__):
                # pylint: disable-next=undefined-variable
                __context__["suma_minion_cnx"] = cnx
        except psycopg2.OperationalError as err:
            log.error("Error on getting database pillar: %s", err.args)
            return
    try:
        cursor = cnx.cursor()
    except psycopg2.InterfaceError as err:
        log.debug("Reconnecting to the DB")
        try:
            cnx = _connect_db()
            log.debug("Reconnected to the DB")
            # pylint: disable-next=undefined-variable
            if not _is_salt_ssh(__opts__):
                # pylint: disable-next=undefined-variable
                __context__["suma_minion_cnx"] = cnx
            cursor = cnx.cursor()
        # pylint: disable-next=redefined-outer-name
        except psycopg2.OperationalError as err:
            log.error("Error on getting database pillar: %s", err.args)
            return
    retry = 0
    while True:
        try:
            if retry:
                cnx = _connect_db()
                log.debug("Reconnected to the DB")
                # pylint: disable-next=undefined-variable
                if not _is_salt_ssh(__opts__):
                    # pylint: disable-next=undefined-variable
                    __context__["suma_minion_cnx"] = cnx
                cursor = cnx.cursor()

            func(cursor)
            break
        except psycopg2.DatabaseError as err:
            retry += 1
            if retry == 3:
                log.error("Error on getting database pillar, giving up: %s", err.args)
                break
            else:
                log.error(
                    "Error on getting database pillar, trying again: %s", err.args
                )
        finally:
            # pylint: disable-next=undefined-variable
            if _is_salt_ssh(__opts__):
                cnx.close()


# pylint: disable-next=unused-argument
def ext_pillar(minion_id, pillar, *args):
    """
    Find SUMA-related pillars for the registered minions and return the data.
    """

    # pylint: disable-next=logging-format-interpolation,consider-using-f-string
    log.debug('Getting pillar data for the minion "{0}"'.format(minion_id))
    ret = {}
    group_formulas = {}
    system_formulas = {}

    # Load the global pillar from DB
    def _load_db_pillar(cursor):
        nonlocal ret
        nonlocal group_formulas
        nonlocal system_formulas
        ret = load_global_pillars(cursor, ret)
        ret = load_org_pillars(minion_id, cursor, ret)
        group_formulas, ret = load_group_pillars(minion_id, cursor, ret)
        system_formulas, ret = load_system_pillars(minion_id, cursor, ret)

    _get_cursor(_load_db_pillar)

    # Including formulas into pillar data
    try:
        ret = salt.utils.dictupdate.merge(
            ret,
            formula_pillars(system_formulas, group_formulas, ret),
            strategy="recurse",
        )
    # pylint: disable-next=broad-exception-caught
    except Exception as error:
        log.error("Error accessing formula pillar data: %s", error)

    return ret


def get_formula_order(pillar):
    """
    Get the formula order either from the legacy file or from the pillar
    """
    if "formula_order" in pillar:
        return pillar.pop("formula_order")
    return []


def load_global_pillars(cursor, pillar):
    """
    Load the global pillar from the database
    """
    log.debug("Loading global pillars from db")
    # Query for global pillar and extract the formula order
    cursor.execute(
        """
            SELECT p.pillar
            FROM susesaltpillar AS p
            WHERE p.server_id is NULL AND p.group_id is NULL AND p.org_id is NULL;"""
    )
    for row in cursor.fetchall():
        pillar = salt.utils.dictupdate.merge(pillar, row[0], strategy="recurse")
    return pillar


def load_org_pillars(minion_id, cursor, pillar):
    """
    Load the org pillar from the database
    """
    cursor.execute(
        """
            SELECT p.pillar
            FROM susesaltpillar AS p,
                 suseminioninfo AS m
            WHERE m.minion_id = %s
              AND p.org_id = (SELECT s.org_id FROM rhnServer AS s WHERE s.id = m.server_id);""",
        (minion_id,),
    )
    for row in cursor.fetchall():
        pillar = salt.utils.dictupdate.merge(pillar, row[0], strategy="recurse")
    return pillar


def load_group_pillars(minion_id, cursor, pillar):
    """
    Load the group pillars from the DB and extract the formulas from it
    """
    groups_query = """
        SELECT p.category, p.pillar
        FROM susesaltpillar AS p,
             suseminioninfo AS m
        WHERE m.minion_id = %s
          AND p.group_id IN (
            SELECT g.server_group_id
            FROM rhnServerGroupMembers AS g
            WHERE g.server_id = m.server_id
          );
    """
    cursor.execute(groups_query, (minion_id,))
    group_formulas = {}
    for row in cursor.fetchall():
        if row[0].startswith(FORMULA_PREFIX):
            # Handle formulas separately
            group_formulas[row[0][len(FORMULA_PREFIX) :]] = row[1]
        else:
            pillar = salt.utils.dictupdate.merge(pillar, row[1], strategy="recurse")

    return (group_formulas, pillar)


def load_system_pillars(minion_id, cursor, pillar):
    """
    Load the system pillars from the DB and extract the formulas from it
    """
    minion_query = """
        SELECT p.category, p.pillar
        FROM susesaltpillar AS p,
             suseminioninfo AS m
        WHERE m.minion_id = %s
          AND m.server_id = p.server_id;"""
    cursor.execute(minion_query, (minion_id,))
    server_formulas = {}
    for row in cursor.fetchall():
        if row[0].startswith(FORMULA_PREFIX):
            # Handle formulas separately
            server_formulas[row[0][len(FORMULA_PREFIX) :]] = row[1]
        else:
            pillar = salt.utils.dictupdate.merge(pillar, row[1], strategy="recurse")

    return (server_formulas, pillar)


def formula_pillars(system_formulas, group_formulas, all_pillar):
    """
    Find formula pillars for the minion, merge them and return the data.
    """
    pillar = {}
    out_formulas = []

    # Loading group formulas
    for formula_name in group_formulas:
        formula_metadata = load_formula_metadata(formula_name)
        if formula_name in out_formulas:
            continue  # already processed
        if not formula_metadata.get("pillar_only", False):
            out_formulas.append(formula_name)
        pillar = salt.utils.dictupdate.merge(
            pillar,
            load_formula_pillar(
                system_formulas.get(formula_name, {}),
                group_formulas[formula_name],
                formula_name,
                formula_metadata,
            ),
            strategy="recurse",
        )

    # Loading minion formulas
    for formula_name in system_formulas:
        if formula_name in out_formulas:
            continue  # already processed
        formula_metadata = load_formula_metadata(formula_name)
        if not formula_metadata.get("pillar_only", False):
            out_formulas.append(formula_name)
        pillar = salt.utils.dictupdate.merge(
            pillar,
            load_formula_pillar(system_formulas[formula_name], {}, formula_name),
            strategy="recurse",
        )

    # Loading the formula order
    order = get_formula_order(all_pillar)
    if order:
        out_formulas = [formula for formula in order if formula in out_formulas]

    pillar["formulas"] = out_formulas

    return pillar


# pylint: disable-next=unused-argument
def load_formula_pillar(system_data, group_data, formula_name, formula_metadata=None):
    """
    Load the data from a specific formula for a minion in a specific group, merge and return it.
    """
    layout_filename = os.path.join(
        MANAGER_FORMULAS_METADATA_STANDALONE_PATH, formula_name, "form.yml"
    )
    if not os.path.isfile(layout_filename):
        layout_filename = os.path.join(
            MANAGER_FORMULAS_METADATA_MANAGER_PATH, formula_name, "form.yml"
        )
        if not os.path.isfile(layout_filename):
            layout_filename = os.path.join(
                CUSTOM_FORMULAS_METADATA_PATH, formula_name, "form.yml"
            )
            if not os.path.isfile(layout_filename):
                log.error(
                    # pylint: disable-next=logging-format-interpolation,consider-using-f-string
                    'Error loading data for formula "{formula}": No form.yml found'.format(
                        formula=formula_name
                    )
                )
                return {}

    try:
        # pylint: disable-next=unspecified-encoding
        layout = yaml.load(open(layout_filename).read(), Loader=yaml.FullLoader)
    # pylint: disable-next=broad-exception-caught
    except Exception as error:
        log.error(
            # pylint: disable-next=logging-format-interpolation,consider-using-f-string
            'Error loading form.yml of formula "{formula}": {message}'.format(
                formula=formula_name, message=str(error)
            )
        )
        return {}

    merged_data = merge_formula_data(layout, group_data, system_data)
    merged_data = adjust_empty_values(layout, merged_data)

    return merged_data


def merge_formula_data(layout, group_data, system_data, scope="system"):
    """
    Merge the group and system formula data, respecting the scope of a value.
    """
    ret = {}

    for element_name in layout:
        if element_name.startswith("$"):
            continue

        element = layout[element_name]
        if not isinstance(element, dict):
            continue

        element_scope = element.get("$scope", scope)
        value = None

        if element.get("$type", "text") in ["group", "hidden-group", "namespace"]:
            value = merge_formula_data(
                element,
                group_data.get(element_name, {}),
                system_data.get(element_name, {}),
                element_scope,
            )
        # edit-group is handled as primitive element - use either system_data or group data, no merging
        elif element_scope == "system":
            value = system_data.get(
                element_name,
                group_data.get(
                    element_name,
                    element.get("$default", element.get("$placeholder", "")),
                ),
            )
        elif element_scope == "group":
            value = group_data.get(
                element_name, element.get("$default", element.get("$placeholder", ""))
            )
        elif element_scope == "readonly":
            value = element.get("$default", element.get("$placeholder", ""))

        ret[element_name] = value
    return ret


def adjust_empty_values(layout, data):
    """
    Adjust empty values in formula data
    """
    ret = {}

    for element_name in layout:
        if element_name.startswith("$"):
            continue

        element = layout[element_name]
        if not isinstance(element, dict):
            continue

        element_type = element.get("$type", "text")
        value = data.get(element_name, "")

        if element_type in ["group", "hidden-group", "namespace"]:
            value = adjust_empty_values(element, data.get(element_name, {}))
        elif element_type in ["edit-group"]:
            prototype = element.get("$prototype")
            subtype = get_edit_group_subtype(element)
            if subtype is EditGroupSubtype.DICTIONARY_OF_DICTIONARIES:
                value = {}
                if isinstance(data.get(element_name), dict):
                    for key, entry in list(data.get(element_name).items()):
                        proc_entry = adjust_empty_values(prototype, entry)
                        value[key] = proc_entry
            elif subtype is EditGroupSubtype.LIST_OF_DICTIONARIES:
                value = []
                if isinstance(data.get(element_name), list):
                    for entry in data.get(element_name):
                        proc_entry = adjust_empty_values(prototype, entry)
                        value.append(proc_entry)

        if not value and "$ifEmpty" in element:
            value = element.get("$ifEmpty")

        if value or not element.get("$optional"):
            ret[element_name] = value
    return ret


def get_edit_group_subtype(element):
    if element is not None and element.get("$prototype"):
        prototype = element.get("$prototype")
        if prototype.get("$key") is None and prototype.get("$type", "group") != "group":
            return EditGroupSubtype.PRIMITIVE_LIST
        if (
            prototype.get("$key") is not None
            and prototype.get("$type", "group") != "group"
        ):
            return EditGroupSubtype.PRIMITIVE_DICTIONARY
        if prototype.get("$key") is None and prototype.get("$type", "group") == "group":
            return EditGroupSubtype.LIST_OF_DICTIONARIES
        if (
            prototype.get("$key") is not None
            and prototype.get("$type", "group") == "group"
        ):
            return EditGroupSubtype.DICTIONARY_OF_DICTIONARIES
    return None


def load_formula_metadata(formula_name):
    if formula_name in formulas_metadata_cache:
        return formulas_metadata_cache[formula_name]

    metadata_filename = None
    metadata_paths_ordered = [
        os.path.join(
            MANAGER_FORMULAS_METADATA_STANDALONE_PATH, formula_name, "metadata.yml"
        ),
        os.path.join(
            MANAGER_FORMULAS_METADATA_MANAGER_PATH, formula_name, "metadata.yml"
        ),
        os.path.join(CUSTOM_FORMULAS_METADATA_PATH, formula_name, "metadata.yml"),
    ]

    # Take the first metadata file that exist
    for mpath in metadata_paths_ordered:
        if os.path.isfile(mpath):
            metadata_filename = mpath
            break

    if not metadata_filename:
        log.error(
            # pylint: disable-next=logging-format-interpolation,consider-using-f-string
            'Error loading metadata for formula "{formula}": No metadata.yml found'.format(
                formula=formula_name
            )
        )
        return {}
    try:
        # pylint: disable-next=unspecified-encoding
        metadata = yaml.load(open(metadata_filename).read(), Loader=yaml.FullLoader)
    # pylint: disable-next=broad-exception-caught
    except Exception as error:
        log.error(
            # pylint: disable-next=logging-format-interpolation,consider-using-f-string
            'Error loading data for formula "{formula}": {message}'.format(
                formula=formula_name, message=str(error)
            )
        )
        return {}

    formulas_metadata_cache[formula_name] = metadata
    return metadata
