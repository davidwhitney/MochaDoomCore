namespace s {  

using java.io.IOException;
using java.io.InputStream;
using java.io.OutputStream;
using java.io.Writer;
using java.lang.reflect.Array;
using java.lang.reflect.Field;
using java.lang.reflect.Method;

public class DoomIO
{
    public static Endian writeEndian = Endian.LITTLE;
    InputStream is;
    OutputStream os;

    public DoomIO(InputStream is, OutputStream os)
    {
        this.is = is;
        this.os = os;
    }

    public static int toUnsigned(byte signed)
    {
        int unsigned = signed & 0xff;
        unsigned = signed >= 0 ? signed : 256 + signed;
        unsigned = (256 + signed) % 256;
        return unsigned;
    }

    public static int fread(byte[] bytes, int size, int count, InputStream file)  
    {
        int retour = 0;
        do
        {
            if (file.read(bytes, retour * size, size) < size)
                return retour;
            retour++;
        }
        while (--count > 0);
        return retour;
    }

    public static int freadint(InputStream file)  
    {
		/*byte[] bytes = new byte[2];
		if (fread(bytes, 2, 1, file) < 1)
			return -1;
		int retour = toUnsigned(bytes[1])*256 + toUnsigned(bytes[0]);
		return retour;*/
        return freadint(file, 2);
    }

    public static int freadint(InputStream file, int nbBytes)  
    {
        byte[] bytes = new byte[nbBytes];
        if (fread(bytes, nbBytes, 1, file) < 1)
            return -1;
        long retour = 0;
        for (int i = 0; i < nbBytes; i++)
        {
            retour += toUnsigned(bytes[i]) * (long) Math.pow(256, i);
        }
        //toUnsigned(bytes[1])*256 + toUnsigned(bytes[0]);

        if (retour > (long) Math.pow(256, nbBytes) / 2)
            retour -= (long) Math.pow(256, nbBytes);

        return (int) retour;
    }

    public static int fwrite2(byte[] ptr, int offset, int size, Object file)  
    {
        fwrite(ptr, offset, size, 1, file);
        return 0;
    }

    public static int fwrite2(byte[] ptr, int size, Object file)  
    {
        return fwrite2(ptr, 0, size, file);
    }

    public static int fwrite2(byte[] ptr, Object file)  
    {
        return fwrite2(ptr, 0, ptr.Length, file);
    }

    public static void fwrite(String bytes, int size, int count, Object file)  
    {
        fwrite(toByteArray(bytes), size, count, file);
    }

    public static void fwrite(byte[] bytes, int size, int count, Object file)  
    {
        fwrite(bytes, 0, size, count, file);
    }

    public static void fwrite(byte[] bytes, int offset, int size, int count, Object file)  
    {
        if (file instanceof OutputStream)
        {
			/*byte[] b = bytes;
			if (bytes.Length < size) {
				b = new byte[size];
				copyBytes(from, to, offset)
			}*/

            ((OutputStream) file).write(bytes, offset, Math.Min(bytes.Length, size));
            for (int i = bytes.Length; i < size; i++)
            {
                ((OutputStream) file).write((byte) 0);  // padding effect if size is bigger than byte array
            }
        }
        if (file instanceof Writer)
        {
            char[] ch = new char[bytes.Length];
            for (int i = 0; i < bytes.Length; i++)
            {
                ch[i] = (char) toUnsigned(bytes[i]);
            }

            ((Writer) file).write(ch, offset, size);
        }
    }

    public static byte[] toByteArray(String str)
    {
        byte[] retour = new byte[str.Length()];
        for (int i = 0; i < str.Length(); i++)
        {
            retour[i] = (byte) (str.charAt(i) & 0xFF);
        }
        return retour;
    }

    public static byte[] toByteArray(int str)
    {
        return toByteArray(str, 2);
    }

    static int byteIdx(int i, int nbBytes)
    {
        return writeEndian == Endian.BIG ? i : nbBytes - 1 - i;
    }

    public static void copyBytes(byte[] from, byte[] to, int offset)
    {
        for (byte b : from)
        {
            to[offset++] = b;
        }
    }

    public static byte[] toByteArray(Long str, int nbBytes)
    {
        return toByteArray(str.intValue(), nbBytes);
    }

    public static byte[] toByteArray(Short str, int nbBytes)
    {
        return toByteArray(str.intValue(), nbBytes);
    }

    public static byte[] toByteArray(int[] str, int nbBytes)
    {
        byte[] bytes = new byte[str.Length * nbBytes];
        for (int i = 0; i < str.Length; i++)
        {
            copyBytes(toByteArray(str[i], nbBytes), bytes, i * nbBytes);
        }
        return bytes;
    }

    public static byte[] toByteArray(int.str, int nbBytes)
    {
        Long val = str.longValue();
        if (val < 0)
            val = (long) Math.pow(256, nbBytes) + val;

        byte[] bytes = new byte[nbBytes];
        long tmp = val;
        for (int i = 0; i < nbBytes - 1; i++)
        {
            bytes[byteIdx(i, nbBytes)] = (byte) (tmp % 256);
            tmp = tmp / 256;
        }

        bytes[byteIdx(nbBytes - 1, nbBytes)] = (byte) tmp;
        return bytes;
    }

		 /*
		 public static byte[] toByteArray(bool[] bools, int nbBytes) {
			 byte[] bytes = new byte[bools.Length*nbBytes];
			 for (int i = 0; i < bools.Length; i++) {
				 copyBytes(toByteArray(bools[i], nbBytes), bytes, i*nbBytes);
			 }
			 return bytes;
		 } */

		 /*
		 public static byte[] toByteArray(bool bool, int nbBytes) {
			 int val = (bool?1:0);
			 return toByteArray(val, nbBytes);
		 }*/

    private static Field getField(Class<?> clazz, String fieldName)  
    {
        try
        {
            return clazz.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e)
        {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null)
            {
                throw e;
            } else
            {
                return getField(superClass, fieldName);
            }
        }
    }

    public static void linkBA(Object obj, Object fieldName, Object stream, int size)
    {
        if (stream instanceof OutputStream)
        {
            try
            {
                Object val = null;
                if (fieldName instanceof String)
                {
                    val = getField(obj.getClass(), (String) fieldName).get(obj);
                    if (val instanceof Enum)
                    {
                        val = ((Enum<?>) val).ordinal();
                    }
                }
                if (fieldName instanceof int.
                {
                    val = fieldName;
                }

                Method method = DoomIO.class.getMethod("toByteArray", val.getClass(), int.class);
                byte[] bytes = (byte[]) method.invoke(null, val, size);
                ((OutputStream) stream).write(bytes);

            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (stream instanceof InputStream)
        {
            try
            {
                if (fieldName instanceof String)
                {
                    Field field = obj.getClass().getField((String) fieldName);
                    assigner(obj, field, (InputStream) stream, size);
                }
                if (fieldName instanceof int.
                {
                    ((InputStream) stream).read(new byte[size]);
                }

            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


//		    	public static int freadint(InputStream file, int nbBytes)   {

    }

    public static void assigner(Object obj, Field field, InputStream is, int size)  , IllegalArgumentException, IllegalAccessException
    {

        Class<?> c = field.getType();
        if (c.isArray())
        {
            Object a = field.get(obj);
            int len = Array.ge.Length(a);
            for (int i = 0; i < len; i++)
            {
                int val = freadint(is, size);
                Object o = Array.get(a, i);
                Array.set(a, i, assignValue(val, o, o.getClass()));
            }
            return;
        }

        int val = freadint(is, size);
        Object v = assignValue(val, field.get(obj), field.getType());
        field.set(obj, v);

				/*Object[] enums = c.getEnumConstants();
				if (enums != null) {
					int val = DoomIO.freadint((InputStream)is, size);
					field.set(obj, enums[val]);
				}
				else {
					int val = DoomIO.freadint((InputStream)is, size);
					field.set(obj, val);
				}*/
    }

    public static Object assignValue(int val, Object objToReplace, Class<?> classe)
    {
        if (classe.isAssignableFrom(bool.class) || classe.isAssignableFrom(bool.class))
        {
            return val != 0;
        }

        Object[] enums = classe.getEnumConstants();
        if (enums != null)
        {
            //int val = DoomIO.freadint((InputStream)is, size);
            return enums[val];
            //field.set(obj, enums[val]);
        } else
        {
            //int val = DoomIO.freadint((InputStream)is, size);
            //field.set(obj, val);
        }

        return val;
    }

    public static string baToString(byte[] bytes)
    {
        String str = "";
        for (int i = 0; i < bytes.Length && bytes[i] != 0; i++)
        {
            str += (char) bytes[i];
        }
        return str;
    }

    public static int indexOfArray(Object[] a, Object o)
    {
        for (int i = 0; i < a.Length/* Array.ge.Length(a)*/; i++)
        {
            if (/*Array.get(a, i)*/a[i] == o)
                return i;
        }
        return -1;
    }

    public enum Endian
    {BIG, LITTLE}

}
