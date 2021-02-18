package com.test.chat.service.api;

import java.io.InputStream;

public interface FileStorageService {

    String saveFile(InputStream content, String fileName);

    InputStream getFileContent(String path);

}
