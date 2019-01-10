## How to check code coverage results in spacewalk-testsuite


##### Get the log

```console
CONTROL="your control-node"
scp -r root@$CONTROL:/root/spacewalk-testsuite-base/coverage .
firefox coverage/input.html
```

##### Analyze:

***IMPORTANT***:
if you have a lot of failures, the coverage will decrease because the steps were not executed. Ideally, you need to have the testsuite results green to evaluate correctly the code coverage and find dead code.

Code coverage result.html is a great tool, **but** needs a human interpretation of the results.

##### Always submit a PR for eliminating dead code or a better fix that you had inspired by code coverage.
