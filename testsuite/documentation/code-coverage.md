# Code Coverage of the server components after a full test suite execution

In your test suite you must configure JaCoCo, connected to the java components and run the full test suite, to see which blocks of code are triggered.

## Goals

- Find features not fully covered through our test-suite
- Discover obsolete code (methods or classes), which are never triggered in any workflow.
- Generate a map of features and files(or even piece of code that cover), with the idea of doing reverse engineering and be able to know which Cucumber feature can be broken when we merge a PR.

## How to configure JaCoCo in your java server

Start the app with Jacoco Agent enabled on TCP port 6300:
For that we need to edit the file `/etc/sysconfig/tomcat` on the server appending
`-javaagent:/tmp/jacocoagent.jar=output=tcpserver,address=*,port=6300`
to `JAVA_OPTS` variable, then restart tomcat: `systemctl restart tomcat.service`

## How to run the test suite with JaCoCo

1. Run the test suite normally, but with the JaCoCo agent enabled
    ```bash
    java -javaagent:/tmp/jacocoagent.jar=output=tcpserver,address=*,port=6300 -jar /tmp/cucumber.jar
    ```
2. Then when a test finish we can use JaCoCo CLI to dump results, let's run this from where we have the product code
    ```bash
    java -jar jacococli.jar dump --address <server_fqdn> --destfile /tmp/jacoco.exec --port 6300 --reset
    ```
3. After that, we can generate a HTML report
    ```bash
    java -jar jacococli.jar report /tmp/jacoco.exec --html /srv/www/htdocs/pub/jacoco-cucumber-report --xml /srv/www/htdocs/pub/jacoco-cucumber-report.xml --sourcefiles /tmp/uyuni-master/java/core/src/main/java --classfiles /srv/tomcat/webapps/rhn/WEB-INF/lib
    ```
4. From the XML report we want to obtain a list of (source_file_path:line_number) for each line of code triggered during a Cucumber feature execution.
    ```
    package name
    + sourcefile name
        + line nr (if mi == 0)
    ```
5. We will have a HashMap stored in Redis handled by KeyValueStore class
6. On that HashMap, the key being the Cucumber Feature filepath and the value being a list of (source_file_path:line_number) for each line of code triggered during the execution of that feature.
