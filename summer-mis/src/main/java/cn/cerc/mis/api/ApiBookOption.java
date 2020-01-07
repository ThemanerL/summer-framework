package cn.cerc.mis.api;

import cn.cerc.core.Record;
import cn.cerc.core.Utils;
import cn.cerc.db.mysql.BuildQuery;
import cn.cerc.db.mysql.SqlQuery;
import cn.cerc.mis.core.CustomService;
import cn.cerc.mis.core.DataValidateException;

public class ApiBookOption extends CustomService {

    /**
     * 根据帐套代码、参数代码获取参数值
     */
    public boolean getValue() throws DataValidateException {
        Record headIn = getDataIn().getHead();

        DataValidateException.stopRun("帐套代码不允许为空", !headIn.hasValue("CorpNo_"));
        String corpNo = Utils.safeString(headIn.getString("CorpNo_"));

        DataValidateException.stopRun("参数代码不允许为空", !headIn.hasValue("Code_"));
        String code = Utils.safeString(headIn.getString("Code_"));

        BuildQuery f = new BuildQuery(this);
        f.add("select Value_ from %s ", systemTable.getBookOptions());
        f.byField("CorpNo_", corpNo);
        f.byField("Code_", code);
        SqlQuery cdsTmp = f.open();

        getDataOut().appendDataSet(cdsTmp);
        return true;
    }

}
