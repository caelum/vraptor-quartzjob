package br.com.caelum.vraptor.quartzjob;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import br.com.caelum.vraptor.events.VRaptorInitialized;
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

@ApplicationScoped
public class QuartzConfigurator {

	private Scheduler scheduler;
	private boolean initialized;

	private final static Logger logger = LoggerFactory.getLogger(QuartzConfigurator.class);
	private Environment env;

	@Deprecated // CDI eyes only
	public QuartzConfigurator() {}

	@Inject
	public QuartzConfigurator(Environment env) throws SchedulerException {
		this.env = env;
	}

	public void initialize(@Observes VRaptorInitialized event) {
		try {
			boolean notProduction = !env.getName().equals("production");
			boolean force = Boolean.parseBoolean(env.get("force.quartz.jobs", "false"));

			if (notProduction && !force) return;

			logger.info("Quartz configurator initializing...");
			scheduler = StdSchedulerFactory.getDefaultScheduler();

			String url = (env.get("host") + "/jobs/configure")
					.replace("https", "http");

			Runnable quartzMe = new StartQuartz(url);
			new Thread(quartzMe).start();

		} catch (Exception e) {
			logger.error("could not schedule job", e);
			throw new RuntimeException(e);
		}
	}

	class StartQuartz implements Runnable {
		private final Integer ONE_MINUTE = 1*60*1000;
		private final String url;

		public StartQuartz(String url) {
			this.url = url;
		}

		@Override
		public void run() {
			try {
				HttpClient http = new HttpClient();
				waitStartup();
				logger.info("Invoking quartz configurator at " + url);
				http.executeMethod(new GetMethod(url));
			} catch (Exception e) {
				logger.error("Could not configure quartz!", e);
			}
		}

		public void waitStartup() throws InterruptedException  {
			String waitTime = env.get("quartz.wait.time", ONE_MINUTE.toString());
			Thread.sleep(Long.parseLong(waitTime));
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
