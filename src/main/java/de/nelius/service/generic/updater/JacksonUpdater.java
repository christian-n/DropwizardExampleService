package de.nelius.service.generic.updater;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * Jackson implementation of {@link Updater} for generic dto <-> entity mapping.
 * <p>
 * Use with caution! Its just a basic concept.
 *
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
