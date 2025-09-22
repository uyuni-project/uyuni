const { defineConfig, globalIgnores } = require("eslint/config");

const eslint = require("@eslint/js");
const tseslint = require("typescript-eslint");

const reactHooks = require("eslint-plugin-react-hooks");
const localRules = require("eslint-plugin-local-rules");
const simpleImportSort = require("eslint-plugin-simple-import-sort");
const prettier = require("eslint-plugin-prettier");
const jsxA11y = require("eslint-plugin-jsx-a11y");
const react = require("eslint-plugin-react");

const globals = require("globals");

const productionRules = {
  "prettier/prettier": "error",
  "@typescript-eslint/no-unused-vars": "error",
  "no-console": "error",
};

module.exports = defineConfig([
  eslint.configs.recommended,
  // In the future, it would be nice to use `tseslint.configs.recommended` here, but legacy code is too far from that for now
  tseslint.configs.stylistic,
  {
    languageOptions: {
      // parser: tsParser,

      globals: {
        ...globals.browser,
        ...globals.node,
        t: true,
        module: true,
        jQuery: true,
      },
    },

    plugins: {
      react,
      "react-hooks": reactHooks,
      "local-rules": localRules,
      "simple-import-sort": simpleImportSort,
      "jsx-a11y": jsxA11y,
      prettier,
    },

    rules: {
      // Too much legacy code holds empty references and such, we can't enable these rules yet, but aim for it in the future
      "no-unused-vars": "off",
      "@typescript-eslint/no-empty-function": "off",
      "no-async-promise-executor": "off",
      "no-prototype-builtins": "off",
      "no-case-declarations": "off",

      "prettier/prettier": "warn",
      "jsx-a11y/anchor-is-valid": "error",
      "react/jsx-no-target-blank": "error",
      "react-hooks/rules-of-hooks": "error",
      eqeqeq: "error",
      radix: ["error", "always"],
      // ESLint doesn't recongize overloads by default
      "no-redeclare": "off",
      "@typescript-eslint/no-redeclare": "error",
      "@typescript-eslint/prefer-for-of": "off",
      // TODO: Eventually this should be "error"
      "local-rules/no-raw-date": "warn",
      "local-rules/intl-apostrophe-curly": "error",
      // TODO: Eventually we should enforce this as well
      // "no-eq-null": "error",
      // TODO: This needs to be reworked with Typescript support in mind
      "no-use-before-define": "off",
      // See https://reactjs.org/blog/2020/09/22/introducing-the-new-jsx-transform.html#eslint
      "react/jsx-uses-react": "off",
      "react/react-in-jsx-scope": "off",
      // This rule is misleading, using [] as a dependency array is completely valid, see https://stackoverflow.com/a/58579462/1470607
      "react-hooks/exhaustive-deps": "off",
      // Enforce sanity in imports
      "simple-import-sort/imports": [
        "error",
        {
          // See https://github.com/lydell/eslint-plugin-simple-import-sort/#custom-grouping
          groups: [
            // Side effect imports
            ["^\\u0000"],
            // HMR needs to be imported before everything else
            ["^react-hot-loader/root"],
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
      // "no-duplicate-imports": "error",
      // We use a `DEPRECATED_` prefix for old components that doesn't conform with this rule
      "react/jsx-pascal-case": "off",
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
      ...(process.env.NODE_ENV === "production" ? productionRules : {}),
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
  globalIgnores(["dist/**/*", "vendors/**/*", "build/yarn/**/*"]),
  {
    // Build scripts are allowed to use the console
    files: ["build/**"],
    rules: {
      "no-console": "off",
    },
  },
]);
