/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { fireEvent, render, waitFor } from "@testing-library/react";
import Modal from "bootstrap/js/dist/modal";

import { PopUp } from "./popup";

const originalBootstrap = globalThis.bootstrap;

beforeAll(() => Object.assign(globalThis, { bootstrap: { Modal } }));
afterAll(() => Object.assign(globalThis, { bootstrap: originalBootstrap }));

test("removes an open modal's backdrop, body state and instance on unmount", () => {
  const onClosePopUp = jest.fn();
  const view = render(<PopUp id="open-popup" title="Details" onClosePopUp={onClosePopUp} />);
  const element = view.getByRole("dialog", { hidden: true });
  Modal.getOrCreateInstance(element).show();
  expect(document.querySelector(".modal-backdrop")).not.toBeNull();
  expect(document.body.classList.contains("modal-open")).toBe(true);

  const updatedOnClose = jest.fn();
  view.rerender(<PopUp id="open-popup" title="Details" onClosePopUp={updatedOnClose} />);
  view.unmount();

  expect(document.querySelector(".modal-backdrop")).toBeNull();
  expect(document.body.classList.contains("modal-open")).toBe(false);
  expect(Modal.getInstance(element)).toBeNull();
  expect(onClosePopUp).not.toHaveBeenCalled();
  expect(updatedOnClose).not.toHaveBeenCalled();
});

test("keeps a non-closable modal open on Escape and backdrop clicks", async () => {
  const view = render(<PopUp id="restricted-popup" title="Important" closableModal={false} />);
  const element = view.getByRole("dialog", { hidden: true });
  const modal = Modal.getOrCreateInstance(element);
  modal.show();

  fireEvent.keyDown(element, { key: "Escape" });
  expect(element.classList.contains("show")).toBe(true);
  await waitFor(() => expect(element.classList.contains("modal-static")).toBe(false));
  fireEvent.mouseDown(element);
  fireEvent.click(element);
  expect(element.classList.contains("show")).toBe(true);
  await waitFor(() => expect(element.classList.contains("modal-static")).toBe(false));

  modal.hide();
  expect(element.classList.contains("show")).toBe(false);
  view.unmount();
});

test("still notifies callers when a closable modal is dismissed", () => {
  const onClosePopUp = jest.fn();
  const view = render(<PopUp id="closable-popup" title="Details" onClosePopUp={onClosePopUp} />);
  const element = view.getByRole("dialog", { hidden: true });
  Modal.getOrCreateInstance(element).show();

  fireEvent.keyDown(element, { key: "Escape" });

  expect(element.classList.contains("show")).toBe(false);
  expect(onClosePopUp).toHaveBeenCalledTimes(1);
  view.unmount();
});
