/*
 * Copyright Symsoft AB 1996-2015. All Rights Reserved.
 */
package se.symsoft.codecamp;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import se.symsoft.cc2016.logutil.RequestLoggingFilter;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmsTerminatorService extends ResourceConfig {

    private static final String API_VERSION = "0.0.1";
    private Swagger swagger;
    private DynamoDBMapper dynamoDB;
    private Map<String, TerminatorData> terminatorDataCache = new HashMap<>();

    public SmsTerminatorService() {
        super(SmsDeliverResource.class, ApiListingResource.class, SmsTerminatorResource.class);
        register(RequestLoggingFilter.class);
        register(JacksonJsonProvider.class);
        register(JacksonFeature.class);
        register(SwaggerSerializers.class);
    }


    private HttpServer start() throws IOException {
        AmazonDynamoDBClient amazonDynamoDBClient = new AmazonDynamoDBClient().withRegion(Regions.EU_WEST_1);
        dynamoDB = new DynamoDBMapper(amazonDynamoDBClient);
        initCache();
        initSwagger();
        URI baseUri = UriBuilder.fromUri("http://0.0.0.0").port(8001).build();
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, this);
        server.getServerConfiguration().addHttpHandler(new CLStaticHttpHandler(SmsTerminatorService.class.getClassLoader(), "web/"),"/terminator-ui");

        server.getServerConfiguration().addHttpHandler(new CLStaticHttpHandler(SmsTerminatorService.class.getClassLoader(), "swagger/"),"/swagger");
        server.getServerConfiguration().addHttpHandler(new CLStaticHttpHandler(SmsTerminatorService.class.getClassLoader(), "swagger/js/"),"/js");
        server.getServerConfiguration().addHttpHandler(new CLStaticHttpHandler(SmsTerminatorService.class.getClassLoader(), "swagger/css/"), "/css");

        server.start();
        return  server;
    }

    private void initSwagger() {
        Info info = new Info()
                .title("SMS Terminator API")
                .description("Symsoft Code Camp 2016 SMS Terminator API")
                .termsOfService("http://symsoft.com/api-terms/")
                .contact(new Contact()
                        .email("thomas.babtist@symsoft.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"))
                .version(API_VERSION);

        // Build a base Swagger. It will be updated in runtime once we are deployed.
        swagger = new Swagger().info(info).basePath("/");
        swagger.addTag(new Tag().name("SMS Delivery").description("Operations related to SMS delivery"));
        swagger.addTag(new Tag().name("SMS Terminators").description("Operations related to SMS Terminators"));


    }

    private void initCache() {
        PaginatedScanList<TerminatorData> pageList = getDynamoDB().scan(TerminatorData.class, new DynamoDBScanExpression());
        pageList.stream().forEach(c -> terminatorDataCache.put(c.getRealm(), c));

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                PaginatedScanList<TerminatorData> pageList1 = getDynamoDB().scan(TerminatorData.class, new DynamoDBScanExpression());
                Map<String, TerminatorData> newData = new HashMap<>();
                pageList1.stream().forEach(c -> newData.put(c.getRealm(), c));
                terminatorDataCache = newData;
            }
        }).start();
    }


    public Swagger getSwagger() {
        return swagger;
    }

    public void setSwagger(Swagger swagger) {
        this.swagger = swagger;
    }

    public DynamoDBMapper getDynamoDB() {
        return dynamoDB;
    }

    public TerminatorData getTerminatorData(String realm) {
        return terminatorDataCache.get(realm);
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(1);
        SmsTerminatorService service = new SmsTerminatorService();
        executor.execute(() -> {
            try {
                service.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });


    }


}
