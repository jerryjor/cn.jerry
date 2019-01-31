package cn.jerry.fabric.chaincode.model;

import java.util.List;

public class PagedData {

    private int totalPages;
    private int currPage;
    private int pageSize;
    private List<String> data;

    public PagedData() {
    }

    public PagedData(int totalPages, int currPage, int pageSize, List<String> data) {
        this.totalPages = totalPages;
        this.currPage = currPage;
        this.pageSize = pageSize;
        this.data = data;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrPage() {
        return currPage;
    }

    public void setCurrPage(int currPage) {
        this.currPage = currPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PagedData{" +
                "totalPages=" + totalPages +
                ", currPage=" + currPage +
                ", pageSize=" + pageSize +
                '}';
    }
}
