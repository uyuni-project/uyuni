## Testing with a proxy

Using a SUSE manager proxy with the testsuite is not mandatory.

If you do not want a proxy, do not define `$PROXY` environment variable
before you run the testsuite. That's all.

If you want a proxy, make this variable point to the machine that will be
the proxy:

```bash
export PROXY=myproxy.example.com
```

and then run the testsuite.


### With sumaform

Sumaform can prepare a proxy virtual machine and declare the `$PROXY`
variable on the controller (in `/root/.bashrc`).

For details on how to declare a proxy in your `main.tf` file,
refer to the "Proxies" chapter of the
[advanced instructions](https://github.com/moio/sumaform/blob/master/README_ADVANCED.md)
for sumaform.
