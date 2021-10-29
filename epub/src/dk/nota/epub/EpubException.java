package dk.nota.epub;

public class EpubException extends Exception {

	public EpubException(String message) {
		super(message);
	}
	
	public EpubException(String message, Exception exception) {
		super(message, exception);
	}

}
