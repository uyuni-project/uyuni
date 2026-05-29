import { forwardRef, useContext, useEffect, useImperativeHandle, useMemo } from "react";

import { FormContext, Radio, Text, TextArea } from "components/input";

interface Props {
  /** The name of the input component */
  name: string;

  /** Indicates whether providing the input is mandatory or not */
  required?: boolean;

  /** The label for the input component */
  label: string;
  /** An optional hint for the input component */
  hint?: string;

  /** Optional custom label for the "Not Needed" option */
  notNeededOptionLabel?: string;
  /** Optional custom label for the "Upload" option */
  uploadOptionLabel?: string;
  /** Optional custom label for the "Paste" option */
  pasteOptionLabel?: string;

  /** The label for the upload field */
  uploadLabel: string;
  /** An optional hint for the upload field */
  uploadHint?: string;

  /** The label for the paste textarea */
  pasteLabel: string;
  /** An optional hint for the paste textarea */
  pasteHint?: string;
  /** An optional placeholder for the paste textarea */
  pastePlaceholder?: string;
}

export interface LargeTextInputRef {
  /**
   * Returns the content of the input depending on the selected input mode.
   * @returns A promise that resolves to the content of the input, either from the uploaded file or the pasted data,
   * or undefined if nothing was selected.
   */
  getContent(): Promise<string | undefined>;
}

export enum LargeTextInputMode {
  NotNeeded = "notNeeded",
  Upload = "upload",
  Paste = "paste",
}

export const LargeTextInput = forwardRef<LargeTextInputRef, Props>(
  (
    {
      name,
      required = false,
      label,
      hint,
      notNeededOptionLabel = t("Not needed"),
      uploadOptionLabel = t("Upload a file"),
      pasteOptionLabel = t("Paste the data"),
      uploadLabel,
      uploadHint,
      pasteLabel,
      pasteHint,
      pastePlaceholder,
    },
    ref
  ): JSX.Element => {
    // Define the available options for the input mode based on whether the input is mandatory or not
    const availableModes = useMemo(() => {
      const availableOptions = [
        { label: uploadOptionLabel, value: LargeTextInputMode.Upload },
        { label: pasteOptionLabel, value: LargeTextInputMode.Paste },
      ];

      if (!required) {
        availableOptions.unshift({ label: notNeededOptionLabel, value: LargeTextInputMode.NotNeeded });
      }

      return availableOptions;
    }, [required, notNeededOptionLabel, uploadOptionLabel, pasteOptionLabel]);

    const initialMode = required ? LargeTextInputMode.Upload : LargeTextInputMode.NotNeeded;

    // Retrieve the form context to store the state
    const { model, setModelValue } = useContext(FormContext);
    const inputMode = model?.[`${name}_inputMode`] ?? initialMode;

    // Initialize the input mode
    useEffect(() => {
      // If a valid mode is already set, do not override it
      if (Object.values(LargeTextInputMode).includes(model?.[`${name}_inputMode`] as LargeTextInputMode)) {
        return;
      }

      // Set to "Not Needed" if the input is not required, "Upload" otherwise
      const initialMode = required ? LargeTextInputMode.Upload : LargeTextInputMode.NotNeeded;
      setModelValue?.(`${name}_inputMode`, initialMode);
    }, [name, required, setModelValue]);

    // Expose the getContent method to allow retrieving the content of the input
    useImperativeHandle(ref, () => ({
      getContent(): Promise<string | undefined> {
        const inputMode = model?.[`${name}_inputMode`];
        switch (inputMode) {
          case LargeTextInputMode.Upload:
            return new Promise((resolve, reject) => {
              const uploadField = document.getElementById(`${name}_uploadedFile`) as HTMLInputElement | null;
              const uploadedFile = uploadField?.files?.[0];
              if (uploadedFile) {
                const reader = new FileReader();
                reader.onload = () => resolve(reader.result as string);
                reader.onerror = () => reject(reader.error);
                reader.readAsText(uploadedFile);
              } else {
                reject(t("Unable to retrieve the uploaded file"));
              }
            });

          case LargeTextInputMode.Paste:
            return Promise.resolve(model?.[`${name}_pastedData`]);
        }

        return Promise.resolve(undefined);
      },
    }));

    return (
      <>
        <Radio
          name={`${name}_inputMode`}
          label={label}
          title={label}
          hint={hint}
          inline={true}
          required
          labelClass="col-md-3"
          divClass="col-md-6"
          items={availableModes}
        />
        {inputMode === LargeTextInputMode.Upload && (
          <Text
            name={`${name}_uploadedFile`}
            label={uploadLabel}
            hint={uploadHint}
            required
            type="file"
            labelClass="col-md-3"
            divClass="col-md-6"
          />
        )}
        {inputMode === LargeTextInputMode.Paste && (
          <TextArea
            name={`${name}_pastedData`}
            label={pasteLabel}
            hint={pasteHint}
            required
            rows={15}
            labelClass="col-md-3"
            divClass="col-md-6"
            placeholder={pastePlaceholder}
          />
        )}
      </>
    );
  }
);
