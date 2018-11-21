package cn.jerry.test.file;

public interface IContentFilter {
    public boolean canBeIgnored(String content);
}
