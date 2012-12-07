package br.com.caelum.vraptor.quartzjob;

public interface CronTask {
	public static final String EVERY_FIFTEEN_MINUTES = "0 0/15 * * * ?";
	
	void execute();
	String frequency();
}
