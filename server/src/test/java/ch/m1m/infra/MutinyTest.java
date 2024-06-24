package ch.m1m.infra;

import com.fasterxml.uuid.Generators;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

public class MutinyTest {

    @Test
    void genUUID() {
        System.out.println("java random uuid " + UUID.randomUUID());
        System.out.println("java random uuid " + UUID.randomUUID());

        System.out.println("fast timeRe uuid " + Generators.timeBasedReorderedGenerator().generate());
        System.out.println("fast timeRe uuid " + Generators.timeBasedReorderedGenerator().generate());

        System.out.println("fast timeEp uuid " + Generators.timeBasedEpochGenerator().generate());
        System.out.println("fast timeEp uuid " + Generators.timeBasedEpochGenerator().generate());
    }

    @Test
    void testStreamUni() {
        Uni<String> uni = Uni.createFrom().item(1)
                .onItem().transform(i -> "hello-" + i)
                // delayRequestForSeconds
                .onItem().delayIt().by(Duration.ofSeconds(5));

        System.out.println("calling subscribe() ...");
        uni.subscribe().with(System.out::println);
        System.out.println("test done");
    }
}
