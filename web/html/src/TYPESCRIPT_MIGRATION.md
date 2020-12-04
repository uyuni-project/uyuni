# Typescript migration

Notes
 - This relies on TS 4.1.2, 3.x does not give sufficient debugging output for automatic migrations.

Migration steps:  

1. Install new dependencies: `yarn`  
1. Migrate CommonJS imports and exports to ES6 as much as possible: `yarn migrate-cjs-to-es6`  
    * This will log errors in places that they don't know how to handle, this is fine.
1. Check which Flow files are found: `yarn find-flow`  
1. Migrate any found Flow files to TS: `yarn migrate-flow`  
1. Check which Javascript files have type annotations without Flow or TS: `yarn find-untyped-annotated`
1. Migrate any of the above to TS: `yarn migrate-untyped-annotated`
    * There is one file which throws here, simply renaming to tsx is sufficient: `mv components/section-toolbar/section-toolbar.js components/section-toolbar/section-toolbar.tsx`
    * Confirm no more files remain that need to be updated, should output no file names: `yarn find-untyped-annotated`
1. Check for compilation errors: `yarn tsc`
    * There is one `Unexpected token` issue in the JSX template in `manager/shared/menu/menu.js`, resolve it by replacing the token with `&gt;`
1. Check which files have `Object` annotations which should be `any`: `yarn find-object-to-any`
    * For context, Flow and TS handle `Object` very differently. In Flow it's a loose type that matches anything, in TS it's a strict type and `any` should be used to allow anything instead.
1. Fix any of the above: `yarn migrate-object-to-any`
1. Some of our `useState` calls are typed incorrectly as `undefined` because they have no annotations, we should mark them as `any`. To find them: `yarn find-untyped-use-state`
1. Fix any of the above: `yarn migrate-untyped-use-state`


Notes:
 - about 10 files need manual Node -> ReactNode migration
 - about 70 files need manual module.exports -> export/export default migration
 - a number of types are declared but not actually exported for other modules to use
 - untyped object initializations `let websocket = {};` could be fixed by adding `any`
 - some components can't be used as JSX children, why?
