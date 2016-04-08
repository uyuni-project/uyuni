/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt.custom;

public class CmdExecCodeAllResult {

    private long pid;
    private int retcode;
    private String stderr;
    private String stdout;

    public long getPid() {
        return pid;
    }

    public int getRetcode() {
        return retcode;
    }

    public String getStderr() {
        return stderr;
    }

    public String getStdout() {
        return stdout;
    }
}
