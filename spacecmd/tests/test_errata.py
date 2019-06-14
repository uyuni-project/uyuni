# coding: utf-8
"""
Test suite for the errata module.
"""

from unittest.mock import MagicMock, patch
from helpers import shell, assert_expect, assert_list_args_expect, assert_args_expect
import spacecmd.errata
from xmlrpc import client as xmlrpclib
import zlib


class TestSCErrata:
    """
    Test suite for "errata" module.
    """
    def test_errata_list_nodata(self, shell):
        """
        Test do_errata_list return no data

        :param shell:
        :return:
        """
        shell.generate_errata_cache = MagicMock()
        shell.all_errata = {"one": None, "two": None}
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            out = spacecmd.errata.do_errata_list(shell, "")

        assert out is None
        assert shell.generate_errata_cache.called
        assert_expect(mprint.call_args_list, "one\ntwo")

    def test_errata_list_with_data(self, shell):
        """
        Test do_errata_list return data for further processing

        :param shell:
        :return:
        """
        shell.generate_errata_cache = MagicMock()
        shell.all_errata = {"one": None, "two": None}
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            out = spacecmd.errata.do_errata_list(shell, "", doreturn=True)

        assert not mprint.called
        assert out is not None
        assert sorted(out) == ["one", "two"]
        assert shell.generate_errata_cache.called

    def test_errata_listaffectedsystems_noargs(self, shell):
        """
        Test do_errata_listaffectedsystems without an arguments.

        :param shell:
        :return:
        """

        shell.help_errata_listaffectedsystems = MagicMock()
        shell.expand_errata = MagicMock()
        shell.client.errata.listAffectedSystems = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            spacecmd.errata.do_errata_listaffectedsystems(shell, "")

        assert not shell.client.errata.listAffectedSystems.called
        assert not shell.expand_errata.called
        assert not mprint.called
        assert shell.help_errata_listaffectedsystems.called

    def test_errata_listaffectedsystems_by_errata_name(self, shell):
        """
        Test do_errata_listaffectedsystems with errata name.

        :param shell:
        :return:
        """

        shell.help_errata_listaffectedsystems = MagicMock()
        shell.expand_errata = MagicMock(return_value=["webstack", "databases"])
        shell.client.errata.listAffectedSystems = MagicMock(side_effect=[
            [{"name": "web1.suse.com"}, {"name": "web2.suse.com"}, {"name": "web3.suse.com"}],
            [{"name": "db1.suse.com"}, {"name": "db2.suse.com"}, {"name": "db3.suse.com"}],
        ])
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            spacecmd.errata.do_errata_listaffectedsystems(shell, "foo")

        assert not shell.help_errata_listaffectedsystems.called
        assert shell.client.errata.listAffectedSystems.called
        assert shell.expand_errata.called
        assert mprint.called

        assert_list_args_expect(mprint.call_args_list,
                                ['webstack:', 'web1.suse.com\nweb2.suse.com\nweb3.suse.com',
                                 '----------', 'databases:', 'db1.suse.com\ndb2.suse.com\ndb3.suse.com'])

    def test_errata_listcves_noargs(self, shell):
        """
        Test do_errata_listcves without arguments.

        :param shell:
        :return:
        """
        shell.help_errata_listcves = MagicMock()
        shell.client.errata.listCves = MagicMock()
        shell.expand_errata = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            spacecmd.errata.do_errata_listcves(shell, "")

        assert not shell.client.errata.listCves.called
        assert not shell.expand_errata.called
        assert not mprint.called
        assert shell.help_errata_listcves.called

    def test_errata_listcves_not_found(self, shell):
        """
        Test do_errata_listcves not found.

        :param shell:
        :return:
        """
        shell.help_errata_listcves = MagicMock()
        shell.client.errata.listCves = MagicMock(return_value=[])
        shell.expand_errata = MagicMock(return_value=[])
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            spacecmd.errata.do_errata_listcves(shell, "invalid")

        assert not shell.help_errata_listcves.called
        assert not shell.client.errata.listCves.called
        assert shell.expand_errata.called
        assert mprint.called
        assert_expect(mprint.call_args_list, "No errata has been found")

    def test_errata_listcves_expanded(self, shell):
        """
        Test do_errata_listcves data print check.

        :param shell:
        :return:
        """
        shell.help_errata_listcves = MagicMock()
        shell.client.errata.listCves = MagicMock(side_effect=[
            ["CVE-1", "CVE-2", "CVE-3"],
            ["CVE-11", "CVE-22", "CVE-33"],
        ])
        shell.expand_errata = MagicMock(return_value=["one", "two"])
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            spacecmd.errata.do_errata_listcves(shell, "CVE*")

        assert not shell.help_errata_listcves.called
        assert shell.client.errata.listCves.called
        assert shell.expand_errata.called
        assert mprint.called

        assert_list_args_expect(mprint.call_args_list,
                                ['one:', 'CVE-1\nCVE-2\nCVE-3', '----------',
                                 'two:', 'CVE-11\nCVE-22\nCVE-33'])

    def test_errata_findbycve_noargs(self, shell):
        """
        Test do_errata_findbycve without arguments.

        :param shell:
        :return:
        """
        shell.help_errata_findbycve = MagicMock()
        shell.client.errata.findByCve = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            spacecmd.errata.do_errata_findbycve(shell, "")

        assert not shell.client.errata.findByCve.called
        assert not mprint.called
        assert shell.help_errata_findbycve.called

    def test_errata_findbycve_cvelist(self, shell):
        """
        Test do_errata_findbycve with CVE list.

        :param shell:
        :return:
        """
        shell.help_errata_findbycve = MagicMock()
        shell.client.errata.findByCve = MagicMock(side_effect=[
            [{"advisory_name": "CVE-123-a"}, {"advisory_name": "CVE-123-b"}],
            [{"advisory_name": "CVE-234-a"}, {"advisory_name": "CVE-234-b"}, {"advisory_name": "CVE-234-c"}],
            [{"advisory_name": "CVE-345-a"}],
        ])
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            spacecmd.errata.do_errata_findbycve(shell, "123 234 345")

        assert not shell.help_errata_findbycve.called
        assert shell.client.errata.findByCve.called
        assert mprint.called

        assert_list_args_expect(mprint.call_args_list,
                                ['123:', 'CVE-123-a', 'CVE-123-b', '----------',
                                 '234:', 'CVE-234-a', 'CVE-234-b', 'CVE-234-c',
                                 '----------', '345:', 'CVE-345-a'])

    def test_errata_details_noargs(self, shell):
        """
        Test do_errata_details without arguments.

        :param shell:
        :return:
        """
        shell.help_errata_details = MagicMock()
        shell.client.errata.getDetails = MagicMock()
        shell.client.errata.listPackages = MagicMock()
        shell.client.errata.listAffectedSystems = MagicMock()
        shell.client.errata.listCves = MagicMock()
        shell.client.errata.applicableToChannels = MagicMock()
        shell.expand_errata = MagicMock()
        mprint = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt:
            spacecmd.errata.do_errata_details(shell, "")

        assert not shell.client.errata.getDetails.called
        assert not shell.client.errata.listPackages.called
        assert not shell.client.errata.listAffectedSystems.called
        assert not shell.client.errata.listCves.called
        assert not shell.client.errata.applicableToChannels.called
        assert not shell.expand_errata.called
        assert not mprint.called
        assert shell.help_errata_details.called

    def test_errata_details_erratum_failure(self, shell):
        """
        Test do_errata_details erratum failure.

        :param shell:
        :return:
        """
        shell.help_errata_details = MagicMock()
        shell.client.errata.getDetails = MagicMock(side_effect=xmlrpclib.Fault(faultCode=42, faultString="Kaboom!"))
        shell.client.errata.listPackages = MagicMock()
        shell.client.errata.listAffectedSystems = MagicMock()
        shell.client.errata.listCves = MagicMock()
        shell.client.errata.applicableToChannels = MagicMock()
        shell.expand_errata = MagicMock(return_value=["cve-one", "cve-two"])
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt, \
                patch("spacecmd.errata.logging", logger) as lgr:
            spacecmd.errata.do_errata_details(shell, "cve*")

        assert shell.client.errata.getDetails.called
        assert not shell.client.errata.listPackages.called
        assert not shell.client.errata.listAffectedSystems.called
        assert not shell.client.errata.listCves.called
        assert not shell.client.errata.applicableToChannels.called
        assert not mprint.called
        assert logger.warning.called
        assert not shell.help_errata_details.called

        assert_args_expect(logger.warning.call_args_list,
                           [
                               (("cve-one is not a valid erratum",), {}),
                               (("cve-two is not a valid erratum",), {}),
                           ])

    def test_errata_details_erratum_data(self, shell):
        """
        Test do_errata_details erratum data.

        :param shell:
        :return:
        """
        shell.help_errata_details = MagicMock()
        shell.client.errata.getDetails = MagicMock(side_effect=[
            {
                "product": "PRODUCT-1", "type": "TYPE-1", "issue_date": "DATE-1",
                "topic": "The quick brown fox jumped over the lazy dog",
                "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                               "Morbi volutpat felis sem, nec condimentum magna facilisis sed. "
                               "Vestibulum id ultrices nisi, mattis laoreet turpis. "
                               "Vestibulum ante ipsum primis in faucibus orci luctus et ultrices "
                               "posuere cubilia Curae; Nam tincidunt quam quis tellus convallis, "
                               "auctor aliquet leo porta. Integer ac justo arcu.",
                "notes": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                         "Morbi volutpat felis sem, nec condimentum magna facilisis sed. "
                         "Vestibulum id ultrices nisi, mattis laoreet turpis. Vestibulum ante "
                         "ipsum primis in faucibus orci luctus et ultrices posuere cubilia "
                         "Curae; Nam tincidunt quam quis tellus convallis, auctor aliquet leo porta. "
                         "Integer ac justo arcu.",
                "solution": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                            "Morbi volutpat felis sem, nec condimentum magna facilisis sed. "
                            "Vestibulum id ultrices nisi, mattis laoreet turpis. "
                            "Vestibulum ante ipsum primis in faucibus orci luctus et ultrices "
                            "posuere cubilia Curae; Nam tincidunt quam quis tellus convallis, "
                            "auctor aliquet leo porta. Integer ac justo arcu.",
                "references": "AAA:AAA00-00.00 "
                              "URL:http://foo.test.com/pub/advisory/00 "
                              "BID:0000 "
                              "URL:http://www.securityfocus.com/bid/0000 "
                              "XF:weblogic-http-response-information(00000) "
                              "URL:http://www.iss.net/security_center/static/00000.php ",

            },
            {
                "product": "PRODUCT-2", "type": "TYPE-2", "issue_date": "DATE-2",
                "topic": "The quick brown fox jumped over the lazy dog",
                "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                               "Morbi volutpat felis sem, nec condimentum magna facilisis sed. "
                               "Vestibulum id ultrices nisi, mattis laoreet turpis. "
                               "Vestibulum ante ipsum primis in faucibus orci luctus et ultrices "
                               "posuere cubilia Curae; Nam tincidunt quam quis tellus convallis, "
                               "auctor aliquet leo porta. Integer ac justo arcu.",
                "notes": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                         "Morbi volutpat felis sem, nec condimentum magna facilisis sed. "
                         "Vestibulum id ultrices nisi, mattis laoreet turpis. Vestibulum ante "
                         "ipsum primis in faucibus orci luctus et ultrices posuere cubilia "
                         "Curae; Nam tincidunt quam quis tellus convallis, auctor aliquet leo porta. "
                         "Integer ac justo arcu.",
                "solution": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                            "Morbi volutpat felis sem, nec condimentum magna facilisis sed. "
                            "Vestibulum id ultrices nisi, mattis laoreet turpis. "
                            "Vestibulum ante ipsum primis in faucibus orci luctus et ultrices "
                            "posuere cubilia Curae; Nam tincidunt quam quis tellus convallis, "
                            "auctor aliquet leo porta. Integer ac justo arcu.",
                "references": "AAA:AAA11-11.11 "
                              "URL:http://foo.test.com/pub/advisory/11 "
                              "BID:1111 "
                              "URL:http://www.securityfocus.com/bid/1111 "
                              "XF:weblogic-http-response-information(11111) "
                              "URL:http://www.iss.net/security_center/static/11111.php ",

            },
        ])
        shell.client.errata.listPackages = MagicMock(side_effect=[
            [
                {"name": "vim", "version": "42", "release": "123", "arch": "x86"},
                {"name": "pico", "version": "1", "release": "234", "arch": "x86"},
            ],
            [
                {"name": "vim", "version": "28", "release": "45", "arch": "x86"},
                {"name": "pico", "version": "2", "release": "12", "arch": "x86"},
            ],
        ])
        shell.client.errata.listAffectedSystems = MagicMock(side_effect=[
            [10001000, 10001002, 10001005],
            [10001000, 10001001],
        ])
        shell.client.errata.listCves = MagicMock(side_effect=[
            ["CVE-1", "CVE-1a"],
            ["CVE-2", "CVE-2a", "CVE-2b"],
        ])
        shell.client.errata.applicableToChannels = MagicMock(side_effect=[
            [
                {"label": "base_channel"}, {"label": "editors_channel"}
            ],
            [
                {"label": "another_base_channel"}, {"label": "special_editors_channel"}
            ],
        ])
        shell.expand_errata = MagicMock(return_value=["cve-one", "cve-two"])
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt, \
                patch("spacecmd.errata.logging", logger) as lgr:
            spacecmd.errata.do_errata_details(shell, "cve*")

        assert not logger.warning.called
        assert not shell.help_errata_details.called
        assert shell.client.errata.getDetails.called
        assert shell.client.errata.listPackages.called
        assert shell.client.errata.listAffectedSystems.called
        assert shell.client.errata.listCves.called
        assert shell.client.errata.applicableToChannels.called
        assert mprint.called

        assert_list_args_expect(mprint.call_args_list,
                                ['Name:       cve-one', 'Product:    PRODUCT-1', 'Type:       TYPE-1',
                                 'Issue Date: DATE-1', '', 'Topic', '-----', 'The quick brown fox jumped over the '
                                                                             'lazy dog', '', 'Description',
                                 '-----------',
                                 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi\nvolutpat felis sem, '
                                 'nec condimentum magna facilisis sed. Vestibulum id\nultrices nisi, mattis laoreet '
                                 'turpis. Vestibulum ante ipsum primis in\nfaucibus orci luctus et ultrices posuere '
                                 'cubilia Curae; Nam tincidunt\nquam quis tellus convallis, auctor aliquet leo porta. '
                                 'Integer ac justo\narcu.', '', 'Notes', '-----',
                                 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi\nvolutpat felis sem, '
                                 'nec condimentum magna facilisis sed. Vestibulum id\nultrices nisi, mattis laoreet '
                                 'turpis. Vestibulum ante ipsum primis in\nfaucibus orci luctus et ultrices posuere '
                                 'cubilia Curae; Nam tincidunt\nquam quis tellus convallis, auctor aliquet leo porta. '
                                 'Integer ac justo\narcu.',
                                 '', 'CVEs', '----', 'CVE-1\nCVE-1a', '', 'Solution', '--------',
                                 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi\nvolutpat felis sem, '
                                 'nec condimentum magna facilisis sed. Vestibulum id\nultrices nisi, mattis laoreet '
                                 'turpis. Vestibulum ante ipsum primis in\nfaucibus orci luctus et ultrices posuere '
                                 'cubilia Curae; Nam tincidunt\nquam quis tellus convallis, auctor aliquet leo porta. '
                                 'Integer ac justo\narcu.', '', 'References', '----------',
                                 'AAA:AAA00-00.00 URL:http://foo.test.com/pub/advisory/00 BID:0000\n'
                                 'URL:http://www.securityfocus.com/bid/0000 XF:weblogic-http-response-\n'
                                 'information(00000)\nURL:http://www.iss.net/security_center/static/00000.php', '',
                                 'Affected Channels', '-----------------', 'base_channel\neditors_channel', '',
                                 'Affected Systems', '----------------', '3', '', 'Affected Packages',
                                 '-----------------', 'pico-1-234:None.x86\nvim-42-123:None.x86', '----------',
                                 'Name:       cve-two', 'Product:    PRODUCT-2', 'Type:       TYPE-2',
                                 'Issue Date: DATE-2', '', 'Topic', '-----', 'The quick brown fox jumped over '
                                                                             'the lazy dog',
                                 '', 'Description', '-----------',
                                 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi\nvolutpat felis sem, '
                                 'nec condimentum magna facilisis sed. Vestibulum id\nultrices nisi, mattis laoreet '
                                 'turpis. Vestibulum ante ipsum primis in\nfaucibus orci luctus et ultrices posuere '
                                 'cubilia Curae; Nam tincidunt\nquam quis tellus convallis, auctor aliquet leo porta. '
                                 'Integer ac justo\narcu.', '', 'Notes', '-----',
                                 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi\nvolutpat felis sem, '
                                 'nec condimentum magna facilisis sed. Vestibulum id\nultrices nisi, mattis laoreet '
                                 'turpis. Vestibulum ante ipsum primis in\nfaucibus orci luctus et ultrices posuere '
                                 'cubilia Curae; Nam tincidunt\nquam quis tellus convallis, auctor aliquet leo porta. '
                                 'Integer ac justo\narcu.',
                                 '', 'CVEs', '----', 'CVE-2\nCVE-2a\nCVE-2b', '', 'Solution', '--------',
                                 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi\nvolutpat felis sem, '
                                 'nec condimentum magna facilisis sed. Vestibulum id\nultrices nisi, mattis laoreet '
                                 'turpis. Vestibulum ante ipsum primis in\nfaucibus orci luctus et ultrices posuere '
                                 'cubilia Curae; Nam tincidunt\nquam quis tellus convallis, auctor aliquet leo porta. '
                                 'Integer ac justo\narcu.', '', 'References', '----------',
                                 'AAA:AAA11-11.11 URL:http://foo.test.com/pub/advisory/11 BID:1111\n'
                                 'URL:http://www.securityfocus.com/bid/1111 XF:weblogic-http-response-\n'
                                 'information(11111)\nURL:http://www.iss.net/security_center/static/11111.php',
                                 '', 'Affected Channels', '-----------------', 'another_base_channel\n'
                                                                               'special_editors_channel',
                                 '', 'Affected Systems', '----------------', '2', '', 'Affected Packages',
                                 '-----------------', 'pico-2-12:None.x86\nvim-28-45:None.x86'])

    def test_errata_details_erratum_none_data(self, shell):
        """
        Test do_errata_details erratum none data.

        :param shell:
        :return:
        """
        shell.help_errata_details = MagicMock()
        shell.client.errata.getDetails = MagicMock(side_effect=[{}, {}])
        shell.client.errata.listPackages = MagicMock(side_effect=[
            [
                {"name": "vim", "version": "42", "release": "123", "arch": "x86"},
                {"name": "pico", "version": "1", "release": "234", "arch": "x86"},
            ],
            [
                {"name": "vim", "version": "28", "release": "45", "arch": "x86"},
                {"name": "pico", "version": "2", "release": "12", "arch": "x86"},
            ],
        ])
        shell.client.errata.listAffectedSystems = MagicMock(side_effect=[[], []])
        shell.client.errata.listCves = MagicMock(side_effect=[[], []])
        shell.client.errata.applicableToChannels = MagicMock(side_effect=[[{}, {}], [{}, {}]])
        shell.expand_errata = MagicMock(return_value=["cve-one", "cve-two"])
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt, \
                patch("spacecmd.errata.logging", logger) as lgr:
            spacecmd.errata.do_errata_details(shell, "cve*")

        assert not logger.warning.called
        assert not shell.help_errata_details.called
        assert shell.client.errata.getDetails.called
        assert shell.client.errata.listPackages.called
        assert shell.client.errata.listAffectedSystems.called
        assert shell.client.errata.listCves.called
        assert shell.client.errata.applicableToChannels.called
        assert mprint.called

        assert_list_args_expect(mprint.call_args_list,
                                ['Name:       cve-one', 'Product:    N/A', 'Type:       N/A', 'Issue Date: N/A', '',
                                 'Topic', '-----', 'N/A', '', 'Description', '-----------', 'N/A', '', 'CVEs', '----',
                                 '', '', 'Solution', '--------', 'N/A', '', 'References', '----------', 'N/A', '',
                                 'Affected Channels',
                                 '-----------------', '', '', 'Affected Systems', '----------------', '0', '',
                                 'Affected Packages', '-----------------', 'pico-1-234:None.x86\nvim-42-123:None.x86',
                                 '----------', 'Name:       cve-two', 'Product:    N/A', 'Type:       N/A',
                                 'Issue Date: N/A', '', 'Topic', '-----', 'N/A', '', 'Description', '-----------',
                                 'N/A', '', 'CVEs', '----', '', '', 'Solution', '--------', 'N/A', '', 'References',
                                 '----------', 'N/A', '', 'Affected Channels', '-----------------', '', '',
                                 'Affected Systems', '----------------', '0', '', 'Affected Packages',
                                 '-----------------', 'pico-2-12:None.x86\nvim-28-45:None.x86'])

    def test_errata_delete_noargs(self, shell):
        """
        Test do_errata_delete without arguments.

        :param shell:
        :return:
        """
        shell.help_errata_delete = MagicMock()
        shell.expand_errata = MagicMock()
        shell.user_confirm = MagicMock()
        shell.client.errata.applicableToChannels = MagicMock()
        shell.client.errata.delete = MagicMock()
        shell.generate_errata_cache = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt, \
                patch("spacecmd.errata.logging", logger) as lgr:
            spacecmd.errata.do_errata_delete(shell, "")

        assert not shell.expand_errata.called
        assert not shell.user_confirm.called
        assert not shell.client.errata.applicableToChannels.called
        assert not shell.client.errata.delete.called
        assert not shell.generate_errata_cache.called
        assert not mprint.called
        assert not logger.info.called
        assert not logger.warning.called
        assert shell.help_errata_delete.called

    def test_errata_publish_noargs(self, shell):
        """
        Test do_errata_publish without arguments.

        :param shell:
        :return:
        """
        shell.help_errata_publish = MagicMock()
        shell.expand_errata = MagicMock()
        shell.user_confirm = MagicMock()
        shell.client.errata.publish = MagicMock()
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt, \
                patch("spacecmd.errata.logging", logger) as lgr:
            spacecmd.errata.do_errata_delete(shell, "")

        assert not shell.expand_errata.called
        assert not shell.user_confirm.called
        assert not shell.client.errata.publish.called
        assert not mprint.called
        assert not logger.warning.called
        assert shell.help_errata_delete.called

    def test_errata_search_noargs(self, shell):
        """
        Test do_errata_search without arguments.

        :param shell:
        :return:
        """
        shell.help_errata_search = MagicMock()
        shell.expand_errata = MagicMock()
        shell.generate_errata_cache = MagicMock()
        shell.client.errata.findByCve = MagicMock()
        shell.all_errata = {}
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt, \
                patch("spacecmd.errata.logging", logger) as lgr:
            spacecmd.errata.do_errata_search(shell, "")

        assert not shell.expand_errata.called
        assert not shell.generate_errata_cache.called
        assert not shell.client.errata.findByCve.called
        assert not mprint.called
        assert not logger.warning.called
        assert shell.help_errata_search.called

    def test_errata_apply_noargs(self, shell):
        """
        Test do_errata_apply without arguments.

        :param shell:
        :return:
        """
        shell.help_errata_apply = MagicMock()
        shell.user_confirm = MagicMock()
        shell.check_api_version = MagicMock()
        shell.get_system_id = MagicMock()
        shell.expand_errata = MagicMock()
        shell.client.errata.listAffectedSystems = MagicMock()
        shell.client.system.getUnscheduledErrata = MagicMock()
        shell.client.system.scheduleApplyErrata = MagicMock()
        shell.all_errata = {}
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt, \
                patch("spacecmd.errata.logging", logger) as lgr:
            spacecmd.errata.do_errata_apply(shell, "")

        assert not shell.user_confirm.called
        assert not shell.check_api_version.called
        assert not shell.get_system_id.called
        assert not shell.expand_errata.called
        assert not shell.client.errata.listAffectedSystems.called
        assert not shell.client.system.getUnscheduledErrata.called
        assert not shell.client.system.scheduleApplyErrata.called
        assert not mprint.called
        assert not logger.warning.called
        assert shell.help_errata_apply.called

    def test_errata_apply_non_interactive_no_errata(self, shell):
        """
        Test do_errata_apply non-interactive, no patches to apply.

        :param shell:
        :return:
        """
        shell.help_errata_apply = MagicMock()
        shell.user_confirm = MagicMock()
        shell.check_api_version = MagicMock()
        shell.get_system_id = MagicMock()
        shell.expand_errata = MagicMock(return_value=[])
        shell.client.errata.listAffectedSystems = MagicMock()
        shell.client.system.getUnscheduledErrata = MagicMock()
        shell.client.system.scheduleApplyErrata = MagicMock()
        shell.all_errata = {}
        shell.options = MagicMock()
        shell.options.yes = True
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt, \
                patch("spacecmd.errata.logging", logger) as lgr:
            spacecmd.errata.do_errata_apply(shell, "foo -s 201901011030")

        assert not shell.help_errata_apply.called
        assert not shell.user_confirm.called
        assert not shell.check_api_version.called
        assert not shell.get_system_id.called
        assert not shell.client.errata.listAffectedSystems.called
        assert not shell.client.system.getUnscheduledErrata.called
        assert not shell.client.system.scheduleApplyErrata.called
        assert not mprint.called
        assert logger.warning.called

        assert_expect(logger.warning.call_args_list,
                      "No patches to apply")

    def test_errata_apply_non_interactive_affects_no_system(self, shell):
        """
        Test do_errata_apply non-interactive, no systems affected.

        :param shell:
        :return:
        """
        shell.help_errata_apply = MagicMock()
        shell.user_confirm = MagicMock()
        shell.check_api_version = MagicMock()
        shell.get_system_id = MagicMock()
        shell.expand_errata = MagicMock(return_value=["cve-one", "cve-two", "cve-three"])
        shell.client.errata.listAffectedSystems = MagicMock(return_value=[])
        shell.client.system.getUnscheduledErrata = MagicMock()
        shell.client.system.scheduleApplyErrata = MagicMock()
        shell.all_errata = {}
        shell.options = MagicMock()
        shell.options.yes = True
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt, \
                patch("spacecmd.errata.logging", logger) as lgr:
            spacecmd.errata.do_errata_apply(shell, "cve* -s 201901011030")

        assert not shell.help_errata_apply.called
        assert not shell.user_confirm.called
        assert not shell.check_api_version.called
        assert not shell.get_system_id.called
        assert not shell.client.system.getUnscheduledErrata.called
        assert not shell.client.system.scheduleApplyErrata.called
        assert not mprint.called
        assert shell.client.errata.listAffectedSystems.called
        assert shell.expand_errata.called
        assert logger.warning.called
        assert logger.debug.called

        assert_list_args_expect(logger.debug.call_args_list,
                                ['cve-one does not affect any systems',
                                 'cve-two does not affect any systems',
                                 'cve-three does not affect any systems'])
        assert_expect(logger.warning.call_args_list,
                      "No patches to apply")

    def test_errata_apply_non_interactive_api_10_11(self, shell):
        """
        Test do_errata_apply non-interactive, API 10.11 version.

        :param shell:
        :return:
        """
        shell.help_errata_apply = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        shell.check_api_version = MagicMock(return_value=True)
        shell.get_system_id = lambda data: zlib.adler32(data.encode("utf-8")) & 0xffffffff
        shell.get_erratum_name = lambda data: "CVE-{}-name".format(data)
        shell.expand_errata = MagicMock(return_value=["CVE-1", "CVE-2"])
        shell.client.errata.listAffectedSystems = MagicMock(side_effect=[
            [{"name": "web1.foo.com"}, {"name": "web2.foo.com"}],
            [{"name": "db1.foo.com"}, {"name": "db2.foo.com"}],
        ])
        shell.client.system.getUnscheduledErrata = MagicMock(side_effect=[
            [
                {"id": "1", "advisory_name": "CVE-1"},
                {"id": "2", "advisory_name": "CVE-2"},
            ],
            [
                {"id": "1", "advisory_name": "CVE-1"},
                {"id": "2", "advisory_name": "CVE-2"},
                {"id": "3", "advisory_name": "CVE-3"},
            ],
            [
                {"id": "1", "advisory_name": "CVE-1"},
            ],
            [
                {"id": "4", "advisory_name": "CVE-4"},
            ],
        ])
        shell.client.system.scheduleApplyErrata = MagicMock()
        shell.all_errata = {}
        shell.options = MagicMock()
        shell.options.yes = True
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt, \
                patch("spacecmd.errata.logging", logger) as lgr:
            spacecmd.errata.do_errata_apply(shell, "cve* -s 201901011030")

        assert not shell.help_errata_apply.called
        assert not logger.warning.called
        assert not logger.debug.called
        assert not shell.user_confirm.called
        assert shell.client.system.scheduleApplyErrata.called
        assert shell.check_api_version.called
        assert shell.client.system.getUnscheduledErrata.called
        assert mprint.called
        assert shell.client.errata.listAffectedSystems.called
        assert shell.expand_errata.called

        assert_list_args_expect(mprint.call_args_list,
                                ['Errata             Systems', '--------------     -------',
                                 'CVE-1                    2\nCVE-2                    2', '',
                                 'Start Time: 20190101T10:30:00'])

        assert_list_args_expect(logger.info.call_args_list,
                                ['Scheduled 3 system(s) for CVE-1-name',
                                 'Scheduled 2 system(s) for CVE-2-name'])

        dt = spacecmd.errata.parse_time_input("201901011030")
        assert_args_expect(shell.client.system.scheduleApplyErrata.call_args_list,
                           [((shell.session, [370082775, 370672600, 464454735], ['1'], dt), {}),
                            ((shell.session, [370082775, 370672600], ['2'], dt), {})])

    def test_errata_apply_non_interactive_api_non1011_version(self, shell):
        """
        Test do_errata_apply non-interactive, API non-10.11 version.

        :param shell:
        :return:
        """
        shell.help_errata_apply = MagicMock()
        shell.user_confirm = MagicMock(return_value=True)
        shell.check_api_version = MagicMock(return_value=False)
        shell.get_system_id = lambda data: zlib.adler32(data.encode("utf-8")) & 0xffffffff
        shell.get_erratum_name = lambda data: "CVE-{}-name".format(data)
        shell.expand_errata = MagicMock(return_value=["CVE-1", "CVE-2"])
        shell.client.errata.listAffectedSystems = MagicMock(side_effect=[
            [{"name": "web1.foo.com"}, {"name": "web2.foo.com"}],
            [{"name": "db1.foo.com"}, {"name": "db2.foo.com"}],
        ])
        shell.client.system.getUnscheduledErrata = MagicMock(side_effect=[
            [
                {"id": "1", "advisory_name": "CVE-1"},
                {"id": "2", "advisory_name": "CVE-2"},
            ],
            [
                {"id": "1", "advisory_name": "CVE-1"},
                {"id": "2", "advisory_name": "CVE-2"},
                {"id": "3", "advisory_name": "CVE-3"},
            ],
            [
                {"id": "1", "advisory_name": "CVE-1"},
            ],
            [
                {"id": "4", "advisory_name": "CVE-4"},
            ],
        ])
        shell.client.system.scheduleApplyErrata = MagicMock()
        shell.all_errata = {}
        shell.options = MagicMock()
        shell.options.yes = True
        mprint = MagicMock()
        logger = MagicMock()

        with patch("spacecmd.errata.print", mprint) as prt, \
                patch("spacecmd.errata.logging", logger) as lgr:
            spacecmd.errata.do_errata_apply(shell, "cve* -s 201901011030")

        assert not shell.help_errata_apply.called
        assert logger.warning.called
        assert not logger.debug.called
        assert not shell.user_confirm.called
        assert shell.client.system.scheduleApplyErrata.called
        assert shell.check_api_version.called
        assert shell.client.system.getUnscheduledErrata.called
        assert mprint.called
        assert shell.client.errata.listAffectedSystems.called
        assert shell.expand_errata.called

        assert_list_args_expect(mprint.call_args_list,
                                ['Errata             Systems', '--------------     -------',
                                 'CVE-1                    2\nCVE-2                    2','',
                                 'Start Time: 20190101T10:30:00'])
        assert_list_args_expect(logger.warning.call_args_list,
                                ['No patches to schedule for web2.foo.com'])
        assert_list_args_expect(logger.info.call_args_list,
                                ['Scheduled 2 patches for db1.foo.com',
                                 'Scheduled 2 patches for db2.foo.com',
                                 'Scheduled 1 patches for web1.foo.com'])
        dt = spacecmd.errata.parse_time_input("201901011030")
        assert_args_expect(shell.client.system.scheduleApplyErrata.call_args_list,
                           [((shell.session, 370082775, ['1', '2'], dt), {}),
                            ((shell.session, 370672600, ['1', '2'], dt), {}),
                            ((shell.session, 464454735, ['1'], dt), {})])
