# How to check code coverage results in the test suite

## Get the log

* log in to the controller as root;
* go to `spacewalk/testsuite` directory;
* copy recursively `coverage` directory to some web server or local directory;
* open that directory in a web browser.

## Analyze

***IMPORTANT***:
if you have a lot of failures, the coverage will decrease because the steps were not executed. Ideally, you need to have the test suite results green to evaluate the code coverage correctly and find dead code.

Code coverage `result.html` is a great tool, **but** needs a human interpretation of the results.

## Submit a PR

This PR will eliminate dead code or provide a better fix that you had inspired by code coverage.
