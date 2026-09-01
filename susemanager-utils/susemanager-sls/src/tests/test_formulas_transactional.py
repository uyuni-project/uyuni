# -*- coding: utf-8 -*-

# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

"""Tests for the transactional formula wrapper state."""

from pathlib import Path

import pytest
import yaml

jinja2 = pytest.importorskip("jinja2")

FORMULAS_TRANSACTIONAL_SLS = (
    Path(__file__).resolve().parents[2]
    / "formulas"
    / "states"
    / "formulas_transactional.sls"
)


def render_wrapper(pillar):
    """
    Render the transactional formula wrapper with the same pillar/raise inputs
    used by Salt's Jinja renderer.
    """
    template = jinja2.Environment().from_string(FORMULAS_TRANSACTIONAL_SLS.read_text())

    def raise_render_error(message):
        raise RuntimeError(message)

    return template.render({"pillar": pillar, "raise": raise_render_error})


def test_empty_wrapper_renders_without_states():
    rendered = render_wrapper({})

    assert rendered.strip() == ""
    assert yaml.safe_load(rendered) is None


def test_wrapper_includes_requested_original_formula():
    rendered = render_wrapper({"transactional_formulas": ["locale"]})

    assert yaml.safe_load(rendered) == {"include": ["locale"]}


def test_unsupported_formula_fails_during_rendering():
    with pytest.raises(
        RuntimeError,
        match=r"^Formulas do not support transactional systems: bind\.$",
    ):
        render_wrapper({"transactional_unsupported_formulas": ["bind"]})


def test_unsupported_formulas_keep_pillar_order_in_render_error():
    with pytest.raises(
        RuntimeError,
        match=r"^Formulas do not support transactional systems: bind, dhcpd\.$",
    ):
        render_wrapper({"transactional_unsupported_formulas": ["bind", "dhcpd"]})
