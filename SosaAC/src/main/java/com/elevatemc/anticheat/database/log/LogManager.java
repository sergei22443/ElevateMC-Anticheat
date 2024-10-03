package com.elevatemc.anticheat.database.log;

import lombok.Getter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
public class LogManager {

    private final Queue<Log> queuedLogs = new ConcurrentLinkedQueue<>();

}
