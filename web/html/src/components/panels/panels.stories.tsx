import * as React from "react";
import { storiesOf } from "@storybook/react";
import { BootstrapPanel } from "./BootstrapPanel";
import { TopPanel } from "./TopPanel";

storiesOf("BootstrapPanel", module).add("with title", () => (
  <BootstrapPanel title="Bootstrap title" footer="testing">
    stuff
  </BootstrapPanel>
));

storiesOf("TopPanel", module)
  .add("with title", () => <TopPanel title="Toppannel with icon and links">stuff</TopPanel>)
  .add("with icon and help link", () => (
    <TopPanel title="Toppannel with icon and links" icon="fa-filter" helpUrl="index.html">
      stuff
    </TopPanel>
  ));
