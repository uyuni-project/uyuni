import { rest } from "msw";
import { setupServer } from "msw/node";

// See https://testing-library.com/docs/ecosystem-user-event/#api
import userEvent from "@testing-library/user-event";

// Reexport everything so we can get all of our utilities from a single location
// See https://testing-library.com/docs/react-testing-library/api/
export * from "@testing-library/react";

// react-select testing utilities: https://github.com/romgain/react-select-event#api
export * from "react-select-event";

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
  specialChars,
} = userEvent;

export const type = async (elementOrPromiseOfElement, text, options) => {
  const mergedOptions = Object.assign({
    /**
     * @testing-library/user-event's `type()` is inconsistent without a delay
     * Since delay is inserted between each keystroke, we use Number.MIN_VALUE to avoid test timeouts
     */
    delay: Number.MIN_VALUE
  }, options);
  await userEvent.type(await elementOrPromiseOfElement, text, mergedOptions);
  return new Promise(resolve => window.requestAnimationFrame(() => resolve()));
}

export * from "./forms";

const server = setupServer();

/** Mock a GET request to `url` with a successful JSON response containing `response` */
server.mockGetJson = function mockGetJson(url, response) {
  return server.use(
    rest.get(url, (req, res, ctx) => {
      return res(ctx.json(response));
    })
  );
};

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

export { server };
