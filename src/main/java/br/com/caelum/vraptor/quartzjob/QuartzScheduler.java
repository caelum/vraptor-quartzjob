package br.com.caelum.vraptor.quartzjob;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.List;

import javax.inject.Inject;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.quartzjob.http.HttpRequestExecutor;
import br.com.caelum.vraptor.quartzjob.http.QuartzHttpRequestJob;

public class QuartzScheduler {
	public static final String METHOD_FACTORY = "methodFactory";
	
	private static final String JOB_IDENTIFIER = "vraptor-request-job";
	private final static Logger logger = LoggerFactory.getLogger(QuartzScheduler.class);

	private Linker linker;

	private QuartzConfigurator scheduler;

	private HttpRequestExecutor methodFactory;

	@Deprecated // CDI eyes only
	QuartzScheduler() {}

	@Inject
	public QuartzScheduler(Linker linker, QuartzConfigurator scheduler,
						   HttpRequestExecutor methodFactory) {
		this.linker = linker;
		this.scheduler = scheduler;
		this.methodFactory = methodFactory;
	}

	public void configure(List<CronTask> tasks) throws SchedulerException {
		logger.info("Starting to configure quartz: " + tasks.size() + " tasks found");

		for(CronTask task : tasks) {
			linker.linkTo(task).execute();
			String url = linker.get().replace("https", "http");
			
			JobDataMap data = new JobDataMap();
			data.put("url", url);
			data.put(METHOD_FACTORY, methodFactory);
			
			JobDetail job = newJob(QuartzHttpRequestJob.class)
					.withIdentity(task.getClass().getName(), JOB_IDENTIFIER)
					.usingJobData(data)
					.build();

			Trigger trigger = newTrigger()
					.withIdentity(task.getClass().getName(), JOB_IDENTIFIER)
					.withSchedule(cronSchedule(task.frequency()))
					.forJob(task.getClass().getName(), JOB_IDENTIFIER)
					.startNow()
					.build();

			logger.info("Registering " + task.getClass().getName() + " to run every " + task.frequency());

			scheduler.add(job, trigger);
		}

		scheduler.start();
		logger.info("Quartz configured and started!");


	}
}
