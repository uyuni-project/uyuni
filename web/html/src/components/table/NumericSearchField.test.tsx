/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { click, fireEvent, render, screen } from "utils/test-utils";

import { NumericSearchField } from "./NumericSearchField";

describe("NumericSearchField", () => {
  test("keeps matcher IDs unique and stable, and filters independent", async () => {
    const onFirstSearch = jest.fn();
    const onSecondSearch = jest.fn();
    const fields = () => (
      <>
        <NumericSearchField name="memory" criteria=">=8" onSearch={onFirstSearch} />
        <NumericSearchField name="memory" criteria="<=16" onSearch={onSecondSearch} />
      </>
    );
    const { container, rerender } = render(fields());
    const matcherInputs = () => Array.from(container.querySelectorAll<HTMLInputElement>("input[id]"));
    const ids = matcherInputs().map((input) => input.id);
    expect(ids).toHaveLength(2);
    expect(new Set(ids).size).toBe(2);

    rerender(fields());
    expect(matcherInputs().map((input) => input.id)).toEqual(ids);

    onFirstSearch.mockClear();
    onSecondSearch.mockClear();
    fireEvent.keyDown(matcherInputs()[1], { key: "ArrowDown", keyCode: 40 });
    await click(screen.getByText("less than"));
    expect(onSecondSearch).toHaveBeenLastCalledWith("<16");
    expect(onFirstSearch).not.toHaveBeenCalled();

    const values = screen.getAllByRole("spinbutton") as HTMLInputElement[];
    expect(values[0].value).toBe("8");
    fireEvent.change(values[1], { target: { value: "20" } });
    expect(onSecondSearch).toHaveBeenLastCalledWith("<20");
    expect(onFirstSearch).not.toHaveBeenCalled();
  });
});
