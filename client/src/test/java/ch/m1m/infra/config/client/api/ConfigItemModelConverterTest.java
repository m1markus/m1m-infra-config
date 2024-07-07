package ch.m1m.infra.config.client.api;

import ch.m1m.config.model.ConfigItemModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigItemModelConverterTest {

    ConfigItemModelConverter cut = new ConfigItemModelConverter();

    @Test
    void convertObject_toJson_and_backToObject() throws JsonProcessingException {
        // arrange
        ConfigItemModel itemRef = createConfigItemModel();

        // act
        String objAsJson = cut.toJson(itemRef);
        ConfigItemModel itemActual = cut.toObject(objAsJson);

        // assert
        assertEquals(itemRef, itemActual);
    }

    @Test
    void convertObjectArray_toJson_and_backToObjectArray() throws JsonProcessingException {
        // arrange
        ConfigItemModel refArray[] = createConfigItemModelArray();

        // act
        String objArrAsJson = cut.toJson(refArray);
        ConfigItemModel actualArray[] = cut.toArray(objArrAsJson);

        // assert
        assertEquals(refArray[0], actualArray[0]);
        assertEquals(refArray[1], actualArray[1]);
    }

    @Test
    void convertObjectArray_to_Array() throws JsonProcessingException {
        // arrange
        ConfigItemModel refArray[] = createConfigItemModelArray();
        String jsonArray = cut.toJson(refArray);

        // act
        List<ConfigItemModel> objList = cut.toList(jsonArray);

        // assert
        assertEquals(2, objList.size());
    }

    private ConfigItemModel createConfigItemModel() {
        return createConfigItemModelWithBuilder("019027c7-faf6-7db5-a16b-135db2ab22d6",
                "myKey-1", "myValue-1", "just an example key-1 / value-1");
    }

    private ConfigItemModel createConfigItemModelWithBuilder(String id, String key, String value, String desc ) {
        return ConfigItemModel.builder()
                .id(id)
                .key(key)
                .value(value)
                .description(desc)
                .created_at(LocalDateTime.now())
                .domain("example.com")
                .application("batch")
                .build();
    }

    private ConfigItemModel[] createConfigItemModelArray() {
        ConfigItemModel item1 = createConfigItemModel();
        ConfigItemModel item2 = createConfigItemModelWithBuilder("01907f2b-5a87-7376-a2ae-2f7c602424d0",
                "myKey-2", "myValue-2", "just an example key-2 / value-2");

        ConfigItemModel[] itemArr = new ConfigItemModel[2];
        itemArr[0] = item1;
        itemArr[1] = item2;
        return itemArr;
    }
}
