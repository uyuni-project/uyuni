-- oracle equivalent source sha1 d8f296e4286ae00d6adf04188d3714c7990a10b8
-- Pre-task: we need to make sure there are no custom states with name equal to
-- some existing configuration channel label. We'll relabel the conflicting
-- channels  by appending a random suffix to them.

UPDATE rhnConfigChannel
SET label = label || '-' || (SELECT cast(trunc(random() * 1000000) AS TEXT) FROM DUAL)
WHERE label IN (SELECT state_name FROM suseCustomState);
