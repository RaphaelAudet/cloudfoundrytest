import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.tokenprovider.OneTimePasscodeTokenProvider;
import reactor.core.publisher.Mono;

import java.util.concurrent.CountDownLatch;


public class TestApp {

    public static void main(String[] args ) throws InterruptedException {
        String apiHost = "api.eu-gb.bluemix.net";
        String oneTimePassCode = "6Vig4j";


        //1. Get the token
        Mono<String> accessToken = getMonoAccessToken(apiHost, oneTimePassCode);
        //2. use the token
        getOrgsWithToken(apiHost,  accessToken);
    }

    public static Mono<String> getMonoAccessToken(String apiHost, String onetimepasscode) throws InterruptedException {
        // standard step 1 : make a DefaultConnectionContext
        DefaultConnectionContext defaultConnectionContext = DefaultConnectionContext.builder()
                .apiHost(apiHost)
                .build();

        // standard step 2 : make a OneTimePasscodeTokenProvider or a passwordGrantTokenProvider
        OneTimePasscodeTokenProvider tokenProvider = OneTimePasscodeTokenProvider.builder()
                .passcode(onetimepasscode)
                .build();

        // Define your Mono<String> for the token
        Mono<String> token = tokenProvider.getToken(defaultConnectionContext);

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

        return token;
    }

    public static void getOrgsWithToken(String apiHost, Mono<String> accesstoken ) throws InterruptedException {

        // standard step 1 : make a DefaultConnectionContext
        DefaultConnectionContext defaultConnectionContext = DefaultConnectionContext.builder()
                .apiHost(apiHost)
                .build();

        // step 2 : build a custom TokenProvider with the auth token
        TokenProvider customTokenProv = new TokenProvider() {
            @Override
            public Mono<String> getToken(ConnectionContext connectionContext) {
                return accesstoken;
            }
        };

        // step 3 : Make the ReactorCloudFoundryClient with your customTokenProv
        ReactorCloudFoundryClient cloudFoundryClient = ReactorCloudFoundryClient.builder()
                .connectionContext(defaultConnectionContext)
                .tokenProvider(customTokenProv)
                .build();

        // step 4 : Make your DefaultCloudFoundryOperations
        DefaultCloudFoundryOperations cloudFoundryOperations = DefaultCloudFoundryOperations.builder()
                .cloudFoundryClient(cloudFoundryClient)
                .build();


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
    }
}
