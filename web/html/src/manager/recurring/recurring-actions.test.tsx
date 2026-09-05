import { beforeEach, describe, expect, jest, test } from "@jest/globals";

import { render } from "utils/test-utils";

import { RecurringActions } from "./recurring-actions";

jest.mock("./recurring-actions-details", () => ({ RecurringActionsDetails: () => null }));
jest.mock("./recurring-actions-edit", () => ({ RecurringActionsEdit: () => null }));
jest.mock("./recurring-actions-list", () => ({ RecurringActionsList: () => null }));

describe("RecurringActions", () => {
  beforeEach(() => {
    delete window.entityType;
    window.history.replaceState(null, "", "/rhn/manager/schedule/recurring-actions");
  });

  test("does not add an undefined action to the unfiltered list URL", () => {
    render(<RecurringActions />);

    expect(window.location.pathname).toBe("/rhn/manager/schedule/recurring-actions");
    expect(window.location.hash).toBe("");
  });
});
