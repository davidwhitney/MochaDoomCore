using System;

namespace boom
{

    public class prboom_comp_t
    {

        public int minver;
        public int maxver;
        public bool state;
        public String cmd;

        public prboom_comp_t(int minver, int maxver, bool state, String cmd)
        {
            this.minver = minver;
            this.maxver = maxver;
            this.state = state;
            this.cmd = cmd;
        }
    }
}