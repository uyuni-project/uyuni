import { beforeEach, describe, expect, jest, test } from "@jest/globals";

import { click, render, screen, selectOptions } from "utils/test-utils";

import { CoCoSettingsForm } from "./CoCoSettingsForm";
import { Settings } from "./Utils";

describe("CoCoSettingsForm", () => {
  const availableEnvironmentTypes = {
    KVM_AMD: "KVM on AMD",
    KVM_INTEL: "KVM on INTEL",
    KVM_IBM_Z: "KVM on IBM Z",
  };

  let initialData: Settings;
  let saveHandler: jest.MockedFunction<(data: Promise<Settings>) => void>;

  beforeEach(() => {
    initialData = {
      enabled: false,
      environmentType: "KVM_AMD",
      attestOnBoot: false,
      attestOnSchedule: false,
      inputData: {},
    };

    saveHandler = jest.fn();

    URL.createObjectURL = jest.fn(() => "blob:mock-url");
    URL.revokeObjectURL = jest.fn();
  });

  test("renders correctly with initial data", () => {
    // Tweak the initial data
    initialData.enabled = true;
    initialData.environmentType = "KVM_INTEL";

    render(
      <CoCoSettingsForm
        initialData={initialData}
        availableEnvironmentTypes={availableEnvironmentTypes}
        showOnScheduleOption={false}
        saveHandler={saveHandler}
      />
    );

    // Ensure the UI has built correctly
    const enabledToggler = screen.getByRole("button", { name: "Enable attestation" });
    expect(enabledToggler.querySelector("i.fa-toggle-on")).not.toBeNull();

    const environmentSelect = screen.getByLabelText("Environment Type") as HTMLSelectElement;
    expect(environmentSelect.value).toBe("KVM_INTEL");

    const onBootToggler = screen.getByRole("button", { name: "Perform attestation during the boot process" });
    expect(onBootToggler.querySelector("i.fa-toggle-off")).not.toBeNull();

    const onScheduleToggler = screen.queryByRole("button", { name: "Perform attestation on a schedule" });
    expect(onScheduleToggler).toBeNull();
  });

  test("calls the save handler with the correct settings object", async () => {
    render(
      <CoCoSettingsForm
        initialData={initialData}
        availableEnvironmentTypes={availableEnvironmentTypes}
        showOnScheduleOption={false}
        saveHandler={saveHandler}
      />
    );

    // Enable the attestation
    await click(screen.getByRole("button", { name: "Enable attestation" }));

    // Change the environment
    selectOptions(screen.getByLabelText("Environment Type"), "KVM on INTEL");

    // Set to perform attestation on boot
    await click(screen.getByRole("button", { name: "Perform attestation during the boot process" }));

    // Click on save
    await click(screen.getByText("Save"));

    expect(saveHandler).toHaveBeenCalledTimes(1);
    await expect(saveHandler.mock.lastCall?.[0]).resolves.toEqual({
      enabled: true,
      environmentType: "KVM_INTEL",
      attestOnBoot: true,
      attestOnSchedule: false,
      inputData: {},
    });
  });

  test("allows to reset to the initial data after making changes", async () => {
    render(
      <CoCoSettingsForm
        initialData={initialData}
        availableEnvironmentTypes={availableEnvironmentTypes}
        showOnScheduleOption={false}
        saveHandler={saveHandler}
      />
    );

    // Enable the attestation
    const enabledToggler = screen.getByRole("button", { name: "Enable attestation" });
    await click(enabledToggler);
    expect(enabledToggler.querySelector("i.fa-toggle-on")).not.toBeNull();

    // Change the environment
    const environmentSelect = screen.getByLabelText("Environment Type") as HTMLSelectElement;
    selectOptions(environmentSelect, "KVM on INTEL");
    expect(environmentSelect.value).toBe("KVM_INTEL");

    // Click on reset
    await click(screen.getByText("Reset Changes"));

    // Check if the fields have reset to the initial model
    expect(enabledToggler.querySelector("i.fa-toggle-on")).toBeNull();
    expect(environmentSelect.value).toBe("KVM_AMD");
  });

  test("renders additional fields to handle environment specific input data", async () => {
    render(
      <CoCoSettingsForm
        initialData={initialData}
        availableEnvironmentTypes={availableEnvironmentTypes}
        showOnScheduleOption={false}
        saveHandler={saveHandler}
      />
    );

    // Enable the attestation
    await click(screen.getByRole("button", { name: "Enable attestation" }));

    // Change the environment to IBM Z
    selectOptions(screen.getByLabelText("Environment Type"), "KVM on IBM Z");

    // Ensure the inputs for the additional data are present
    expect(screen.getByLabelText("Secure execution header")).not.toBeNull();

    expect(screen.getByRole("radio", { name: "Upload a file" })).not.toBeNull();
    expect(screen.getByRole("radio", { name: "Paste the data" })).not.toBeNull();
  });

  test("preserves additional input data when they are not changed", async () => {
    // Set the input data in the initial settings
    initialData.enabled = true;
    initialData.environmentType = "KVM_IBM_Z";
    initialData.inputData = {
      secure_execution_header: "ZHVtbXkgYmluYXJ5IGRhdGE=",
      host_key_document: "dummy certificate data",
    };

    render(
      <CoCoSettingsForm
        initialData={initialData}
        availableEnvironmentTypes={availableEnvironmentTypes}
        showOnScheduleOption={false}
        saveHandler={saveHandler}
      />
    );

    // The download buttons should be present
    expect(screen.getByLabelText("Download the current Secure execution header")).not.toBeNull();
    expect(screen.getByLabelText("Download the current Host Key Document certificate")).not.toBeNull();

    // Make an unrelated change and save
    await click(screen.getByRole("button", { name: "Perform attestation during the boot process" }));
    await click(screen.getByText("Save"));

    // The inputData should be unchanged
    expect(saveHandler).toHaveBeenCalledTimes(1);
    await expect(saveHandler.mock.lastCall?.[0]).resolves.toEqual({
      enabled: true,
      environmentType: "KVM_IBM_Z",
      attestOnBoot: true,
      attestOnSchedule: false,
      inputData: {
        secure_execution_header: "ZHVtbXkgYmluYXJ5IGRhdGE=",
        host_key_document: "dummy certificate data",
      },
    });
  });

  test("calls the save handler with the expected additional data", async () => {
    render(
      <CoCoSettingsForm
        initialData={initialData}
        availableEnvironmentTypes={availableEnvironmentTypes}
        showOnScheduleOption={false}
        saveHandler={saveHandler}
      />
    );

    // Enable the attestation
    await click(screen.getByRole("button", { name: "Enable attestation" }));

    // Change the environment to IBM Z
    selectOptions(screen.getByLabelText("Environment Type"), "KVM on IBM Z");

    const secureExecutionHeaderInput = screen.getByLabelText("Secure execution header");
    const file = new File(["dummy binary data"], "secure-extension-header.bin", { type: "application/octet-stream" });
    upload(secureExecutionHeaderInput, file);

    await click(screen.getByLabelText("Paste the data"));

    const hostKeyDocumentText = screen.getByLabelText("PEM certificate");
    paste(hostKeyDocumentText, "dummy certificate data");

    // Click on save
    await click(screen.getByText("Save"));

    expect(saveHandler).toHaveBeenCalledTimes(1);
    await expect(saveHandler.mock.calls[0][0]).resolves.toEqual({
      enabled: true,
      environmentType: "KVM_IBM_Z",
      attestOnBoot: false,
      attestOnSchedule: false,
      inputData: {
        secure_execution_header: "ZHVtbXkgYmluYXJ5IGRhdGE=",
        host_key_document: "dummy certificate data",
      },
    });
  });
});
