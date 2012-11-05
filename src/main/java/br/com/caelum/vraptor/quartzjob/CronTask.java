package br.com.caelum.vraptor.quartzjob;

public interface CronTask {
	public static final String EVERY_FIFTEEN_MINUTES = "0 0/15 * * * ?";
	public static final String EVERY_FIRST_DAY = "0 9 1 * * ?";
	void execute();
	String frequency();
}
