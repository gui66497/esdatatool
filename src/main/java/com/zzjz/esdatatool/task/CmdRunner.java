package com.zzjz.esdatatool.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @author 房桂堂
 * @description CmdRunner
 * @date 2019/3/21 14:09
 */
public class CmdRunner implements Runnable {

    /**
     * 命令
     */
    private String cmd;

    /**
     * 构造函数
     * @param cmd cmd
     */
    public CmdRunner(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public void run() {
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

}
