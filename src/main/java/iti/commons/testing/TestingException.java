/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing;

public class TestingException extends RuntimeException {

    private static final long serialVersionUID = 8878219795112138064L;

    public TestingException(Exception e) {
        super(e);
    }

    public TestingException(String message) {
        super(message);
    }

}
