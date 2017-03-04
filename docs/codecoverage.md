## How to check code coverage results in spacewalk-testsuite


##### Get the log

```console
CONTROL="your control-node"
scp -r root@$CONTROL:/root/spacewalk-testsuite-base/coverage .
firefox coverage/input.html
```

##### Analyze:

***IMPORTANT***: 
if you have 30 failures, the coverage will decrease because the steps were not executed.

Code coverage result.hmtl is a great tool, **but** needs a human interpretation of the results.

Ideally, you need to have the testsuite results green for evaulte 100% correct the code coverage and find dead code.


##### Submit always a Pr for eliminate dead-code or a better fix that you had inspired by codecoverage.

