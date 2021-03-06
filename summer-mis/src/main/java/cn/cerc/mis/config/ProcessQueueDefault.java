package cn.cerc.mis.config;

import cn.cerc.db.core.ServerConfig;
import cn.cerc.db.mysql.BatchScript;
import cn.cerc.db.queue.QueueDB;
import cn.cerc.db.queue.QueueMode;
import cn.cerc.db.queue.QueueQuery;
import cn.cerc.mis.core.BookHandle;
import cn.cerc.mis.core.LocalService;
import cn.cerc.mis.message.MessageProcess;
import cn.cerc.mis.task.AbstractTask;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

@Slf4j
public class ProcessQueueDefault extends AbstractTask {

    public static void main(String[] args) {
        ProcessQueueDefault obj = new ProcessQueueDefault();
        obj.run();
    }

    @Override
    public void execute() throws Exception {
        QueueQuery query = new QueueQuery(this);
        query.setQueueMode(QueueMode.recevie);
        query.add("select * from %s ", QueueDB.SUMMER);
        query.open();
        if (!query.getActive()) {
            return;
        }

        // 建立服务执行环境
        String corpNo = query.getHead().getString("_corpNo_");
        if ("".equals(corpNo)) {
            log.error("_corpNo_ is null");
            return;
        }

        String userCode = query.getHead().getString("_userCode_");
        if ("".equals(userCode)) {
            log.error("_userCode_ is null");
            return;
        }

        String service = query.getHead().getString("_service_");
        if ("".equals(service)) {
            log.error("_service_ is null");
            return;
        }

        // 调用队列内容中指定的服务
        BookHandle bh = new BookHandle(this, corpNo);
        bh.setUserCode(userCode);

        LocalService svr = new LocalService(bh);
        svr.setService(service);
        svr.getDataIn().appendDataSet(query, true);

        String msgId = query.getHead().getString("_queueId_");
        JSONObject content = JSONObject.fromObject(query.getHead().getString("_content_"));

        // 更新消息状态
        BatchScript bs = new BatchScript(this);
        if (svr.exec()) {
            bs.add("update %s set Process_=%s,Content_='%s' where UID_=%s", systemTable.getUserMessages(),
                    MessageProcess.ok.ordinal(), content.toString(), msgId);
        } else {
            bs.add("update %s set Process_=%s,Content_='%s' where UID_=%s", systemTable.getUserMessages(),
                    MessageProcess.wait.ordinal(), content.toString(), msgId);
        }
        bs.exec();

        // 移除队列
        query.remove();
    }

    @Override
    public void run() {
        if (ServerConfig.enableTaskService()) {
            super.run();
        }
    }
}
