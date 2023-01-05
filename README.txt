This is a program based off the Atari game Asteroids done to test threading in java
and to explore "final class" project structure (I've found its not great amd packages are better)

The program is organized into a few helper classes (calc, gui, and utilities) containing the math and graphics classes,
and a game class do handle movement, player locations and laser firing, and asteroid spawning and colision

Everything is done using 2d points and lines alongside some basic algebra and geometry to do checks and translations

The different movements (keyboard reading and player move alongside astroid moving and laser moving) were put onto threads
most notably seperating the players movement and keyboard presses from the rest of the game, so movement can occur at any time
independent of the rest of the other moving things

many of the classes here are early prototypes of code I use today, more notably ExtendableThread, Panel, Frame, and Listener,
featured in (newer code) github.com/evanyip05/Storyboard2 (older code) .../APCSFinal-nonMatrix3DRenderer