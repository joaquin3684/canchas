package JsonFormats;

import akka.http.scaladsl.model.DateTime;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import scala.Option;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class DateTimeDeserializer extends JsonDeserializer<DateTime> {

    @Override
    public DateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
          DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd'T'hh:mm:ss");
           Option<DateTime> v = DateTime.fromIsoDateTimeString(p.getText());
            return v.get();
    }
}
