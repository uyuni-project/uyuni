import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { HeaderSearch } from "./search";

export const renderer = (parent: Element) => SpaRenderer.renderGlobalReact(<HeaderSearch />, parent);
