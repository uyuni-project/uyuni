import { fireEvent, render } from "@testing-library/react";

import { ToggleButtonGroup, ToggleButtonOption } from "./toggle-button-group";

describe("ToggleButtonGroup", () => {
  const options: ToggleButtonOption<string>[] = [
    { value: "option1", label: "Option 1" },
    { value: "option2", label: "Option 2" },
    { value: "option3", label: "Option 3", disabled: true },
  ];

  test("renders buttons based on options", () => {
    const { getByText } = render(<ToggleButtonGroup value="option1" options={options} onChange={() => {}} />);

    expect(getByText("Option 1")).toBeTruthy();
    expect(getByText("Option 2")).toBeTruthy();
    expect(getByText("Option 3")).toBeTruthy();
  });

  test("calls onChange with correct value when a button is clicked", () => {
    const handleChange = jest.fn();
    const { getByText } = render(<ToggleButtonGroup value="option1" options={options} onChange={handleChange} />);

    fireEvent.click(getByText("Option 2"));
    expect(handleChange).toHaveBeenCalledWith("option2");
  });

  test("disabled buttons do not trigger onChange", () => {
    const handleChange = jest.fn();
    const { getByText } = render(<ToggleButtonGroup value="option1" options={options} onChange={handleChange} />);

    fireEvent.click(getByText("Option 3"));
    expect(handleChange).not.toHaveBeenCalled();
  });
});
