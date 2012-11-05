package br.com.caelum.vraptor.quartzjob;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.List;

import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.ioc.Component;

@Component
public class QuartzConfiguration {

	private final static Logger logger = LoggerFactory.getLogger(QuartzConfiguration.class);

	private final Linker linker;

	private final QuartzScheduler scheduler;

	public QuartzConfiguration(DefaultLinker linker, QuartzScheduler scheduler) throws SchedulerException {
		this.linker = linker;
		this.scheduler = scheduler;
	}

	public void configure(List<CronTask> tasks) throws SchedulerException {
		logger.info("Starting to configure quartz: " + tasks.size() + " tasks found");

		for(CronTask task : tasks) {
			linker.linkTo(task).execute();
			String url = linker.get();

			JobDetail job = newJob(QuartzHttpRequestJob.class)
					.withIdentity(task.getClass().getName(), "gnarus")
					.usingJobData("url", url)
					.build();
			
			Trigger trigger = newTrigger()
					.withIdentity(task.getClass().getName(), "gnarus")
					.withSchedule(cronSchedule(task.frequency()))
					.forJob(task.getClass().getName(), "gnarus")
					.startNow()
					.build();

			logger.info("Registering " + task.getClass().getName() + " to run every " + task.frequency());
			
			scheduler.add(job, trigger);
		}
		
		scheduler.start();
		logger.info("Quartz configured and started!");

		
	}
}
