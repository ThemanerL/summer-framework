package cn.cerc.mis.tools;

import cn.cerc.core.IHandle;
import cn.cerc.db.mysql.SqlQuery;
import cn.cerc.mis.core.Application;
import cn.cerc.mis.core.ISystemTable;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 扫描待翻译的中文
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExportChinese {

    private Set<String> items = new TreeSet<>();

    private static String getChinese(String temp) {
        int index = temp.indexOf("R.asString");
        if (index > -1) {
            String s1 = temp.substring(index);
            if (s1.indexOf("\"") > -1) {
                String s2 = s1.substring(s1.indexOf("\"") + 1);
                if (s2.indexOf("\")") > -1) {
                    String s3 = s2.substring(0, s2.indexOf("\")"));
                    if (s3.length() > 0) {
                        return s3;
                    }
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        ExportChinese ec = new ExportChinese();
        // 扫描指定目录下所有的java文件
        ec.scanFile("C:\\Users\\l1091\\Documents\\i-work\\fc-project\\fc-app\\src\\main\\java");
        // 将扫描的结果存入到数据库
        // ec.writeDict(new AppHandle());
        System.out.println(new Gson().toJson(ec.getItems()));
    }

    /**
     * 扫描指定路径的java文件
     *
     * @param srcPath 路径
     */
    public void scanFile(String srcPath) {

        // 调用查找文件方法
        List<File> files = loadFiles(new File(srcPath), "java");

        // 循环出文件
        for (File file : files) {
            // 再查找java文件中的字符串
            BufferedReader buffReader = null;
            String line = "";
            try {
                // 输入流
                buffReader = new BufferedReader(new FileReader(file));
                // 按行读取
                while ((line = buffReader.readLine()) != null) {
                    String word = getChinese(line);
                    if (word != null) {
                        log.info("{} {}", file.getName(), word);
                        items.add(word);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    buffReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 写入字典
     *
     * @param handle 上下文环境
     */
    public void writeDict(IHandle handle) {
        ISystemTable systemTable = Application.getBean("systemTable", ISystemTable.class);
        SqlQuery ds = new SqlQuery(handle);
        ds.add("select * from %s", systemTable.getLangDict());
        ds.open();
        for (String text : this.getItems()) {
            if (!ds.locate("cn_", text)) {
                ds.append();
                ds.setField("cn_", text);
                ds.post();
            }
        }
    }

    public Set<String> getItems() {
        return this.items;
    }

    // 查找文件
    private List<File> loadFiles(File fileDir, String fileType) {
        List<File> lfile = new ArrayList<File>();
        File[] fs = fileDir.listFiles();
        for (File f : fs) {
            if (f.isFile()) {
                if (fileType.equals(f.getName().substring(f.getName().lastIndexOf(".") + 1))) {
                    lfile.add(f);
                }
            } else {
                List<File> ftemps = loadFiles(f, fileType);
                lfile.addAll(ftemps);
            }
        }
        return lfile;
    }

}
