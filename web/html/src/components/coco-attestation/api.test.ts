import { describe, expect, test } from "@jest/globals";

import { normalizeSingleCoCoSettingsResponse } from "./api";

const settings = {
  enabled: true,
  environmentType: "KVM_AMD",
  attestOnBoot: false,
  attestOnSchedule: false,
  inputData: {},
};

describe("normalizeSingleCoCoSettingsResponse", () => {
  test("keeps the current nested response format", () => {
    expect(normalizeSingleCoCoSettingsResponse({ supported: true, settings })).toEqual({
      supported: true,
      settings,
    });
  });

  test("supports the legacy flat response format", () => {
    expect(normalizeSingleCoCoSettingsResponse({ supported: true, ...settings })).toEqual({
      supported: true,
      settings,
    });
  });
});
