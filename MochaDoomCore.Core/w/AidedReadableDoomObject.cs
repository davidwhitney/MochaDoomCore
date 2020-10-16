using System.IO;

namespace w
{
    /**
 * This is for objects that can be read from disk, but cannot
 * self-determine their own.Length for some reason.
 *
 * @author Maes
 */

    public interface AidedReadableDoomObject
    {

        void read(Stream f, int len);
    }
}