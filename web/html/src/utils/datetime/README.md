TODO: Write once the spec is finalized

There is no need to manually handle timezones before passing `localizedMoment` instances to the API. The API internally stringifies the values with `JSON.stringify()` which results in ISO strings in UTC.  
