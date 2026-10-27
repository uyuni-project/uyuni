import { localizedMoment } from "utils/datetime";
import Network from "utils/network";
import { act, fireEvent, render } from "utils/test-utils";

import { ActionSchedule } from "./action-schedule";

describe("ActionSchedule", () => {
  afterEach(() => jest.restoreAllMocks());

  test("ignores a maintenance response after unmount", async () => {
    let resolveRequest: (data: any) => void = () => undefined;
    const request = new Promise((resolve) => {
      resolveRequest = resolve;
    });
    jest.spyOn(Network, "post").mockReturnValue(request as any);
    const onDateTimeChanged = jest.fn();

    const { unmount } = render(
      <ActionSchedule
        earliest={localizedMoment()}
        systemIds={[1000]}
        actionType="state.apply"
        actionDescription=""
        onDateTimeChanged={onDateTimeChanged}
        onActionDescriptionChanged={jest.fn()}
      />
    );

    unmount();

    await act(async () => {
      resolveRequest({
        data: {
          maintenanceWindowsMultiSchedules: false,
          maintenanceWindows: [
            {
              id: 1,
              from: "2026-07-22 10:00",
              to: "2026-07-22 11:00",
              fromMilliseconds: 1784710800000,
              toMilliseconds: 1784714400000,
            },
          ],
        },
      });
      await request;
    });

    expect(onDateTimeChanged).not.toHaveBeenCalled();
  });

  test("handles action description changes and enforces maxLength 1000", async () => {
    jest.spyOn(Network, "post").mockResolvedValue({
      data: {
        maintenanceWindowsMultiSchedules: false,
        maintenanceWindows: [],
      },
    } as any);

    const onActionDescriptionChanged = jest.fn();

    const { findByText } = render(
      <ActionSchedule
        earliest={localizedMoment()}
        actionDescription=""
        onDateTimeChanged={jest.fn()}
        onActionDescriptionChanged={onActionDescriptionChanged}
      />
    );

    const input = (await findByText(t("Comment / Reason:")))
      .closest(".row")
      ?.querySelector("input") as HTMLInputElement;
    expect(input).toBeDefined();
    expect(input.maxLength).toBe(1000);

    // Type a normal description
    fireEvent.change(input, { target: { value: "Short description" } });
    expect(onActionDescriptionChanged).toHaveBeenCalledWith("Short description");

    // Type a 1000-character description (allowed)
    const exactDesc = "a".repeat(1000);
    fireEvent.change(input, { target: { value: exactDesc } });
    expect(onActionDescriptionChanged).toHaveBeenCalledWith(exactDesc);
  });
});
