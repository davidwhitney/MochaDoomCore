namespace f {  

using v.graphics.Wipers;

public interface Wiper
{
    bool ScreenWipe(Wipers.WipeType type, int x, int y, int width, int height, int ticks);

    bool EndScreen(int x, int y, int width, int height);

    bool StartScreen(int x, int y, int width, int height);

    enum Wipe : Wipers.WipeType
    {
        // simple gradual pixel change for 8-bit only
        // MAES: this transition isn't guaranteed to always terminate
        // see Chocolate Strife develpment. Unused in Doom anyway.
        ColorXForm(
                Wipers.WipeFunc.initColorXForm,
                Wipers.WipeFunc.doColorXForm,
                Wipers.WipeFunc.exitColorXForm
        ),
        // weird screen melt
        Melt(
                Wipers.WipeFunc.initMelt,
                Wipers.WipeFunc.doMelt,
                Wipers.WipeFunc.exitMelt
        ),
        ScaledMelt(
                Wipers.WipeFunc.initScaledMelt,
                Wipers.WipeFunc.doScaledMelt,
                Wipers.WipeFunc.exitMelt
        );

        private readonly Wipers.WipeFunc initFunc;
        private readonly Wipers.WipeFunc doFunc;
        private readonly Wipers.WipeFunc exitFunc;

        Wipe(Wipers.WipeFunc initFunc, Wipers.WipeFunc doFunc, Wipers.WipeFunc exitFunc)
        {
            this.initFunc = initFunc;
            this.doFunc = doFunc;
            this.exitFunc = exitFunc;
        }

        
        public Wipers.WipeFunc getDoFunc()
        {
            return doFunc;
        }

        
        public Wipers.WipeFunc getExitFunc()
        {
            return exitFunc;
        }

        
        public Wipers.WipeFunc getInitFunc()
        {
            return initFunc;
        }
    }
}
