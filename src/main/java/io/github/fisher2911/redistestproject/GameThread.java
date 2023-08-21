package io.github.fisher2911.redistestproject;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class GameThread extends Thread {

    private final GameManager gameManager;
    private final Map<UUID, Bot> bots;
    private final int minScore;
    private final JedisPooled jedisPooled;
    private boolean running = true;

    public GameThread(GameManager gameManager, Map<UUID, Bot> bots, int minScore, JedisPooled jedisPooled) {
        this.gameManager = gameManager;
        this.bots = bots;
        this.minScore = minScore;
        this.jedisPooled = jedisPooled;
    }

    @Override
    public void run() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        while (this.running) {
            for (final var entry : this.bots.entrySet()) {
                if (random.nextInt() <= 50) continue;
                final Bot bot = entry.getValue();
                final int addScore = random.nextInt(-2, 10);
                this.addScore(bot, addScore);
            }
            try {
                Thread.sleep(random.nextInt(500, 2000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean addBot(Bot bot) {
        if (this.bots.containsKey(bot.uuid())) {
            return false;
        }
        final String currentScoreStr = this.jedisPooled.hget(Main.REDIS_SCORE_KEY, bot.uuid().toString());
        if (currentScoreStr == null && this.minScore > 0) return false;
        final int currentScore;
        if (currentScoreStr == null) {
            currentScore = 0;
        } else {
            currentScore = Integer.parseInt(currentScoreStr);
        }
        if (currentScore < this.minScore) return false;
        this.bots.put(bot.uuid(), bot);
        System.out.println("Added bot " + bot.name() + " to game thread " + this.minScore + " current score is " + currentScore);
        return true;
    }

    public void addScore(Bot bot, int score) {
        final String currentScoreStr = this.jedisPooled.hget(Main.REDIS_SCORE_KEY, bot.uuid().toString());
        final int currentScore;
        if (currentScoreStr == null) {
            currentScore = 0;
        } else {
            currentScore = Integer.parseInt(currentScoreStr);
        }
        this.jedisPooled.hset(Main.REDIS_SCORE_KEY, bot.uuid().toString(), String.valueOf(Math.max(0, currentScore + score)));
        this.gameManager.attemptBotTransfer(bot, this);
    }

    public void removeBot(Bot bot) {
        this.bots.remove(bot.uuid());
    }

    public boolean containsBot(Bot bot) {
        return this.bots.containsKey(bot.uuid());
    }

    public int countBots() {
        return this.bots.size();
    }

    public void stopThread() {
        this.running = false;
    }
}
