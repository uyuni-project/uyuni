import * as React from "react";

import { clear, render, screen, type } from "utils/test-utils";

import { Form } from "../form/Form";
import { Range } from "./Range";

describe("Range", () => {
  function renderWithForm(content: React.ReactNode, initialModel = {}, onChange?, onSubmit?) {
    const Wrapper = () => {
      const [model, setModel] = React.useState(initialModel);
      return (
        <Form
          model={model}
          onChange={(newModel) => {
            setModel(newModel);
            onChange?.(newModel);
          }}
          onSubmit={(newModel) => {
            onSubmit?.(newModel);
          }}
        >
          {React.Children.toArray(content)}
        </Form>
      );
    };
    return render(<Wrapper />);
  }

  test("renders with minimal props", () => {
    expect(() => {
      renderWithForm(<Range name="range" prefix="port" />);
    }).not.toThrow();
  });

  test("renders with default values", () => {
    renderWithForm(<Range name="range" prefix="port" label="Port range" defaultStart="1000" defaultEnd="1100" />);
    const startInput = screen.getByRole("textbox", { name: "Port range start" }) as HTMLInputElement;
    expect(startInput.value).toBe("1000");
    const endInput = screen.getByRole("textbox", { name: "Port range end" }) as HTMLInputElement;
    expect(endInput.value).toBe("1100");
  });

  test("change values", async () => {
    const initialModel = {
      port_start: "10",
      port_end: "1100",
    };
    const onChange = jest.fn();

    renderWithForm(<Range name="range" prefix="port" label="Port range" />, initialModel, onChange);
    const startInput = screen.getByRole("textbox", { name: "Port range start" });

    clear(startInput);
    expect(onChange).toHaveBeenCalledWith({ port_start: "", port_end: "1100" });

    await type(startInput, "900");
    expect(onChange).toHaveBeenCalledWith({ port_start: "900", port_end: "1100" });

    const endInput = screen.getByRole("textbox", { name: "Port range end" });
    clear(endInput);
    await type(endInput, "903");
    expect(onChange).toHaveBeenCalledWith({ port_start: "900", port_end: "903" });
  });

  test("validation", async () => {
    const onChange = jest.fn();

    renderWithForm(
      <Range
        name="range"
        prefix="port"
        label="Port range"
        validate={(value) => {
          const message = t("Both values need to be positive integers");
          const hasValues = Object.values(value).every((item) => item != null);
          if (!hasValues) {
            return message;
          }

          const isInteger = Object.values(value).every((item) => typeof item === "string" && item.match(/^[0-9]+$/));
          if (!isInteger) {
            return message;
          }
          const { port_start, port_end } = value;
          const isOrdered = parseInt(port_start, 10) <= parseInt(port_end, 10);
          if (!isOrdered) {
            return message;
          }
        }}
      />,
      {},
      onChange
    );

    const startInput = screen.getByRole("textbox", { name: "Port range start" });
    const endInput = screen.getByRole("textbox", { name: "Port range end" });

    await type(startInput, "900");
    expect(onChange).toHaveBeenCalledWith({ port_start: "900", port_end: "" });
    screen.findByText(/Both values need to be positive integers/);

    await type(endInput, "800");
    expect(onChange).toHaveBeenCalledWith({ port_start: "900", port_end: "800" });
    screen.findByText(/Both values need to be positive integers/);

    await type(endInput, "NaN");
    expect(onChange).toHaveBeenCalledWith({ port_start: "900", port_end: "NaN" });
    screen.findByText(/Both values need to be positive integers/);

    await type(endInput, "901");
    expect(onChange).toHaveBeenCalledWith({ port_start: "900", port_end: "901" });
    expect(screen.queryByText(/Both values need to be positive integers/)).toBeNull();
  });
});
