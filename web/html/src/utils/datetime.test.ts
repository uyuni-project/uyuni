// These are simply sufficiently different so it's easy to check outputs
window.userTimeZone = "America/Los_Angeles";
window.serverTimeZone = "Asia/Tokyo";

// eslint-disable-next-line import/first
import localizedMoment from "./datetime";

describe("localizedMoment", () => {
  const validISOString = "2020-01-30T23:00:00.000Z";

  test("can't be passed a Date instance", () => {
    expect(() => {
      localizedMoment(new Date() as any);
    }).toThrow();
  });

  test("parses valid input", () => {
    const result = localizedMoment(validISOString);
    expect(result.isValid()).toEqual(true);
    expect(result.toAPIString()).toEqual(validISOString);
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

  // TODO: What format do we expect to output here?
  /*
  test("toUserDisplayString", () => {
    const result = localizedMoment(validISOString);
    console.log(result.toUserDisplayString());
  });
  */
});
