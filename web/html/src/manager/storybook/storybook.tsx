import { Fragment, useEffect, useState } from "react";

import debugUtils from "core/debugUtils";

import { Button } from "components/buttons";
import { IconTag } from "components/icontag";

import { useQueryParams } from "utils/hooks";

import { StoryRow } from "./layout";
import stories from "./stories";
import styles from "./storybook.module.scss";

const STORAGE_KEY = "storybook-show-code";

export const Storybook = () => {
  const { tab, story } = useQueryParams();

  const normalize = (input = "") => input.replaceAll(" ", "-").toLowerCase();
  const isDeprecated = (input = "") => input.startsWith("DEPRECATED_");

  const activeTab = normalize(tab) || normalize(stories[0]?.title);

  const [_, set_] = useState(0);
  const invalidate = () => set_((ii) => ii + 1);

  const [showCode, setShowCode] = useState(!!localStorage.getItem(STORAGE_KEY));
  const toggleShowCode = (value: boolean) => {
    setShowCode(value);
    if (value) {
      localStorage.setItem(STORAGE_KEY, "true");
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  };

  useEffect(() => {
    if (!story) {
      return;
    }
    document.getElementById(story)?.scrollIntoView();
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
          <Button text="toggle code" className="btn-default" handler={() => toggleShowCode(!showCode)} />
        </StoryRow>
      </div>

      <div className="spacewalk-content-nav">
        <ul className="nav nav-tabs">
          {stories
            .sort((a, b) => a.title.localeCompare(b.title))
            .map((item) => {
              const tabTitle = normalize(item.title);
              const href = new URL(window.location.href);
              href.searchParams.set("tab", tabTitle);
              href.searchParams.delete("story");

              return (
                <li key={tabTitle} className={tabTitle === activeTab ? "active" : ""}>
                  <a href={href.toString()} className="js-spa">
                    {item.title}
                  </a>
                </li>
              );
            })}
        </ul>
      </div>

      {stories.map((group) => (
        <div key={`${group.title}`}>
          {normalize(group.title) === activeTab &&
            group.stories
              ?.sort((a, b) => {
                if (isDeprecated(a.title) && !isDeprecated(b.title)) return +1;
                if (!isDeprecated(a.title) && isDeprecated(b.title)) return -1;
                return a.title.localeCompare(b.title);
              })
              .map((item, index) => {
                const storyTitle = normalize(item.title);
                const href = new URL(window.location.href);
                href.searchParams.set("story", storyTitle);
                return (
                  <Fragment key={`${group.title}-${item.title}`}>
                    <p id={storyTitle}>
                      <a href={href.toString()}>
                        <code>{item.title}</code>
                      </a>
                    </p>
                    <div className={styles.story}>
                      <div>{item.component ? <item.component /> : null}</div>
                      {showCode ? (
                        <pre>
                          <code>{item.raw}</code>
                        </pre>
                      ) : null}
                    </div>
                    {group.stories && group.stories.length - 1 > index ? <hr /> : null}
                  </Fragment>
                );
              })}
        </div>
      ))}
    </>
  );
};
