import { useState } from "react";

import { SubmitButton } from "components/buttons";
import { Form, Text } from "components/input";

const timeout = (ms = 0) => new Promise<void>((resolve) => window.setTimeout(() => resolve(), ms));

export default () => {
  const [model, setModel] = useState({ foo: "Foo" });
  const [isValid, setIsValid] = useState(true);

  const asyncValidator = async (value: string) => {
    await timeout(300);

    if (!value.toLowerCase().includes("o")) {
      return "Must include the letter 'o'";
    }
  };

  const onValidate = (newIsValid: boolean) => {
    console.log(newIsValid);
    setIsValid(newIsValid);
  };

  return (
    <Form model={model} onChange={setModel} onValidate={onValidate}>
      <Text name="foo" required validate={asyncValidator} debounceValidate={500} />
      <SubmitButton id="submit-btn" className="btn-success" text="Submit" disabled={!isValid} />
    </Form>
  );
};
