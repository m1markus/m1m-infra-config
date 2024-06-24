package ch.m1m.infra;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class UUIDv7 {

    private TimeBasedEpochGenerator uuidGenerator = Generators.timeBasedEpochGenerator();

    public UUID generate() {
        return uuidGenerator.generate();
    }
}
