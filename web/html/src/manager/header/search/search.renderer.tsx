import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { HeaderSearch } from "./search";

SpaRenderer.renderGlobalReact(<HeaderSearch />, document.getElementById("header-search"));
