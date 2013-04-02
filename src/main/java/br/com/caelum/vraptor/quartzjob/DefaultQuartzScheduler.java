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
public class DefaultQuartzScheduler implements QuartzScheduler {

	private static final int TEN_SECONDS = 10000;
	private final Scheduler scheduler;
	private boolean initialized;

	private final static Logger logger = LoggerFactory.getLogger(DefaultQuartzScheduler.class);
	private final Environment env;
	private final Linker linker;
	
	public DefaultQuartzScheduler(Environment env, Linker linker) throws SchedulerException {
		this.env = env;
		this.linker = linker;
		scheduler = StdSchedulerFactory.getDefaultScheduler();
	}
	
	@Override
	@PostConstruct
	public void schedule() {
		try {
			logger.info("Quartz servlet config initializing...");
			if(!env.getName().equals("production")) return;

			String url = (quartzControllerUrl()).replace("https", "http");

			Runnable quartzMe = new StartQuartz(url);
			new Thread(quartzMe).start();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * this method may be overwritten if you need to use other 
	 * controller to configure your tasks.
	 * @see QuartzController
	 */
	protected String quartzControllerUrl() {
		linker.linkTo(QuartzController.class).config();
		String url = linker.get();
		return url;
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
	
	@Override
	public boolean isInitialized() {
		return initialized;
	}
	
	@PreDestroy
	public void destroy() throws SchedulerException {
		scheduler.shutdown();
	}
}
