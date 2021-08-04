package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.dto.StatisticDTO;
import com.wjk.halo.model.dto.StatisticWithUserDTO;
import com.wjk.halo.service.StatisticService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/statistics")
public class StatisticController {

    private final StatisticService statisticService;

    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    //获取博客信息：博客数，评论数，博客基本信息等
    @GetMapping
    public StatisticDTO statistics(){
        return statisticService.getStatistic();
    }

    //除了上面的信息，另外再获取当前用户信息
    @GetMapping("user")
    public StatisticWithUserDTO statisticWithUser(){
        return statisticService.getStatisticWithUser();
    }

}
