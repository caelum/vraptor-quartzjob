package br.com.caelum.vraptor.quartzjob;

public interface Linker {

	<T> T linkTo(T controller);

	<T> T linkTo(Class<T> controllerType);

	String get();

}