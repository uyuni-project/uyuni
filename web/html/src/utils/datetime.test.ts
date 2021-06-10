import { localizedMoment } from "./datetime";

console.log(window.userTimeZone);
console.log(window.serverTimeZone);

describe("localizedMoment", () => {
  const validISOString = "2020-01-30T23:00:00.000Z";

  test("parses valid input", () => {
    const result = localizedMoment(validISOString);
    expect(result.isValid()).toEqual(true);
    expect(result.toAPIValue()).toEqual(validISOString);
  });

  test("displaying formats doesn't mutate the underlying instance", () => {
    const result = localizedMoment(validISOString);
    result.toUserDateTimeString();
    expect(result.tz()).toEqual("UTC");
    result.toServerDateTimeString();
    expect(result.tz()).toEqual("UTC");
    result.toAPIValue();
    expect(result.tz()).toEqual("UTC");
  });

  test("keeps utility functions", () => {
    expect(localizedMoment.isMoment(localizedMoment())).toEqual(true);
  });

  test("correctly rolls between zones", () => {
    const original = localizedMoment(validISOString);
    // This is not how we usually consume this, but it is a valid use case which covers corner cases
    const zonedStringA = localizedMoment(original)
      .tz(localizedMoment.userTimeZone)
      .toISOString(true);
    expect(zonedStringA).toEqual("2020-01-30T15:00:00.000-08:00");

    const zonedStringB = localizedMoment(zonedStringA)
      .tz(localizedMoment.serverTimeZone)
      .toISOString(true);
    // Note the shifted date
    expect(zonedStringB).toEqual("2020-01-31T08:00:00.000+09:00");

    expect(localizedMoment(zonedStringB).toISOString()).toEqual(validISOString);
  });

  // TODO: What format do we expect to output here?
  /*
  test("toUserDisplayString", () => {
    const result = localizedMoment(validISOString);
    console.log(result.toUserDisplayString());
  });
  */
});
