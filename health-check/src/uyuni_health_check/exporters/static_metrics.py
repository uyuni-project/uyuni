import os
import re
from abc import ABC, abstractmethod

metrics_config = {
    "tomcat_xmx_size": {
        "filepath": "spacewalk-debug/conf/tomcat/tomcat/conf.d/tomcat_java_opts.conf",
        "pattern": r'-Xmx(\d)+([kKmMgG])',
    },
    "max_threads_ipv4": {
        "filepath": "spacewalk-debug/conf/tomcat/tomcat/server.xml",
        "pattern": r'<Connector[^>]*address="127\.0\.0\.1"[^>]*maxThreads="(\d+)"[^>]*\/>',
    },
    "cpu_count": {
        "filepath": "hardware.txt",
        "pattern": r'CPU\(s\):\s+(\d+)',
    },
}

class StaticMetric(ABC):
    def __init__(self, name, supportconfig_path, filepath):
        self.name = name
        self.supportconfig_path = supportconfig_path
        self.filepath = filepath

    def is_present(self):
        return os.path.isfile(os.path.join(self.supportconfig_path, self.filepath))

    @abstractmethod
    def get_value(self):
        pass

class LogFileStaticMetric(StaticMetric):
    def __init__(self, name, supportconfig_path, filepath, pattern):
        super().__init__(name, supportconfig_path, filepath)
        self.pattern = pattern

    def get_value(self):
        with open(os.path.join(self.supportconfig_path, self.filepath)) as f:
            content = f.read()
            pattern = re.compile(self.pattern)
            match = re.search(pattern, content)

            if self.name == "tomcat_xmx_size":
                xmx_value = match.group(1) if match else None
                xmx_unit = match.group(2) if match else None
                if xmx_value is None or xmx_unit is None:
                    return 0
                if xmx_unit == 'm' or xmx_unit == 'M':
                    return int(xmx_value)
                if xmx_unit == 'k' or xmx_unit == 'K':
                    return int(xmx_value) * 1024
                if xmx_unit == 'g' or xmx_unit == 'G':
                    return int(xmx_value) * 1024 * 1024
            else:
                return int(match.group(1))
            
    def is_present(self):
        return super().is_present()

class StaticMetricFactory:
    @staticmethod
    def create_metric(name, supportconfig_path, filepath, pattern):
        return LogFileStaticMetric(name, supportconfig_path, filepath, pattern)

def create_static_metrics_collection(supportconfig_path):
    return {
        name: StaticMetricFactory.create_metric(name, supportconfig_path, config["filepath"], config["pattern"])
        for name, config in metrics_config.items()
    }