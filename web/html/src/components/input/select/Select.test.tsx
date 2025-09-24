import * as React from "react";
import { useState } from "react";

import { SubmitButton } from "components/buttons";
import { Field, Form } from "components/formik";

import { click, render, screen, timeout } from "utils/test-utils";

import { Select } from "./Select";

describe("Select", () => {
  test("renders with minimal props", async () => {
    expect(() => {
      render(
        <Select
          label="Level"
          options={[
            { value: "beginner", label: "Beginner label" },
            { value: "normal", label: "Normal label" },
            { value: "expert", label: "Expert label" },
          ]}
        />
      );
    }).not.toThrow();

    await click(screen.getByLabelText("Level"));
    expect(screen.getByText("Expert label")).toBeDefined();
  });

  test("initial value is kept", () => {
    const Setup = () => {
      const [value, setValue] = useState("expert");
      return (
        <Select
          label="Level"
          value={value}
          onChange={(newValue) => setValue(newValue)}
          options={[
            { value: "beginner", label: "Beginner label" },
            { value: "normal", label: "Normal label" },
            { value: "expert", label: "Expert label" },
          ]}
        />
      );
    };
    render(<Setup />);

    expect(screen.getByText("Expert label")).toBeDefined();
  });

  test("custom value field is used", async () => {
    const onSetValue = jest.fn();

    const Setup = () => {
      const [value, _setValue] = useState<string | undefined>();
      const setValue = (newValue: typeof value) => {
        _setValue(newValue);
        onSetValue(newValue);
      };

      return (
        <Select
          label="Level"
          value={value}
          onChange={(newValue) => setValue(newValue)}
          options={[
            { customValue: "beginner", label: "Beginner label" },
            { customValue: "normal", label: "Normal label" },
            { customValue: "expert", label: "Expert label" },
          ]}
          getOptionValue={(option) => option.customValue}
          isClearable
        />
      );
    };
    render(<Setup />);

    await click(screen.getByLabelText("Level"));
    await click(screen.getByText("Expert label"));
    expect(onSetValue).toBeCalledWith("expert");
  });

  test("custom value field can be cleared", async () => {
    const onSetValue = jest.fn();

    const Setup = () => {
      const [value, _setValue] = useState<string | undefined>("normal");
      const setValue = (newValue: typeof value) => {
        _setValue(newValue);
        onSetValue(newValue);
      };

      return (
        <Select
          label="Level"
          value={value}
          onChange={(newValue) => setValue(newValue)}
          options={[
            { customValue: "beginner", label: "Beginner label" },
            { customValue: "normal", label: "Normal label" },
            { customValue: "expert", label: "Expert label" },
          ]}
          getOptionValue={(option) => option.customValue}
          isClearable
        />
      );
    };
    render(<Setup />);

    await click(screen.getByLabelText("Clear"));
    expect(onSetValue).toBeCalledWith(undefined);
  });

  test("custom label field is used", async () => {
    const Setup = () => {
      return (
        <Select
          label="Level"
          options={[
            { value: "beginner", customLabel: "Beginner label" },
            { value: "normal", customLabel: "Normal label" },
            { value: "expert", customLabel: "Expert label" },
          ]}
          getOptionLabel={(option) => option.customLabel}
        />
      );
    };
    render(<Setup />);

    await click(screen.getByLabelText("Level"));
    expect(screen.getByText("Beginner label")).toBeDefined();
    expect(screen.getByText("Normal label")).toBeDefined();
    expect(screen.getByText("Expert label")).toBeDefined();
  });

  test("async values are fetched", async () => {
    const loadOptions = () => {
      return new Promise((resolve) => {
        window.setTimeout(() => {
          resolve([
            { value: "beginner", label: "Beginner label" },
            { value: "normal", label: "Normal label" },
            { value: "expert", label: "Expert label" },
          ]);
        }, 200);
      });
    };

    const Setup = () => {
      return <Select label="Level" loadOptions={loadOptions} />;
    };
    render(<Setup />);

    await timeout(400);
    await click(screen.getByLabelText("Level"));
    expect(screen.getByText("Beginner label")).toBeDefined();
    expect(screen.getByText("Normal label")).toBeDefined();
    expect(screen.getByText("Expert label")).toBeDefined();
  });

  test("default value option is used", async () => {
    const onLoadOptions = jest.fn();
    let resolveLoadOptions;
    const loadOptions = () => {
      onLoadOptions();
      return new Promise((r) => (resolveLoadOptions = r));
    };

    const Setup = () => {
      const [value, setValue] = useState("expert");
      return (
        <Select
          label="Level"
          value={value}
          onChange={(newValue) => setValue(newValue)}
          defaultValueOption={{ value: "expert", label: "Expert label" }}
          loadOptions={loadOptions}
        />
      );
    };
    render(<Setup />);

    expect(screen.getByText("Expert label")).toBeDefined();
    resolveLoadOptions([]);
  });

  test("data-testid is present in the DOM", () => {
    render(<Select data-testid="level-testid" />);
    expect(screen.getByTestId("level-testid")).toBeDefined();
  });

  test("single values can be picked", async () => {
    const onSetValue = jest.fn();
    const Setup = () => {
      const [value, _setValue] = useState<string | undefined>();
      const setValue = (newValue: typeof value) => {
        _setValue(newValue);
        onSetValue(newValue);
      };

      return (
        <Select
          label="Level"
          value={value}
          onChange={(newValue) => setValue(newValue)}
          options={[
            { value: "beginner", label: "Beginner label" },
            { value: "normal", label: "Normal label" },
            { value: "expert", label: "Expert label" },
          ]}
        />
      );
    };
    render(<Setup />);

    await click(screen.getByLabelText("Level"));
    await click(screen.getByText("Normal label"));
    expect(onSetValue).toBeCalledWith("normal");
  });

  test("single values can be cleared", async () => {
    const onSetValue = jest.fn();
    const Setup = () => {
      const [value, _setValue] = useState<string | undefined>("expert");
      const setValue = (newValue: typeof value) => {
        _setValue(newValue);
        onSetValue(newValue);
      };

      return (
        <Select
          label="Level"
          value={value}
          onChange={(newValue) => setValue(newValue)}
          options={[
            { value: "beginner", label: "Beginner label" },
            { value: "normal", label: "Normal label" },
            { value: "expert", label: "Expert label" },
          ]}
          isClearable
        />
      );
    };
    render(<Setup />);

    await click(screen.getByLabelText("Clear"));
    expect(onSetValue).toBeCalledWith(undefined);
  });

  test("multiple values can be picked", async () => {
    const onSetValue = jest.fn();
    const Setup = () => {
      const [value, _setValue] = useState<string[]>();
      const setValue = (newValue: typeof value) => {
        _setValue(newValue);
        onSetValue(newValue);
      };

      return (
        <Select
          label="Level"
          value={value}
          onChange={(newValue) => setValue(newValue)}
          options={[
            { value: "beginner", label: "Beginner label" },
            { value: "normal", label: "Normal label" },
            { value: "expert", label: "Expert label" },
          ]}
          isMulti
        />
      );
    };
    render(<Setup />);

    await click(screen.getByLabelText("Level"));
    await click(screen.getByText("Normal label"));
    expect(onSetValue).toBeCalledWith(["normal"]);

    await click(screen.getByLabelText("Level"));
    await click(screen.getByText("Expert label"));
    expect(onSetValue).toBeCalledWith(["normal", "expert"]);
  });

  test("multiple values can be cleared", async () => {
    const onSetValue = jest.fn();
    const Setup = () => {
      const [value, _setValue] = useState<string[]>(["normal", "expert"]);
      const setValue = (newValue: typeof value) => {
        _setValue(newValue);
        onSetValue(newValue);
      };

      return (
        <Select
          label="Level"
          value={value}
          onChange={(newValue) => setValue(newValue)}
          options={[
            { value: "beginner", label: "Beginner label" },
            { value: "normal", label: "Normal label" },
            { value: "expert", label: "Expert label" },
          ]}
          isMulti
          isClearable
        />
      );
    };
    render(<Setup />);

    await click(screen.getByLabelText("Clear"));
    expect(onSetValue).toBeCalledWith([]);
  });

  test("binds to Formik data", async () => {
    const onSubmit = jest.fn();

    const Setup = () => {
      const initialValues = {
        level: "expert",
      };
      return (
        <Form initialValues={initialValues} onSubmit={(newValues) => onSubmit(newValues)}>
          <Field
            name="level"
            label="Level"
            as={Field.Select}
            options={[
              { value: "beginner", label: "Beginner label" },
              { value: "normal", label: "Normal label" },
              { value: "expert", label: "Expert label" },
            ]}
          />
          <SubmitButton>Submit</SubmitButton>
        </Form>
      );
    };
    render(<Setup />);

    expect(screen.getByText("Expert label")).toBeDefined();
    await click(screen.getByLabelText("Level"));
    await click(screen.getByText("Normal label"));
    await click(screen.getByText("Submit"));
    expect(onSubmit).toBeCalledWith({ level: "normal" });
  });
});
