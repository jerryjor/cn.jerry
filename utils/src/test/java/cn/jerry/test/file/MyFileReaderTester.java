package cn.jerry.test.file;

import java.io.File;
import java.util.List;

public class MyFileReaderTester {
    public static void main(String[] args) {
        MyFileReader reader = new MyFileReader();
        File file = reader.getFile("D:/", "simpleorder.log.2015-04-24");
        List<String> content = reader.readContent(file);
        for (String line : content) {
            System.out.println(line);
        }
    }
}
