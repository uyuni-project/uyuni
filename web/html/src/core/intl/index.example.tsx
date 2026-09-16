/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

export default () => {
  const fooCount = 1;

  return (
    <>
      <p>
        {t("Please see <link>Format.JS docs</link> for the message syntax.", {
          link: (str) => (
            <a
              href="https://formatjs.github.io/docs/core-concepts/icu-syntax/"
              target="_blank"
              rel="noopener noreferrer"
              key={str}
            >
              {str}
            </a>
          ),
        })}
      </p>

      <p>
        {t(
          `Always use plural forms for numbers: {fooCount, plural,
            one {only one item}
            other {{fooCount} items}
          }.`,
          {
            fooCount,
          }
        )}
      </p>
    </>
  );
};
