package br.com.caelum.vraptor.quartzjob;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.environment.Environment;
import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;

@ApplicationScoped
@Component
public class QuartzScheduler {

	private static final int TEN_SECONDS = 10000;
	private final Scheduler scheduler;
	private boolean initialized;

	private final static Logger logger = LoggerFactory.getLogger(QuartzScheduler.class);
	private final Environment env;
	
	public QuartzScheduler(Environment env) throws SchedulerException {
		this.env = env;
		scheduler = StdSchedulerFactory.getDefaultScheduler();
	}
	
	@PostConstruct
	public void initialize() {
		try {
			logger.info("Quartz servlet config initializing...");
			if(!env.getName().equals("production")) return;

			String url = (env.get("host") + "/jobs/configure").replace("https", "http");

			Runnable quartzMe = new StartQuartz(url);
			new Thread(quartzMe).start();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	class StartQuartz implements Runnable {
		private final String url;

		public StartQuartz(String url) {
			this.url = url;
		}
		
		@Override
		public void run() {
			try {
				waitForServerStartup();
				logger.info("Invoking quartz configurator at " + url);
				HttpClient http = new HttpClient();
				int status = http.executeMethod(new GetMethod(url));
				if (status != 200) {
					throw new RuntimeException("could not configure quartz, " + url + "answered with " + status);
				}
			} catch (Exception e) {
				logger.error("Could not start quartz!", e);
			}
		}

		private void waitForServerStartup() throws InterruptedException {
			Thread.sleep(TEN_SECONDS);
		}
	}
	
	public void add(JobDetail job, Trigger trigger) throws SchedulerException {
		scheduler.scheduleJob(job, trigger);
	}
	
	public void start() throws SchedulerException {
		scheduler.start();
		initialized = true;
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	@PreDestroy
	public void destroy() throws SchedulerException {
		scheduler.shutdown();
	}
}
