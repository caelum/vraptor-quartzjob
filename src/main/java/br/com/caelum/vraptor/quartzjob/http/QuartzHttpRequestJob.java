package br.com.caelum.vraptor.quartzjob.http;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.quartzjob.QuartzConfiguration;

public class QuartzHttpRequestJob implements Job {

	private final static Logger logger = LoggerFactory.getLogger(QuartzHttpRequestJob.class);

	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		JobDataMap data = ctx.getMergedJobDataMap();
		String url = (String) data.get("url");
		HttpRequestExecutor requestExecutor = (HttpRequestExecutor) data.get(QuartzConfiguration.METHOD_FACTORY);

		try {
			logger.info("executing task in URL " + url);
			int statusCode = requestExecutor.execute(url);
			logger.info("task returned status code " + statusCode);
		} catch(Exception e) {
			throw new JobExecutionException(e);
		}
	}
}
