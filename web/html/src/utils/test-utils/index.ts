// See https://testing-library.com/docs/ecosystem-user-event/#api
import userEvent from "@testing-library/user-event";

// Reexport everything so we can get all of our utilities from a single location
// See https://testing-library.com/docs/react-testing-library/api/
export * from "@testing-library/react";

export * from "./timer";
export * from "./forms";
export * from "./server";
export * from "./mock";
export { screen } from "./screen";

// @testing-library/user-event has messed up exports, just manually reexport everything
export const {
  click,
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
