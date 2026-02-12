import { Fragment, useState } from "react";

import { docsLocale, productName } from "core/user-preferences";

import { SubmitButton } from "components/buttons";
import { useInputValue } from "components/hooks/forms/useInputValue";
import { Messages } from "components/messages/messages";

import { ThemeProps } from "../login";
import { getFormMessages, getGlobalMessages } from "../messages";
import useLoginApi from "../use-login-api";
import logo from "./footer-logo.svg";
import styles from "./login.module.scss";
import uyuniLogo from "./uyuni-logo.svg";

const UyuniThemeLogin = (props: ThemeProps) => {
  const loginInput = useInputValue("");
  const passwordInput = useInputValue("");
  const { onLogin, success, messages } = useLoginApi();
  const [isLoading, setIsLoading] = useState(false);

  const { product } = props;

  return (
    <Fragment>
      <header className="navbar-pf navbar" role="presentation" />
      <div className="spacewalk-main-column-layout">
        <section className={styles.contentArea}>
          <Messages
            items={getGlobalMessages(
              props.validationErrors,
              props.schemaUpgradeRequired,
              props.diskspaceSeverity,
              props.sccForwardWarning
            )}
          />
          <div className={styles.content}>
            <div className={`${styles.half} ${styles.left}`}>
              <h1 className={styles.h1}>
                {productName === "Uyuni" ? (
                  <img src={uyuniLogo} alt={product.productName} className={styles.uyuniLogo} />
                ) : (
                  product.bodyTitle
                )}
              </h1>
              <p>{t("Discover a new way of managing your servers, packages, patches and more via one interface.")}</p>
              <p>
                {t("Learn more about {productName}:", {
                  productName: product.productName,
                })}
                <a href={product.url} className={styles.learnMore} target="_blank" rel="noopener noreferrer">
                  {t("View website")}
                </a>
              </p>
            </div>
            <div className={`${styles.half} ${styles.right}`}>
              <Messages items={getFormMessages(success, messages)} />
              <h2 className={styles.h2}>{t("Sign In")}</h2>
              <form
                onSubmit={async (event) => {
                  event.preventDefault();
                  if (isLoading) {
                    return;
                  }

                  setIsLoading(true);
                  const success = await onLogin({
                    login: loginInput.value,
                    password: passwordInput.value,
                  });
                  if (success) {
                    window.location.replace(props.bounce);
                  }
                  setIsLoading(false);
                }}
                name="loginForm"
                className={styles.form}
              >
                <input
                  id="username-field"
                  name="username"
                  className="form-control"
                  type="text"
                  placeholder={t("Login")}
                  maxLength={parseInt(props.loginLength, 10)}
                  autoFocus={true}
                  required
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
                  required
                  {...passwordInput}
                />
                <SubmitButton
                  id="login-btn"
                  className={`btn-block btn-primary ${styles.submit}`}
                  text={t("Sign In")}
                  disabled={isLoading}
                />
              </form>
              {props.legalNote ? <p className="gray-text small-text">{props.legalNote}</p> : null}
            </div>
          </div>
        </section>
        {/* We don't use a <footer> tag here to avoid colliding with legacy global styles */}
        <div className={styles.footer} role="contentinfo">
          <div className={styles.content}>
            <div>
              <a href="/rhn/apidoc/index.jsp">API Documentation</a>
            </div>

            <div>
              {t("{productName} release {versionNumber}", {
                productName,
                versionNumber: (
                  <a href={`/docs/${docsLocale}/release-notes/release-notes-server.html`}>{props.webVersion}</a>
                ),
              })}
            </div>
            {props.customFooter ? <div>{props.customFooter}</div> : null}
            <a href="https://www.suse.com/" target="_blank" rel="noopener noreferrer" className={styles.logoLink}>
              <img src={logo} alt="SUSE" width="120" height="40" />
            </a>
          </div>
        </div>
      </div>
    </Fragment>
  );
};

export default UyuniThemeLogin;
