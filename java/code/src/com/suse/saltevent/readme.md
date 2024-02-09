# How to run it

- Build and deploy rhn.jar file
- Copy the file `saltEventProcessor.conf` to a server at `etc/rhn/saltEventProcessor.conf`
- Copy file `mrg_salt_event_processor` to the server
- Run `bash <path/to/mrg_salt_event_processor>`

All salt event will be processed by this new process.