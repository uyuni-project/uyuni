import * as React from "react";
import { render, screen, openMenu, select, getFieldValuesByName, clearFirst, type } from "utils/test-utils";

import { Form } from "./Form";
import { Select } from "./Select";

describe("Select", () => {
  // Use these to test model changes in tests
  let model: object;
  let onChange: (model: any) => void;

  beforeEach(() => {
    model = {};
    onChange = () => {};
  });

  function renderWithForm(content: React.ReactNode) {
    return render(
      <Form model={model} onChange={onChange} title="test form">
        {React.Children.toArray(content)}
      </Form>
    );
  }

  test("renders with minimal props", () => {
    expect(() => {
      renderWithForm(<Select name="level" label="Level" options={["beginner", "normal", "expert"]} />);
    }).not.toThrow();
    openMenu(screen.getByLabelText("Level"));
    expect(screen.getByText("beginner")).toBeDefined();
  });

  test("renders with labelled options", async () => {
    renderWithForm(
      <Select
        name="level"
        label="Level"
        options={[
          { value: "beginner", label: "Beginner" },
          { value: "normal", label: "Normal" },
          { value: "expert", label: "Expert" },
        ]}
      />
    );
    await select(screen.getByLabelText("Level"), "Expert");
    expect(getFieldValuesByName("test form", "level")).toStrictEqual(["expert"]);
    openMenu(screen.getByLabelText("Level"));
    expect(screen.getByText("Beginner")).toBeDefined();
  });

  test("fancy multiple select test", async () => {
    model = { flavor: ["vanilla", "strawberry"] };
    renderWithForm(
      <Select
        name="flavor"
        label="Flavor"
        placeholder={t("Start typing...")}
        emptyText={t("No flavor")}
        options={[
          { value: "chocolate", label: "Chocolate", color: "#7B3F00" },
          { value: "strawberry", label: "Strawberry", color: "#DF0000" },
          { value: "vanilla", label: "Vanilla", color: "#F3E5AB" },
        ]}
        isMulti
        formatOptionLabel={(object) => <div style={{ color: object.color }}>{object.label}</div>}
      />
    );
    expect(getFieldValuesByName("test form", "flavor")).toStrictEqual(["vanilla", "strawberry"]);
    await clearFirst(screen.getByLabelText("Flavor"));
    expect(getFieldValuesByName("test form", "flavor")).toStrictEqual(["strawberry"]);
    await select(screen.getByLabelText("Flavor"), "Chocolate");
    expect(screen.getByText("Chocolate").style.color).toEqual("rgb(123, 63, 0)");
    expect(getFieldValuesByName("test form", "flavor")).toStrictEqual(["strawberry", "chocolate"]);
    await clearFirst(screen.getByLabelText("Flavor"));
    await clearFirst(screen.getByLabelText("Flavor"));
    expect(getFieldValuesByName("test form", "flavor")).toStrictEqual([""]);
    expect(screen.getByText("Start typing...")).toBeDefined();
    await type(screen.getByLabelText("Flavor"), "Mint");
    expect(screen.getByText("No flavor")).toBeDefined();
  });

  // Previously the value was set but it was not correctly reflected in the UI
  test("default value is shown to the user", () => {
    model = {};
    renderWithForm(
      <Select
        name="foo"
        options={[
          { value: "value 1", label: "label 1" },
          { value: "value 2", label: "label 2" },
          { value: "value 3", label: "label 3" },
        ]}
        defaultValue="value 2"
      />
    );
    expect(model).toStrictEqual({ foo: "value 2" });
    expect(screen.getByText("label 2")).toBeDefined();
  });
});
