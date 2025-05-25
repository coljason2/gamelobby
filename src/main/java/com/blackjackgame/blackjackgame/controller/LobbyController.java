package com.blackjackgame.blackjackgame.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Controller
class LobbyController {

    private final List<String> games = Arrays.asList("猜數字遊戲", "21點", "大老二", "剪刀石頭布", "擲骰子比大小", "記憶配對", "猜地雷", "圈圈叉叉", "擲骰競賽","長短比較遊戲","數數看");
    private final Random random = new Random();

    @GetMapping({"/lobby", "/"})
    public String lobby(Model model) {
        model.addAttribute("games", games);
        return "lobby";
    }

    @GetMapping("/lobby/game/{name}")
    public String launchGame(@PathVariable String name,
                             @RequestParam(name = "pairs", required = false, defaultValue = "3") int pairs,
                             @RequestParam(name = "size", required = false, defaultValue = "5") int size,
                             Model model, HttpSession session) {
        model.addAttribute("gameName", name);

        if ("猜數字遊戲".equals(name)) {
            int target = random.nextInt(100) + 1;
            session.setAttribute("targetNumber", target);
            log.info("targetNumber:{}", target);
            model.addAttribute("message", "請輸入 1~100 之間的數字");
            return "guess-number";
        }

        if ("剪刀石頭布".equals(name)) {
            return "rps-game";
        }

        if ("擲骰子比大小".equals(name)) {
            return "dice-game";
        }

        if ("擲骰競賽".equals(name)) {
            return "dice-competition-form";
        }

        if ("長短比較遊戲".equals(name)) {
            return "length-compare-game";
        }

        if ("數數看".equals(name)) {
            return "count";
        }

        if ("記憶配對".equals(name)) {
            return "memory-game";
        }

        if ("猜地雷".equals(name)) {
            int gridSize = size * size;
            int mineCount = size;
            Set<Integer> mines = new HashSet<>();
            while (mines.size() < mineCount) {
                mines.add(random.nextInt(gridSize));
            }
            session.setAttribute("mines", mines);
            session.setAttribute("revealed", new HashSet<Integer>());
            model.addAttribute("gridSize", gridSize);
            model.addAttribute("size", size);
            return "minefield-game";
        }

        if ("圈圈叉叉".equals(name)) {
            char[] board = new char[9];
            Arrays.fill(board, ' ');
            session.setAttribute("tttBoard", board);
            session.setAttribute("tttTurn", 'X');
            model.addAttribute("board", board);
            model.addAttribute("turn", 'X');
            return "tic-tac-toe";
        }


        return "game";
    }

    @PostMapping("/lobby/game/rps")
    public String handleRps(@RequestParam String choice, Model model) {
        String[] options = {"剪刀", "石頭", "布"};
        String computer = options[random.nextInt(3)];
        String result;

        if (choice.equals(computer)) {
            result = "平手！電腦出的是 " + computer;
        } else if ((choice.equals("剪刀") && computer.equals("布")) ||
                (choice.equals("石頭") && computer.equals("剪刀")) ||
                (choice.equals("布") && computer.equals("石頭"))) {
            result = "你贏了！電腦出的是 " + computer;
        } else {
            result = "你輸了！電腦出的是 " + computer;
        }

        model.addAttribute("gameName", "剪刀石頭布");
        model.addAttribute("result", result);
        return "rps-game";
    }

    @PostMapping("/lobby/game/guess")
    public String handleGuess(@RequestParam int guess, HttpSession session, Model model) {
        int target = (int) session.getAttribute("targetNumber");
        String message;

        if (guess < target) {
            message = "太小了！再試一次。";
        } else if (guess > target) {
            message = "太大了！再試一次。";
        } else {
            message = "恭喜你猜對了！目標數字是 " + target;
            session.removeAttribute("targetNumber");
        }

        model.addAttribute("gameName", "比數字大小");
        model.addAttribute("message", message);
        return "guess-number";
    }

    @PostMapping("/lobby/game/dice")
    public String handleDiceGame(Model model) {
        int playerDice1 = random.nextInt(6) + 1;
        int playerDice2 = random.nextInt(6) + 1;
        int computerDice1 = random.nextInt(6) + 1;
        int computerDice2 = random.nextInt(6) + 1;

        int playerTotal = playerDice1 + playerDice2;
        int computerTotal = computerDice1 + computerDice2;

        String result;
        if (playerTotal > computerTotal) {
            result = "你擲出 " + playerDice1 + " 和 " + playerDice2 + "，總和 " + playerTotal +
                    "。\n電腦擲出 " + computerDice1 + " 和 " + computerDice2 + "，總和 " + computerTotal +
                    "。\n你贏了！";
        } else if (playerTotal < computerTotal) {
            result = "你擲出 " + playerDice1 + " 和 " + playerDice2 + "，總和 " + playerTotal +
                    "。\n電腦擲出 " + computerDice1 + " 和 " + computerDice2 + "，總和 " + computerTotal +
                    "。\n你輸了！";
        } else {
            result = "雙方都擲出 " + playerTotal + "，平手！";
        }

        model.addAttribute("gameName", "擲骰子比大小");
        model.addAttribute("result", result);

        // 👇 傳骰子點數給模板
        model.addAttribute("playerDice1", playerDice1);
        model.addAttribute("playerDice2", playerDice2);
        model.addAttribute("computerDice1", computerDice1);
        model.addAttribute("computerDice2", computerDice2);

        return "dice-game";
    }

    @PostMapping("/lobby/game/minefield/click")
    @ResponseBody
    public Map<String, Object> handleMineClick(@RequestParam int index, HttpSession session) {
        Set<Integer> mines = (Set<Integer>) session.getAttribute("mines");
        Set<Integer> revealed = (Set<Integer>) session.getAttribute("revealed");
        Map<String, Object> response = new HashMap<>();

        if (mines == null || revealed == null) {
            response.put("status", "error");
            response.put("message", "遊戲尚未開始");
            return response;
        }

        if (revealed.contains(index)) {
            response.put("status", "ok");
            response.put("mine", false);
            response.put("message", "此格已翻開");
            return response;
        }

        revealed.add(index);
        session.setAttribute("revealed", revealed);

        boolean isMine = mines.contains(index);
        response.put("status", "ok");
        response.put("mine", isMine);
        if (isMine) {
            response.put("message", "踩到地雷！遊戲結束");
            // 清除session 遊戲結束
            session.removeAttribute("mines");
            session.removeAttribute("revealed");
        } else {
            response.put("message", "安全");
        }

        return response;
    }

    @GetMapping("/lobby/game/dice-competition")
    public String diceCompetitionForm(Model model) {
        model.addAttribute("gameName", "擲骰競賽");
        return "dice-competition-form";
    }

    @PostMapping("/lobby/game/dice-competition")
    public String startDiceCompetition(@RequestParam List<String> players, Model model) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (String player : players) {
            List<Integer> dices = Arrays.asList(
                    random.nextInt(6) + 1,
                    random.nextInt(6) + 1,
                    random.nextInt(6) + 1
            );
            int total = dices.stream().mapToInt(Integer::intValue).sum();

            String dicesStr = dices.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));

            Map<String, Object> result = new HashMap<>();
            result.put("name", player);
            result.put("dices", dices);
            result.put("dicesStr", dicesStr);  // 新增骰子字串
            result.put("total", total);
            results.add(result);
        }

        results.sort((a, b) -> ((int) b.get("total")) - ((int) a.get("total")));
        // 不需要再reverse，sort已經降冪

        model.addAttribute("gameName", "擲骰競賽");
        model.addAttribute("results", results);
        return "dice-competition-result";
    }


}
