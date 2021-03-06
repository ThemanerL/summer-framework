package cn.cerc.mis.rds;

import cn.cerc.core.IHandle;
import cn.cerc.db.core.ServerConfig;
import cn.cerc.db.jiguang.JiguangConnection;
import cn.cerc.db.mysql.MysqlConnection;
import cn.cerc.db.mysql.SlaveMysqlConnection;
import cn.cerc.db.mysql.SqlConnection;
import cn.cerc.db.queue.AliyunQueueConnection;
import cn.cerc.mis.core.Application;

public class StubHandle implements IHandle, AutoCloseable {
    public static final String DefaultBook = "999001";
    public static final String DefaultUser = DefaultBook + "01";
    public static final String DefaultProduct = "999001000001";

    // 生产部
    public static final String DefaultDept = "10050001";
    public static final String clientIP = "127.0.0.1";
    private IHandle handle;

    public StubHandle() {
        handle = Application.getHandle();
        handle.init(DefaultBook, DefaultUser, clientIP);
    }

    public StubHandle(String corpNo, String userCode) {
        handle = Application.getHandle();
        handle.init(corpNo, userCode, clientIP);
    }

    public SqlConnection getConnection() {
        return (MysqlConnection) handle.getProperty(MysqlConnection.sessionId);
    }

    @Override
    public String getCorpNo() {
        return handle.getCorpNo();
    }

    @Override
    public String getUserCode() {
        return handle.getUserCode();
    }

    @Override
    public String getUserName() {
        return handle.getUserName();
    }

    @Override
    public Object getProperty(String key) {
        if ("request".equals(key)) {
            return null;
        }
        Object obj = handle.getProperty(key);
        if (obj == null && MysqlConnection.sessionId.equals(key)) {
            MysqlConnection conn = new MysqlConnection();
            conn.setConfig(ServerConfig.getInstance());
            handle.setProperty(key, conn);
        }
        if (obj == null && SlaveMysqlConnection.sessionId.equals(key)) {
            SlaveMysqlConnection conn = new SlaveMysqlConnection();
            conn.setConfig(ServerConfig.getInstance());
            handle.setProperty(key, conn);
        }

        if (obj == null && AliyunQueueConnection.sessionId.equals(key)) {
            AliyunQueueConnection conn = new AliyunQueueConnection();
            conn.setConfig(ServerConfig.getInstance());
            handle.setProperty(key, conn);
        }
        if (obj == null && JiguangConnection.sessionId.equals(key)) {
            JiguangConnection conn = new JiguangConnection();
            conn.setConfig(ServerConfig.getInstance());
            handle.setProperty(key, conn);
        }
        return obj;
    }

    @Override
    public void setProperty(String key, Object value) {
        throw new RuntimeException("调用了未被实现的接口");
    }

    @Override
    public boolean init(String bookNo, String userCode, String clientCode) {
        throw new RuntimeException("调用了未被实现的接口");
    }

    @Override
    public boolean init(String token) {
        throw new RuntimeException("调用了未被实现的接口");
    }

    @Override
    public boolean logon() {
        return false;
    }

    @Override
    public void close() {
        handle.close();
    }

}
