package br.com.caelum.vraptor.quartzjob;

import java.util.List;

import javax.inject.Inject;

import org.quartz.SchedulerException;

import br.com.caelum.vraptor4.Controller;
import br.com.caelum.vraptor4.Get;
import br.com.caelum.vraptor4.Result;

@Controller
public class QuartzController {

	private List<CronTask> tasks;
	private QuartzConfiguration cfg;
	private QuartzScheduler scheduler;
	private Result result;

	@Deprecated // CDI eyes only
	public QuartzController() {}

	@Inject
	public QuartzController(List<CronTask> tasks,
			QuartzConfiguration cfg, QuartzScheduler
			scheduler, Result result) {

		this.tasks = tasks;
		this.cfg = cfg;
		this.scheduler = scheduler;
		this.result = result;
	}

	@Get("/jobs/configure")
	public void config() throws SchedulerException {
		if(!scheduler.isInitialized()) cfg.configure(tasks);
		result.nothing();
	}
}
