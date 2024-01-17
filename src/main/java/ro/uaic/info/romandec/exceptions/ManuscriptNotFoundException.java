package ro.uaic.info.romandec.exceptions;

public class ManuscriptNotFoundException extends RuntimeException{
    public ManuscriptNotFoundException(String msg) {
        super(msg);
    }
}
