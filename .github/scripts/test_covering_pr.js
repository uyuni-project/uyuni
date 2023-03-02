const Redis = require('ioredis');
const fs = require('fs').promises;

const main = async () => {
    const changedFiles = process.argv.slice(2)
    const redis = new Redis({
        host: process.env.REDIS_HOST,
        port: process.env.REDIS_PORT,
        username: process.env.REDIS_USER,
        password: process.env.REDIS_PASS,
        enableReadyCheck: false
    });
    let tests = new Set();

    /**
     * Loop over all the files changed in the PR and check if it's covered by a test
     **/
    for (const filepath of changedFiles) {
        var classpath = filepath.replace('java/code/src/', '');
        await redis.smembers(classpath, function(err, test_names) {
            test_names.forEach(function(test) {
                tests.add(test);
            });
        });
    }

    console.log('<details><summary>Suggested tests to cover this Pull Request</summary><ul><li>%s</ul></details><suggested_tests>', Array.from(tests).join('<li>'));
    process.exit(0);
}
main();
