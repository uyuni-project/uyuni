import * as React from "react";

import { docsLocale } from "core/user-preferences";

type Props = {
  productName: string;
  webVersion: string;
  customFooter: React.ReactNode;
};

const LoginFooter = (props: Props) => (
  <footer>
    <div>
      <div className="wrapper wrap">
        <div>
          <a href="/rhn/help/about.do">About</a>
        </div>
        <div className="footer-copyright">
          <a href="/rhn/help/Copyright.do">Copyright Notice</a>{" "}
        </div>
        <div className={`footer-release`}>
          {`${props.productName} release `}
          <a href={`/docs/${docsLocale}/release-notes/release-notes-server.html`}>{props.webVersion}</a>
        </div>
        <div>{props.customFooter}</div>
      </div>
      <div className="bottom-line" />
    </div>
  </footer>
);

export default LoginFooter;
