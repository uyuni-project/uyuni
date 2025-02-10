import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { Storybook } from "./storybook";

export const renderer = (parent: Element) =>
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <Storybook />
    </>,
    parent
  );
