--
-- Take seconds since epoch (January 1, 1970 UTC) and convert it
-- to time-zone'd timestamp. Mainly as compatibility with Oracle
-- which does not have the single-parameter to_timestamp.
--

create function epoch_seconds_to_timestamp_tz(secs in numeric)
returns timestamp with time zone
as
$$
begin
	return to_timestamp(secs);
end;
$$ language plpgsql;

