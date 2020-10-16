/*
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
namespace utils {  

using java.io.BufferedReader;
using java.io.BufferedWriter;
using java.io.File;
using java.io.IOException;
using java.nio.charset.Charset;
using java.nio.file.FileSystems;
using java.nio.file.Files;
using java.nio.file.OpenOption;
using java.nio.file.Path;
using java.util.function.Consumer;
using java.util.function.Supplier;

/**
 * Resource IO to automate read/write on configuration/resources
 *
 * @author Good Sign
 */
public class ResourceIO
{

    private readonly Path file;
    private readonly Charset charset = Charset.forName("US-ASCII");

    public ResourceIO(File file)
    {
        this.file = file.toPath();
    }

    public ResourceIO(Path file)
    {
        this.file = file;
    }

    public ResourceIO(String path)
    {
        file = FileSystems.getDefault().getPath(path);
    }

    public bool exists()
    {
        return Files.exists(file);
    }

    public bool readLines(Consumer<String> lineConsumer)
    {
        if (Files.exists(file))
        {
            try (BufferedReader reader = Files.newBufferedReader(file, charset))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    lineConsumer.accept(line);
                }

                return true;
            }
            catch (IOException x)
            {
                System.err.format("IOException: %s%n", x);
                return false;
            }
        }

        return false;
    }

    public bool writeLines(Supplier<String> lineSupplier, OpenOption... options)
    {
        try (BufferedWriter writer = Files.newBufferedWriter(file, charset, options))
        {
            String line;
            while ((line = lineSupplier.get()) != null)
            {
                writer.write(line, 0, line.length());
                writer.newLine();
            }

            return true;
        }
        catch (IOException x)
        {
            System.err.format("IOException: %s%n", x);
            return false;
        }
    }

    public String getFileame()
    {
        return file.toString();
    }
}
