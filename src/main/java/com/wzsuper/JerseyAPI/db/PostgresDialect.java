package com.wzsuper.JerseyAPI.db;

public class PostgresDialect extends Dialect {

    public PostgresDialect() {
    }

    @Override
    public String getCountSQL(String sql) {
        return String.format("select count(1) from(%s) a", sql);
    }

    @Override
    public String getPageSQL(String sql, int offset, int limit) {
        return String.format("%s limit %s offset %s", sql, limit, offset);
    }
}