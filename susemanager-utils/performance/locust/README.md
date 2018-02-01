# Load Testing for SUSE-Manager

##### Manual Installation

``` pip install locustio ```

##### How to run the tests manually.


1. Update the ```locust_config.yml``` file with your server, user and password GUI user variable.
  

2. Run locust.

Run with:

``` bash
locust -f my_locust_file.py --no-web -c 1000 -r 100
```

Or via GUI with:

``` bash
locust -f my_locust_file.py 
```
