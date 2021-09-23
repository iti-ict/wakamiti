package iti.kukumo.server.spi;

import java.util.Optional;

public interface ApplicationContext {
    Optional<String> user();
}