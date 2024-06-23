package ch.m1m.infra;

import io.smallrye.mutiny.subscription.UniEmitter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class PollMapEntry {
    private String pollId;
    private String clientRegisterKey;
    private UniEmitter<? super Object> emitter;
}
