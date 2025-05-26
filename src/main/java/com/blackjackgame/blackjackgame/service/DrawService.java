package com.blackjackgame.blackjackgame.service;

import com.blackjackgame.blackjackgame.model.DrawResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
public class DrawService {
    private final AtomicInteger currentIssue = new AtomicInteger(1);
    private final List<DrawResult> drawHistory = new ArrayList<>();
    private volatile long lastDrawTimestamp = System.currentTimeMillis();
    private static final DateTimeFormatter FILE_NAME_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    @Autowired
    private ObjectMapper objectMapper;


    public synchronized DrawResult performDrawAndGetResult() {
        int issue = currentIssue.getAndIncrement();

        List<Integer> pool = IntStream.rangeClosed(1, 42).boxed().collect(Collectors.toList());
        Collections.shuffle(pool);
        List<Integer> result = pool.subList(0, 3).stream().sorted().collect(Collectors.toList());

        DrawResult draw = new DrawResult();
        draw.setNumbers(result);
        draw.setDrawTime(LocalDateTime.now());
        draw.setIssueNumber(issue);

        // 紀錄本次開獎時間，用來計算倒數
        lastDrawTimestamp = System.currentTimeMillis();

        drawHistory.add(draw);
        this.saveToFile(draw);

        return draw;
    }

    // 新增方法取得目前（下一期）期數
    public int getCurrentIssue() {
        return currentIssue.get();
    }

    // 計算距離下一次開獎剩餘秒數 (假設每30秒開獎一次)
    public long getSecondsUntilNextDraw() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastDrawTimestamp;
        long interval = 30_000L; // 30秒
        long remaining = interval - elapsed;
        return remaining > 0 ? remaining / 1000 : 0;
    }

    private void saveToFile(DrawResult draw) {
        String date = draw.getDrawTime().toLocalDate().format(FILE_NAME_FORMAT);
        Path dir = Paths.get("draws");
        Path filePath = dir.resolve(date + ".txt");

        try {
            Files.createDirectories(dir);
            String json = objectMapper.writeValueAsString(draw);


            Files.write(
                    filePath,
                    Collections.singletonList(json),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            log.error("[DrawService] 儲存開獎記錄失敗", e);
        }
    }


    // 依期數取得指定的開獎結果，找不到回傳 null 或 Optional<DrawResult>
    public DrawResult getDrawResult(int issue) {
        return drawHistory.stream()
                .filter(draw -> draw.getIssueNumber() == issue)
                .findFirst()
                .orElse(null);
    }

    // 取得最近 count 期的開獎結果，若不足則全部回傳
    public List<DrawResult> getRecentResults(int count) {
        if (drawHistory.isEmpty()) {
            loadTodayDrawsFromFile();
        }

        int size = drawHistory.size();
        if (size == 0) return Collections.emptyList();

        int fromIndex = Math.max(0, size - count);
        return drawHistory.subList(fromIndex, size);
    }

    @PostConstruct
    public void init() {
        log.info("[DrawService] 初始化時讀取歷史檔案");
        loadTodayDrawsFromFile();
    }

    private void loadTodayDrawsFromFile() {
        String today = LocalDate.now().format(FILE_NAME_FORMAT);
        Path filePath = Paths.get("draws", today + ".txt");

        if (!Files.exists(filePath)) return;

        try (Stream<String> lines = Files.lines(filePath)) {
            lines.forEach(line -> {
                try {
                    DrawResult draw = objectMapper.readValue(line, DrawResult.class);
                    drawHistory.add(draw);

                    // 更新 currentIssue 為當前最大期數 + 1
                    if (draw.getIssueNumber() >= currentIssue.get()) {
                        currentIssue.set(draw.getIssueNumber() + 1);
                    }
                } catch (Exception e) {
                    log.warn("[DrawService] 無法解析行: " + line, e);
                }
            });
            log.info("[DrawService] 成功載入今日開獎記錄，共 {} 筆，下一期從 {} 開始",
                    drawHistory.size(), currentIssue.get());
        } catch (IOException e) {
            log.error("[DrawService] 無法讀取記錄檔: " + filePath, e);
        }
    }


}
