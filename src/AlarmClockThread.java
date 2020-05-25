import java.util.Timer;
import java.util.TimerTask;

public class AlarmClockThread{
	public long timeLimit;
	public float latency;
	public ClientSarg cs;

	public Timer timer;

	public AlarmClockThread(long timeLimit, float expectedNetworkLatency, ClientSarg cs) {
		super();
		this.timeLimit = timeLimit * 1000; // translate to milliseconds
		this.latency = expectedNetworkLatency;
		this.cs = cs;
		
		this.timeLimit -= latency;
		this.timeLimit -= 500; // buffer for half of a second

		timer = new Timer();
		timer.schedule(new RemindTask(), this.timeLimit);
	}

	class RemindTask extends TimerTask {
		public void run() {
			System.out.println("Time's up!");
			cs.sendMove();
			timer.cancel(); // Terminate the timer thread
		}
	}

}
