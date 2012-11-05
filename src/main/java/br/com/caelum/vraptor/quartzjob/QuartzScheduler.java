package br.com.caelum.vraptor.quartzjob;

import javax.annotation.PreDestroy;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;

@ApplicationScoped
@Component
public class QuartzScheduler {

	private final Scheduler scheduler;
	private boolean initialized;

	public QuartzScheduler() throws SchedulerException {
		scheduler = StdSchedulerFactory.getDefaultScheduler();
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
