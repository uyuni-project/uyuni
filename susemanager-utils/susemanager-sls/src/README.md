## Python Code Maintenance

Test are written with PyTest. This way:

1. Create your "test_foo.py" file.

2. Import with double-dot your package,
   so it will be included in the sys path, e.g.:

   from ..beacons import pkgset

3. Create a test function "def test_my_foo(..."

4. Rock-n-roll by simply calling "py.test".


Don't mind `.cache` and `__pycache__` directories,
they are ignored in an explicit `.gitignore`.

Have fun. :)
