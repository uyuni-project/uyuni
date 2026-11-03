/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { SectionState } from "components/FormulaForm";

import { act, click, fireEvent, render, screen } from "utils/test-utils";

import {
  ElementDefinition,
  evalExpression,
  FormulaFormContext,
  FormulaFormContextProvider,
  FormulaFormRenderer,
  generateFormulaComponent,
  generateFormulaComponentForId,
  get,
  isFiltered,
  text,
} from "./FormulaComponentGenerator";

/**
 * The default wrapper renders the label as a sibling of the input without a `for`
 * attribute, so the generated fields can only be looked up by their id.
 */
const field = <T extends HTMLElement = HTMLInputElement>(id = "field") => document.getElementById(id) as T;

/** Minimal stand-in for the form instance the generator reads from */
const mockFormulaForm = (overrides: any = {}) => ({
  props: {
    scope: "system",
    sectionsExpanded: SectionState.Expanded,
    setSectionsExpanded: jest.fn(),
    searchCriteria: "",
    ...overrides.props,
  },
  handleChange: jest.fn(),
  getFormulaValues: () => overrides.values ?? {},
});

/** Render whatever the generator produced for a single element */
const renderElement = (element: ElementDefinition, value: any, options: any = {}) => {
  const formulaForm = options.formulaForm ?? mockFormulaForm(options);
  const utils = render(
    <>
      {generateFormulaComponentForId(
        element,
        value,
        formulaForm,
        options.id ?? "field",
        options.wrapper,
        options.disabled
      )}
    </>
  );
  return { ...utils, formulaForm };
};

describe("get", () => {
  test("falls back to the default only for undefined", () => {
    expect(get(undefined, "fallback")).toBe("fallback");
    expect(get(null, "fallback")).toBeNull();
    expect(get(0, "fallback")).toBe(0);
    expect(get("", "fallback")).toBe("");
    expect(get(false, "fallback")).toBe(false);
  });
});

describe("text", () => {
  test("returns an empty string for any falsy input", () => {
    expect(text(undefined)).toBe("");
    expect(text(null)).toBe("");
    expect(text("")).toBe("");
    // Numbers are not special cased, so 0 collapses to an empty string too
    expect(text(0)).toBe("");
  });

  test("expands the ${productName} placeholder", () => {
    expect(text("Welcome to ${productName}")).toBe("Welcome to Uyuni");
    expect(text("${productName} and ${productName}")).toBe("Uyuni and Uyuni");
  });

  test("passes non-string values through untouched", () => {
    const element = { $name: "x" };
    expect(text(element)).toBe(element);
    expect(text(42)).toBe(42);
  });
});

describe("isFiltered", () => {
  test("is only truthy for a non-empty criteria", () => {
    expect(isFiltered("abc")).toBeTruthy();
    expect(isFiltered("")).toBeFalsy();
    expect(isFiltered(undefined)).toBeFalsy();
    expect(isFiltered(null)).toBeFalsy();
  });
});

describe("evalExpression", () => {
  test("evaluates a literal condition", () => {
    const formulaForm = mockFormulaForm();

    expect(evalExpression("field", true, formulaForm)).toBe(true);
    expect(evalExpression("field", false, formulaForm)).toBe(false);
  });

  test("exposes the whole form under formValues", () => {
    const formulaForm = mockFormulaForm({ values: { mode: "advanced" } });

    expect(evalExpression("field", "formValues.mode == 'advanced'", formulaForm)).toBe(true);
    expect(evalExpression("field", "formValues.mode == 'basic'", formulaForm)).toBe(false);
  });

  test("exposes the value at the given id under `this`", () => {
    const formulaForm = mockFormulaForm({ values: { group: { field: "on" } } });

    expect(evalExpression("group#field", "this.value == 'on'", formulaForm)).toBe(true);
  });

  test("exposes the enclosing values under `this.parent`", () => {
    const formulaForm = mockFormulaForm({ values: { group: { sibling: "yes", field: "on" } } });

    expect(evalExpression("group#field", "this.parent.value.sibling == 'yes'", formulaForm)).toBe(true);
  });

  test("resolves `this` to null when the id has no value yet", () => {
    const formulaForm = mockFormulaForm({ values: {} });

    expect(evalExpression("missing", "this == null", formulaForm)).toBe(true);
  });
});

describe("generateFormulaComponent", () => {
  const element: any = { $id: "port", $type: "number", $name: "Port", $scope: "system" };

  test("builds the element id by joining the parents with the element $id", () => {
    render(<>{generateFormulaComponent({ ...element }, "80", mockFormulaForm(), "network#tcp")}</>);

    expect(field("network#tcp#port")).toBeTruthy();
  });

  test("uses the bare $id when there are no parents", () => {
    render(<>{generateFormulaComponent({ ...element }, "80", mockFormulaForm())}</>);

    expect(field("port")).toBeTruthy();
  });
});

