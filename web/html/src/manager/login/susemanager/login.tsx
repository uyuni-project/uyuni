import * as React from "react";

import { docsLocale } from "core/user-preferences";

import { AsyncButton } from "components/buttons";
import { useInputValue } from "components/hooks/forms/useInputValue";
import { Messages } from "components/messages";

import { flatten } from "utils";

import { ThemeProps } from "../login";
import { getFormMessages, getGlobalMessages } from "../messages";
import useLoginApi from "../use-login-api";
import styles from "./login.css";
import logo from "./logo.svg";
import mobileLogo from "./mobile-logo.svg";

const SusemanagerThemeLogin = (props: ThemeProps) => {
  const loginInput = useInputValue("");
  const passwordInput = useInputValue("");
  const { onLogin, success, messages } = useLoginApi();

  const { product } = props;
  const globalMessages = getGlobalMessages(
    props.validationErrors,
    props.schemaUpgradeRequired,
    props.diskspaceSeverity
  );
  const formMessages = getFormMessages(success, messages);
  const errorMessages = globalMessages.concat(formMessages).filter((item) => item.severity !== "success");
  return (
    <div className={styles.background}>
      <section className={styles.wrapper}>
        <div className={styles.brand}>
          <img src={logo} alt="SUSE logo" width="150" height="27" />
          <div>
            <h1 className={styles.title}>{product.bodyTitle}</h1>
            <p>{t("Discover a new way of managing your servers, packages, patches and more via one interface.")}</p>
          </div>
          {t(
            <p className={styles.productInfo}>
              <a href={product.url} target="_blank" rel="noopener noreferrer" className={styles.productLink}>
                Learn more
              </a>{" "}
              about {product.key}.
            </p>
          )}
        </div>
        <div className={styles.loginArea}>
          <div className={styles.loginHeader}>
            <img src={mobileLogo} alt="SUSE logo" width="70" height="35" className={styles.mobileLogo} />
            <div className={styles.messagesArea}>
              <Messages items={errorMessages} />
            </div>
          </div>
          <form onSubmit={(event) => event.preventDefault()} className={styles.loginForm}>
            <h1 className={flatten([styles.title, styles.loginTitle])}>{t("Sign In")}</h1>
            <label htmlFor="username">{t("Username")}</label>
            <input
              id="username-field"
              name="username"
              className={`form-control ${styles.input}`}
              type="text"
              maxLength={parseInt(props.loginLength, 10)}
              autoFocus={true}
              {...loginInput}
            />
            <label htmlFor="password">{t("Password")}</label>
            <input
              id="password-field"
              name="password"
              className={`form-control ${styles.input}`}
              type="password"
              autoComplete="password"
              maxLength={parseInt(props.passwordLength, 10)}
              {...passwordInput}
            />
            <AsyncButton
              id="login-btn"
              className={`btn-block ${styles.button}`}
              defaultType="btn-primary"
              text={t("Sign In")}
              action={() =>
                onLogin({
                  login: loginInput.value,
                  password: passwordInput.value,
                }).then((success) => {
                  if (!success) {
                    return;
                  }
                  window.location.replace(props.bounce);
                })
              }
            />
          </form>
          <div className={styles.loginFooter}>
            <a href="/rhn/help/Copyright.do">Copyright Notice</a>
            <span>
              {`${props.productName} release `}
              <a href={`/docs/${docsLocale}/release-notes/release-notes-server.html`}>{props.webVersion}</a>
            </span>
            {props.customFooter && <span>{props.customFooter}</span>}
          </div>
        </div>
      </section>
    </div>
  );
};

export default SusemanagerThemeLogin;
