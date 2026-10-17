# Move configuration values from the config files into the database

## What does this PR change?

Most configuration values are now stored in the `rhnConfiguration` database
table instead of `/etc/rhn/rhn.conf` and the files in
`/usr/share/rhn/config-defaults/`. This covers all keys used by the code that
are not needed to access the database. These keys stay in the files:

- keys sourced as shell variables
- keys used by the proxy, which has no database

Every key has a `value` and a `default_value`. A `NULL` value means that the
default value is used. The default value is only changed by schema
migrations, so a changed default reaches every installation that did not
override it.

The PR has one commit per step, plus one follow-up commit.

### 1. Java access to database configuration values

`RhnConfigurationFactory` offers access similar to `Config.get()`:

```java
RhnConfigurationFactory db = RhnConfigurationFactory.getSingleton();
int size = db.getInteger(RhnConfiguration.KEYS.JAVA_SALT_BATCH_SIZE);
db.setValue(RhnConfiguration.KEYS.JAVA_SALT_BATCH_SIZE, 100);
db.resetToDefault(RhnConfiguration.KEYS.JAVA_SALT_BATCH_SIZE);
```

- The getters `getString`, `getInteger`, `getLong`, `getDouble`,
  `getBoolean` and `getList` return the default value when no value is set.
- `getDefault*` returns the default value.
- `setValue` stores `NULL` when the value equals the default. Booleans are
  compared by meaning, so `false` matches a default of `0`.
- `default_value` is mapped read-only (`insertable = false, updatable = false`)
  and has no setter.
- Boolean parsing matches `Config` (`1`, `y`, `true`, `yes`, `on`).
- JSPs use `rhn:getDbConfig('KEY')`. Nav and struts ACLs use
  `db_config_is(KEY)`.

### 2. SQL: only define the default value

- Fresh installations insert every key with `value = NULL`.
- The migration `upgrade/next/100-rhnConfiguration-null-default-values.sql`:
  - sets values that equal their default to `NULL`
  - normalizes the password policy flags to `true`/`false`. Before, fresh
    installations used `0` and upgraded ones used `false`.

### 3. Python read access

`spacewalk.server.db_config.DB_CFG` provides read-only access in the same style
as `CFG`:

```python
from spacewalk.server.db_config import DB_CFG

DB_CFG.PSW_CHECK_LENGTH_MIN          # value or default, converted like CFG
DB_CFG.get_bool("ENABLE_SNAPSHOTS")
DB_CFG.get_list("CHECKSUM_PRIORITY_LIST")
DB_CFG.get_default("SESSION_LIFETIME")
```

Changing values is only done in Java.

### 4. `uyuni-update-config`

The migration from `rhn.conf` to the database is table driven:

- Values are read directly from `rhn.conf`.
- A value that equals the default is stored as `NULL`.
- The values are committed before the lines are removed from `rhn.conf`.
- Keys are accepted with and without their namespace prefix, the same way
  Java and Python resolved them before.

### 5. Keys moved into the database

73 keys were moved. The database key is the former key with dots replaced by
underscores, in upper case. For example, `java.salt_batch_size` becomes
`JAVA_SALT_BATCH_SIZE`. For each key:

- the default is inserted by `upgrade/next/110-rhnConfiguration-config-file-keys.sql`
  and by the data file for new installations
- the key is removed from the config defaults (`rhn_java.conf`,
  `rhn_web.conf`, `rhn.conf`, `rhn_server.conf`, `rhn_server_satellite.conf`,
  `rhn_server_susemanager.conf`)
- Java, the JSPs, the ACLs, Python and `mgr-create-bootstrap-repo` read the
  value from the database
- `uyuni-update-config` moves a value that is still set in `rhn.conf`

`documentation/config-defaults-usage.md` lists every moved key and explains
why the other keys stay in the files.

Keys that stay in the files, besides the DB access, shell and proxy keys:

- keys that configure services at startup: thread pools, Salt API, HTTP
  client, SMTP, localization, event stream
- keys that depend on the product build: product name, versions,
  `product_tree_tag`, locales, themes
- secrets and file system paths
- Hibernate, Quartz and SSO settings, which are passed as a whole to the
  libraries
