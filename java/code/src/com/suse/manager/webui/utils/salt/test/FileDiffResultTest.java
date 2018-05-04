package com.suse.manager.webui.utils.salt.test;

import com.google.gson.reflect.TypeToken;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.webui.utils.salt.custom.FilesDiffResult;
import com.suse.utils.Json;

import java.util.Map;
import java.util.Optional;

public class FileDiffResultTest extends JMockBaseTestCaseWithUser  {
    static final String JSON_FILE_DIFF_RESPONSE = "dummy_files_diff_res.json";
    public void testFileDiffResult() throws Exception {
        String jsonResult = TestUtils.readAll(TestUtils.findTestData(JSON_FILE_DIFF_RESPONSE));
        TypeToken<Map<String, FilesDiffResult>> typeToken = new TypeToken<Map<String, FilesDiffResult>>() { };
        Map<String, FilesDiffResult> results = Json.GSON.fromJson(jsonResult, typeToken.getType());

        //File exist on client with same contents
        FilesDiffResult fileDiffResult = results.get("file_|-file_deploy_2_|-/tmp/newsql.sql_|-managed");
        Optional<String> newfile = fileDiffResult.getPChanges(FilesDiffResult.FileResult.class).getNewfile();
        assertTrue(fileDiffResult.isResult());
        assertFalse(newfile.isPresent());

        //File exist on client with different contents
        fileDiffResult = results.get("file_|-file_deploy_1_|-/tmp/sub-trd.sql_|-managed");
        newfile = fileDiffResult.getPChanges(FilesDiffResult.FileResult.class).getNewfile();
        assertFalse(fileDiffResult.isResult());
        assertFalse(newfile.isPresent());

        //File doesn't exist on client already
                fileDiffResult = results.get("file_|-file_deploy_6_|-/tmp/gitrec_|-managed");
        newfile = fileDiffResult.getPChanges(FilesDiffResult.FileResult.class).getNewfile();
        assertFalse(fileDiffResult.isResult());
        assertTrue(newfile.isPresent());

        //symlinks

        //symlink doesn't exist on system already
        fileDiffResult = results.get("file_|-file_deploy_3_|-/billa/billa.sql_|-symlink");
        Optional<String> newSymlink = fileDiffResult.getPChanges(FilesDiffResult.SymLinkResult.class).getNewSymlink();
        assertFalse(fileDiffResult.isResult());
        assertTrue(newSymlink.isPresent());

        //Directory doesnt't exist on client
        fileDiffResult = results.get("file_|-file_deploy_7_|-/tmp/testdir_|-directory");
        TypeToken<Map<String, FilesDiffResult.DirectoryResult>> typeTokenD =
                new TypeToken<Map<String, FilesDiffResult.DirectoryResult>>() { };
        FilesDiffResult.DirectoryResult dirPchanges = fileDiffResult.getPChanges(typeTokenD)
                .values()
                .stream()
                .findFirst()
                .get();
        Optional<String> newDir = dirPchanges.getDirectory();
        assertFalse(fileDiffResult.isResult());
        assertTrue(newDir.isPresent());

        //Directory exits exist on with different owner
        fileDiffResult = results.get("file_|-file_deploy_5_|-/tmp/testdir_|-directory");
        dirPchanges = fileDiffResult.getPChanges(typeTokenD)
                .values()
                .stream()
                .findFirst()
                .get();
        newDir = dirPchanges.getDirectory();
        Optional<String> user = dirPchanges.getUser();
        Optional<String> mode = dirPchanges.getMode();

        assertFalse(fileDiffResult.isResult());
        assertFalse(newDir.isPresent());
        assertEquals("root", user.get());
        assertEquals("0644", mode.get());
    }
}
