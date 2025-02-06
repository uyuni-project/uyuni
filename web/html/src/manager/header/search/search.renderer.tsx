import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { HeaderSearch } from "./search";

export const renderer = (id) => SpaRenderer.renderGlobalReact(<HeaderSearch />, document.getElementById(id));
