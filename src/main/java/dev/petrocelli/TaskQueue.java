package dev.petrocelli;

import java.time.*;
import java.util.*;

/** Deterministic polling queue. Execute handlers outside the queue monitor. */
public final class TaskQueue {
    public enum Status { READY, RUNNING, SUCCEEDED, DEAD }
    public record Task(String id,String payload,int attempts,Instant due,Status status) {}
    private final Clock clock;
    private final int maxAttempts;
    private final Duration retryDelay;
    private final Map<String,Task> tasks=new LinkedHashMap<>();
    public TaskQueue(Clock clock,int maxAttempts,Duration retryDelay) {
        this.clock=Objects.requireNonNull(clock); this.retryDelay=Objects.requireNonNull(retryDelay);
        if(maxAttempts<1 || retryDelay.isNegative() || retryDelay.isZero())
            throw new IllegalArgumentException("Positive attempts and delay required");
        this.maxAttempts=maxAttempts;
    }
    public synchronized Task submit(String id,String payload) {
        if(id==null || id.isBlank() || payload==null || payload.isBlank())
            throw new IllegalArgumentException("Blank task");
        var old=tasks.get(id);
        if(old!=null) {
            if(!old.payload().equals(payload)) throw new IllegalArgumentException("Conflicting task id");
            return old;
        }
        var task=new Task(id,payload,0,clock.instant(),Status.READY); tasks.put(id,task); return task;
    }
    public synchronized Optional<Task> claim() {
        var now=clock.instant();
        var next=tasks.values().stream().filter(t->t.status()==Status.READY && !t.due().isAfter(now))
            .min(Comparator.comparing(Task::due).thenComparing(Task::id));
        return next.map(t->{ var running=new Task(t.id(),t.payload(),t.attempts()+1,t.due(),Status.RUNNING);
            tasks.put(t.id(),running); return running; });
    }
    public synchronized Task succeed(String id,int attempt) { return finish(id,attempt,true); }
    public synchronized Task fail(String id,int attempt) { return finish(id,attempt,false); }
    private Task finish(String id,int attempt,boolean success) {
        var task=tasks.get(id);
        if(task==null) throw new NoSuchElementException("Unknown task");
        if(task.status()!=Status.RUNNING || task.attempts()!=attempt)
            throw new IllegalStateException("Stale or invalid completion");
        var status=success?Status.SUCCEEDED:task.attempts()>=maxAttempts?Status.DEAD:Status.READY;
        var due=status==Status.READY?clock.instant().plus(retryDelay):task.due();
        var next=new Task(id,task.payload(),task.attempts(),due,status); tasks.put(id,next); return next;
    }
    public synchronized Task get(String id) {
        var task=tasks.get(id); if(task==null) throw new NoSuchElementException("Unknown task"); return task;
    }
}
