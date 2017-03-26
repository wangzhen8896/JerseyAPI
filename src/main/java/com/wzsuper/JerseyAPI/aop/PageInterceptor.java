package com.wzsuper.JerseyAPI.aop;

import com.wzsuper.JerseyAPI.db.Dialect;
import com.wzsuper.JerseyAPI.db.Page;
import com.wzsuper.JerseyAPI.db.PostgresDialect;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;
import java.util.Properties;


@Intercepts(value =
        {@Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }),
         @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})}
)
public class PageInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(PageInterceptor.class);

    private String DataBaseType;

    public static final ThreadLocal<Page> localPage = new ThreadLocal<Page>();

    /**
     * 开始分页
     * @param pageNum
     * @param pageSize
     */
    public static void startPage(int pageNum, int pageSize) {
        localPage.set(new Page(pageNum, pageSize));
    }

    /**
     * 结束分页并返回结果，该方法必须被调用，否则localPage会一直保存下去，直到下一次startPage
     * @return
     */
    public static Page endPage() {
        Page page = localPage.get();
        localPage.remove();
        return page;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (invocation.getTarget() instanceof StatementHandler) {
            Page page = localPage.get();
            if(page != null){
                StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
                MetaObject metaStatementHandler = SystemMetaObject.forObject(statementHandler);
                // 分离代理对象链(由于目标类可能被多个拦截器拦截，从而形成多次代理，通过下面的两次循环
                // 可以分离出最原始的的目标类)
                while (metaStatementHandler.hasGetter("h")) {
                    Object object = metaStatementHandler.getValue("h");
                    metaStatementHandler = SystemMetaObject.forObject(object);
                }
                // 分离最后一个代理对象的目标类
                while (metaStatementHandler.hasGetter("target")) {
                    Object object = metaStatementHandler.getValue("target");
                    metaStatementHandler = SystemMetaObject.forObject(object);
                }
                MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");

                BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
                String sql = boundSql.getSql();
                Dialect dialect = new PostgresDialect();

                String countSQL = dialect.getCountSQL(sql);
                Connection connection = (Connection) invocation.getArgs()[0];
                getCount(countSQL, connection, mappedStatement, boundSql, page);

                String pageSQL =  dialect.getPageSQL(sql, page.getOffset(), page.getPageSize());
                Field field = BoundSql.class.getDeclaredField("sql");
                field.setAccessible(true);
                field.set(boundSql, pageSQL);
            }
        }else if (invocation.getTarget() instanceof ResultSetHandler) {
            Object result = invocation.proceed();
            Page page = localPage.get();
            if(page != null) {
                page.setResult((List) result);
            }
            return result;
        }
        return invocation.proceed();

    }


    /**
     * 获取总记录数
     * @param sql
     * @param connection
     * @param mappedStatement
     * @param boundSql
     * @param page
     */
    private void getCount(String sql, Connection connection, MappedStatement mappedStatement,
                                  BoundSql boundSql, Page page) {
        PreparedStatement countStmt = null;
        ResultSet rs = null;
        try {
            countStmt = connection.prepareStatement(sql);
            BoundSql countBS = new BoundSql(mappedStatement.getConfiguration(), sql, boundSql.getParameterMappings(), boundSql.getParameterObject());
            ParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, boundSql.getParameterObject(), countBS);
            parameterHandler.setParameters(countStmt);
            rs = countStmt.executeQuery();
            int totalCount = 0;
            if (rs.next()) {
                totalCount = rs.getInt(1);
            }
            page.setCount(totalCount);
            int totalPage = totalCount / page.getPageSize() + ((totalCount % page.getPageSize() == 0) ? 0 : 1);
            page.setPageCount(totalPage);
        } catch (SQLException e) {
            logger.error("Ignore this exception", e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("Ignore this exception", e);
            }
            try {
                countStmt.close();
            } catch (SQLException e) {
                logger.error("Ignore this exception", e);
            }
        }
    }



    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler || target instanceof ResultSetHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}  