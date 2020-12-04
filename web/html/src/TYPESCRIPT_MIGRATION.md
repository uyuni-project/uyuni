# Typescript migration

Migration steps:  

1. Install new dependencies: `yarn`  
2. Check which Flow files are found: `yarn find-flow`  
3. Migrate any found Flow files to TS: `yarn migrate-flow`
4. Check which Javascript files have type annotations without Flow or TS: `yarn find-untyped-annotated`
5. Migrate any of the above to TS: `yarn migrate-untyped-annotated`
    * There is one file which throws here, simply renaming to tsx is sufficient: `mv components/section-toolbar/section-toolbar.js components/section-toolbar/section-toolbar.tsx`
    * Confirm no more files remain that need to be updated, should output no file names: `yarn find-untyped-annotated`
6. Check for compilation errors: `yarn tsc`
    * There is one `Unexpected token` issue in the JSX template in `manager/shared/menu/menu.js`, resolve it by replacing the token with `&gt;`
7. Check which files have `Object` annotations which should be `any`: `yarn find-object-to-any`
    * For context, Flow and TS handle `Object` very differently. In Flow it's a loose type that matches anything, in TS it's a strict type and `any` should be used to allow anything instead.
8. Fix any of the above: `yarn migrate-object-to-any`
9. We have many files which are CommonJS, but they should be ES6. Without migrating all of them, we can still fix many of the issues this creates. Check which files block scope import react: `yarn find-block-react`
10. Migrate any of the above imports to ES6: `yarn migrate-block-react`
11. Some of our `useState` calls are typed incorrectly as `undefined` because they have no annotations, we should mark them as `any`. To find them: `yarn find-untyped-use-state`
12. Fix any of the above: `yarn migrate-untyped-use-state`


Notes:
 - about 10 files which need manual Node -> ReactNode migration
 - many, many instances where the type is `Object`, but should be `any`
   grep and selectively replace `: Object` in .ts files, ~100 items, do not replace Object.entries etc
 - a number of types are declared but not actually exported for other modules to use
 - untyped object initializations `let websocket = {};` could be fixed by adding `any`
 - some components can't be used as JSX children, why?
