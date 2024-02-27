The `web` subdirectory contains the React based modern web UI, related assets such as fonts and images, translations, and some miscellaneous legacy bits and pieces.  
For the legacy web UI, please see the `java` subdirectory.  

The `web` subdirectory consists of roughly the following main chunks:  

 - `web/po`: Translations.
 - `web/html/javascript`: Legacy scripts, most of them global. Over time we're slowly trying to sunset these piece by piece.  
 - `web/html/src/branding`: Branding assets, such as stylesheet sources, fonts, images etc.
 - `web/html/src/build`: Build tooling for the web UI.
 - `web/html/src/components`, `web/html/src/core`, `web/html/src/manager`, `web/html/src/utils`: Source code for the web UI.
 - `web/html/dist`: Output directory for the frontend build, do not check this directory in nor modify it directly, your changes will be overwritten by the next build.  

## Frontend development quick start

 - Install [Node](https://nodejs.org/en/download)  
 - Install [Yarn](https://classic.yarnpkg.com/en/docs/install)  
 - In the repository root, run `yarn install`  
 - Run development against a server of your choice `yarn proxy https://server.example.com`  

## Scripts

We use [Yarn](https://yarnpkg.com/) as the package manager and script runner for the frontend codebase. All scripts are scoped to `web/html/src`.  
The following scripts cover most day-to-day uses, see `web/html/src/package.json` for more:  

 - Run lint with autofixer: `yarn lint`
 - Run unit tests: `yarn test`  
 - Run the Typescript checker: `yarn tsc`  
 - Build the web UI: `yarn build`  
 - Run lint, tests, Typescript checker, and build the application: `yarn all`  
 - Audit production dependencies: `yarn audit-production-dependencies`
 - Run a development proxy against a server: `yarn proxy https://server.example.com`  

## VSCode

If you use VSCode for development, please install [the ESLint extension](https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint).
