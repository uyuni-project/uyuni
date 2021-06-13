import { localizedMoment } from "./localizedMoment";

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
    expect(result.utcOffset()).toEqual(0);
    result.toServerDateTimeString();
    expect(result.utcOffset()).toEqual(0);
    result.toAPIValue();
    expect(result.utcOffset()).toEqual(0);
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
      .utcOffset(localizedMoment.userTimeZone.utcOffset)
      .toISOString(true);
    expect(zonedStringA).toEqual("2020-01-30T16:00:00.000-07:00");

    const zonedStringB = localizedMoment(zonedStringA)
      .utcOffset(localizedMoment.serverTimeZone.utcOffset)
      .toISOString(true);
    // Note the shifted date
    expect(zonedStringB).toEqual("2020-01-31T08:00:00.000+09:00");

    expect(localizedMoment(zonedStringB).toISOString(false)).toEqual(validISOString);
  });

  test("full server string keeps offset", () => {
    expect(localizedMoment().toServerString()).toContain("GMT+9");
  });

  test("full user string keeps offset", () => {
    expect(localizedMoment().toUserString()).toContain("GMT-7");
  });
});
