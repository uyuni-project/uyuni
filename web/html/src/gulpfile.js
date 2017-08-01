/* File: gulpfile.js */

var gulp = require('gulp');
var gutil = require('gulp-util');
var source = require('vinyl-source-stream');
var browserify = require('browserify');
var babelify = require('babelify');
var glob  = require('glob');
var es = require('event-stream');
var rename = require('gulp-rename')
const eslint = require('gulp-eslint');
var bundlerOpts = null;

gulp.task('devel-opts', function(done) {
    bundlerOpts = {
        debug: true, // Gives us sourcemapping
        cache: {}, packageCache: {}, fullPaths: true // Requirement of watchify
    };
});

gulp.task('prod-opts', function(done) {
    bundlerOpts = {
        debug: false,
        cache: {}, packageCache: {}, fullPaths: false
    };
});

gulp.task('bundle-manager', function(done) {
    glob('./manager/**/*.js', function(err, files) {
        if(err) done(err);

        var tasks = files.map(function(entry) {
            bundlerOpts['entries'] = [entry]
            var bundler = browserify(bundlerOpts);

            return bundler
                .transform("babelify", {presets: ["es2015", "react"]})
                .external("react") // exclude react from these bundles
                .external("react-dom") // exclude react-dom from these bundles
                .bundle() // Create the initial bundle when starting the task
                .on('error', function (err) {
                    console.log(err.toString());
                    this.emit("end");
                    process.exit(1);
                })
                .pipe(source(entry)) // this is a surrogate file name //'org-state-catalog-app.js'
                .pipe(rename({
                    extname: '.bundle.js'
                }))
                .pipe(gulp.dest('../javascript'));

            });

        es.merge(tasks).on('end', done);
    })
});

gulp.task('lint', () => {
   // ESLint ignores files with "node_modules" paths.
    // So, it's best to have gulp ignore the directory as well.
    // Also, Be sure to return the stream from the task;
    // Otherwise, the task may end before the stream has finished.
    const components = "../../html/src/components/*.js"
    const manager = "../src/manager/*.js"
    const utils = "../../html/src/utils/*.js"
    const gulpfile = "gulpfile.js"
    return gulp.src([components, manager, utils, gulpfile, '!node_modules/**'])
        // eslint() attaches the lint output to the "eslint" property
        // of the file object so it can be used by other modules.
        .pipe(eslint())
        // eslint.format() outputs the lint results to the console.
        // Alternatively use eslint.formatEach() (see Docs).
        .pipe(eslint.format())
        // To have the process exit with an error code (1) on
        // lint error, return the stream and pipe to failAfterError last.
        .pipe(eslint.failAfterError());
});


gulp.task("watch", function(event) {
    var watcher1 = gulp.watch(["./manager/**/*.js"], ["bundle-manager"]);
    watcher1.on('change', function(event) {
        gutil.log('File changed: ' + event.path);
    });
    var watcher1 = gulp.watch(["./components/*.js"], ["bundle-manager"]);
    watcher1.on('change', function(event) {
        gutil.log('File changed: ' + event.path);
    })
})

gulp.task('default', ['prod-opts', 'bundle-manager']);

gulp.task('devel', ['devel-opts', 'bundle-manager']);

gulp.task('devel-watch', ['devel-opts', 'watch', 'bundle-manager']);
