// @flow
import React from 'react';
import styles from './login.css';

type Props = {
  productName: string,
  webVersion: string,
  customFooter: string,
}

const LoginFooter = (props: Props) => (
  <footer className={styles.footer_wrapper}>
    <div className={styles.footer_fixed_bottom}>
      <div className="wrapper wrap">
        <div className="footer-copyright">
          <a href="/rhn/help/Copyright.do">Copyright Notice</a>{' '}
        </div>
        <div className={`footer-release ${styles.footer_release_container}`}>
          {`${props.productName} release `}
          <a href="/rhn/help/dispatcher/release_notes">
            {props.webVersion}
          </a>
        </div>
        <div>{props.customFooter}</div>
      </div>
      <div className="bottom-line" />
    </div>
  </footer>
);

export default LoginFooter;
