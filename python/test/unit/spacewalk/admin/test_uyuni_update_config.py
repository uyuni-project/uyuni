#  pylint: disable=missing-module-docstring
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
