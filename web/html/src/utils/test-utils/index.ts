import { rest } from "msw";
import { setupServer } from "msw/node";

// See https://testing-library.com/docs/ecosystem-user-event/#api
import userEvent from "@testing-library/user-event";
import * as selectEvent from "react-select-event";

// Reexport everything so we can get all of our utilities from a single location
// See https://testing-library.com/docs/react-testing-library/api/
export * from "@testing-library/react";

// react-select testing utilities: https://github.com/romgain/react-select-event#api
export * from "react-select-event";

export * from "./timer";

// @testing-library/user-event has messed up exports, just manually reexport everything
export const {
  click,
  dblClick,
  upload,
  clear,
  selectOptions,
  deselectOptions,
  tab,
  hover,
  unhover,
  paste,
} = userEvent;

/**
 * This is a usable alternative for @testing-library/user-event's `type()`.
 * The library;s `type()` is non-deterministic without a delay and prohibitively
 * slow even if you use Number.MIN_VALUE as the delay value.
 * Instead we use the `paste()` method which inserts the full text in one go and
 * pretend there is no difference.
 */
export const type = async <T extends HTMLInputElement>(elementOrPromiseOfElement: T | Promise<T>, text: string, append = false) => {
  const target = await elementOrPromiseOfElement;
  if (!append) {
    userEvent.clear(target);
  }
  userEvent.paste(target, text, undefined);

  /**
   * `window.requestAnimationFrame` mandatory to ensure we don't proceed until
   * the UI has updated, expect non-deterministic results without this.
   */
  return new Promise(resolve => window.requestAnimationFrame(() => resolve(undefined)));
};

export * from "./forms";

export const select = async (...[input, option, config]: Parameters<typeof selectEvent.select>) => {
  return selectEvent.select(input, option, Object.assign({}, {container: document.body}, config));
}

const baseServer = setupServer();
const serverAddons = {
  /** Mock a GET request to `url` with a successful JSON response containing `response` */
  mockGetJson(url, response) {
    return server.use(
      rest.get(url, (req, res, ctx) => {
        return res(ctx.json(response));
      })
    );
  }
};

type Server = typeof baseServer & typeof serverAddons;
const server: Server = Object.assign(baseServer, serverAddons);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

export { server };

export * from "./mock";
