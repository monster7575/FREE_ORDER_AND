package com.favinet.freeorder.data.model;

import java.util.ArrayList;
import java.util.List;

public class UploadCon extends ResponseData {
    public List<attach> data;

    public UploadCon()
    {
        data = new ArrayList<>();
    }

    public class attach
    {
        String path;

        public String getPath() {
            return path;
        }
    }
}

