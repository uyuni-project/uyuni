import { localizedMoment } from "./localizedMoment";

describe("localizedMoment", () => {
  const validISOString = "2020-01-30T23:00:00.000Z";

  test("parses valid input", () => {
    const result = localizedMoment(validISOString);
    expect(result.isValid()).toEqual(true);
  });

  test("JSON.stringify() yields ISO strings in UTC", () => {
    expect(JSON.stringify(localizedMoment(validISOString))).toEqual(JSON.stringify(validISOString));
  });

  test("displaying formats doesn't mutate the underlying instance", () => {
    const result = localizedMoment(validISOString);
    result.toUserDateTimeString();
    expect(result.tz()).toEqual("UTC");
    result.toServerDateTimeString();
    expect(result.tz()).toEqual("UTC");
  });

  test("keeps utility functions", () => {
    expect(localizedMoment.isMoment(localizedMoment())).toEqual(true);
  });

  test("keeps globals", () => {
    expect(localizedMoment.serverTimeZone).toBeDefined();
    expect(localizedMoment.userTimeZone).toBeDefined();
  });

  test("doesn't expose internals", () => {
    expect((localizedMoment as any).userDateFormat).not.toBeDefined();
    expect((localizedMoment as any).userTimeFormat).not.toBeDefined();
  });

  test("implements all custom methods", () => {
    expect(localizedMoment().toServerString()).toBeDefined();
    expect(localizedMoment().toServerDateTimeString()).toBeDefined();
    expect(localizedMoment().toServerDateString()).toBeDefined();
    expect(localizedMoment().toServerTimeString()).toBeDefined();
    expect(localizedMoment().toUserString()).toBeDefined();
    expect(localizedMoment().toUserDateTimeString()).toBeDefined();
    expect(localizedMoment().toUserDateString()).toBeDefined();
    expect(localizedMoment().toUserTimeString()).toBeDefined();
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

    expect(localizedMoment(zonedStringB).toISOString(false)).toEqual(validISOString);
  });

  test("full server string keeps offset", () => {
    expect(localizedMoment().toServerString()).toContain("Asia/Tokyo");
  });

  test("full user string keeps offset", () => {
    expect(localizedMoment().toUserString()).toContain("America/Los_Angeles");
  });
});
