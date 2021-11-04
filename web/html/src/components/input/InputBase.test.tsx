import * as React from "react";
import { render, screen } from "utils/test-utils";

import { Form } from "./Form";
import { InputBase } from "./InputBase";

describe("InputBase", () => {
  // Use these to test model changes in tests
  let model;
  let onChange;

  // Children are mandatory in InputBase
  const placeholderChild = () => null;

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
      renderWithForm(<InputBase name="foo">{placeholderChild}</InputBase>);
    }).not.toThrow();
  });

  test("triggers onChange", () => {
    // Only set value once
    let isFirstFire = true;
    model = {
      foo: "foo",
    };

    renderWithForm(
      <InputBase name="foo" invalidHint={t("Minimum 2 characters")} validators={[(value) => value.length > 2]}>
        {({ setValue }) => {
          if (isFirstFire) {
            // Realistically this should be with a user interaction, but we manually fire it off to see if it propagates
            setValue("foo", "bar");
            isFirstFire = false;
          }
          return null;
        }}
      </InputBase>
    );
    expect(model).toStrictEqual({ foo: "bar" });
    expect(screen.queryByText(/Minimum 2 characters/)).toBeNull();
  });

  test("validation error", () => {
    // Only set value once
    let isFirstFire = true;
    model = {
      username: "jdoe",
    };

    renderWithForm(
      <InputBase
        name="username"
        label="Username"
        invalidHint={t("Minimum 2 characters")}
        validators={[(value) => value.length > 2]}
      >
        {({ setValue }) => {
          if (isFirstFire) {
            setValue("username", "fo");
            isFirstFire = false;
          }
          return null;
        }}
      </InputBase>
    );
    expect(model).toStrictEqual({ username: "fo" });
    screen.findByText(/Minimum 2 characters/);
  });

  test("multiple properties", () => {
    // Only set value once
    let isFirstFire = true;
    model = {
      firstname: "Joe",
      lastname: "Hacker",
    };

    renderWithForm(
      <InputBase
        name={["firstname", "lastname"]}
        label="User"
        invalidHint={t("Minimum 2 characters")}
        validators={[(value) => Object.values<typeof model>(value).every((v) => v.length > 2)]}
      >
        {({ setValue }) => {
          if (isFirstFire) {
            setValue("firstname", "John");
            isFirstFire = false;
          }
          return null;
        }}
      </InputBase>
    );
    expect(model).toStrictEqual({ firstname: "John", lastname: "Hacker" });
    expect(screen.queryByText(/Minimum 2 characters/)).toBeNull();
  });

  test("default value applies to the model when no value is present", () => {
    model = {};

    renderWithForm(
      <InputBase name="foo" defaultValue="defaultValue">
        {placeholderChild}
      </InputBase>
    );
    expect(model).toStrictEqual({ foo: "defaultValue" });
  });

  test("default value does not apply to the model when a value is already present", () => {
    model = {
      foo: "initialValue",
    };

    renderWithForm(
      <InputBase name="foo" defaultValue="defaultValue">
        {placeholderChild}
      </InputBase>
    );
    expect(model).toStrictEqual({ foo: "initialValue" });
  });
});
