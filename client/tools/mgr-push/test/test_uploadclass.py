from unittest.mock import Mock, patch, MagicMock
import sys

# In order to run the tests in a system where the rpm-python package is not available we can mock its
# import by overwriting the sys.modules dictionary. Same reasoning applies to the platform module.
sys.modules['platform'] = MagicMock()
sys.modules['rpm'] = MagicMock()

from http.client import HTTPMessage

from unittest import TestCase
from email.policy import Compat32
from rhnpush import rhnpush_main
from rhnpush import rhnpush_config


class TestUpload(TestCase):

    config = {
        "options_defaults": {
            "newest": "0",
            "usage": "0",
            "header": "0",
            "test": "0",
            "nullorg": "0",
            "source": "0",
            "stdin": "0",
            "verbose": "0",
            "force": "0",
            "nosig": "0",
            "list": "0",
            "exclude": "",
            "files": "",
            "orgid": "",
            "reldir": "",
            "count": "",
            "dir": "",
            "server": "http://rhn.redhat.com/APP",
            "channel": "",
            "cache_lifetime": "600",
            "new_cache": "0",
            "extended_test": "0",
            "no_session_caching": "0",
            "proxy": "",
            "tolerant": "0",
            "ca_chain": "/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT",
            "timeout": None,
        },
        "settings": Mock(),
        "section": "rhnpush",
        "username": None,
        "password": None,
        "newest": True,
        "usage": None,
        "header": None,
        "test": None,
        "nullorg": None,
        "source": None,
        "stdin": None,
        "verbose": 0,
        "force": None,
        "nosig": None,
        "list": None,
        "exclude": [""],
        "files": [],
        "orgid": "",
        "reldir": "",
        "count": "",
        "dir": "/opt/mytools/",
        "server": "uyuni-srv-2206",
        "channel": ["custom-deb-tools"],
        "cache_lifetime": 600,
        "new_cache": None,
        "extended_test": None,
        "no_session_caching": None,
        "proxy": "",
        "tolerant": None,
        "ca_chain": "/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT",
        "timeout": 300,
    }

    headerinfo = {
        "policy": Compat32(),
        "_headers": [
            ("Date", "Mon, 25 Jul 2022 10:48:01 GMT"),
            ("Server", "Apache"),
            ("X-Frame-Options", "SAMEORIGIN"),
            ("Content-Length", "0"),
            ("X-RHN-Check-Package-Exists", "1"),
            ("Cache-Control", "no-cache,no-store,must-revalidate,private"),
            ("Pragma", "no-cache"),
            ("Expires", "0"),
            (
                "Content-Security-Policy",
                "default-src 'self' https: wss: ; "
                "script-src 'self' https: 'unsafe-inline' 'unsafe-eval'; "
                "img-src 'self' https: data: ;"
                "style-src 'self' https: 'unsafe-inline' ",
            ),
            ("X-XSS-Protection", "1; mode=block"),
            ("X-Content-Type-Options", "nosniff"),
            ("X-Permitted-Cross-Domain-Policies", "master-only"),
            ("Content-Type", "text/xml"),
        ],
        "_unixfrom": None,
        "_payload": "",
        "_charset": None,
        "preamble": None,
        "epilogue": None,
        "defects": [],
        "_default_type": "text/plain",
    }
    http_headers = HTTPMessage()
    http_headers.__dict__ = headerinfo

    def setUp(self):
        self._upload = rhnpush_main.UploadClass(None)

    @patch(
        "rhnpush.rhnpush_v2.PingPackageUpload.ping",
        Mock(return_value=[200, "OK", http_headers]),
    )
    @patch("rhnpush.rhnpush_cache.RHNPushSession", Mock())
    @patch("up2date_client.rhnserver.RhnServer", Mock())
    @patch("rhnpush.uploadLib.call", Mock(side_effect=["123", 0]))
    def test_packages(self):
        server_digest_hash = {
            "existentPackage_1.0-1_amd64.deb": [
                "sha256",
                "cd5b4348e7b76c2287a9476f3f3ef3911480fe8e872ac541ffb598186a9e9607",
            ],
            "newPackage_2.0-1_amd64.deb": "",
        }

        pkgs_info = {
            "existentPackage_1.0-1_amd64.deb": {
                "name": "existentPackage",
                "version": "1.0",
                "release": "1",
                "epoch": "",
                "arch": "amd64-deb",
                "checksum_type": "sha256",
                "checksum": "cd5b4348e7b76c2287a9476f3f3ef3911480fe8e872ac541ffb598186a9e9607",
            },
            "newPackage_2.0-1_amd64.deb": {
                "name": "newPackage",
                "version": "2.0",
                "release": "1",
                "epoch": "",
                "arch": "amd64-deb",
                "checksum_type": "sha256",
                "checksum": "ece4eedf7a5c65396d136b5765226e2c8b10f268c744b0ab1fa2625e35384a00",
            },
        }

        digest_hash = {
            "existentPackage_1.0-1_amd64.deb": (
                "sha256",
                "cd5b4348e7b76c2287a9476f3f3ef3911480fe8e872ac541ffb598186a9e9607",
            ),
            "newPackage_2.0-1_amd64.deb": (
                "sha256",
                "ece4eedf7a5c65396d136b5765226e2c8b10f268c744b0ab1fa2625e35384a00",
            ),
        }

        with patch(
            "rhnpush.rhnpush_main.UploadClass.package", Mock(return_value=0)
        ) as package, patch(
            "rhnpush.rhnpush_main.UploadClass.check_package_exists",
            Mock(return_value=(server_digest_hash, pkgs_info, digest_hash)),
        ), patch(
            "rhnpush.uploadLib.listdir",
            Mock(return_value=["newPackage_2.0-1_amd64.deb"]),
        ):
            config_parser = rhnpush_config.rhnpushConfigParser()
            with (patch.dict(config_parser.__dict__, TestUpload.config)):
                self._upload.options = config_parser
                self._upload.directory()

                self._upload.packages()
                package.assert_called_with(
                    "newPackage_2.0-1_amd64.deb",
                    "sha256",
                    "ece4eedf7a5c65396d136b5765226e2c8b10f268c744b0ab1fa2625e35384a00",
                )

    class FakePackageHeader:
        def hdr(self):
            return TestUpload.mocked_deb_pkg_header_cadabra

        is_source = False
        is_signed = True
        packaging = "deb"

        @staticmethod
        def is_signed(cls):
            return cls.is_signed

    class FakeRHNPushSession:
        def __init__(self):
            self.session = (
                "90xbad6b03797f6a640472463247de4604cc986c551af54e4f04284268bb0ced287"
            )
            self.location = "/var/lib/wwwrun/.rhnpushcache"

        def readSession(self):
            pass

        def writeSession(self):
            pass

        def getSessionString(self):
            return self.session

    @patch(
        "rhnpush.rhnpush_v2.PingPackageUpload.ping",
        Mock(return_value=[200, "OK", http_headers]),
    )
    @patch("rhnpush.uploadLib.UploadClass.checkSession", Mock(return_value=1))
    @patch("rhnpush.uploadLib.call", Mock(return_value=0))
    @patch("rhnpush.rhnpush_cache.RHNPushSession", FakeRHNPushSession)
    def test_newest(self):

        mocked_deb_pkg_header = {
            "name": "existentPackage",
            "arch": "amd64-deb",
            "epoch": "",
            "version": "1.0",
            "release": "1+a",
        }

        mocked_deb_pkg = {
            "header": mocked_deb_pkg_header,
            "header_start": 0,
            "header_end": 602736,
            "input_stream": None,
            "checksum_type": "sha256",
            "checksum": "cd5b4348e7b76c2287a9476f3f3ef3911480fe8e872ac541ffb598186a9e9607",
            "payload_stream": None,
            "payload_size": 602736,
            "header_data": None,
        }

        mocked_pkg_info = {
            "header": mocked_deb_pkg_header,
            "checksum_type": "sha256",
            "checksum": "cd5b4348e7b76c2287a9476f3f3ef3911480fe8e872ac541ffb598186a9e9607",
            "packageSize": 602736,
            "header_start": 0,
            "header_end": 602736,
            "nvrea": ("existentPackage", "1.0", "1+a", "", "amd64-deb"),
        }

        pkgs_info_new = {
            "existentPackage_1.0-1+a_amd64.deb": {
                "name": "existentPackage",
                "version": "1.0b",
                "release": "1",
                "epoch": "",
                "arch": "amd64-deb",
                "checksum_type": "sha256",
                "checksum": "cd5b4348e7b76c2287a9476f3f3ef3911480fe8e872ac541ffb598186a9e9607",
            }
        }

        digest_hash_new = {
            "existentPackage_1.0-1+a_amd64.deb": (
                "sha256",
                "cd5b4348e7b76c2287a9476f3f3ef3911480fe8e872ac541ffb598186a9e9607",
            )
        }


        with patch(
            "rhnpush.uploadLib.listdir",
            Mock(return_value=["existentPackage_1.0-1+a_amd64.deb"]),
        ), patch("os.access", Mock(return_value=True)), patch(
            "rhnpush.rhnpush_main.UploadClass.check_package_exists",
            Mock(
                return_value=(
                    {"existentPackage_1.0-1+a_amd64.deb": ""},
                    pkgs_info_new,
                    digest_hash_new,
                )
            ),
        ), patch(
            "rhnpush.uploadLib.package_from_filename",
            Mock(return_value=mocked_deb_pkg),
        ), patch(
            "rhnpush.uploadLib.UploadClass._processFile",
            Mock(return_value=mocked_pkg_info),
        ), patch(
            "rhnpush.uploadLib.get_package_header",
            Mock(return_value=TestUpload.FakePackageHeader),
        ), patch(
            "rhnpush.uploadLib.UploadClass._listChannel",
            Mock(
                return_value=[
                    ["existentPackage", "1.0", "1+1", "", "amd64-deb", "custom-deb-tools"]
                ]
            ),
        ), patch(
            "rhnpush.rhnpush_main.UploadClass._push_package_v2",
            Mock(return_value=(200, "OK")),
        ) as push_package:

            config_parser = rhnpush_config.rhnpushConfigParser()
            with (patch.dict(config_parser.__dict__, TestUpload.config)):
                self._upload.options = config_parser
                self._upload.directory()
                self._upload.newest()
                self._upload.packages()
                push_package.assert_called_with(
                    "existentPackage_1.0-1+a_amd64.deb",
                    "sha256",
                    "cd5b4348e7b76c2287a9476f3f3ef3911480fe8e872ac541ffb598186a9e9607",
                )
