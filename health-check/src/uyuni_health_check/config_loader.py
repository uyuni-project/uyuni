import os
import configparser
from pathlib import Path
import yaml
import json
from jinja2 import Environment, FileSystemLoader

class ConfigLoader:
    def __init__(self):
        self.base_dir = os.path.dirname(os.path.abspath(__file__))
        self.config_dir = os.path.join(self.base_dir, "config")
        self.templates_dir = os.path.join(self.base_dir, self.config_dir, "templates")
        self.containers_dir = os.path.join(self.base_dir, "containers")
        self.jinja_env = Environment(loader=FileSystemLoader(self.templates_dir))
        self.load_config()


    def load_config(self, file_path='config.ini'):
        self.global_config = configparser.ConfigParser()
        self.global_config.read(os.path.join(self.base_dir, file_path))

    def load_yaml(self, filename):
        file_path = os.path.join(self.config_dir, filename)
        with open(file_path, "r") as file:
            return yaml.safe_load(file)

    def load_jinja_template(self, template_name):
        return self.jinja_env.get_template(template_name)
    
    def get_json_template_filepath(self, json_relative_path):
        return os.path.join(self.templates_dir, json_relative_path)

    def load_dockerfile_dir(self, dockerfile_dir):
        dockerfile_dir = os.path.join(self.containers_dir, dockerfile_dir)
        return dockerfile_dir

    def write_config(self, component, config_file_path, content, isjson=False):
        basedir = Path(os.path.join(self.config_dir, component))
        if not basedir.exists():
            basedir.mkdir(parents=True)
        file_path = os.path.join(self.config_dir, component, config_file_path)
        with open(file_path, "w") as file:
            if isjson:
                json.dump(content, file)
            else:
                file.write(content)

    def get_config_dir_path(self, component):
        return os.path.join(self.base_dir, "config", component)

    def get_config_file_path(self, component):
        return os.path.join(self.base_dir, "config", component, "config.yaml")

    def get_sources_dir(self, component):
        return os.path.join(self.base_dir, component)

    def get_grafana_config_dir(self):
        return os.path.join(self.config_dir, "grafana")

    def get_prometheus_config_dir(self):
        return os.path.join(self.config_dir, "prometheus")


if __name__ == "__main__":
    config = ConfigLoader()
    # promtail_template = config.load_jinja_template("promtail/promtail.yaml.j2")
