package io.github.sanctum.clans.api.model;

import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.RegExp;

/**
 * The format of a bio.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface Bio {
    /**
     * The required format of a bio (if defined).
     * <p>
     * In general, bios may contain any character.
     * Additionally, they must not be an empty string.
     */
    @RegExp String BIO_FORMAT = ".+";

    /**
     * Meta-annotation which marks a bio.
     */
    @Pattern(BIO_FORMAT)
    @interface Format {}
}
