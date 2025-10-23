import eslint from "@eslint/js";
import { defineConfig, globalIgnores } from "eslint/config";
import jsxA11y from "eslint-plugin-jsx-a11y";
import prettier from "eslint-plugin-prettier";
import react from "eslint-plugin-react";
import reactHooks from "eslint-plugin-react-hooks";
import simpleImportSort from "eslint-plugin-simple-import-sort";
import unicorn from "eslint-plugin-unicorn";
import globals from "globals";
import tseslint from "typescript-eslint";

import localRules from "./eslint-local-rules/index.js";

const isProduction = process.env.NODE_ENV === "production";

export default defineConfig([
  globalIgnores(["html/src/dist/**/*", "html/src/vendors/**/*", "html/javascript/**/*"]),
  eslint.configs.recommended,
  tseslint.configs.recommended,
  {
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node,
        ...globals.builtin,
        t: true,
        module: true,
        jQuery: true,
      },
    },

    plugins: {
      react,
      "react-hooks": reactHooks,
      "simple-import-sort": simpleImportSort,
      "jsx-a11y": jsxA11y,
      "local-rules": localRules,
      unicorn,
      prettier,
    },

    rules: {
      // We use `@typescript-eslint/no-unused-vars` instead
      "no-unused-vars": "off",
      "@typescript-eslint/no-unused-vars": [
        isProduction ? "error" : "warn",
        {
          caughtErrors: "none",
          ignoreRestSiblings: true,
          destructuredArrayIgnorePattern: "^_",
        },
      ],
      "prettier/prettier": isProduction ? "error" : "warn",
      "no-console": isProduction ? "error" : "warn",
      // Too much legacy code holds empty references and such, we can't enable these rules yet, but aim for it in the future
      "@typescript-eslint/no-explicit-any": "off",
      "no-async-promise-executor": "off",

      "no-case-declarations": "error",
      "jsx-a11y/anchor-is-valid": "error",
      "react/jsx-no-target-blank": "error",
      "react-hooks/rules-of-hooks": "error",
      eqeqeq: "error",
      radix: ["error", "always"],
      "unicorn/no-useless-spread": "error",
      // Align with existing code style
      "@typescript-eslint/no-redeclare": "error",
      "@typescript-eslint/prefer-for-of": "off",
      "@typescript-eslint/consistent-type-definitions": "off",
      "@typescript-eslint/no-redundant-type-constituents": "off",
      "@typescript-eslint/no-inferrable-types": "off",
      "@typescript-eslint/consistent-generic-constructors": "off",
      "local-rules/no-raw-date": "error",
      "local-rules/intl-apostrophe-curly": "error",
      "no-eq-null": "error",
      // Enforce sanity in imports
      "simple-import-sort/imports": [
        "error",
        {
          // See https://github.com/lydell/eslint-plugin-simple-import-sort/#custom-grouping
          groups: [
            // Side effect imports
            ["^\\u0000"],
            ["^react$", "^react-dom$"],
            // Fullcalendar needs to be imported before its plugins
            ["^@fullcalendar/react"],
            // Packages
            ["^@?\\w"],
            // Root imports, each grouped together
            ["^manager"],
            ["^core"],
            ["^components"],
            ["^utils"],
            // Relative imports
            ["^\\."],
          ],
        },
      ],
      "sort-imports": "off",
      "no-duplicate-imports": "error",
      // We use a `DEPRECATED_` prefix for old components that doesn't conform with this rule
      "react/jsx-pascal-case": [
        "error",
        {
          ignore: ["DEPRECATED_*"],
        },
      ],
      "no-restricted-imports": [
        "error",
        {
          paths: [
            {
              name: "node-gettext",
              message: "Please import from `core/intl/node-gettext` instead.",
            },
            // TODO: List everything we want to limit once the implementation is done
            // {
            //   name: "formik",
            //   importNames: ["Field", "Form"],
            //   // TODO: Update message once we move the directory to where it should be
            //   message: "Please import from `components/formik` instead.",
            // },
          ],
        },
      ],
    },

    settings: {
      "import/resolver": {
        alias: {
          map: [
            ["components", "./components"],
            ["core", "./core"],
            ["manager", "./manager"],
            ["utils", "./utils"],
          ],
        },
      },
    },
  },
  {
    // Build scripts and tests are allowed to use the console
    files: ["html/src/build.js", "html/src/build/**", "html/src/utils/test-utils/**", "**/*.test.{ts,tsx}"],
    rules: {
      "no-console": "off",
    },
  },
  {
    // Examples and tests can have lingering vars to exemplify
    files: ["**/*.example.{ts,tsx}", "**/*.test.{ts,tsx}"],
    rules: {
      "@typescript-eslint/no-unused-vars": "off",
    },
  },
]);
