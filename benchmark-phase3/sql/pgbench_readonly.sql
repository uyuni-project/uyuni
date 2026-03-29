\set aid random(1, :scale * 100000)
SELECT abalance FROM pgbench_accounts WHERE aid = :aid;
