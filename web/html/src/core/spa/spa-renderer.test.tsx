import { Component, forwardRef, useImperativeHandle } from "react";
import ReactDOM from "react-dom";

import SpaRenderer from "./spa-renderer";

describe("SpaRenderer", () => {
  let container: HTMLDivElement;

  beforeEach(() => {
    window.pageRenderers = {
      spa: {
        globalRenderersToUpdate: [],
        reactAppsName: [],
        reactRenderers: [],
        previousReactRenderers: [],
      },
    };
    container = document.createElement("div");
    document.body.appendChild(container);
  });

  afterEach(() => {
    ReactDOM.unmountComponentAtNode(container);
    container.remove();
    jest.restoreAllMocks();
  });

  test("renders function components without adding a ref", () => {
    const consoleError = jest.spyOn(console, "error").mockImplementation(() => undefined);
    const FunctionComponent = () => <span>Global content</span>;

    SpaRenderer.renderGlobalReact(<FunctionComponent />, container);

    expect(consoleError).not.toHaveBeenCalled();
    expect(window.pageRenderers?.spa?.globalRenderersToUpdate).toHaveLength(0);
  });

  test("renders forwarded-ref components without registering them for SPA updates", () => {
    const ForwardRefComponent = forwardRef((_props, ref) => {
      useImperativeHandle(ref, () => ({}), []);
      return <span>Global content</span>;
    });

    SpaRenderer.renderGlobalReact(<ForwardRefComponent />, container);

    expect(window.pageRenderers?.spa?.globalRenderersToUpdate).toHaveLength(0);
  });

  test("registers class components for SPA updates by default", () => {
    const onSpaEndNavigation = jest.fn();

    class GlobalComponent extends Component {
      onSPAEndNavigation = onSpaEndNavigation;

      render() {
        return <span>Global content</span>;
      }
    }

    SpaRenderer.renderGlobalReact(<GlobalComponent />, container);
    SpaRenderer.onSpaEndNavigation();

    expect(onSpaEndNavigation).toHaveBeenCalledTimes(1);
  });
});
