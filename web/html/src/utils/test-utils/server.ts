import { rest } from "msw";
import { setupServer } from "msw/node";

const baseServer = setupServer();
const serverAddons = {
  /** Mock a GET request to `url` with a successful JSON response containing `response` */
  mockGetJson(url, response) {
    return server.use(
      rest.get(url, (req, res, ctx) => {
        return res(ctx.json(response));
      })
    );
  },
};

type Server = typeof baseServer & typeof serverAddons;
const server: Server = Object.assign(baseServer, serverAddons);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

export { server };
