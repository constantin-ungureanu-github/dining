package fsm;

import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Dining {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("Dining");

        List<ActorRef> chopsticks = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            chopsticks.add(system.actorOf(Props.create(ForkPair.class), "Chopstick_" + i));

        List<ActorRef> philosophers = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            philosophers.add(system.actorOf(Props.create(Philosopher.class, "Philosopher_" + i, chopsticks.get(i), chopsticks.get((i + 1) % 5)), "Philosopher_" + i));

        philosophers.stream().forEach(philosopher -> philosopher.tell(Philosopher.Events.Think, ActorRef.noSender()));
    }
}
