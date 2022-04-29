import * as React from "react";

import { docsLocale } from "core/user-preferences";

import styles from "./login.css";

type Props = {
  productName: string;
  webVersion: string;
  customFooter: React.ReactNode;
};

const LoginFooter = (props: Props) => (
  <footer className={styles.footer_wrapper}>
    <div className={styles.footer_fixed_bottom}>
      <div className="wrapper wrap">
        <div className="footer-copyright">
          <a href="/rhn/help/Copyright.do">Copyright Notice</a>{" "}
        </div>
        <div className={`footer-release ${styles.footer_release_container}`}>
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
