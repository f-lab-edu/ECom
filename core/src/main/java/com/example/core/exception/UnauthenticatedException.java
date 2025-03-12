package fan.graphic.core.exception;

public class UnauthenticatedException extends BaseException {
    public UnauthenticatedException(ResponseErrorCode code) {
        super(code);
    }
}
