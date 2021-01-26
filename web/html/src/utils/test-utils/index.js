import { rest } from "msw";
import { setupServer } from "msw/node";

// See https://testing-library.com/docs/ecosystem-user-event/#api
import userEvent from "@testing-library/user-event";

// Reexport everything so we can get all of our utilities from a single location
// See https://testing-library.com/docs/react-testing-library/api/
export * from "@testing-library/react";

// @testing-library/user-event has messed up exports, just manually reexport everything
export const {
  click,
  dblClick,
  // We provide our own `type()` below
  type: rawType,
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

/** @testing-library/user-event's `type()` is inconsistent without a delay */
export const type = async (element, text, options) => {
  const mergedOptions = Object.assign({
    delay: 10
  }, options);
  return rawType(element, text, mergedOptions);
}

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
