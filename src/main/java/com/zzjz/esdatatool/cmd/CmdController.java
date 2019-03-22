package com.zzjz.esdatatool.cmd;

import com.zzjz.esdatatool.task.CmdRunner;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
     * 备份es表
     * @param index 索引名
     * @param dates 日期(可以是2019.10.01也可以是2018.10.01-2018.10.12)
     * @return 结果
     */
    @RequestMapping(value = "backup/{index}/{dates}", method = RequestMethod.GET)
    public String backup(@PathVariable("index") String index, @PathVariable("dates") String dates) throws InterruptedException {
        System.out.println("准备执行backup,index为" + index + ",datas为" + dates);
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy.MM.dd");
        //5个线程去跑命令
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        if (dates.split("-").length > 0) {
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
                        "  --input=http://es1:9200/" + index + "-" + startTime.toString(format) + " \\\n" +
                        "  --output=/tmp/" + index + "-" + startTime.toString(format) + ".json \\\n" +
                        "  --type=data";
                CmdRunner command = new CmdRunner(cmd);
                executorService.submit(command);
                startTime = startTime.plusDays(1);
            }
        } else {
            String cmd = "docker run --rm --net=host -v " + dumpDst + ":/tmp " + dumpImg +" \\\n" +
                    "  --input=http://es1:9200/" +index + "-" + dates + " \\\n" +
                    "  --output=/tmp/" + index + "-" + dates + ".json \\\n" +
                    "  --type=data";
            CmdRunner command = new CmdRunner(cmd);
            executorService.submit(command);
        }
        System.out.println("调用了shutdown");
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) {
                System.out.println("所有的子线程都结束了,任务完成");
                break;
            }
            Thread.sleep(1000);
        }
        return "备份任务下发成功!";
    }

}
