/** Ben F Rayfield offers this "common" software to everyone opensource GNU LGPL */
package humanainet.common;

/** When tested, use @TestedIn(someClass.class). Until then use void.class to say needs testing. */
public @interface TestedIn{
	Class value();
}
