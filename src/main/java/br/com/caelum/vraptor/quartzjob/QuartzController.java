package br.com.caelum.vraptor.quartzjob;

import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Result;
import com.google.common.collect.Lists;
import org.quartz.SchedulerException;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

@Controller
public class QuartzController {

	private List<CronTask> tasks;
	private QuartzScheduler cfg;
	private QuartzConfigurator scheduler;
	private Result result;

	@Deprecated // CDI eyes only
	public QuartzController() {}

	@Inject
	public QuartzController(Instance<CronTask> tasks,
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
