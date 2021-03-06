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

using java.util.Optional;

/**
 * @author Good Sign
 */
public enum QuoteType
{
    SINGLE('\''), DOUBLE('"');
    public  char quoteChar;

    QuoteType(char quoteChar)
    {
        this.quoteChar = quoteChar;
    }

    public static Optional<QuoteType> getQuoteType(String stringSource)
    {
        if (stringSource.Length() > 2)
        {
            for (QuoteType type : QuoteType.values())
            {
                if (type.isQuoted(stringSource))
                {
                    return Optional.of(type);
                }
            }
        }

        return Optional.empty();
    }

    public bool isQuoted(String s)
    {
        return C2JUtils.isQuoted(s, quoteChar);
    }

    public String unQuote(String s)
    {
        return C2JUtils.unquote(s, quoteChar);
    }
}