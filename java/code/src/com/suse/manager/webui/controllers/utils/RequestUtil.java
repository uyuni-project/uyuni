package com.suse.manager.webui.controllers.utils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import spark.Request;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class RequestUtil {
    private RequestUtil() { }

    public static List<FileItem> parseMultipartRequest(Request request) {
        DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
        ServletContext servletContext = request.raw().getServletContext();
        File repository = (File) servletContext
                .getAttribute("javax.servlet.context.tempdir");
        fileItemFactory.setRepository(repository);
        try {
            return new ServletFileUpload(fileItemFactory).parseRequest(request.raw());
        } catch (FileUploadException e) {
            throw new RuntimeException(e);
        }
    }
    public static Optional<String> findStringParam(List<FileItem> items, String name) {
        return findParamItem(items, name).map(FileItem::getString);
    }

    public static Optional<FileItem> findParamItem(List<FileItem> items, String name) {
        return items.stream()
                .filter(FileItem::isFormField)
                .filter(item -> name.equals(item.getFieldName()))
                .findFirst();
    }
    public static Optional<FileItem> findFileItem(List<FileItem> items, String name) {
        return items.stream()
                .filter(item -> !item.isFormField())
                .filter(item -> name.equals(item.getFieldName()))
                .filter(item -> item.getSize() > 0)
                .findFirst();
    }
}
