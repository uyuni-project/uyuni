import { Fragment, useEffect, useState } from "react";

import debugUtils from "core/debugUtils";

import { Button } from "components/buttons";
import { IconTag } from "components/icontag";

import { StoryRow } from "./layout";
import stories from "./stories";
import styles from "./storybook.module.less";

const STORAGE_KEY = "storybook-show-code";

export const Storybook = () => {
  const [_hash, setHash] = useState(window.location.hash);
  const hash = _hash.replace(/^#/, "");
  const normalize = (input: string = "") => input.replaceAll(" ", "-").toLowerCase();

  const activeTabHash = normalize(hash) || normalize(stories[0]?.title);

  const [, _invalidate] = useState(0);
  const invalidate = () => _invalidate((ii) => ii + 1);

  const [showCode, _setShowCode] = useState(!!localStorage.getItem(STORAGE_KEY));
  const setShowCode = (value: boolean) => {
    _setShowCode(value);
    if (value) {
      localStorage.setItem(STORAGE_KEY, "true");
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  };

  useEffect(() => {
    const listener = () => setHash(window.location.hash);
    window.addEventListener("hashchange", listener);
    return () => {
      window.removeEventListener("hashchange", listener);
    };
  }, []);

  return (
    <>
      <div className={styles.header}>
        <h1>
          <IconTag type="experimental" />
          {t("Development debugging page")}
        </h1>
        <p>{t("This is a hidden page used by developers, if you found it by accident, good job!")}</p>
        <p>
          <code>{document.body.className}</code>
        </p>

        <StoryRow>
          <Button
            text="toggle base theme"
            className="btn-default"
            handler={() => {
              debugUtils.toggleTheme();
              invalidate();
            }}
          />
          <Button
            text="toggle theme update"
            className="btn-default"
            handler={() => {
              debugUtils.toggleUpdatedTheme();
              invalidate();
            }}
          />
          <Button text="toggle code" className="btn-default" handler={() => setShowCode(!showCode)} />
        </StoryRow>
      </div>

      <div className="spacewalk-content-nav">
        <ul className="nav nav-tabs">
          {stories
            .sort((a, b) => a.title.localeCompare(b.title))
            .map((item) => {
              const tabHash = normalize(item.title);
              return (
                <li key={tabHash} className={tabHash === activeTabHash ? "active" : ""}>
                  <a href={`#${tabHash}`}>{item.title}</a>
                </li>
              );
            })}
        </ul>
      </div>

      {stories.map((group) => (
        <div key={`${group.title}`}>
          {normalize(group.title) === activeTabHash &&
            group.stories?.map((item) => (
              <Fragment key={`${group.title}-${item.title}`}>
                <p>
                  <code>{item.title}</code>
                </p>
                <div className={styles.story}>
                  <div>{item.component ? <item.component /> : null}</div>
                  {showCode ? (
                    <pre>
                      <code>{item.raw}</code>
                    </pre>
                  ) : null}
                </div>
                <hr />
              </Fragment>
            ))}
        </div>
      ))}
    </>
  );
};
