import os
from typing import Any, Dict
import tomli
from pathlib import Path
import json
from jinja2 import Environment, FileSystemLoader, Template

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
CONFIG_DIR = os.path.join(BASE_DIR, "config")
TEMPLATES_DIR = os.path.join(CONFIG_DIR, "templates")
CONTAINERS_DIR = os.path.join(BASE_DIR, "containers")
CONFIG_TOML_PATH = os.environ.get("HEALTH_CHECK_TOML", "config.toml")

def get_json_template_filepath(json_relative_path: str) -> str:
    return os.path.join(TEMPLATES_DIR, json_relative_path)

def load_jinja_template(template: str) -> Template:
    return _conf.jinja_env.get_template(template)

def load_dockerfile_dir(dockerfile_dir: str) -> str:
    return os.path.join(CONTAINERS_DIR, dockerfile_dir)

def get_config_dir_path(component: str) -> str:
    return os.path.join(CONFIG_DIR, component)

def load() -> Dict:
    return _conf.parsed_config

def load_prop(property: str) -> Any:
    res = _conf.parsed_config.copy()
    for prop_part in property.split('.'):
        try:
            res = res.get(prop_part, {})
        except (AttributeError, ValueError):
            res = None
            break
    return res

def write_config(component: str, config_file_path: str, content: str, is_json=False):
    basedir = Path(get_config_dir_path(component))
    if not basedir.exists():
        basedir.mkdir(parents=True)
    file_path = os.path.join(basedir, config_file_path)
    with open(file_path, "w") as file:
        if is_json:
            json.dump(content, file, indent=4)
        else:
            file.write(content)

def get_config_file_path(component):
    return os.path.join(get_config_dir_path(component), "config.yaml")

def get_sources_dir(component):
    return os.path.join(BASE_DIR, component)

def get_grafana_config_dir():
    return os.path.join(CONFIG_DIR, "grafana")

def get_prometheus_config_dir():
    return os.path.join(CONFIG_DIR, "prometheus")

class ConfigLoader:
    def __init__(self):
        self.jinja_env = Environment(loader=FileSystemLoader(TEMPLATES_DIR))
        self.parsed_config: dict[str, Any] = {}
        self.load_config()

    def load_config(self, file_path=CONFIG_TOML_PATH):
        conf_file = os.path.join(BASE_DIR, file_path)
        if not os.path.exists(conf_file):
            raise ValueError(f"Config file does not exist: {conf_file}")

        with open(conf_file, "rb") as f:
            self.parsed_config = tomli.load(f)

    def load_jinja_template(self, template_name):
        return self.jinja_env.get_template(template_name)

_conf = ConfigLoader()
