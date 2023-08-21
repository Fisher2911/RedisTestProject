package io.github.fisher2911.redistestproject;

import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager extends Thread {

    public static final int BOT_COUNT = 5;

    private final List<GameThread> threads = new ArrayList<>();
    private final JedisPooled pooled;
    private boolean running = true;

    public GameManager(JedisPooled pooled) {
        this.pooled = pooled;
    }

    @Override
    public void run() {
        for (int i = 100; i >= 0; i -= 10) {
            this.threads.add(new GameThread(this, new ConcurrentHashMap<>(), i, this.pooled));
        }
        final GameThread zeroThread = this.threads.get(this.threads.size() - 1);
        for (int i = 0; i < BOT_COUNT; i++) {
            zeroThread.addBot(new Bot(UUID.randomUUID(), "Bot " + i));
        }
        for (final var thread : this.threads) {
            thread.start();
        }
        while (this.running) {
            if (this.shouldEndGame()) {
                this.stopGame();
                System.out.println("Game ended!");
            }
        }
    }

    private boolean shouldEndGame() {
        return this.threads.get(0).countBots() == BOT_COUNT;
    }

    public void attemptBotTransfer(Bot bot, GameThread thread) {
        for (final var game : this.threads) {
            if (game.containsBot(bot)) break;
            if (game.addBot(bot)) {
                thread.removeBot(bot);
                return;
            }
        }
    }

    public void stopGame() {
        this.running = false;
        for (final var thread : this.threads) {
            thread.stopThread();
        }
    }

    public boolean isRunning() {
        return this.running;
    }

}
