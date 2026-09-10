import { localizedMoment } from "utils/datetime";
import Network from "utils/network";
import { act, render } from "utils/test-utils";

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
        onDateTimeChanged={onDateTimeChanged}
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
});
