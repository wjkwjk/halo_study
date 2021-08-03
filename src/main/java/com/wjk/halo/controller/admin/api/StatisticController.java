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

    @GetMapping
    public StatisticDTO statistics(){
        return statisticService.getStatistic();
    }

    public StatisticWithUserDTO statisticWithUser(){
        return statisticService.getStatisticWithUser();
    }

}
