import { applyOverrides } from "./override";

describe("license overrides", () => {
  test("replaces the deprecated GPL-3.0 identifier from pwstrength-bootstrap", () => {
    expect(applyOverrides("pwstrength-bootstrap", "1.2.6", "(GPL-3.0 OR MIT)")).toBe("(GPL-3.0-only OR MIT)");
  });
});
