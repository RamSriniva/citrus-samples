JDBC sample ![Logo][1]
==============

This sample uses JDBC database connection to verify stored data in SQL query results sets.

Objectives
---------

The [todo-list](../todo-app/README.md) sample application stores data to a relational database. This sample shows 
the usage of database JDBC validation actions in Citrus. We are able to execute SQL statements on a database target. 
See the [reference guide][4] database chapter for details.

The database source is configured as Spring datasource in the application context ***citrus-context.xml***.
    
```java
@Bean
public SingleConnectionDataSource dataSource() {
    SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
    dataSource.setDriverClassName(JdbcDriver.class.getName());
    dataSource.setUrl("jdbc:citrus:http://localhost:3306/testdb");
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    return dataSource;
}
```
    
As you can see we are using a special Citrus JDBC driver here. This driver connects to the Citrus JDBC server mock.    

In the test case we can verify any JDBC operation on the datasource without having to actually create the data in the database.

```java
$(receive()
    .endpoint(jdbcServer)
    .type(MessageType.JSON)
    .message(JdbcMessage.execute("SELECT id, title, description FROM todo_entries")));

$(send()
    .endpoint(jdbcServer)
    .type(MessageType.JSON)
    .message(JdbcMessage.success().dataSet("[ {" +
                "\"id\": \"" + UUID.randomUUID().toString() + "\"," +
                "\"title\": \"${todoName}\"," +
                "\"description\": \"${todoDescription}\"," +
                "\"done\": \"false\"" +
            "} ]")));
```

Run
---------

**NOTE:** This test depends on the [todo-app](../todo-app/) WAR which must have been installed into your local maven repository using `mvn clean install` beforehand.

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean verify -Dsystem.under.test.mode=embedded
    
This executes the complete Maven build lifecycle. The embedded option automatically starts a Jetty web
container before the integration test phase. The todo-list system under test is automatically deployed in this phase.
After that the Citrus test cases are able to interact with the todo-list application in the integration test phase.

During the build you will see Citrus performing some integration tests.
After the tests are finished the embedded Jetty web container and the todo-list application are automatically stopped.

System under test
---------

The sample uses a small todo list application as system under test. The application is a web application
that you can deploy on any web container. You can find the todo-list sources [here](../todo-app). Up to now we have started an 
embedded Jetty web container with automatic deployments during the Maven build lifecycle. This approach is fantastic 
when running automated tests in a continuous build.
  
Unfortunately the Jetty server and the sample application automatically get stopped when the Maven build is finished. 
There may be times we want to test against a standalone todo-list application.  

You can start the sample todo list application in Jetty with this command.

     mvn jetty:run

This starts the Jetty web container and automatically deploys the todo list app. Point your browser to
 
    http://localhost:8080/todolist/

You will see the web UI of the todo list and add some new todo entries.

Now we are ready to execute some Citrus tests in a separate JVM.

Citrus test
---------

Once the sample application is deployed and running you can execute the Citrus test cases.
Open a separate command line terminal and navigate to the sample folder.

Execute all Citrus tests by calling

     mvn verify

You can also pick a single test by calling

     mvn verify -Dit.test=<testname>

You should see Citrus performing several tests with lots of debugging output in both terminals (sample application server
and Citrus test client). And of course green tests at the very end of the build.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
 [4]: https://citrusframework.org/reference/html#actions-database
