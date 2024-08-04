# lzreposync

TODO: project description

## How to work in this project

1. Create a new virtual environment
```sh
$ python3.11 -m venv .venv
$ . .venv/bin/activate
```
2. Install `lzreposync` in *editable* mode
``` sh
$ pip install -e .
```
3. Try `lzreposync`
``` sh
$ lzreposync -u https://download.opensuse.org/update/leap/15.5/oss/repodata/
```

### How do I ...?

- add new a dependency? Add the *pypi* name to the `dependencies` list in the `[project]` section in `pyproject.toml`.
- run tests? After installing with `pip install .` (or `pip install -e .`), `pytest tests/` runs all tests. Sometimes a `rehash` is required to ensure `.venv/bin/pytest` is used by your shell.
