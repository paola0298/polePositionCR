package Client.Logic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ConnectionTest {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put("action", "get_track");

        String outdata;
        try{
            outdata = mapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("OutData: " + outdata);

        Connection con = new Connection("localhost", 8080);
        var data = con.connect(outdata);

        JsonNode result;
        try {
            result = mapper.readTree(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("[Server] " + result.toPrettyString());
    }
}
