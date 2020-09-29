# coding: utf-8
"""
Setup file.
"""
import os
from setuptools import setup, find_packages


def get_version_changelog():
    """
    Get a version from the current changelog.
    """
    changelog = None
    version = "4.2.1"
    for fname in os.listdir(os.path.dirname(os.path.abspath(__file__))):
        if fname.endswith(".changes"):
            changelog = fname
            break

    if changelog:
        with open(changelog, "r") as hcl:
            for line in hcl.readlines():
                if "version" in line:
                    version = line.split(" ")[-1]  # Typically version is the last one
                    break
    return version

setup(
    name='spacecmd',
    version=get_version_changelog(),
    packages=find_packages(where="src"),
    package_dir={
        "": "src",
    }
)
