\set aid random(1, :scale * 100000)
\set delta random(-1000, 1000)
UPDATE pgbench_accounts SET abalance = abalance + :delta WHERE aid = :aid;
INSERT INTO pgbench_history (tid, bid, aid, delta, mtime, filler)
VALUES (1, 1, :aid, :delta, now(), 'phase3');
