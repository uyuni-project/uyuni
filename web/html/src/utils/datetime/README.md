# Handling dates and times

All dates and times on the frontend should be handled via `localizedMoment`. `localizedMoment` is a wrapper around [Moment](https://momentjs.com/docs/) and [Moment Timezone](https://momentjs.com/timezone/) and can be initialized the same way a regular moment can:  

```ts
const now = localizedMoment();
```

Internally, values are handled as UTC. There is no need to manually handle timezones before passing `localizedMoment` instances to the API. The API internally stringifies the values with `JSON.stringify()` which results in ISO strings in UTC.  

```ts
try {
  const input = await Network.get("/foo");
  const result = localizedMoment(input);
  await Network.post("/bar", { result });
} catch (error) {
  // ...
}
```

`localizedMoment` exposes a number of methods such as `toUserDateTimeString()`, `toUserDateString()` etc to correctly display values to the user. In general, all values should be shown in the user configured time zone, but if needed, values can also be shown in the server time zone with `toServerDateTimeString()` etc.  

```tsx
const now = localizedMoment();
return (
  <p>Latest render was at {now.toUserDateTimeString()}</p>
);
```

Time zone configuration values are expected from the server and errors are logged when the values are missing or the server and the client can't agree on what the current date and time is (after accounting for time zones).  
