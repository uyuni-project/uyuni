#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2026 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SPDX-License-Identifier: GPL-2.0-only

import importlib.machinery
import importlib.util
import os
from unittest.mock import MagicMock, patch

from spacewalk.server.rhnUser import check_password

SCRIPT = os.path.join(
    os.path.dirname(__file__), "../../../../../spacewalk/admin/uyuni-update-config"
)


def load_script():
    loader = importlib.machinery.SourceFileLoader("uyuni_update_config", SCRIPT)
    spec = importlib.util.spec_from_loader(loader.name, loader)
    module = importlib.util.module_from_spec(spec)
    loader.exec_module(module)
    return module


def test_hash_plain_text_passwords():
    module = load_script()
    select = MagicMock()
    select.fetchall_dict.return_value = [{"id": 1, "password": "secret"}]
    update = MagicMock()
    with patch.object(module.rhnSQL, "initDB"), patch.object(
        module.rhnSQL, "prepare", side_effect=[select, update]
    ), patch.object(module.rhnSQL, "commit") as commit:
        module.hash_plain_text_passwords()

    update.execute.assert_called_once()
    kwargs = update.execute.call_args.kwargs
    assert kwargs["id"] == 1
    assert check_password("secret", kwargs["password"]) == 1
    commit.assert_called_once()


def test_hash_plain_text_passwords_nothing_to_do():
    module = load_script()
    select = MagicMock()
    select.fetchall_dict.return_value = None
    with patch.object(module.rhnSQL, "initDB"), patch.object(
        module.rhnSQL, "prepare", return_value=select
    ) as prepare, patch.object(module.rhnSQL, "commit") as commit:
        module.hash_plain_text_passwords()

    prepare.assert_called_once()
    commit.assert_not_called()


def test_main_move_config_to_db_only():
    module = load_script()
    with patch.object(
        module.sys, "argv", ["uyuni-update-config", "--move-config-to-db"]
    ), patch.object(module, "move_config_to_db") as move, patch.object(
        module, "init_dirs_and_perms"
    ) as init_dirs:
        assert module.main() == 0

    move.assert_called_once()
    init_dirs.assert_not_called()


def test_main_does_not_move_config_to_db():
    module = load_script()
    steps = [
        "init_dirs_and_perms",
        "check_and_configure_fips",
        "run_uyuni_configfiles_sync",
        "init_scc_login",
        "import_suma_gpg_keyring",
        "copy_ca",
        "hash_plain_text_passwords",
        "rename_config_keys",
        "change_billing_data_service",
        "remove_product_name_overwrite",
        "change_hostname",
    ]
    patches = [patch.object(module, name, return_value=0) for name in steps]
    with patch.object(module.sys, "argv", ["uyuni-update-config"]), patch.object(
        module, "move_config_to_db"
    ) as move:
        for p in patches:
            p.start()
        try:
            module.main()
        finally:
            for p in patches:
                p.stop()

    move.assert_not_called()
