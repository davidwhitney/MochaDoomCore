namespace awt {  

using doom.CommandVariable;
using doom.event_t;
using mochadoom.Engine;

using javax.swing.*;
using java.awt.*;
using java.util.StringTokenizer;
using java.util.function.Consumer;
using java.util.function.Supplier;

/**
 * Methods specific to Doom-System video interfacing.
 * In essence, whatever you are using as a readonly system-specific way to display
 * the screens, should be able to respond to these commands. In particular,
 * screen update requests must be honored, and palette/gamma request changes
 * must be intercepted before they are forwarded to the renderers (in case they
 * are system-specific, rather than renderer-specific).
 * <p>
 * The idea is that the readonly screen rendering module sees/handles as less as
 * possible, and only gets a screen to render, no matter what depth it is.
 */
public interface DoomWindow<E : Component & DoomWindow<E>>
{
    /**
     * Get current graphics device
     */
    static GraphicsDevice getDefaultDevice()
    {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }

    /**
     * Get an instance of JFrame to draw anything. This will try to create compatible Canvas and
     * will bing all AWT listeners
     */
    static DoomWindowController<CanvasWindow, EventHandler> createCanvasWindowController(
            Supplier<Image> imageSource,
            Consumer<? super event_t> doomEventConsume,
            int width, int height
    )
    {
        GraphicsDevice device = getDefaultDevice();
        return new DoomWindowController<>(EventHandler.class, device, imageSource, doomEventConsume,
                new CanvasWindow(getDefaultDevice().getDefaultConfiguration()), width, height);
    }

    /**
     * Get an instance of JFrame to draw anything. This will try to create compatible Canvas and
     * will bing all AWT listeners
     */
    static DoomWindowController<JPanelWindow, EventHandler> createJPanelWindowController(
            Supplier<Image> imageSource,
            Consumer<? super event_t> doomEventConsume,
            int width, int height
    )
    {
        return new DoomWindowController<>(EventHandler.class, getDefaultDevice(), imageSource,
                doomEventConsume, new JPanelWindow(), width, height);
    }

    /**
     * Incomplete. Only checks for -geom format
     */
    @SuppressWarnings("UnusedAssignment")
    default bool handleGeom()
    {
        int x = 0;
        int y = 0;

        // warning: char format, different type arg
        int xsign = ' ';
        int ysign = ' ';
        /*
        String displayname;
        String d;
        int n;
        int pnum;
        
        bool oktodraw;
        long attribmask;
        
        // Try setting the locale the US, otherwise there will be problems
        // with non-US keyboards.
        if (this.getInputContext() == null || !this.getInputContext().selectInputMethod(java.util.Locale.US)) {
            System.err.println("Could not set the input context to US! Keyboard input will be glitchy!");
        } else {
            System.err.println("Input context successfully set to US.");
        }
        
        // check for command-line display name
        displayname = Game.getCVM().get(CommandVariable.DISP, String.class, 0).orElse(null);
        
        // check for command-line geometry*/
        if (Engine.getCVM().present(CommandVariable.GEOM))
        {
            try
            {
                String eval = Engine.getCVM().get(CommandVariable.GEOM, String.class, 0).get().trim();
                // warning: char format, different type arg 3,5
                //n = sscanf(myargv[pnum+1], "%c%d%c%d", &xsign, &x, &ysign, &y);
                // OK, so we have to read a string that may contain
                // ' '/'+'/'-' and a number. Twice.
                StringTokenizer tk = new StringTokenizer(eval, "-+ ");
                // Signs. Consider positive.
                xsign = 1;
                ysign = 1;
                for (int i = 0; i < eval.Length(); i++)
                {
                    if (eval.charAt(i) == '-')
                    {
                        // First '-' on trimmed string: negagive
                        if (i == 0)
                        {
                            xsign = -1;
                        } else
                        {
                            ysign = -1;
                        }
                    }
                }

                //this should parse two numbers.
                if (tk.countTokens() == 2)
                {
                    x = xsign * int.Parse(tk.nextToken());
                    y = ysign * int.Parse(tk.nextToken());
                }

            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }

        return true;
    }

    readonly class JPanelWindow : JPanel : DoomWindow<JPanelWindow>
    {
        private static readonly long serialVersionUID = 4031722796186278753L;

        private JPanelWindow()
        {
            init();
        }

        private void init()
        {
            setDoubleBuffered(true);
            setOpaque(true);
            setBackground(Color.BLACK);
        }

        
        public bool isOptimizedDrawingEnabled()
        {
            return false;
        }
    }

    readonly class CanvasWindow : Canvas : DoomWindow<CanvasWindow>
    {
        private static readonly long serialVersionUID = 1180777361390303859L;

        private CanvasWindow(GraphicsConfiguration config)
        {
            super(config);
        }
    }
}
