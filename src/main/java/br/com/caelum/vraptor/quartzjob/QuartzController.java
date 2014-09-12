package br.com.caelum.vraptor.quartzjob;

import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.quartz.SchedulerException;

import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Result;

import com.google.common.collect.Lists;

@Controller
public class QuartzController {

	private List<CronTask> tasks;
	private QuartzScheduler cfg;
	private QuartzConfigurator scheduler;
	private Result result;

	@Deprecated // CDI eyes only
	public QuartzController() {}

	@Inject
	public QuartzController(@Any Instance<CronTask> tasks,
			QuartzScheduler cfg, QuartzConfigurator
			scheduler, Result result) {
		this.tasks = Lists.newArrayList(tasks);
		this.cfg = cfg;
		this.scheduler = scheduler;
		this.result = result;
	}

	@Get("/jobs/configure")
	public void config() throws SchedulerException {
		if (!scheduler.isInitialized()) 
			cfg.configure(tasks);
		result.nothing();
	}
}
