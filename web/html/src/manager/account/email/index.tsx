import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { AccountEmailForm } from "./form";

type RendererProps = {
  currentEmail?: string;
  targetUserId?: number;
  targetUserName?: string;
  contextMode?: "own" | "admin";
};

export const renderer = (
  id: string,
  locale: string,
  { currentEmail = "", targetUserId, targetUserName = "", contextMode = "own" }: RendererProps = {}
) => {
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <AccountEmailForm
        currentEmail={currentEmail}
        userId={targetUserId}
        userName={targetUserName}
        contextMode={contextMode}
      />
    </>,
    document.getElementById(id)
  );
};
