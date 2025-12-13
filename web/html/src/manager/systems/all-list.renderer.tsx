import _unescape from "lodash/unescape";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { AllSystems } from "./all-list";

type RendererProps = {
  isAdmin: boolean;
  queryColumn?: string;
  query?: string;
};

export const renderer = (id: string, docsLocale: string, { isAdmin, queryColumn, query }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <AllSystems docsLocale={docsLocale} isAdmin={isAdmin} queryColumn={queryColumn} query={_unescape(query)} />
    </>,
    document.getElementById(id)
  );
