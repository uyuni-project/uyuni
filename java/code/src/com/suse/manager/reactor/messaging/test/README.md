# How to generate JSON dumps of Salt commands like the ones in this directory

Run on the SUSE Manager server (password is the `server.secret_key` from /etc/rhn/rhn.conf) :

```
curl -si https://localhost:9080/login -d "username=admin" -d "password=<password>" -d eauth=file
```

Take the resulting token and:

```
curl -NsS https://localhost:9080/events?token=<your token>
```

Then run Salt command(s)

# How to format/prettify JSON

```
cat file.json | python -m json.tool
```

