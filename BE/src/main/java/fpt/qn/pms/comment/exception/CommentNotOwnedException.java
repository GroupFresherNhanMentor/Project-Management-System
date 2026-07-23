package fpt.qn.pms.comment.exception;

import fpt.qn.pms.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class CommentNotOwnedException extends AppException {
    public CommentNotOwnedException() {
        super(HttpStatus.FORBIDDEN, "You can only modify or delete your own comments");
    }
}
