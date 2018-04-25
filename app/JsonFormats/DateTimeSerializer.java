package JsonFormats;

import akka.http.scaladsl.model.DateTime;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class DateTimeSerializer extends JsonSerializer<DateTime> {

    @Override
    public void serialize(
            DateTime value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeString( value.toIsoDateTimeString());
    }
}