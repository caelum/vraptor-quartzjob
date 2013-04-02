package br.com.caelum.vraptor.quartzjob;

import java.util.List;

import org.quartz.SchedulerException;

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.mauth.Open;

@Resource
@Open
public class QuartzController {

	private final List<CronTask> tasks;
	private final QuartzConfiguration cfg;
	private final DefaultQuartzScheduler scheduler;
	private final Result result;

	public QuartzController(List<CronTask> tasks, QuartzConfiguration cfg, DefaultQuartzScheduler scheduler, Result result) {
		this.tasks = tasks;
		this.cfg = cfg;
		this.scheduler = scheduler;
		this.result = result;
	}
	
	@Get("/jobs/configure")
	public void config() {
			try {
				if (!scheduler.isInitialized())
					cfg.configure(tasks);
				result.nothing();
			} catch (SchedulerException e) {
				throw new RuntimeException("could not configure scheduler", e);
			}
	}
}
