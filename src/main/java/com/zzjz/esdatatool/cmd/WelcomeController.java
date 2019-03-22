package com.zzjz.esdatatool.cmd;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 房桂堂
 * @description WelcomeController
 * @date 2019/3/22 13:37
 */
@Controller
public class WelcomeController {

    @RequestMapping("/welcome")
    public String welcome() {
        return "welcome";
    }
}
