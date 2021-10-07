import * as React from "react";
import { Loading } from "./Loading";

export default {
  component: Loading,
  title: "Utils/Loading",
};

export const normal = () => <Loading />;
export const withText = () => <Loading text="loading text" />;
export const withBorder = () => <Loading text="loading text" withBorders />;
