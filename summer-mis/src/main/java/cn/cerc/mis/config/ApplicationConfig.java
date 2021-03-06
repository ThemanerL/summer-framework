package cn.cerc.mis.config;

import cn.cerc.core.DataSet;
import cn.cerc.core.IHandle;
import cn.cerc.db.core.Curl;
import cn.cerc.db.core.ServerConfig;
import cn.cerc.mis.client.RemoteService;
import cn.cerc.mis.client.ServiceFactory;
import cn.cerc.mis.core.Application;
import cn.cerc.mis.core.ClientDevice;
import cn.cerc.mis.language.Language;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

@Slf4j
public class ApplicationConfig {

    /**
     * 本地主机
     */
    public static final String Local_Host = "http://127.0.0.1";

    public static final String App_Path = "/public/";

    public static String rewrite(String form) {
        return ApplicationConfig.App_Path + form;
    }

    /**
     * 服务器角色
     */
    public static final String App_Role_Key = "app.role";
    public static final String App_Role_Master = "master";
    public static final String App_Role_Replica = "replica";

    /**
     * 远程服务地址
     */
    public static final String Rempte_Host_Key = "remote.host";

    public static String getToken(IHandle handle) {
        return (String) handle.getProperty(Application.token);
    }

    public static boolean isMaster() {
        String appRole = ServerConfig.getInstance().getProperty(ApplicationConfig.App_Role_Key, ApplicationConfig.App_Role_Master);
        return ApplicationConfig.App_Role_Master.equals(appRole);
    }

    public static boolean isReplica() {
        return !ApplicationConfig.isMaster();
    }

    /**
     * 注册token信息到中央数据库
     */
    public static void registerToken(String userCode, String token) {
        Curl curl = new Curl();
        curl.put("userCode", userCode).put("token", token).put("machine", ServerConfig.getAppName());

        String host = RemoteService.getApiHost(ServiceFactory.Public);
        String site = host + ApplicationConfig.App_Path + "ApiTaskToken.register";
        String response = curl.doPost(site);
        log.warn("token {} 注册结果 {}", token, response);
    }

    /**
     * 获取用户授权令牌
     *
     * @param userCode    用户帐号
     * @param password    用户密码
     * @param machineCode 设备码
     */
    public static String getAuthToken(String userCode, String password, String machineCode) {
        // 构建public地址
        String host = RemoteService.getApiHost(ServiceFactory.Public);
        String url = host + ApplicationConfig.App_Path + "Login.getToken";

        // 构建登录请求参数
        DataSet dataIn = new DataSet();
        dataIn.getHead().setField("userCode", userCode);
        dataIn.getHead().setField("password", password);
        dataIn.getHead().setField("clientId", machineCode);
        dataIn.getHead().setField("device", ClientDevice.APP_DEVICE_PC);
        dataIn.getHead().setField("languageId", Language.zh_CN);
        String json = dataIn.getJSON();

        String token = null;
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost(url);
            StringEntity postingString = new StringEntity(json);
            post.setEntity(postingString);
            post.addHeader("Content-Type", "application/json;charset=utf-8");

            // 发起post请求
            HttpResponse response = client.execute(post);
            String content = EntityUtils.toString(response.getEntity(), "utf-8");
            log.info("返回数据 {}", content);

            // 解析post结果
            JSONObject object = JSONObject.fromObject(content);
            boolean result = object.getBoolean("result");
            String message = object.getString("message");
            if (!result) {
                log.error("用户 {} 初始化token失败", userCode);
                throw new RuntimeException(message);
            }

            // 取消外围 []，还原标准的dataSet格式
            String data = object.getString("data");
            data = data.substring(1, data.length() - 1);

            DataSet dataSet = new DataSet();
            dataSet.setJSON(data);

            token = dataSet.getHead().getString("token");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return token;
    }

}
