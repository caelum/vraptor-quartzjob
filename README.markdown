# vraptor-quartzjob

A simple quartz scheduler

## installing

Vraptor-quartzjob.jar can be downloaded from maven's repository, or configured in any compatible tool:

```xml
<dependency>
    <groupId>br.com.caelum.vraptor</groupId>
    <artifactId>vraptor-quartzjob</artifactId>
    <version>4.0.2</version> <!--or the latest version-->
    <scope>compile</scope>
</dependency>
```

## using

You must configure the host of your application in with VRaptor's environment support.
So, for example, add to your development.properties de following configuration:

```properties
host=http://localhost:8080
```

For more information about environments, check the [VRaptor docs](http://www.vraptor.org/en/docs/environment).

To schedule jobs in your application, simply create a controller and implement the CronTask interface:

```
@Controller
public class MyJob implements CronTask {
    @Override
    public void execute() {
        System.out.println("executing job");
    }

    @Override
    public String frequency() {
        return "*/10 * * * * ?";
    }
}
```

With this class, vraptor-quartzjob will execute a request to this controller every 10 minutes.

The String returned in `frequency` method should be a unix-like cron expression used by quartz.
This format is well documented with a lot of examples in the quartz documentation:
http://www.quartz-scheduler.org/documentation/quartz-2.2.x/tutorials/tutorial-lesson-06

# help

Get help from vraptor developers and the community at vraptor mailing list.
