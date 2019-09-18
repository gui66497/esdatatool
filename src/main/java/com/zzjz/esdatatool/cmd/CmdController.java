package com.zzjz.esdatatool.cmd;

import com.zzjz.esdatatool.bean.EsEntity;
import com.zzjz.esdatatool.task.CmdRunner;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 房桂堂
 * @description CmdController
 * @date 2019/3/21 14:53
 */
@RestController
public class CmdController {

    @Value("${dumpDst}")
    String dumpDst;

    @Value("${dumpImg}")
    String dumpImg;

    @Value("${threadNum}")
    int threadNum;

    /**
     * 批量备份es表
     * @param esEntity esEntity
     * @return 结果
     */
    @RequestMapping(value = "backup", method = RequestMethod.POST)
    public String backup(@RequestBody EsEntity esEntity) throws InterruptedException, IOException {
        String index = esEntity.getIndex();
        String dates = esEntity.getDates();
        String separator = esEntity.getSeparator();
        String esCon = esEntity.getEsCon();
        String msg = "准备执行backup,index为" + index + ",datas为" + dates;
        //sendMessageToAll(msg);
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy.MM.dd");
        //5个线程去跑命令
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        if (dates.contains("-")) {
            String startDate = dates.split("-")[0];
            String endDate = dates.split("-")[1];
            System.out.println("startDate:" + startDate);
            System.out.println("endDate:" + endDate);
            DateTime startTime = DateTime.parse(startDate, format);
            DateTime endTime = DateTime.parse(endDate, format);
            while (startTime.isBefore(endTime) || startTime.isEqual(endTime)) {
                //通过java调用docker命令行时切记要关闭-it参数
                //有-t会报cannot enable tty mode on non tty input,即 在非TTY输入上不能启用TTY模式
                //有-i会导致命令执行完但不释放线程,导致线程卡死
                String cmd = "docker run --rm --net=host -v " + dumpDst + ":/tmp " + dumpImg +" \\\n" +
                        "  --input=" + esCon + "/"  + index + separator + startTime.toString(format) + " \\\n" +
                        "  --output=/tmp/" + index + "-" + startTime.toString(format) + ".json \\\n" +
                        "  --type=data";
                CmdRunner command = new CmdRunner(cmd);
                executorService.submit(command);
                startTime = startTime.plusDays(1);
            }
        } else {
            String cmd = "docker run --rm --net=host -v " + dumpDst + ":/tmp " + dumpImg +" \\\n" +
                    "  --input=" + esCon + "/" +index + separator + dates + " \\\n" +
                    "  --output=/tmp/" + index + "-" + dates + ".json \\\n" +
                    "  --type=data";
            CmdRunner command = new CmdRunner(cmd);
            executorService.submit(command);
        }
        System.out.println("调用了shutdown");
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) {
                String overMsg = "所有的子线程都结束了,任务完成";
                //sendMessageToAll(overMsg);
                break;
            }
            Thread.sleep(1000);
        }
        return "备份任务下发成功!";
    }

    /**
     * todo 需要把mapping先导过去
     * @param esEntity esEntity
     * @return
     * @throws InterruptedException
     */
    @RequestMapping(value = "reindex", method = RequestMethod.POST)
    public String reindex(@RequestBody EsEntity esEntity) throws InterruptedException {
        String index = esEntity.getIndex();
        String dates = esEntity.getDates();
        String separator = esEntity.getSeparator();
        String esCon = esEntity.getEsCon();
        String targetEsCon = esEntity.getTargetEsCon();

        String msg = "准备执行reindex,index为" + index + ",datas为" + dates;
        System.out.println(msg);
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy.MM.dd");
        //5个线程去跑命令
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);

        if (dates.contains("-")) {
            String startDate = dates.split("-")[0];
            String endDate = dates.split("-")[1];
            System.out.println("startDate:" + startDate);
            System.out.println("endDate:" + endDate);
            DateTime startTime = DateTime.parse(startDate, format);
            DateTime endTime = DateTime.parse(endDate, format);
            while (startTime.isBefore(endTime) || startTime.isEqual(endTime)) {
                // 1.先执行mapping的导入
                String mappingCmd = getCmdStr(esEntity, "mapping", startTime.toString(format));
                runCmd(mappingCmd);
                // 2.执行data的导入
                String cmd = getCmdStr(esEntity, "data", startTime.toString(format));
                CmdRunner command = new CmdRunner(cmd);
                executorService.submit(command);
                startTime = startTime.plusDays(1);
            }
        } else {
            // 1.先执行mapping的导入
            String mappingCmd = getCmdStr(esEntity, "mapping", dates);
            /*String mappingCmd = "docker run --rm --net=host -v " + dumpDst + ":/tmp " + dumpImg + " \\\n" +
                    "  --input=" + esCon + "/" + index + separator + dates + " \\\n" +
                    "  --output=" + targetEsCon + "/" + index + separator + dates + " \\\n" +
                    "  --type=mapping";*/
            runCmd(mappingCmd);
            // 2.执行data的导入
            String cmd = getCmdStr(esEntity, "data", dates);
            /*String cmd = "docker run --rm --net=host -v " + dumpDst + ":/tmp " + dumpImg +" \\\n" +
                    "  --input=" + esCon + "/" +index + separator + dates + " \\\n" +
                    "  --output=" + targetEsCon + "/" + index + separator + dates + " \\\n" +
                    "  --type=data";*/
            CmdRunner command = new CmdRunner(cmd);
            executorService.submit(command);
        }
        System.out.println("调用了shutdown");
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) {
                String overMsg = "所有的子线程都结束了,任务完成";
                System.out.println(overMsg);
                break;
            }
            Thread.sleep(1000);
        }
        return "迁移任务下发成功!";

    }

    /**
     * 获取cmd命令.
     * @param esEntity es实体
     * @param type 类型（data或mapping）
     * @param date 日期
     * @return 命令
     */
    private String getCmdStr(EsEntity esEntity, String type, String date) {
        return "docker run --rm --net=host -v " + dumpDst + ":/tmp " + dumpImg + " \\\n" +
                "  --input=" + esEntity.getEsCon() + "/" + esEntity.getIndex() + esEntity.getSeparator() + date + " \\\n" +
                "  --output=" + esEntity.getTargetEsCon() + "/" + esEntity.getIndex() + esEntity.getSeparator() + date + " \\\n" +
                "  --type=" + type;
    }

    public void runCmd(String cmd) {
        Process process;
        ProcessBuilder pbuilder;
        try {
            System.out.println("cmd命令为" + cmd);
            pbuilder = new ProcessBuilder(cmd.split("\\s+"));
            pbuilder.redirectErrorStream(true);
            process = pbuilder.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));
            System.out.println("调用Input");
            String line = "";
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
            System.out.println(Thread.currentThread().getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 群发消息至每个客户端
     * @param msg 消息
     * @throws IOException IOException
     */
    public static void sendMessageToAll(String msg) throws IOException {
        System.out.println(msg);
        CopyOnWriteArraySet<MyWebSocket> webSockets = MyWebSocket.getWebSocket();
        for (MyWebSocket myWebSocket : webSockets) {
            myWebSocket.sendMessage(msg);
        }
    }
}
