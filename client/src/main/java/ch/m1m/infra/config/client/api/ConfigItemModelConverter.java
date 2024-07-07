package ch.m1m.infra.config.client.api;

import ch.m1m.config.model.ConfigItemModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Arrays;
import java.util.List;

public class ConfigItemModelConverter {

    ObjectMapper objectMapper = new ObjectMapper();

    public ConfigItemModelConverter() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public ConfigItemModel toObject(String input) throws JsonProcessingException {
        return objectMapper.readValue(input, ConfigItemModel.class);
    }

    public String toJson(ConfigItemModel object) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    public ConfigItemModel[] toArray(String input) throws JsonProcessingException {
        return objectMapper.readValue(input, ConfigItemModel[].class);
    }

    public List<ConfigItemModel> toList(String input) throws JsonProcessingException {
        ConfigItemModel[] array = toArray(input);
        return Arrays.asList(array);
    }

    public String toJson(ConfigItemModel[] objArray) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objArray);
    }
}
