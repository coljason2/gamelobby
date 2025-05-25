package com.blackjackgame.blackjackgame.game;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlackjackGame {

    private static final ThreadLocal<List<String>> playerHand = ThreadLocal.withInitial(ArrayList::new);
    private static final Map<String, Integer> cardValues = new HashMap<>();
    private static final Random random = new Random();

    static {
        cardValues.put("A", 11); cardValues.put("2", 2); cardValues.put("3", 3);
        cardValues.put("4", 4); cardValues.put("5", 5); cardValues.put("6", 6);
        cardValues.put("7", 7); cardValues.put("8", 8); cardValues.put("9", 9);
        cardValues.put("10", 10); cardValues.put("J", 10); cardValues.put("Q", 10); cardValues.put("K", 10);
    }

    private static String drawCard() {
        List<String> deck = new ArrayList<>(cardValues.keySet());
        return deck.get(random.nextInt(deck.size()));
    }

    private static int calculateHandValue(List<String> hand) {
        int sum = 0, aceCount = 0;
        for (String card : hand) {
            int value = cardValues.get(card);
            sum += value;
            if ("A".equals(card)) aceCount++;
        }
        while (sum > 21 && aceCount > 0) {
            sum -= 10;
            aceCount--;
        }
        return sum;
    }

    static class Player {
        String name;
        int money = 1000;
        List<String> hand = new ArrayList<>();
        int roundTotal = 0;
        boolean isBusted = false;
        boolean isOut = false;

        Player(String name) {
            this.name = name;
        }

        void play() throws InterruptedException {
            hand.clear();
            hand.add(drawCard());
            hand.add(drawCard());
            System.out.println(name + " 的初始牌：" + hand);

            while (true) {
                int total = calculateHandValue(hand);
                roundTotal = total;

                if (total < 17) {
                    String card = drawCard();
                    hand.add(card);
                    System.out.println(name + " 要牌：" + card + "（目前點數：" + calculateHandValue(hand) + "）");
                    Thread.sleep(600);
                } else {
                    if (total > 21) {
                        isBusted = true;
                        System.out.println(name + " 爆牌！手牌：" + hand + "（點數：" + total + "）");
                    } else {
                        System.out.println(name + " 停手，手牌：" + hand + "（點數：" + total + "）");
                    }
                    break;
                }
            }
        }
    }

    private static List<String> dealerPlay() {
        List<String> dealerHand = new ArrayList<>();
        dealerHand.add(drawCard());
        dealerHand.add(drawCard());
        System.out.println("\n🔸 莊家初始牌：" + dealerHand);

        while (calculateHandValue(dealerHand) < 17) {
            String card = drawCard();
            dealerHand.add(card);
            System.out.println("🔸 莊家要牌：" + card + "（目前點數：" + calculateHandValue(dealerHand) + "）");
        }

        int total = calculateHandValue(dealerHand);
        if (total > 21) {
            System.out.println("🔸 莊家爆牌！手牌：" + dealerHand + "（點數：" + total + "）");
        } else {
            System.out.println("🔸 莊家停手，手牌：" + dealerHand + "（點數：" + total + "）");
        }

        return dealerHand;
    }

    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        ExecutorService threadPool = Executors.newFixedThreadPool(3);

        List<Player> players = Arrays.asList(
                new Player("玩家一"),
                new Player("玩家二"),
                new Player("玩家三")
        );

        int round = 1;
        while (true) {
            // 過濾出場中玩家
            List<Player> activePlayers = new ArrayList<>();
            for (Player p : players) {
                if (!p.isOut) {
                    activePlayers.add(p);
                }
            }

            if (activePlayers.isEmpty()) {
                System.out.println("\n💥 所有玩家都已出局，遊戲結束！");
                break;
            }

            System.out.println("\n==========================");
            System.out.println("🔁 第 " + round + " 回合開始");
            System.out.println("==========================");

            CountDownLatch latch = new CountDownLatch(activePlayers.size());

            for (Player player : activePlayers) {
                threadPool.submit(() -> {
                    try {
                        player.play();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();

            // 莊家出牌
            List<String> dealerHand = dealerPlay();
            int dealerTotal = calculateHandValue(dealerHand);
            boolean dealerBust = dealerTotal > 21;

            // 比牌並更新金額
            System.out.println("\n💰 結算結果：");
            for (Player p : activePlayers) {
                if (p.isBusted) {
                    p.money -= 100;
                    System.out.println(p.name + " 爆牌，輸了！餘額：" + p.money);
                } else if (dealerBust || p.roundTotal > dealerTotal) {
                    p.money += 100;
                    System.out.println(p.name + " 贏了！餘額：" + p.money);
                } else if (p.roundTotal < dealerTotal) {
                    p.money -= 100;
                    System.out.println(p.name + " 輸了！餘額：" + p.money);
                } else {
                    System.out.println(p.name + " 平手！餘額：" + p.money);
                }

                if (p.money <= 0) {
                    p.isOut = true;
                    System.out.println("⚠ " + p.name + " 已破產出局！");
                }

                // 重設狀態
                p.isBusted = false;
                p.roundTotal = 0;
            }

            System.out.print("\n是否繼續下一回合？（Enter 繼續，輸入 n 或 exit 結束）: ");
            String input = scanner.nextLine().trim().toLowerCase();
            if ("n".equals(input) || "exit".equals(input)) break;

            round++;
        }

        threadPool.shutdownNow();
        System.out.println("🎮 遊戲結束！");
    }
}
