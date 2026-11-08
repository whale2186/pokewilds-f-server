package com.pkmngen.game;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/** Persistent local identities; never replace a caller's existing identity file. */
final class LanIdentity {
    private LanIdentity() { }

    static String player(Path directory, String displayName) throws IOException {
        Files.createDirectories(directory);
        Path stable = directory.resolve("coop-profile.txt");
        if (Files.exists(stable)) return loadOrCreate(stable);
        Path legacy = directory.resolve("coop-profile-" + displayName + ".txt");
        if (!Files.exists(legacy)) {
            try (java.util.stream.Stream<Path> files = Files.list(directory)) {
                Path[] candidates = files.filter(p -> p.getFileName().toString().matches("coop-profile-.+\\.txt")).toArray(Path[]::new);
                if (candidates.length > 1) throw new IOException("Multiple legacy LAN profiles; select the original runtime directory/profile before migrating");
                if (candidates.length == 1) legacy = candidates[0];
            }
        }
        if (Files.exists(legacy)) {
            String identity = loadOrCreate(legacy);
            try { Files.write(stable, identity.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW); }
            catch (FileAlreadyExistsException won) { /* Validate the installed identity. */ }
        }
        String identity = loadOrCreate(stable);
        Path alias = directory.resolve("coop-profile-" + displayName + ".txt");
        if (!Files.exists(alias)) {
            try { Files.write(alias, identity.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW); }
            catch (FileAlreadyExistsException won) { /* Stable profile remains canonical. */ }
        }
        return identity;
    }

    static String loadOrCreate(Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        if (!Files.exists(file)) {
            try {
                Files.write(file, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            } catch (FileAlreadyExistsException otherProcessWon) { /* Validate the winner below. */ }
        }
        if (Files.size(file) != 36) throw new IOException("Invalid LAN identity file: " + file);
        String value = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        try {
            if (UUID.fromString(value).toString().equals(value)) return value;
        } catch (IllegalArgumentException invalid) { /* Fail closed; do not silently change saves. */ }
        throw new IOException("Invalid LAN identity file: " + file);
    }
}
