package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.LocalCall;

import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * Created by matei on 2/17/17.
 */
public class Openscap {

    public static class OpenscapResult {

        @SerializedName("error")
        private String error;

        @SerializedName("success")
        private boolean success;

        @SerializedName("upload_dir")
        private String uploadDir;

        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getUploadDir() {
            return uploadDir;
        }
    }

    private Openscap() { }

    public static LocalCall<OpenscapResult> xccdf(String parameters) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        args.put("params", parameters);
        return new LocalCall<>("openscap.xccdf", Optional.empty(), Optional.of(args),
                new TypeToken<OpenscapResult>() { });
    }

}
