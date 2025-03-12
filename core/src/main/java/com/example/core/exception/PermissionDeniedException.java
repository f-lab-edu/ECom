package fan.graphic.core.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionDeniedException extends BaseException {

    public PermissionDeniedException() {
        super(ResponseErrorCode.PERMISSION_DENIED);
    }

    public PermissionDeniedException(ResponseErrorCode code) {
        super(code);
    }
}
