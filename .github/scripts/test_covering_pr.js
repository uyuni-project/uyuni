const Redis = require('ioredis');

const main = async () => {
    const changedFiles = process.argv.slice(2);
    const redis = new Redis({
        host: process.env.REDIS_HOST,
        port: process.env.REDIS_PORT,
        username: process.env.REDIS_USER,
        password: process.env.REDIS_PASS,
        enableReadyCheck: false
    });

    /**
     * Loop over all the files changed in the PR and check if it's covered by a test
     **/
    try {
        const tests = new Set();

        for (const filepath of changedFiles) {
            const classpath = filepath.replace('java/code/src/', '');
            const testNames = await redis.smembers(classpath);
            testNames.forEach(test => tests.add(test));
        }

        console.log(Array.from(tests).join(','));
    } catch (error) {
        console.error('Error:', error);
        process.exitCode = 1;
    } finally {
        await redis.quit();
    }
};

main().catch(error => {
    console.error('Unexpected error:', error);
    process.exitCode = 1;
});
