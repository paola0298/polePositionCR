package Client.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonTest {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        var json = "{\"status\":\"success\"}";
        JsonNode root;
        try {
            root = mapper.readTree(json);
        } catch (JsonProcessingException ex) {
            System.err.println("Error parsing json.");
            return;
        }

        System.out.println(root.toPrettyString());
    }
}
