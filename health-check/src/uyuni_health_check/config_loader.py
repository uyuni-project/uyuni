import os
import yaml
from jinja2 import Environment, FileSystemLoader


class ConfigLoader:
    def __init__(self):
        self.base_dir = os.path.dirname(os.path.abspath(__file__))
        self.templates_dir = os.path.join(self.base_dir, "templates")
        self.config_dir = os.path.join(self.base_dir, "config")
        self.containers_dir = os.path.join(self.base_dir, "containers")
        self.jinja_env = Environment(loader=FileSystemLoader(self.templates_dir))

    def load_yaml(self, filename):
        file_path = os.path.join(self.config_dir, filename)
        with open(file_path, "r") as file:
            return yaml.safe_load(file)

    def load_jinja_template(self, template_name):
        return self.jinja_env.get_template(template_name)

    def load_dockerfile_dir(self, dockerfile_dir):
        dockerfile_dir = os.path.join(self.containers_dir, dockerfile_dir)
        return dockerfile_dir

    def write_config(self, component, content):
        file_path = os.path.join(self.config_dir, component, "config.yaml")
        with open(file_path, "w") as file:
            file.write(content)

    def get_config_file_path(self, component):
        return os.path.join(self.base_dir, "config", component, "config.yaml")

    def get_sources_path(self):
        return self.base_dir

    def get_grafana_config_dir(self):
        return os.path.join(self.base_dir, "grafana", "conf")

    def get_prometheus_config_dir(self):
        return os.path.join(self.base_dir, "prometheus", "conf")


if __name__ == "__main__":
    config = ConfigLoader()
    # promtail_template = config.load_jinja_template("promtail/promtail.yaml.j2")
