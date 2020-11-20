import { rest } from "msw";
import { setupServer } from "msw/node";

// Reexport everything so we can get all of our utilities from a single location
export * from "@testing-library/react";

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
