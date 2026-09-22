# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Errata advisory names longer than 150 characters
  In order to synchronize products whose errata have long advisory names
  As the admin user
  I want the server to store and validate advisory identifiers up to 256 characters

  Scenario: Pre-requisite: Widen the errata advisory and repo regen reason columns
    # WORKAROUND to be fixed in this card: https://github.com/SUSE/spacewalk/issues/32152
    # See https://github.com/SUSE/spacewalk/issues/30285#issuecomment-4279626321 for the original report.
    # Targets both the unpatched DBstring(100) backend (current state on Manager-5.2) and the
    # DBstring(150) state (bsc#1273846, master/head only, not yet backported) without touching
    # unrelated fields like "rights": DBstring(100).
    # No service restart here on purpose: taskomatic invokes spacewalk-repo-sync as a fresh
    # subprocess per sync (RepoSyncTask.getSyncCommand), so it re-reads this file from disk on
    # the next run; the ALTER/view changes are live for new DB connections immediately. A
    # restart would be actively harmful here - it recreates the server container from its base
    # image (confirmed: rpm -V shows the file reverts to the pristine packaged version), wiping
    # this sed patch before any reposync ever consumes it.
    When I run "sed -i -E -e 's/(advisory_name)(.): DBstring\((100|150)\)/\1\2: DBstring(256)/' -e 's/(advisory)(.): DBstring\((100|150)\)/\1\2: DBstring(256)/' /usr/lib/python3.*/site-packages/spacewalk/server/importlib/backendOracle.py" on "server"
    And I run "echo 'DROP VIEW IF EXISTS rhnServerOutdatedPackages; ALTER TABLE rhnRepoRegenQueue ALTER COLUMN reason TYPE varchar(256); ALTER TABLE rhnErrata ALTER COLUMN advisory TYPE varchar(256), ALTER COLUMN advisory_name TYPE varchar(256); CREATE OR REPLACE VIEW rhnServerOutdatedPackages (server_id, package_name_id, package_evr_id, package_arch_id, package_nvre, errata_id, errata_advisory) AS SELECT DISTINCT SNC.server_id, P.name_id, P.evr_id, P.package_arch_id, PN.name || CHR(45) || evr_t_as_vre_simple(PE.evr), E.id, E.advisory FROM rhnPackageName PN, rhnPackageEVR PE, rhnPackage P, rhnServerNeededCache SNC LEFT OUTER JOIN rhnErrata E ON SNC.errata_id = E.id WHERE SNC.package_id = P.id AND P.name_id = PN.id AND P.evr_id = PE.id;' | spacewalk-sql -" on "server"
