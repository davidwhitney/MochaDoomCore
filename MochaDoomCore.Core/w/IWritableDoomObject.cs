using System.IO;

namespace w
{
    public interface IWritableDoomObject
    {
        void write(Stream dos);
    }
}