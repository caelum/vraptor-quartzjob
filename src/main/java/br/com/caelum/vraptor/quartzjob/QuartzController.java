package br.com.caelum.vraptor.quartzjob;

import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.controller.DefaultBeanClass;
import com.google.common.collect.Lists;
import org.quartz.SchedulerException;

import javax.ejb.Schedule;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class QuartzController {

	private BeanManager beanManager;
	private List<CronTask> tasks;
	private QuartzScheduler cfg;
	private QuartzConfigurator scheduler;
	private Result result;

	@Deprecated // CDI eyes only
	public QuartzController() {}

	@Inject
	public QuartzController(QuartzScheduler cfg, QuartzConfigurator
			scheduler, Result result, BeanManager beanManager) {
		this.beanManager = beanManager;
		this.cfg = cfg;
		this.scheduler = scheduler;
		this.result = result;
	}

	@Get("/jobs/configure")
	public void config() throws SchedulerException {
		Set<Bean<?>> beans = beanManager.getBeans(CronTask.class);
		if (!scheduler.isInitialized())
			cfg.configure(beans);
		result.nothing();
	}

}