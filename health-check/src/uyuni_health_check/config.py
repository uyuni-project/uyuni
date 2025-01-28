import functools
import os
from typing import Any, Dict, List
import tomli
from pathlib import Path
import json
import jinja2

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
CONFIG_DIR = os.path.join(BASE_DIR, "config")
TEMPLATES_DIR = os.path.join(CONFIG_DIR, "templates")
CONTAINERS_DIR = os.path.join(BASE_DIR, "containers")
CONFIG_TOML_PATH = os.environ.get("HEALTH_CHECK_TOML", os.path.join(BASE_DIR, "config.toml"))

@functools.lru_cache
def _init_jinja_env() -> jinja2.Environment:
    return jinja2.Environment(loader=jinja2.FileSystemLoader(TEMPLATES_DIR))

@functools.lru_cache
def parse_config() -> Dict:
    if not os.path.exists(CONFIG_TOML_PATH):
        raise ValueError(f"Config file does not exist: {CONFIG_TOML_PATH}")

    with open(CONFIG_TOML_PATH, "rb") as f:
        conf = tomli.load(f)
    return conf

def get_json_template_filepath(json_relative_path: str) -> str:
    return os.path.join(TEMPLATES_DIR, json_relative_path)

def load_jinja_template(template: str) -> jinja2.Template:
    return _init_jinja_env().get_template(template)

def load_dockerfile_dir(dockerfile_dir: str) -> str:
    return os.path.join(CONTAINERS_DIR, dockerfile_dir)

def get_config_dir_path(component: str) -> str:
    return os.path.join(CONFIG_DIR, component)

def load_prop(property: str) -> Any:
    res = parse_config().copy()
    for prop_part in property.split('.'):
        try:
            res = res[prop_part]
        except Exception as e:
            raise ValueError(
                f"Invalid config lookup ({property}); trying to get {prop_part} from {res}"
            ) from e
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

def get_all_container_image_names() -> List[str]:
    res = []
    conf = parse_config().copy()
    for section in conf.values():
        if "image" in section:
            res.append(section.get("image"))
    return res