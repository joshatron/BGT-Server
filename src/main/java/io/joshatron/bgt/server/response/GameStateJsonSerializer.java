package io.joshatron.bgt.server.response;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.joshatron.tak.engine.game.GameState;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class GameStateJsonSerializer extends JsonSerializer<GameState> {

    @Override
    public void serialize(GameState gameState, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        JSONObject json = gameState.exportToJson();
        jsonGenerator.writeNumberField("size", json.getInt("size"));
        jsonGenerator.writeNumberField("whiteStones", json.getInt("whiteStones"));
        jsonGenerator.writeNumberField("whiteCapstones", json.getInt("whiteCapstones"));
        jsonGenerator.writeNumberField("blackStones", json.getInt("blackStones"));
        jsonGenerator.writeNumberField("blackCapstones", json.getInt("blackCapstones"));
        jsonGenerator.writeStringField("first", json.getString("first"));
        jsonGenerator.writeStringField("current", json.getString("current"));
        jsonGenerator.writeArrayFieldStart("turns");
        for(int i = 0; i < json.getJSONArray("turns").length(); i++) {
            JSONObject turn = json.getJSONArray("turns").getJSONObject(i);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("type", turn.getString("type"));
            jsonGenerator.writeObjectFieldStart("location");
            jsonGenerator.writeNumberField("x", turn.getJSONObject("location").getInt("x"));
            jsonGenerator.writeNumberField("y", turn.getJSONObject("location").getInt("y"));
            jsonGenerator.writeEndObject();
            if(turn.has("pieceType")) {
                jsonGenerator.writeStringField("pieceType", turn.getString("pieceType"));
            }
            if(turn.has("direction")) {
                jsonGenerator.writeStringField("direction", turn.getString("direction"));
            }
            if(turn.has("pickedUp")) {
                jsonGenerator.writeNumberField("pickedUp", turn.getInt("pickedUp"));
            }
            if(turn.has("placed")) {
                jsonGenerator.writeArrayFieldStart("placed");
                for(int j = 0; j < turn.getJSONArray("placed").length(); j++) {
                    jsonGenerator.writeNumber(turn.getJSONArray("placed").getInt(j));
                }
                jsonGenerator.writeEndArray();
            }
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeArrayFieldStart("board");
        for(int i = 0; i < json.getJSONArray("board").length(); i++) {
            jsonGenerator.writeStartArray();
            for(int j = 0; j < json.getJSONArray("board").getJSONArray(i).length(); j++) {
                jsonGenerator.writeStartArray();
                JSONArray spot = json.getJSONArray("board").getJSONArray(i).getJSONArray(j);
                for(int k = 0; k < spot.length(); k++) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField("type", spot.getJSONObject(k).getString("type"));
                    jsonGenerator.writeStringField("player", spot.getJSONObject(k).getString("player"));
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