describe("generateFormulaComponentForId", () => {
  describe("basic input types", () => {
    // jsdom rejects values that do not fit the input type, so each type needs a valid sample
    test.each([
      ["text", "some text"],
      ["email", "user@example.com"],
      ["url", "https://example.com"],
      ["date", "2026-01-31"],
      ["time", "10:30"],
    ])("renders a %s input", (type, value) => {
      renderElement({ $type: type, $name: "Field", $scope: "system" }, value);

      const input = field<HTMLInputElement>();
      expect(input.tagName).toBe("INPUT");
      expect(input.type).toBe(type);
      expect(input.value).toBe(value);
    });

    test("carries the name, placeholder, title and pattern over to the input", () => {
      renderElement(
        {
          $type: "text",
          $name: "Hostname",
          $scope: "system",
          $placeholder: "e.g. localhost",
          $help: "The host to connect to",
          $match: "[a-z]+",
        },
        "srv"
      );

      const input = field<HTMLInputElement>();
      expect(input.name).toBe("Hostname");
      expect(input.placeholder).toBe("e.g. localhost");
      expect(input.title).toBe("The host to connect to");
      expect(input.pattern).toBe("[a-z]+");
    });

    test("reports edits through the form's change handler", () => {
      const { formulaForm } = renderElement({ $type: "text", $name: "Field", $scope: "system" }, "");

      fireEvent.change(field(), { target: { value: "typed" } });

      expect(formulaForm.handleChange).toHaveBeenCalled();
    });
  });

  describe("number", () => {
    test("renders a number input with the configured bounds", () => {
      renderElement({ $type: "number", $name: "Port", $scope: "system", $min: 1, $max: 65535 }, 80);

      const input = field<HTMLInputElement>();
      expect(input.type).toBe("number");
      expect(input.min).toBe("1");
      expect(input.max).toBe("65535");
      expect(input.step).toBe("1");
      expect(input.value).toBe("80");
    });

    test("leaves the bounds empty when none are configured", () => {
      renderElement({ $type: "number", $name: "Port", $scope: "system" }, 80);

      expect(field<HTMLInputElement>().min).toBe("");
      expect(field<HTMLInputElement>().max).toBe("");
    });
  });

  describe("color", () => {
    test("renders a color input inside a picker group", () => {
      const { container } = renderElement({ $type: "color", $name: "Accent", $scope: "system" }, "#ff0000");

      const input = container.querySelector(".small-color-picker input") as HTMLInputElement;
      expect(input.type).toBe("color");
      expect(input.value).toBe("#ff0000");
    });
  });

  describe("datetime", () => {
    test("renders a datetime-local input", () => {
      renderElement({ $type: "datetime", $name: "Starts", $scope: "system" }, "2026-01-01T10:00");

      const input = field<HTMLInputElement>();
      expect(input.type).toBe("datetime-local");
      expect(input.value).toBe("2026-01-01T10:00");
    });
  });

  describe("textarea", () => {
    test("renders a textarea with the configured size", () => {
      renderElement({ $type: "textarea", $name: "Script", $scope: "system", $cols: 40, $rows: 12 }, "echo hi");

      const textarea = field<HTMLTextAreaElement>();
      expect(textarea.tagName).toBe("TEXTAREA");
      expect(textarea.cols).toBe(40);
      expect(textarea.rows).toBe(12);
      expect(textarea.value).toBe("echo hi");
    });

    test("defaults to five rows", () => {
      renderElement({ $type: "textarea", $name: "Script", $scope: "system" }, "");

      expect(field<HTMLTextAreaElement>().rows).toBe(5);
    });
  });

  describe("select", () => {
    test("renders one option per configured value", () => {
      renderElement({ $type: "select", $name: "Mode", $scope: "system", $values: ["fast", "safe"] }, "safe");

      const select = field<HTMLSelectElement>();
      expect(Array.from(select.options).map((option) => option.value)).toEqual(["fast", "safe"]);
      expect(select.value).toBe("safe");
    });

    test("renders no options when there are no configured values", () => {
      renderElement({ $type: "select", $name: "Mode", $scope: "system" }, "");

      expect(field<HTMLSelectElement>().options).toHaveLength(0);
    });
  });

  describe("boolean", () => {
    test("renders a checkbox labelled with the element name", () => {
      renderElement({ $type: "boolean", $name: "Enabled", $scope: "system" }, true);

      const checkbox = screen.getByLabelText("Enabled") as HTMLInputElement;
      expect(checkbox.type).toBe("checkbox");
      expect(checkbox.checked).toBe(true);
    });

    test("shows a help icon next to the checkbox when the help differs from the name", () => {
      const { container } = renderElement(
        { $type: "boolean", $name: "Enabled", $scope: "system", $help: "Turns the feature on" },
        false
      );

      expect(container.querySelector(".help-icon-checkbox .spacewalk-help-link")?.getAttribute("title")).toBe(
        "Turns the feature on"
      );
    });

    test("omits the help icon when the help repeats the name", () => {
      const { container } = renderElement(
        { $type: "boolean", $name: "Enabled", $scope: "system", $help: "Enabled" },
        false
      );

      expect(container.querySelector(".help-icon-checkbox")).toBeNull();
    });
  });

  describe("password", () => {
    test("renders the password input with generate and reveal buttons", () => {
      renderElement({ $type: "password", $name: "Secret", $scope: "system" }, "hunter2");

      expect(field<HTMLInputElement>().type).toBe("password");
      expect(screen.getByTitle("Generate new password")).toBeTruthy();
      expect(screen.getByTitle("Show/hide password")).toBeTruthy();
    });

    test("renders an empty password as text to avoid browser autocompletion", () => {
      renderElement({ $type: "password", $name: "Secret", $scope: "system" }, "");

      expect(field<HTMLInputElement>().type).toBe("text");
    });
  });

  describe("group", () => {
    const groupElement = () =>
      ({
        $type: "group",
        $name: "Network",
        $scope: "system",
        host: { $id: "host", $type: "text", $name: "Hostname", $scope: "system" },
      }) as any;

    test("renders a panel holding the children of the group", () => {
      renderElement(groupElement(), { host: "localhost" });

      expect(screen.getByText("Network")).toBeTruthy();
      expect(field<HTMLInputElement>("field#host").value).toBe("localhost");
    });

    test("hides the group when neither it nor its children match the search criteria", () => {
      const { container } = renderElement(
        groupElement(),
        { host: "localhost" },
        {
          props: { searchCriteria: "storage" },
        }
      );

      expect(container.innerHTML).toBe("");
    });

    test("keeps the group when one of its children matches the search criteria", () => {
      renderElement(groupElement(), { host: "localhost" }, { props: { searchCriteria: "hostname" } });

      expect(field("field#host")).toBeTruthy();
    });
  });

  describe("namespace", () => {
    test("renders the children without any surrounding panel", () => {
      const { container } = renderElement(
        {
          $type: "namespace",
          $name: "Hidden",
          $scope: "system",
          host: { $id: "host", $type: "text", $name: "Host", $scope: "system" },
        } as any,
        { host: "localhost" }
      );

      expect(field("field#host")).toBeTruthy();
      expect(container.querySelector(".panel")).toBeNull();
      expect(screen.queryByText("Hidden")).toBeNull();
    });
  });

  describe("edit-group", () => {
    test("renders an edit group with its add button", () => {
      renderElement(
        {
          $type: "edit-group",
          $name: "Servers",
          $scope: "system",
          $minItems: 0,
          $maxItems: 3,
          $newItemValue: "",
          $prototype: { $type: "text", $name: "Server", $scope: "system" },
        },
        ["alpha"]
      );

      expect(screen.getByRole("button", { name: "Add Servers" })).toBeTruthy();
      expect(screen.getByDisplayValue("alpha")).toBeTruthy();
    });
  });

  describe("unknown types", () => {
    test("logs an error and dumps the raw value", () => {
      const errorSpy = jest.spyOn(Loggerhead, "error").mockImplementation(() => {});

      renderElement({ $type: "wat", $name: "Mystery", $scope: "system" }, { a: 1 });

      expect(errorSpy).toHaveBeenCalledWith("Unknown $type: wat");
      expect(screen.getByText('{"a":1}')).toBeTruthy();

      errorSpy.mockRestore();
    });
  });

  describe("visibility", () => {
    test("renders nothing when $visible evaluates to false", () => {
      const { container } = renderElement(
        { $type: "text", $name: "Field", $scope: "system", $visible: "formValues.mode == 'advanced'" } as any,
        "",
        { values: { mode: "basic" } }
      );

      expect(container.innerHTML).toBe("");
    });

    test("renders the element when $visible evaluates to true", () => {
      renderElement(
        { $type: "text", $name: "Field", $scope: "system", $visible: "formValues.mode == 'advanced'" } as any,
        "",
        { values: { mode: "advanced" } }
      );

      expect(field()).toBeTruthy();
    });

    test("honours the deprecated $visibleIf equality condition", () => {
      const visible = renderElement(
        { $type: "text", $name: "Field", $scope: "system", $visibleIf: "mode == 'advanced'" } as any,
        "",
        { values: { mode: "advanced" } }
      );
      expect(visible.container.innerHTML).not.toBe("");
      visible.unmount();

      const hidden = renderElement(
        { $type: "text", $name: "Field", $scope: "system", $visibleIf: "mode == 'advanced'" } as any,
        "",
        { values: { mode: "basic" } }
      );
      expect(hidden.container.innerHTML).toBe("");
    });

    test("honours the deprecated $visibleIf inequality condition", () => {
      const { container } = renderElement(
        { $type: "text", $name: "Field", $scope: "system", $visibleIf: "mode != 'basic'" } as any,
        "",
        { values: { mode: "basic" } }
      );

      expect(container.innerHTML).toBe("");
    });

    test("hides the element when the deprecated $visibleIf has no comparison at all", () => {
      const { container } = renderElement(
        { $type: "text", $name: "Field", $scope: "system", $visibleIf: "mode" } as any,
        "",
        { values: { mode: "advanced" } }
      );

      expect(container.innerHTML).toBe("");
    });
  });

  describe("required", () => {
    test("marks the input and its label as required when $required evaluates to true", () => {
      const { container } = renderElement(
        { $type: "text", $name: "Field", $scope: "system", $required: true } as any,
        ""
      );

      expect(field<HTMLInputElement>().required).toBe(true);
      expect(container.querySelector(".required-form-field")).toBeTruthy();
    });

    test("leaves the input optional when $required evaluates to false", () => {
      const { container } = renderElement(
        { $type: "text", $name: "Field", $scope: "system", $required: "formValues.mode == 'advanced'" } as any,
        "",
        { values: { mode: "basic" } }
      );

      expect(field<HTMLInputElement>().required).toBe(false);
      expect(container.querySelector(".required-form-field")).toBeNull();
    });
  });

  describe("disabled", () => {
    test("disables an element whose scope does not match the form scope", () => {
      renderElement({ $type: "text", $name: "Field", $scope: "group" }, "", { props: { scope: "system" } });

      expect(field<HTMLInputElement>().disabled).toBe(true);
    });

    test("keeps system scoped elements editable regardless of the form scope", () => {
      renderElement({ $type: "text", $name: "Field", $scope: "system" }, "", { props: { scope: "group" } });

      expect(field<HTMLInputElement>().disabled).toBe(false);
    });

    test("disables the element when $disabled evaluates to true", () => {
      renderElement({ $type: "text", $name: "Field", $scope: "system", $disabled: "formValues.locked" } as any, "", {
        values: { locked: true },
      });

      expect(field<HTMLInputElement>().disabled).toBe(true);
    });

    test("inherits the disabled flag from the parent when the element has no $disabled", () => {
      renderElement({ $type: "text", $name: "Field", $scope: "system" }, "", { disabled: true });

      expect(field<HTMLInputElement>().disabled).toBe(true);
    });

    test("lets an explicit $disabled of false override an inherited disabled flag", () => {
      renderElement({ $type: "text", $name: "Field", $scope: "system", $disabled: false } as any, "", {
        disabled: true,
      });

      expect(field<HTMLInputElement>().disabled).toBe(false);
    });
  });

  describe("wrapping", () => {
    test("renders a label and a help icon by default", () => {
      const { container } = renderElement(
        { $type: "text", $name: "Field", $scope: "system", $help: "Some guidance" },
        ""
      );

      expect(container.querySelector("label")?.textContent).toBe("Field:");
      expect(container.querySelector(".spacewalk-help-link")?.getAttribute("title")).toBe("Some guidance");
    });

    test("omits the help icon when the help repeats the element name", () => {
      const { container } = renderElement({ $type: "text", $name: "Field", $scope: "system", $help: "Field" }, "");

      expect(container.querySelector(".spacewalk-help-link")).toBeNull();
    });

    test("uses a caller supplied wrapper instead of the default one", () => {
      const wrapper = jest.fn((name, required, element) => <div data-testid="custom-wrapper">{element}</div>);

      renderElement(
        { $type: "text", $name: "Field", $scope: "system", $help: "Some guidance", $required: true } as any,
        "abc",
        { wrapper }
      );

      expect(screen.getByTestId("custom-wrapper")).toBeTruthy();
      expect(wrapper).toHaveBeenCalledWith("Field", true, expect.anything(), "Some guidance");
    });
  });
});

