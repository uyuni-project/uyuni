# How to generate JSON dumps of Salt commands like the ones in this directory

Run on the SUSE Manager server:

```
curl -si localhost:9080/login -d "username=admin" -d "password=admin" -d eauth=auto
```

Take the resulting token and:

```
curl -NsS localhost:9080/events?token=<your token>
```

Then run Salt command(s)

# How to format/prettify JSON

```
cat file.json | python -m json.tool
```

