package ch.m1m.infra;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedReorderedGenerator;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class UUIDv6 {

    private TimeBasedReorderedGenerator uuidGenerator = Generators.timeBasedReorderedGenerator();

    public UUID generate() {
        return uuidGenerator.generate();
    }
}
