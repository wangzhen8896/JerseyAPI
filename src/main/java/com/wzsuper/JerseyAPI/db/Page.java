package com.wzsuper.JerseyAPI.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by wangzhen on 2017/2/22.
 */
public class Page<T> {

    private static final Logger logger = LoggerFactory.getLogger(Page.class);

    public static final int DEFAULT_PAGESIZE = 10;

    private int pageSize = DEFAULT_PAGESIZE;
    private int pageNum = 1;
    private int pageCount = 0;
    private int count = 0;

    private boolean selectCount = false;

    private List<T> result;

    public Page(int pageNum, int pageSize){
        if(pageNum > 1){
            this.pageNum = pageNum;
        }
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public int getOffset(){
        return  (this.pageNum - 1) * pageSize;
    }













}
