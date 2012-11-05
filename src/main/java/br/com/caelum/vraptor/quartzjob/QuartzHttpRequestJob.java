package br.com.caelum.vraptor.quartzjob;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzHttpRequestJob implements Job {

	private final static Logger logger = LoggerFactory.getLogger(QuartzHttpRequestJob.class);

	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		String url = (String) ctx.getMergedJobDataMap().get("url");

		try {
			logger.info("executing task in URL " + url);
			
			HttpClient client = new HttpClient();
			int statusCode = client.executeMethod(new PostMethod(url));
			
			logger.info("task returned status code " + statusCode);
			
		} catch(Exception e) {
			throw new JobExecutionException(e);
		}
	}
}
