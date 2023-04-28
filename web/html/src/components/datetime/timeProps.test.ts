import { localizedMoment } from "utils";

import { getTimeProps } from "./timeProps";

describe("timeProps", () => {
  test("keeps timezone information intact", () => {
    const validISOString = "2020-01-30T23:00:00.000Z";

    const { title, dateTime } = getTimeProps(localizedMoment(validISOString));
    expect(title).toEqual("2020-01-30 15:00 PST");
    expect(dateTime).toEqual("2020-01-30T23:00:00.000+00:00");
  });
});
