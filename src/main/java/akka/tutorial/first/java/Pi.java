package akka.tutorial.first.java;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.routing.RoundRobinRouter;
import akka.util.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pi {
	static final Logger logger = LoggerFactory.getLogger(Pi.class);
	
	public static class Listener extends UntypedActor {
		static final Logger logger = LoggerFactory.getLogger(Listener.class);

		public void onReceive(Object message) {
			if (message instanceof PiApproximation) {
				PiApproximation approximation = (PiApproximation) message;
				logger.info(String
						.format("\n\tPi approximation = \t\t%s\n\tComputation time = \t%s",
								approximation.getPi(),
								approximation.getDuration()));
				getContext().system().shutdown();
			} else {
				unhandled(message);
			}
		}
	}
	
    public static void main( String[] args ) {
          Pi pi = new Pi();
          pi.calculate(8, 10000, 10000);
    }
    
    public void calculate(final int nrOfWorkers, final int nrOfElements, final int nrOfMessages) {
    	// Create an Akka system
    	ActorSystem system = ActorSystem.create("PiSystem");
    	
    	final ActorRef listener = system.actorOf(new Props(Listener.class), "listener");
    	
    	 ActorRef master = system.actorOf(new Props(new UntypedActorFactory() {
    		 public UntypedActor create() {
    		 return new Master(nrOfWorkers, nrOfMessages, nrOfElements, listener);
    		 }
         }), "master");
    	 
    	 // start the calculation
    	 master.tell(new Calculate());
    }
    
    private static class Calculate {
    	
    }
    
    private static class Work {
    	private final int start;
    	private final int nrOfElements;
    	
    	public Work(int start, int nrOfElements) {
    		this.start = start;
    		this.nrOfElements = nrOfElements;
    	}

    	public int getStart() {
    		return start;
    	}

    	public int getNrOfElements() {
    		return nrOfElements;
    	}
    }

    private static class Result {
    	private final double value;

    	public Result(double value) {
    		this.value = value;
    	}

    	public double getValue() {
    		return value;
    	}
    } 
    
    private static class PiApproximation {
    	private final double pi;
    	private final Duration duration;

    	public PiApproximation(double pi, Duration duration) {
    		this.pi = pi;
    		this.duration = duration;
    	}

    	public double getPi() {
    		return pi;
    	}

    	public Duration getDuration() {
    		return duration;
    	}
    }
    
    public static class Worker extends UntypedActor {
    	public void onReceive(Object message) {
    		if (message instanceof Work) {
    			Work work = (Work) message;
    			double result = calculatePiFor(work.getStart(),
    					work.getNrOfElements());
    			getSender().tell(new Result(result), getSelf());
    		} else {
    			unhandled(message);
    		}
    	}
    	
    	private double calculatePiFor(int start, int nrOfElements) {
    		double acc = 0.0;
    		for (int i = start * nrOfElements; i <= ((start + 1) * nrOfElements - 1); i++) {
    			acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1);
    		}
    		return acc;
    	}
    }
    
	private static class Master extends UntypedActor {
		private final int nrOfMessages;
		private final int nrOfElements;

		private double pi;
		private int nrOfResults;
		private final long start = System.currentTimeMillis();

		private final ActorRef listener;
		private final ActorRef workerRouter;

		public Master(final int nrOfWorkers, int nrOfMessages,
				int nrOfElements, ActorRef listener) {
			this.nrOfMessages = nrOfMessages;
			this.nrOfElements = nrOfElements;
			this.listener = listener;

			workerRouter = this.getContext().actorOf(
					new Props(Worker.class).withRouter(new RoundRobinRouter(
							nrOfWorkers)), "workerRouter");
		}

		public void onReceive(Object message) {
			if (message instanceof Calculate) {
				for (int start = 0; start < nrOfMessages; start++) {
					workerRouter.tell(new Work(start, nrOfElements), getSelf());
				}
			} else if (message instanceof Result) {
				Result result = (Result) message;
				pi += result.getValue();
				nrOfResults += 1;
				if (nrOfResults == nrOfMessages) {
					// Send the result to the listener
					Duration duration = Duration.create(
							System.currentTimeMillis() - start,
							TimeUnit.MILLISECONDS);
					listener.tell(new PiApproximation(pi, duration), getSelf());
					// Stops this actor and all its supervised children
					getContext().stop(getSelf());
				}
			} else {
				unhandled(message);
			}
		}

	}
}
