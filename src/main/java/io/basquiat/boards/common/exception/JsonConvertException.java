package io.basquiat.boards.common.exception;

/**
 * json convert 관련 에러 처리 exception
 * created by basquiat
 */
public class JsonConvertException extends RuntimeException {

    /**
     * Constructor with one parameter
     * @param message
     */
    public JsonConvertException(String message) {
        super(message);
    }

}
