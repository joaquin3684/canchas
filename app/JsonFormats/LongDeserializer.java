package JsonFormats;


import akka.parboiled2.ParserInput;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import scala.Float;
import scala.Int;
import java.lang.Long;
import scala.StringContext;
import scala.collection.mutable.StringBuilder;
import scala.math.Ordering;

import java.io.IOException;

public class LongDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Long v =  p.getValueAsLong();
        return v;
    }
}
