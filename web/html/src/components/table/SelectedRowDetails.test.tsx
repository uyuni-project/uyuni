import { fireEvent, render, screen } from "@testing-library/react";

import { SelectedRowDetails } from "./SelectedRowDetails";

describe("SelectedRowDetails", () => {
  const defaultProps = {
    itemCount: 10,
    onClear: jest.fn(),
    onSelectAll: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("is hidden when no items are selected", () => {
    const { container } = render(<SelectedRowDetails {...defaultProps} selectable selectedCount={0} />);

    expect(container.querySelector(".hide-details")).toBeTruthy();
  });

  test("shows selected count and action buttons when some items are selected", () => {
    render(<SelectedRowDetails {...defaultProps} selectable selectedCount={3} />);

    expect(screen.getByText(/3 items selected/i)).toBeTruthy();
    expect(screen.getByRole("button", { name: /clear all/i })).toBeTruthy();
    expect(screen.getByRole("button", { name: /across all pages/i })).toBeTruthy();
  });

  test("shows all selected message when all items are selected", () => {
    render(<SelectedRowDetails {...defaultProps} selectable selectedCount={10} />);

    expect(screen.getByText(/across all pages selected/i)).toBeTruthy();

    expect(screen.queryByRole("button", { name: /select all/i })).toBeNull();
  });

  test("calls onSelectAll when Select All button is clicked", () => {
    const onSelectAll = jest.fn();

    render(<SelectedRowDetails {...defaultProps} selectable selectedCount={3} onSelectAll={onSelectAll} />);

    fireEvent.click(screen.getByRole("button", { name: /select all/i }));

    expect(onSelectAll).toHaveBeenCalledTimes(1);
  });

  test("calls onClear when Clear All button is clicked", () => {
    const onClear = jest.fn();

    render(<SelectedRowDetails {...defaultProps} selectable selectedCount={3} onClear={onClear} />);

    fireEvent.click(screen.getByRole("button", { name: /clear all/i }));

    expect(onClear).toHaveBeenCalledTimes(1);
  });
});
