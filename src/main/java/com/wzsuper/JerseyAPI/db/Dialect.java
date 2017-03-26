package com.wzsuper.JerseyAPI.db;

/**
 * Created by wangzhen on 2017/2/22.
 */
public abstract class Dialect {

    public static enum DataBaseType{
        MYSQL,
        ORACLE,
        POSTGRES
    }

    public abstract String getCountSQL(String sql);

    public abstract String getPageSQL(String sql, int offset, int limit);
}
