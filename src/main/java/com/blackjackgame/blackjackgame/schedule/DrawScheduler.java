package com.blackjackgame.blackjackgame.schedule;

import com.blackjackgame.blackjackgame.model.BetResult;
import com.blackjackgame.blackjackgame.model.DrawResult;
import com.blackjackgame.blackjackgame.service.BetService;
import com.blackjackgame.blackjackgame.service.DrawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DrawScheduler {


    @Autowired
    private DrawService drawService;

    @Autowired
    private BetService betService;

    @Scheduled(fixedDelay = 30000, initialDelay = 30000)
    public void autoDrawAndSettle() {
        // 進行開獎，並取得 DrawResult 包含號碼與期數
        DrawResult draw = drawService.performDrawAndGetResult();

        // 用本期號碼結算下注
        List<BetResult> winners = betService.settleBets(draw.getIssueNumber(), draw.getNumbers());

        log.info("新一期開獎：期數 {} 號碼 {}", draw.getIssueNumber(), draw.getNumbers());

        for (BetResult winner : winners) {
            if (winner.getMatchCount() > 0) {
                log.info("🎉 {} 中 {} 個號碼：{}", winner.getUser(), winner.getMatchCount(), winner.getNumbers());
            }
        }
    }
}
