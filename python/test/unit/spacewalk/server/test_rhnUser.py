#  pylint: disable=missing-module-docstring,invalid-name
from spacewalk.server.rhnUser import check_password, encrypt_password


def test_check_password_pbkdf2():
    stored = encrypt_password("secret")
    assert check_password("secret", stored) == 1
    assert check_password("wrong", stored) == 0


def test_check_password_rejects_plain_text():
    assert check_password("secret", "secret") == 0


def test_check_password_rejects_empty_key():
    assert check_password("", encrypt_password("secret")) == 0
