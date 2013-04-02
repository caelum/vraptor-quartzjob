package br.com.caelum.vraptor.quartzjob;

public interface QuartzScheduler {

	void schedule();

	boolean isInitialized();

}
