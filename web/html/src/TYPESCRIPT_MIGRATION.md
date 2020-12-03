# Typescript migration

Migration steps:  

1. Install new dependencies: `yarn`  
2. Check which Flow files are found: `yarn find-flow`  
3. Migrate any found Flow files to TS: `yarn migrate-flow`
4. Check which Javascript files have type annotations without Flow or TS: `yarn find-untyped-annotated`
5. Migrate any of the above to TS: `yarn migrate-untyped-annotated`
6. Check compilation state: `yarn tsc`


Notes:
 - about 10 files which need manual Node -> ReactNode migration
 - many, many instances where the type is `Object`, but should be `any`
   grep and selectively replace `: Object` in .ts files, ~100 items, do not replace Object.entries etc
 - a number of types are declared but not actually exported for other modules to use
 - untyped object initializations `let websocket = {};` could be fixed by adding `any`
 - some components can't be used as JSX children, why?