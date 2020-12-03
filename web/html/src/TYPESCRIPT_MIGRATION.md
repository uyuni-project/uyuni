# Typescript migration

Migration steps:  

1. Install new dependencies: `yarn`  
2. Check which Flow files are found: `yarn find-flow`  
3. Migrate any found Flow files to TS: `yarn migrate-flow`
4. Check which Javascript files have type annotations without Flow or TS: `yarn find-untyped-annotated`
5. Migrate any of the above to TS: `yarn migrate-untyped-annotated`
    * There is one file which throws here, simply renaming to tsx is sufficient: `mv components/section-toolbar/section-toolbar.js components/section-toolbar/section-toolbar.tsx`
6. Check for compilation errors: `yarn tsc`
    * There is one `Unexpected token` issue in the JSX template in `manager/shared/menu/menu.js`, resolve it by replacing the token with `&gt;`
7. Check which files have `Object` annotations which should be `any`: `yarn find-object-to-any`
    * For context, Flow and TS handle `Object` very differently. In Flow it's a loose type that matches anything, in TS it's a strict type and `any` should be used to allow anything instead.
8. Fix any of the above: `yarn migrate-object-to-any`

Notes:
 - about 10 files which need manual Node -> ReactNode migration
 - many, many instances where the type is `Object`, but should be `any`
   grep and selectively replace `: Object` in .ts files, ~100 items, do not replace Object.entries etc
 - a number of types are declared but not actually exported for other modules to use
 - untyped object initializations `let websocket = {};` could be fixed by adding `any`
 - some components can't be used as JSX children, why?


Object issues:
Object.assign
Object.create
Object.values

: Object[^\.]