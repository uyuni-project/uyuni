import * as React from "react";

type Props = {
  title: string;
  text: React.ReactNode;
  customHeader: React.ReactNode;
};

const LoginHeader = (props: Props) => (
  <header className="navbar-pf">
    <div className="wrap">
      <div className="navbar-header d-flex flex-row">
        <div className="custom-text">{props.customHeader}</div>
      </div>
      <ul className="nav navbar-nav navbar-utility d-flex flex-row">
        <li>
          <a href="/rhn/help/about.do" className="about-link">
            About
          </a>
        </li>
      </ul>
    </div>
  </header>
);

export default LoginHeader;