- keys used by Perl, Salt grains or the search server
- keys read or edited by other tools:
  - `sign_metadata` (edited by `mgr-sign-metadata-ctl`)
  - `ssh_push_*` (read by `salt-secrets-config.py`)
  - `java.sc_*` (read by `spacewalk-report`)
  - `java.unify_custom_channel_management` (read by `spacewalk-common-channels`)
  - `fromdir` and `pam_auth_service` (edited by container scripts)
- `min_user_len`, `max_user_len` and `max_email_len`, which Java and Python
  read under different names

Related changes:

- "Disconnected" in *Admin > Manager Configuration > General* and the
  *Enable SCC data forwarding* page now write to the database instead of
  `rhn.conf`. This made `SCCDataForwardingConfigAction` unused, so it is
  removed. The ISSv1 parent default, which was written when switching to
  connected mode, is removed with it.
- Static fields that read configuration values at class load time now read
  them when they are used: in `SmtpMail`, `ConfigFileData` and
  `DownloadController`.
- `susemanager-tools` now requires `spacewalk-backend-server` for `DB_CFG`.
- `rhn.conf.5` and `spacewalk-repo-sync` document the database configuration.
- The testsuite now sets these values in the database: the steps that
  configure the hub peripheral and the steps that toggle source package
  syncing.
- The JUnit and devcontainer example `rhn.conf` files drop the moved keys.

### 6. Setup

- `spacewalk-setup` writes these values to the database instead of
  `rhn.conf`: `disconnected`, `enable_nvrea`, `web.subscribe_proxy_channel`,
  `force_package_upload` and `web.default_mail_from`. It uses the new helper
  `Spacewalk::Setup::write_db_config`.
- The container setup stores the default mail sender in the database.
- The initial `rhn.conf` template no longer sets `enable_snapshots`,
  `web.restrict_mail_domains` and `server.satellite.reposync_nevra_filter`.
  It only set them to their default values.

### Bugs fixed along the way

- `uyuni-update-config` never committed the moved values. The values were
  rolled back but the lines were still removed from `rhn.conf`, so they were
  lost.
- `uyuni-update-config` migrated `server.min_passwd_len` with the value of
  `max_passwd_len`.
- `ExtAuthenticationAction` stored the default organization into
  `EXTAUTH_USE_ORGUNIT` instead of `EXTAUTH_DEFAULT_ORGID`.
- Reading `EXTAUTH_DEFAULT_ORGID` threw a `NumberFormatException` when the
  value was empty.

## Upgrade notes

- In the container, the schema upgrade (`91-schema-update.sh`) runs before
  `uyuni-update-config`. Existing `rhn.conf` overrides are therefore moved
  into the database on the first start after the upgrade.
- On installations that are not containerized, `uyuni-update-config` runs
  before `uyuni-check-database`. Overrides are then moved one restart later.
- Values set in `rhn.conf` later, for example by sumaform, are moved on the
  next server start.

## Known gaps / follow-ups

- Most moved keys can only be changed with SQL. The exceptions are keys that
  already have a UI or API: password policy, external authentication,
  disconnected and SCC forwarding. An admin API or UI to view, set and reset
  configuration values should follow.
- `DISCONNECTED` is only used by the settings page and could be removed.

## Test coverage

- New JUnit test `RhnConfigurationFactoryTest` covers fallback, set, reset,
  storing NULL for the default, booleans and empty values.
- Existing JUnit tests now set database values instead of `Config` values.
  The tests roll back their transaction.
- New pytest `test_db_config.py`. The satellite tools tests mock `DB_CFG`
  through a `conftest.py`.
- Run locally:
  - `mvn package` succeeds
  - Checkstyle passes on all changed Java files
  - `test_db_config.py` and `test_download.py` pass, except
    `test_reposync_configured_http_proxy_passed_to_urlgrabber`. That test
    also fails without this PR, because of the local urlgrabber version.
- Not run locally:
  - JUnit tests (they need the PostgreSQL test container)
  - `test_reposync.py` and `test_yum_src.py` (they need the `rpm` and
    `solv` modules)
  - acceptance tests

## Links

- Changelog fragments: `*.changes.mcalmer.config-to-db` in `java`, `schema`,
  `python/spacewalk`, `spacewalk/admin`, `spacewalk/config`, `spacewalk/setup`,
  `susemanager`, `web` and `containers/server-image`.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
