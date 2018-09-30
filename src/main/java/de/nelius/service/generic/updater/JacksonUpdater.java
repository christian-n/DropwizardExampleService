package de.nelius.service.generic.updater;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * @author Christian Nelius
 */
public class JacksonUpdater implements Updater {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> T update(T source, Map<String, Object> dto) {
        try {
            return objectMapper.readerForUpdating(source).readValue(objectMapper.writeValueAsString(dto));
        } catch (IOException e) {
            throw new UpdaterException(e);
        }
    }

}
