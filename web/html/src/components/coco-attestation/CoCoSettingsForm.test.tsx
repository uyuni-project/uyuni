import { beforeEach, describe, expect, jest, test } from "@jest/globals";

import { click, render, screen, selectOptions } from "utils/test-utils";

import { CoCoSettingsForm } from "./CoCoSettingsForm";
import { Settings } from "./Utils";

jest.mock("components/picker/recurring-event-picker", () => ({
  RecurringEventPicker: () => <div data-testid="recurring-event-picker" />,
}));

describe("CoCoSettingsForm", () => {
  const availableEnvironmentTypes = {
    KVM_AMD: "KVM on AMD",
    KVM_INTEL: "KVM on INTEL",
  };

  let initialData: Settings;
  let saveHandler: jest.MockedFunction<(data: Settings) => void>;

  beforeEach(() => {
    initialData = {
      enabled: false,
      environmentType: "KVM_AMD",
      attestOnBoot: false,
      attestOnSchedule: false,
    };

    saveHandler = jest.fn();
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
    expect(saveHandler.mock.lastCall?.[0]).toEqual({
      enabled: true,
      environmentType: "KVM_INTEL",
      attestOnBoot: true,
      attestOnSchedule: false,
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

  test("does not change disabled execution toggles", async () => {
    render(
      <CoCoSettingsForm
        initialData={initialData}
        availableEnvironmentTypes={availableEnvironmentTypes}
        showOnScheduleOption={false}
        saveHandler={saveHandler}
      />
    );

    const onBootToggler = screen.getByRole("button", { name: "Perform attestation during the boot process" });
    await click(onBootToggler);

    expect(onBootToggler.querySelector("i.fa-toggle-off")).not.toBeNull();

    await click(screen.getByText("Save"));

    expect(saveHandler).toHaveBeenCalledTimes(1);
    expect(saveHandler.mock.calls[0][0]).toEqual({
      enabled: false,
      environmentType: "KVM_AMD",
      attestOnBoot: false,
      attestOnSchedule: false,
    });
  });

  test("does not render or submit schedule state when the schedule option is hidden", async () => {
    initialData.attestOnSchedule = true;

    render(
      <CoCoSettingsForm
        initialData={initialData}
        availableEnvironmentTypes={availableEnvironmentTypes}
        showOnScheduleOption={false}
        saveHandler={saveHandler}
      />
    );

    expect(screen.queryByRole("button", { name: "Perform attestation on a schedule" })).toBeNull();
    expect(screen.queryByText("Select a schedule")).toBeNull();

    await click(screen.getByText("Save"));

    expect(saveHandler).toHaveBeenCalledTimes(1);
    expect(saveHandler.mock.calls[0][0]).toEqual({
      enabled: false,
      environmentType: "KVM_AMD",
      attestOnBoot: false,
      attestOnSchedule: false,
    });
  });

  test("renders the schedule picker when the schedule option is enabled", async () => {
    initialData.enabled = true;

    render(
      <CoCoSettingsForm
        initialData={initialData}
        availableEnvironmentTypes={availableEnvironmentTypes}
        showOnScheduleOption
        saveHandler={saveHandler}
      />
    );

    expect(screen.queryByText("Select a schedule")).toBeNull();

    await click(screen.getByRole("button", { name: "Perform attestation on a schedule" }));

    expect(screen.getByText("Select a schedule")).not.toBeNull();
    expect(screen.getByTestId("recurring-event-picker")).not.toBeNull();
  });
});
