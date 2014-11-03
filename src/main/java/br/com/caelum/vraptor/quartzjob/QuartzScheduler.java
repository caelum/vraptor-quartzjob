package br.com.caelum.vraptor.quartzjob;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.lang.reflect.Method;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.com.caelum.vraptor.environment.Environment;
import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.quartzjob.http.HttpRequestExecutor;
import br.com.caelum.vraptor.quartzjob.http.QuartzHttpRequestJob;

public class QuartzScheduler {
	public static final String METHOD_FACTORY = "methodFactory";
	
	private static final String JOB_IDENTIFIER = "vraptor-request-job";
	private final static Logger logger = LoggerFactory.getLogger(QuartzScheduler.class);

	private Linker linker;

	private QuartzConfigurator scheduler;

	private HttpRequestExecutor methodFactory;

	private final Router router;
	
	private final Environment env;
	
	private final Container container;

	@Deprecated // CDI eyes only
	QuartzScheduler() {
		this(null, null, null, null, null, null);
	}

	@Inject
	public QuartzScheduler(Linker linker, QuartzConfigurator scheduler,
						   HttpRequestExecutor methodFactory, Router router,
						   Environment env, Container container) {
		this.linker = linker;
		this.scheduler = scheduler;
		this.methodFactory = methodFactory;
		this.router = router;
		this.env = env;
		this.container = container;
	}

	public void configure(Set<Bean<?>> tasks)  {
		logger.info("Starting to configure quartz tasks found");

		try {
			for(Bean<?> task : tasks) {
				configureTrigger(task);
			}
			scheduler.start();
			logger.info("Quartz configured and started!");
		} catch (Exception e) {
			logger.error("Error during quartz configuration", e);
		}

	}

	private void configureTrigger(Bean<?> bean) throws SchedulerException {
		Class<CronTask> taskClass = (Class<CronTask>) bean.getBeanClass();
		CronTask task = newInstance(taskClass);

		Method method = getMethod(taskClass);
		String url = env.get("host") + router.urlFor(taskClass, method);

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

	private Method getMethod(Class<CronTask> taskClass) {
		try {
			return taskClass.getMethod("execute");
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private CronTask newInstance(Class<CronTask> task) {  
		return container.instanceFor(task);
	}

}
