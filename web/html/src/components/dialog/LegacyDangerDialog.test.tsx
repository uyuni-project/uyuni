import { click, render, screen } from "utils/test-utils";

import { DangerDialog } from "./LegacyDangerDialog";

describe("LegacyDangerDialog", () => {
  const originalModal = (jQuery.fn as any).modal;

  afterEach(() => {
    if (originalModal === undefined) {
      delete (jQuery.fn as any).modal;
    } else {
      (jQuery.fn as any).modal = originalModal;
    }
  });

  test("hides before awaiting an asynchronous confirmation", async () => {
    const calls: string[] = [];
    const modal = jest.fn(function (this: JQuery) {
      calls.push("hide");
      return this;
    });
    const onConfirmAsync = jest.fn(async () => {
      calls.push("confirm");
    });
    (jQuery.fn as any).modal = modal;

    render(
      <DangerDialog id="legacy-danger-dialog" title="Confirm" submitText="Delete" onConfirmAsync={onConfirmAsync} />
    );

    await click(screen.getByRole("button", { name: "Delete" }));

    expect(modal).toBeCalledWith("hide");
    expect(onConfirmAsync).toBeCalledWith(true);
    expect(calls).toEqual(["hide", "confirm"]);
  });
});
