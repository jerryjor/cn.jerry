package cn.jerry.fabric.api.fabric.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ListResult<T extends Serializable> extends Result<ArrayList<T>> {
    private static final long serialVersionUID = 3576356845725428115L;
    private ArrayList<T> data;

    public ListResult() {
        super();
    }

    public ListResult(String code, String message) {
        super(code, message);
    }

    public ArrayList<T> getData() {
        return data;
    }

    public void setData(ArrayList<T> data) {
        this.data = data;
    }

}
