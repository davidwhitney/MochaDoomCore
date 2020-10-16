namespace i {  

/**
 * Interface for Doom-to-System event handling methods
 *
 * @author Velktron
 */


public interface DoomEventInterface
{

    /**
     * The implementation is windowing subsystem-specific
     * e.g. DOS, XServer, AWT or Swing or whatever.
     */
    void GetEvent();

    bool mouseMoving();

    void setMouseMoving(bool mousMoving);
}
