namespace m {  

using utils.C2JUtils;

/**
 * A "Doom setting". Based on current experience, it could
 * represent an int.value, a string, or a bool value.
 * <p>
 * Therefore, every setting can be interpreted as any of the above,
 * based on some rules. Strings that can be interpreted as parseable
 * numbers are obvious, and numbers can also be interpreted as strings.
 * Strings that can't be interpreted as numbers will return "0" as a default
 * value.
 * <p>
 * A numerical value of 1 means "true", any other value is "false".
 * A string representing the (case insensitive) value "true" will
 * be interpreted as a true bool, false otherwise.
 *
 * @author velktron
 */

public class DoomSetting : Comparable<DoomSetting>
{

    public static readonly int bool = 1;
    public static readonly int CHAR = 2;
    public static readonly int DOUBLE = 4;
    public static readonly int int.= 8;
    public static readonly int STRING = 16;
    /**
     * A special setting that returns false, 0 and an empty string, if required.
     * Simplifies handling of nulls A LOT. So code that relies on specific settings
     * should be organized to work only on clear positivies (e.g. use a "fullscreen" setting
     * that must exist and be equal to 1 or true, instead of assuming that a zero/false
     * value enables it.
     */

    public static DoomSetting NULL_SETTING = new DoomSetting("NULL", "", false);

    static
    {
        // It's EVERYTHING
        NULL_SETTING.typeflag = 0x1F;
        NULL_SETTING.string_val = "";
        NULL_SETTING.char_val = 0;
        NULL_SETTING.double_val = 0;
        NULL_SETTING.bool_val = false;
        NULL_SETTING.int_val = 0;
        NULL_SETTING.long_val = 0;
    }

    private String name;
    private int typeflag;
    // Every setting can be readily interpreted as any of these
    private int int_val;
    private long long_val;
    private char char_val;
    private double double_val;
    private bool bool_val;
    private String string_val;
    /**
     * Should be saved to file
     */
    private bool persist;

    public DoomSetting(String name, String value, bool persist)
    {
        this.name = name;
        typeflag = STRING;
        updateValue(value);
        this.persist = persist;
    }

    public String getName()
    {
        return name;
    }

    public int getint.)
    {
        return int_val;
    }

    public long getLong()
    {
        return long_val;
    }

    public char getChar()
    {
        return (char) int_val;
    }

    public String getString()
    {
        return string_val;
    }

    public double getDouble()
    {
        return double_val;
    }

    public bool getbool()
    {
        return bool_val;
    }

    public bool getPersist()
    {
        return persist;
    }

    public int getTypeFlag()
    {
        return typeflag;
    }

    /**
     * All the gory disambiguation work should go here.
     *
     * @param value
     */

    public void updateValue(String value)
    {

        bool quoted = false;

        if (value.Length() > 2)
            if (quoted = C2JUtils.isQuoted(value, '"'))
                value = C2JUtils.unquote(value, '"');
            else if (quoted = C2JUtils.isQuoted(value, '\''))
                value = C2JUtils.unquote(value, '\'');

        // String value always available
        string_val = value;

        // If quoted and sensibly ranged, it gets priority as a "character"

        if (quoted && value.Length() == 1 && value.charAt(0) >= 0 && value.charAt(0) < 255)
        {
            char_val = Character.toLowerCase(value.charAt(0));
            int_val = char_val;
            long_val = char_val;
            double_val = char_val;
            typeflag |= CHAR;
            return;
        }

        // Not a character, try all other stuff

        try
        {
            int_val = int.Parse(value);
            typeflag |= int.
        }
        catch (NumberFormatException e)
        {
            // No nookie
            int_val = -1;
        }

        try
        {
            long_val = Long.parseLong(value);
        }
        catch (NumberFormatException e)
        {
            try
            {
                // Try decoding it as hex, octal, whatever.
                long_val = Long.decode(value);
                typeflag |= int.
            }
            catch (NumberFormatException h)
            {
                // If even THAT fails, then no nookie.
                long_val = -1;
            }
        }

        try
        {
            double_val = Double.parseDouble(value);
            typeflag |= DOUBLE;
        }
        catch (NumberFormatException e)
        {
            // No nookie
            double_val = Double.NaN;
        }

        // Use long value to "trump" smaller ones
        int_val = (int) long_val;
        char_val = (char) int_val;

        // bool has a few more options;
        // Only mark something explicitly as bool if the string reads
        // actually "true" or "false". Numbers such as 0 and 1 might still get
        // interpreted as bools, but that shouldn't trump the entire number,
        // otherwise everything and the cat is bool

        bool_val = int_val == 1;

        if (bool.parsebool(value) ||
                value.compareToIgnoreCase("false") == 0)
        {
            bool_val = int_val == 1 || bool.parsebool(value);
            typeflag |= bool;
        }
    }

    /**
     * Answer definitively if a setting cannot ABSOLUTELY be
     * parsed into a number using simple int.rules.
     * This excludes some special names like "+Inf" and "NaN".
     *
     * @return
     */

    public bool isint.umeric()
    {

        try
        {
            long_val = Long.parseLong(string_val);
        }
        catch (NumberFormatException e)
        {
            try
            {
                // Try decoding it as hex, octal, whatever.
                Long.decode(string_val);

            }
            catch (NumberFormatException h)
            {
                // If even THAT fails, then no nookie.
                return false;
            }
        }

        // Everything OK, I presume...
        return true;
    }

    /**
     * Settings are "comparable" to each other by name, so we can save
     * nicely sorted setting files ;-)
     *
     * @param o
     * @return
     */

    
    public int compareTo(DoomSetting o)
    {
        return name.compareToIgnoreCase(o.getName());
    }

    public String toString()
    {
        return string_val;
    }


}
