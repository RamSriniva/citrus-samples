Citrus samples ![Logo][1]
==============

JavaEE Arquillian sample
---------
  
The JavaEE sample project uses the Arquillian framework in order to create a micro deployment for in-container testing. 
With Arquillian the tests are executed within application container boundaries. The sample uses the Wildfly application server
as container and provides access to EJB resources, Mail services and JNDI JMS resources such as destinations and connection factories.

The sample integrates with Citrus and Arquillian for automated integration testing of JavaEE applications. You can read more about the
cooperation of Citrus and Arquillian in [reference guide][4].
  
Objectives
---------

The system under test is a JavaEE EJB application that manages employees in a company. We can add, edit and remove employees in a registry over
multiple APIs. The application provides a REST and JMS API for managing the list of employees known to the system.

In a test scenario the needed classes are deployed as Shrinkwrap micro deployments in the Wildfly application container. The Arquillian
framework gives access to container managed resources such as JNDI resources and dependency injection via CDI.

The Maven project POM includes the Arquillian dependencies in combination with an managed Wildfly container.

```xml
<dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.jboss.arquillian</groupId>
        <artifactId>arquillian-bom</artifactId>
        <version>${arquillian.version}</version>
        <scope>import</scope>
        <type>pom</type>
      </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
      <groupId>javax</groupId>
      <artifactId>javaee-api</artifactId>
      <version>${jee.api.version}</version>
      <scope>provided</scope>
    </dependency>
    
    <dependency>
      <groupId>org.jboss.shrinkwrap.resolver</groupId>
      <artifactId>shrinkwrap-resolver-impl-maven</artifactId>
      <version>2.2.2</version>
      <scope>test</scope>
    </dependency>
    
    <!-- Arquillian JUnit -->
    <dependency>
      <groupId>org.jboss.arquillian.junit</groupId>
      <artifactId>arquillian-junit-container</artifactId>
      <version>${arquillian.version}</version>
      <scope>test</scope>
    </dependency>
</dependencies>
```

The dependencies include the **javaee-api**, **shrinkwrap** and **arquillian-junit-container**. In addition to that we need 
the managed Wildfly container configuration.
 
```xml
<dependency>
  <groupId>org.wildfly</groupId>
  <artifactId>wildfly-arquillian-container-managed</artifactId>
  <version>8.2.1.Final</version>
  <scope>test</scope>
</dependency>
``` 

The arquillian tests are configured with a **arquillian.xml** configuration file:

```xml
<arquillian xmlns="http://jboss.org/schema/arquillian" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://jboss.org/schema/arquillian http://jboss.org/schema/arquillian/arquillian_1_0.xsd">

  <defaultProtocol type="Servlet 3.0"/>

  <container qualifier="wildfly-managed" default="true">
    <configuration>
      <property name="jbossHome">${jboss.home}</property>
      <property name="serverConfig">standalone-arquillian.xml</property>
      <property name="allowConnectingToRunningServer">true</property>
    </configuration>
  </container>

  <extension qualifier="citrus">
    <property name="autoPackage">false</property>
    <property name="citrusVersion">2.7</property>
  </extension>
</arquillian>
```

In **citrus-context.xml** we define the Citrus client and server components that are used during the tests.

```xml
<!-- Mail server mock -->
<citrus-mail:server id="mailServer"
                  auto-start="true"
                  port="2222"/>

<!-- Sms Gateway SOAP server -->
<citrus-ws:server id="smsGatewayServer"
                  auto-start="true"
                  port="18008"
                  timeout="5000"/>
```

These components are used during the tests. The EJB JavaEE application connects to those services as client during the test. 
Now lets have a look at a first sample arquillian test with its Shrinkwrap JavaEE deployments.

```java
public class Deployments {

    private static final String CXF_VERSION = "3.1.4";
    private static final String CXF_GROUP_ID = "org.apache.cxf";

    /**
     * Default employee registry application with REST Http resource.
     * @return
     */
    public static WebArchive employeeWebRegistry() {
        return employeeRegistry().addClasses(RegistryApplication.class, EmployeeResource.class);
    }

    /**
     * Default employee registry application with JMS resource MDB.
     * @return
     */
    public static WebArchive employeeJmsRegistry() {
        return employeeRegistry().addClass(EmployeeJmsResource.class);
    }

    /**
     * Default web archive for the employe registry application.
     * @return
     */
    private static WebArchive employeeRegistry() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(
                        MailService.class, SmsGatewayService.class, Employees.class,
                        Employee.class, EmployeeRepository.class)
                .addPackage(SmsGateway.class.getPackage())
                .addAsLibraries(Maven.configureResolver()
                        .workOffline(true)
                        .resolve(CXF_GROUP_ID + ":cxf-rt-frontend-jaxws:" + CXF_VERSION,
                                CXF_GROUP_ID + ":cxf-rt-transports-http:" + CXF_VERSION)
                        .withTransitivity()
                        .asFile());
    }
}
```

The tests use the Arquillian and Citrus resources with annotation based injection. All resources are automatically injected to the
test before it is executed.

```java
@RunWith(Arquillian.class)
@RunAsClient
public class EmployeeResourceTest {

    @CitrusFramework
    private Citrus citrusFramework;

    @ArquillianResource
    private URL baseUri;

    private String serviceUri;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return Deployments.employeeWebRegistry();
    }

    @Before
    public void setUp() throws MalformedURLException {
        serviceUri = new URL(baseUri, "registry/employee").toExternalForm();
    }

    @Test
    @InSequence(1)
    @CitrusTest
    public void testPostAndGet(@CitrusResource TestRunner citrus) {
        citrus.http(httpActionBuilder-> httpActionBuilder
            .client(serviceUri)
            .send()
            .post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body("name=Penny&age=20"));

        citrus.http(httpActionBuilder-> httpActionBuilder
            .client(serviceUri)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        citrus.http(httpActionBuilder-> httpActionBuilder
            .client(serviceUri)
            .send()
            .post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body("name=Leonard&age=21"));

        citrus.http(httpActionBuilder-> httpActionBuilder
            .client(serviceUri)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        citrus.http(httpActionBuilder-> httpActionBuilder
            .client(serviceUri)
            .send()
            .post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body("name=Sheldon&age=22"));

        citrus.http(httpActionBuilder-> httpActionBuilder
            .client(serviceUri)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        citrus.http(httpActionBuilder-> httpActionBuilder
            .client(serviceUri)
            .send()
            .get()
            .accept(MediaType.APPLICATION_XML));

        citrus.http(httpActionBuilder-> httpActionBuilder
            .client(serviceUri)
            .receive()
            .response(HttpStatus.OK)
            .body("<employees>" +
                       "<employee>" +
                         "<age>20</age>" +
                         "<name>Penny</name>" +
                        "</employee>" +
                        "<employee>" +
                          "<age>21</age>" +
                          "<name>Leonard</name>" +
                        "</employee>" +
                        "<employee>" +
                          "<age>22</age>" +
                          "<name>Sheldon</name>" +
                        "</employee>" +
                      "</employees>"));
    }
    
    [...]
}
```

This is how we can integrate Citrus with Arquillian. Both frameworks cooperate with each other in order to make JavaEE applications testable 
with automated in-container deployments.

Run
---------

You can run the tests by calling 

    mvn verify

You should see Citrus performing several tests with lots of debugging output in the terminal. And of course green tests 
at the very end of the build.

Information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html
 [4]: https://citrusframework.org/reference/html#arquillian
