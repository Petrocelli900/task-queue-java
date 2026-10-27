package dev.petrocelli;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public final class TaskQueueTest {
    private static final class MutableClock extends Clock {
        private Instant now=Instant.parse("2026-01-01T00:00:00Z");
        public ZoneId getZone(){return ZoneOffset.UTC;}
        public Clock withZone(ZoneId zone){return Clock.fixed(now,zone);}
        public Instant instant(){return now;}
        void advance(Duration duration){now=now.plus(duration);}
    }
    private static void check(boolean value){if(!value)throw new AssertionError();}
    private static void rejects(Runnable action){
        try {action.run();}catch(IllegalStateException expected){return;}
        throw new AssertionError("Expected stale completion rejection");
    }
    public static void main(String[] args)throws Exception {
        var clock=new MutableClock(); var queue=new TaskQueue(clock,2,Duration.ofSeconds(10));
        queue.submit("job","payload"); queue.submit("job","payload");
        var first=queue.claim().orElseThrow(); check(first.attempts()==1);
        check(queue.claim().isEmpty());
        queue.fail("job",1); check(queue.claim().isEmpty());
        clock.advance(Duration.ofSeconds(9)); check(queue.claim().isEmpty());
        clock.advance(Duration.ofSeconds(1)); check(queue.claim().orElseThrow().attempts()==2);
        rejects(()->queue.succeed("job",1));
        check(queue.fail("job",2).status()==TaskQueue.Status.DEAD); check(queue.claim().isEmpty());
        queue.submit("ok","work"); queue.claim().orElseThrow(); queue.succeed("ok",1);
        check(queue.get("ok").status()==TaskQueue.Status.SUCCEEDED);
        rejects(()->queue.fail("ok",1));
        var concurrent=new TaskQueue(clock,1,Duration.ofSeconds(1)); concurrent.submit("only","work");
        var pool=Executors.newFixedThreadPool(8); var futures=new ArrayList<Future<Boolean>>();
        var start=new CountDownLatch(1); int wins=0;
        try {
            for(int i=0;i<40;i++)futures.add(pool.submit(()->{start.await();return concurrent.claim().isPresent();}));
            start.countDown(); for(var future:futures)if(future.get(10,TimeUnit.SECONDS))wins++;
        }finally{pool.shutdownNow();}
        check(wins==1);
        System.out.println("TaskQueue: OK");
    }
}
