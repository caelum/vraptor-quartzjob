package br.com.caelum.vraptor.quartzjob;

import br.com.caelum.vraptor.environment.Environment;
import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.quartzjob.http.HttpRequestExecutor;
import br.com.caelum.vraptor.quartzjob.http.QuartzHttpRequestJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class QuartzScheduler {
	public static final String METHOD_FACTORY = "methodFactory";
	
	private static final String JOB_IDENTIFIER = "vraptor-request-job";
	private final static Logger logger = LoggerFactory.getLogger(QuartzScheduler.class);

	private Linker linker;

	private QuartzConfigurator scheduler;

	private HttpRequestExecutor methodFactory;

	private Router router;

	private Environment env;

	@Deprecated // CDI eyes only
	QuartzScheduler() {}

	@Inject
	public QuartzScheduler(Linker linker, QuartzConfigurator scheduler,
						   HttpRequestExecutor methodFactory, Router router,
						   Environment env) {
		this.linker = linker;
		this.scheduler = scheduler;
		this.methodFactory = methodFactory;
		this.router = router;
		this.env = env;
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
		try {
			Constructor<CronTask> defaultConstructor = task.getDeclaredConstructor();
			defaultConstructor.setAccessible(true);
			return defaultConstructor.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
