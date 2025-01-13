import os
import re
from abc import ABC, abstractmethod

metrics_config = {
    "tomcat_xmx_size": {
        "filepath": "spacewalk-debug/conf/tomcat/tomcat/conf.d/tomcat_java_opts.conf",
        "pattern": r'-Xmx(\d)+([kKmMgG])',
    },
    "max_threads": {
        "filepath": "spacewalk-debug/conf/tomcat/tomcat/server.xml",
        "pattern": r'<Connector[^>]*address="127\.0\.0\.1"[^>]*maxThreads="(\d+)"[^>]*\/>',
    },
    "queued_salt_events": {
        "filepath": "plugin-susemanager.txt",
        "pattern": r"(?s)select count\(\*\) from susesaltevent.*?(\d+)",
    },
    "java_salt_batch_size": {
        "filepath": "spacewalk-debug/conf/rhn/rhn/rhn.conf",
        "pattern": r'^java.salt_batch_size\s*=\s*(\d+)\s*$',
        "default": 200,
    },
    "taskomatic_channel_repodata_workers": {
        "filepath": "spacewalk-debug/conf/rhn/rhn/rhn.conf",
        "pattern": r'^java.taskomatic_channel_repodata_workers\s*=\s*(\d+)\s*$',
        "default": 2,
    },
    "org_quartz_threadpool_threadcount": {
        "filepath": "spacewalk-debug/conf/rhn/rhn/rhn.conf",
        "pattern": r'^org.quartz.threadPool.threadCount\s*=\s*(\d+)\s*$',
        "default": 20,
    },
    "org_quartz_scheduler_idlewaittime": {
        "filepath": "spacewalk-debug/conf/rhn/rhn/rhn.conf",
        "pattern": r'^org.quartz.scheduler.idleWaitTime\s*=\s*(\d+)\s*$',
        "default": 5000,
    },
    "taskomatic_minion_action_executor_parallel_threads": {
        "filepath": "spacewalk-debug/conf/rhn/rhn/rhn.conf",
        "pattern": r'^taskomatic.minion_action_executor.parallel_threads\s*=\s*(\d+)\s*$',
        "default": 1,
    },
    "java_message_queue_thread_pool_size": {
        "filepath": "spacewalk-debug/conf/rhn/rhn/rhn.conf",
        "pattern": r'^java.message_queue_thread_pool_size\s*=\s*(\d+)\s*$',
        "default": 5,
    },
    "salt_thread_pool": {
        "filepath": "plugin-saltconfiguration.txt",
        "pattern": r'^thread_pool\s*:\s*(\d+)\s*$',
        "default": 100,
    },
    "cpu_count": {
        "filepath": "hardware.txt",
        "pattern": r'CPU\(s\):\s+(\d+)',
        "label": "hw",
    },
    "mem_total": {
        "filepath": "basic-health-check.txt",
        "pattern": r'Mem:\s+(\d+)\s+\d+\s+\d+',
        "label": "memory",
    },
    "mem_used": {
        "filepath": "basic-health-check.txt",
        "pattern": r'Mem:\s+\d+\s+(\d+)\s+\d+',
        "label": "memory",
    },
    "mem_free": {
        "filepath": "basic-health-check.txt",
        "pattern": r'Mem:\s+\d+\s+\d+\s+(\d+)',
        "label": "memory",
    },
    "swap_total": {
        "filepath": "basic-health-check.txt",
        "pattern": r'Swap:\s+(\d+)\s+\d+\s+\d+',
        "label": "memory",
    },
    "swap_used": {
        "filepath": "basic-health-check.txt",
        "pattern": r'Swap:\s+\d+\s+(\d+)\s+\d+',
        "label": "memory",
    },
    "swap_free": {
        "filepath": "basic-health-check.txt",
        "pattern": r'Swap:\s+\d+\s+\d+\s+(\d+)',
        "label": "memory",
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
    def __init__(self, name, supportconfig_path, filepath, pattern, default = None):
        super().__init__(name, supportconfig_path, filepath)
        self.pattern = pattern
        self.default = default

    def get_value(self):
        with open(os.path.join(self.supportconfig_path, self.filepath)) as f:
            content = f.read()
            pattern = re.compile(self.pattern, flags=re.MULTILINE)
            match = re.search(pattern, content)

            if not match:
                return -1 if not self.default else self.default

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
    def create_metric(name, supportconfig_path, filepath, pattern, default=None):
        return LogFileStaticMetric(name, supportconfig_path, filepath, pattern, default)

def create_static_metrics_collection(supportconfig_path):
    return {
        name: StaticMetricFactory.create_metric(name, supportconfig_path, config["filepath"], config["pattern"], config.get("default"))
        for name, config in metrics_config.items()
    }