/**
 * Capture the context the provider exposes so its logic can be asserted on directly.
 * `systemData` and `groupData` always default to `{}` because the provider dereferences
 * both of them unconditionally while generating the initial values.
 */
const renderProvider = (props: any = {}) => {
  let context: any;
  const utils = render(
    <FormulaFormContextProvider
      scope="system"
      systemData={{}}
      groupData={{}}
      sectionsExpanded={SectionState.Expanded}
      setSectionsExpanded={jest.fn()}
      searchCriteria=""
      {...props}
    >
      <FormulaFormContext.Consumer>
        {(value) => {
          context = value;
          return null;
        }}
      </FormulaFormContext.Consumer>
    </FormulaFormContextProvider>
  );
  return { ...utils, getContext: () => context };
};

describe("FormulaFormContextProvider", () => {
  describe("layout preprocessing", () => {
    test("defaults missing $type to text and derives a humanised $name from the key", () => {
      const { getContext } = renderProvider({ layout: { server_name: {} } });

      expect(getContext().layout.server_name).toMatchObject({
        $type: "text",
        $id: "server_name",
        $name: "Server Name",
      });
    });

    test("defaults $help to the element name", () => {
      const { getContext } = renderProvider({ layout: { host: { $name: "Host" } } });

      expect(getContext().layout.host.$help).toBe("Host");
    });

    test("defaults $placeholder to an empty string", () => {
      const { getContext } = renderProvider({ layout: { host: {} } });

      expect(getContext().layout.host.$placeholder).toBe("");
    });

    test("expands ${productName} in the name, help and placeholder", () => {
      const { getContext } = renderProvider({
        layout: { host: { $name: "${productName} host", $help: "On ${productName}", $placeholder: "${productName}" } },
      });

      expect(getContext().layout.host).toMatchObject({
        $name: "Uyuni host",
        $help: "On Uyuni",
        $placeholder: "Uyuni",
      });
    });

    test("rewrites hidden-group into a namespace", () => {
      const { getContext } = renderProvider({ layout: { secrets: { $type: "hidden-group", inner: {} } } });

      expect(getContext().layout.secrets.$type).toBe("namespace");
    });

    test("propagates the group scope down to the children that do not set one", () => {
      const { getContext } = renderProvider({
        layout: { net: { $type: "group", $scope: "group", host: {}, port: { $scope: "system" } } },
      });

      expect(getContext().layout.net.host.$scope).toBe("group");
      expect(getContext().layout.net.port.$scope).toBe("system");
    });

    test("names the $key of an edit-group prototype and makes it required", () => {
      const { getContext } = renderProvider({
        layout: { mounts: { $type: "edit-group", $prototype: { $type: "group", $key: {}, size: {} } } },
      });

      expect(getContext().layout.mounts.$prototype.$key).toMatchObject({ $name: "Key", $required: true });
    });

    test("defaults the edit-group $itemName", () => {
      const { getContext } = renderProvider({
        layout: { mounts: { $type: "edit-group", $prototype: { $type: "group", path: {} } } },
      });

      expect(getContext().layout.mounts.$itemName).toBe("Item ${i}");
    });

    test("derives the new item template of a list of dictionaries from the prototype", () => {
      const { getContext } = renderProvider({
        layout: {
          mounts: {
            $type: "edit-group",
            $prototype: { $type: "group", path: { $default: "/srv" }, active: { $type: "boolean" } },
          },
        },
      });

      expect(getContext().layout.mounts.$newItemValue).toMatchObject({ path: "/srv", active: false });
    });

    test("derives the new item template of a primitive list from the prototype default", () => {
      const { getContext } = renderProvider({
        layout: { ports: { $type: "edit-group", $prototype: { $type: "number", $default: 80 } } },
      });

      expect(getContext().layout.ports.$newItemValue).toBe(80);
    });

    test("derives the new item template of a primitive dictionary as a key value pair", () => {
      const { getContext } = renderProvider({
        layout: {
          labels: {
            $type: "edit-group",
            $prototype: { $type: "text", $default: "v", $key: { $type: "text", $default: "k" } },
          },
        },
      });

      expect(getContext().layout.labels.$newItemValue).toEqual(["k", "v"]);
    });

    test("pads the edit-group default up to $minItems", () => {
      const { getContext } = renderProvider({
        layout: { ports: { $type: "edit-group", $minItems: 3, $prototype: { $type: "text" } } },
      });

      expect(getContext().layout.ports.$default).toEqual(["", "", ""]);
    });
  });

  describe("default values", () => {
    test.each([
      ["boolean", {}, false],
      ["color", {}, "#000000"],
      ["password", {}, ""],
      ["text", {}, ""],
      ["number", {}, ""],
      ["select", { $values: ["first", "second"] }, "first"],
    ])("defaults a %s element to %p", (type, extra, expected) => {
      const { getContext } = renderProvider({ layout: { field: { $type: type, ...(extra as object) } } });

      expect(getContext().values.field).toEqual(expected);
    });

    test("prefers an explicit $default over the type default", () => {
      const { getContext } = renderProvider({ layout: { field: { $type: "boolean", $default: true } } });

      expect(getContext().values.field).toBe(true);
    });
  });

  describe("value resolution", () => {
    test("prefers system data over group data for system scoped elements", () => {
      const { getContext } = renderProvider({
        layout: { host: { $scope: "system", $default: "default" } },
        groupData: { host: "from-group" },
        systemData: { host: "from-system" },
      });

      expect(getContext().values.host).toBe("from-system");
    });

    test("falls back to group data for system scoped elements", () => {
      const { getContext } = renderProvider({
        layout: { host: { $scope: "system", $default: "default" } },
        groupData: { host: "from-group" },
      });

      expect(getContext().values.host).toBe("from-group");
    });

    test("falls back to the layout default when there is no stored data at all", () => {
      const { getContext } = renderProvider({ layout: { host: { $scope: "system", $default: "default" } } });

      expect(getContext().values.host).toBe("default");
    });

    test("ignores system data for group scoped elements", () => {
      const { getContext } = renderProvider({
        layout: { host: { $scope: "group", $default: "default" } },
        groupData: { host: "from-group" },
        systemData: { host: "from-system" },
      });

      expect(getContext().values.host).toBe("from-group");
    });

    test("ignores both data sources for readonly elements", () => {
      const { getContext } = renderProvider({
        layout: { host: { $scope: "readonly", $default: "default" } },
        groupData: { host: "from-group" },
        systemData: { host: "from-system" },
      });

      expect(getContext().values.host).toBe("default");
    });

    test("builds nested values for groups", () => {
      const { getContext } = renderProvider({
        layout: { net: { $type: "group", host: {}, port: { $type: "number" } } },
        systemData: { net: { host: "localhost" } },
      });

      expect(getContext().values.net).toMatchObject({ host: "localhost", port: "" });
    });

    test("records validation metadata alongside every value", () => {
      const { getContext } = renderProvider({
        layout: { net: { $type: "group", host: { $name: "Host", $required: true, $match: "[a-z]+" } } },
      });

      expect(getContext().values.net.$meta$host).toMatchObject({
        id: "net#host",
        name: "Host",
        required: true,
        match: "[a-z]+",
      });
    });

    test("falls back to an empty array when the stored edit-group data is not a list", () => {
      const { getContext } = renderProvider({
        layout: { ports: { $type: "edit-group", $prototype: { $type: "text" } } },
        systemData: { ports: "not-a-list" },
      });

      expect(getContext().values.ports).toEqual([]);
    });
  });

  describe("data preprocessing", () => {
    test("turns a stored primitive dictionary into a list of pairs", () => {
      const { getContext } = renderProvider({
        layout: { labels: { $type: "edit-group", $prototype: { $type: "text", $key: { $type: "text" } } } },
        systemData: { labels: { env: "prod", tier: "web" } },
      });

      expect(getContext().values.labels).toEqual([
        ["env", "prod"],
        ["tier", "web"],
      ]);
    });

    test("turns a stored dictionary of dictionaries into a list carrying the $key", () => {
      const { getContext } = renderProvider({
        layout: { mounts: { $type: "edit-group", $prototype: { $type: "group", $key: {}, size: {} } } },
        systemData: { mounts: { "/srv": { size: "10G" } } },
      });

      expect(getContext().values.mounts).toHaveLength(1);
      expect(getContext().values.mounts[0]).toMatchObject({ $key: "/srv", size: "10G" });
    });
  });

  describe("onChange", () => {
    test("writes a top level value", () => {
      const { getContext } = renderProvider({ layout: { host: {} } });

      act(() => getContext().onChange("host", "updated"));

      expect(getContext().values.host).toBe("updated");
    });

    test("writes a nested value addressed by a hash separated path", () => {
      const { getContext } = renderProvider({ layout: { net: { $type: "group", host: {} } } });

      act(() => getContext().onChange("net#host", "updated"));

      expect(getContext().values.net.host).toBe("updated");
    });
  });

  describe("clearValues", () => {
    test("resets the values back to the layout defaults when confirmed", () => {
      const { getContext } = renderProvider({ layout: { host: { $default: "default" } } });
      act(() => getContext().onChange("host", "edited"));

      act(() => getContext().clearValues(() => true));

      expect(getContext().values.host).toBe("default");
    });

    test("keeps the group data when clearing a system scoped form", () => {
      const { getContext } = renderProvider({
        scope: "system",
        layout: { host: { $default: "default" } },
        groupData: { host: "from-group" },
      });
      act(() => getContext().onChange("host", "edited"));

      act(() => getContext().clearValues(() => true));

      expect(getContext().values.host).toBe("from-group");
    });

    test("discards the group data when clearing a group scoped form", () => {
      const { getContext } = renderProvider({
        scope: "group",
        layout: { host: { $default: "default" } },
        groupData: { host: "from-group" },
      });

      act(() => getContext().clearValues(() => true));

      expect(getContext().values.host).toBe("default");
    });

    test("leaves the values untouched when the confirmation is declined", () => {
      const { getContext } = renderProvider({ layout: { host: { $default: "default" } } });
      act(() => getContext().onChange("host", "edited"));

      act(() => getContext().clearValues(() => false));

      expect(getContext().values.host).toBe("edited");
    });
  });

  describe("validate", () => {
    test("reports no errors for a valid form", () => {
      const { getContext } = renderProvider({ layout: { host: { $name: "Host", $default: "localhost" } } });

      expect(getContext().validate().errors).toBeUndefined();
    });

    test("strips the validation metadata out of the returned values", () => {
      const { getContext } = renderProvider({ layout: { host: { $default: "localhost" } } });

      expect(getContext().validate().values).toEqual({ host: "localhost" });
    });

    test("omits group scoped elements from the returned values", () => {
      const { getContext } = renderProvider({
        layout: { host: { $default: "localhost" }, owner: { $scope: "group", $default: "team" } },
      });

      expect(getContext().validate().values).toEqual({ host: "localhost" });
    });

    test("reports a required element that has no value", () => {
      const { getContext } = renderProvider({ layout: { host: { $name: "Host", $required: true } } });

      expect(getContext().validate().errors).toMatchObject({ required: ["Host"] });
    });

    test("accepts a required element once it has a value", () => {
      const { getContext } = renderProvider({ layout: { host: { $name: "Host", $required: true } } });

      act(() => getContext().onChange("host", "localhost"));

      expect(getContext().validate().errors).toBeUndefined();
    });

    test("skips the required check for elements hidden by $visible", () => {
      const { getContext } = renderProvider({
        layout: {
          mode: { $default: "basic" },
          host: { $name: "Host", $required: true, $visible: "formValues.mode == 'advanced'" },
        },
      });

      expect(getContext().validate().errors).toBeUndefined();
    });

    test("runs the required check for elements that $visible keeps on screen", () => {
      const { getContext } = renderProvider({
        layout: {
          mode: { $default: "advanced" },
          host: { $name: "Host", $required: true, $visible: "formValues.mode == 'advanced'" },
        },
      });

      expect(getContext().validate().errors).toMatchObject({ required: ["Host"] });
    });

    test("skips the required check for elements hidden by the deprecated $visibleIf", () => {
      const { getContext } = renderProvider({
        layout: {
          mode: { $default: "basic" },
          host: { $name: "Host", $required: true, $visibleIf: "mode == 'advanced'" },
        },
      });

      expect(getContext().validate().errors).toBeUndefined();
    });

    test("reports a value that does not match $match", () => {
      const { getContext } = renderProvider({
        layout: { host: { $name: "Host", $match: "[a-z]+", $default: "NOT-LOWERCASE" } },
      });

      expect(getContext().validate().errors).toMatchObject({ invalid: ["Host"] });
    });

    test("anchors $match so a partial match is still reported", () => {
      const { getContext } = renderProvider({
        layout: { host: { $name: "Host", $match: "[a-z]+", $default: "abc123" } },
      });

      expect(getContext().validate().errors).toMatchObject({ invalid: ["Host"] });
    });

    test("accepts a value matching an already anchored $match", () => {
      const { getContext } = renderProvider({
        layout: { host: { $name: "Host", $match: "^[a-z]+$", $default: "abc" } },
      });

      expect(getContext().validate().errors).toBeUndefined();
    });

    test("logs and tolerates an unparsable $match", () => {
      const errorSpy = jest.spyOn(Loggerhead, "error").mockImplementation(() => {});

      const { getContext } = renderProvider({ layout: { host: { $name: "Host", $match: "[", $default: "abc" } } });

      expect(getContext().validate().errors).toBeUndefined();
      expect(errorSpy).toHaveBeenCalled();

      errorSpy.mockRestore();
    });

    test("reports an element failing both checks once per category", () => {
      const { getContext } = renderProvider({
        layout: { host: { $name: "Host", $required: true, $match: "[a-z]+" } },
      });

      const errors = getContext().validate().errors;
      expect(errors.required).toEqual(["Host"]);
      expect(errors.invalid).toEqual(["Host"]);
    });

    test("substitutes $ifEmpty for an empty value", () => {
      const { getContext } = renderProvider({ layout: { host: { $ifEmpty: "fallback" } } });

      expect(getContext().validate().values).toEqual({ host: "fallback" });
    });

    test("leaves an optional empty value alone instead of applying $ifEmpty", () => {
      const { getContext } = renderProvider({ layout: { host: { $ifEmpty: "fallback", $optional: true } } });

      expect(getContext().validate().values).toEqual({ host: "" });
    });

    test("converts a primitive dictionary edit-group back into an object", () => {
      const { getContext } = renderProvider({
        layout: { labels: { $type: "edit-group", $prototype: { $type: "text", $key: { $type: "text" } } } },
        systemData: { labels: { env: "prod" } },
      });

      expect(getContext().validate().values).toEqual({ labels: { env: "prod" } });
    });

    test("drops primitive dictionary entries that have no key", () => {
      const { getContext } = renderProvider({
        layout: { labels: { $type: "edit-group", $prototype: { $type: "text", $key: { $type: "text" } } } },
        systemData: { labels: { env: "prod" } },
      });

      act(() =>
        getContext().onChange("labels", [
          ["env", "prod"],
          ["", "orphan"],
        ])
      );

      expect(getContext().validate().values).toEqual({ labels: { env: "prod" } });
    });

    test("converts a dictionary of dictionaries edit-group back into a keyed object", () => {
      const { getContext } = renderProvider({
        layout: { mounts: { $type: "edit-group", $prototype: { $type: "group", $key: {}, size: {} } } },
        systemData: { mounts: { "/srv": { size: "10G" } } },
      });

      expect(getContext().validate().values).toEqual({ mounts: { "/srv": { size: "10G" } } });
    });

    test("drops empty entries of a primitive list edit-group", () => {
      const { getContext } = renderProvider({
        layout: { ports: { $type: "edit-group", $prototype: { $type: "text" } } },
        systemData: { ports: ["80", "", "443"] },
      });

      expect(getContext().validate().values).toEqual({ ports: ["80", "443"] });
    });

    test("omits an edit-group that ends up empty", () => {
      const { getContext } = renderProvider({
        layout: { ports: { $type: "edit-group", $prototype: { $type: "text" } } },
        systemData: { ports: [""] },
      });

      expect(getContext().validate().values).toEqual({});
    });

    test("omits a group that ends up empty", () => {
      const { getContext } = renderProvider({ layout: { net: { $type: "group", $scope: "group", host: {} } } });

      expect(getContext().validate().values).toEqual({});
    });
  });
});

