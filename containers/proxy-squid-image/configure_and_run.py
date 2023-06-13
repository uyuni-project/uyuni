import re
import subprocess
import sys
import yaml


class UyuniConfigReader:
    def __init__(self, config_file_path="/etc/uyuni/config.yaml"):
        self.config_file_path = config_file_path

    def read_config(self):
        with open(self.config_file_path) as source:
            return yaml.safe_load(source)


class SquidConfigReader:
    def __init__(self, squid_conf_path="/etc/squid/squid.conf"):
        self.squid_conf_path = squid_conf_path

    def read_config(self):
        with open(self.squid_conf_path, "r") as f:
            return f.read()

    def write_config(self, squid_config):
        with open(self.squid_conf_path, "w") as f:
            f.seek(0, 0)
            f.write(squid_config)
            f.truncate()


class SquidConfigurator:

    def configure(self, squid_config, uyuni_config):
        parameter_methods = [
            self._set_cache_dir_and_size,
            self._set_access_log,
            self._set_bandwidth_limit,
            self._set_client_lifetime,
            self._set_request_timeout,
            self._set_range_offset_limit,
            self._set_read_ahead_gap,
            self._set_client_request_buffer_max_size,
        ]
        for method in parameter_methods:
            squid_config = method(squid_config, uyuni_config)
        return squid_config

    @staticmethod
    def _handle_error(error_message):
        print(f"Squid Configuration Error: {error_message}")
        sys.exit(1)

    @staticmethod
    def _set_cache_dir_and_size(squid_config, uyuni_config):
        max_cache_size_mb = uyuni_config.get("max_cache_size_mb")
        if max_cache_size_mb is None:
            return squid_config

        if max_cache_size_mb < 1:
            SquidConfigurator._handle_error(
                "max_cache_size_mb must be greater than 1")

        squid_config = re.sub(r"cache_dir aufs .*",
                              f"cache_dir aufs /var/cache/squid {max_cache_size_mb} 16 256",
                              squid_config)
        return squid_config

    @staticmethod
    def _set_access_log(squid_config, uyuni_config=None):
        return re.sub(r"access_log .*",
                      "access_log stdio:/proc/self/fd/1 squid",
                      squid_config)

    @staticmethod
    def _is_bandwidth_limit_set(bandwidth_limit):
        if not isinstance(bandwidth_limit, int):
            return False
        if bandwidth_limit < 0:
            return False
        return True

    @staticmethod
    def _set_bandwidth_limit(squid_config, uyuni_config):
        bandwidth_limit_kbps = uyuni_config.get("bandwidth_limit_kbps")
        if bandwidth_limit_kbps is None:
            return squid_config

        if bandwidth_limit_kbps < 1:
            SquidConfigurator._handle_error(
                "bandwidth_limit_kbps must be greater than 1")

        bandwidth_limit_bps = int(bandwidth_limit_kbps / 8 * 1000)
        squid_config = re.sub(r"# delay_pools .*",
                              "delay_pools 1",
                              squid_config)
        squid_config = re.sub(r"# delay_parameters 1 .*",
                              f"delay_parameters 1 {bandwidth_limit_bps}/{bandwidth_limit_bps}",
                              squid_config)
        return squid_config

    @staticmethod
    def _set_client_lifetime(squid_config, uyuni_config):
        client_lifetime = uyuni_config.get("client_lifetime")

        if client_lifetime is None:
            return squid_config

        if client_lifetime < 1:
            SquidConfigurator._handle_error(
                "client_lifetime must be greater than 1")

        squid_config = re.sub(r"# client_lifetime .*",
                              f"client_lifetime {client_lifetime} minutes",
                              squid_config)
        return squid_config

    @staticmethod
    def _set_request_timeout(squid_config, uyuni_config):
        request_timeout = uyuni_config.get("request_timeout")

        if request_timeout is None:
            return squid_config

        if request_timeout < 1:
            SquidConfigurator._handle_error(
                "request_timeout must be greater than 1")

        squid_config = re.sub(r"# request_timeout .*",
                              f"request_timeout {request_timeout} minutes",
                              squid_config)
        return squid_config

    @staticmethod
    def _set_range_offset_limit(squid_config, uyuni_config):
        range_offset_limit = uyuni_config.get("range_offset_limit")

        if range_offset_limit is None:
            return squid_config
        if range_offset_limit < 1:
            SquidConfigurator._handle_error(
                "range_offset_limit must be greater than 1")
        squid_config = re.sub(r"# range_offset_limit .*",
                              f"range_offset_limit {range_offset_limit} KB",
                              squid_config)
        return squid_config

    @staticmethod
    def _set_read_ahead_gap(squid_config, uyuni_config):
        read_ahead_gap = uyuni_config.get("read_ahead_gap")

        if read_ahead_gap is None:
            return squid_config

        if read_ahead_gap < 1:
            SquidConfigurator._handle_error(
                "read_ahead_gap must be greater than 1")

        squid_config = re.sub(r"# read_ahead_gap .*",
                              f"read_ahead_gap {read_ahead_gap} KB",
                              squid_config)
        return squid_config

    @staticmethod
    def _set_client_request_buffer_max_size(squid_config, uyuni_config):
        client_request_buffer_max_size = uyuni_config.get(
            "client_request_buffer_max_size")

        if client_request_buffer_max_size is None:
            return squid_config

        if client_request_buffer_max_size < 1:
            SquidConfigurator._handle_error(
                "client_request_buffer_max_size must be greater than 1")

        squid_config = re.sub(r"# client_request_buffer_max_size .*",
                              f"client_request_buffer_max_size {client_request_buffer_max_size} KB",
                              squid_config)
        return squid_config

    @staticmethod
    def enable_delay_pools_flag(squid_config):
        index = squid_config.find("delay_pools 1")
        if index >= 0:
            return "--enable-delay-pools"
        else:
            return ""


if __name__ == "__main__":
    uyuni_conf_reader = UyuniConfigReader()
    squid_conf_reader = SquidConfigReader()
    squid_configurator = SquidConfigurator()

    squid_config_upd = squid_configurator.configure(
        squid_conf_reader.read_config(),
        uyuni_conf_reader.read_config(),
    )
    squid_conf_reader.write_config(squid_config_upd)

    enable_delay_pools_flag = squid_configurator.enable_delay_pools_flag(
        squid_config_upd)

    subprocess.run(["squid", "-z", "--foreground"])
    subprocess.run(["squid", "-FC", "-d", "1",
                    "--foreground", enable_delay_pools_flag])
