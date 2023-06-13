import sys
import unittest

from configure_and_run import SquidConfigurator

sys.path.append('..')


class TestSquidConfigurator(unittest.TestCase):
    def setUp(self):
        self.configurator = SquidConfigurator()

    def test_set_cache_dir_and_size(self):
        squid_config = "cache_dir aufs /var/cache/squid 1000 16 256"
        uyuni_config = {"max_cache_size_mb": 2000}
        expected_config = "cache_dir aufs /var/cache/squid 2000 16 256"
        self.assertEqual(expected_config, self.configurator._set_cache_dir_and_size(
            squid_config, uyuni_config))

    def test_set_access_log(self):
        squid_config = "access_log /var/log/squid/access.log squid"
        expected_config = "access_log stdio:/proc/self/fd/1 squid"
        self.assertEqual(
            expected_config, self.configurator._set_access_log(squid_config))

    def test_set_bandwidth_limit(self):
        squid_config = "# delay_pools 0\n" \
                       "# delay_parameters 1 1000/1000\n"
        uyuni_config = {"bandwidth_limit_kbps": 100}
        expected_config = "delay_pools 1\n" \
                          "delay_parameters 1 12500/12500\n"
        self.assertEqual(expected_config, self.configurator._set_bandwidth_limit(
            squid_config, uyuni_config))

    def test_set_client_lifetime(self):
        squid_config = "# client_lifetime 1 minutes"
        uyuni_config = {"client_lifetime": 5}
        expected_config = "client_lifetime 5 minutes"
        self.assertEqual(expected_config, self.configurator._set_client_lifetime(
            squid_config, uyuni_config))

    def test_set_request_timeout(self):
        squid_config = "# request_timeout 1 minutes"
        uyuni_config = {"request_timeout": 5}
        expected_config = "request_timeout 5 minutes"
        self.assertEqual(expected_config, self.configurator._set_request_timeout(
            squid_config, uyuni_config))

    def test_set_range_offset_limit(self):
        squid_config = "# range_offset_limit 100 KB"
        uyuni_config = {"range_offset_limit": 200}
        expected_config = "range_offset_limit 200 KB"
        self.assertEqual(expected_config, self.configurator._set_range_offset_limit(
            squid_config, uyuni_config))

    def test_set_read_ahead_gap(self):
        squid_config = "# read_ahead_gap 100 KB"
        uyuni_config = {"read_ahead_gap": 200}
        expected_config = "read_ahead_gap 200 KB"
        self.assertEqual(expected_config, self.configurator._set_read_ahead_gap(
            squid_config, uyuni_config))

    def test_set_client_request_buffer_max_size(self):
        squid_config = "# client_request_buffer_max_size 100 KB"
        uyuni_config = {"client_request_buffer_max_size": 200}
        expected_config = "client_request_buffer_max_size 200 KB"
        self.assertEqual(expected_config,
                         self.configurator._set_client_request_buffer_max_size(squid_config, uyuni_config))

    def test_configure(self):
        squid_config = "cache_dir aufs /var/cache/squid 1000 16 256\n" \
                       "access_log /var/log/squid/access.log squid\n" \
                       "# delay_pools 0\n" \
                       "# delay_parameters 1 1000/1000\n" \
                       "# client_lifetime 1 minutes\n" \
                       "# request_timeout 1 minutes\n" \
                       "# range_offset_limit 100 KB\n" \
                       "# read_ahead_gap 100 KB\n" \
                       "# client_request_buffer_max_size 100 KB\n"
        uyuni_config = {
            "max_cache_size_mb": 2000,
            "bandwidth_limit_kbps": 100,
            "client_lifetime": 5,
            "request_timeout": 5,
            "range_offset_limit": 200,
            "read_ahead_gap": 200,
            "client_request_buffer_max_size": 200
        }
        expected_config = "cache_dir aufs /var/cache/squid 2000 16 256\n" \
                          "access_log stdio:/proc/self/fd/1 squid\n" \
                          "delay_pools 1\n" \
                          "delay_parameters 1 12500/12500\n" \
                          "client_lifetime 5 minutes\n" \
                          "request_timeout 5 minutes\n" \
                          "range_offset_limit 200 KB\n" \
                          "read_ahead_gap 200 KB\n" \
                          "client_request_buffer_max_size 200 KB\n"
        self.assertEqual(expected_config, self.configurator.configure(
            squid_config, uyuni_config))
