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

# A more hacky way, but closer to the API reality

- Change the `https` into `http` in `SaltService.SALT_MASTER_URI`
_ Deploy the change
- Edit `/etc/salt/master.d/susemanager.conf` to comment the `ssl_crt` and `ssl_key` lines and add `disable_ssl: true` below them.
- `salt-service restart`
- On the server `zypper in tcpdump` and `tcpdump -s 0 -A -i lo port 9080`

You will now see the HTTP frames as they are sent to the client, ready to copy them.

**Don't push the change !**