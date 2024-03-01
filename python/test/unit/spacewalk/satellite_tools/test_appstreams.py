"""Unit tests for AppStreams reposync module."""
import os
import pytest
from mock import Mock

import spacewalk.satellite_tools.appstreams
from spacewalk.satellite_tools.appstreams import ModuleMdImporter, Nsvca, Nevra

import gi

# pylint: disable-next=wrong-import-position
gi.require_version("Modulemd", "2.0")
from gi.repository import Modulemd


@pytest.fixture(name="importer")
def fixture_importer():
    spacewalk.satellite_tools.appstreams.rhnSQL.initDB = Mock()
    spacewalk.satellite_tools.appstreams.rhnSQL.commit = Mock()

    module_path = os.path.abspath(__file__)
    module_directory = os.path.dirname(module_path)
    modules_yaml_path = os.path.join(module_directory, "sample-modules.yaml")
    return ModuleMdImporter(1, modules_yaml_path)


@pytest.fixture(name="make_stream")
def fixture_make_stream():
    def _make_stream(name: str, stream: str, version: int, context: str, arch: str):
        stream = Modulemd.ModuleStreamV2.new(name, stream)
        stream.set_version(version)
        stream.set_context(context)
        stream.set_arch(arch)
        return stream

    return _make_stream


def test_nsvca_repr(make_stream):
    stream = make_stream("my-module", "1.0", 1001, "fedcba98", "x86_64")
    nsvca = Nsvca(stream)
    assert "my-module:1.0:1001:fedcba98:x86_64" == str(nsvca)


@pytest.mark.parametrize(
    "nevra,expected_output",
    [
        (
            Nevra("my-package", "1", "1.0", "1+MODULE+el9", "x86_64"),
            "my-package-1:1.0-1+MODULE+el9.x86_64",
        ),
        (
            Nevra("my-package", "0", "1.0", "1+MODULE+el9", "noarch"),
            "my-package-0:1.0-1+MODULE+el9.noarch",
        ),
        (
            Nevra("my-package", None, "1.0", "1+MODULE+el9", "src"),
            "my-package-1.0-1+MODULE+el9.src",
        ),
    ],
)
def test_nevra_repr(nevra, expected_output):
    assert expected_output == str(nevra)


def test_validate(importer):
    importer.validate()

    importer.modulemd_file = "INVALID_FILE"
    with pytest.raises(gi.repository.GLib.GError) as exc:
        importer.validate()

    assert exc.value.domain == "modulemd-yaml-error-quark"


# pylint: disable=protected-access
def test_get_modules(importer):
    importer._index_modulemd()
    modules = importer._get_modules()
    assert 7 == len(modules)

    module_names = [m.get_module_name() for m in modules]
    assert "maven" in module_names
    assert "ruby" in module_names
    assert "nodejs" in module_names
    assert "php" in module_names
    assert "nginx" in module_names
    assert "postgresql" in module_names
    assert "redis" in module_names


@pytest.mark.parametrize(
    "nevra_input,name,epoch,version,release,arch",
    [
        (
            "my-package-1:1.0-1+MODULE+el9.x86_64",
            "my-package",
            "1",
            "1.0",
            "1+MODULE+el9",
            "x86_64",
        ),
        (
            "my-package-0:1.0-1.x86_64",
            "my-package",
            "0",
            "1.0",
            "1",
            "x86_64",
        ),
        (
            "my-package-1.0-1.noarch",
            "my-package",
            None,
            "1.0",
            "1",
            "noarch",
        ),
        (
            "my-package-1:1.0-1+MODULE+el9.src",
            "my-package",
            "1",
            "1.0",
            "1+MODULE+el9",
            "src",
        ),
        (
            "apache-commons-cli-0:1.5.0-4.module+el9.2.0+14755+4b0b4b45.noarch",
            "apache-commons-cli",
            "0",
            "1.5.0",
            "4.module+el9.2.0+14755+4b0b4b45",
            "noarch",
        ),
        (
            "nodejs-1:18.18.2-2.module+el9.3.0+15867+4ff8d002.x86_64",
            "nodejs",
            "1",
            "18.18.2",
            "2.module+el9.3.0+15867+4ff8d002",
            "x86_64",
        ),
        (
            "ruby-0:3.1.2-141.module+el9.1.0+13172+8d1baf64.i686",
            "ruby",
            "0",
            "3.1.2",
            "141.module+el9.1.0+13172+8d1baf64",
            "i686",
        ),
        (
            "php-0:8.1.27-1.module+el9.3.0+16050+d5cd6ed5.x86_64",
            "php",
            "0",
            "8.1.27",
            "1.module+el9.3.0+16050+d5cd6ed5",
            "x86_64",
        ),
        (
            "apcu-panel-0:5.1.21-1.module+el9.3.0+16050+d5cd6ed5.noarch",
            "apcu-panel",
            "0",
            "5.1.21",
            "1.module+el9.3.0+16050+d5cd6ed5",
            "noarch",
        ),
        (
            "perl-Text-Tabs+Wrap-0:2013.0523-396.module_el8.1.0+6019+b22674e1.noarch",
            "perl-Text-Tabs+Wrap",
            "0",
            "2013.0523",
            "396.module_el8.1.0+6019+b22674e1",
            "noarch",
        ),
    ],
)
def test_parse_rpm_name(nevra_input, name, epoch, version, release, arch):
    pkg = ModuleMdImporter._parse_rpm_name(nevra_input)
    assert name == pkg.name
    assert epoch == pkg.epoch
    assert version == pkg.version
    assert release == pkg.release
    assert arch == pkg.arch
