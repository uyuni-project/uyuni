// See https://testing-library.com/docs/ecosystem-user-event/#api
import userEvent from "@testing-library/user-event";
import { act } from "react-dom/test-utils";

import { asyncIdleCallback } from "utils/idle-callback";

// Reexport everything so we can get all of our utilities from a single location
// See https://testing-library.com/docs/react-testing-library/api/
export * from "@testing-library/react";

export * from "./timer";
export * from "./timeout";
export * from "./forms";
export * from "./server";
export * from "./mock";
export { screen } from "./screen";

export const click = async (...args: Parameters<typeof userEvent.click>) => {
  await act(async () => {
    await new Promise<void>(async (resolve) => {
      await userEvent.click(...args);
      asyncIdleCallback(() => resolve(), 0);
    });
  });
};

// @testing-library/user-event has messed up exports, just manually reexport everything
export const {
  dblClick,
  upload,
  clear,
  selectOptions,
  deselectOptions,
  tab,
  hover,
  unhover,
  paste,
  // `type` is intentionally omitted here, see `./forms.ts`
} = userEvent;
