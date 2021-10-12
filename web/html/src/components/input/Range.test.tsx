import * as React from "react";
import { render, screen, clear, type, waitForElementToBeRemoved } from "utils/test-utils";

import { Form } from "./Form";
import { Range } from "./Range";

describe("Range", () => {
  // Use these to test model changes in tests
  let model;
  let onChange;

  beforeEach(() => {
    model = {};
    onChange = () => {};
  });

  function renderWithForm(content) {
    return render(
      <Form model={model} onChange={onChange}>
        {React.Children.toArray(content)}
      </Form>
    );
  }

  test("renders with minimal props", () => {
    expect(() => {
      renderWithForm(<Range name="range" prefix="port" />);
    }).not.toThrow();
  });

  test("renders with default values", () => {
    model = {};
    renderWithForm(<Range name="range" prefix="port" label="Port range" defaultStart="1000" defaultEnd="1100" />);
    const startInput = screen.getByRole("textbox", { name: "Port range start" }) as HTMLInputElement;
    expect(startInput.value).toBe("1000");
    const endInput = screen.getByRole("textbox", { name: "Port range end" }) as HTMLInputElement;
    expect(endInput.value).toBe("1100");
  });

  test("change values", async () => {
    model = {
      port_start: "10",
      port_end: "1100",
    };
    onChange = () => {};

    renderWithForm(<Range name="range" prefix="port" label="Port range" />);
    const startInput = screen.getByRole("textbox", { name: "Port range start" });

    clear(startInput);
    expect(model).toStrictEqual({ port_start: "", port_end: "1100" });

    await type(startInput, "900");
    expect(model).toStrictEqual({ port_start: "900", port_end: "1100" });

    const endInput = screen.getByRole("textbox", { name: "Port range end" });
    clear(endInput);
    await type(endInput, "903");
    expect(model).toStrictEqual({ port_start: "900", port_end: "903" });
  });

  test("validation", async (done) => {
    onChange = () => done();

    renderWithForm(
      <Range
        name="range"
        prefix="port"
        label="Port range"
        invalidHint={t("Both values need to be positive integers")}
        validators={[
          (value) => Object.values(value).every((item) => item != null),
          (value) => Object.values(value).every((item) => typeof item === "string" && item.match(/^[0-9]+$/)),
          ({ port_start, port_end }) => parseInt(port_start, 10) <= parseInt(port_end, 10),
        ]}
      />
    );

    const startInput = screen.getByRole("textbox", { name: "Port range start" });
    const endInput = screen.getByRole("textbox", { name: "Port range end" });

    await type(startInput, "900");
    expect(model).toStrictEqual({ port_start: "900" });
    screen.findByText(/Both values need to be positive integers/);

    await type(endInput, "800");
    expect(model).toStrictEqual({ port_start: "900", port_end: "800" });
    screen.findByText(/Both values need to be positive integers/);

    await type(endInput, "NaN");
    expect(model).toStrictEqual({ port_start: "900", port_end: "NaN" });
    screen.findByText(/Both values need to be positive integers/);

    await type(endInput, "901");
    expect(model).toStrictEqual({ port_start: "900", port_end: "901" });
    await waitForElementToBeRemoved(() => screen.queryByText(/Both values need to be positive integers/));
  });
});