describe("FormulaFormRenderer", () => {
  const renderForm = (props: any = {}) =>
    render(
      <FormulaFormContextProvider
        scope="system"
        systemData={{}}
        groupData={{}}
        sectionsExpanded={SectionState.Expanded}
        setSectionsExpanded={jest.fn()}
        searchCriteria=""
        {...props}
      >
        <FormulaFormRenderer />
      </FormulaFormContextProvider>
    );

  test("renders one field per layout entry inside a form", () => {
    const { container } = renderForm({
      layout: { host: { $name: "Host", $default: "localhost" }, port: { $type: "number", $default: 80 } },
    });

    expect(container.querySelector("form#formula-form")).toBeTruthy();
    expect(field<HTMLInputElement>("host").value).toBe("localhost");
    expect(field<HTMLInputElement>("port").value).toBe("80");
  });

  test("renders nothing when there are no values", () => {
    const { container } = render(
      <FormulaFormContext.Provider
        value={
          {
            scope: "system",
            layout: {},
            values: null,
            sectionsExpanded: SectionState.Expanded,
            setSectionsExpanded: jest.fn(),
            searchCriteria: "",
          } as any
        }
      >
        <FormulaFormRenderer />
      </FormulaFormContext.Provider>
    );

    expect(container.innerHTML).toBe("");
  });

  test("stores the typed text of a text field", () => {
    renderForm({ layout: { host: { $name: "Host" } } });

    fireEvent.change(field("host"), { target: { value: "example.com" } });

    expect(field<HTMLInputElement>("host").value).toBe("example.com");
  });

  test("stores the checked state rather than the value of a checkbox", async () => {
    renderForm({ layout: { enabled: { $type: "boolean", $name: "Enabled" } } });

    expect(field<HTMLInputElement>("enabled").checked).toBe(false);

    await click(field("enabled"));

    expect(field<HTMLInputElement>("enabled").checked).toBe(true);
  });

  test("stores a numeric value for a number field", () => {
    renderForm({ layout: { port: { $type: "number" } } });

    fireEvent.change(field("port"), { target: { value: "8080" } });

    expect(field<HTMLInputElement>("port").value).toBe("8080");
  });

  test("stores an empty string when a number field is cleared", () => {
    renderForm({ layout: { port: { $type: "number", $default: 80 } } });

    fireEvent.change(field("port"), { target: { value: "" } });

    expect(field<HTMLInputElement>("port").value).toBe("");
  });

  test("registers a validation trigger the provider can fire", () => {
    let context: any;
    render(
      <FormulaFormContextProvider
        scope="system"
        layout={{ host: { $name: "Host" } }}
        systemData={{}}
        groupData={{}}
        sectionsExpanded={SectionState.Expanded}
        setSectionsExpanded={jest.fn()}
        searchCriteria=""
      >
        <FormulaFormRenderer />
        <FormulaFormContext.Consumer>
          {(value) => {
            context = value;
            return null;
          }}
        </FormulaFormContext.Consumer>
      </FormulaFormContextProvider>
    );

    // Validating submits the form through the registered trigger, which must stay a no-op
    let result: any;
    act(() => {
      result = context.validate();
    });

    expect(result.values).toEqual({ host: "" });
  });
});
