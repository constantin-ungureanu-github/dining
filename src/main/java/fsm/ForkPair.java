package fsm;

import static fsm.ForkPair.States.Available;
import static fsm.ForkPair.States.Taken;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import fsm.ForkPair.States;
import fsm.ForkPair.TakenBy;

public class ForkPair extends AbstractFSM<States, TakenBy> {
    {
        startWith(Available, new TakenBy(null));
        when(Available, matchEventEquals(pickUp, (take, data) -> goTo(Taken).using(new TakenBy(sender())).replying(new Taken(self()))));
        when(Taken, matchEventEquals(pickUp, (take, data) -> stay().replying(new Busy(self()))).event((event, data) -> (event == putDown) && (data.philosopher == sender()), (event, data) -> goTo(Available).using(new TakenBy(null))));
        initialize();
    }

    public static enum States {
        Available, Taken
    }

    public static final Object putDown = new Object() {
    };

    public static final Object pickUp = new Object() {
    };

    public static final class TakenBy {
        public final ActorRef philosopher;

        public TakenBy(ActorRef philosopher) {
            this.philosopher = philosopher;
        }
    }

    public static final class Taken {
        public final ActorRef fork;

        public Taken(ActorRef fork) {
            this.fork = fork;
        }
    }

    public static final class Busy {
        public final ActorRef chopstick;

        public Busy(ActorRef chopstick) {
            this.chopstick = chopstick;
        }
    }
}
