package com.wjk.halo.service;

import com.wjk.halo.model.dto.StatisticDTO;
import com.wjk.halo.model.dto.StatisticWithUserDTO;

public interface StatisticService {

    StatisticDTO getStatistic();

    StatisticWithUserDTO getStatisticWithUser();
}
