package fsm;

import static fsm.Philosopher.Events.Think;
import static fsm.Philosopher.States.Eating;
import static fsm.Philosopher.States.ForkDenied;
import static fsm.Philosopher.States.Hungry;
import static fsm.Philosopher.States.Thinking;
import static fsm.Philosopher.States.WaitFork;
import static fsm.Philosopher.States.Waiting;
import static java.util.concurrent.TimeUnit.SECONDS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.actor.FSM;
import fsm.ForkPair.Busy;
import fsm.ForkPair.Taken;
import fsm.Philosopher.States;
import fsm.Philosopher.TakenChopsticks;
import scala.concurrent.duration.Duration;

public class Philosopher extends AbstractFSM<States, TakenChopsticks> {
    private static Logger log = LoggerFactory.getLogger(Philosopher.class);

    private String name;
    private ActorRef left;
    private ActorRef right;

    public Philosopher(String name, ActorRef left, ActorRef right) {
        this.name = name;
        this.left = left;
        this.right = right;
    }

    public enum States {
        Waiting, Thinking, Hungry, WaitFork, ForkDenied, Eating
    }

    public enum Events {
        Think
    }

    private FSM.State<States, TakenChopsticks> startEating(ActorRef left, ActorRef right) {
        log.info("{} has picked up {} and {} and starts to eat", name, left.path().name(), right.path().name());
        return goTo(Eating).using(new TakenChopsticks(left, right)).forMax(Duration.create(5, SECONDS));
    }

    private FSM.State<States, TakenChopsticks> startThinking() {
        log.info("{} has started thinking", name);
        return goTo(Thinking).using(new TakenChopsticks(null, null)).forMax(Duration.create(5, SECONDS));
    }

    private FSM.State<States, TakenChopsticks> goHungry() {
        left.tell(ForkPair.Events.Aquire, self());
        right.tell(ForkPair.Events.Aquire, self());
        log.info("{} has gone hungry", name);
        return goTo(Hungry);
    }

    public static final class TakenChopsticks {
        public final ActorRef left;
        public final ActorRef right;

        public TakenChopsticks(ActorRef left, ActorRef right) {
            this.left = left;
            this.right = right;
        }
    }

    {
        startWith(Waiting, null);

        when(Waiting, matchEventEquals(Think, (think, data) -> startThinking()));

        when(Thinking, matchEventEquals(StateTimeout(), (event, data) -> goHungry()));

        when(Hungry, matchEvent(Taken.class, (taken, data) -> taken.fork == left, (taken, data) -> goTo(WaitFork).using(new TakenChopsticks(left, null)))
                        .event(Taken.class, (taken, data) -> taken.fork == right, (taken, data) -> goTo(WaitFork).using(new TakenChopsticks(null, right)))
                        .event(Busy.class, (busy, data) -> goTo(ForkDenied)));

        when(WaitFork, matchEvent(Taken.class, (taken, data) -> (taken.fork == left && data.left == null && data.right != null), (taken, data) -> startEating(left, right))
                            .event(Taken.class, (taken, data) -> (taken.fork == right && data.left != null && data.right == null), (taken, data) -> startEating(left, right))
                            .event(Busy.class, (busy, data) -> {
                                if (data.left != null)
                                    left.tell(ForkPair.Events.Release, self());
                                if (data.right != null)
                                    right.tell(ForkPair.Events.Release, self());
                                return startThinking();
                            }));

        when(ForkDenied, matchEvent(Taken.class, (taken, data) -> {
            taken.fork.tell(ForkPair.Events.Release, self());
            return startThinking();
        }).event(Busy.class, (busy, data) -> startThinking()));

        when(Eating, matchEventEquals(StateTimeout(), (event, data) -> {
            right.tell(ForkPair.Events.Release, self());
            left.tell(ForkPair.Events.Release, self());
            return startThinking();
        }));

        initialize();
    }
}
