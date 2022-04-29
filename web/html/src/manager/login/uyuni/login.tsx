import * as React from "react";

import { AsyncButton } from "components/buttons";
import { useInputValue } from "components/hooks/forms/useInputValue";
import { Messages } from "components/messages";

import { ThemeProps } from "../login";
import { getFormMessages, getGlobalMessages } from "../messages";
import useLoginApi from "../use-login-api";
import styles from "./login.css";
import LoginFooter from "./login-footer";
import LoginHeader from "./login-header";

const UyuniThemeLogin = (props: ThemeProps) => {
  const loginInput = useInputValue("");
  const passwordInput = useInputValue("");
  const { onLogin, success, messages } = useLoginApi();

  const { product } = props;

  return (
    <React.Fragment>
      <LoginHeader title={product.title} text={product.headerTitle} customHeader={props.customHeader} />

      <div className={`spacewalk-main-column-layout ${styles.fixed_content}`}>
        <section id="spacewalk-content">
          <div className="wrap">
            <Messages
              items={getGlobalMessages(props.validationErrors, props.schemaUpgradeRequired, props.diskspaceSeverity)}
            />
            <div className="col-sm-6">
              <h1>{product.bodyTitle}</h1>
              <p className="gray-text margins-updown">
                {t("Discover a new way of managing your servers, packages, patches and more via one interface.")}
              </p>
              <p className="gray-text">
                {t("Learn more about")} {product.key}:
                <a href={product.url} className="btn-dark" target="_blank" rel="noopener noreferrer">
                  {" "}
                  View website
                </a>
              </p>
            </div>
            <div className="col-sm-5 col-sm-offset-1">
              <Messages items={getFormMessages(success, messages)} />
              <h2 className="gray-text">{t("Sign In")}</h2>
              <form onSubmit={(event) => event.preventDefault()} name="loginForm">
                <div className="margins-updown">
                  <input
                    id="username-field"
                    name="username"
                    className="form-control"
                    type="text"
                    placeholder={t("Login")}
                    maxLength={parseInt(props.loginLength, 10)}
                    autoFocus={true}
                    {...loginInput}
                  />
                  <input
                    id="password-field"
                    name="password"
                    className="form-control"
                    type="password"
                    autoComplete="password"
                    placeholder={t("Password")}
                    maxLength={parseInt(props.passwordLength, 10)}
                    {...passwordInput}
                  />
                  <AsyncButton
                    id="login-btn"
                    className="btn-block"
                    defaultType="btn-success"
                    text={t("Sign In")}
                    action={() =>
                      onLogin({
                        login: loginInput.value,
                        password: passwordInput.value,
                      }).then((success) => success && window.location.replace(props.bounce))
                    }
                  />
                </div>
              </form>
              <hr />
              <p className="gray-text small-text">{props.legalNote}</p>
            </div>
          </div>
        </section>
      </div>

      <LoginFooter productName={props.productName} customFooter={props.customFooter} webVersion={props.webVersion} />
    </React.Fragment>
  );
};

export default UyuniThemeLogin;
