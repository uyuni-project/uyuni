import { useRef, useState } from "react";

import { Form } from "components/input";
import { LargeTextInput, LargeTextInputRef } from "components/large-text-input";

import { click, render, screen, type, upload, waitFor } from "utils/test-utils";

type LargeTextInputTestFormProps = {
  required?: boolean;
  onContent: (content: string | undefined) => void;
  onValidate?: (valid: boolean) => void;
};

const LargeTextInputTestForm = ({ required = false, onContent, onValidate }: LargeTextInputTestFormProps) => {
  const [model, setModel] = useState({});
  const inputRef = useRef<LargeTextInputRef>(null);

  return (
    <Form model={model} onChange={(updatedModel) => setModel({ ...updatedModel })} onValidate={onValidate}>
      <LargeTextInput
        ref={inputRef}
        name="certificate"
        required={required}
        label="Root CA certificate"
        uploadLabel="Certificate File"
        pasteLabel="PEM certificate"
      />
      <button type="button" onClick={() => inputRef.current?.getContent().then(onContent)}>
        Read
      </button>
      <button type="button" onClick={() => setModel({})}>
        Reset
      </button>
    </Form>
  );
};

describe("LargeTextInput", () => {
  test("returns no content when optional input is not needed", async () => {
    const onContent = jest.fn();

    render(<LargeTextInputTestForm onContent={onContent} />);

    expect((screen.getByLabelText("Not needed") as HTMLInputElement).checked).toBe(true);
    expect(screen.queryByLabelText("Certificate File")).toBeNull();

    await click(screen.getByRole("button", { name: "Read" }));

    await waitFor(() => expect(onContent).toHaveBeenCalledWith(undefined));
  });

  test("required input starts in upload mode", () => {
    render(<LargeTextInputTestForm required onContent={jest.fn()} />);

    expect(screen.queryByLabelText("Not needed")).toBeNull();
    expect((screen.getByLabelText("Upload a file") as HTMLInputElement).checked).toBe(true);
    expect(screen.getByLabelText("Certificate File")).not.toBeNull();
  });

  test("reports required upload input as invalid until a file is selected", async () => {
    const onValidate = jest.fn();

    render(<LargeTextInputTestForm required onContent={jest.fn()} onValidate={onValidate} />);

    await waitFor(() => expect(onValidate).toHaveBeenLastCalledWith(false));

    await upload(screen.getByLabelText("Certificate File"), new File(["certificate content"], "certificate.pem"));

    await waitFor(() => expect(onValidate).toHaveBeenLastCalledWith(true));
  });

  test("reports required pasted input as invalid until content is entered", async () => {
    const onValidate = jest.fn();

    render(<LargeTextInputTestForm required onContent={jest.fn()} onValidate={onValidate} />);

    await click(screen.getByLabelText("Paste the data"));

    await waitFor(() => expect(onValidate).toHaveBeenLastCalledWith(false));

    await type(screen.getByLabelText("PEM certificate"), "certificate content");

    await waitFor(() => expect(onValidate).toHaveBeenLastCalledWith(true));
  });

  test("returns pasted content", async () => {
    const onContent = jest.fn();

    render(<LargeTextInputTestForm required onContent={onContent} />);

    await click(screen.getByLabelText("Paste the data"));
    await type(
      screen.getByLabelText("PEM certificate"),
      "-----BEGIN CERTIFICATE-----\ncontent\n-----END CERTIFICATE-----"
    );
    await click(screen.getByRole("button", { name: "Read" }));

    await waitFor(() =>
      expect(onContent).toHaveBeenCalledWith("-----BEGIN CERTIFICATE-----\ncontent\n-----END CERTIFICATE-----")
    );
  });

  test("returns uploaded file content", async () => {
    const onContent = jest.fn();

    render(<LargeTextInputTestForm required onContent={onContent} />);

    await upload(screen.getByLabelText("Certificate File"), new File(["certificate content"], "certificate.pem"));
    await click(screen.getByRole("button", { name: "Read" }));

    await waitFor(() => expect(onContent).toHaveBeenCalledWith("certificate content"));
  });

  test("restores the default input mode when the form model is reset", async () => {
    render(<LargeTextInputTestForm required onContent={jest.fn()} />);

    await click(screen.getByLabelText("Paste the data"));
    await type(screen.getByLabelText("PEM certificate"), "certificate content");

    expect((screen.getByLabelText("Paste the data") as HTMLInputElement).checked).toBe(true);

    await click(screen.getByRole("button", { name: "Reset" }));

    await waitFor(() => expect((screen.getByLabelText("Upload a file") as HTMLInputElement).checked).toBe(true));
    expect(screen.getByLabelText("Certificate File")).not.toBeNull();
  });
});
