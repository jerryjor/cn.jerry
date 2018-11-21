package cn.jerry.test.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyFileReader {
    private List<IContentFilter> filters = initFilters();

    private List<IContentFilter> initFilters() {
        filters = new ArrayList<IContentFilter>();
        filters.add(new LogContentFilter());
        return filters;
    }

    public File getFile(String path, String name) {
        boolean endFlag = path.endsWith("/");
        File file = new File(path + (endFlag ? "" : "/") + name);
        return file.exists() ? file : null;
    }

    public List<String> readContent(File file) {
        List<String> content = new ArrayList<String>();

        FileReader fr = null;
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);
        try {
            String line = "";
            boolean ignored = false;
            while ((line = br.readLine()) != null) {
                for (IContentFilter filter : filters) {
                    ignored = filter.canBeIgnored(line);
                    if (ignored) break;
                }
                if (!ignored) {
                    content.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }
}
