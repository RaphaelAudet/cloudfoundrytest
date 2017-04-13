import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.tokenprovider.OneTimePasscodeTokenProvider;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class TestApp {

    public static void main(String[] args ) throws InterruptedException {
//        String apiHost = "api.eu-gb.bluemix.net";
        String apiHost = "api.run.pivotal.io";
//        String oneTimePassCode = "B5pVwj";
        String username ="missa";
        String password = "missapassword";


        getMonoAccessTokenPassword(apiHost, username, password);

//        getOrgsOldSchoolPasscode(apiHost, oneTimePassCode);

        //1. Get the token
//        Mono<String> accessToken = getMonoAccessToken(apiHost, oneTimePassCode);
        //2. use the token
//        getOrgsWithToken(apiHost,  accessToken);
    }

    public static Mono<String> getMonoAccessTokenPasscode(String apiHost, String onetimepasscode) throws InterruptedException {
        System.out.println("step 1");
        // standard step 1 : make a DefaultConnectionContext
        DefaultConnectionContext defaultConnectionContext = DefaultConnectionContext.builder()
                .apiHost(apiHost)
                .build();

        System.out.println("step 2");
        // standard step 2 : make a OneTimePasscodeTokenProvider or a passwordGrantTokenProvider
        OneTimePasscodeTokenProvider tokenProvider = OneTimePasscodeTokenProvider.builder()
                .passcode(onetimepasscode)
                .build();

        System.out.println("step 3");
        // Define your Mono<String> for the token
        Mono<String> token = tokenProvider.getToken(defaultConnectionContext);

        System.out.println("step 4");
        // Send request
        CountDownLatch latch1 = new CountDownLatch(1);
        token.map(e -> e)
                .subscribe(
                        System.out::println,
                        throwable -> {
                            throwable.printStackTrace();
                            latch1.countDown();
                        }
                        , () -> latch1.countDown()
                );

        latch1.await();
        System.out.println("step 5");
        return token;
    }

    public static Mono<String> getMonoAccessTokenPassword(String apiHost, String username, String password) throws InterruptedException {
        System.out.println("step 1");
        // standard step 1 : make a DefaultConnectionContext
        DefaultConnectionContext defaultConnectionContext = DefaultConnectionContext.builder()
                .apiHost(apiHost)
                .build();

        System.out.println("step 2");
        // standard step 2 : make a OneTimePasscodeTokenProvider or a passwordGrantTokenProvider
            PasswordGrantTokenProvider tokenProvider  = PasswordGrantTokenProvider.builder()
                    .username(username)
                    .password(password)
                    .build();

        System.out.println("step 3");
        // Define your Mono<String> for the token
        Mono<String> token = tokenProvider.getToken(defaultConnectionContext);

        System.out.println("step 4");
        // Send request
        CountDownLatch latch1 = new CountDownLatch(1);
        token.map(e -> e)
                .subscribe(
                        System.out::println,
                        throwable -> {
                            throwable.printStackTrace();
                            latch1.countDown();
                        }
                        , () -> latch1.countDown()
                );

        latch1.await();
        System.out.println("step DONE");
        List<String> myToken = new ArrayList<String>();
        token.subscribe(e -> myToken.add(e));
        System.out.println(myToken.get(0));
        return token;
    }

    public static void getOrgsWithToken(String apiHost, Mono<String> accesstoken ) throws InterruptedException {

        System.out.println("step 6");
        // standard step 1 : make a DefaultConnectionContext
        DefaultConnectionContext defaultConnectionContext = DefaultConnectionContext.builder()
                .apiHost(apiHost)
                .build();

        System.out.println("step 7");
        // step 2 : build a custom TokenProvider with the auth token
        TokenProvider customTokenProv = new TokenProvider() {
            @Override
            public Mono<String> getToken(ConnectionContext connectionContext) {
                return accesstoken;
            }
        };

        System.out.println("step 8");
        // step 3 : Make the ReactorCloudFoundryClient with your customTokenProv
        ReactorCloudFoundryClient cloudFoundryClient = ReactorCloudFoundryClient.builder()
                .connectionContext(defaultConnectionContext)
                .tokenProvider(customTokenProv)
                .build();


        System.out.println("step 9");
        // step 4 : Make your DefaultCloudFoundryOperations
        DefaultCloudFoundryOperations cloudFoundryOperations = DefaultCloudFoundryOperations.builder()
                .cloudFoundryClient(cloudFoundryClient)
                .build();



        System.out.println("step 10");
        // Send request
        CountDownLatch latch = new CountDownLatch(1);
        cloudFoundryOperations.organizations()
                .list()
                .map(orgitem -> orgitem.getName())
                .subscribe(System.out::println, throwable -> {
                            throwable.printStackTrace();
                            latch.countDown();
                        }
                        , () -> latch.countDown());
        latch.await();

        System.out.println("step DONE");
    }

    public static void getOrgsOldSchoolPasscode(String apiHost, String onetimepasscode)  throws InterruptedException {
        System.out.println("Old School Onetimepasscode");
        DefaultConnectionContext defaultConnectionContext = DefaultConnectionContext.builder()
                .apiHost(apiHost)
                .build();
        OneTimePasscodeTokenProvider tokenProvider = OneTimePasscodeTokenProvider.builder()
                .passcode(onetimepasscode)
                .build();

        ReactorCloudFoundryClient cloudFoundryClient = ReactorCloudFoundryClient.builder()
                .connectionContext(defaultConnectionContext)
                .tokenProvider(tokenProvider)
                .build();

        DefaultCloudFoundryOperations cloudFoundryOperations = DefaultCloudFoundryOperations.builder()
                .cloudFoundryClient(cloudFoundryClient)
                .build();

        System.out.println("step sending");
        // Send request
        CountDownLatch latch = new CountDownLatch(1);
        cloudFoundryOperations.organizations()
                .list()
                .map(orgitem -> orgitem.getName())
                .subscribe(System.out::println, throwable -> {
                            throwable.printStackTrace();
                            latch.countDown();
                        }
                        , () -> latch.countDown());
        latch.await();

        System.out.println("step DONE");
    }

    public static void getOrgsOldSchoolPassword(String apiHost, String username, String password)  throws InterruptedException {
        System.out.println("Old School password");
        DefaultConnectionContext defaultConnectionContext = DefaultConnectionContext.builder()
                .apiHost(apiHost)
                .build();

        PasswordGrantTokenProvider tokenProvider  = PasswordGrantTokenProvider.builder()
                .username(username)
                .password(password)
                .build();

        ReactorCloudFoundryClient cloudFoundryClient = ReactorCloudFoundryClient.builder()
                .connectionContext(defaultConnectionContext)
                .tokenProvider(tokenProvider)
                .build();

        DefaultCloudFoundryOperations cloudFoundryOperations = DefaultCloudFoundryOperations.builder()
                .cloudFoundryClient(cloudFoundryClient)
                .build();

        System.out.println("sending");
        // Send request
        CountDownLatch latch = new CountDownLatch(1);
        cloudFoundryOperations.organizations()
                .list()
                .map(orgitem -> orgitem.getName())
                .subscribe(System.out::println, throwable -> {
                            throwable.printStackTrace();
                            latch.countDown();
                        }
                        , () -> latch.countDown());
        latch.await();

        System.out.println("step DONE");
    }
}
