package io.github.fisher2911.redistestproject;

import redis.clients.jedis.JedisPooled;
import redis.embedded.RedisServer;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    public static final String REDIS_HOST = "localhost";
    public static final int REDIS_PORT = 6379;
    public static final String REDIS_SCORE_KEY = "score";

    public static void main(String[] args) throws InterruptedException {
        final JedisPooled jedisPooled = new JedisPooled(REDIS_HOST, REDIS_PORT);
        final RedisServer redisServer = new RedisServer(6379);
        redisServer.start();
        final GameManager gameManager = new GameManager(jedisPooled);
        gameManager.start();
        final AtomicBoolean shouldStop = new AtomicBoolean(false);
        while (gameManager.isRunning() && !shouldStop.get()) {
        }
        System.out.println("Stopped redis server");
        gameManager.stopGame();
        jedisPooled.close();
        redisServer.stop();
        System.out.println("Test");
    }

}
