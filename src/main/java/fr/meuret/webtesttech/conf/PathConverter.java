package fr.meuret.webtesttech.conf;

import com.beust.jcommander.IStringConverter;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Path converter for converting command line option to a Path structure.
 *
 * @see Path
 */
public class PathConverter implements IStringConverter<Path> {
    @Override
    public Path convert(String s) {
        return Paths.get(s);

    }
}
